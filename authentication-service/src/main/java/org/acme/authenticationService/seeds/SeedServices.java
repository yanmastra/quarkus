package org.acme.authenticationService.seeds;

import com.acme.authorization.utils.JsonUtils;
import com.acme.authorization.utils.PasswordGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.acme.authenticationService.data.entity.*;
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

    @ConfigProperty(name = "auth-service.clear-seed", defaultValue = "false")
    boolean clearSeed;

    @Inject
    ObjectMapper objectMapper;

//    private FirebaseAuthClient authClient;

    void onStart(@Observes StartupEvent event) throws InterruptedException {
        logger.info("##############          SEEDING         #################");
        JsonUtils.setObjectMapper(objectMapper);

        sf.withTransaction(session -> {
                    Application application = createSystemApp();
                    List<PanacheEntityBase> entities = new ArrayList<>();
                    Role role = createRole();
                    List<Permission> permissions = createPermission();
                    AuthUser authUser = createUser();

                    Uni<?> result = session.createQuery("from Application A where A.code=:code", Application.class)
                            .setParameter("code", application.getCode())
                            .getSingleResult();


                    result = result.onItem().invoke(resultX -> {
                        if (resultX == null) {
                            entities.add(application);
                        }
                    });
                    result = result.call(r -> AuthUser.find("username=?1", authUser.getUsername()).firstResult()
                            .onItem().invoke(rx -> {
                                if (rx == null) {
                                    authUser.setCreatedBy("SYSTEM");
                                    String pass = PasswordGenerator.generatePassword(32, true);
                                    authUser.setPasswordTextPlain(pass);
                                    logger.info("ROOT PASSWORD:"+pass);
                                    authUser.setVerified(true);
                                    entities.add(authUser);

                                    authUser.generateUUID();
                                    UserApp userApp = new UserApp(authUser, application);
                                    entities.add(userApp);
                                } else {
                                    authUser.setId(((AuthUser) rx).getId());
                                }
                            }));

                    result = result.chain(r -> Role.find("code=?1 and appCode=?2", "SYSTEM_ROOT", "SYSTEM")
                            .firstResult()
                            .onItem().invoke(resultX -> {
                                if (resultX == null) {
                                    role.setCreatedBy("SYSTEM");
                                    entities.add(role);
                                    entities.add(new UserRole(authUser, role));
                                }else if (resultX instanceof Role role1){
                                    role.setId(role1.getId());
                                } else {
                                    role.setCode("SYSTEM_ROOT");
                                    role.setAppCode("SYSTEM");
                                }
                            }));


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

                    return result.call(r -> session.persistAll(entities.toArray()));
                })
                .subscribe().with(r -> logger.info("SEEDING COMPLETE"));


        List<Permission> toBePersisted = new ArrayList<>();
        sf.withTransaction(session -> session.createQuery("select A from Application A where code != 'SYSTEM' and deletedAt is null", Application.class).getResultList()
                .chain(result -> {
                    Uni<?> uniChecker = Uni.createFrom().nullItem();

                    for (Application app: result) {
                        for (Permission p: createPermission()) {
                            uniChecker = uniChecker.call(r -> Permission.find("appCode=?1 and code=?2", app.getCode(), p.getCode()).firstResult()
                                    .onItem().invoke(existingP -> {
                                        if (existingP == null) {
                                            logger.info("create permission:"+p.getCode()+" for app:"+app.getCode());
                                            p.setId(null);
                                            p.setAppCode(app.getCode());
                                            toBePersisted.add(p);
                                        } else {
                                            p.setId(((Permission) existingP).getId());
                                        }
                                    })
                            );
                        }
                    }
                    return uniChecker;
                }).chain(r -> session.persistAll(toBePersisted.toArray()).onFailure().invoke(throwable -> {
                    logger.error(throwable.getMessage(), throwable);
                })))
                .subscribe().with(r -> logger.info("SEEDING 2 COMPLETE"));

        List<UserApp> userAppToBePersisted = new ArrayList<>();
        sf.withTransaction(session -> session.createQuery("select U from AuthUser U", AuthUser.class)
                .getResultList()
                .chain(result -> {
                    Uni<?> uniChecker = Uni.createFrom().nullItem();

                    for (AuthUser ua: result) {
                        uniChecker = uniChecker.chain(r -> session.createQuery("select UR from UserRole UR where UR.authUser.id = :userId", UserRole.class)
                                .setParameter("userId", ua.getId())
                                .getResultList())
                                .chain(xUserRoles -> {
                                    Uni<?> uniRoleChecker = Uni.createFrom().nullItem();
                                    for (UserRole ur: xUserRoles) {
                                        uniRoleChecker = uniRoleChecker.chain(r -> session.createQuery("select UA from UserApp UA where UA.userId= :userId and UA.appCode= :appCode", UserApp.class)
                                                .setParameter("userId", ua.getId())
                                                .setParameter("appCode", ur.getRole().getAppCode())
                                                .getResultList())
                                                .map(list -> {
                                                    if (list.isEmpty()) {
                                                        userAppToBePersisted.add(new UserApp(ua.getId(), ur.getRole().getAppCode()));
                                                    }
                                                    return false;
                                                });
                                    }
                                    return uniRoleChecker;
                                });
                    }
                    return uniChecker;
                })
                .chain(r -> session.persistAll(userAppToBePersisted.toArray()).onFailure().invoke(throwable -> logger.error(throwable.getMessage(), throwable))))
                .subscribe().with(r -> logger.info("SEEDING 3 COMPLETE"));

//        FirebaseAuthRequest request = new FirebaseAuthRequest();
//        request.setEmail("yanmastra59@gmail.com");
//        request.setPassword("Berantas");
//        Uni.createFrom().nullItem()
//                .chain(r -> authClient.signUp(request))
//                .onFailure().invoke(throwable -> {
//                    logger.error(throwable.getMessage(), throwable);
//                    if (throwable.getCause() != null && throwable.getCause() instanceof WebApplicationException eWeb) {
//                        logger.error("Error response:"+eWeb.getResponse().getEntity());
//                    }
//                })
//                .subscribe().with(r -> logger.info("SEEDING 4 COMPLETE"));
//        Uni.createFrom().nullItem()
//                .chain(r -> authClient.signIn(request))
//                .map(r -> {
//                    logger.info("response:"+JsonUtils.toJson(r));
//                    return r;
//                })
//                .onFailure().invoke(throwable -> {
//                    logger.error(throwable.getMessage(), throwable);
//                    if (throwable.getCause() != null && throwable.getCause() instanceof WebApplicationException eWeb) {
//                        logger.error("Error response:"+eWeb.getResponse().getEntity());
//                    }
//                })
//                .subscribe().with(r -> logger.info("SEEDING 5 COMPLETE"));
    }

    public List<Permission> createPermission() {
        return new ArrayList<>(Arrays.asList(
                new Permission("SYSTEM", "CREATE_SYS_USER", "Create System AuthUser"),
                new Permission("SYSTEM", "UPDATE_SYS_USER", "Update System AuthUser"),
                new Permission("SYSTEM", "ASSIGN_TO_SYSTEM", "Assign AuthUser role to system"),
                new Permission("SYSTEM", "CHANGE_SYSTEM_ROLE", "Change any system role"),
                new Permission("SYSTEM", "CREATE_USER", "Create any User"),
                new Permission("SYSTEM", "UPDATE_USER", "Update any User"),
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
                new Permission("SYSTEM", "DELETE_APP", "Delete any Application"),
                new Permission("SYSTEM", "CREATE_APP_ADMIN", "Create an Administrator for an Application"),
                new Permission("SYSTEM", "VIEW_ALL_USER", "View All Users")
        ));
    }

    private Role createRole() {
        return new Role(null, "SYSTEM", "SYSTEM_ROOT", "System Root", "Role for Root AuthUser");
    }

    private AuthUser createUser() {
        return new AuthUser(null, "ROOT_SYSTEM", "system@root.io", "Root System");
    }

    private Application createSystemApp(){
        Application application = new Application("SYSTEM", "System Administration");
        application.setCreatedBy("SYSTEM_ROOT");
        return application;
    }

}
