package org.acme.authenticationService.resources.api.v1;

import com.acme.authorization.json.ResponseJson;
import com.acme.authorization.security.UserSecurityContext;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.authenticationService.dao.Permission;
import org.acme.authenticationService.services.PermissionService;
import org.jboss.logging.Logger;

import java.util.List;

@Path("/api/v1/permission")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PermissionResource {
    @Inject
    PermissionService permissionService;

    @Inject
    Logger logger;

    @RolesAllowed({"VIEW_ALL", "VIEW_PERMISSION"})
    @GET
    public Uni<List<Permission>> get(@Context UserSecurityContext context) {
        return permissionService.findAll(context.getUserPrincipal().getAppCode());
    }

    @RolesAllowed({"VIEW_ALL", "VIEW_PERMISSION"})
    @GET
    @Path("{id}")
    public Uni<Permission> getOne(@PathParam("id") String id) {
        return permissionService.findOne(id);
    }
    @RolesAllowed({"CREATE_PERMISSION"})
    @POST
    public Uni<Response> create(Permission permission, @Context UserSecurityContext context) {
        permission.setAppCode(context.getUserPrincipal().getAppCode());
        return permissionService.create(permission)
                .onItem().transform(r -> Response.status(Response.Status.OK).entity(r).build());
    }

    @RolesAllowed({"UPDATE_PERMISSION"})
    @PUT
    @Path("{id}")
    public Uni<Permission> update(@PathParam("id") String id, Permission permission, @Context UserSecurityContext context) {
        try {
            permission.setAppCode(context.getUserPrincipal().getAppCode());
            return permissionService.update(id, permission);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Uni.createFrom().failure(e);
        }
    }

    @RolesAllowed({"DELETE_PERMISSION"})
    @DELETE
    @Path("{id}")
    public Uni<Response> delete(@PathParam("id") String id) {
        return permissionService.delete(id).onItem().transform(result ->
                Response.status(result ? 200 : 500)
                        .entity(new ResponseJson<>(result, result ? "Permission: %s has been deleted successfully".formatted(id) : "Unable to delete"))
                        .build()
        );
    }
}
