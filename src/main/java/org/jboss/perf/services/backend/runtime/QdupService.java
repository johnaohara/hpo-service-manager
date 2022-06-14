package org.jboss.perf.services.backend.runtime;

import io.hyperfoil.tools.qdup.QDup;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.runtime.annotations.ConfigItem;
import org.apache.commons.lang.ArrayUtils;
import org.jboss.logging.Logger;
import org.jboss.perf.data.entity.ExperimentDAO;
import org.jboss.perf.parser.ExperimentBuilder;
import org.jboss.perf.parser.YamlParser;
import org.jboss.perf.services.dto.TrialConfig;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.Permission;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@ApplicationScoped
@RuntimeEnvironment(name = "QDUP")
public class QdupService implements IRuntimeEnvironment {

    @ConfigItem(name = "hpo.qdup.username")
    public String USER=  "johara";

    @ConfigItem(name = "hpo.qdup.host")
    public String HOST = "localhost";


    private static final ExecutorService qDupExecutor = Executors.newWorkStealingPool();

    private static final Logger logger = Logger.getLogger(QdupService.class);

    private Map<String, Object> config = new ConcurrentHashMap<>();


    public String newRun(ExperimentDAO experimentDAO, TrialConfig trialConfig) {

        configureQDUP();

        qDupExecutor.submit(() -> {
            try {
                executeBlocking(experimentDAO, trialConfig);
            } catch (QdupRuntimeException e) {
                logger.error("Exception occurred running qDup script", e);
            }
        });


        return null;
    }

    @Override
    public void parseConfig(ExperimentBuilder builder, Object o) {
        System.out.println("Parse qDupConfig");

        Map configMap = (Map) o;

        builder.addqDupJob(
                configMap.get("targetHost").toString()
                , configMap.get("user").toString()
                , configMap.get("script").toString()
        );

        List<Map<?, ?>> params = (List<Map<?, ?>>) configMap.get("params");
        for (Map paramMapping : params) {
//            builder.add("qDup~params", paramMapping);
            builder.addqDupParamMapping(paramMapping.get("name").toString(), paramMapping.get("tuneable").toString());
        }
        /*
        mappings.put("qDup~params", new YamlParser.SectionParser<Map<String, Object>>(
                (paramsMap, builder) -> numElements(paramsMap, 2) && containsKeys(paramsMap, "name", "tuneable")
                , (paramsMap, builder) -> builder.addqDupParamMapping(paramsMap.get("name").toString(), paramsMap.get("tuneable").toString()))
        );
*/

    }


    class TempSecurityManager extends SecurityManager {
        @Override
        public void checkExit(int status) {
            throw new SecurityException(String.valueOf(status)); //do not exit JVM if System.exit() is called
        }

        @Override
        public void checkPermission(Permission perm) {
            // Allow other activities by default
        }
    }

    void executeBlocking(ExperimentDAO experimentDAO, TrialConfig trialConfig) throws QdupRuntimeException {

        //TODO:: different implementations of args mappers for different input types
//        Map<String, String> jobParams = trialConfig.tunableConfigs().stream()
//                .filter(tunableConfig -> experimentDAO.qDup.params.containsKey(tunableConfig.name()))
//                .collect(Collectors.toMap(tuneable -> experimentDAO.qDup.params.get(tuneable.name()), tuneable -> tuneable.value().toString()));

        List<String> jobParams = trialConfig.tunableConfigs().stream()
                .filter(tunableConfig -> experimentDAO.qDup.params.containsKey(tunableConfig.name()))
                .map( tunableConfig -> tunableConfig.value().toString())
                .collect(Collectors.toList());

        String jobArgs = "ARGS=".concat(String.join(" ", jobParams));

        String[] args = {"-S", "SCRIPT_URL=".concat(experimentDAO.qDup.scriptUrl), "-S", jobArgs};

//        jobParams.entrySet().stream().map( (entry) -> entry.getKey().concat("=").concat(entry.getValue()) ).reduce();

        String qDupFilePath = null;
        try {
            qDupFilePath = ((URL) config.get("qDupFile")).getPath();
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

        //Setup env for running qDup in - need to swap out SecurityManager to catch `System.exit()` calls
        SecurityManager securityManager = System.getSecurityManager();
        System.setSecurityManager(new TempSecurityManager());
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;

        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final String utf8 = StandardCharsets.UTF_8.name();

        QDup qDup = new QDup(qDupArgs);

        String disableServer = System.getProperty("disableRestApi");
        try (PrintStream tempPrintStream = new PrintStream(baos, true, utf8)) {
//            System.setOut(tempPrintStream);
//            System.setErr(tempPrintStream);

            System.setProperty("disableRestApi", "true");

            qDup.run();
        } catch (SecurityException e) {
            if (!e.getMessage().equals("0")) {
                String msg = qDup.getRunDebug();
                if (msg != null) {
                    logger.warnv("qDup failed to start correctly");
                } else {
                    try {
                        logger.warnv(String.format("QDup failed to start correctly caused by: \n %s ", baos.toString(utf8)));
                    } catch (UnsupportedEncodingException ex) {
                        //ignore
                    }
                }
            }
        } catch (Throwable e) {
            throw new QdupRuntimeException(e);
            //String.format("QDup Parser threw following error: %s ", e.getMessage());
            //ignore
        } finally {
            //restore SecurityManager
            System.setSecurityManager(securityManager);
//            System.setOut(originalOut);
//            System.setErr(originalErr);

            if (disableServer != null) {
                System.setProperty("disableRestApi", disableServer);
            }

        }
    }

    private void configureQDUP() {
        config.putIfAbsent("USER", USER);
        config.putIfAbsent("HOST", HOST);
        config.putIfAbsent("qDupFile", QdupService.class.getClassLoader().getResource("qDup/wrapper.qdup.yaml"));
    }
}

