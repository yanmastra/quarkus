package com.acme.authorization.it;

import com.acme.authorization.json.UserOnly;
import com.acme.authorization.security.Authorizer;
import com.acme.authorization.security.UserPrincipal;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.quarkus.test.junit.QuarkusTest;
import io.vertx.ext.web.handler.HttpException;
import jakarta.inject.Singleton;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.UUID;

import static io.restassured.RestAssured.given;

@QuarkusTest
public class AuthorizationResourceTest {

    @Singleton
    public Authorizer provideAuthorizer(){
        return new Authorizer() {
            @Override
            public UserPrincipal authorize(String accessToken) {
                if (!dummyToken.equals(accessToken)) throw new HttpException(HttpResponseStatus.BAD_REQUEST.code(), "Invalid token!");
                UserOnly userOnly = new UserOnly(UUID.randomUUID().toString(), "testuser", "testuser@test.io", "Test User");
                return new UserPrincipal(userOnly, Arrays.asList("VIEW_ALL", "CREATE_ALL"), "TEST_APP", accessToken);
            }

            @Override
            public int getPriority() {
                return 1;
            }
        };
    }

    private static final String dummyToken = UUID.randomUUID().toString();
    private static final String tokenNotProvided="{\"success\":false,\"message\":\"Token not provided!\"}";
    private static final String accessDenied="{\"success\":false,\"message\":\"Access Denied!\"}";

    @Test
    public void testWithToken() {
        given()
                .when()
                .header(HttpHeaders.AUTHORIZATION, dummyToken)
                .get("/authorization")
                .then()
                .statusCode(200);
    }

    @Test
    public void testWithoutTokenWithoutAccept() {
        given()
                .when()
                .get("/authorization")
                .then()
                .statusCode(400)
                .body(Matchers.is(tokenNotProvided));
    }

    @Test
    public void testWithoutTokenWithAccept() {
        given()
                .when()
                .accept(MediaType.APPLICATION_JSON)
                .get("/authorization")
                .then()
                .statusCode(400)
                .rootPath("/authorization/user")
                .body(Matchers.is(tokenNotProvided));
    }

    @Test
    public void testWithTokenWithAccept() {
        given()
                .when()
                .accept(MediaType.APPLICATION_JSON)
                .cookie(HttpHeaders.AUTHORIZATION, dummyToken)
                .get("/authorization")
                .then()
                .statusCode(200);
    }

    @Test
    public void testWithKey() {
        given()
                .when()
                .queryParam("key", dummyToken)
                .get("/authorization")
                .then()
                .statusCode(200);
    }

    @Test
    public void testWithKeyNotFound() {
        given()
                .when()
                .queryParam("key", dummyToken)
                .get("/user")
                .then()
                .statusCode(204);
    }

    @Test
    public void testWithKeyWithAcceptNotFound() {
        given()
                .when()
                .accept(MediaType.APPLICATION_JSON)
                .queryParam("key", dummyToken)
                .get("/user")
                .then()
                .statusCode(404);
    }


    @Test
    public void testWithKeyWithAcceptForbidden() {
        given()
                .when()
                .accept(MediaType.APPLICATION_JSON)
                .queryParam("key", dummyToken)
                .get("/authorization/user_get_data")
                .then()
                .statusCode(403)
                .body(Matchers.is(accessDenied));
    }

    @Test
    public void testWithKeyWithoutTokenToRestrictedEndpoint() {
        given()
                .when()
                .get("/authorization/user_get_data")
                .then()
                .statusCode(400)
                .body(Matchers.is(tokenNotProvided));
    }

    @Test
    public void testWithKeyWithoutTokenToPublicEndpoint() {
        given()
                .when()
                .get("/auth")
                .then()
                .statusCode(200);
    }
}
