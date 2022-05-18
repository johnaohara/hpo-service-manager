package org.jboss.perf.services.dto;

import java.util.Map;

public record QDupJob(String targetHost, String username, String scriptUrl, Map<String, String> params) {
    public void addParam(String key, String value){
        params.put(key, value);
    }
}
