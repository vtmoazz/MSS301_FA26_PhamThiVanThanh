package fu.se192641.user.service;

import fu.se192641.user.dto.PageDTO;
import fu.se192641.user.dto.UserDTO;

public interface UserService {

    UserDTO create(UserDTO request);

    UserDTO update(Long userId, UserDTO request);

    UserDTO getById(Long userId);

    void deactivate(Long userId);

    PageDTO<UserDTO> list(int page, int size, String role, String status);
}
