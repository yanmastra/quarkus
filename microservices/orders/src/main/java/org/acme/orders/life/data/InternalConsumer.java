package org.acme.orders.life.data;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

@ApplicationScoped
public class InternalConsumer {

    @Inject
    Logger logger;

    @Incoming("quote-requests")
    public void quoteRequest(String text) {
        logger.info("received topic: quote-requests, message:"+text);
    }
}
