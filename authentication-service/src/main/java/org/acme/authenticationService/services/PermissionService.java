package org.acme.authenticationService.services;

import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.panache.common.Parameters;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import org.acme.authenticationService.dao.Permission;
import org.acme.authenticationService.data.repository.PermissionRepository;
import org.jboss.logging.Logger;

import java.util.List;

@ApplicationScoped
public class PermissionService {

    @Inject
    PermissionRepository repository;
    @Inject
    Logger logger;

    @WithTransaction
    public Uni<List<Permission>> findAll(String appCode) {
        return repository.findAll().filter("myApplicationFilter", Parameters.with("appCode", appCode))
                .list().onItem().transform(permissions -> permissions.stream().map(Permission::fromDTO).toList());
    }

    @WithTransaction
    public Uni<Permission> create(Permission permission) {
        return repository.save(permission.toDTO()).onItem().transform(Permission::fromDTO);
    }

    @WithTransaction
    public Uni<Permission> findOne(String id) {
        logger.info("getting permission:"+id);
        return repository.findById(id).chain(item -> {
            if (item != null) return Uni.createFrom().item(Permission.fromDTO(item));
            else throw new NotFoundException("Permission with id:%s not found".formatted(id));
        });
    }

    @WithTransaction
    public Uni<Permission> update(String id, Permission permission) {
        return repository.findById(id).call(item -> {
            if (item != null) {
                item.setName(permission.getName());
                return repository.persist(item);
            } else throw new RuntimeException("Permission:"+permission.getId()+" not found");
        }).onItem().transform(Permission::fromDTO);
    }

    @WithTransaction
    public Uni<Boolean> delete(String id) {
        return repository.deleteById(id);
    }
}
