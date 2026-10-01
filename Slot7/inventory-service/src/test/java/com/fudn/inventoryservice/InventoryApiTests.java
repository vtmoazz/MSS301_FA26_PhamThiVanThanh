package com.fudn.inventoryservice;

import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test-case bo sung cho Inventory API, bam sat inventory-service_test.md.
 * Chu y boundary test GreaterThanEqual (>=) va ghi nhan thieu validation (quantity am).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class InventoryApiTests {

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

    private boolean checkStock(String skuCode, String quantity) {
        return RestAssured.given()
                .when().get("/api/inventory?skuCode=" + skuCode + "&quantity=" + quantity)
                .then().statusCode(200)
                .extract().response().as(Boolean.class);
    }

    // TC3 - Boundary: dung bang ton kho (100) -> true (vi dung >=)
    @Test
    void shouldReturnTrueWhenQuantityEqualsStock() {
        assertTrue(checkStock("pixel_8", "100"));
    }

    // TC4 - Boundary: vuot 1 don vi (101) -> false
    @Test
    void shouldReturnFalseWhenQuantityExceedsStockByOne() {
        assertFalse(checkStock("pixel_8", "101"));
    }

    // TC5 - SKU khong ton tai -> false (khong loi)
    @Test
    void shouldReturnFalseWhenSkuNotExist() {
        assertFalse(checkStock("not_exist_sku", "1"));
    }

    // TC6 - Thieu quantity -> 400
    @Test
    void shouldReturn400WhenQuantityMissing() {
        RestAssured.given()
                .when().get("/api/inventory?skuCode=iphone_15")
                .then().statusCode(400);
    }

    // TC7 - Thieu skuCode -> 400
    @Test
    void shouldReturn400WhenSkuCodeMissing() {
        RestAssured.given()
                .when().get("/api/inventory?quantity=10")
                .then().statusCode(400);
    }

    // TC8 - quantity sai kieu (abc) -> 400
    @Test
    void shouldReturn400WhenQuantityWrongType() {
        RestAssured.given()
                .when().get("/api/inventory?skuCode=iphone_15&quantity=abc")
                .then().statusCode(400);
    }

    // TC9 - quantity am (-5) -> 200 + true (ghi nhan: chua co validation chan so am)
    @Test
    void shouldReturnTrueWhenQuantityNegative_currentBehavior() {
        assertTrue(checkStock("iphone_15", "-5"));
    }

    // TC10 - Sai method (POST) -> 405
    @Test
    void shouldReturn405WhenMethodNotAllowed() {
        RestAssured.given()
                .when().post("/api/inventory?skuCode=iphone_15&quantity=1")
                .then().statusCode(405);
    }
}
