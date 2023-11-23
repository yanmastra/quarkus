package org.acme.inventory.resource;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import org.acme.microservices.common.crud.CrudEndpoint;
import org.acme.inventory.data.entity.Category;
import org.acme.inventory.data.repository.CategoryRepository;

@Path("/api/v1/category")
public class CategoryCrudResource extends CrudEndpoint<Category, Category> {

    @Inject
    CategoryRepository repository;

    @Override
    protected PanacheRepositoryBase<Category, String> getRepository() {
        return repository;
    }

    @Override
    protected Category fromEntity(Category entity) {
        return entity;
    }

    @Override
    protected Category toEntity(Category category) {
        return category;
    }

    @Override
    protected Uni<Category> update(Category entity, Category category) {
        entity.setName(category.getName());
        return Uni.createFrom().item(entity);
    }
}
