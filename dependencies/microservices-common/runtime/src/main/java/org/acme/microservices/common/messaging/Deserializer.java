package org.acme.microservices.common.messaging;

import com.acme.authorization.security.Authorizer;
import com.acme.authorization.security.HttpAuthorizer;
import com.acme.authorization.utils.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.logging.Logger;

public class Deserializer<T extends MessagingQuote<?>> implements org.apache.kafka.common.serialization.Deserializer<T> {
    private final Logger logger = Logger.getLogger(Deserializer.class);
    private HttpAuthorizer defaultAuthorizerPrior = null;

    private Authorizer getDefaultAuthorizerPrior() {
        if (defaultAuthorizerPrior == null) {
            String authorizationUrl = ConfigProvider.getConfig().getValue("authorization.service-url", String.class);
            if (StringUtils.isBlank(authorizationUrl)) authorizationUrl = "http://localhost:10001/auth";

            logger.info("authorizationUrl:"+authorizationUrl);
            defaultAuthorizerPrior = new HttpAuthorizer.Builder()
                    .setObjectMapper(JsonUtils.getObjectMapper())
                    .setUrl(authorizationUrl)
                    .isShowErrorLog(true)
                    .build();
        }
        return defaultAuthorizerPrior;
    }

    private final StringDeserializer deserializer;

    public Deserializer() {
        deserializer = new StringDeserializer();
    }

    @Override
    public T deserialize(String s, byte[] bytes) {
        String sData = deserializer.deserialize(s, bytes);
        T data = null;

        Class<T> properClass = getProperClass();

        if (properClass != null) {
            data = JsonUtils.fromJson(sData, properClass);
        }

        if (data == null) {
            TypeReference<T> properTypeReference = getProperTypeReference();
            if (properTypeReference != null) data = JsonUtils.fromJson(sData, properTypeReference);
        }

        if (data == null) {
            data = JsonUtils.fromJson(sData, new TypeReference<>() {
            });
        }

        try {
            data.principal = getDefaultAuthorizerPrior().authorize(data.accessToken);
            return data;
        } catch (Throwable e) {
            logger.error("Insecure message: topic:"+s+", authorization status: "+e.getMessage(), e);
        }
        return data;
    }

    protected Class<T> getProperClass() {
        return null;
    }

    protected TypeReference<T> getProperTypeReference() {
        return null;
    }
}
