package com.acme.authorization.provider;

import com.acme.authorization.json.ResponseJson;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.logging.Log;
import io.vertx.ext.web.handler.HttpException;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ErrorMapper implements ExceptionMapper<RuntimeException> {

    @Inject
    ObjectMapper objectMapper;

    @Override
    public Response toResponse(RuntimeException exception) {
        Log.error(exception.getMessage(), exception);
        ResponseJson<?> responseJson = new ResponseJson<>(
                false,
                exception.getCause() != null ? exception.getCause().getMessage() : exception.getMessage()
        );
        try {
            if (exception instanceof HttpException httpException) {
                return Response.status(httpException.getStatusCode()).entity(objectMapper.writeValueAsString(responseJson)).build();
            } else
                return Response.status(500).entity(objectMapper.writeValueAsString(responseJson)).build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
