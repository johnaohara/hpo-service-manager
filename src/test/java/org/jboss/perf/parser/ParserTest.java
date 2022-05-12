package org.jboss.perf.parser;

import org.jboss.perf.services.dto.ExperimentConfig;
import org.junit.jupiter.api.Test;

import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class ParserTest {


    @Test
    public void testParser(){

        YamlParser parser = new YamlParser();

        String config = new Scanner(ParserTest.class.getClassLoader().getResourceAsStream("HPOaaS_Example.yaml"), "UTF-8").useDelimiter("\\A").next();

        try {
            ExperimentConfig experimentConfig = parser.parseYaml(config);

            assertEquals("local-test", experimentConfig.getExperimentName());

            assertEquals("local-test", experimentConfig.getHpoExperiment().name());
            assertEquals(1, experimentConfig.getHpoExperiment().parallel_trials());
        } catch (ConfigParserException e) {
            fail();
        }


    }
}
