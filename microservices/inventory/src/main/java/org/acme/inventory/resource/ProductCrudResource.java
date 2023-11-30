package org.acme.inventory.resource;

import com.acme.authorization.security.UserPrincipal;
import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import org.acme.inventory.data.entity.Category;
import org.acme.inventory.data.entity.Product;
import org.acme.inventory.data.entity.Unit;
import org.acme.inventory.data.repository.ProductRepository;
import org.acme.inventory.json.ProductJson;
import org.acme.microservices.common.crud.CrudEndpoint;
import org.acme.microservices.common.crud.WriteType;
import org.acme.microservices.common.messaging.MessagingQuote;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

@Path("/api/v1/product")
public class ProductCrudResource extends CrudEndpoint<Product, ProductJson> {

    @Inject
    ProductRepository repository;
    @Inject
    Logger logger;

    @Inject
    @Channel("productUpdate")
    Emitter<MessagingQuote<ProductJson>> productUpdateChannel;

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

        Uni<Product> productUni = Uni.createFrom().item(entity);
        if (StringUtils.isNotBlank(productJson.getUnitId())) {
            productUni = Unit.findById(productJson.getUnitId()).map(unit -> {
                entity.setUnit((Unit) unit);
                return entity;
            });
        }

        if (StringUtils.isNotBlank(productJson.getCategoryId())) {
            productUni = Category.findById(productJson.getCategoryId()).map(ctg -> {
                entity.setCategory((Category) ctg);
                return entity;
            });
        }

        logger.info("update product:"+entity);
        return productUni;
    }

    @Override
    protected void onWriteSuccess(ProductJson productJson, UserPrincipal principal, WriteType type) {
        MessagingQuote<ProductJson> data = new MessagingQuote<>();
        data.actionCode = type.name();
        data.data = productJson;
        if (principal != null)
            data.accessToken = principal.getAccessToken();
        logger.info("sending message to 'productUpdate' channel, msg:"+data);
        productUpdateChannel.send(data);
    }
}
