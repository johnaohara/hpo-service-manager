package org.jboss.perf.services.dto;

import java.util.List;

public record RecommendedConfig(
        String experiment_name
        , String direction
        , String objective_function
        , Float optimalValue
        , List<TunableConfig> tunables ) {}
