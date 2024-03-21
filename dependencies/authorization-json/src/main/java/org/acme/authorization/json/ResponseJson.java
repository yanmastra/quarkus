package org.acme.authorization.json;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseJson<E> {
    @JsonProperty("success")
    private boolean success;
    @JsonProperty("message")
    private String message;
    @JsonProperty("data")
    private E data;

    @JsonProperty("search")
    private String search;
    @JsonProperty("page")
    private Integer page = null;
    @JsonProperty("size")
    private Integer size = null;
    @JsonProperty("total_data")
    private Integer totalData = null;

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

    @JsonIgnore
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    @JsonIgnore
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @JsonIgnore
    public E getData() {
        return data;
    }

    public void setData(E data) {
        this.data = data;
    }

    @JsonIgnore
    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    @JsonIgnore
    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }
    @JsonIgnore
    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    @JsonIgnore
    public int getTotalData() {
        return totalData;
    }

    public void setTotalData(int totalData) {
        this.totalData = totalData;
    }

    @JsonIgnore
    @Override
    public String toString() {
        return "ResponseJson{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", data=" + (data == null ? null : data.getClass().getName()) +
                '}';
    }
}
