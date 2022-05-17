package org.jboss.perf.api;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ApiExceptionHandler implements ExceptionMapper<ApiException> {

    @Override
    public Response toResponse(ApiException exception)
    {
        return Response.status(Response.Status.BAD_REQUEST).entity(exception.failure).build();
    }
}
