package org.jboss.perf.services.backend.runtime;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

@ApplicationScoped
public class RuntimeProducer {

    @Inject
    @Any
    Instance<IRuntimeEnvironment> runtimeEnvironmentSource;

    public IRuntimeEnvironment getRuntime(String runtime) {
        IRuntimeEnvironment runtimeEnvironment = runtimeEnvironmentSource.select(new RuntimeEnvironment.RuntimeEnvironmentQualifier(runtime)).get();
        return  runtimeEnvironment;
    }
}
