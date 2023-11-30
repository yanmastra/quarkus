package org.acme.inventory.life.data;

import io.smallrye.common.annotation.Blocking;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.microservices.common.messaging.MessagingQuote;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

import java.util.Map;
import java.util.Random;

@ApplicationScoped
public class InternalConsumer {

    private final Random random = new Random();

    @Inject
    Logger logger;

    @Incoming("inventory")
    @Blocking
    public void processStock(MessagingQuote<Map<String, Object>> stockText) throws InterruptedException {
        logger.info("received: topic: stock, text:"+stockText);
    }
}
