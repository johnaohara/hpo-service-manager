package org.jboss.perf.api;

import io.hyperfoil.tools.horreum.api.QueryResult;
import org.jboss.perf.services.HPOaaS;
import org.jboss.perf.services.HorreumService;
import org.jboss.perf.services.HpoService;
import org.jboss.perf.services.dto.HpoExperiment;
import org.jboss.perf.services.dto.TrialConfig;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

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
        return hpoService.getRunningExperiments();
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
    public TrialConfig experimentConfig(@PathParam("name") String name, @PathParam("trial") String trial) {
        TrialConfig trialConfig = hpoService.getExperimentConfig(name, trial);
        return trialConfig;
    }


    @POST
    @Path("/experiment")
    @Produces(MediaType.APPLICATION_JSON)
    public NewExperimentResult newExperiment(String config){
        String errors = hpOaaS. createNewExperiment(config);
        if ( errors == null){
            return NewExperimentResult.success();
        } else {
            return NewExperimentResult.failure(errors);
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
    public Object getTestLabels(@PathParam("id") Integer id){
        return horreumService.getVariables(id);
    }


//    @POST
//    @Path("/experiment/{name}/result")
//    @Produces(MediaType.TEXT_PLAIN)
//    public List<TunableConfig> experimentConfig(@PathParam("name") String name, String trial) {
//        TrialConfig trialConfig = hpoService.getExperimentConfig(name, trial);
//        return trialConfig.getConfigList();
//    }
}