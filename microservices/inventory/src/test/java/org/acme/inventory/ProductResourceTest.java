package org.acme.inventory;

import com.acme.authorization.utils.JsonUtils;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.acme.inventory.json.ProductJson;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Random;

import static io.restassured.RestAssured.given;

@QuarkusTest
public class ProductResourceTest {

    private static final Random random = new Random();

    @Test
    public void testHelloEndpoint() {
        ProductJson productJson = new ProductJson();
        productJson.setCode(random.nextLong(9999999999999L)+"");
        productJson.setName("Product "+random.nextInt(999));
        productJson.setPrice(BigDecimal.valueOf(100 * random.nextInt(99)));
        productJson.setCogs(BigDecimal.valueOf(10 * random.nextInt(99)));
        productJson.setStock(1000L);
        productJson.setStockOnHold(0L);
        productJson.setStockOutstanding(1000L);

        given()
                .when()
                .body(JsonUtils.toJson(productJson))
                .contentType(ContentType.JSON)
                .post("/api/v1/product")
                .then()
                .statusCode(200);
    }
}
