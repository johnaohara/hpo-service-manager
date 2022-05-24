package org.jboss.perf.services.backend.runtime;

import org.jboss.perf.data.entity.ExperimentDAO;
import org.jboss.perf.parser.ExperimentBuilder;
import org.jboss.perf.services.dto.TrialConfig;

public interface IRuntimeEnvironment {

    /*
     * TODO:: Either need to;
     *  a - expose one-to-one direct mapping of tuneables -> params
     *  b - create dynamically mappings of tunable -> params
     *  c - pass to jenkins job to parse param mappings
     *
     *  supporting (b) will be more versatile, but by default (a) will allow for less config
     * */

    String newRun(ExperimentDAO experimentDAO, TrialConfig trialConfig);

    void parseConfig(ExperimentBuilder builder, Object o);

}
