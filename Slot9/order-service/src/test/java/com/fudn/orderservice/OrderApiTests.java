package com.fudn.orderservice;

import com.fudn.orderservice.repository.OrderRepository;
import com.fudn.orderservice.stub.InventoryStubs;
import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.utility.DockerImageName;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test-case bo sung cho Order API khi da goi Inventory Service qua OpenFeign.
 * Inventory Service duoc gia lap bang WireMock, MySQL chay bang Testcontainers.
 * Ghi chu: chua co validation input va exception handler -> moi loi tu Inventory deu tra 500.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableWireMock(@ConfigureWireMock(baseUrlProperties = "inventory.url"))
class OrderApiTests {

    @ServiceConnection
    static MySQLContainer mySQLContainer = new MySQLContainer(DockerImageName.parse("mysql:8.3.0"));

    @LocalServerPort
    private Integer port;

    @Autowired
    private OrderRepository orderRepository;

    static {
        mySQLContainer.start();
    }

    @BeforeEach
    void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    // Dat hang so luong lon, gia thap phan, con hang -> 201 va luu 1 ban ghi
    @Test
    void shouldSubmitOrderWithLargeQuantityAndDecimalPrice() {
        InventoryStubs.stubInventoryCall("pixel_8", 5);
        long before = orderRepository.count();

        String json = """
                { "skuCode": "pixel_8", "price": 899.99, "quantity": 5 }
                """;
        var body = RestAssured.given().contentType("application/json").body(json)
                .when().post("/api/order")
                .then().statusCode(201)
                .extract().body().asString();

        assertThat(body, Matchers.is("Order Placed Successfully"));
        assertThat(orderRepository.count(), Matchers.is(before + 1));
    }

    // Het hang -> 500 va KHONG luu don
    @Test
    void shouldNotSaveOrderWhenOutOfStock() {
        InventoryStubs.stubInventoryOutOfStock("iphone_15", 101);
        long before = orderRepository.count();

        String json = """
                { "skuCode": "iphone_15", "price": 1000, "quantity": 101 }
                """;
        RestAssured.given().contentType("application/json").body(json)
                .when().post("/api/order")
                .then().statusCode(500);

        assertThat(orderRepository.count(), Matchers.is(before));
    }

    // SKU khong ton tai: Inventory tra false -> 500
    @Test
    void shouldFailWhenSkuDoesNotExist() {
        InventoryStubs.stubInventoryOutOfStock("nokia_3310", 1);

        String json = """
                { "skuCode": "nokia_3310", "price": 50, "quantity": 1 }
                """;
        RestAssured.given().contentType("application/json").body(json)
                .when().post("/api/order")
                .then().statusCode(500);
    }

    // Inventory Service loi (503) -> Feign nem FeignException -> 500, khong luu don
    @Test
    void shouldFailWhenInventoryServiceUnavailable() {
        stubFor(get(urlPathEqualTo("/api/inventory"))
                .willReturn(aResponse().withStatus(503)));
        long before = orderRepository.count();

        String json = """
                { "skuCode": "iphone_15", "price": 1000, "quantity": 1 }
                """;
        RestAssured.given().contentType("application/json").body(json)
                .when().post("/api/order")
                .then().statusCode(500);

        assertThat(orderRepository.count(), Matchers.is(before));
    }

    // Thieu skuCode: Inventory that tu choi (400 thieu tham so bat buoc) -> 500
    @Test
    void shouldFailWhenSkuCodeMissing() {
        stubFor(get(urlPathEqualTo("/api/inventory"))
                .willReturn(aResponse().withStatus(400)));

        String json = """
                { "price": 1000, "quantity": 1 }
                """;
        RestAssured.given().contentType("application/json").body(json)
                .when().post("/api/order")
                .then().statusCode(500);
    }

    // Sai kieu quantity ("abc") -> 400, Jackson loi truoc khi goi Inventory
    @Test
    void shouldReturn400WhenQuantityWrongType() {
        String json = """
                { "skuCode": "galaxy_24", "price": 1000, "quantity": "abc" }
                """;
        RestAssured.given().contentType("application/json").body(json)
                .when().post("/api/order")
                .then().statusCode(400);

        verify(0, anyRequestedFor(anyUrl()));
    }

    // Sai method (GET thay vi POST) -> 405
    @Test
    void shouldReturn405WhenMethodNotAllowed() {
        RestAssured.given()
                .when().get("/api/order")
                .then().statusCode(405);
    }

    // Sai path (/api/orders) -> 404
    @Test
    void shouldReturn404WhenPathWrong() {
        RestAssured.given().contentType("application/json").body("{}")
                .when().post("/api/orders")
                .then().statusCode(404);
    }
}
