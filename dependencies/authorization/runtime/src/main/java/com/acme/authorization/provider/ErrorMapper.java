package com.acme.authorization.provider;

import io.quarkus.runtime.util.StringUtil;
import io.vertx.ext.web.handler.HttpException;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.json.Json;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonValue;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.server.spi.AsyncExceptionMapperContext;
import org.jboss.resteasy.reactive.server.spi.ResteasyReactiveAsyncExceptionMapper;

import java.util.Optional;
import java.util.stream.Stream;

@Singleton
public class ErrorMapper implements ResteasyReactiveAsyncExceptionMapper<Exception> {

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

            JsonObjectBuilder job = Json.createObjectBuilder()
                    .add("success", false);
            if (StringUtil.isNullOrEmpty(message)) {
                job.add("message", JsonValue.NULL);
            } else {
                job.add("message", message);
            }
            String responsePayload = job.build().toString();

            context.setResponse(Response.status(status)
                    .entity(responsePayload)
                    .type(MediaType.APPLICATION_JSON)
                    .build());
        } else {
            try (Stream<HtmlErrorMapper> errorMapperStream = htmlErrorMappers.stream()) {
                Optional<HtmlErrorMapper> errorMapper = errorMapperStream.findFirst();
                errorMapper.ifPresent(htmlErrorMapper -> context.setResponse(htmlErrorMapper.getResponse(exception)));
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }
}
