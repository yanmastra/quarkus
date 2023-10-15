package org.acme.authenticationService.resources.api.v1;

import com.acme.authorization.security.UserSecurityContext;
import com.acme.authorization.utils.ValidationUtils;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.handler.HttpException;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.authenticationService.dao.UserOnly;
import org.acme.authenticationService.services.UserService;
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

    @RolesAllowed({"CREATE_USER"})
    @POST
    public Uni<Response> create(UserOnly user, @Context UserSecurityContext context) {
        if (!ValidationUtils.isEmail(user.getEmail())) {
            throw new HttpException(HttpResponseStatus.BAD_REQUEST.code(), "Incorrect email format");
        }

        try {
            user.setCreatedBy(context.getUserPrincipal().getName());
            return userService.saveUser(user)
                    .onItem().transform(r -> Response.status(Response.Status.OK).entity(r).build())
                    .onFailure().transform(throwable -> new HttpException(500, throwable.getMessage()));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Uni.createFrom().nullItem();
        }
    }

    @RolesAllowed({"VIEW_ALL", "VIEW_USER"})
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

    @RolesAllowed({"UPDATE_USER"})
    @PUT
    @Path("/{id}")
    public Uni<Response> update(UserOnly user, @PathParam("id") String id, @Context UserSecurityContext context) {
        logger.info("request: id=" + id + ", data:" + user);
        try {
            user.setUpdatedBy(context.getUserPrincipal().getName());
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
    @RolesAllowed({"VIEW_ALL", "VIEW_USER"})
    public Uni<List<org.acme.authenticationService.dao.UserOnly>> getUser() {
        logger.info("get user");
        return userService.findAll();
    }
}
