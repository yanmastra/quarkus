package org.acme.microservices.common.messaging;

import com.acme.authorization.security.UserPrincipal;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessagingQuote<E> {
    @JsonProperty("access_token")
    public String accessToken;
    public E data;
    @JsonProperty("action_code")
    public String actionCode;
    public UserPrincipal principal;

    public MessagingQuote() {
    }

    @Override
    public String toString() {
        return "MessagingQuote{" +
                "accessToken='" + accessToken + '\'' +
                ", actionCode='" + actionCode + '\'' +
                ", data=" + data +
                ", principal=" + principal +
                '}';
    }
}
