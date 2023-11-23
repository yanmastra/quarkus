package com.acme.authorization.json;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseJson<E> {
    private boolean success;
    private String message;
    private E data;

    public ResponseJson() {
    }

    public ResponseJson(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public ResponseJson(boolean success, String message, E data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public ResponseJson(E data) {
        this.data = data;
        this.success = true;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public E getData() {
        return data;
    }

    public void setData(E data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "ResponseJson{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}
