package com.acme.authorization.it;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

@QuarkusTest
public class AuthorizationResourceTest {

    @Test
    public void testHelloEndpoint() {
        given()
                .when().get("/authorization")
                .then()
                .statusCode(200)
                .body(is("Hello authorization"));
    }
}
