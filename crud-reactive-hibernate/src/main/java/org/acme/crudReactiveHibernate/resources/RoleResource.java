package org.acme.crudReactiveHibernate.resources;


import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.handler.HttpException;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.crudReactiveHibernate.dao.RoleOnly;
import org.acme.crudReactiveHibernate.services.RoleService;
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

    @GET
    @Path("/app/{appCode}")
    public Uni<List<RoleOnly>> get(@PathParam("appCode") String appCode) {
        try {
            return service.findByApp(appCode);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Uni.createFrom().item(new ArrayList<>());
        }
    }

    @GET
    @Path("/app/{appCode}/code/{code}")
    public Uni<Response> getOne(@PathParam("code") String code, @PathParam("appCode") String appCode) {
        try {
            return service.findOne(appCode, code).onItem().transform(i -> Response.ok().entity(i).build())
                    .onFailure().invoke(e -> logger.error(e.getMessage(), e));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Uni.createFrom().item(Response.status(Response.Status.NOT_FOUND).build());
        }
    }

    @PUT
    @Path("/app/{appCode}/code/{code}")
    public Uni<Response> update(RoleOnly data, @PathParam("code") String code, @PathParam("appCode") String appCode) {
        try {
            return service.update(appCode, code, data).map(response -> {
                if (response != null) return Response.ok().entity(response).build();
                else return Response.status(Response.Status.NOT_FOUND).build();
            }).onFailure().invoke(e -> logger.error(e.getMessage(), e));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Uni.createFrom().item(Response.status(Response.Status.NOT_FOUND).build());
        }
    }

    @POST
    public Uni<Response> create(RoleOnly role) {
        try {
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
