package fu.se192641.user.controller;

import fu.se192641.user.common.OnCreate;
import fu.se192641.user.dto.ApiResponse;
import fu.se192641.user.dto.UserDTO;
import fu.se192641.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.groups.Default;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/users", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Users", description = "Quản lý creator / người dùng")
public class SE192641UserController {

    private final UserService userService;

    public SE192641UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Create user (default status ACTIVE)")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> create(@Validated({Default.class, OnCreate.class}) @RequestBody UserDTO request) {
        UserDTO created = userService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Created successfully", created));
    }

    @Operation(summary = "Update user (partial update)")
    @PutMapping(value = "/{userId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse> update(@PathVariable Long userId, @Validated @RequestBody UserDTO request) {
        return ResponseEntity.ok(ApiResponse.ok("Updated successfully", userService.update(userId, request)));
    }

    @Operation(summary = "Get user detail")
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse> getById(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.ok("Successful", userService.getById(userId)));
    }

    @Operation(summary = "Deactivate user (status = DEACTIVATED)")
    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse> deactivate(@PathVariable Long userId) {
        userService.deactivate(userId);
        return ResponseEntity.ok(ApiResponse.ok("Deactivated successfully", null));
    }

    @Operation(summary = "List users with paging and optional role/status filter")
    @GetMapping
    public ResponseEntity<ApiResponse> list(@RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size,
                                            @RequestParam(required = false) String role,
                                            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(ApiResponse.ok("Successful", userService.list(page, size, role, status)));
    }
}
