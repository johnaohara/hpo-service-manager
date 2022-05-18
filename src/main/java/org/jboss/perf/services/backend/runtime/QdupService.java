package org.jboss.perf.services.backend.runtime;

import io.hyperfoil.tools.qdup.QDup;
import io.quarkus.runtime.StartupEvent;
import org.apache.commons.lang.ArrayUtils;
import org.jboss.perf.data.entity.ExperimentDAO;
import org.jboss.perf.services.dto.TrialConfig;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@ApplicationScoped
@RuntimeEnvironment(name = "qdup")
public class QdupService implements IRuntimeEnvironment {

    private static final ExecutorService qDupExecutor = Executors.newWorkStealingPool();

    private Map<String, Object> config = new ConcurrentHashMap<>();

    public void startup(@Observes StartupEvent startupEvent) {

        //TODO: config
        config.put("USER", "johara");
        config.put("HOST", "localhost");


        config.put("qDupFile", QdupService.class.getClassLoader().getResource("qDup/wrapper.qdup.yaml"));
        try {
            config.put("tmpDirectory", Files.createTempDirectory("qDup"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public String newRun(ExperimentDAO experimentDAO, TrialConfig trialConfig) {

        //TODO:: different implementations of args mappers for different input types
        Map<String, String> jobParams = trialConfig.tunableConfigs().stream()
                .filter(tunableConfig -> experimentDAO.qDup.params.containsKey(tunableConfig.name()))
                .collect(Collectors.toMap(tuneable -> experimentDAO.qDup.params.get(tuneable.name()), tuneable -> tuneable.value().toString()));

        String[] args = {"-S", "ARGS=[test]"};

//        jobParams.entrySet().stream().map( (entry) -> entry.getKey().concat("=").concat(entry.getValue()) ).reduce();


        try {


            qDupExecutor.submit(() -> {

                String qDupFilePath = null;
                try {
                    qDupFilePath = ((URL)config.get("qDupFile")).getPath();
                    System.out.println("Using qDup yaml file: ".concat(qDupFilePath));

                    File qDupFile = new File(qDupFilePath);
                    if (!qDupFile.exists()) {
                        System.err.printf("File not found: %s\n", qDupFilePath);
//                        System.exit(1);
                    } else {
                        qDupFilePath = qDupFile.getPath();
                    }
                } catch (SecurityException | NullPointerException | IllegalArgumentException exception) {
                }

                String[] qDupBaseArgs = {
                        qDupFilePath
                        , "-B"
                        , "/tmp/hpoQdup"
                        , "-S"
                        , "USER=" + config.get("USER")
                        , "-S"
                        , "HOST=" + config.get("HOST")
                };

                String[] qDupArgs = (String[]) ArrayUtils.addAll(qDupBaseArgs, args);

                QDup qDup = new QDup(qDupArgs);
                qDup.run();
            });


        } catch (Exception e) {
            return e.getLocalizedMessage();
        }

        return null;
    }
}
