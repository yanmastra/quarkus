package org.acme.crudReactiveHibernate;

import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.handler.HttpException;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.crudReactiveHibernate.dao.User;
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
    public Uni<Response> create(User user) {
        try {
            return userService.saveUser(user)
                    .onItem().transform(r -> Response.status(Response.Status.OK).entity(r).build())
                    .onFailure().transform(throwable -> new HttpException(500, throwable.getMessage()));
        }catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Uni.createFrom().nullItem();
        }
    }

    @GET
    @Path("user")
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<List<org.acme.crudReactiveHibernate.dao.User>> getUser() {
        return userService.findAll();
    }
}
