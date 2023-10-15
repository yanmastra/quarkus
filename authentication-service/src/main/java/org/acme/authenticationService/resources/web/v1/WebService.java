package org.acme.authenticationService.resources.web.v1;

import com.acme.authorization.security.UserPrincipal;
import io.quarkus.hibernate.reactive.panache.PanacheQuery;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.quarkus.runtime.util.StringUtil;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.authenticationService.dao.ApplicationJson;
import org.acme.authenticationService.dao.web.ApplicationModel;
import org.acme.authenticationService.dao.web.Home;
import org.acme.authenticationService.data.entity.Application;
import org.acme.authenticationService.data.entity.Role;
import org.acme.authenticationService.data.repository.ApplicationRepository;
import org.acme.authenticationService.data.repository.RoleRepository;
import org.acme.authenticationService.data.repository.UserRepository;
import org.acme.authenticationService.data.repository.UserRoleRepository;
import org.acme.authenticationService.resources.web.WebUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

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
                    model.data = result;
                    model.page = page;
                    model.size = size;
                    model.search = search;
                    return Uni.createFrom().item(model);
                })
                .chain(model -> query.count().map(count -> {
                    model.totalData = count.intValue();
                    return model;
                }));
    }

    @WithTransaction
    public Uni<Boolean> createApp(ApplicationJson app, UserPrincipal principal) {
        Application application = new Application(null, app.getName(), app.getDescription());
        application.setCreatedBy(principal.getUser().getUsername());
        return appRepo.persist(application).map(Objects::nonNull);
    }
}
