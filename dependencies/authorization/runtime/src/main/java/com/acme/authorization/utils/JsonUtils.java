package com.acme.authorization.utils;

import com.acme.authorization.json.UserOnly;
import com.acme.authorization.security.UserPrincipal;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.util.List;

public class JsonUtils {
    private JsonUtils() {
    }

    private static ObjectMapper objectMapper;
    private static Logger logger = Logger.getLogger(JsonUtils.class);

    public static ObjectMapper getObjectMapper() {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
            configure(objectMapper);
        }
        return objectMapper;
    }

    public static void setObjectMapper(ObjectMapper objectMapper) {
        JsonUtils.objectMapper = objectMapper;
//        configure(JsonUtils.objectMapper);
    }

    private static void configure(ObjectMapper objectMapper) {
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_UNWRAPPED_TYPE_IDENTIFIERS, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);

        SimpleModule module = new SimpleModule();
        module.addDeserializer(UserPrincipal.class, new UserPrincipalDeserializer());
        objectMapper.registerModule(module);
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
        public UserPrincipal deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            try {
                JsonNode jn = jsonParser.getCodec().readTree(jsonParser);
                UserPrincipalJson principalJson = jsonParser.getCodec().treeToValue(jn, UserPrincipalJson.class);
                return new UserPrincipal(principalJson.getUser(), principalJson.getAllowedRoles(), principalJson.getAppCode(), principalJson.getAccessToken());
            } catch (IOException e){
                logger.error(e.getMessage(), e);
                throw e;
            }
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private static class UserPrincipalJson {
        @JsonProperty("user")
        private UserOnly user;
        @JsonProperty("allowed_roles")
        private List<String> allowedRoles;

        @JsonProperty("app_code")
        private String appCode;
        @JsonProperty("access_token")
        private String accessToken;

        public UserPrincipalJson() {
        }

        public UserOnly getUser() {
            return user;
        }

        public void setUser(UserOnly user) {
            this.user = user;
        }

        public List<String> getAllowedRoles() {
            return allowedRoles;
        }

        public void setAllowedRoles(List<String> allowedRoles) {
            this.allowedRoles = allowedRoles;
        }

        public String getAppCode() {
            return appCode;
        }

        public void setAppCode(String appCode) {
            this.appCode = appCode;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }
    }
}
