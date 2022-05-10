package org.jboss.perf.services;

import io.hyperfoil.tools.HorreumClient;
import io.hyperfoil.tools.horreum.api.QueryResult;
import io.hyperfoil.tools.horreum.entity.alerting.Variable;
import io.hyperfoil.tools.horreum.entity.json.Label;
import io.hyperfoil.tools.horreum.entity.json.LabelValue;
import io.hyperfoil.tools.horreum.entity.json.Test;
import io.hyperfoil.tools.horreum.entity.json.Transformer;
import io.quarkus.panache.common.Sort;
import io.quarkus.runtime.StartupEvent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
/*
* TODO:
*   - retry logic
* */
public class HorreumService {


    //TODO: expose configuration
    private static final String HORREUM_BASE_URL = "http://localhost:8080";
//    private static final String HORREUM_BASE_URL = "http://localhost:18088";
//    private static final String HORREUM_KEYCLOAK_BASE_URL = null;
    private static final String HORREUM_KEYCLOAK_BASE_URL = "http://localhost:8180";
//    private static final String HORREUM_KEYCLOAK_BASE_URL = "http://localhost:18180";
    private static final String HORREUM_USERNAME = "user";
    private static final String HORREUM_PASSWORD = "secret";

    protected static HorreumClient horreumClient;

    void startup(@Observes StartupEvent event) {
        horreumClient = new HorreumClient.Builder()
                .horreumUrl(HORREUM_BASE_URL + "/")
                .keycloakUrl(HORREUM_KEYCLOAK_BASE_URL)
                .horreumUser(HORREUM_USERNAME)
                .horreumPassword(HORREUM_PASSWORD)
                .build();

    }

    public boolean jobExists(String jobName){
        Test test = horreumClient.testService.getByNameOrId(jobName);
        return !(test == null);
    }

    public boolean experimentExists() {
        return true;
    }

    public List<String> getTests() {
        List<Test> tests = horreumClient.testService.list(null, 20, 0, null, Sort.Direction.Ascending );
        List<String> testNames = tests.stream().map(test -> test.name).collect(Collectors.toList());
        return  testNames;
    }

    public QueryResult getRunData(Integer id) {
        //TODO:: dynamically extract data from Horreum

        Map<String, Object> runSummary = (Map<String, Object>) horreumClient.runService.getRun(id, "");
        Integer schemaId = Integer.parseInt(((Map<String, ?>) runSummary.get("schema")).keySet().stream().findFirst().get());

        List<Label> labels = horreumClient.schemaService.labels(schemaId);

        //TODO:: execute any functions on server side

        Label avgLabel = labels.stream().filter(label -> label.name.equals("Average")).findFirst().get();
        String query = avgLabel.extractors.stream().findFirst().get().jsonpath;
        QueryResult result = horreumClient.runService.queryData(id, query, "urn:test:0.1", false);

        return result;
    }

    public Object getVariables(Integer testID){
        List<LabelValue> labelvalues = horreumClient.datasetService.queryDataSetLabels(testID);

        return labelvalues;
    }
}
