package org.acme.crudReactiveHibernate.services;

import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.panache.common.Parameters;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.PersistenceException;
import org.acme.crudReactiveHibernate.dao.Permission;
import org.acme.crudReactiveHibernate.data.PermissionRepository;

import java.util.List;

@ApplicationScoped
public class PermissionService {

    @Inject
    PermissionRepository repository;

    @WithTransaction
    public Uni<List<Permission>> findAll() {
        return repository.findAll().filter("publicPermission", Parameters.with("isPublic", "true"))
                .list().onItem().transform(permissions -> permissions.stream().map(Permission::fromDTO).toList());
    }

    @WithTransaction
    public Uni<Permission> create(Permission permission) {
        return repository.persist(permission.toDTO()).map(permission1 -> {
            if (permission1 != null) return Permission.fromDTO(permission1);
            else throw new PersistenceException("Failed to persisting data");
        });
    }
}
