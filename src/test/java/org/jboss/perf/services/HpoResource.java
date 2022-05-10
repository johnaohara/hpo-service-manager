package org.jboss.perf.services;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.testcontainers.containers.GenericContainer;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Map;


public class HpoResource implements
        QuarkusTestResourceLifecycleManager {

    @Inject
    @ConfigProperty(name="hpo.service.image", defaultValue = "quay.io/johara/hpo-service:0.0.1-SNAPSHOT")
    String service_image;

    private static final String _image = "quay.io/johara/hpo-service:0.0.1-SNAPSHOT";

    private static GenericContainer<?> hpo;

    @Override
    public Map<String, String> start() {
//        hpo = new GenericContainer<>(message);
        hpo = new GenericContainer<>(_image);
        hpo.addExposedPort(50051);
        hpo.addExposedPort(8085);
        hpo.start();
        return Collections.singletonMap("quarkus.grpc.clients.hpo.port", hpo.getMappedPort(50051).toString());
    }

    @Override
    public void stop() {
        if (hpo != null) {
            hpo.stop();
        }
    }
}
