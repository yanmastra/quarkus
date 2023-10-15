package com.acme.authorization.json;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jboss.resteasy.reactive.RestForm;

import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SignInCredential {
    @RestForm("username")
    public String username;
    @RestForm("password")
    public String password;
    @JsonProperty("app_code")
    public String appCode;
    @JsonIgnore
    public Date expToken;

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
