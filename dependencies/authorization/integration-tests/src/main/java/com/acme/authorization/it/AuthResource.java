package com.acme.authorization.it;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

@Path("/auth/test")
public class AuthResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> index(@Context SecurityContext context) {
        return Uni.createFrom().item(
                Response.ok(context.getUserPrincipal()).build()
        );
    }
}
