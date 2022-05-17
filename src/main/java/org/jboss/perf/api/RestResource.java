package org.jboss.perf.api;

import com.fasterxml.jackson.databind.node.ValueNode;
import io.hyperfoil.tools.horreum.api.QueryResult;
import org.jboss.perf.api.dto.RunningExperiment;
import org.jboss.perf.data.entity.TrialResultDAO;
import org.jboss.perf.services.HPOaaS;
import org.jboss.perf.services.backend.HorreumService;
import org.jboss.perf.services.backend.HpoService;
import org.jboss.perf.services.dto.HpoExperiment;
import org.jboss.perf.services.dto.HpoExperimentDetails;
import org.jboss.perf.services.dto.RecommendedConfig;
import org.jboss.perf.services.dto.TrialConfig;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;

@Path("/api/hpo")
public class RestResource {

    @Inject
    HpoService hpoService;

    @Inject
    HorreumService horreumService;

    @Inject
    HPOaaS hpOaaS;

//    All running experiements from HPO Service
    @GET
    @Path("/experiment")
    @Produces(MediaType.APPLICATION_JSON)
    public List<RunningExperiment> experiments() {
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
    @Path("/experiment/{name}/status")
    @Produces(MediaType.APPLICATION_JSON)
    public HpoExperimentDetails experimentStatusByName(@PathParam("name") String name) {
        HpoExperimentDetails details = hpoService.getExperimentStatusByName(name);
        return details;
    }

    @GET
    @Path("/experiment/{name}/recommend")
    @Produces(MediaType.APPLICATION_JSON)
    public RecommendedConfig experimentRecommendConfigByName(@PathParam("name") String name) {
        RecommendedConfig details = hpoService.getExperimentRecommendedConfig(name);
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
        ApiResult result = hpOaaS.createNewExperiment(config);
        if ( result.status == ApiResult.NewExperimentStatus.SUCCESS){
            return result;
        } else {
            throw new ApiException(result);
        }
    }

    @PUT
    @Path("/experiment/state")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ApiResult newExperiment(ExperimentState experimentState){
        String errors = hpOaaS.changeExperimentState(experimentState.name, experimentState.state);
        if ( errors == null){
            return ApiResult.success();
        } else {
            return ApiResult.failure(errors);
        }
    }

    @POST
    @Path("/experiment/rerun")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ApiResult reRunExperiment(ExperimentState experimentState){
        String errors = hpOaaS.rerunExperiemnt(experimentState.name);
        if ( errors == null){
            return ApiResult.success();
        } else {
            return ApiResult.failure(errors);
        }
    }

    @GET
    @Path("/experiment/{name}/config")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public RecommendedConfig reRunExperiment(@PathParam("name") String experimentName){
        RecommendedConfig config = hpoService.getRecommendedConfig(experimentName);
        return config;
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


    static class ExperimentState {

        public String name;
        public String state;
    }

//    @POST
//    @Path("/experiment/{name}/result")
//    @Produces(MediaType.TEXT_PLAIN)
//    public List<TunableConfig> experimentConfig(@PathParam("name") String name, String trial) {
//        TrialConfig trialConfig = hpoService.getExperimentConfig(name, trial);
//        return trialConfig.getConfigList();
//    }
}