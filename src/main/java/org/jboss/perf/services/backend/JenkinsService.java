package org.jboss.perf.services.backend;

import com.offbytwo.jenkins.JenkinsServer;
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
    public static final String JENKINS_URL = "http://localhost:8080/jenkins";
    public static final String USERNAME = "admin";
    public static final String PASSWORD = "password";

    private JenkinsServer jenkins;

    public void startup(@Observes StartupEvent startupEvent) {
        try {
            jenkins = new JenkinsServer(new URI(JENKINS_URL), USERNAME, PASSWORD);
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
                .filter(tunableConfig -> experimentDAO.jenkins.params.containsKey(tunableConfig))
                .collect(Collectors.toMap(tuneable -> experimentDAO.jenkins.params.get(tuneable.name()), tuneable -> tuneable.value()));

        try {

            //TODO: investigate passing a parameter file.
            // will need a HPO job that parses the parameter file and starts the correct jenkins jobs
            jenkins.getJob(experimentDAO.jenkins.job_url).build(jobParams);

        } catch (IOException e) {
            return e.getLocalizedMessage();
        }

        return null;
    }
}
