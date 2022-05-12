package org.jboss.perf.services.dto;

public record Horreum (
        String job,
        Integer jobID,
        String auth,
        String metric
) {}
