package org.acme.authorization.json;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdditionalUserDataJson {
    @JsonProperty("id")
    private String id;
    @JsonProperty("value")
    private String value;

    public AdditionalUserDataJson() {
    }

    public AdditionalUserDataJson(String id, String value) {
        this.id = id;
        this.value = value;
    }

    @JsonIgnore
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @JsonIgnore
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
