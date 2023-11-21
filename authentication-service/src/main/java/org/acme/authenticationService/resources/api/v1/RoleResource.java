package org.acme.authenticationService.resources.api.v1;


import com.acme.authorization.security.UserSecurityContext;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.handler.HttpException;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.authenticationService.dao.RoleAddPermissionRequest;
import org.acme.authenticationService.dao.RoleOnly;
import org.acme.authenticationService.services.RoleService;
import org.acme.authenticationService.services.Validator;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.List;

@Path("/api/v1/role")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoleResource {
    @Inject
    RoleService service;
    @Inject
    Logger logger;

    @RolesAllowed({"VIEW_ALL", "VIEW_ROLE"})
    @GET
    public Uni<List<RoleOnly>> get(@Context UserSecurityContext context) {
        try {
            return service.findByApp(context.getUserPrincipal().getAppCode());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Uni.createFrom().item(new ArrayList<>());
        }
    }

    @RolesAllowed({"VIEW_ALL", "VIEW_ROLE"})
    @GET
    @Path("{code}")
    public Uni<Response> getOne(@PathParam("code") String code, @Context UserSecurityContext context) {
        try {
            return service.findOne(context.getUserPrincipal().getAppCode(), code).onItem().transform(i -> Response.ok().entity(i).build())
                    .onFailure().invoke(e -> logger.error(e.getMessage(), e));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Uni.createFrom().item(Response.status(Response.Status.NOT_FOUND).build());
        }
    }

    @RolesAllowed({"UPDATE_ROLE"})
    @PUT
    @Path("{code}")
    public Uni<Response> update(RoleOnly data, @PathParam("code") String code, @Context UserSecurityContext context) {
        try {
            data.setUpdatedBy(context.getUserPrincipal().getName());
            return service.update(context.getUserPrincipal().getAppCode(), code, data).map(response -> {
                if (response != null) return Response.ok().entity(response).build();
                else return Response.status(Response.Status.NOT_FOUND).build();
            }).onFailure().invoke(e -> logger.error(e.getMessage(), e));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Uni.createFrom().item(Response.status(Response.Status.NOT_FOUND).build());
        }
    }

    @RolesAllowed({"ROLE_ADD_PERMISSION"})
    @PUT
    @Path("{code}/add_permission")
    public Uni<Response> addPermission(RoleAddPermissionRequest data, @PathParam("code") String code, @Context UserSecurityContext context) {
        try {
            return service.addPermission(context.getUserPrincipal().getAppCode(), code, data).map(response -> {
                if (response != null) return Response.ok().entity(response).build();
                else return Response.status(Response.Status.NOT_FOUND).build();
            }).onFailure().transform(e -> new HttpException(500, e.getMessage(), e));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Uni.createFrom().item(Response.status(Response.Status.NOT_FOUND).build());
        }
    }

    @RolesAllowed({"CREATE_ROLE"})
    @POST
    public Uni<Response> create(RoleOnly role, @Context UserSecurityContext context) {
        try {
            role.setAppCode(context.getUserPrincipal().getAppCode());
            role.setCreatedBy(context.getUserPrincipal().getName());
            if (!Validator.validateRole(role)) {
                throw new IllegalArgumentException("Role code is not allowed");
            }

            return service.create(role)
                    .onItem().transform(r -> Response.status(Response.Status.OK).entity(r).build())
                    .onFailure().transform(throwable -> {
                        logger.error(throwable.getMessage(), throwable);
                        return new HttpException(500, throwable.getMessage());
                    });
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Uni.createFrom().item(Response.status(500).entity(e).build());
        }
    }
}
