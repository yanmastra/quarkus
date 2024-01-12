package org.acme.authenticationService.dao;

import com.acme.authorization.json.AdditionalUserDataJson;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.acme.authenticationService.data.entity.AdditionalUserData;
import org.acme.authenticationService.data.entity.AuthUser;
import org.acme.authenticationService.data.entity.UserRole;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class UserOnly extends User {

    public UserOnly() {
    }

    public UserOnly(String id, String username, String email, String name) {
        super(id, username, email, name);
    }

    public UserOnly(com.acme.authorization.json.UserOnly general) {
        setId(general.getId());
        setEmail(general.getEmail());
        setUsername(general.getUsername());
        setRolesIds(general.getRolesIds());
        setAdditionalData(general.getAdditionalData());
        setCreatedBy(general.getCreatedBy());
    }

    @JsonIgnore
    @Override
    public AuthUser toDto() {
        AuthUser authUser = new AuthUser(getId(), getUsername(), getEmail(), getName());
        List<AdditionalUserData> additionalUserData = getAdditionalData().entrySet().stream().map(entry -> {
            String[] appAndField = entry.getKey().split("__");
            if (appAndField.length == 2) {
                return new AdditionalUserData(entry.getValue().getId(), authUser, appAndField[0], appAndField[1], entry.getValue().getValue());
            }
            return null;
        }).filter(Objects::nonNull).toList();
        authUser.setAdditionalUserData(additionalUserData);
        return authUser;
    }

    public static UserOnly fromDto(AuthUser authUser) {
        return fromDto(authUser, null);
    }

    public static UserOnly fromDto(AuthUser authUser, String appCode) {
        UserOnly nUser = new UserOnly(authUser.getId(), authUser.getUsername(), authUser.getEmail(), authUser.getName());
        authUser.getRoles().forEach(role -> {
            if (StringUtils.isNotBlank(appCode) && !role.getRole().getAppCode().equals(appCode)) return;

            if (nUser.rolesIds.containsKey(role.getRole().getAppCode()))
                nUser.rolesIds.get(role.getRole().getAppCode()).add(role.getRole().getCode());
            else {
                nUser.rolesIds.put(role.getRole().getAppCode(), new ArrayList<>(Collections.singletonList(role.getRole().getCode())));
            }
        });

        authUser.getAdditionalUserData().stream().filter(item -> StringUtils.isBlank(appCode) || appCode.equals(item.getAppCode()))
                        .forEach(item -> nUser.getAdditionalData().put(item.getAppCode()+"__"+item.getFieldCode(), new AdditionalUserDataJson(item.getId(), item.getValue())));

        nUser.setCreatedAt(authUser.getCreatedAt());
        nUser.setCreatedBy(authUser.getCreatedBy());
        nUser.setUpdatedAt(authUser.getUpdatedAt());
        nUser.setUpdatedBy(authUser.getUpdatedBy());
        nUser.setDeletedAt(authUser.getDeletedAt());
        nUser.setDeletedBy(authUser.getDeletedBy());
        return nUser;
    }

    @JsonProperty("roles_ids")
    private Map<String, List<String>> rolesIds = new HashMap<>();

    /**
     * to get app_code map with role code
     * @return value: Map&#60app_code, List&#60role_code&#62&#62
     */
    @JsonIgnore
    public Map<String, List<String>> getRolesIds() {
        return rolesIds;
    }

    public void setRolesIds(Map<String, List<String>> rolesIds) {
        this.rolesIds = rolesIds;
    }
}
