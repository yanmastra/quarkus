package org.acme.authenticationService.resources.web.v1;

import com.acme.authorization.json.User;
import com.acme.authorization.security.UserPrincipal;
import com.acme.authorization.utils.JsonUtils;
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
import org.acme.authenticationService.dao.ApplicationJson;
import org.acme.authenticationService.dao.Permission;
import org.acme.authenticationService.dao.RoleOnly;
import org.acme.authenticationService.dao.UserOnly;
import org.acme.authenticationService.dao.web.*;
import org.acme.authenticationService.data.entity.Application;
import org.acme.authenticationService.data.entity.Role;
import org.acme.authenticationService.data.entity.RolePermission;
import org.acme.authenticationService.data.repository.*;
import org.acme.authenticationService.resources.web.WebUtils;
import org.acme.authenticationService.services.MailService;
import org.acme.authenticationService.services.UserService;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class WebService {

    @Inject
    UserRepository userRepo;
    @Inject
    ApplicationRepository appRepo;
    @Inject
    RoleRepository roleRepo;
    @Inject
    UserRoleRepository userRoleRepo;

    @ConfigProperty(name = "application-name", defaultValue = "Example App")
    String appName;
    @Inject
    Logger logger;

    @Inject
    MailService mailService;
    @Inject
    PermissionRepository permissionRepo;
    @Inject
    RolePermissionRepository rolePermissionRepo;
    @Inject
    UserService userService;
    @Inject
    UserAppRepository userAppRepository;

    private <E extends BaseModel> Uni<E> createReturnResponse(Uni<E> uni, UserPrincipal principal) {
        return uni.chain(model -> appRepo.findById(principal.getAppCode()).map(app -> {
            model.appName = app.getName();
            return model;
        })).map(model -> {
            model.user = principal.getUser();
            return model;
        });
    }

    @WithTransaction
    public Uni<Home> getHomePageData(UserPrincipal principal) {
        Uni<Home> homeUni = Uni.createFrom().item(principal)
                .chain(ctx -> {
                    if (ctx.getAppCode().equals("SYSTEM")) {
                        return appRepo.count().map(count -> {
                            Home home = WebUtils.createModel(new Home(), appName);
                            home.applicationCount = count;
                            return home;
                        }).chain(home -> userRepo.count().map(uCount -> {
                            home.userCount = uCount;
                            return home;
                        })).chain(home -> roleRepo.count().map(count -> {
                            home.roleCount = count;
                            return home;
                        })).chain(home -> permissionRepo.count().map(count -> {
                            home.permissionCount = count;
                            return home;
                        }));
                    } else {
                        String appCode = principal.getAppCode();
                        return appRepo.find("where code=?1", appCode).firstResult().chain(
                                app -> appRepo.find("where parent=?1 and deletedAt is null", app).list().map(count -> {
                                    Home home = WebUtils.createModel(new Home(), app.getName());
                                    home.applicationCount = count.size()+1;
                                    home.childAppCodes = new ArrayList<>(count.stream().map(Application::getCode).toList());
                                    home.childAppCodes.add(app.getCode());
                                    return home;
                                }))
                                .chain(home -> userAppRepository.find("where appCode in (:appCodes)", Map.of(
                                                "appCodes", home.childAppCodes
                                        ))
                                        .count().map(uCount -> {
                                    home.userCount = uCount;
                                    return home;
                                })).chain(home -> roleRepo.find("where appCode in (:appCodes)", Map.of(
                                        "appCodes", home.childAppCodes
                                )).count().map(count -> {
                                    home.roleCount = count;
                                    return home;
                                })).chain(home -> permissionRepo.find("where appCode in (:appCodes)", Map.of(
                                        "appCodes", home.childAppCodes
                                )).count().map(count -> {
                                    home.permissionCount = count;
                                    return home;
                                }));
                    }
                });
        return createReturnResponse(homeUni, principal);
    }

    private PanacheQuery<Application> getApplicationQuery(UserPrincipal principal, String search) {
        PanacheQuery<Application> applicationPanacheQuery;

        if (principal.getAppCode().equals("SYSTEM")) {
            if (!StringUtil.isNullOrEmpty(search)) {
                applicationPanacheQuery = appRepo.find("where (name like ?1 or parent.name like ?1)", Sort.descending("createdAt"), "%"+search.toLowerCase()+"%");
            } else
                applicationPanacheQuery = appRepo.findAll(Sort.descending("createdAt"));
        } else {
            String appCode = principal.getAppCode();

            if (!StringUtil.isNullOrEmpty(search)) {
                applicationPanacheQuery = appRepo.find("where (code=?1 or parent.code=?1) and (name like ?2 or parent.name like ?2)", Sort.descending("createdAt"), appCode, "%"+search.toLowerCase()+"%");
            } else
                applicationPanacheQuery = appRepo.find("where (code=?1 or parent.code=?1)", Sort.descending("createdAt"), appCode);
        }
        applicationPanacheQuery = applicationPanacheQuery.filter("deletedAppFilter", Parameters.with("isDeleted", false));
        return applicationPanacheQuery;
    }

    @WithTransaction
    public Uni<ApplicationModel> getApplicationModel(int page, int size, String search, UserPrincipal principal) {

        final PanacheQuery<Application> query = getApplicationQuery(principal, search);
        Uni<ApplicationModel>  applicationModelUni = query.page(Page.of(page - 1, size))
                .list().map(result -> result.stream().map(ApplicationJson::fromDto).toList())
                .chain(result -> {
                    ApplicationModel model = WebUtils.createModel(new ApplicationModel(), appName);
                    model.user = principal.getUser();
                    model.data = result;
                    model.page = page;
                    model.size = size;
                    model.search = search;

                    Uni<Long>[] countOfChild = new Uni[]{Uni.createFrom().nullItem()};
                    model.data.forEach(item -> {
                        countOfChild[0] = countOfChild[0].chain(n -> appRepo.count("parent.code = ?1", item.getCode()))
                                .map(count -> {
                                    model.childAppCount.put(item.getCode(), count);
                                    return count;
                                });
                    });
                    return countOfChild[0].map(r -> model);
                })
                .chain(model -> query.count().map(count -> {
                    model.totalData = count.intValue();
                    return model;
                }))
                .map(model -> {
                    model.user = principal.getUser();
                    return model;
                });
        return createReturnResponse(applicationModelUni, principal);
    }

    @WithTransaction
    public Uni<Boolean> createApp(ApplicationJson app, UserPrincipal principal) {
        Application application = app.toDto();
        application.setCreatedBy(principal.getUser().getUsername());
        if (StringUtils.isBlank(app.getParentCode())) {
            return appRepo.persist(application).map(Objects::nonNull);
        } else {
            return appRepo.findById(app.getParentCode())
                    .chain(item -> {
                        if (item != null) {
                            if (item.getParent() != null)
                                throw new RuntimeException("Application child could not be a parent");

                            application.setParent(item);
                            return appRepo.persist(application);
                        }
                        throw new RuntimeException("Parent application with code:"+app.getParentCode()+" not found");
                    })
                    .map(Objects::nonNull);
        }
    }

    @WithTransaction
    public Uni<ApplicationDetailModel> getApplicationDetailsModel(int page, int size, String appCode, String search, UserPrincipal userPrincipal) {
        Uni<ApplicationDetailModel> applicationDetailModelUni = appRepo.find("code = ?1 and deletedAt is null", appCode)
                .firstResult()
                .map(application -> {
                    if (application == null) throw new HttpException(404, "Application:"+appCode+" not found!");
                    ApplicationDetailModel model = WebUtils.createModel(new ApplicationDetailModel(), appName);
                    model.user = userPrincipal.getUser();
                    model.page = page;
                    model.size = size;
                    model.search = search;
                    model.application = ApplicationJson.fromDto(application);
                    return model;
                })
                .chain(model -> roleRepo.findByApp(model.application.getCode())
                        .map(roles -> {
                            model.roles = roles.stream().map(RoleOnly::fromDTO).toList();
                            return model;
                        }))
                .chain(model -> {
                    PanacheQuery<Application> applicationPanacheQuery = appRepo.find("where parent.code=?1", model.application.getCode());
                    Uni<ApplicationDetailModel> modelUni = applicationPanacheQuery.page(Page.of(page - 1, size)).list().map(list -> {
                        try {
                            model.data = list.stream().map(ApplicationJson::fromDto)
                                    // to shorting the item by createdAt field with DESC and if there is any null value of createdAt it would be putted to the last
                                    .sorted(Comparator.comparing(ApplicationJson::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                                    .toList();
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                            model.data = new ArrayList<>();
                        }
                        return model;
                    });
                    modelUni = modelUni.chain(model1 -> applicationPanacheQuery.count().map(count -> {
                        model.totalData = count.intValue();
                        return model;
                    }));
                    return modelUni;
                });
        return createReturnResponse(applicationDetailModelUni, userPrincipal);
    }

    @WithTransaction
    public Uni<Boolean> deleteApplication(String appCode, String name) {
        return appRepo.findById(appCode)
                .chain(item -> {
                    if (item != null) {
                        item.setDeletedBy(name);
                        return appRepo.delete(item)
                                .onItem()
                                .transform(result -> true);
                    }
                    throw new RuntimeException("Application with code: "+appCode+" not found");
                });
    }

    @WithTransaction
    public Uni<UserPageModel> getUsersModel(int page, int size, String search, UserPrincipal principal) {
        Uni<UserPageModel> resultUni = appRepo.findById(principal.getAppCode())
                .chain(app -> userService.findAll(page, size, search, principal)
                        .map(listResponseJson -> {

                            UserPageModel model = WebUtils.createModel(new UserPageModel(), app.getName());
                            model.appName = principal.getAppCode();
                            model.user = principal.getUser();
                            model.data = listResponseJson.getData();
                            model.page = page;
                            model.size = size;

                            model.search = search;
                            return model;
                        }))
                .chain(data -> {
                    List<String> userIds = data.data.stream().map(User::getId).toList();
                    return userRoleRepo.find("where authUser.id in :ids", Sort.ascending("authUser.name"), Map.of("ids", userIds))
                            .list()
                            .map(userRoles -> {
                                userRoles.forEach(role -> {
                                    List<RoleOnly> roleOnliest = data.roles.computeIfAbsent(role.getUser().getId(), kx -> new ArrayList<>());
                                    roleOnliest.add(RoleOnly.fromDTO(role.getRole()));
                                });
                                return data;
                            })
                            .onFailure().invoke(throwable -> {
                                logger.error(throwable.getMessage(), throwable);
                            });
                });

        resultUni = resultUni.map(data -> {
            data.roles.forEach((k, v) -> {
                Map<String, List<String>> names = new HashMap<>();
                v.forEach(rol -> {
                    List<String> name = names.computeIfAbsent(rol.getAppCode(), kx -> new ArrayList<>());
                    name.add(rol.getName());
                });
                data.roleNames.put(k, JsonUtils.toJson(names));
            });
            return data;
        });
        return createReturnResponse(resultUni, principal);
    }

    @WithTransaction
    public Uni<Boolean> createUser(UserRestForm user, UserPrincipal userPrincipal) {
        UserOnly authUser = new UserOnly(null, user.username, user.email, user.name);
        return userService.createNewUser(authUser, user.appCode, user.roleCode, userPrincipal)
                .map(rx -> true);
    }

    @WithTransaction
    public Uni<RolesPageModel> getRoles(Integer page, int size, String search, UserPrincipal principal) {
        PanacheQuery<Role> applicationPanacheQuery;

        if (principal.getAppCode().equals("SYSTEM")) {
            if (!StringUtil.isNullOrEmpty(search)) {
                applicationPanacheQuery = roleRepo.find("where (name like ?1)", "%"+search.toLowerCase()+"%");
            } else
                applicationPanacheQuery = roleRepo.findAll();
        } else {
            String appCode = principal.getAppCode();

            if (!StringUtil.isNullOrEmpty(search)) {
                applicationPanacheQuery = roleRepo.find("where (appCode=?1) and (name like ?2)", appCode, "%"+search.toLowerCase()+"%");
            } else
                applicationPanacheQuery = roleRepo.find("where (appCode=?1)", appCode);
        }
        applicationPanacheQuery = applicationPanacheQuery.filter("deletedRoleFilter", Parameters.with("isDeleted", false));

        final PanacheQuery<Role> query = applicationPanacheQuery;
        Uni<RolesPageModel> rolesPageModelUni = query.page(Page.of(page - 1, size))
                .list().map(result -> result.stream().map(RoleOnly::fromDTO).toList())
                .chain(result -> query.count().map(count -> {
                    RolesPageModel model = WebUtils.createModel(new RolesPageModel(), appName);
                    model.user = principal.getUser();
                    model.data = result;
                    model.page = page;
                    model.size = size;
                    model.search = search;
                    model.totalData = count.intValue();
                    return model;
                }));
        return createReturnResponse(rolesPageModelUni, principal);
    }

    @WithTransaction
    public Uni<RoleFormModel> createRoleForm(RoleFormModel baseModel, UserPrincipal principal) {
        final PanacheQuery<Application> applicationPanacheQuery = getApplicationQuery(principal, "");;
        Uni<RoleFormModel> roleFormModelUni = applicationPanacheQuery.list()
                .map(listApp -> listApp.stream().map(
                        app -> {
                            ApplicationJson jsonApp = new ApplicationJson();
                            jsonApp.setCode(app.getCode());
                            jsonApp.setName(app.getName());
                            return jsonApp;
                        }
                ).toList())
                .map(result -> {
                    baseModel.apps = result;
                    return baseModel;
                });
        return createReturnResponse(roleFormModelUni, principal);
    }

    @WithTransaction
    public Uni<RoleOnly> createRole(RoleRestForm role, UserPrincipal principal) {
        if (StringUtils.isBlank(role.appCode)) throw new IllegalArgumentException("Application code can't be empty!");
        if (StringUtils.isBlank(role.code)) throw new IllegalArgumentException("Role code can't be empty!");
        if (StringUtils.isBlank(role.name)) throw new IllegalArgumentException("Role name can't be empty!");

        Role eRole = new Role(null, role.appCode, role.code, role.name, role.description);
        eRole.setCreatedBy(principal.getUser().getUsername());

        return roleRepo.findById(role.appCode, role.code)
                .chain(result -> {
                    if (result != null) {
                        String msg = "Role with code:"+role.code+" in application:"+role.appCode+" already exists!";
                        throw new HttpException(HttpResponseStatus.CONFLICT.code(), msg, new Exception(msg));
                    }
                    return Uni.createFrom().nullItem();
                })
                .chain(r -> roleRepo.persist(eRole))
                .map(RoleOnly::fromDTO);
    }

    @WithTransaction
    public Uni<RoleFormAddPermissionModel> createRoleFormAddPermission(String roleId, RoleFormAddPermissionModel model, UserPrincipal principal) {
        if (StringUtils.isBlank(roleId)) throw new IllegalArgumentException("{role_id} can't be empty in path \"web/v1/roles/{role_id}/add_permission\"");
        Uni<RoleFormAddPermissionModel> roleFormAddPermissionModelUni = roleRepo.findById(roleId)
                .chain(role -> permissionRepo.findByAppCode(role.getAppCode()).map(permissions -> {
                            model.role = RoleOnly.fromDTO(role);
                            model.permissions = permissions.stream()
                                    .map(Permission::fromDTO)
                                    .collect(Collectors.toMap(org.acme.authorization.json::getId, permission -> permission));

                            model.rolePermissions = role.getPermissions().stream()
                                    .sorted((o1, o2) -> o2.getPermission().getCreatedAt().compareTo(o1.getPermission().getCreatedAt()))
                                    .map(rolePermission -> Permission.fromDTO(rolePermission.getPermission()))
                                    .collect(Collectors.toMap(org.acme.authorization.json::getId, permission -> permission));
                            return model;
                }));
        return createReturnResponse(roleFormAddPermissionModelUni, principal);
    }

    @WithTransaction
    public Uni<PermissionPageModel> getPermission(Integer page, int size, String search, UserPrincipal principal) {
        PanacheQuery<org.acme.authenticationService.data.entity.Permission> permissionPanacheQuery;

        if (principal.getAppCode().equals("SYSTEM")) {
            if (!StringUtil.isNullOrEmpty(search)) {
                permissionPanacheQuery = permissionRepo.find("where (name like ?1)", Sort.descending("createdAt"), "%"+search.toLowerCase()+"%");
            } else
                permissionPanacheQuery = permissionRepo.findAll(Sort.descending("createdAt"));
        } else {
            String appCode = principal.getAppCode();

            if (!StringUtil.isNullOrEmpty(search)) {
                permissionPanacheQuery = permissionRepo.find("where (appCode=?1) and (name like ?2)", Sort.descending("createdAt"), appCode, "%"+search.toLowerCase()+"%");
            } else
                permissionPanacheQuery = permissionRepo.find("where (appCode=?1)", Sort.descending("createdAt"), appCode);
        }
        permissionPanacheQuery = permissionPanacheQuery.filter("deletedPermissionFilter", Parameters.with("isDeleted", false));

        final PanacheQuery<org.acme.authenticationService.data.entity.Permission> query = permissionPanacheQuery;
        Uni<PermissionPageModel> permissionPageModelUni = query.page(Page.of(page - 1, size))
                .list().map(result -> result.stream().map(Permission::fromDTO).toList())
                .chain(result -> query.count().map(count -> {
                    PermissionPageModel model = WebUtils.createModel(new PermissionPageModel(), appName);
                    model.user = principal.getUser();
                    model.data = result;
                    model.page = page;
                    model.size = size;
                    model.search = search;
                    model.totalData = count.intValue();
                    return model;
                }));
        return createReturnResponse(permissionPageModelUni, principal);
    }

    @WithTransaction
    public Uni<PermissionFormModel> createPermissionForm(PermissionFormModel baseModel, UserPrincipal principal) {
        final PanacheQuery<Application> applicationPanacheQuery = getApplicationQuery(principal, "");;
        Uni<PermissionFormModel> permissionPageModelUni = applicationPanacheQuery.list()
                .map(listApp -> listApp.stream().map(
                        app -> {
                            ApplicationJson jsonApp = new ApplicationJson();
                            jsonApp.setCode(app.getCode());
                            jsonApp.setName(app.getName());
                            return jsonApp;
                        }
                ).toList())
                .map(result -> {
                    baseModel.apps = result;
                    return baseModel;
                });
        return createReturnResponse(permissionPageModelUni, principal);
    }

    @WithTransaction
    public Uni<Boolean> createPermission(PermissionRestForm permission, UserPrincipal principal) {
        org.acme.authenticationService.data.entity.Permission ePermission = new org.acme.authenticationService.data.entity.Permission(permission.appCode, permission.code, permission.name);
        ePermission.setCreatedBy(principal.getUser().getUsername());

        return permissionRepo.findByAppAndCode(permission.appCode, permission.code)
                .chain(perm -> {
                    if (perm != null) throw new HttpException(HttpResponseStatus.CONFLICT.code(), "Permission: "+permission.code+" already exists in application:"+permission.appCode+"!");
                    return permissionRepo.save(ePermission);
                })
                .map(Objects::nonNull);
    }

    @WithTransaction
    public Uni<RoleOnly> addPermissionToRole(String id, RoleAddPermissionRestForm data, UserPrincipal principal) {
        return roleRepo.findById(id)
                .chain(result -> appRepo.findById(result.getAppCode())
                        .chain(app -> permissionRepo.find("where id in (:ids)", Map.of("ids", Arrays.asList(data.permissions))).list()
                                .onFailure().invoke(throwable -> logger.error("find in ids error: "+throwable.getMessage(), throwable))
                                .map(permissions -> {
                                    result.setUpdatedBy(principal.getUser().getUsername());
                                    result.setUpdatedAt(new Date());
                                    return permissions.stream().map(p -> {
                                        if (result.getAppCode().equals(p.getAppCode()) || (app.getParent() != null && app.getParent().getCode().equals(p.getAppCode())))
                                            return new RolePermission(result, p);
                                        throw new IllegalArgumentException("Permission :"+p.getName()+" is not allowed to assigned to role:"+result.getName()+" because they are difference application!");
                                    }).toList();
                                }))
                ).chain(rolePermissions -> rolePermissionRepo.persist(rolePermissions))
                .chain(r -> roleRepo.findById(id))
                .map(RoleOnly::fromDTO);
    }

    @WithTransaction
    public Uni<Boolean> unAssignPermission(String roleId, String permissionId, String name) {
        return roleRepo.findById(roleId)
                .chain(role -> {
                    if (role == null) throw new NotFoundException("Role with id:"+roleId+" not found!");
                    return permissionRepo.findById(permissionId)
                            .chain(permission -> {
                                if (permission == null) throw new NotFoundException("Permission with id:"+permissionId+" not found!");
                                return rolePermissionRepo.delete("where id.role.id=?1 and id.permission.id=?2", role.getId(), permission.getId())
                                        .map(result -> {
                                            logger.info("delete result:"+result);
                                            return result > 0;
                                        });
                            })
                            .onItem().call(result -> {
                                role.setUpdatedAt(new Date());
                                role.setUpdatedBy(name);
                                return Uni.createFrom().item(true);
                            })
                            .onFailure().call(throwable -> {
                                logger.error(throwable.getMessage(), throwable);
                                return Uni.createFrom().item(false);
                            });
                });
    }
}
