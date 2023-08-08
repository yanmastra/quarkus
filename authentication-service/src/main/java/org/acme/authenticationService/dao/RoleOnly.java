package org.acme.authenticationService.dao;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoleOnly extends Role {

    public RoleOnly() {
    }

    public RoleOnly(String id, String code, String appCode, String name, String description) {
        super(id, code, appCode, name, description);
    }

    public static RoleOnly fromDTO(org.acme.authenticationService.data.entity.Role dto) {
        RoleOnly roleOnly = new RoleOnly(dto.getId(), dto.getCode(), dto.getAppCode(), dto.getName(), dto.getDescription());
        roleOnly.setCreatedAt(dto.getCreatedAt());
        roleOnly.setCreatedBy(dto.getCreatedBy());
        roleOnly.setUpdatedAt(dto.getUpdatedAt());
        roleOnly.setUpdatedBy(dto.getUpdatedBy());
        roleOnly.setDeletedAt(dto.getDeletedAt());
        roleOnly.setDeletedBy(dto.getDeletedBy());
        return roleOnly;
    }

    @Override
    public org.acme.authenticationService.data.entity.Role toDto() {
        return new org.acme.authenticationService.data.entity.Role(super.getId(), super.getAppCode(), super.getCode(), super.getName(), super.getDescription());
    }
}
