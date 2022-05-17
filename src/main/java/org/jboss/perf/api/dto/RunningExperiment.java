package org.jboss.perf.api.dto;

import org.jboss.perf.data.entity.ExperimentDAO;

public record RunningExperiment(String experimentName, Integer total_Trials, Integer currentTrial, ExperimentDAO.State currentState) {}
