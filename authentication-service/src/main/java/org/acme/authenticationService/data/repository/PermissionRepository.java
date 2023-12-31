package org.acme.authenticationService.data.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.authenticationService.data.entity.Permission;
import org.acme.authenticationService.data.entity.RolePermission;
import org.jboss.logging.Logger;

import java.util.List;

@ApplicationScoped
public class PermissionRepository implements PanacheRepositoryBase<Permission, String> {

    @Inject
    Logger logger;

    public Uni<Permission> save(Permission permission) {
        return find("where appCode=?1 and code=?2", permission.getAppCode(), permission.getCode())
                .firstResult().onItem().transform(permission1 -> {
            logger.info("result: "+permission1);
            if (permission1 != null && permission1.getDeletedAt() != null) {
                permission1.setDeletedAt(null);
                permission1.setDeletedBy(null);
                permission1.setName(permission.getName());
                return permission1;
            } else {
                permission.setId(null);
                permission.setDeletedBy(null);
                permission.setDeletedAt(null);
                return permission;
            }
        }).call(item -> persist(item).map(item1 -> {
            logger.info("persisting permission:"+item1);
            return item1;
        }));
    }

    @Override
    public Uni<Boolean> deleteById(String s) {
        logger.info("deleting P:"+s);
        return RolePermission.delete("where id.permission.id=?1", s).chain(result -> Permission.find("id=?1",s)
                .filter("deletedPermissionFilter", Parameters.with("isDeleted", false)).firstResult()
                .chain(r -> {
            if (r != null) return r.delete().map(d -> true);
            else throw new RuntimeException("Permission: "+s+" not found");
        }));
    }

    @Override
    public Uni<Permission> findById(String s) {
        return Permission.find("id=?1", s)
                .filter("deletedPermissionFilter", Parameters.with("isDeleted", false))
                .firstResult();
    }

    public Uni<List<Permission>> findByAppCode(String appCode) {
        return Permission.find("appCode=?1", Sort.descending("createdAt"), appCode)
                .filter("deletedPermissionFilter", Parameters.with("isDeleted", false))
                .list();
    }

    public Uni<Permission> findByAppAndCode(String appCode, String code) {
        return Permission.find("appCode=?1 and code=?2", appCode, code)
                .filter("deletedPermissionFilter", Parameters.with("isDeleted", false))
                .firstResult();
    }
}
