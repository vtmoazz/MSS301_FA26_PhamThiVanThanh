package fu.se192641.user.service.impl;

import fu.se192641.user.common.ApiException;
import fu.se192641.user.dto.PageDTO;
import fu.se192641.user.dto.UserDTO;
import fu.se192641.user.entity.Role;
import fu.se192641.user.entity.User;
import fu.se192641.user.entity.UserStatus;
import fu.se192641.user.repository.UserRepository;
import fu.se192641.user.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    public static final int MAX_PAGE_SIZE = 100;

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserDTO create(UserDTO request) {
        String username = request.getUsername().trim();
        if (userRepository.existsByUsername(username)) {
            throw ApiException.duplicate("Username '" + username + "' already exists");
        }
        User user = new User();
        user.setUsername(username);
        user.setEmail(request.getEmail().trim());
        user.setRole(Role.valueOf(request.getRole()));
        user.setRatePerPost(request.getRatePerPost());
        user.setMaxDailyPosts(request.getMaxDailyPosts());
        user.setLevel(request.getLevel());
        user.setStatus(request.getStatus() == null ? UserStatus.ACTIVE : UserStatus.valueOf(request.getStatus()));
        return toDto(userRepository.save(user));
    }

    /** Cập nhật từng phần: trường nào null thì giữ nguyên giá trị cũ. */
    @Override
    @Transactional
    public UserDTO update(Long userId, UserDTO request) {
        User user = find(userId);
        if (request.getUsername() != null) {
            String username = request.getUsername().trim();
            if (username.isEmpty()) {
                throw ApiException.validation("username must not be blank");
            }
            if (userRepository.existsByUsernameAndUserIdNot(username, userId)) {
                throw ApiException.duplicate("Username '" + username + "' already exists");
            }
            user.setUsername(username);
        }
        if (request.getEmail() != null) {
            if (request.getEmail().isBlank()) {
                throw ApiException.validation("email must not be blank");
            }
            user.setEmail(request.getEmail().trim());
        }
        if (request.getRole() != null) {
            user.setRole(Role.valueOf(request.getRole()));
        }
        if (request.getRatePerPost() != null) {
            user.setRatePerPost(request.getRatePerPost());
        }
        if (request.getMaxDailyPosts() != null) {
            user.setMaxDailyPosts(request.getMaxDailyPosts());
        }
        if (request.getLevel() != null) {
            user.setLevel(request.getLevel());
        }
        if (request.getStatus() != null) {
            user.setStatus(UserStatus.valueOf(request.getStatus()));
        }
        return toDto(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getById(Long userId) {
        return toDto(find(userId));
    }

    /** "Xoá" mềm: chỉ chuyển trạng thái sang DEACTIVATED để giữ lịch sử chiến dịch. */
    @Override
    @Transactional
    public void deactivate(Long userId) {
        User user = find(userId);
        user.setStatus(UserStatus.DEACTIVATED);
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public PageDTO<UserDTO> list(int page, int size, String role, String status) {
        if (page < 0) {
            throw ApiException.validation("page must be >= 0");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw ApiException.validation("size must be between 1 and " + MAX_PAGE_SIZE);
        }
        Role roleFilter = parse(Role.class, role, "role");
        UserStatus statusFilter = parse(UserStatus.class, status, "status");
        Page<User> result = userRepository.search(roleFilter, statusFilter,
                PageRequest.of(page, size, Sort.by("userId").ascending()));
        List<UserDTO> content = result.getContent().stream().map(this::toDto).toList();
        return PageDTO.of(result, content);
    }

    private User find(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> ApiException.notFound("User " + userId + " not found"));
    }

    private static <E extends Enum<E>> E parse(Class<E> type, String value, String field) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Enum.valueOf(type, value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw ApiException.validation(field + " '" + value + "' is not valid");
        }
    }

    private UserDTO toDto(User user) {
        UserDTO dto = new UserDTO();
        dto.setUserId(user.getUserId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole().name());
        dto.setRatePerPost(user.getRatePerPost());
        dto.setMaxDailyPosts(user.getMaxDailyPosts());
        dto.setLevel(user.getLevel());
        dto.setStatus(user.getStatus().name());
        return dto;
    }
}
