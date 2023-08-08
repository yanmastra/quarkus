package org.acme.authenticationService.services;

import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.PersistenceException;
import jakarta.ws.rs.NotFoundException;
import org.acme.authenticationService.dao.RoleOnly;
import org.acme.authenticationService.dao.RoleWithPermission;
import org.acme.authenticationService.data.repository.RoleRepository;
import org.acme.authenticationService.data.entity.RoleId;
import org.jboss.logging.Logger;

import java.util.List;

@ApplicationScoped
public class RoleService {
    @Inject
    RoleRepository repository;

    @Inject
    Logger logger;

    @WithTransaction
    public Uni<List<RoleOnly>> findByApp(String appCode) {
        return repository.findByApp(appCode).onItem().transform(permissions -> permissions.stream().map(RoleOnly::fromDTO).toList());
    }

    @WithTransaction
    public Uni<RoleOnly> create(RoleOnly role) {
        return repository.persist(role.toDto()).map(role1 -> {
            if (role1 != null) return RoleOnly.fromDTO(role1);
            else throw new PersistenceException("Failed to persisting data");
        });
    }

    @WithTransaction
    public Uni<RoleWithPermission> findOne(String appCode, String code) {
        return repository.findById(appCode, code).map(RoleWithPermission::fromDTO);
    }

    @WithTransaction
    public Uni<RoleOnly> update(String appCode, String code, RoleOnly data) {
        return repository.update("updatedAt=CURRENT_TIMESTAMP(), name=?1, description=?2 where id=?3", data.getName(), data.getDescription(), new RoleId(appCode, code))
                .onItem().transformToUni(integer -> {
                    logger.info("update result: " + integer);
                    if (integer == 1) return repository.findById(appCode, code);
                    else return Uni.createFrom().nullItem();
                }).onItem().transform(role -> {
                    if (role != null) return RoleOnly.fromDTO(role);
                    else throw new NotFoundException("Role not found");
                });

//        return repository.findById(new RoleId(appCode, code))
//                .call(role -> RolePermission.delete("role=?1", role).onItem().transform(aLong -> role)).call(role -> {
//                    List<Uni<Void>> updates = new ArrayList<>();
//                    for (Permission p : data.getPermissions()) {
//                        updates.add(RolePermission.persist(new RolePermission(role, p.toDTO())));
//                    }
//                    return Uni.join().all(updates).usingConcurrencyOf(3000).andFailFast();
//                })
//                .onItem().call(role -> repository.findById(role.getId())).onItem().transform(RoleWithPermission::fromDTO);
    }
}
