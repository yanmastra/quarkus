package org.acme.microservices.common.crud;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.panache.common.Page;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.handler.HttpException;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.jboss.logging.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class CrudEndpoint<Entity extends CrudableEntity, Dao> {
    protected abstract PanacheRepositoryBase<Entity, String> getRepository();
    protected abstract Dao fromEntity(Entity entity);
    protected abstract Entity toEntity(Dao dao);
    protected abstract Uni<Entity> update(Entity entity, Dao dao);

    @Inject
    Logger logger;

    @GET
    @WithTransaction
    public Uni<List<Dao>> getList(
            @PathParam("page") Integer page,
            @PathParam("size") Integer size,
            @Context SecurityContext context
            ) {
        if (page == null || page <= 0) page = 1;
        if (size == null || size < 5) size = 5;

        return getRepository().findAll().page(Page.of(page -1, size))
                .list().map(list -> list.stream().map(this::fromEntity).toList());
    }

    @GET
    @Path("{id}")
    @WithTransaction
    public Uni<Dao> getList(
            @PathParam("id") String id,
            @Context SecurityContext context
    ) {
        return getRepository().findById(id).map(this::fromEntity);
    }

    @POST
    @WithTransaction
    public Uni<Response> create(Dao dao, @Context SecurityContext context) {
        try {
            Entity entity = toEntity(dao);
            return getRepository().persist(entity)
                    .onItem()
                    .transform(entity1 -> {
                        Map<String, Object> data = new HashMap<>();
                        data.put("success", true);
                        data.put("data", fromEntity(entity1));
                        return Response.ok(data).build();
                    });
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Uni.createFrom().item(Response.status(500)
                    .entity(Json.createObjectBuilder().add("success", false)
                            .add("message", e.getMessage())
                            .build()
                            .toString()
                    ).build());
        }
    }

    @PUT
    @Path("{id}")
    @WithTransaction
    public Uni<Response> update(
            @PathParam("id") String id,
            Dao dao,
            @Context SecurityContext context
    ) {
        try {
            Entity entity = toEntity(dao);
            entity.setId(id);
            return getRepository().findById(id)
                    .call(result -> {
                        if (result == null) throw new HttpException(404, "Product:"+id+" not found");
                        else {
                            return update(result, dao)
                                    .call(result1 -> getRepository().persist(result1));
                        }
                    })
                    .onItem()
                    .transform(entity1 -> {
                        Map<String, Object> data = new HashMap<>();
                        data.put("success", true);
                        data.put("data", fromEntity(entity1));
                        return Response.ok(data).build();
                    });
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Uni.createFrom().item(Response.status(500)
                    .entity(Json.createObjectBuilder().add("success", false)
                            .add("message", e.getMessage())
                            .build()
                            .toString()
                    ).build());
        }
    }

    @DELETE
    @Path("{id}")
    @WithTransaction
    public Uni<Response> delete(
            @PathParam("id") String id,
            @Context SecurityContext context
    ) {
        return getRepository().deleteById(id)
                .map(result -> {
                    JsonObject data = Json.createObjectBuilder()
                            .add("success", result)
                            .add("data", Json.createObjectBuilder().add("id", id).build())
                            .build();
                    if (result) {
                        return Response.ok(data.toString()).build();
                    } else {
                        return Response.status(500).entity(data.toString()).build();
                    }
                });
    }

}
