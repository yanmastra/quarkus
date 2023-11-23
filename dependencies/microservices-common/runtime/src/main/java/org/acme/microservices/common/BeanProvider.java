package org.acme.microservices.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ext.Provider;
import com.acme.authorization.utils.JsonUtils;

@Provider
public class BeanProvider {

    @ApplicationScoped
    public ObjectMapper provideObjectMapper() {
        return JsonUtils.getObjectMapper();
    }
}
