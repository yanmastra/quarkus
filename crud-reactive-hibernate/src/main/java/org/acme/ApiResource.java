package org.acme;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.util.List;

@Path("/api/v1")
public class ApiResource {

    @Inject
    UserService userService;
    @Inject Logger logger;

    @POST
    @Path("user")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Response> createUser(org.acme.dao.User user) {
        try {
            return userService.saveUser(user);
        }catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Uni.createFrom().nullItem();
        }
    }

    @GET
    @Path("user")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<List<org.acme.dao.User>> getUser() {
        return userService.findAll();
    }
}
