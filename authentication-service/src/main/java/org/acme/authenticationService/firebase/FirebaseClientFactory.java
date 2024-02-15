package org.acme.authenticationService.firebase;

import io.quarkus.arc.DefaultBean;
import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.client.api.ClientLogger;

import java.net.URI;

@ApplicationScoped
public class FirebaseClientFactory {

    private FirebaseAuthClient authClient;
    private FirebaseTokenClient tokenClient;
    private final ClientLogger clientLogger = new ClientLogger(){

        @Override
        public void setBodySize(int bodySize) {

        }

        @Override
        public void logResponse(HttpClientResponse response, boolean redirect) {
            logger.info("<-- " + response.statusCode()+" "+ response.request().absoluteURI());
        }

        @Override
        public void logRequest(HttpClientRequest request, Buffer body, boolean omitBody) {
            logger.info(request.getMethod() + " " + request.absoluteURI() +
                    request.query() +
                    "\nbody:"+ new String(body.getBytes()));
        }
    };

    @Inject
    Logger logger;

    @Singleton
    @DefaultBean
    public FirebaseAuthClient createFirebaseAuthClient() {
        if (authClient == null) {
            authClient = QuarkusRestClientBuilder.newBuilder()
                    .baseUri(URI.create("https://identitytoolkit.googleapis.com"))
                    .clientLogger(clientLogger)
                    .build(FirebaseAuthClient.class);
        }
        return authClient;
    }

    @Singleton
    @DefaultBean
    public FirebaseTokenClient createFirebaseTokenClient() {
        if (tokenClient == null) {
            tokenClient = QuarkusRestClientBuilder.newBuilder()
                    .baseUri(URI.create("https://securetoken.googleapis.com"))
                    .clientLogger(clientLogger)
                    .build(FirebaseTokenClient.class);
        }
        return tokenClient;
    }
}
