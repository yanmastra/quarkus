package org.acme.inventory.resource;


import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.acme.microservices.common.crud.CrudEndpoint;
import org.acme.inventory.data.entity.Unit;
import org.acme.inventory.data.repository.UnitRepository;
import org.jboss.logging.Logger;

@Path("/api/v1/unit")
public class UnitResource  extends CrudEndpoint<Unit, Unit> {

    @Inject
    UnitRepository repository;
    @Inject
    Logger logger;


    @Override
    protected PanacheRepositoryBase<Unit, String> getRepository() {
        return repository;
    }

    @Override
    protected Unit fromEntity(Unit entity) {
        return entity;
    }

    @Override
    protected Unit toEntity(Unit uni) {
        return uni;
    }

    @Override
    protected Uni<Unit> update(Unit entity, Unit uni) {
        entity.setName(uni.getName());
        return Uni.createFrom().item(entity);
    }

    @Override
    public Uni<Response> create(Unit unit, SecurityContext context) {
        logger.info(unit.getName());
        return super.create(unit, context);
    }
}
