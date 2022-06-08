/*
 * Copyright MapStruct Authors.
 *
 * Licensed under the Apache License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package org.jboss.perf.services.dto;

import io.kruize.hpo.ExperimentDetails;
import io.kruize.hpo.RecommendedConfigReply;
import org.jboss.perf.data.entity.*;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;

/**
 * @author Thomas Kratz
 */
@Mapper(collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface HpoMapper {

    public static HpoMapper INSTANCE (){
        return Mappers.getMapper(HpoMapper.class);
    }


    @Mapping(source = "hpo_algo_impl", target = "hpoAlgoImpl")
    @Mapping(source = "name", target = "experimentName")
    @Mapping(source = "objective_function", target = "objectiveFunction")
    @Mapping(source = "parallel_trials", target = "parallelTrials")
    @Mapping(source = "slo_class", target = "sloClass")
    @Mapping(source = "total_trials", target = "totalTrials")
    @Mapping(source = "tuneables", target = "tuneablesList")
    @Mapping(source = "value_type", target = "valueType")
    ExperimentDetails map(HpoExperiment experimentDetails);

    @Mapping(source = "hpoAlgoImpl", target = "hpo_algo_impl")
    @Mapping(source = "experimentName", target = "name")
    @Mapping(source = "objectiveFunction", target = "objective_function")
    @Mapping(source = "parallelTrials", target = "parallel_trials")
    @Mapping(source = "sloClass", target = "slo_class")
    @Mapping(source = "totalTrials", target = "total_trials")
    @Mapping(source = "tuneablesList", target = "tuneables")
    @Mapping(source = "valueType", target = "value_type")
    HpoExperiment map(ExperimentDetails experimentDetails);

    ExperimentDAO mapDAO(HpoExperiment experimentDetails);

    @Mapping(source = "name", target = "name")
    @Mapping(source = "total_trials", target = "total_trials")
    @Mapping(source = "currentTrial", target = "current_trial")
    @Mapping(source = "recommended", target = "recommendedConfig")
    HpoExperimentDetails mapDAO(ExperimentDAO experiment);


    @Mapping(source = "reply.optimalValue.objectiveFunction", target = "objective_function")
    @Mapping(source = "reply.optimalValue.value", target = "optimalValue")
    @Mapping(source = "tunablesList", target = "tunables")
    RecommendedConfig map(RecommendedConfigReply reply);

    @Mapping(source = "valueType", target = "value_type")
    @Mapping(source = "lowerBound", target = "lower_bound")
    @Mapping(source = "upperBound", target = "upper_bound")
    ExperimentTunable map(ExperimentDetails.Tunable value);

    @Mapping(source = "value_type", target = "valueType")
    @Mapping(source = "lower_bound", target = "lowerBound")
    @Mapping(source = "upper_bound", target = "upperBound")
    ExperimentDetails.Tunable map(ExperimentTunable perm);

    @Mapping(source = "configList", target = "tunableConfigs")
    TrialConfig map(io.kruize.hpo.TrialConfig trialConfig);

    TunableConfig map(io.kruize.hpo.TunableConfig tunableConfig);

    HorreumDAO map(Horreum value);

    Horreum map(HorreumDAO value);

    JenkinsDAO map(JenkinsJob value);

    JenkinsJob map(JenkinsDAO value);

    @Mapping(source = "optimalValue", target = "value")
    @Mapping(source = "tunables", target = "tunables")
    TrialResultDAO mapDAO(RecommendedConfig recommendedConfig);

    @Mapping(source = "value", target = "optimalValue")
    @Mapping(source = "tunables", target = "tunables")
    RecommendedConfig mapDAO(TrialResultDAO recommendedConfig);

    @Mapping(source = "name", target = "tunable")
    @Mapping(source = "value", target = "value")
    TunableValueDAO mapDAO(TunableConfig tunables);

    @Mapping(source = "tunable", target = "name")
    @Mapping(source = "value", target = "value")
    TunableConfig mapDAO(TunableValueDAO tunables);

//    @Mapping(source = "targetHost", target = "targetHost")
//    @Mapping(source = "targetHost", target = "targetHost")
//    @Mapping(source = "targetHost", target = "targetHost")
    QDupDAO map(QDupJob getqDupJob);
}
