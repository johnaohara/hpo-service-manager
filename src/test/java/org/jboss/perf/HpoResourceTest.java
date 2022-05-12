package org.jboss.perf;

import io.kruize.hpo.ExperimentDetails;
import io.kruize.hpo.HpoServiceGrpc;
import io.quarkus.grpc.GrpcClient;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.perf.services.HpoResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@Disabled
@QuarkusTest
@QuarkusTestResource(HpoResource.class)
public class HpoResourceTest {

    @ConfigProperty(name="hpo.service.image", defaultValue = "quay.io/johara/hpo-service:0.0.1-SNAPSHOT")
    String message;

    @GrpcClient("hpo")
    HpoServiceGrpc.HpoServiceBlockingStub blockingHpoService;

    @BeforeEach
    public void setupExperiemnts() {
        ExperimentDetails petclinicExperiment = ExperimentDetails.newBuilder()
                .setExperimentName("petclinic-sample-2-75884c5549-npvgd")
                .setExperimentId("1")
                .setHpoAlgoImpl("optuna_tpe")
                .build();
        blockingHpoService.newExperiment(petclinicExperiment);
    }


    @Test
    public void testRestEndpoint() {
        given()
                .when().get("/hpo/experiments")
                .then()
                .statusCode(200)
                .body(is("[\"petclinic-sample-2-75884c5549-npvgd\"]"));

        given()
                .when().get("/hpo/experiments/petclinic-sample-2-75884c5549-npvgd")
                .then()
                .statusCode(200)
                .body(is("[\"petclinic-sample-2-75884c5549-npvgd\"]"));

    }

    @Test
    public void testGrpcEndpoint() {
        given()
                .when().get("/hpo/experiments")
                .then()
                .statusCode(200)
                .body(is("[\"petclinic-sample-2-75884c5549-npvgd\"]"));

    }


    @Test
    public void runExperiment() {



        given()
                .when().get("/hpo/experiments")
                .then()
                .statusCode(200)
                .body(is("[\"petclinic-sample-2-75884c5549-npvgd\"]"));

    }

}