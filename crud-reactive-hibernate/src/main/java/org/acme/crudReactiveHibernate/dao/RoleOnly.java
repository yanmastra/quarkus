package org.acme.crudReactiveHibernate.dao;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.acme.crudReactiveHibernate.data.entity.RoleId;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoleOnly extends Role {

    public RoleOnly() {
    }

    public RoleOnly(String code, String appCode, String name, String description) {
        super(code, appCode, name, description);
    }

    public static RoleOnly fromDTO(org.acme.crudReactiveHibernate.data.entity.Role dto) {
        RoleOnly roleOnly = new RoleOnly(dto.getId().getCode(), dto.getId().getAppCode(), dto.getName(), dto.getDescription());
        roleOnly.setCreatedAt(dto.getCreatedAt());
        roleOnly.setCreatedBy(dto.getCreatedBy());
        roleOnly.setUpdatedAt(dto.getUpdatedAt());
        roleOnly.setUpdatedBy(dto.getUpdatedBy());
        roleOnly.setDeletedAt(dto.getDeletedAt());
        roleOnly.setDeletedBy(dto.getDeletedBy());
        return roleOnly;
    }

    @Override
    public org.acme.crudReactiveHibernate.data.entity.Role toDto() {
        return new org.acme.crudReactiveHibernate.data.entity.Role(new RoleId(super.getAppCode(), super.getCode()), super.getName(), super.getDescription());
    }
}
