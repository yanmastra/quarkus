package org.acme.authenticationService.services;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import org.acme.authenticationService.data.entity.Application;
import org.acme.authenticationService.data.entity.AuthUser;
import org.acme.authenticationService.data.entity.Role;
import org.acme.authenticationService.data.entity.UserRole;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;

@QuarkusTest
public class AuthenticationEndpointTest {

    @Inject
    Logger logger;


    @Inject
    Mutiny.SessionFactory sf;

//    @PostConstruct
    public void init() {
        AuthUser authUser = new AuthUser(
                null,
                "wayan_mastra",
                "wayan_mastra@gmail.com",
                "Wayan Mastra"
        );
        authUser.setVerified(true);
        authUser.setPasswordTextPlain("password");
        saveUser(authUser);
    }

    public void saveUser(AuthUser authUser) {
        Application data = new Application(
                "TEST_UNIT_APPLICATION",
                "Test Unit Application",
                "This is dummy test data"
        );
        data.setCreatedBy("TEST_UNIT");

        Role role = new Role(
                null,
                "TEST_UNIT_APPLICATION",
                "TEST_UNIT_ROLE_TESTER",
                "Test Unit Tester",
                "This is dummy test data"
        );
        role.setCreatedBy("TEST_UNIT");

        Uni<?> userUni = sf.withTransaction(session -> {
            List<PanacheEntityBase> willPersisted = new ArrayList<>();
            Uni<?> existing = session.find(Application.class, data.getCode())
                    .onItem().invoke(existedApp -> {
                        if (existedApp != null) {
                            data.setCode(existedApp.getCode());
                        } else {
                            willPersisted.add(data);
                        }
                    });

            existing = existing.chain(r -> AuthUser.find("where username=?1", authUser.getUsername())
                    .firstResult().onItem().invoke(existing1 -> {
                        if (existing1 instanceof AuthUser sAuthUser){
                            authUser.setId(sAuthUser.getId());
                        } else {
                            willPersisted.add(authUser);
                        }
                    }));

            existing = existing.chain(r -> Role.find("where code=?1", role.getCode())
                    .firstResult()
                    .onItem().invoke(existing1 -> {
                        if (existing1 instanceof Role sRole) {
                            role.setId(sRole.getId());
                            role.setCode(sRole.getCode());
                        } else {
                            willPersisted.add(role);
                        }
                    }));
            existing = existing.chain(r -> UserRole.find("where authUser.id=?1 and role.id=?2", authUser.getId(), role.getId())
                    .firstResult()
                    .onItem().invoke(existing1 -> {
                        if (existing1 == null) {
                            UserRole userRole = new UserRole(authUser, role);
                            willPersisted.add(userRole);
                        }
                    }));
            return existing.call(r -> session.persistAll(willPersisted.toArray()));
        });
        userUni.subscribe().with(authUser1 -> logger.info("dummy user created:"));
    }

    @Test
    public void authenticationTest() {
        init();

        JsonObject jo = Json.createObjectBuilder()
                .add("username", "wayan_mastra@gmail.com")
                .add("password", "password")
                .add("app_code", "TEST_UNIT_APPLICATION")
                .build();
        String body = jo.toString();
        Response response = given().body(body)
                .contentType("application/json")
                .header("Accept", "application/json")
                .post(URI.create("/api/v1/auth/authenticate"))
                .then()
                .statusCode(200)
                .extract()
                .response();
    }
}
