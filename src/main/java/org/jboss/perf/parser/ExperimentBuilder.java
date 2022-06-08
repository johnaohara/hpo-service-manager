package org.jboss.perf.parser;

import io.quarkus.runtime.StartupEvent;
import org.jboss.perf.services.backend.runtime.RuntimeEnvironment;
import org.jboss.perf.services.backend.runtime.RuntimeProducer;
import org.jboss.perf.services.dto.ExperimentConfig;
import org.jboss.perf.services.dto.ExperimentTunable;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RequestScoped
public class ExperimentBuilder {

    @Inject
    RuntimeProducer runtimeProducer;

    private Map<String, YamlParser.SectionParser> mappings;

    public void startup(@Observes StartupEvent startupEvent) {
        mappings = new HashMap<>();

        mappings.put("root", new YamlParser.SectionParser<Map<String, Map>>(
                        (section, builder) -> !section.isEmpty() && numElements(section, 1) && containsKeys(section, "hpo")
                        , (section, builder) -> {
                    for (Map.Entry<String, Map> entry : section.entrySet()) { //can not use streams here, otherwise ConfigParserException is not propagated back from lambda :(
                        builder.add(entry.getKey(), entry.getValue());
                    }
                }
                )
        );

        mappings.put("hpo", new YamlParser.SectionParser<Map<String, Object>>(
                        (section, builder) -> containsKeys(section, "name")
                        , (sectionMap, builder) -> {
                    for (Map.Entry<String, Object> entry : sectionMap.entrySet()) {
                        builder.add("hpo~".concat(entry.getKey()), entry.getValue());
                    }
                }
                )
        );

        mappings.put("hpo~name", new YamlParser.SectionParser<String>(
                (section, builder) -> true
                , (expName, builder) -> builder.setName(expName))
        );

        mappings.put("hpo~experiment-manager", new YamlParser.SectionParser<Map<String, Object>>(
                        (experimentManagerMap, builder) -> numElements(experimentManagerMap, 1)
                        , (experimentManagerMap, builder) -> {

                    Set keys = experimentManagerMap.keySet();
                    String manager = (String) keys.stream().findFirst().get();

                    runtimeProducer.getRuntime(manager).parseConfig(builder, experimentManagerMap.get(manager));


//                    builder.addqDupJob(
//                            jenkinsMap.get("targetHost").toString()
//                            , jenkinsMap.get("user").toString()
//                            , jenkinsMap.get("script").toString()
//                    );
//
//                    List<?> params = (List<?>) jenkinsMap.get("params");
//                    for (Object paramMapping : params) {
//                        builder.add("qDup~params", paramMapping);
//                    }
                })
        );


        mappings.put("qDup~params", new YamlParser.SectionParser<Map<String, Object>>(
                (paramsMap, builder) -> numElements(paramsMap, 2) && containsKeys(paramsMap, "name", "tuneable")
                , (paramsMap, builder) -> builder.addqDupParamMapping(paramsMap.get("name").toString(), paramsMap.get("tuneable").toString()))
        );


        mappings.put("hpo~horreum", new YamlParser.SectionParser<Map<String, Object>>(
                        (horreumMap, builder) -> (horreumMap.keySet().size() >= 2) && containsKeys(horreumMap, "jobID", "auth")
                        , (horreumMap, builder) -> {
                    Integer jobID = Integer.parseInt(horreumMap.get("jobID").toString());
                    builder.addHorreum(
                            jobID
                    );
                    builder.setHorreumJobID(jobID);
                })
        );

        mappings.put("hpo~hpo_search_space", new YamlParser.SectionParser<Map<String, Object>>(
                        (hpoMap, builder) -> numElements(hpoMap, 8) && containsKeys(hpoMap, "total_trials", "parallel_trials", "value_type", "hpo_algo_impl", "objective_function", "tuneables", "slo_class", "direction")
                        , (hpoMap, builder) -> {
                    builder.addHpoSearchSpace(builder.config.getExperimentName(),
                            builder.config.getHorreumJobID(),
                            Integer.parseInt(hpoMap.get("total_trials").toString()),
                            Integer.parseInt(hpoMap.get("parallel_trials").toString()),
                            hpoMap.get("value_type").toString(),
                            hpoMap.get("hpo_algo_impl").toString(),
                            hpoMap.get("objective_function").toString(),
                            hpoMap.get("slo_class").toString(),
                            hpoMap.get("direction").toString()
                    );

                    builder.add("hpo~tunableList", hpoMap.get("tuneables"));

                }
                )
        );

        mappings.put("hpo~tunableList", new YamlParser.SectionParser<List<Map<String, String>>>(
                        (tunablesList, builder) -> true
                        , (tunablesList, builder) -> {
                    for (Map<String, String> tunableMapping : tunablesList) {
                        builder.add("hpo~tunable", tunableMapping);
                    }
                }
                )
        );

        mappings.put("hpo~tunable", new YamlParser.SectionParser<Map<String, Object>>(
                        (tunable, builder) -> numElements(tunable, 5) && containsKeys(tunable, "value_type", "lower_bound", "name", "upper_bound", "step")
                        , (tunable, builder) -> {
                    builder.addTuneable(
                            tunable.get("value_type").toString(),
                            tunable.get("name").toString(),
                            Double.parseDouble(tunable.get("lower_bound").toString()),
                            Double.parseDouble(tunable.get("upper_bound").toString()),
                            Double.parseDouble(tunable.get("step").toString())
                    );
                })
        );

    }

    static boolean numElements(Map map, Integer numElements) {
        return map.keySet().size() == numElements;
    }

    static boolean containsKeys(Map map, String... elements) {
        for (String element : elements) {
            if (!map.containsKey(element)) {
                return false;
            }
        }
        return true;
    }


    private ExperimentConfig config;

    public static ExperimentBuilder instance() {
        return new ExperimentBuilder();
    }

    <T> ExperimentBuilder add(String mappingName, T value) throws ConfigParserException {
        if (mappings.containsKey(mappingName)) {
            YamlParser.SectionParser sectionParser = mappings.get(mappingName);
            if (sectionParser.validator.validate(value, this)) {
                sectionParser.procesor.process(value, this);
            } else {
                throw new ConfigParserException("Could not validate mapping: " + mappingName.replaceAll("~", " -> "));
            }
            return this;
        } else {
            throw new ConfigParserException("Unknown mapping value: " + mappingName);
        }
    }

    private ExperimentBuilder() {
        this.config = new ExperimentConfig();
    }

    ExperimentBuilder setName(String name) {
        this.config.setExperimentName(name);
        return this;
    }

    ExperimentBuilder setHorreumJobID(Integer jobID) {
        this.config.setHorreumJobID(jobID);
        return this;
    }

    public ExperimentBuilder addqDupJob(String targetHost, String user, String scriptUrl ) {
        this.config.defineQdupJob(targetHost, user, scriptUrl);
        return this;
    }

    public ExperimentBuilder addJenkinsJob(String job, String job_url) {
        this.config.defineJenkinsJob(job, job_url);
        return this;
    }

    private void addHorreum( Integer jobID) {
        this.config.defineHorreum(jobID);
    }

    private void addHpoSearchSpace(String name, Integer test_id, Integer total_trials, Integer parallel_trials, String value_type,
                                   String hpo_algo_impl, String objective_function, String slo_class, String direction) {
        this.config.defineHpoExperiment(name, test_id, total_trials, parallel_trials, value_type,
                hpo_algo_impl, objective_function, slo_class, direction);
    }

    private void addTuneable(String value_type, String name, Double lower_bound,
                             Double upper_bound, Double step) {
        ExperimentTunable tuneable = new ExperimentTunable(name, value_type, lower_bound, upper_bound, step);

        this.config.getHpoExperiment().addTuneable(tuneable);
    }


    public void addqDupParamMapping(String name, String tuneable) {
        this.config.getqDupJob().addParam(name, tuneable);
    }


    public void addJenkinsParamMapping(String name, String tuneable) {
        this.config.getJenkinsJob().addParam(name, tuneable);
    }

    public ExperimentConfig build() throws ConfigParserException {
        //TODO:: add validation of configuration
        return this.config;
    }

}
