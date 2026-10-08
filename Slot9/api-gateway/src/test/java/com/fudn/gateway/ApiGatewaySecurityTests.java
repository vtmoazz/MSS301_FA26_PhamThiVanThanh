package com.fudn.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

import static com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test bao mat Gateway: request khong co JWT bi chan (401) truoc khi toi service dich.
 */
@SpringBootTest
@AutoConfigureMockMvc
@EnableWireMock(@ConfigureWireMock(baseUrlProperties = {
        "services.product.url", "services.order.url", "services.inventory.url"}))
class ApiGatewaySecurityTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void healthEndpointIsPublic() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    void getProductsWithoutTokenShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().exists("WWW-Authenticate"));

        verify(0, anyRequestedFor(anyUrl()));
    }

    @Test
    void postOrderWithoutTokenShouldReturn401() throws Exception {
        mockMvc.perform(post("/api/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"skuCode\":\"iphone_15\",\"price\":1000,\"quantity\":1}"))
                .andExpect(status().isUnauthorized());

        verify(0, anyRequestedFor(anyUrl()));
    }
}
