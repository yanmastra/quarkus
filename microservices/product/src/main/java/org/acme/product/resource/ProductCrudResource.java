package org.acme.product.resource;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import org.acme.crud.endpoint.CrudEndpoint;
import org.acme.product.data.entity.Product;
import org.acme.product.data.entity.Unit;
import org.acme.product.data.repository.ProductRepository;
import org.acme.product.json.ProductJson;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;

@Path("/api/v1/product")
public class ProductCrudResource extends CrudEndpoint<Product, ProductJson> {

    @Inject
    ProductRepository repository;
    @Inject
    Logger logger;

    @Override
    protected PanacheRepositoryBase<Product, String> getRepository() {
        return repository;
    }

    @Override
    protected ProductJson fromEntity(Product entity) {
        return ProductJson.fromEntity(entity);
    }

    @Override
    protected Product toEntity(ProductJson productJson) {
        return productJson.toEntity();
    }

    @Override
    protected Uni<Product> update(Product entity, ProductJson productJson) {
        entity.setName(productJson.getName());
        entity.setPrice(productJson.getPrice());
        entity.setCode(productJson.getCode());
        entity.setCogs(productJson.getCogs());
        entity.setStock(productJson.getStock());
        entity.setStockOnHold(productJson.getStockOnHold());
        entity.setStockOutstanding(productJson.getStockOutstanding());

        if (StringUtils.isNotBlank(productJson.getUnitId())) {
            return Unit.findById(productJson.getUnitId()).map(unit -> {
                entity.setUnit((Unit) unit);
                logger.info("update product:"+entity);
                return entity;
            });
        }
        return Uni.createFrom().item(entity);
    }
}
