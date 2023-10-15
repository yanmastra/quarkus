package com.acme.authorization.provider;

import com.acme.authorization.json.ResponseJson;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.ext.web.handler.HttpException;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
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
        logger.error(requestContext.getUriInfo().getPath()+"::"+exception.getMessage(), exception);

        MultivaluedMap<String, String> headers = requestContext.getHeaders();
        if (headers.containsKey(HttpHeaders.ACCEPT) && headers.getFirst(HttpHeaders.ACCEPT).equals(MediaType.APPLICATION_JSON)) {
            ResponseJson<?> responseJson = new ResponseJson<>(
                    false,
                    exception.getCause() != null ? exception.getCause().getMessage() : exception.getMessage()
            );
            Response response = null;
            try {
                if (exception instanceof HttpException httpException) {

                    responseJson.setMessage(httpException.getPayload());
                    response = Response.status(httpException.getStatusCode()).entity(objectMapper.writeValueAsString(responseJson)).build();
                }

                if (response == null)
                    response = Response.status(500).entity(objectMapper.writeValueAsString(responseJson)).build();
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            context.setResponse(response);

        } else {
            try (Stream<HtmlErrorMapper> errorMapperStream = htmlErrorMappers.stream()) {
                errorMapperStream.findFirst().ifPresent(htmlErrorMapper -> context.setResponse(htmlErrorMapper.getResponse(exception)));
            } catch (Exception e){
                logger.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public Response toResponse(Exception exception) {
        logger.error(exception.getMessage(), exception);
        ResponseJson<?> responseJson = new ResponseJson<>(
                false,
                exception.getCause() != null ? exception.getCause().getMessage() : exception.getMessage()
        );
        try {
            if (exception instanceof HttpException httpException) {
                responseJson.setMessage(httpException.getPayload());
                return Response.status(httpException.getStatusCode()).entity(objectMapper.writeValueAsString(responseJson)).build();
            } else
                return Response.status(500).entity(objectMapper.writeValueAsString(responseJson)).build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
