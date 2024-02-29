package org.acme.authenticationService.services;

import com.acme.authorization.json.ResponseJson;
import com.acme.authorization.security.UserPrincipal;
import com.acme.authorization.utils.JsonUtils;
import com.acme.authorization.utils.PasswordGenerator;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.quarkus.hibernate.reactive.panache.PanacheQuery;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import io.quarkus.runtime.util.StringUtil;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.handler.HttpException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.container.ContainerRequestContext;
import org.acme.authenticationService.dao.ApplicationJson;
import org.acme.authenticationService.dao.UpdatePasswordRequest;
import org.acme.authenticationService.dao.UserOnly;
import org.acme.authenticationService.dao.UserWithPermission;
import org.acme.authenticationService.data.entity.*;
import org.acme.authenticationService.data.repository.*;
import org.acme.authenticationService.firebase.FirebaseAuthClient;
import org.acme.authenticationService.firebase.FirebaseAuthRequest;
import org.acme.authenticationService.firebase.FirebaseAuthResponse;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.ClientWebApplicationException;

import java.util.*;

@ApplicationScoped
public class UserService {

    @Inject
    UserRepository userRepository;
    @Inject
    UserRoleRepository userRoleRepository;
    @Inject
    ApplicationRepository appRepo;
    @Inject
    RoleRepository roleRepo;
    @Inject MailService mailService;
    @Inject
    UserAppRepository userAppRepository;
    @Inject
    AdditionalUserDataRepository additionalUserDataRepo;
    @RestClient
    FirebaseAuthClient firebaseAuthClient;

    @Inject
    Logger logger;

    /**
     * to create user from API endpoint
     * @param user
     * @param context
     * @return
     */
    @WithTransaction
    public Uni<ResponseJson<UserOnly>> saveUser(UserOnly user, ContainerRequestContext context) {
        UserPrincipal principal = UserPrincipal.valueOf(context);

        user.setCreatedBy(principal.getName());
        AuthUser authUser = user.toDto();
        authUser.setCreatedBy(principal.getName());
        String password = PasswordGenerator.generatePassword(8, false);
        authUser.setPasswordTextPlain(password);

        Optional<Map.Entry<String, List<String>>> first = user.getRolesIds().entrySet().stream().findFirst();
        if (first.isEmpty() || first.get().getValue().isEmpty()) throw new IllegalArgumentException("Please specify Role and Application Code!");
        String appCode = first.get().getKey();
        String roleCode = first.get().getValue().get(0);
        return appRepo.findById(appCode)
                .chain(app -> {
                    if (app.getUsingFirebase()) {
                        return firebaseSignUp(authUser.getEmail(), password, app.getFirebaseApiKey())
                                .onFailure().invoke(throwable -> logger.error(throwable.getMessage(), throwable))
                                .chain(firebaseResponse -> {
                                    app.addAdditionalUserDataField(FirebaseAuthClient.FIREBASE_VERIFIED);
                                    app.addAdditionalUserDataField(FirebaseAuthClient.FIREBASE_LOCAL_ID);

                                    AdditionalUserData firebaseVerified = new AdditionalUserData(null, authUser, appCode, FirebaseAuthClient.FIREBASE_VERIFIED, "UNVERIFIED");
                                    AdditionalUserData firebaseLocalId = new AdditionalUserData(null, authUser, appCode, FirebaseAuthClient.FIREBASE_LOCAL_ID, firebaseResponse.getLocalId());

                                    authUser.getAdditionalUserData().add(firebaseVerified);
                                    authUser.getAdditionalUserData().add(firebaseLocalId);
                                    return saveUserResponse(app, authUser, roleCode);
                                });
                    } else {
                        return saveUserResponse(app, authUser, roleCode);
                    }
                });
    }

    private Uni<FirebaseAuthResponse> firebaseSignUp(String email, String password, String apiKey) {
        return firebaseAuthClient.signUp(new FirebaseAuthRequest(email, password), apiKey)
                .onFailure().invoke(throwable -> logger.error(throwable.getMessage(), throwable));
    }

    private Uni<ResponseJson<UserOnly>> saveUserResponse(Application app, AuthUser authUser, String roleCode) {
        return roleRepo.findById(app.getCode(), roleCode).chain(role -> saveUser(authUser, app, role)
                .onItem().transform(UserOnly::fromDto)
                .map(saved -> new ResponseJson<>(
                        true,
                        "",
                        saved
                )));
    }

    @WithTransaction
    public Uni<UserOnly> updateUser(String id, UserOnly user) {
        return userRepository.update("name = ?1 where id = ?2", user.getName(), id)
                .onItem().transformToUni(integer -> {
                    logger.info("updated result: " + integer + ", id:" + id);
                    return userRepository.find("id = ?1", id).firstResult().onItem().transform(UserOnly::fromDto);
                })
                .onFailure().invoke(throwable -> logger.error(throwable.getMessage(), throwable));

    }

    public Uni<ResponseJson<List<UserOnly>>> findAllUser(Integer page, Integer size, String search, UserPrincipal principal) {
        PanacheQuery<AuthUser> usersPanacheQuery;
        ResponseJson<List<UserOnly>> responseJson = new ResponseJson<>(true, null);
        responseJson.setPage(page);
        responseJson.setSize(size);
        responseJson.setSearch(search);

        if (principal.getAppCode().equals("SYSTEM")) {

            if (!StringUtil.isNullOrEmpty(search)) {
                usersPanacheQuery = userRepository.find("where (name like ?1 or username like ?1 or email like ?1)", Sort.ascending("name"), "%" + search.toLowerCase() + "%");
            } else
                usersPanacheQuery = userRepository.findAll(Sort.ascending("name"));
            usersPanacheQuery = usersPanacheQuery.filter("deletedAppFilter", Parameters.with("isDeleted", false));
            final PanacheQuery<AuthUser> finalUsersPanacheQuery = usersPanacheQuery;
            return finalUsersPanacheQuery.page(Page.of(page -1, size)).list()
                    .map(result -> {
                        responseJson.setData(result.stream().map(UserOnly::fromDto).toList());
                        return responseJson;
                    })
                    .chain(response -> finalUsersPanacheQuery.count())
                    .map(count -> {
                        responseJson.setTotalData(count.intValue());
                        return responseJson;
                    });
        } else {
            String appCode = principal.getAppCode();
            return userAppRepository.find("where appCode = :appCode", Map.of("appCode", appCode)).list()
                    .chain(userApps -> {
                        List<String> userIds = userApps.stream().map(UserApp::getUserId).toList();
                        Map<String, Object> params = new HashMap<>(Map.of("ids", userIds));

                        PanacheQuery<AuthUser> usersPanacheQuery2;
                        if (StringUtils.isNotBlank(search)) {
                            params.put("search", search);

                            usersPanacheQuery2 = userRepository.find("where id in :ids and (name like :search or username like :search or email like :search)",
                                    Sort.descending("createdAt"),
                                    params
                            );
                        } else {
                            usersPanacheQuery2 = userRepository.find("where id in :ids",
                                    Sort.descending("createdAt"),
                                    params);
                        }
                        usersPanacheQuery2 = usersPanacheQuery2.filter("deletedAppFilter", Parameters.with("isDeleted", false));

                        final PanacheQuery<AuthUser> finalUsersPanacheQuery = usersPanacheQuery2;
                        return usersPanacheQuery2.page(Page.of(page-1, size)).list()
                                .map(result -> {
                                    responseJson.setData(result.stream().map(user -> UserOnly.fromDto(user, appCode)).toList());
                                    return responseJson;
                                })
                                .chain(r -> finalUsersPanacheQuery.count())
                                .map(count -> {
                                    responseJson.setTotalData(count.intValue());
                                    return responseJson;
                                });
                    });
        }
    }

    @WithTransaction
    public Uni<ResponseJson<List<UserOnly>>> findAll(Integer page, Integer size, String search, UserPrincipal principal) {
        return findAllUser(page, size, search, principal)
                .chain(data -> {
                    List<String> userIds = data.getData().stream().map(UserOnly::getId).toList();
                    return userRoleRepository.find("where authUser.id in (:ids) and authUser.deletedAt is null", Map.of("ids", userIds))
                            .list()
                            .map(result -> {
                                Map<String, Map<String, List<String>>> roleMaps = new HashMap<>();
                                result.forEach(ur -> {
                                    Map<String, List<String>> roleIds = roleMaps.computeIfAbsent(ur.getUser().getId(), key -> new HashMap<>());
                                    roleIds.computeIfAbsent(ur.getRole().getAppCode(), key -> new ArrayList<>())
                                            .add(ur.getRole().getCode());
                                });
                                return roleMaps;
                            })
                            .map(roleMaps -> {
                                data.getData().forEach(item -> item.setRolesIds(roleMaps.computeIfAbsent(item.getId(), key -> new HashMap<>())));
                                return data;
                            });
                });
    }

    @WithTransaction
    public Uni<UserWithPermission> findDetail(String id) {
        return userRepository.find("id = ?1", id).firstResult().chain(user -> {
            if (user != null) return userRoleRepository.find("authUser=?1", user).list().map(result -> {
                user.setRoles(new HashSet<>(result));
                return UserWithPermission.fromDto(user);
            });
            else throw new NotFoundException("AuthUser not found");
        });
    }

    /**
     * saving complete user data that included hashed password
     * @param authUser
     * @param app
     * @param role
     * @return
     */
    private Uni<AuthUser> saveUser(AuthUser authUser, Application app, Role role) {
        if (app == null) throw new HttpException(HttpResponseStatus.NOT_FOUND.code(), "Application not found!");
        if (role == null) throw new HttpException(HttpResponseStatus.NOT_FOUND.code(), "Role not found!");

        return userRepository.find("where username=:username or email=:email", Map.of(
                "username", authUser.getUsername(),
                "email", authUser.getEmail()
                ))
                .firstResult()
                .map(existed -> {
                    if (existed != null) throw new HttpException(HttpResponseStatus.CONFLICT.code(), "User with same email or username already exists!");
                    return authUser;
                })
                .chain(roBePersisted -> userRepository.persist(roBePersisted).onFailure().invoke(throwable -> {
                            logger.error("Persisting user:"+throwable.getMessage(), throwable);
                            throw new RuntimeException(throwable.getMessage(), throwable);
                }).chain(saved -> {
                    if (StringUtils.isBlank(app.getAdditionalUserDataFields())) return Uni.createFrom().item(saved);
                    ApplicationJson appJson = ApplicationJson.fromDto(app);

                    Uni<?> uniAdditions = Uni.createFrom().nullItem();
                    for(String field: appJson.getAdditionalUserDataFields()) {
                        AdditionalUserData addUserData = authUser.getAdditionalUserData().stream().filter(item -> item.getFieldCode().equals(field) && item.getAppCode().equals(appJson.getCode()))
                                .findFirst().orElse(null);
                        if (addUserData == null) {
                            addUserData = new AdditionalUserData(null, saved, app.getCode(), field, null);
                        }
                        final AdditionalUserData fAddUserData = addUserData;
                        uniAdditions = uniAdditions.chain(r -> additionalUserDataRepo.persist(fAddUserData).onFailure()
                                .invoke(throwable -> {
                                    logger.error("Persisting additional-data:"+throwable.getMessage(), throwable);
                                    throw new RuntimeException(throwable.getMessage(), throwable);
                                })
                                .map(saved.getAdditionalUserData()::add));
                    }
                    return uniAdditions.map(r -> saved);
                })
                .chain(saved -> {
                    logger.info("saved user:"+saved+", role:"+role);
                    return userRoleRepository.persist(new UserRole(saved, role))
                            .map(r -> {
                                saved.addRole(role);
                                return saved;
                            });
                })
                .chain(saved -> {
                    logger.info("saved userRole:"+saved.getRoles()+", app:"+app);
                    return userAppRepository.persist(new UserApp(saved, app))
                            .map(r -> saved);
                }));
    }

    /**
     * to create new user from Web Page
     * @param userOnly
     * @param appCode
     * @param roleCode
     * @param principal
     * @return
     */
    public Uni<?> createNewUser(UserOnly userOnly, String appCode, String roleCode, UserPrincipal principal) {
        return appRepo.findById(appCode)
                .chain(app -> roleRepo.findById(app.getCode(), roleCode).chain(role -> {

                    String password = PasswordGenerator.generatePassword(8, true);
                    AuthUser authUser = userOnly.toDto();
                    authUser.setPasswordTextPlain(password);
                    authUser.setCreatedBy(principal.getName());
                    authUser.setVerified(true);

                    Uni<Map<String, String>> uniSave = saveUser(authUser, app, role)
                            .map(r -> Map.of("appName", app.getName(), "roleName", role.getName(), "userId", r.getId()));

                    if (app.getUsingFirebase()) {
                        uniSave = uniSave.chain(r -> firebaseSignUp(userOnly.getEmail(), password, app.getFirebaseApiKey())
                                .map(s -> r));
                    }
                    uniSave = uniSave.chain(r -> {
                        if (StringUtils.isBlank(r.get("userId"))) throw new RuntimeException("Failed to save user!");

                        logger.info("sending email to:" + authUser.getEmail());
                        return mailService.createSignInfoEmail(
                                        "EN",
                                        authUser.getUsername(),
                                        password,
                                        authUser.getName(),
                                        authUser.getEmail(),
                                        r.get("appName"),
                                        r.get("roleName"))
                                .onFailure().invoke(throwable -> {
                                    logger.error("error sending email:" + throwable.getMessage(), throwable);
                                    throw new RuntimeException(throwable.getMessage(), throwable);
                                })
                                .map(x -> r);
                    });
                    return uniSave;
                }));
    }

    @WithTransaction
    public Uni<ResponseJson<UserOnly>> updatePassword(UpdatePasswordRequest request, UserPrincipal principal) {
        if (StringUtils.isBlank(request.getPassword()) ||
                StringUtils.isBlank(request.getConfirmPassword()) ||
                request.getPassword().length() < 6 ||
                !request.getPassword().equals(request.getConfirmPassword()))
            throw new IllegalArgumentException("Bad password!");

        logger.info("principal:"+principal);

        return appRepo.findById(principal.getAppCode())
                .chain(app -> userRepository.find("where id=?1", principal.getUser().getId())
                        .firstResult()
                        .onFailure().invoke(throwable -> {
                            logger.error("On finding:" + throwable.getMessage(), throwable);
                        })
                        .chain(result -> {
                            if (app.getUsingFirebase()) {
                                FirebaseAuthRequest updatePassRequest = new FirebaseAuthRequest();
                                updatePassRequest.setPassword(request.getPassword());
                                updatePassRequest.setIdToken(principal.getFirebaseToken());

                                logger.info("fb key:"+ app.getFirebaseApiKey());
                                return firebaseAuthClient.updateProfile(updatePassRequest, app.getFirebaseApiKey())
                                        .onFailure().invoke(throwable -> {
                                            if (throwable instanceof ClientWebApplicationException clientWebApplicationException) {
                                                logger.error("response:" + JsonUtils.toJson(clientWebApplicationException.getResponse()));
                                            }
                                        })
                                        .chain(res -> {
                                            result.setPasswordTextPlain(request.getPassword());
                                            result.setVerified(true);
                                            return userRepository.persist(result);
                                        });
                            } else {
                                result.setPasswordTextPlain(request.getPassword());
                                result.setVerified(true);
                                return userRepository.persist(result);
                            }
                        }))
                .chain(result -> userRoleRepository.find("where authUser.id=?1", result.getId())
                        .list()
                        .map(userRoles -> {
                            result.setRoles(new HashSet<>(userRoles));
                            return result;
                        })
                )
                .map(result -> new ResponseJson<>(true, null, UserOnly.fromDto(result)));
    }
}
