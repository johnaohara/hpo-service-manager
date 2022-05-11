package org.jboss.perf.services.backend;

import io.kruize.hpo.*;
import io.quarkus.grpc.GrpcClient;
import org.jboss.logging.Logger;
import org.jboss.perf.services.dto.HpoExperiment;
import org.jboss.perf.services.dto.HpoMapper;

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

    public HpoExperiment getExperimentByName(String name) {
        ExperimentNameParams nameParams = ExperimentNameParams.newBuilder().setExperimentName(name).build();
        try {
            ExperimentDetails experimentDetails = blockingHpoService.getExperimentDetails(nameParams);
            return HpoMapper.INSTANCE.map(experimentDetails);
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
        return HpoMapper.INSTANCE.map(trialConfig);
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
                .setValue(trial.doubleValue())
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


        return HpoMapper.INSTANCE.map(newTrialConfig);

    }

    public String newExperiment(HpoExperiment hpoExperiment) {
        ExperimentDetails experimentDetails = HpoMapper.INSTANCE.map(hpoExperiment);

        try {
            blockingHpoService.newExperiment(experimentDetails);
            return null;
        } catch (io.grpc.StatusRuntimeException rte) {
            logger.error(rte.getLocalizedMessage());
            return rte.getLocalizedMessage();
        }
    }
}
