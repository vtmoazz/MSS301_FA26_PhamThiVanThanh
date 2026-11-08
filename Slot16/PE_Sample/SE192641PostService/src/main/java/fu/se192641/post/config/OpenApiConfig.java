package fu.se192641.post.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI postServiceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("SE192641 Post Service")
                        .version("1.0")
                        .description("MSS301 PE sample - đặt bài đăng tài trợ, gọi User Service qua OpenFeign (port 8082)"))
                .servers(List.of(new Server().url("http://localhost:8082").description("Post Service")));
    }
}
