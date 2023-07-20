package org.acme.crudReactiveHibernate.resources;

import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.handler.HttpException;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.crudReactiveHibernate.dao.Permission;
import org.acme.crudReactiveHibernate.services.PermissionService;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.List;

@Path("/api/v1/permission")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PermissionResource {
    @Inject
    PermissionService permissionService;

    @Inject
    Logger logger;

    @GET
    public Uni<List<Permission>> get() {
        try {
            return permissionService.findAll();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Uni.createFrom().item(new ArrayList<>());
        }
    }

    @POST
    public Uni<Response> create(Permission permission) {
        try {
            return permissionService.create(permission)
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
