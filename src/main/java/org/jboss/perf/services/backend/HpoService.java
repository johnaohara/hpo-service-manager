package org.jboss.perf.services.backend;

import io.kruize.hpo.*;
import io.quarkus.grpc.GrpcClient;
import org.jboss.logging.Logger;
import org.jboss.perf.data.entity.ExperimentDAO;
import org.jboss.perf.services.dto.HpoExperiment;
import org.jboss.perf.services.dto.HpoExperimentDetails;
import org.jboss.perf.services.dto.HpoMapper;
import org.jboss.perf.services.dto.RecommendedConfig;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class HpoService {

    private static final Logger logger = Logger.getLogger(HpoService.class);

    @GrpcClient("hpo")
    HpoServiceGrpc.HpoServiceBlockingStub blockingHpoService;

    public List<String> getRunningExperiments() {
        ExperimentsListReply reply = blockingHpoService.experimentsList(ExperimentsListParams.newBuilder().build());

        List<String> response = reply.getExperimentList().asByteStringList().stream().map(val -> val.toStringUtf8()).collect(Collectors.toList());

        return response;
    }

    public HpoExperimentDetails getExperimentStatusByName(String name) {
        ExperimentDAO experiment = ExperimentDAO.find("name", name).firstResult();

        if ( experiment != null) {
            HpoExperimentDetails response = HpoMapper.INSTANCE().mapDAO(experiment);
            return response;
        }

        return null;
    }

    public void stopExperimentByName(String name) {

        try {
            ExperimentNameParams nameParams = ExperimentNameParams.newBuilder().setExperimentName(name).build();
            blockingHpoService.stopExperiment(nameParams);
        } catch (Exception e){
        }
    }


    public RecommendedConfig getExperimentRecommendedConfig(String name) {
        ExperimentDAO experiment = ExperimentDAO.find("name", name).firstResult();

        if ( experiment != null) {
            RecommendedConfig response = HpoMapper.INSTANCE().mapDAO(experiment.recommended);
            return response;
        }

        return null;
    }

    public HpoExperiment getExperimentByName(String name) {
        ExperimentNameParams nameParams = ExperimentNameParams.newBuilder().setExperimentName(name).build();
        try {
            ExperimentDetails experimentDetails = blockingHpoService.getExperimentDetails(nameParams);
            return HpoMapper.INSTANCE().map(experimentDetails);
        } catch (io.grpc.StatusRuntimeException rte) {
            switch (rte.getStatus().getCode()) {
                case NOT_FOUND -> logger.infof("Could not find experiment running in HPO: %s", name);
            }

            return null;
        }
    }

    public org.jboss.perf.services.dto.TrialConfig getExperimentConfig(String name, Integer trial) {
        ExperimentTrial expTrial = ExperimentTrial.newBuilder().setExperimentName(name).setTrial(trial).build();
        io.kruize.hpo.TrialConfig trialConfig = blockingHpoService.getTrialConfig(expTrial);
        return HpoMapper.INSTANCE().map(trialConfig);
    }

    public boolean experimentExists(String experimentName) {
        return !experimentDoesNotExists(experimentName);
    }

    public boolean experimentDoesNotExists(String experimentName) {
        return this.getExperimentByName(experimentName) == null;
    }

    public void newResult(String experimentName, String result, Integer trial) {

        ExperimentTrialResult experimentTrialResult = ExperimentTrialResult.newBuilder()
                .setExperimentName(experimentName)
                .setResult(ExperimentTrialResult.Result.SUCCESS)
                .setValue(Float.parseFloat(result))
                .setValueType("double") //TODO:: pick up from config
                .setTrial(trial).build();
        //save result
        blockingHpoService.updateTrialResult(experimentTrialResult);

    }
    public Integer newTrial(String experimentName) {
    //get next trial
        NewExperimentsReply experimentsReply =  blockingHpoService.generateNextConfig(
                ExperimentNameParams.newBuilder()
                        .setExperimentName(experimentName)
                        .build()
        );

        return experimentsReply.getTrialNumber();
    }

    public org.jboss.perf.services.dto.TrialConfig getTrialConfig(String experimentName, Integer trial){

        //get trial config
        TrialConfig newTrialConfig = blockingHpoService.getTrialConfig(
                ExperimentTrial.newBuilder()
                        .setExperimentName(experimentName)
                        .setTrial(trial)
                        .build()
        );


        org.jboss.perf.services.dto.TrialConfig trialConfig = HpoMapper.INSTANCE().map(newTrialConfig);
        logger.infof("Received new config for trial: %s", trial);
        trialConfig.tunableConfigs().forEach(config -> logger.infof("%s: %s", config.name(), config.value()));

        return trialConfig;

    }

    public String newExperiment(HpoExperiment hpoExperiment) {
        ExperimentDetails experimentDetails = HpoMapper.INSTANCE().map(hpoExperiment);

        try {
            //TODO:: catch RPC exceptions here
            blockingHpoService.newExperiment(experimentDetails);
            return null;
        } catch (io.grpc.StatusRuntimeException rte) {
            logger.error(rte.getLocalizedMessage());
            return rte.getLocalizedMessage();
        }
    }

    public RecommendedConfig getRecommendedConfig(String experimentName){
        try {
            ExperimentNameParams experimentNameParams = ExperimentNameParams.newBuilder().setExperimentName(experimentName).build();
            RecommendedConfigReply configReply = blockingHpoService.getRecommendedConfig(experimentNameParams);
            RecommendedConfig  config = HpoMapper.INSTANCE().map(configReply);
            return config;
        } catch (io.grpc.StatusRuntimeException rte) {
            logger.error(rte.getLocalizedMessage());
        }
        return null;
    }
}
