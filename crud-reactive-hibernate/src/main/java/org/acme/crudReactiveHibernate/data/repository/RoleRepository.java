package org.acme.crudReactiveHibernate.data.repository;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Parameters;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Singleton;
import org.acme.crudReactiveHibernate.data.entity.*;

import java.util.Date;
import java.util.List;

@Singleton
public class RoleRepository implements PanacheRepositoryBase<Role, RoleId> {

    public Uni<List<Role>> findByApp(String appCode) {
        return findAll().filter("deletedRoleFilter", Parameters.with("isDeleted", false))
                .filter("myApplicationRoles", Parameters.with("appCode", appCode))
                .list();
    }

    public Uni<PanacheEntityBase> addAnPermission(Role role, Permission permission) {
        return RolePermission.findById(new RolePermissionId(role, permission)).call(result -> {
            if (result instanceof RolePermission rp) {
                rp.getRole().setUpdatedAt(new Date());
                return persist(rp.getRole());
            } else {
                RolePermission rp = new RolePermission(role, permission);
                return rp.persist();
            }
        }).onItem().call(i -> Role.findById(role.getId()));
    }
}