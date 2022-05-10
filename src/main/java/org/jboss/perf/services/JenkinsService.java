package org.jboss.perf.services;

import com.offbytwo.jenkins.JenkinsServer;
import io.quarkus.runtime.StartupEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import java.net.URI;
import java.net.URISyntaxException;

@ApplicationScoped
public class JenkinsService {

    //TODO:: configurable
    public static final String JENKINS_URL = "http://localhost:8080/jenkins";
    public static final String USERNAME = "admin";
    public static final String PASSWORD = "password";

    private JenkinsServer jenkins;

    public void startup(@Observes StartupEvent startupEvent){
        try {
            jenkins = new JenkinsServer(new URI(JENKINS_URL), USERNAME, PASSWORD);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
