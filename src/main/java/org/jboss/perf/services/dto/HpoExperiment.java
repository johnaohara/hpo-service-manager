package org.jboss.perf.services.dto;

import java.util.List;

public record HpoExperiment(String name, Integer test_id, Integer total_trials,
        Integer parallel_trials,
        String value_type,
        String hpo_algo_impl,
        String objective_function,
        String slo_class,
        String direction,
        List<ExperimentTunable> tuneables) {

    public void addTuneable(ExperimentTunable tunable){
        tuneables.add(tunable);
    }
}
