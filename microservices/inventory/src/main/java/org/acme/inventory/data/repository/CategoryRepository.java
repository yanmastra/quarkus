package org.acme.inventory.data.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.inventory.data.entity.Category;

@ApplicationScoped
public class CategoryRepository implements PanacheRepositoryBase<Category, String> {
}
