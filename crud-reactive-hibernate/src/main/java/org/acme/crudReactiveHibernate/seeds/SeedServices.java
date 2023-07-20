package org.acme.crudReactiveHibernate.seeds;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.acme.crudReactiveHibernate.data.PermissionRepository;
import org.acme.crudReactiveHibernate.data.RoleRepository;
import org.acme.crudReactiveHibernate.data.UserRepository;
import org.acme.crudReactiveHibernate.data.entity.*;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@ApplicationScoped
public class SeedServices {

    @Inject
    Logger logger;

    @Inject
    PermissionRepository permissionRepository;

    @Inject
    RoleRepository roleRepository;


    @Inject
    UserRepository userRepository;

    @Inject
    Mutiny.SessionFactory sf;

    void onStart(@Observes StartupEvent event) throws InterruptedException {
        logger.info("##############          SEEDING         #################");
        sf.withTransaction(session -> session.find(Role.class, new RoleId("SYSTEM", "SYSTEM_ROOT")))
                .map(result -> {
                    logger.info("existing role: " + result);
                    return !Objects.isNull(result);
                })
                .call(result -> {
                    if (result) {
                        return sf.withTransaction(session -> RolePermission.delete("id.role.id.appCode", "SYSTEM")
                                        .call(r -> Role.delete("id.appCode", "SYSTEM"))
                                        .call(r -> Permission.delete("appCode", "SYSTEM"))
                                        .call(r -> User.delete("username", "ROOT_SYSTEM")))
                                .onItem().invoke(r -> {
                                    logger.info("Cleaning success");
                                });
                    } else
                        return Uni.createFrom().nullItem();
                })
                .call(r -> {
                    Role role = createRole();
                    List<PanacheEntityBase> entities = new ArrayList<>();
                    entities.add(role);

                    List<Permission> permissions = createPermission();
                    entities.addAll(permissions);

                    for (Permission p : permissions) {
                        entities.add(new RolePermission(role, p));
                    }

                    User user = createUser();
                    entities.add(user);
                    entities.add(new UserRole(user, role));

                    return sf.withTransaction(session -> session.persistAll(entities.toArray()));
                })
                .subscribe().with(r -> logger.info("SEEDING COMPLETE"));
    }

    private List<Permission> createPermission() {
        return new ArrayList<>(Arrays.asList(
                new Permission("SYSTEM", "CREATE_SYS_USER", "Create System User"),
                new Permission("SYSTEM", "UPDATE_SYS_USER", "Update System User"),
                new Permission("SYSTEM", "ASSIGN_TO_SYSTEM", "Assign User role to system"),
                new Permission("SYSTEM", "CHANGE_SYSTEM_ROLE", "Change any system role"),
                new Permission("SYSTEM", "CREATE_USER", "Create any User"),
                new Permission("SYSTEM", "CREATE_APP", "Create any Application"),
                new Permission("SYSTEM", "CREATE_ROLE", "Create any Role"),
                new Permission("SYSTEM", "UPDATE_ROLE", "Update Role"),
                new Permission("SYSTEM", "VIEW_ALL", "View All"),
                new Permission("SYSTEM", "VIEW_USER", "View USER"),
                new Permission("SYSTEM", "VIEW_ROLE", "View Role"),
                new Permission("SYSTEM", "VIEW_PERMISSION", "View Permission"),
                new Permission("SYSTEM", "CHANGE_ROLE_PERMISSION", "Change Role Permission")
        ));
    }

    private Role createRole() {
        return new Role(new RoleId("SYSTEM", "SYSTEM_ROOT"), "System Root", "Role for Root User");
    }

    private User createUser() {
        return new User(null, "ROOT_SYSTEM", "system@root.io", "Root System");
    }

}
