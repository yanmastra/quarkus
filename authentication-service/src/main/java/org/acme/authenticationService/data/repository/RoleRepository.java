package org.acme.authenticationService.data.repository;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.authenticationService.data.entity.Permission;
import org.acme.authenticationService.data.entity.Role;
import org.acme.authenticationService.data.entity.RolePermission;
import org.acme.authenticationService.data.entity.RolePermissionId;

import java.util.Date;
import java.util.List;

@ApplicationScoped
public class RoleRepository implements PanacheRepositoryBase<Role, String> {

    public Uni<List<Role>> findByApp(String appCode) {
        return findAll(Sort.descending("createdAt")).filter("deletedRoleFilter", Parameters.with("isDeleted", false))
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

    public Uni<Role> findById(String appCode, String code) {
        return find("appCode=?1 and code=?2", appCode, code).firstResult();
    }
}
