package com.acme.authorization.security;

import org.acme.authorization.json.ResponseJson;
import com.acme.authorization.utils.UrlUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.quarkus.runtime.util.StringUtil;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.handler.HttpException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HttpAuthorizer implements Authorizer {

    private String authorizationUrl;
    private ObjectMapper objectMapper;
    private final Logger logger = Logger.getLogger(HttpAuthorizer.class);

    private boolean showErrorLog = true;

    @Override
    public UserPrincipal authorize(String accessToken) {
        try {
            Map<String, String> headers = new HashMap<>();
            headers.put(HttpHeaders.AUTHORIZATION, accessToken);
            headers.put(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
            String response = UrlUtils.call(HttpMethod.GET, authorizationUrl, null, headers, showErrorLog);
            ResponseJson<UserPrincipal> responseJson = objectMapper.readValue(response, new TypeReference<>(){});
            return responseJson.getData();
        } catch (IOException e) {
            if (showErrorLog) logger.error(e.getMessage(), e);
        }
        throw new HttpException(HttpResponseStatus.UNAUTHORIZED.code(), "Unauthorized");
    }

    @Override
    public int getPriority() {
        return 999;
    }

    public static class Builder{
        private final HttpAuthorizer authorizer;

        public Builder(){
            authorizer = new HttpAuthorizer();
        }

        public Builder setUrl(String url) {
            this.authorizer.authorizationUrl = url;
            return this;
        }

        public Builder setObjectMapper(ObjectMapper objectMapper) {
            this.authorizer.objectMapper = objectMapper;
            return this;
        }

        public Builder isShowErrorLog(boolean b) {
            this.authorizer.showErrorLog = b;
            return this;
        }

        public HttpAuthorizer build() {
            if (StringUtil.isNullOrEmpty(authorizer.authorizationUrl))
                throw new IllegalArgumentException("Authorization URL couldn't be null");

            if (authorizer.objectMapper == null) {
                authorizer.objectMapper = new ObjectMapper();
            }
            return authorizer;
        }
    }
}
