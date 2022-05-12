package org.jboss.perf.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ValueNode;
import io.hyperfoil.tools.horreum.api.RunService;
import io.hyperfoil.tools.horreum.entity.json.Run;
import org.jboss.logging.Logger;
import org.jboss.perf.data.entity.ExperimentDAO;
import org.jboss.perf.data.entity.HorreumDAO;
import org.jboss.perf.parser.ConfigParserException;
import org.jboss.perf.parser.YamlParser;
import org.jboss.perf.services.backend.HorreumService;
import org.jboss.perf.services.backend.HpoService;
import org.jboss.perf.services.backend.JenkinsService;
import org.jboss.perf.services.dto.ExperimentConfig;
import org.jboss.perf.services.dto.HpoMapper;
import org.jboss.perf.services.dto.TrialConfig;

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
    JenkinsService jenkinsService;

    @Inject
    YamlParser yamlParser;

    @Transactional
    public String processResult(Run run) {
        ExperimentDAO experimentDAO = ExperimentDAO.findByTestId(run.testid);

        if (experimentDAO == null) {
            return logFailureMsg("Could not find experiment id: ".concat(run.testid.toString()));
        }

        RunService.RunSummary runSummary = horreumService.getRunSummary(run.id);

        if ( runSummary == null ){
            return "Could not find datasets for run: ".concat(run.id.toString());
        }

        final ObjectMapper objectMapper = new ObjectMapper();

        Map<Integer, Map<String, ValueNode>> datasetLabelValues = new HashMap<>();
        runSummary.datasets.forEach( jsonNode -> datasetLabelValues.put(jsonNode.intValue(), horreumService.queryDataSetLabels(jsonNode.asInt())));


//        Map<Integer, Map<String, ValueNode>> datasetLabelValues = runSummary.datasets.stream().collect(
//                Collectors.toMap(
//                        datset -> datset.id
//                        , dataset -> horreumService.queryDataSetLabels(dataset.id)
//                ));

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

        //update result value
        hpoService.newResult(experimentDAO.name, objectiveFunctionValue, experimentDAO.currentTrial);

        //generate new trial
        experimentDAO.currentTrial = hpoService.newTrial(experimentDAO.name);
        experimentDAO.persist();

        //get new trial config
        TrialConfig trialConfig = hpoService.getTrialConfig(experimentDAO.name, experimentDAO.currentTrial);

        String jenkinsRuns = jenkinsService.newRun(experimentDAO, trialConfig);

        if (jenkinsRuns != null) {
            return logFailureMsg("Failed to start jenkins run: ".concat(jenkinsRuns));
        }

        return null;
    }


    @Transactional
    public String createNewExperiment(String config) {

        try {
            ExperimentConfig experimentConfig = yamlParser.parseYaml(config);
            LOG.infof("New experiement: %s", experimentConfig);

            //1. verify that experiment does not already exist
            if (hpoService.experimentExists(experimentConfig.getExperimentName())) {
                String error = "Experiment already exists in HPO service: %s".formatted(experimentConfig.getExperimentName());
                LOG.warn(error);
                return error;
            }

            //2. TODO:: verify that config is valid for lab env
            //a. TODO:: Verify job exists in Jenkins
            //c. TODO:: verify that Horreum job is configured

            //2. persist new experiment configuration
            //a. STATUS is NEW
            ExperimentDAO experiment = HpoMapper.INSTANCE.mapDAO(experimentConfig.getHpoExperiment());
            experiment.state = ExperimentDAO.State.NEW;
            experiment.name = experimentConfig.getExperimentName(); //TODO: check automatic mapping

            experiment.horreum =  HpoMapper.INSTANCE.map(experimentConfig.getHorreum());
            experiment.jenkins = HpoMapper.INSTANCE.map(experimentConfig.getJenkinsJob());

            experiment.persist();

            //3. Set up experiment
            //a. create new experiment in HPO service
            String result = hpoService.newExperiment(experimentConfig.getHpoExperiment());
            if (result != null) {
                //Failed to create hpo experiment
                return "Failed to create new experiment in HPO service: ".concat(result);
            }

            //4. Update persisted experiment state to READY
            experiment.state = ExperimentDAO.State.READY;
            experiment.persist();

            //4. Inform result
            return null;

        } catch (ConfigParserException e) {
            e.printStackTrace();
            return "Could not parse config: ".concat(e.getMessage());
        }

    }

    public String startExperiment(ExperimentDAO experiment) {

        String jenkinsJobStatus = jenkinsService.newRun(experiment, hpoService.getExperimentConfig(experiment.name, experiment.currentTrial));

        if ( jenkinsJobStatus != null ){
            return logFailureMsg("Could not start jenkins job for experiment ".concat(experiment.name).concat(": ").concat(jenkinsJobStatus));
        }

        return null;
    }


    @Transactional
    public List<String> getRunningExperiments() {

        List<String> experimentNames = null;
        try (Stream<ExperimentDAO> experiments = ExperimentDAO.streamAll()) {
            experimentNames = experiments
                    .map(e -> e.name)
                    .collect(Collectors.toList());
        }
        return experimentNames;

//                .map(experiment -> experiment).collect(Collectors.toList());
    }

    public String rerunExperiemnt(String experimentName) {
        ExperimentDAO experimentDAO = ExperimentDAO.find("name", experimentName).firstResult();
        if( experimentDAO == null) {
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

        if ( !experimentDAO.state.nextState().equals(stateVal) ){
            return logFailureMsg("Can not transition experiment State from: ".concat(experimentDAO.state.toString()).concat(" to: ").concat(state.toString()));
        }

        switch (stateVal){
            case RUNNING -> { //TODO: check that error msg is not swallowed
                String result = startExperiment(experimentDAO);
                if ( result == null) {
                    experimentDAO.state =experimentDAO.state.nextState();
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

}
