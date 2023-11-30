package org.acme.orders.data.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.orders.data.entity.Product;

@ApplicationScoped
public class ProductRepository implements PanacheRepositoryBase<Product, String> {
}
