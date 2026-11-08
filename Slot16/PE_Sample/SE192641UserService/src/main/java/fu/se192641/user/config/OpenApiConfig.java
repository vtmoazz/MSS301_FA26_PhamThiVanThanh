package fu.se192641.user.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI userServiceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("SE192641 User Service")
                        .version("1.0")
                        .description("MSS301 PE sample - quản lý creator (port 8081)"))
                .servers(List.of(new Server().url("http://localhost:8081").description("User Service")));
    }
}
