package com.fudn.orderservice;

import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test-case bo sung cho Order API, bam sat cac tinh huong mo ta trong order-service_test.md.
 * Ghi chu: Part 1 chua co validation input, nen thieu field / body rong van tra 201 (ghi nhan hanh vi hien tai).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderApiTests {

    @ServiceConnection
    static MySQLContainer mySQLContainer = new MySQLContainer(DockerImageName.parse("mysql:8.3.0"));

    @LocalServerPort
    private Integer port;

    static {
        mySQLContainer.start();
    }

    @BeforeEach
    void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    // TC2 - Dat hang so luong lon, gia thap phan -> 201
    @Test
    void shouldSubmitOrderWithLargeQuantityAndDecimalPrice() {
        String json = """
                { "skuCode": "pixel_8", "price": 899.99, "quantity": 5 }
                """;
        var body = RestAssured.given().contentType("application/json").body(json)
                .when().post("/api/order")
                .then().statusCode(201)
                .extract().body().asString();
        assertThat(body, Matchers.is("Order Placed Successfully"));
    }

    // TC3 - Thieu skuCode -> 201 (chua co validation, sku_code luu null)
    @Test
    void shouldReturn201WhenSkuCodeMissing() {
        String json = """
                { "price": 1000, "quantity": 1 }
                """;
        RestAssured.given().contentType("application/json").body(json)
                .when().post("/api/order")
                .then().statusCode(201);
    }

    // TC4 - Sai kieu quantity ("abc") -> 400 (Jackson khong parse duoc)
    @Test
    void shouldReturn400WhenQuantityWrongType() {
        String json = """
                { "skuCode": "galaxy_24", "price": 1000, "quantity": "abc" }
                """;
        RestAssured.given().contentType("application/json").body(json)
                .when().post("/api/order")
                .then().statusCode(400);
    }

    // TC6 - Body rong {} -> 201 (chua co validation)
    @Test
    void shouldReturn201WhenBodyEmpty() {
        RestAssured.given().contentType("application/json").body("{}")
                .when().post("/api/order")
                .then().statusCode(201);
    }

    // TC7 - Sai method (GET thay vi POST) -> 405
    @Test
    void shouldReturn405WhenMethodNotAllowed() {
        RestAssured.given()
                .when().get("/api/order")
                .then().statusCode(405);
    }

    // TC8 - Sai path (/api/orders) -> 404
    @Test
    void shouldReturn404WhenPathWrong() {
        RestAssured.given().contentType("application/json").body("{}")
                .when().post("/api/orders")
                .then().statusCode(404);
    }
}
