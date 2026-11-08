package fu.se192641.post.client;

import fu.se192641.post.dto.ApiResponse;
import fu.se192641.post.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Gọi User Service. Khi user không tồn tại, User Service trả HTTP 404
 * và Feign ném FeignException.NotFound.
 */
@FeignClient(name = "user-service", url = "${user.service.url}")
public interface UserClient {

    @GetMapping("/api/users/{userId}")
    ApiResponse<UserDTO> getUser(@PathVariable("userId") Long userId);
}
