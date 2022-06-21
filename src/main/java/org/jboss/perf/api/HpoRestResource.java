package org.jboss.perf.api;

import org.jboss.perf.services.backend.HpoService;
import org.jboss.perf.services.dto.ExperimentTunable;
import org.jboss.perf.services.dto.HpoExperiment;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import io.quarkus.logging.Log;
import org.jboss.perf.services.dto.TrialConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


@Path("/experiment_trials")
public class HpoRestResource {

    @Inject
    HpoService hpoService;

    private static final List<String> validAlgorithms = Arrays.asList("optuna_tpe", "optuna_tpe_multivariate", "optuna_skopt");

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public TrialConfig handleGet(
            @QueryParam("experiment_name") String experiment_name,
            @QueryParam("trial_number") Integer trial_number
    ) {

        if (experiment_name == null || trial_number == null || trial_number < 0) {
            throw new BadRequestException("Invalid arguments");
        }

        if (!hpoService.experimentExists(experiment_name)) {
            throw new NotFoundException("Experiment not found: ".concat(experiment_name));
        }

        HpoExperiment experiment = hpoService.getExperimentByName(experiment_name);

        if (!experiment.current_trial().equals(trial_number)) {
            throw new BadRequestException("Invalid trial number: ".concat(trial_number.toString()));
        }

        Log.info("Experiment_Name = " + experiment.name());
        Log.info("Trial_Number = " + experiment.current_trial());

        return hpoService.getTrialConfig(experiment_name, trial_number);

    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String handlePost(Map<String, Object> payload) {
        Log.info(payload);

        if (payload.get("operation") == null) {
            throw new BadRequestException("Missing `operation` node");
        }

        String operation = payload.get("operation").toString();
        switch (operation) {
            case "EXP_TRIAL_GENERATE_NEW":
                return handle_generate_new_operation(payload);
            case "EXP_TRIAL_GENERATE_SUBSEQUENT":
                return handle_generate_subsequent_operation(payload);
            case "EXP_TRIAL_RESULT":
                return handle_result_operation(payload);
            case "EXP_STOP":
                return handle_stop_operation(payload);
            default:
                throw new BadRequestException("Unknown operation: ".concat(operation));
        }
    }

    private String handle_generate_new_operation(Map<String, Object> payload) {
//        Boolean is_valid_json_object = validate_trial_generate_json(json_object)
        Boolean is_valid_json_object = true;

        Object expName = ((Map<String, Object>) payload.get("search_space")).get("experiment_name");
        if (!is_valid_json_object || expName == null || hpoService.experimentExists(expName.toString())) {
            throw new BadRequestException();
        }

        Map<String, Object> search_space_json = (Map<String, Object>) payload.get("search_space");

        if (!payload.containsKey("parallel_trials")) {
            payload.put("parallel_trials", 1);
        }

        String id_ = !search_space_json.containsKey("experiment_id") ? null : search_space_json.get("experiment_id").toString();
        String experiment_name = !search_space_json.containsKey("experiment_name") ? null : search_space_json.get("experiment_name").toString();
        Integer total_trials = !search_space_json.containsKey("total_trials") ? null : Integer.parseInt(search_space_json.get("total_trials").toString());
        Integer parallel_trials = !search_space_json.containsKey("parallel_trials") ? null : Integer.parseInt(search_space_json.get("parallel_trials").toString());
        String objective_function = !search_space_json.containsKey("objective_function") ? null : search_space_json.get("objective_function").toString();
        String value_type = !search_space_json.containsKey("value_type") ? null : search_space_json.get("value_type").toString();
        String direction = !search_space_json.containsKey("direction") ? null : search_space_json.get("direction").toString();
        String hpo_algo_impl = !search_space_json.containsKey("hpo_algo_impl") ? null : search_space_json.get("hpo_algo_impl").toString();
        String slo_class = null; //TODO: need to define this!
        List<ExperimentTunable> tuneables = new ArrayList<>();
        if (search_space_json.containsKey("tunables")) {
            ((List<Map<String, Object>>) search_space_json.get("tunables")).forEach(map -> {
                ExperimentTunable tunable = new ExperimentTunable(
                        map.get("name").toString(),
                        map.get("value_type").toString(),
                        Double.parseDouble(map.get("lower_bound").toString()),
                        Double.parseDouble(map.get("upper_bound").toString()),
                        Double.parseDouble(map.get("step").toString())
                );
                tuneables.add(tunable);
            });

        }
//        tunables = payload.get("tunables"]

        HpoExperiment newExperiment = new HpoExperiment(experiment_name,
                id_, total_trials, parallel_trials, null, value_type,
                hpo_algo_impl, objective_function, slo_class, direction, tuneables);

        Log.info("Total Trials = ".concat(total_trials.toString()));
        Log.info("Parallel Trials = ".concat(parallel_trials.toString()));

        if (!validAlgorithms.contains(hpo_algo_impl)) {
            throw new BadRequestException("Invalid algorithm");
        }

        return hpoService.newExperiment(newExperiment);
    }


    private String handle_generate_subsequent_operation(Map<String, Object> payload) {
//        Boolean is_valid_json_object = validate_trial_generate_json(json_object);
        Boolean is_valid_json_object = true;
        if (!is_valid_json_object) {
            throw new BadRequestException();
        }
        String experiment_name = getExpName(payload);

        var experiment = hpoService.getExperimentByName(experiment_name);
        if (experiment.current_trial() == -1) {
            throw new BadRequestException("Trials completed for experiment: " + experiment_name);
        }
        return experiment.current_trial().toString();
    }

    private String handle_result_operation(Map<String, Object> payload) {
        String experiment_name = getExpName(payload);
        HpoExperiment experiment = hpoService.getExperimentByName(experiment_name);
        Integer trialNum = !payload.containsKey("trial_number") ? null : Integer.parseInt(payload.get("trial_number").toString());
        if (!experiment.current_trial().equals(trialNum)) {
            throw new BadRequestException("Invalid trial number");
        }

        hpoService.newResult(experiment_name,
                payload.get("trial_result").toString(),
                trialNum);
        return "0";
    }


    private String handle_stop_operation(Map<String, Object> payload) {
        String expName = getExpName(payload);
        hpoService.stopExperimentByName(expName);
        return "0";
    }

    private String getExpName(Map<String, Object> payload) {
        String experiment_name = payload.get("experiment_name").toString();

        if (!hpoService.experimentExists(experiment_name)) {
            throw new NotFoundException("Experiment not found: ".concat(experiment_name));
        }
        return experiment_name;
    }

}