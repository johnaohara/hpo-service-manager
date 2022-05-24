package org.jboss.perf.parser;

import org.jboss.perf.services.dto.ExperimentConfig;
import org.snakeyaml.engine.v2.api.Load;
import org.snakeyaml.engine.v2.api.LoadSettings;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Map;

@ApplicationScoped
public class YamlParser {
    @Inject
    ExperimentBuilder experimentBuilder;

    public ExperimentConfig parseYaml(String yml) throws ConfigParserException {
        LoadSettings settings = LoadSettings.builder().build();
        Load load = new Load(settings);
        Map<String, Object> map = (Map<String, Object>) load.loadFromString(yml);

        return experimentBuilder.add("root", map).build();
    }

    static class SectionParser<T> {
        public validator<T> validator;
        public procesor<T> procesor;

        public SectionParser(YamlParser.validator<T> validator, YamlParser.procesor<T> procesor) {
            this.validator = validator;
            this.procesor = procesor;
        }
    }

    interface validator<T> {
        boolean validate(T section, ExperimentBuilder experimentBuilder);
    }

    interface procesor<T> {
        void process(T section, ExperimentBuilder experimentBuilder) throws ConfigParserException;
    }

}