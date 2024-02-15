package org.acme.authenticationService.resources.api.v1;

import com.acme.authorization.json.ResponseJson;
import com.acme.authorization.security.UserPrincipal;
import com.acme.authorization.security.UserSecurityContext;
import com.acme.authorization.utils.ValidationUtils;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.handler.HttpException;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.acme.authenticationService.dao.UpdatePasswordRequest;
import org.acme.authenticationService.dao.UserOnly;
import org.acme.authenticationService.services.UserService;
import org.jboss.logging.Logger;

@Path("/api/v1/user")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject
    UserService userService;
    @Inject
    Logger logger;

    @RolesAllowed({"CREATE_USER", "CREATE_OWN_USER"})
    @POST
    public Uni<Response> create(UserOnly user, @Context ContainerRequestContext context) {
        if (!ValidationUtils.isEmail(user.getEmail())) {
            throw new HttpException(HttpResponseStatus.BAD_REQUEST.code(), "Incorrect email format");
        }

        try {
            return userService.saveUser(user, context)
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
                    .onItem().transform(r -> Response.ok(r).build())
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
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> getUser(
            @QueryParam("page") Integer page,
            @QueryParam("size") Integer size,
            @QueryParam("search") String search,
            @Context SecurityContext context
    ) {
        logger.info("get user");
        if (page == null || page < 1) page = 1;
        if (size == null) size = 20;
        size = Math.max(size, 5);

        return userService.findAll(page, size, search, UserPrincipal.valueOf(context))
                .map(result -> Response.ok(result).build());
    }

    @GET
    @Path("profile")
    public Uni<ResponseJson<UserOnly>> getProfile(@Context SecurityContext context) {
        return Uni.createFrom().item(UserPrincipal.valueOf(context))
                .map(principal -> {
                    com.acme.authorization.json.UserOnly userOnly = UserPrincipal.valueOf(context).getUser();
                    ResponseJson<UserOnly> responseJson = new ResponseJson<>(true, null);
                    responseJson.setData(new UserOnly(userOnly));
                    responseJson.getData().setRolesIds(principal.getUser().getRolesIds());
                    return responseJson;
                });
    }

    @POST
    @RolesAllowed("CHANGE_OWN_PASSWORD")
    @Path("update_password")
    public Uni<Response> updatePassword(UpdatePasswordRequest request, @Context SecurityContext context) {
        return userService.updatePassword(request, UserPrincipal.valueOf(context))
                .onItem().transform(r -> Response.ok(r).build())
                .onFailure().transform(throwable -> {
                    logger.error(throwable.getMessage(), throwable);
                    return new HttpException(500, throwable.getMessage());
                });
    }
}
