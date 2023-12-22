package org.acme.authenticationService.services;

import com.acme.authorization.json.AuthenticationResponse;
import com.acme.authorization.json.ResponseJson;
import com.acme.authorization.utils.JsonUtils;
import com.acme.authorization.utils.UrlUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.smallrye.mutiny.Uni;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import org.acme.authenticationService.data.entity.*;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.hamcrest.Matchers;
import org.hibernate.reactive.mutiny.Mutiny;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.*;

import static io.restassured.RestAssured.given;

@QuarkusTest
public class AuthenticationEndpointTest {

    @Inject
    Logger logger;
    private static final Random random = new Random();

    @ConfigProperty(name = "quarkus.http.port", defaultValue = "10001")
    public String port;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    Mutiny.SessionFactory sf;

    @PostConstruct
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

        List<Permission> permissions = Arrays.asList(
                new Permission(data.getCode(), "CREATE_USER", "Create Test User"),
                new Permission(data.getCode(), "VIEW_USER", "View Test User"),
                new Permission(data.getCode(), "UPDATE_USER", "Update Test User"),
                new Permission(data.getCode(), "CREATE_ROLE", "Create Role Test User"),
                new Permission(data.getCode(), "ROLE_ADD_PERMISSION", "Add permission to Role Test User"),
                new Permission(data.getCode(), "VIEW_ROLE", "View Roles Test User")
        );

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

            for (Permission p: permissions) {
                existing = existing.chain(r -> RolePermission.find("where role.appCode=?1 role.code=?2 and permission.code=?3", role.getAppCode(), role.getCode(), p.getId())
                        .firstResult()
                        .onItem().invoke(existing1 -> {
                            if (existing1 == null) {
                                RolePermission rp = new RolePermission();
                                rp.setRole(role);
                                rp.setPermission(p);
                                willPersisted.add(rp);
                            }
                        }));
            }
            return existing.call(r -> session.persistAll(willPersisted.toArray()));
        });
        userUni
                .onFailure()
                .invoke(throwable -> {
                    assert throwable != null;
                    logger.error("Error when creating test data: "+throwable.getMessage(), throwable);
                })
                .subscribe().with(authUser1 -> logger.info("dummy user created:"));
    }

    @Test
    public void authenticationTest() throws Throwable {
        init();

        JsonObject jo = Json.createObjectBuilder()
                .add("username", "wayan_mastra@gmail.com")
                .add("password", "password")
                .add("app_code", "TEST_UNIT_APPLICATION")
                .build();
        String body = jo.toString();
                given().body(body)
                .contentType(ContentType.JSON)
                .header("Accept", "application/json")
                .post(URI.create("/api/v1/auth/authenticate"))
                .then()
                .statusCode(200);
    }

    @Test
    public void testAuthenticateWithUnknownApp() {
        JsonObject jo = Json.createObjectBuilder()
                .add("username", "wayan_mastra@gmail.com")
                .add("password", "password")
                .add("app_code", "XX_APPLICATION")
                .build();
        String body = jo.toString();
        given().body(body)
                .contentType(ContentType.JSON)
                .header("Accept", "application/json")
                .post(URI.create("/api/v1/auth/authenticate"))
                .then()
                .statusCode(403)
                .body(Matchers.is("{\"success\":false,\"message\":\"Application not found\"}"));

        jo = Json.createObjectBuilder()
                .add("username", "system@root.io")
                .add("password", "password")
                .add("app_code", "TEST_UNIT_APPLICATION")
                .build();
        body = jo.toString();
        given().body(body)
                .contentType(ContentType.JSON)
                .header("Accept", "application/json")
                .post(URI.create("/api/v1/auth/authenticate"))
                .then()
                .statusCode(404)
                .body(Matchers.is("{\"success\":false,\"message\":\"Invalid credential\"}"));
    }

    @Test
    public void testCreateUser() throws Throwable {
        JsonObject jo = Json.createObjectBuilder()
                .add("username", "wayan_mastra@gmail.com")
                .add("password", "password")
                .add("app_code", "TEST_UNIT_APPLICATION")
                .build();
        String body = jo.toString();
        String sBody = UrlUtils.call(
                HttpMethod.POST,
                "http://localhost:"+port+"/api/v1/auth/authenticate",
                body,
                Map.of(HttpHeaders.CONTENT_TYPE+"", ContentType.JSON+"", HttpHeaders.ACCEPT+"", ContentType.JSON+""),
                true
        );

//        assert "{}".equals(sBody):
//                sBody;

        ResponseJson<AuthenticationResponse> response = JsonUtils.fromJson(sBody, new TypeReference<>() {
        });

        assert response != null;


        String username = "wayan.mastra+"+random.nextInt(999);
        body = Json.createObjectBuilder()
                .add("email", username+"@gmail.com")
                .add("username", username)
                .add("name", "Test Dummy User "+username)
                .build().toString();

        given().body(body)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .auth().oauth2(response.getData().accessToken)
                .post(URI.create("/api/v1/user"))
                .then()
                .body(Matchers.is("{}"))
                .statusCode(200);
    }
}
