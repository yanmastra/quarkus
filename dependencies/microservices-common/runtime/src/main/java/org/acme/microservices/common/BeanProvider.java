package org.acme.microservices.common;

import com.acme.authorization.utils.JsonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ext.Provider;

@Provider
public class BeanProvider {

    @ApplicationScoped
    public ObjectMapper provideObjectMapper() {
        return JsonUtils.getObjectMapper();
    }
}
