package org.jboss.perf.hooks;

import io.hyperfoil.tools.horreum.entity.json.Run;
import org.jboss.logmanager.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/horrem")
public class HorreumWebook {

    Logger logger = Logger.getLogger(HorreumWebook.class.getName());

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("newRun")
    public String handleNewRun(Run run){
        logger.info("New run received. Test ID: ".concat(String.valueOf(run.testid)).concat("; with id: ").concat(String.valueOf(run.id)));
        return "OK";

    }
}
