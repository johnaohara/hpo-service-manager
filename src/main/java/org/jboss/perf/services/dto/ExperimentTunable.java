package org.jboss.perf.services.dto;

public record ExperimentTunable(String name,
                                String value_type,
                                Double lower_bound,
                                Double upper_bound,
                                Double step) {
}
