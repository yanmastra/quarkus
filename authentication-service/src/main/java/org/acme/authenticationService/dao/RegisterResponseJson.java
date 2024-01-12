package org.acme.authenticationService.dao;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RegisterResponseJson {
    @JsonProperty("otp_key")
    private String otpKey;
    @JsonProperty("process_id")
    private String processId;

    @JsonProperty("user_data")
    private RegisterRequestJson userData;
    @JsonProperty("temporary_access_token")
    private String temporaryAccessToken;

    public RegisterResponseJson(String otpKey, String processId) {
        this.otpKey = otpKey;
        this.processId = processId;
    }

    public RegisterResponseJson() {
    }

    public String getOtpKey() {
        return otpKey;
    }

    public void setOtpKey(String otpKey) {
        this.otpKey = otpKey;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public RegisterRequestJson getUserData() {
        return userData;
    }

    public void setUserData(RegisterRequestJson userData) {
        this.userData = userData;
    }

    public String getTemporaryAccessToken() {
        return temporaryAccessToken;
    }

    public void setTemporaryAccessToken(String temporaryAccessToken) {
        this.temporaryAccessToken = temporaryAccessToken;
    }
}
