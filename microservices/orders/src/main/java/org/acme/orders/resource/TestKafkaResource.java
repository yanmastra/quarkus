package org.acme.orders.resource;

import org.acme.authorization.json.ResponseJson;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import org.acme.microservices.common.messaging.MessagingQuote;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

import java.util.Map;
import java.util.concurrent.CompletionStage;

@Path("api/v1/test_kafka")
public class TestKafkaResource {

    @Inject
    @Channel("inventory")
    Emitter<MessagingQuote<Map<String, Object>>> inventoryStockChannel;

    @Inject
    ObjectMapper objectMapper;
    @Inject
    Logger logger;

    @POST
    public Uni<ResponseJson<String>> sendMessageKafka(MessagingQuote<Map<String, Object>> quote) {
        try {
            CompletionStage<Void> result  = inventoryStockChannel.send(quote);
            return Uni.createFrom().item(new ResponseJson<>(true, "message sent"));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Uni.createFrom().item(new ResponseJson<>(false, e.getMessage()));
        }
    }
}
