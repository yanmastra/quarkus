package org.acme.authenticationService.resources.web.v1;

import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("web/v1")
@Produces(MediaType.TEXT_HTML)
public class WebResource {
    @PermitAll
    @GET
    @Path("/home")
    public Uni<Response> home() {
        return Uni.createFrom().item(Response.ok().entity("<!doctype html><html><head></head><body></body></html>").build());
    }
}
