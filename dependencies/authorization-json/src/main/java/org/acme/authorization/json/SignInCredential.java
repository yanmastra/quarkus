package org.acme.authorization.json;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SignInCredential implements Credential {
    @JsonProperty("username")
    private String username;
    @JsonProperty("password")
    private String password;
    @JsonProperty("app_code")
    private String appCode;
    @JsonIgnore
    private Date expToken;

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
                ", password=''********'" +
                ", appCode='" + appCode + '\'' +
                '}';
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getAppCode() {
        return appCode;
    }

    @Override
    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }

    @Override
    public Date getExpToken() {
        return expToken;
    }

    @Override
    public void setExpToken(Date expToken) {
        this.expToken = expToken;
    }
}
