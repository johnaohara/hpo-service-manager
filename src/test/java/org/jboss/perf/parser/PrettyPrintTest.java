package org.jboss.perf.parser;

import org.junit.Test;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;

public class PrettyPrintTest {

    private static String expt = "ExperimentConfg{experiment=local-test-Jenkins, jenkins=JenkinsJob[job=test, job_url=test, params={parallel=parallel, memoryRequest=memoryRequest, cpuRequest=cpuRequest}], horreum=Horreum[jobID=10, auth=null], hpo=HpoExperiment[name=local-test-Jenkins, test_id=10, total_trials=10, parallel_trials=1, value_type=double, hpo_algo_impl=optuna_tpe, objective_function=AverageJenkins, slo_class=response_time, direction=maximize, tuneables=[ExperimentTunable[name=memoryRequest, value_type=double, lower_bound=10.0, upper_bound=30.0, step=1.0], ExperimentTunable[name=cpuRequest, value_type=double, lower_bound=1.0, upper_bound=3.0, step=0.01], ExperimentTunable[name=parallel, value_type=double, lower_bound=1.0, upper_bound=100.0, step=1.0]]]}";


    @Test
    public void prettyPrintExperiment() {

        StringBuilder outputBuilder = new StringBuilder();
        PrintStream os = System.out;
        String pad = "";

        for (int i = 0; i < expt.length(); i++) {
            char curChar = expt.charAt(i);
            switch (curChar) {
                case '{', '[':
                    pad = pad.concat("  ");
                    outputBuilder.append(" ").append(curChar);

                    os.println(outputBuilder);
                    outputBuilder = new StringBuilder();
                    outputBuilder.append(pad);

                    break;
                case '}', ']':
                    pad = pad.length() > 0 ? pad.substring(0, pad.length() - 2) : "";
                    os.println(outputBuilder);
                    outputBuilder = new StringBuilder();
                    outputBuilder.append(pad).append(curChar);
                    os.println(outputBuilder);
                    outputBuilder = new StringBuilder();
                    break;
                case ',':
                    os.println(outputBuilder.append(curChar));
                    outputBuilder = new StringBuilder();
                    outputBuilder.append(pad);
                    break;
                default:
                    outputBuilder.append(curChar);
            }
        }


    }


}
