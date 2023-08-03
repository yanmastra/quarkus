package org.acme.crudReactiveHibernate.provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.logging.Log;
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
        org.acme.crudReactiveHibernate.dao.Response response = new org.acme.crudReactiveHibernate.dao.Response(
                false,
                exception.getCause() != null ? exception.getCause().getMessage(): exception.getMessage()
        );
        try {
            return Response.status(500).entity(objectMapper.writeValueAsString(response)).build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
