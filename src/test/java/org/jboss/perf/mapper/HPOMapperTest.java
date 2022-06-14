package org.jboss.perf.mapper;

import io.kruize.hpo.ExperimentDetails;
import org.jboss.perf.parser.ConfigParserException;
import org.jboss.perf.parser.ParserTest;
import org.jboss.perf.parser.YamlParser;
import org.jboss.perf.services.dto.ExperimentConfig;
import org.jboss.perf.services.dto.ExperimentTunable;
import org.jboss.perf.services.dto.HpoMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Scanner;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@Disabled
public class HPOMapperTest {

    private static ExperimentConfig config;

    @BeforeAll
    public static void init(){
        YamlParser yamlParser = new YamlParser();
        String configFile = new Scanner(ParserTest.class.getClassLoader().getResourceAsStream("HPOaaS_Example.yaml"), "UTF-8").useDelimiter("\\A").next();
        try {
            config = yamlParser.parseYaml(configFile);
        } catch (ConfigParserException e) {
            e.printStackTrace();
            fail(e.getLocalizedMessage());
        }
    }


    @Test
    public void mapHpoExperiment(){
        assertNotNull(config);

        ExperimentDetails experimentDetails = HpoMapper.INSTANCE().map(config.getHpoExperiment());

        assertEquals(config.getHpoExperiment().name(), experimentDetails.getExperimentName());
        assertEquals(config.getHpoExperiment().hpo_algo_impl(), experimentDetails.getHpoAlgoImpl());
        assertEquals(config.getHpoExperiment().direction(), experimentDetails.getDirection());
        assertEquals(config.getHpoExperiment().objective_function(), experimentDetails.getObjectiveFunction());
        assertEquals(config.getHpoExperiment().value_type(), experimentDetails.getValueType());
        assertEquals(config.getHpoExperiment().slo_class(), experimentDetails.getSloClass());
        assertEquals(config.getHpoExperiment().total_trials(), experimentDetails.getTotalTrials());
        assertEquals(config.getHpoExperiment().parallel_trials(), experimentDetails.getParallelTrials());
        assertNotNull(experimentDetails.getTuneablesList());
        experimentDetails.getTuneablesList().forEach( tuneable -> {
            ExperimentTunable experimentTunable = config.getHpoExperiment().tuneables().stream().filter(configTuneable -> configTuneable.name().equals(tuneable.getName())).findFirst().get();
                    assertEquals(experimentTunable.name(), tuneable.getName());
                    assertEquals(experimentTunable.value_type(), tuneable.getValueType());
                    assertEquals(experimentTunable.step(), tuneable.getStep());
                    assertEquals(experimentTunable.upper_bound(), tuneable.getUpperBound());
                    assertEquals(experimentTunable.lower_bound(), tuneable.getLowerBound());
                });
        assertNotEquals(0, experimentDetails.getTuneablesList().size());
    }
}
