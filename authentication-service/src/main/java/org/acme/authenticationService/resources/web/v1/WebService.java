package org.acme.authenticationService.resources.web.v1;

import com.acme.authorization.security.UserPrincipal;
import io.quarkus.hibernate.reactive.panache.PanacheQuery;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.quarkus.runtime.util.StringUtil;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.handler.HttpException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.authenticationService.dao.ApplicationJson;
import org.acme.authenticationService.dao.RoleOnly;
import org.acme.authenticationService.dao.web.ApplicationDetailModel;
import org.acme.authenticationService.dao.web.ApplicationModel;
import org.acme.authenticationService.dao.web.Home;
import org.acme.authenticationService.data.entity.Application;
import org.acme.authenticationService.data.entity.Role;
import org.acme.authenticationService.data.repository.ApplicationRepository;
import org.acme.authenticationService.data.repository.RoleRepository;
import org.acme.authenticationService.data.repository.UserRepository;
import org.acme.authenticationService.data.repository.UserRoleRepository;
import org.acme.authenticationService.resources.web.WebUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;

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

    @WithTransaction
    public Uni<Home> getHomePageData(UserPrincipal principal) {
        return Uni.createFrom().item(principal)
                .chain(ctx -> {
                    if (ctx.getAppCode().equals("SYSTEM")) {
                        Uni<Home> homeUni = appRepo.count().map(count -> {
                            Home home = WebUtils.createModel(new Home(), appName);
                            home.applicationCount = count;
                            return home;
                        }).chain(home -> userRepo.count().map(uCount -> {
                            home.userCount = uCount;
                            return home;
                        }));
                        return homeUni;
                    } else {
                        String appCode = ctx.getAppCode();
                        Uni<Home> homeUni = appRepo.find("where code=?1", appCode).firstResult().chain(
                                app -> appRepo.count("where parent=?1 and deletedAt is null", app).map(count -> {
                                    Home home = WebUtils.createModel(new Home(), app.getName());
                                    home.applicationCount = count;
                                    return home;
                                }))
                                .chain(home -> roleRepo.findByApp(appCode).chain(result -> {
                                    Uni<Long> countUni = Uni.createFrom().item(0L);
                                    for (Role r: result) {
                                        countUni = countUni.chain(count -> userRoleRepo.count("where role=?1", r).map(res -> count + res));
                                    }
                                    return countUni;
                                }).map(userCount -> {
                                    home.userCount = userCount;
                                    return home;
                                }));
                        return homeUni;
                    }
                });
    }

    @WithTransaction
    public Uni<ApplicationModel> getApplicationModel(int page, int size, String search, UserPrincipal principal) {
        PanacheQuery<Application> applicationPanacheQuery;

        if (principal.getAppCode().equals("SYSTEM")) {
            if (!StringUtil.isNullOrEmpty(search)) {
                applicationPanacheQuery = appRepo.find("where (name like ?1 or parent.name like ?1)", "%"+search.toLowerCase()+"%");
            } else
                applicationPanacheQuery = appRepo.findAll();
        } else {
            String appCode = principal.getAppCode();

            if (!StringUtil.isNullOrEmpty(search)) {
                applicationPanacheQuery = appRepo.find("where (code=?1 or parent.code=?1) and (name like ?2 or parent.name like ?2)", appCode, "%"+search.toLowerCase()+"%");
            } else
                applicationPanacheQuery = appRepo.find("where (code=?1 or parent.code=?1)", appCode);
        }
        applicationPanacheQuery = applicationPanacheQuery.filter("deletedAppFilter", Parameters.with("isDeleted", false));

        final PanacheQuery<Application> query = applicationPanacheQuery;
        return query.page(Page.of(page - 1, size))
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
                }));
    }

    @WithTransaction
    public Uni<Boolean> createApp(ApplicationJson app, UserPrincipal principal) {
        Application application = new Application(app.getCode(), app.getName(), app.getDescription());
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
        return appRepo.find("code = ?1 and deletedAt is null", appCode)
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
}
