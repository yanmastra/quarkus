package org.acme.authorization.json;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthenticationResponse<E extends User> {
    @JsonProperty("access_token")
    public String accessToken;
    @JsonProperty("firebase_token")
    public String firebaseToken;
    @JsonProperty("refresh_token")
    public String refreshToken;

    @JsonProperty("user")
    public E user;

    public AuthenticationResponse() {
    }

    public AuthenticationResponse(String accessToken, String refreshToken, E user) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.user = user;
    }

    public AuthenticationResponse(String accessToken, String firebaseToken, String refreshToken, E user) {
        this.accessToken = accessToken;
        this.firebaseToken = firebaseToken;
        this.refreshToken = refreshToken;
        this.user = user;
    }

    @JsonIgnore
    @Override
    public String toString() {
        return "AuthenticationResponse{" +
                "accessToken='" + accessToken + '\'' +
                ", refreshToken='" + refreshToken + '\'' +
                ", user=" + (user == null ? null : user.getId()) +
                '}';
    }
}
