package org.acme.crudReactiveHibernate.data;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Singleton;
import org.acme.crudReactiveHibernate.data.entity.Permission;

@Singleton
public class PermissionRepository implements PanacheRepositoryBase<Permission, String> {

    public Uni<Permission> save(Permission permission) {
        Uni<Permission> existing = find("where code=?1", permission.getCode()).firstResult();
        return existing.call(permission1 -> {
            if (permission1 != null && permission1.getDeletedAt() != null) {
                permission1.setDeletedAt(null);
                permission1.setName(permission.getName());
                return persist(permission1);
            } else {
                return persist(permission);
            }
        });
    }
}
