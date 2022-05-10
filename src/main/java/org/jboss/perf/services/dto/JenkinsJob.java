package org.jboss.perf.services.dto;

import java.util.Map;

public record JenkinsJob(String job, String job_url, Map<String, String> params) {
    public void addParam(String key, String value){
        params.put(key, value);
    }
}
