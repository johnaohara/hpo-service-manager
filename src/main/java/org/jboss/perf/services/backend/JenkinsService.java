package org.jboss.perf.services.backend;

import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.JobWithDetails;
import com.offbytwo.jenkins.model.QueueReference;
import io.quarkus.runtime.StartupEvent;
import org.jboss.perf.data.entity.ExperimentDAO;
import org.jboss.perf.services.dto.TrialConfig;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class JenkinsService {

    //TODO:: configurable
    public static final String JENKINS_URL = "http://localhost:18080/";
    public static final String USERNAME = "admin";
    public static final String PASSWORD = "11f44a7b79bafc0f35babdeebee1030de4";

    private JenkinsServer jenkins;

    public void startup(@Observes StartupEvent startupEvent) {
        try {
            jenkins = new JenkinsServer(new URI(JENKINS_URL), USERNAME, PASSWORD);
//            jenkins = new JenkinsServer(new URI(JENKINS_URL));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public String newRun(ExperimentDAO experimentDAO, TrialConfig trialConfig) {

        /*
         * TODO:: Either need to;
         *  a - expose one-to-one direct mapping of tuneables -> params
         *  b - create dynamically mappings of tunable -> params
         *  c - pass to jenkins job to parse param mappings
         *
         *  supporting (b) will be more versatile, but by default (a) will allow for less config
         * */

        Map<String, String> jobParams = trialConfig.tunableConfigs().stream()
                .filter(tunableConfig -> experimentDAO.jenkins.params.containsKey(tunableConfig.name()))
                .collect(Collectors.toMap(tuneable -> experimentDAO.jenkins.params.get(tuneable.name()), tuneable -> tuneable.value().toString()));

        try {

            //TODO: investigate passing a parameter file.
            // will need a HPO job that parses the parameter file and starts the correct jenkins jobs
            JobWithDetails job = jenkins.getJob(experimentDAO.jenkins.job_url);
            if ( job == null){
                return "Could not find job: ".concat(experimentDAO.jenkins.job_url);
            }
            job.build(jobParams);

        } catch (IOException e) {
            return e.getLocalizedMessage();
        }

        return null;
    }
}
