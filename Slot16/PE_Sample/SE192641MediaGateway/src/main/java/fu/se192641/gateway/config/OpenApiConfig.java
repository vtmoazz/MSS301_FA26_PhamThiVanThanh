package fu.se192641.gateway.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI gatewayOpenApi() {
        return new OpenAPI().info(new Info()
                .title("SE192641 Media Gateway")
                .version("1.0")
                .description("MSS301 PE sample - cổng vào duy nhất (port 8080): /api/users/** → 8081, /api/posts/** → 8082"));
    }
}
