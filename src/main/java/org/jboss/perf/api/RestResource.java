package org.jboss.perf.api;

import com.fasterxml.jackson.databind.node.ValueNode;
import io.hyperfoil.tools.horreum.api.QueryResult;
import org.jboss.perf.data.entity.ExperimentDAO;
import org.jboss.perf.services.HPOaaS;
import org.jboss.perf.services.backend.HorreumService;
import org.jboss.perf.services.backend.HpoService;
import org.jboss.perf.services.dto.HpoExperiment;
import org.jboss.perf.services.dto.TrialConfig;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;

@Path("/hpo")
public class RestResource {

    @Inject
    HpoService hpoService;

    @Inject
    HorreumService horreumService;

    @Inject
    HPOaaS hpOaaS;

//    All running experiements from HPO Service
    @GET
    @Path("/experiments")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> experiments() {
        return hpOaaS.getRunningExperiments();
    }

    @GET
    @Path("/experiment/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public HpoExperiment experimentByName(@PathParam("name") String name) {
        HpoExperiment details = hpoService.getExperimentByName(name);
        return details;
    }

    @GET
    @Path("/experiment/{name}/trial/{trial}")
    @Produces(MediaType.APPLICATION_JSON)
    public TrialConfig experimentConfig(@PathParam("name") String name, @PathParam("trial") Integer trial) {
        TrialConfig trialConfig = hpoService.getExperimentConfig(name, trial);
        return trialConfig;
    }


    @POST
    @Path("/experiment")
    @Produces(MediaType.APPLICATION_JSON)
    public ApiResult newExperiment(String config){
        String errors = hpOaaS.createNewExperiment(config);
        if ( errors == null){
            return ApiResult.success();
        } else {
            return ApiResult.failure(errors);
        }
    }

    @PUT
    @Path("/experiment/state")
    @Produces(MediaType.APPLICATION_JSON)
    public ApiResult newExperiment(String experimentName, ExperimentDAO.State state){
        String errors = hpOaaS.changeExperimentState(experimentName, state);
        if ( errors == null){
            return ApiResult.success();
        } else {
            return ApiResult.failure(errors);
        }
    }

    @GET
    @Path("horreum/tests")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getHorreumTests(){
        return horreumService.getTests();
    }

    @GET
    @Path("horreum/run/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public QueryResult getHorreumRunData(@PathParam("id") Integer id){
        return horreumService.getRunData(id);
    }

    @GET
    @Path("horreum/test/{id}/labels")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, ValueNode> getTestLabels(@PathParam("id") Integer id){
        return horreumService.queryDataSetLabels(id);
    }


//    @POST
//    @Path("/experiment/{name}/result")
//    @Produces(MediaType.TEXT_PLAIN)
//    public List<TunableConfig> experimentConfig(@PathParam("name") String name, String trial) {
//        TrialConfig trialConfig = hpoService.getExperimentConfig(name, trial);
//        return trialConfig.getConfigList();
//    }
}