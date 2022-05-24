package org.jboss.perf.services.backend.runtime;

import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.JobWithDetails;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.runtime.annotations.ConfigItem;
import org.jboss.perf.data.entity.ExperimentDAO;
import org.jboss.perf.parser.ExperimentBuilder;
import org.jboss.perf.services.dto.TrialConfig;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
@RuntimeEnvironment(name = "JENKINS")
public class JenkinsService implements IRuntimeEnvironment {

    @ConfigItem( name = "hpo.jenkins.url")
    public String JENKINS_URL;

    @ConfigItem( name = "hpo.jenkins.username")
    public String USERNAME;

    @ConfigItem( name = "hpo.jenkins.password")
    public String PASSWORD;

    private JenkinsServer jenkins;

    public String newRun(ExperimentDAO experimentDAO, TrialConfig trialConfig) {
        connectToJenkins();
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

    @Override
    public void parseConfig(ExperimentBuilder builder, Object o) {

/*
        mappings.put("hpo~jenkins", new YamlParser.SectionParser<Map<String, Object>>(
                        (jenkinsMap, builder) -> numElements(jenkinsMap, 3) && containsKeys(jenkinsMap, "job")
                        , (jenkinsMap, builder) -> {
                    builder.addJenkinsJob(
                            jenkinsMap.get("job").toString()
                            , jenkinsMap.get("job_url").toString());

                    List<?> params = (List<?>) jenkinsMap.get("params");
                    for (Object paramMapping : params) {
                        builder.add("jenkins~params", paramMapping);
                    }
                })
        );

        mappings.put("jenkins~params", new YamlParser.SectionParser<Map<String, Object>>(
                (paramsMap, builder) -> numElements(paramsMap, 2) && containsKeys(paramsMap, "name", "tuneable")
                , (paramsMap, builder) -> builder.addJenkinsParamMapping(paramsMap.get("name").toString(), paramsMap.get("tuneable").toString()))
        );
*/


    }

    private void connectToJenkins(){
        try {
            if ( jenkins == null) {
                jenkins = new JenkinsServer(new URI(JENKINS_URL), USERNAME, PASSWORD);
            }
//            jenkins = new JenkinsServer(new URI(JENKINS_URL));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
