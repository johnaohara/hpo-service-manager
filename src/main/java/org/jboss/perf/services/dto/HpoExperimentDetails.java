package org.jboss.perf.services.dto;


import org.jboss.perf.data.entity.TrialResultDAO;

import java.util.List;
import java.util.Map;

public class HpoExperimentDetails {
    public String name;
    public Integer total_trials;
    public Integer current_trial;

    public Map<Integer, TrialResultDAO> trialHistory;
    public TrialResultDAO recommendedConfig;
}
