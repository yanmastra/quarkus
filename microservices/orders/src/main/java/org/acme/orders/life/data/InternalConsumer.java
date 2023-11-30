package org.acme.orders.life.data;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.microservices.common.messaging.MessagingQuote;
import org.acme.orders.data.entity.Product;
import org.acme.orders.data.repository.ProductRepository;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;

@ApplicationScoped
public class InternalConsumer {

    @Inject
    Logger logger;

    @Inject
    ProductRepository productRepository;
    @Inject
    Mutiny.SessionFactory sf;

    @Incoming("productUpdate")
    public void productUpdate(MessagingQuote<Product> data) {
        saveProduct(data.data);
    }

    private void saveProduct(Product data) {
        sf.withTransaction((session, transaction) -> {
            Uni<?> productUni = session.find(Product.class, data.getId())
                    .chain(result -> {
                        if (result != null) {
                            result.setCategoryId(data.getCategoryId());
                            result.setUnitId(data.getUnitId());
                            result.setName(data.getName());
                            result.setPrice(data.getPrice());
                            result.setCogs(data.getCogs());
                            result.setStock(data.getStock());
                            result.setStockOnHold(data.getStockOnHold());
                            result.setStockOutstanding(data.getStockOutstanding());
                        } else result = data;
                        return productRepository.persist(result);
                    });
            return productUni.onItem().invoke(product -> logger.info("product updated:"+product))
                    .onFailure().invoke(throwable -> logger.error("Product update failed:" + throwable.getMessage(), throwable));
        }).subscribe().with(r -> logger.info("PRODUCT SAVED:"+r));
    }
}
