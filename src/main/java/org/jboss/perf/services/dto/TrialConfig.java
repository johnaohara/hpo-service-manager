package org.jboss.perf.services.dto;

import java.util.List;

public record TrialConfig(List<TunableConfig> tunableConfigs) {}
