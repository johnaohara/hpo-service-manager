/*
 * Copyright MapStruct Authors.
 *
 * Licensed under the Apache License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */
package org.jboss.perf.services.dto;

import io.kruize.hpo.ExperimentDetails;
import org.jboss.perf.data.entity.ExperimentDAO;
import org.jboss.perf.data.entity.HorreumDAO;
import org.jboss.perf.data.entity.JenkinsDAO;
import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.factory.Mappers;

/**
 * @author Thomas Kratz
 */
@Mapper(collectionMappingStrategy = CollectionMappingStrategy.ADDER_PREFERRED,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface HpoMapper {

    HpoMapper INSTANCE = Mappers.getMapper(HpoMapper.class);


    @Mapping(source = "name", target = "experimentName")
    @Mapping(source = "total_trials", target = "totalTrials")
    @Mapping(source = "direction", target = "direction")
    @Mapping(source = "tuneables", target = "tuneablesList")
    @Mapping(source = "hpo_algo_impl", target = "hpoAlgoImpl")
    ExperimentDetails map(HpoExperiment experimentDetails);

    @Mapping(source = "total_trials", target = "total_trials")
    @Mapping(source = "direction", target = "direction")
    @Mapping(source = "tuneables", target = "tunables")
    @Mapping(source = "hpo_algo_impl", target = "hpo_algo_impl")
    ExperimentDAO mapDAO(HpoExperiment experimentDetails);

//    @Mapping(source = "experimentName", target = "experiment_name")
    @Mapping(source = "direction", target = "direction")
    @Mapping(source = "tuneablesList", target = "tuneables")
    HpoExperiment map(ExperimentDetails experimentDetails);

    @Mapping(source = "name", target = "name")
    ExperimentTunable map(ExperimentDetails.Tunable value);

    ExperimentDetails.Tunable map(ExperimentTunable perm);

    @Mapping(source = "configList", target = "tunableConfigs")
    TrialConfig map(io.kruize.hpo.TrialConfig trialConfig);

    @Mapping(source = "name", target = "name")
    @Mapping(source = "value", target = "value")
    TunableConfig map(io.kruize.hpo.TunableConfig tunableConfig);


    HorreumDAO map(Horreum value);

    Horreum map(HorreumDAO value);

    JenkinsDAO map(JenkinsJob value);

    JenkinsJob map(JenkinsDAO value);
}
