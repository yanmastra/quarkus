package org.acme.product.data.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.product.data.entity.Product;

@ApplicationScoped
public class ProductRepository implements PanacheRepositoryBase<Product, String> {
}
