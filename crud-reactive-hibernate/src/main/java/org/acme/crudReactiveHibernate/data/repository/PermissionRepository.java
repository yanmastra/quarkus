package org.acme.crudReactiveHibernate.data.repository;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.quarkus.logging.Log;
import io.quarkus.panache.common.Parameters;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Singleton;
import org.acme.crudReactiveHibernate.data.entity.Permission;
import org.acme.crudReactiveHibernate.data.entity.RolePermission;

@Singleton
public class PermissionRepository implements PanacheRepositoryBase<Permission, String> {

    public Uni<Permission> save(Permission permission) {
        return find("where appCode=?1 and code=?2", permission.getAppCode(), permission.getCode())
                .firstResult().onItem().transform(permission1 -> {
            Log.info("result: "+permission1);
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
            Log.info("persisting permission:"+item1);
            return item1;
        }));
    }

    @Override
    public Uni<Boolean> deleteById(String s) {
        Log.info("deleting P:"+s);
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
}
