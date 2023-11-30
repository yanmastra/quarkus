package org.acme.orders.life.data;

import com.fasterxml.jackson.core.type.TypeReference;
import org.acme.microservices.common.messaging.Deserializer;
import org.acme.microservices.common.messaging.MessagingQuote;
import org.acme.orders.data.entity.Product;

public class ProductDeserializer extends Deserializer<MessagingQuote<Product>> {
    @Override
    protected TypeReference<MessagingQuote<Product>> getProperTypeReference() {
        return new TypeReference<MessagingQuote<Product>>() {
        };
    }
}
