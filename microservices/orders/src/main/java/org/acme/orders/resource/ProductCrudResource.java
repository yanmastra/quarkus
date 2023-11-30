package org.acme.orders.resource;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.handler.HttpException;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.acme.microservices.common.crud.CrudEndpoint;
import org.acme.orders.data.entity.Product;
import org.acme.orders.data.repository.ProductRepository;

@Path("/api/v1/product")
public class ProductCrudResource extends CrudEndpoint<Product, Product> {

    @Inject
    ProductRepository repository;

    @Override
    protected PanacheRepositoryBase<Product, String> getRepository() {
        return repository;
    }

    @Override
    protected Product fromEntity(Product entity) {
        return entity;
    }

    @Override
    protected Product toEntity(Product product) {
        return product;
    }

    @Override
    protected Uni<Product> update(Product entity, Product product) {
        entity.setName(product.getName());
        entity.setPrice(product.getPrice());
        entity.setCogs(product.getCogs());
        entity.setStockOnHold(product.getStockOnHold());
        entity.setStockOutstanding(product.getStockOutstanding());
        entity.setStock(product.getStock());
        entity.setUnitId(product.getUnitId());
        entity.setCategoryId(product.getCategoryId());
        return Uni.createFrom().item(product);
    }

    @Override
    public Uni<Response> create(Product product, SecurityContext context) {
        throw new HttpException(HttpResponseStatus.FORBIDDEN.code(), "Request not allowed!");
    }

    @Override
    public Uni<Response> update(String id, Product product, SecurityContext context) {
        throw new HttpException(HttpResponseStatus.FORBIDDEN.code(), "Request not allowed!");
    }

    @Override
    public Uni<Response> delete(String id, SecurityContext context) {
        throw new HttpException(HttpResponseStatus.FORBIDDEN.code(), "Request not allowed!");
    }
}
