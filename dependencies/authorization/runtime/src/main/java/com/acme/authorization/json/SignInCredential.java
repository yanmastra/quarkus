package com.acme.authorization.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SignInCredential {
    public String username;
    public String password;
    @JsonProperty("app_code")
    public String appCode;

    public SignInCredential() {
    }

    public SignInCredential(String username, String password, String appCode) {
        this.username = username;
        this.password = password;
        this.appCode = appCode;
    }

    @Override
    public String toString() {
        return "SignInCredential{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", appCode='" + appCode + '\'' +
                '}';
    }
}
