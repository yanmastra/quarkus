package com.acme.authorization.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.ext.web.handler.HttpException;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.json.Json;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.server.spi.AsyncExceptionMapperContext;
import org.jboss.resteasy.reactive.server.spi.ResteasyReactiveAsyncExceptionMapper;

import java.util.stream.Stream;

@Singleton
public class ErrorMapper implements ResteasyReactiveAsyncExceptionMapper<Exception> {

    @Inject
    ObjectMapper objectMapper;
    @Inject
    Logger logger;
    @Inject
    ContainerRequestContext requestContext;

    @Inject
    Instance<HtmlErrorMapper> htmlErrorMappers;

    @Override
    public void asyncResponse(Exception exception, AsyncExceptionMapperContext context) {
        logger.error(requestContext.getUriInfo().getPath() + "::" + exception.getMessage(), exception, exception.getCause());

        String message = null;
        int status = 500;

        MultivaluedMap<String, String> headers = requestContext.getHeaders();
        if (headers.containsKey(HttpHeaders.ACCEPT) && headers.getFirst(HttpHeaders.ACCEPT).equals(MediaType.APPLICATION_JSON)) {
            if (exception instanceof HttpException httpException) {
                message = httpException.getPayload();
                status = httpException.getStatusCode();
            } else if (exception instanceof ClientErrorException clientError) {
                message = clientError.getMessage();
                status = clientError.getResponse().getStatus();
            } else if (exception instanceof SecurityException securityException) {
                message = securityException.getMessage();
                status = 403;
            } else {
                Throwable cause = exception.getCause();
                message = cause == null ? exception.getMessage() : cause.getMessage();
            }

            String responsePayload = Json.createObjectBuilder()
                    .add("success", false)
                    .add("message", message)
                    .build().toString();

            context.setResponse(Response.status(status)
                    .entity(responsePayload)
                    .type(MediaType.APPLICATION_JSON)
                    .build());
        } else {
            try (Stream<HtmlErrorMapper> errorMapperStream = htmlErrorMappers.stream()) {
                errorMapperStream.findFirst().ifPresent(htmlErrorMapper -> context.setResponse(htmlErrorMapper.getResponse(exception)));
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }
}
