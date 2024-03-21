package org.acme.authorization.json;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public abstract class User implements Serializable {
    @JsonProperty("id")
    private String id;
    @JsonProperty("username")
    private String username;
    @JsonProperty("email")
    private String email;
    @JsonProperty("name")
    private String name;
    @JsonProperty("created_at")
    private Date createdAt;
    @JsonProperty("created_by")
    private String createdBy;
    @JsonProperty("updated_at")
    private Date updatedAt;
    @JsonProperty("updated_by")
    private String updatedBy;
    @JsonProperty("deleted_at")
    private Date deletedAt;
    @JsonProperty("deleted_by")
    private String deletedBy;

    @JsonProperty("additional_data")
    private Map<String, AdditionalUserDataJson> additionalData;

    public User() {
    }

    public User(String id, String username, String email, String name) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.name = name;
    }

    @JsonIgnore
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @JsonIgnore
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @JsonIgnore
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @JsonIgnore
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonIgnore
    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @JsonIgnore
    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    @JsonIgnore
    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    @JsonIgnore
    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    @JsonIgnore
    public Date getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Date deletedAt) {
        this.deletedAt = deletedAt;
    }

    @JsonIgnore
    public String getDeletedBy() {
        return deletedBy;
    }

    public void setDeletedBy(String deletedBy) {
        this.deletedBy = deletedBy;
    }

    public Map<String, AdditionalUserDataJson> getAdditionalData() {
        if (additionalData == null) additionalData = new HashMap<>();
        return additionalData;
    }

    /**
     * to set additional data of User,
     * @param additionalData additional data with format Map&#60app_code__field_code, UserAdditionalData&#62
     */
    public void setAdditionalData(Map<String, AdditionalUserDataJson> additionalData) {
        this.additionalData = additionalData;
    }

    public String getAdditionalValue(String fieldCode) {
        if (StringUtils.isEmpty(fieldCode)) throw new IllegalArgumentException(getClass().getName()+".getAdditionalValue(String fieldCode), 'fieldCode' can't be empty!");
        String[] appAndField = fieldCode.split("__");
        if (appAndField.length == 1) {
            return getAdditionalData().entrySet().stream()
                    .filter(entry -> fieldCode.equals(entry.getKey().substring(entry.getKey().indexOf("__"))))
                    .map(entry -> entry.getValue().getValue())
                    .findFirst().orElse(null);
        } else if (appAndField.length == 2) {
            AdditionalUserDataJson data = getAdditionalData().get(fieldCode);
            if (data != null) return data.getValue();
        }
        return null;
    }

    @JsonIgnore
    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", createdAt=" + createdAt +
                ", createdBy='" + createdBy + '\'' +
                ", updatedAt=" + updatedAt +
                ", updatedBy='" + updatedBy + '\'' +
                ", deletedAt=" + deletedAt +
                ", deletedBy='" + deletedBy + '\'' +
                '}';
    }
}
