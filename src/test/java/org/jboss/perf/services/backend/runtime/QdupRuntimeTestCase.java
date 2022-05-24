package org.jboss.perf.services.backend.runtime;


import io.quarkus.test.junit.QuarkusTest;
import org.jboss.perf.data.entity.ExperimentDAO;
import org.jboss.perf.data.entity.QDupDAO;
import org.jboss.perf.services.backend.runtime.IRuntimeEnvironment;
import org.jboss.perf.services.backend.runtime.RuntimeProducer;
import org.jboss.perf.services.dto.TrialConfig;
import org.jboss.perf.services.dto.TunableConfig;
import org.junit.Assert;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

@QuarkusTest
public class QdupRuntimeTestCase {

    @Inject
    RuntimeProducer runtimeProducer;

    @Test
    @Disabled
    public void testDownloadScript() {

        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        HttpRequest request = null;
        try {
            request = HttpRequest.newBuilder()
                    .uri(new URI("http://localhost:8888/static/example.sh"))
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            Assert.assertEquals(200, response.statusCode());
            assertNotNull(response.body());
        } catch (URISyntaxException | IOException | InterruptedException e) {
            fail(e);
        }
    }


    @Test
    public void testQdupRuntime() {
        try {

//            QdupService qDupRuntime = new QdupService();
//
//            qDupRuntime.startup(null);
            QdupService qDupRuntime = (QdupService) runtimeProducer.getRuntime("qdup");

            ExperimentDAO experimentDAO = new ExperimentDAO();

            experimentDAO.name = "qdup-test";
            experimentDAO.state = ExperimentDAO.State.RUNNING;

            QDupDAO qDupDAO = new QDupDAO();

            //Can not use QuarkusTest unitl JPMS issue solved
//            qDupDAO.scriptUrl = "http://localhost:8888/static/example.sh";
            qDupDAO.scriptUrl = "https://raw.githubusercontent.com/johnaohara/hpo-service-manager/qdup-backend/src/test/resources/example.sh";
            qDupDAO.targetHost = InetAddress.getLocalHost().getHostName();
            qDupDAO.username = System.getProperty("user.name");

            qDupDAO.params = new HashMap<>();

            qDupDAO.params.put("memory", "memory");
            qDupDAO.params.put("cpu", "cpu");

            experimentDAO.qDup = qDupDAO;

            List<TunableConfig> tunableConfigs = new ArrayList<>();

            tunableConfigs.add(new TunableConfig("memory", 1000.0f, "double"));
            tunableConfigs.add(new TunableConfig("cpu", 3.0f, "double"));

            TrialConfig trialConfig = new TrialConfig(tunableConfigs);

            qDupRuntime.executeBlocking(experimentDAO, trialConfig);
        } catch (UnknownHostException | QdupRuntimeException e) {
            fail(e);
        }


    }

}
