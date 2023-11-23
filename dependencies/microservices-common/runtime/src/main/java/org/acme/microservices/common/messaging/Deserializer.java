package org.acme.microservices.common.messaging;

import com.acme.authorization.security.Authorizer;
import com.acme.authorization.security.HttpAuthorizer;
import com.fasterxml.jackson.core.type.TypeReference;
import com.acme.authorization.utils.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.logging.Logger;

import java.util.Map;

public class Deserializer implements org.apache.kafka.common.serialization.Deserializer<String> {
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
    public void configure(Map<String, ?> configs, boolean isKey) {
        deserializer.configure(configs, isKey);
    }

    @Override
    public String deserialize(String s, byte[] bytes) {
        String sData = deserializer.deserialize(s, bytes);
        try {
            MessagingQuote<?> data = JsonUtils.fromJson(sData, new TypeReference<>() {
            });
            data.principal = getDefaultAuthorizerPrior().authorize(data.accessToken);
            return JsonUtils.toJson(data);
        } catch (Throwable e) {
            logger.error("Insecure message: topic:"+s+", authorization status: "+e.getMessage(), e);
        }
        return sData;
    }
}
