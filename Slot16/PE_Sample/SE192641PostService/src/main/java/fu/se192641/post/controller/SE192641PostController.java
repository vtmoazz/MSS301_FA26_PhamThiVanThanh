package fu.se192641.post.controller;

import fu.se192641.post.common.OnCreate;
import fu.se192641.post.dto.ApiResponse;
import fu.se192641.post.dto.PageDTO;
import fu.se192641.post.dto.PostDTO;
import fu.se192641.post.dto.PostDetailDTO;
import fu.se192641.post.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.groups.Default;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/posts", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Posts", description = "Đặt chiến dịch bài đăng tài trợ")
public class SE192641PostController {

    private final PostService postService;

    public SE192641PostController(PostService postService) {
        this.postService = postService;
    }

    @Operation(summary = "Create post (user must exist and be ACTIVE, default status SCHEDULED)")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<PostDTO>> create(
            @Validated({Default.class, OnCreate.class}) @RequestBody PostDTO request) {
        PostDTO created = postService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Created successfully", created));
    }

    @Operation(summary = "Update post (partial update, totalCost recalculated with the user's current rate)")
    @PutMapping(value = "/{postId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<PostDTO>> update(@PathVariable Long postId,
                                                       @Validated @RequestBody PostDTO request) {
        return ResponseEntity.ok(ApiResponse.ok("Updated successfully", postService.update(postId, request)));
    }

    @Operation(summary = "Get post detail (includes user from User Service)")
    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostDetailDTO>> getById(@PathVariable Long postId) {
        return ResponseEntity.ok(ApiResponse.ok("Successful", postService.getById(postId)));
    }

    @Operation(summary = "Cancel post (status = CANCELLED)")
    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResponse<Void>> cancel(@PathVariable Long postId) {
        postService.cancel(postId);
        return ResponseEntity.ok(ApiResponse.ok("Cancelled successfully", null));
    }

    @Operation(summary = "List posts with paging and optional advertiserName/status filter")
    @GetMapping
    public ResponseEntity<ApiResponse<PageDTO<PostDetailDTO>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String advertiserName,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(ApiResponse.ok("Successful", postService.list(page, size, advertiserName, status)));
    }
}
