package com.acme.authorization.provider;

import com.acme.authorization.json.ResponseJson;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.ext.web.handler.HttpException;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.server.spi.AsyncExceptionMapperContext;
import org.jboss.resteasy.reactive.server.spi.ResteasyReactiveAsyncExceptionMapper;

@Singleton
public class ErrorMapper implements ResteasyReactiveAsyncExceptionMapper<Exception> {

    @Inject
    ObjectMapper objectMapper;
    @Inject
    Logger logger;
    @Inject
    ContainerRequestContext requestContext;

    @Override
    public void asyncResponse(Exception exception, AsyncExceptionMapperContext context) {
        logger.error(requestContext.getUriInfo().getPath()+"::"+exception.getMessage(), exception);
        ResponseJson<?> responseJson = new ResponseJson<>(
                false,
                exception.getCause() != null ? exception.getCause().getMessage() : exception.getMessage()
        );
        Response response = null;
        try {
            if (exception instanceof HttpException httpException) {
                responseJson.setMessage(httpException.getPayload());
                response = Response.status(httpException.getStatusCode()).entity(objectMapper.writeValueAsString(responseJson)).build();
            } else
                response = Response.status(500).entity(objectMapper.writeValueAsString(responseJson)).build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        context.setResponse(response);
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
