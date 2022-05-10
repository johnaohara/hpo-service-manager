package org.jboss.perf.services.dto;

import java.util.ArrayList;
import java.util.HashMap;

public class ExperimentConfig {

    private String experimentName;
    private JenkinsJob jenkinsJob;
    private Horreum horreum;
    private HpoExperiment hpoExperiment;

    public String getExperimentName() {
        return experimentName;
    }

    public void defineJenkinsJob(String job, String job_url ) {
        this.jenkinsJob = new JenkinsJob(job, job_url, new HashMap<>());
    }

    public void defineHpoExperiment(String name, Integer total_trials, Integer parallel_trials, String value_type,
                                    String hpo_algo_impl, String objective_function, String slo_class, String direction) {
        this.hpoExperiment = new HpoExperiment(name, total_trials, parallel_trials, value_type,
                hpo_algo_impl, objective_function, slo_class, direction, new ArrayList<>());
    }

    public void defineHorreum(String url, String job, Integer jobID, String metric){
        this.horreum = new Horreum(url, job, jobID, null, metric); //TODO:: define auth for Horreum configuration
    }


    public JenkinsJob getJenkinsJob() {
        return jenkinsJob;
    }

    public Horreum getHorreum() {
        return horreum;
    }

    public HpoExperiment getHpoExperiment() {
        return hpoExperiment;
    }

    public void setExperimentName(String experimentName) {
        this.experimentName = experimentName;
    }



    @Override
    public String toString() {
        return "ExperimentConfg{" +
                "experiment=" + experimentName +
                ", jenkins=" + jenkinsJob +
                ", horreum=" + horreum +
                ", hpo=" + hpoExperiment +
                '}';
    }
}
