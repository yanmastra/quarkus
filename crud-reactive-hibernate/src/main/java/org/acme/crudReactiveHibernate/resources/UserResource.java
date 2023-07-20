package org.acme.crudReactiveHibernate.resources;

import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.handler.HttpException;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.crudReactiveHibernate.dao.UserOnly;
import org.acme.crudReactiveHibernate.services.UserService;
import org.jboss.logging.Logger;

import java.util.List;

@Path("/api/v1/user")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject
    UserService userService;
    @Inject
    Logger logger;

    @POST
    public Uni<Response> create(UserOnly user) {
        try {
            return userService.saveUser(user)
                    .onItem().transform(r -> Response.status(Response.Status.OK).entity(r).build())
                    .onFailure().transform(throwable -> new HttpException(500, throwable.getMessage()));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Uni.createFrom().nullItem();
        }
    }

    @GET
    @Path("/{id}")
    public Uni<Response> get(@PathParam("id") String id) {
        try {
            return userService.findDetail(id)
                    .onItem().transform(r -> Response.ok().entity(r).build())
                    .onFailure().transform(throwable -> {
                        logger.error(throwable.getMessage(), throwable);
                        return new HttpException(500, throwable.getMessage());
                    });
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Uni.createFrom().nullItem();
        }
    }

    @PUT
    @Path("/{id}")
    public Uni<Response> update(UserOnly user, @PathParam("id") String id) {
        logger.info("request: id=" + id + ", data:" + user);
        try {
            return userService.updateUser(id, user)
                    .onItem().transform(r -> Response.status(Response.Status.OK).entity(r).build())
                    .onFailure().transform(throwable -> {
                        logger.error(throwable.getMessage(), throwable);
                        return new HttpException(500, throwable.getMessage());
                    });
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Uni.createFrom().nullItem();
        }
    }

    @GET
    public Uni<List<org.acme.crudReactiveHibernate.dao.UserOnly>> getUser() {
        return userService.findAll();
    }
}
