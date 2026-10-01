package com.fudn.gateway;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test routing cua API Gateway: khong can Docker hay cac service that.
 * Ca 3 route duoc tro sang cung 1 WireMock server dong vai Product / Order / Inventory.
 */
@SpringBootTest
@AutoConfigureMockMvc
@EnableWireMock(@ConfigureWireMock(baseUrlProperties = {
        "services.product.url", "services.order.url", "services.inventory.url"}))
class ApiGatewayRoutingTests {

    private static final String ORDER_JSON = """
            {"skuCode":"iphone_15","price":1000,"quantity":1}
            """;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthEndpointShouldBeUp() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("UP")));
    }

    @Test
    void getProductsShouldBeRoutedToProductService() throws Exception {
        stubFor(WireMock.get(urlEqualTo("/api/products"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[{\"id\":\"1\",\"name\":\"iPhone 15\",\"price\":1000}]")));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("iPhone 15")));

        verify(getRequestedFor(urlEqualTo("/api/products")));
    }

    @Test
    void postProductShouldForwardBody() throws Exception {
        String productJson = """
                {"name":"iPhone 15","description":"iPhone 15 is a smartphone from Apple","price":1000}
                """;
        stubFor(WireMock.post(urlEqualTo("/api/products"))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":\"abc\",\"name\":\"iPhone 15\"}")));

        mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content(productJson))
                .andExpect(status().isCreated())
                .andExpect(content().string(containsString("\"id\":\"abc\"")));

        verify(postRequestedFor(urlEqualTo("/api/products")).withRequestBody(equalToJson(productJson)));
    }

    @Test
    void productSubPathShouldBeRouted() throws Exception {
        stubFor(WireMock.put(urlEqualTo("/api/products/abc"))
                .willReturn(aResponse().withStatus(200).withBody("updated")));

        mockMvc.perform(put("/api/products/abc").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk())
                .andExpect(content().string("updated"));

        verify(putRequestedFor(urlEqualTo("/api/products/abc")));
    }

    @Test
    void postOrderShouldBeRoutedToOrderService() throws Exception {
        stubFor(WireMock.post(urlEqualTo("/api/order"))
                .willReturn(aResponse().withStatus(201).withBody("Order Placed Successfully")));

        mockMvc.perform(post("/api/order").contentType(MediaType.APPLICATION_JSON).content(ORDER_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().string("Order Placed Successfully"));

        verify(postRequestedFor(urlEqualTo("/api/order"))
                .withHeader("Content-Type", WireMock.containing("application/json"))
                .withRequestBody(equalToJson(ORDER_JSON)));
    }

    @Test
    void downstreamErrorStatusShouldBePassedThrough() throws Exception {
        stubFor(WireMock.post(urlEqualTo("/api/order"))
                .willReturn(aResponse().withStatus(500)));

        mockMvc.perform(post("/api/order").contentType(MediaType.APPLICATION_JSON).content(ORDER_JSON))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void inventoryRouteShouldForwardQueryParams() throws Exception {
        stubFor(WireMock.get(urlPathEqualTo("/api/inventory"))
                .withQueryParam("skuCode", equalTo("iphone_15"))
                .withQueryParam("quantity", equalTo("1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("true")));

        mockMvc.perform(get("/api/inventory").param("skuCode", "iphone_15").param("quantity", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void unknownPathShouldReturn404WithoutCallingServices() throws Exception {
        mockMvc.perform(get("/api/khong-co-route"))
                .andExpect(status().isNotFound());

        verify(0, anyRequestedFor(anyUrl()));
    }
}
