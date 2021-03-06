package org.jboss.perf.services;

import com.fasterxml.jackson.databind.node.ValueNode;
import io.hyperfoil.tools.horreum.api.RunService;
import io.hyperfoil.tools.horreum.entity.json.Run;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.OnOverflow;
import org.jboss.logging.Logger;
import org.jboss.perf.Util;
import org.jboss.perf.api.ApiResult;
import org.jboss.perf.api.dto.RunningExperiment;
import org.jboss.perf.data.entity.ExperimentDAO;
import org.jboss.perf.data.entity.TrialResultDAO;
import org.jboss.perf.data.entity.TunableValueDAO;
import org.jboss.perf.parser.ConfigParserException;
import org.jboss.perf.parser.YamlParser;
import org.jboss.perf.services.backend.HorreumService;
import org.jboss.perf.services.backend.HpoService;
import org.jboss.perf.services.backend.runtime.IRuntimeEnvironment;
import org.jboss.perf.services.backend.runtime.RuntimeProducer;
import org.jboss.perf.services.dto.*;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationScoped
public class HPOaaS {

    private static final Logger LOG = Logger.getLogger(HPOaaS.class);


    @Inject
    HpoService hpoService;

    @Inject
    HorreumService horreumService;

    @Inject
    RuntimeProducer runtimeProducer;

    @Inject
    YamlParser yamlParser;

    @Channel("experiment-details-out")
    @OnOverflow(OnOverflow.Strategy.DROP)
    Emitter<HpoExperimentDetails> experimentDetailsEmitter;

    @Channel("experiments-summary-out")
    Emitter<List<RunningExperiment>> experimentSummaryEmitter;

    @Transactional
    public String processResult(Run run) {
        ExperimentDAO experimentDAO = ExperimentDAO.findByTestId(run.testid);

        if (experimentDAO == null) {
            return logFailureMsg("Could not find experiment id: ".concat(run.testid.toString()));
        }

        if ( !experimentDAO.state.equals(ExperimentDAO.State.RUNNING)){
            return logFailureMsg("Experiment not currently running!: ".concat(experimentDAO.state.toString()));
        }

        //force re-calc of datasets - need to figure out how to receive updates asynchronously from Horreum
        horreumService.recalculateDatasets(run.id);

        RunService.RunSummary runSummary = horreumService.getRunSummary(run.id);

        if (runSummary == null || runSummary.datasets == null || runSummary.datasets.size() == 0) {
            return "Could not find datasets for run: ".concat(run.id.toString());
        }

        Map<Integer, Map<String, ValueNode>> datasetLabelValues = new HashMap<>();
        runSummary.datasets.forEach(jsonNode -> datasetLabelValues.put(jsonNode.intValue(), horreumService.queryDataSetLabels(jsonNode.asInt())));

        if (datasetLabelValues == null || datasetLabelValues.size() < 1) {
            return logFailureMsg("Could not find experiment id: ".concat(run.testid.toString()));
        }

        //TODO:: handle multiple datasets
        Integer datasetID = datasetLabelValues.keySet().stream().findFirst().orElse(-1);

        if (datasetID == -1) {
            return logFailureMsg("Could not find dataset");
        }

        List<ValueNode> values = datasetLabelValues.get(datasetID).entrySet().stream()
                .filter(entry -> experimentDAO.objective_function.equals(entry.getKey()))
                .map(entry -> entry.getValue())
                .collect(Collectors.toList());

        if (values == null || values.size() == 0) {
            return logFailureMsg("failed to extract values for objective function: ".concat(experimentDAO.objective_function));
        }

        //TODO:: handle multiple values
        String objectiveFunctionValue = values.stream().findFirst().get().asText();

        if ( objectiveFunctionValue.equals("null")){
            return logFailureMsg("failed to extract values for objective function: ".concat(experimentDAO.objective_function));
        }
        //update result value
        hpoService.newResult(experimentDAO.name, objectiveFunctionValue, experimentDAO.currentTrial);

        //persist trial result
        TrialResultDAO trialResult = experimentDAO.trialHistory.get(experimentDAO.currentTrial);
        if ( trialResult == null ) {
            return logFailureMsg("failed to extract trial result for current trial: ".concat(experimentDAO.currentTrial.toString()));
        }

        if ( objectiveFunctionValue != null) {
            trialResult.value = Float.valueOf(objectiveFunctionValue);
        }
        trialResult.persist();

        //generate new trial
        experimentDAO.currentTrial = hpoService.newTrial(experimentDAO.name);
        experimentDAO.persist();

        HpoExperimentDetails updatedExperiment = HpoMapper.INSTANCE().mapDAO(experimentDAO);
        experimentDetailsEmitter.send(updatedExperiment);

        experimentSummaryEmitter.send(getRunningExperiments());

        //get new trial config
        if (!(experimentDAO.currentTrial == -1)) {
            //get new trial config
            TrialConfig trialConfig = hpoService.getTrialConfig(experimentDAO.name, experimentDAO.currentTrial);

            TrialResultDAO newTrialResult = new TrialResultDAO();

            List<TunableValueDAO> newTunables = trialConfig.tunableConfigs().stream().map(config -> new TunableValueDAO(config.name(), config.value())).collect(Collectors.toList());
            newTrialResult.tunables.addAll(newTunables);

            experimentDAO.trialHistory.put(experimentDAO.currentTrial, newTrialResult);

            //TODO:: clean this up so we can register multiple runtime environments
            IRuntimeEnvironment runtimeEnvironment = getRuntime( experimentDAO.jenkins != null ? "jenkins" : "qdup");
            String jobStatus = runtimeEnvironment.newRun(experimentDAO, trialConfig);

            if (jobStatus != null) {
                return logFailureMsg("Failed to start run: ".concat(jobStatus));
            }
        } else { //experiemnt has finished
            LOG.infof("Experiment: %s has finished", experimentDAO.name);
            //GET recommended config
            RecommendedConfig recommendedConfig = this.getRecomendedConfig(experimentDAO.name);

            TrialResultDAO recommended = HpoMapper.INSTANCE().mapDAO(recommendedConfig);
            //TODO:: save recommended config
            experimentDAO.currentTrial = experimentDAO.total_trials;
            experimentDAO.recommended = recommended;
            experimentDAO.state = ExperimentDAO.State.FINISHED;

            experimentDAO.persist();
            LOG.info(recommendedConfig);
        }
        return null;
    }


    @Transactional
    public ApiResult createNewExperiment(String config) {

        try {
            ExperimentConfig experimentConfig = yamlParser.parseYaml(config);

            LOG.infof("New experiement: %s", Util.prettyPrintExperiment(experimentConfig.toString() ));

            //1. verify that experiment does not already exist
            if (hpoService.experimentExists(experimentConfig.getExperimentName())) {
                hpoService.stopExperimentByName(experimentConfig.getExperimentName());
//                String error = "Experiment already exists in HPO service: %s".formatted(experimentConfig.getExperimentName());
//                LOG.warn(error);
//                return ApiResult.failure(error);
            }

            //2. TODO:: verify that config is valid for lab env
            //a. TODO:: Verify job exists in Jenkins
            //c. TODO:: verify that Horreum job is configured

            //2. persist new experiment configuration
            //a. STATUS is NEW
            ExperimentDAO experiment = HpoMapper.INSTANCE().mapDAO(experimentConfig.getHpoExperiment());
            experiment.state = ExperimentDAO.State.NEW;
            experiment.name = experimentConfig.getExperimentName(); //TODO: check automatic mapping

            experiment.horreum = HpoMapper.INSTANCE().map(experimentConfig.getHorreum());
            if ( experimentConfig.getJenkinsJob() != null ) {
                experiment.jenkins = HpoMapper.INSTANCE().map(experimentConfig.getJenkinsJob());
            }
            if ( experimentConfig.getqDupJob() != null) {
                experiment.qDup = HpoMapper.INSTANCE().map(experimentConfig.getqDupJob());
            }

            experiment.persist();

            //3. Set up experiment
            //a. create new experiment in HPO service
            String result = hpoService.newExperiment(experimentConfig.getHpoExperiment());
            if (result != null) {
                //Failed to create hpo experiment
                return ApiResult.failure("Failed to create new experiment in HPO service: ".concat(result));
            }

            //4. Update persisted experiment state to READY
            experiment.state = ExperimentDAO.State.READY;
            experiment.persist();

            //4. Inform result
            return ApiResult.success(experiment.name);

        } catch (ConfigParserException e) {
            e.printStackTrace();
            return ApiResult.failure("Could not parse config: ".concat(e.getMessage()));
        }

    }

    public String startExperiment(ExperimentDAO experiment) {
        TrialConfig trialConfig = hpoService.getExperimentConfig(experiment.name, experiment.currentTrial);

        TrialResultDAO trialResult = new TrialResultDAO();

        List<TunableValueDAO> newTunables = trialConfig.tunableConfigs().stream().map(config -> new TunableValueDAO(config.name(), config.value())).collect(Collectors.toList());
        trialResult.tunables.addAll(newTunables);

        trialResult.persist();

        experiment.trialHistory.put(experiment.currentTrial, trialResult);
        experiment.persist();


        IRuntimeEnvironment runtimeEnvironment = getRuntime( experiment.jenkins != null ? "jenkins" : "qdup");

        String jobStatus = runtimeEnvironment.newRun(experiment, trialConfig);

        if (jobStatus != null) {
            return logFailureMsg("Could not start job for experiment ".concat(experiment.name).concat(": ").concat(jobStatus));
        }

        return null;
    }

    public RecommendedConfig getRecomendedConfig(String experimentName){
        return  hpoService.getRecommendedConfig(experimentName);
    }

    @Transactional
    public List<RunningExperiment> getRunningExperiments() {

        List<RunningExperiment> runningExperiments = null;
        try (Stream<ExperimentDAO> experiments = ExperimentDAO.streamAll()) {
            runningExperiments = experiments
                    .map(e -> new RunningExperiment(e.name, e.total_trials, e.currentTrial, e.state))
                    .collect(Collectors.toList());
        }
        return runningExperiments;

    }

    @Transactional
    public void deleteExperiment(String name) {
        try {
            hpoService.stopExperimentByName(name);
        } catch (Exception e){
            //do nothing
        }
        ExperimentDAO.delete("name", name);
    }

    public String rerunExperiemnt(String experimentName) {
        ExperimentDAO experimentDAO = ExperimentDAO.find("name", experimentName).firstResult();
        if (experimentDAO == null) {
            return "Could not find experiment: ".concat(experimentName);
        }
        return startExperiment(experimentDAO);
    }


    @Transactional
    public String changeExperimentState(String experimentName, String state) {

        ExperimentDAO.State stateVal = ExperimentDAO.State.valueOf(state);
        ExperimentDAO experimentDAO = ExperimentDAO.find("name", experimentName).firstResult();

        if (experimentDAO == null) {
            return logFailureMsg("Could not find experiment: ".concat(experimentName));
        }

//        if (!experimentDAO.state.nextState().equals(stateVal)) {
//            return logFailureMsg("Can not transition experiment State from: ".concat(experimentDAO.state.toString()).concat(" to: ").concat(state.toString()));
//        }

        switch (stateVal) {
            case NEW -> { //TODO check that experiment is in HPO
//                String result = startExperiment(experimentDAO);
//                if (result == null) {
//                    experimentDAO.state = experimentDAO.state.nextState().nextState();
//                    experimentDAO.persist();
//                } else {
//                    return result;
//                }
            }
            case PAUSED -> {
                experimentDAO.state = ExperimentDAO.State.PAUSED;
                experimentDAO.persist();
            }
            case RUNNING -> { //TODO: check that error msg is not swallowed
                String result = startExperiment(experimentDAO);
                if (result == null) {
                    experimentDAO.state = experimentDAO.state.nextState();
                    experimentDAO.persist();
                } else {
                    return result;
                }
            }
        }


        return null;
    }

    private String logFailureMsg(String msg) {
        LOG.error(msg);
        return msg;
    }


    private IRuntimeEnvironment getRuntime(String runtime){
        return runtimeProducer.getRuntime(runtime);
    }
}
