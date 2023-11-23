package com.acme.authorization.utils;

import com.acme.authorization.json.UserOnly;
import com.acme.authorization.security.UserPrincipal;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JsonUtils {
    private JsonUtils() {
    }

    private static ObjectMapper objectMapper;
    private static Logger logger = Logger.getLogger(JsonUtils.class);

    public static ObjectMapper getObjectMapper() {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
            objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            objectMapper.configure(SerializationFeature.FAIL_ON_UNWRAPPED_TYPE_IDENTIFIERS, false);
            objectMapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            objectMapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);

            SimpleModule module = new SimpleModule();
            module.addDeserializer(UserPrincipal.class, new UserPrincipalDeserializer());
            objectMapper.registerModule(module);
        }

        return objectMapper;
    }

    public static String toJson(Object object, boolean throwException) {
        try {
            return getObjectMapper().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            if (throwException) throw new RuntimeException(e);
            else {
                logger.error(e.getMessage(), e);
                return null;
            }
        }
    }

    public static String toJson(Object object) {
        return toJson(object, false);
    }

    public static <E> E fromJson(String json, TypeReference<E> typeReference, boolean throwException) {
        try {
            return getObjectMapper().readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            if (throwException) throw new RuntimeException(e);
            else {
                logger.error(e.getMessage(), e);
                return null;
            }
        }
    }

    public static <E> E fromJson(String json, Class<E> eClass, boolean throwException) {
        try {
            return getObjectMapper().readValue(json, eClass);
        } catch (JsonProcessingException e) {
            if (throwException) throw new RuntimeException(e);
            else {
                logger.error(e.getMessage(), e);
                return null;
            }
        }
    }

    public static <E> E fromJson(String json, Class<E> eClass) {
        return fromJson(json, eClass, false);
    }

    public static <E> E fromJson(String json, TypeReference<E> typeReference) {
        return fromJson(json, typeReference, false);
    }

    private static class UserPrincipalDeserializer extends StdDeserializer<UserPrincipal> {
        protected UserPrincipalDeserializer(Class<UserPrincipal> vc) {
            super(vc);
        }

        protected UserPrincipalDeserializer() {
            super((Class<UserPrincipal>) null);
        }

        @Override
        public UserPrincipal deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
            JsonNode jn = jsonParser.getCodec().readTree(jsonParser);

            UserOnly user = null;
            JsonNode jnUser = jn.get("user");
            if (jnUser != null) {
                user = jsonParser.getCodec().treeToValue(jnUser, UserOnly.class);
            }
            List<String> roles = new ArrayList<>();
            JsonNode role = jn.get("allowed_roles");
            if (role != null) {
                if (role instanceof ArrayNode arrayNode) {
                    for (int i = 0; i < arrayNode.size(); i++) {
                        roles.add(arrayNode.get(i).asText());
                    }
                }
            }

            JsonNode jnAppCode = jn.get("app_code");
            String appCode = null;
            if (jnAppCode != null)
                appCode = jnAppCode.asText();

            String accessToken = null;
            JsonNode jnToken = jn.get("access_token");
            if (jnToken != null)
                accessToken = jnToken.asText();

            return new UserPrincipal(user, roles, appCode, accessToken);
        }
    }
}
