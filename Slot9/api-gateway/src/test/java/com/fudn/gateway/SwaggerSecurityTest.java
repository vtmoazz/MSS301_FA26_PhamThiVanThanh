package com.fudn.gateway;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItems;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Swagger tai Gateway: UI va docs tong hop truy cap khong can JWT, API nghiep vu van can JWT.
 * WireMock dong vai 3 service tra ve /api-docs.
 */
@SpringBootTest
@AutoConfigureMockMvc
@EnableWireMock(@ConfigureWireMock(baseUrlProperties = {
        "services.product.url", "services.order.url", "services.inventory.url"}))
class SwaggerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void swaggerUiShouldBeAccessibleWithoutToken() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection());
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }

    @Test
    void swaggerConfigShouldListThreeServices() throws Exception {
        mockMvc.perform(get("/v3/api-docs/swagger-config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.urls[*].name")
                        .value(hasItems("Product Service", "Order Service", "Inventory Service")))
                .andExpect(jsonPath("$.urls[*].url").value(hasItems(
                        "/aggregate/product-service/v3/api-docs",
                        "/aggregate/order-service/v3/api-docs",
                        "/aggregate/inventory-service/v3/api-docs")));
    }

    @Test
    void aggregateProductDocsShouldBePermittedWithoutToken() throws Exception {
        stubFor(WireMock.get(urlEqualTo("/api-docs"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"info\":{\"title\":\"Product Service API\"}}")));

        mockMvc.perform(get("/aggregate/product-service/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Product Service API")));

        // Gateway rewrite path thanh /api-docs truoc khi forward
        verify(getRequestedFor(urlEqualTo("/api-docs")));
    }

    @Test
    void aggregateOrderAndInventoryDocsShouldBeRouted() throws Exception {
        stubFor(WireMock.get(urlEqualTo("/api-docs"))
                .willReturn(aResponse().withStatus(200).withBody("{}")));

        mockMvc.perform(get("/aggregate/order-service/v3/api-docs"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/aggregate/inventory-service/v3/api-docs"))
                .andExpect(status().isOk());

        verify(2, getRequestedFor(urlEqualTo("/api-docs")));
    }

    @Test
    void protectedApiShouldRequireToken() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void corsPreflightShouldBeAllowed() throws Exception {
        mockMvc.perform(options("/api/products")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"));
    }
}
