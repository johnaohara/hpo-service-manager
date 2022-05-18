package org.jboss.perf.services.dto;

import java.util.ArrayList;
import java.util.HashMap;

public class ExperimentConfig {

    private String experimentName;
    private Integer horreumJobID;
    private JenkinsJob jenkinsJob;
    private QDupJob qDupJob;
    private Horreum horreum;
    private HpoExperiment hpoExperiment;

    public String getExperimentName() {
        return experimentName;
    }

    public Integer getHorreumJobID() {
        return horreumJobID;
    }

    public void defineJenkinsJob(String job, String job_url ) {
        this.jenkinsJob = new JenkinsJob(job, job_url, new HashMap<>());
    }

    public void defineQdupJob(String targetHost, String user, String scriptUrl) {
        this.qDupJob = new QDupJob(targetHost, user, scriptUrl, new HashMap<>());
    }

    public void defineHpoExperiment(String name, Integer test_id, Integer total_trials, Integer parallel_trials, String value_type,
                                    String hpo_algo_impl, String objective_function, String slo_class, String direction) {
        this.hpoExperiment = new HpoExperiment(name, test_id, total_trials, parallel_trials, value_type,
                hpo_algo_impl, objective_function, slo_class, direction, new ArrayList<>());
    }

    public void defineHorreum(Integer jobID){
        this.horreum = new Horreum(jobID, null); //TODO:: define auth for Horreum configuration
    }


    public JenkinsJob getJenkinsJob() {
        return jenkinsJob;
    }

    public QDupJob getqDupJob() {
        return qDupJob;
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
    public void setHorreumJobID(Integer jobID) {
        this.horreumJobID = jobID;
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
