package org.acme.product.resource;


import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import org.acme.crud.endpoint.CrudEndpoint;
import org.acme.product.data.entity.Unit;
import org.acme.product.data.repository.UnitRepository;

@Path("/api/v1/unit")
public class UnitResource  extends CrudEndpoint<Unit, Unit> {

    @Inject
    UnitRepository repository;

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
}
