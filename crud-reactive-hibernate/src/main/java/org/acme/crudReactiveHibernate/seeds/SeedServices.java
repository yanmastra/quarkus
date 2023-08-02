package org.acme.crudReactiveHibernate.seeds;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.acme.crudReactiveHibernate.data.entity.*;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ApplicationScoped
public class SeedServices {

    @Inject
    Logger logger;

    @Inject
    Mutiny.SessionFactory sf;

    @ConfigProperty(name = "crud-reactive-hibernate.clear-seed", defaultValue = "false")
    boolean clearSeed;

    void onStart(@Observes StartupEvent event) throws InterruptedException {
        logger.info("##############          SEEDING         #################");
        sf.withTransaction(session -> {
                    List<PanacheEntityBase> entities = new ArrayList<>();
                    Role role = createRole();
                    List<Permission> permissions = createPermission();
                    User user = createUser();

                    Uni<Role> result = session.find(Role.class, new RoleId("SYSTEM", "SYSTEM_ROOT"));
                    result = result.onItem().invoke(resultX -> {
                        if (resultX == null) {
                            role.setCreatedBy("SYSTEM");
                            entities.add(role);
                        } else {
                            role.setId(resultX.getId());
                        }
                    });
                    for (Permission p: permissions) {
                        result = result.onItem().call(r -> Permission.find("appCode=?1 and code=?2", p.getAppCode(), p.getCode()).firstResult()
                                .onItem().invoke(permission -> {
                                    if (permission == null) {
                                        p.setCreatedBy("SYSTEM");
                                        entities.add(p);
                                        entities.add(new RolePermission(role, p));
                                    } else {
                                        p.setId(((Permission) permission).getId());
                                    }
                                }));
                    }

                    result = result.call(r -> User.find("username=?1", user.getUsername()).firstResult()
                            .onItem().invoke(rx -> {
                                if (rx == null) {
                                    user.setCreatedBy("SYSTEM");
                                    entities.add(user);
                                    entities.add(new UserRole(user, role));
                                } else {
                                    user.setId(((User) rx).getId());
                                }
                            }));
                    return result.call(r -> session.persistAll(entities.toArray()));
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
                new Permission("SYSTEM", "CHANGE_ROLE_PERMISSION", "Change Role Permission"),
                new Permission("SYSTEM", "CREATE_PERMISSION", "Create any Permission"),
                new Permission("SYSTEM", "UPDATE_PERMISSION", "Update any Permission"),
                new Permission("SYSTEM", "DELETE_PERMISSION", "Delete any Permission"),
                new Permission("SYSTEM", "CREATE_APP_ADMIN", "Create an Administrator for an Application")
        ));
    }

    private Role createRole() {
        return new Role(new RoleId("SYSTEM", "SYSTEM_ROOT"), "System Root", "Role for Root User");
    }

    private User createUser() {
        return new User(null, "ROOT_SYSTEM", "system@root.io", "Root System");
    }

}
