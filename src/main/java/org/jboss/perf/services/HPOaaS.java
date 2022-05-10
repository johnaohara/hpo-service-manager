package org.jboss.perf.services;

import io.hyperfoil.tools.horreum.entity.json.Run;
import org.jboss.logging.Logger;
import org.jboss.perf.data.entity.ExperimentDAO;
import org.jboss.perf.parser.ConfigParserException;
import org.jboss.perf.parser.YamlParser;
import org.jboss.perf.services.dto.ExperimentConfig;
import org.jboss.perf.services.dto.HpoMapper;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

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

    public void processResult(Run run){
        ExperimentDAO.findByTestId(run.testid);
//                .onItem().call()
//                .onFailure().call();
    }


    @Transactional
    public String createNewExperiment(String config){

        try {
            ExperimentConfig experimentConfig = yamlParser.parseYaml(config);
            LOG.infof("New experiement: %s", experimentConfig);

            //1. verify that experiment does not already exist
            if( hpoService.experimentExists(experimentConfig.getExperimentName()) ){
                String error = "Experiment already exists: %s".formatted(experimentConfig.getExperimentName());
                LOG.warn(error);
                return error;
            }

            //2. verify that config is valid for lab env
                //a. Verify job exists in Jenkins
                //c. verify that Horreum job is configured

            //2. persist new experiment configuration
                //a. STATUS is NEW
            ExperimentDAO experiment = HpoMapper.INSTANCE.mapDAO(experimentConfig.getHpoExperiment());
            experiment.state = ExperimentDAO.State.NEW;
            experiment.name = experimentConfig.getExperimentName(); //TODO: check automatic mapping
            experiment.persist();

            //3. Set up experiment
                //a. create new experiment in HPO service
            String result = hpoService.newExperiment(experimentConfig.getHpoExperiment());
            if( result != null){
                //Failed to create hpo experiment
                return "Failed to create new experiment in HPO service: ".concat(result);
            }

            //4. Update persisted experiment state to READY
            experiment.state = ExperimentDAO.State.READY;
            experiment.persist();

            //4. Inform result
            return  null;

        } catch (ConfigParserException e) {
            e.printStackTrace();
            return "Could not parse config: ". concat(e.getMessage());
        }

    }

}
