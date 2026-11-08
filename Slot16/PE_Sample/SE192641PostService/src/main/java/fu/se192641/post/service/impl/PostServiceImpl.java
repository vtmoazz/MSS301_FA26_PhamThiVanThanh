package fu.se192641.post.service.impl;

import feign.FeignException;
import fu.se192641.post.client.UserClient;
import fu.se192641.post.common.ApiException;
import fu.se192641.post.common.DateUtils;
import fu.se192641.post.dto.PageDTO;
import fu.se192641.post.dto.PostDTO;
import fu.se192641.post.dto.PostDetailDTO;
import fu.se192641.post.dto.UserDTO;
import fu.se192641.post.entity.Post;
import fu.se192641.post.entity.PostStatus;
import fu.se192641.post.repository.PostRepository;
import fu.se192641.post.service.PostService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PostServiceImpl implements PostService {

    private static final Logger log = LoggerFactory.getLogger(PostServiceImpl.class);
    private static final int MAX_PAGE_SIZE = 100;

    private final PostRepository postRepository;
    private final UserClient userClient;

    public PostServiceImpl(PostRepository postRepository, UserClient userClient) {
        this.postRepository = postRepository;
        this.userClient = userClient;
    }

    @Override
    @Transactional
    public PostDTO create(PostDTO dto) {
        checkDates(dto.getStartDate(), dto.getEndDate());
        UserDTO user = requireActiveUser(dto.getUserId());
        checkNumberOfPosts(dto.getNumberOfPosts(), user);

        Post post = new Post();
        post.setAdvertiserName(dto.getAdvertiserName());
        post.setAdvertiserEmail(dto.getAdvertiserEmail());
        post.setAdvertiserPhone(dto.getAdvertiserPhone());
        post.setStartDate(dto.getStartDate());
        post.setEndDate(dto.getEndDate());
        post.setNumberOfPosts(dto.getNumberOfPosts());
        post.setStatus(dto.getStatus() == null ? PostStatus.SCHEDULED : PostStatus.valueOf(dto.getStatus()));
        post.setUserId(user.getUserId());
        post.setTotalCost(totalCost(user, post.getStartDate(), post.getEndDate()));
        return toDTO(postRepository.save(post));
    }

    @Override
    @Transactional
    public PostDTO update(Long postId, PostDTO dto) {
        Post post = findPost(postId);

        if (dto.getAdvertiserName() != null) post.setAdvertiserName(dto.getAdvertiserName());
        if (dto.getAdvertiserEmail() != null) post.setAdvertiserEmail(dto.getAdvertiserEmail());
        if (dto.getAdvertiserPhone() != null) post.setAdvertiserPhone(dto.getAdvertiserPhone());
        if (dto.getStartDate() != null) post.setStartDate(dto.getStartDate());
        if (dto.getEndDate() != null) post.setEndDate(dto.getEndDate());
        if (dto.getStatus() != null) post.setStatus(PostStatus.valueOf(dto.getStatus()));
        checkDates(post.getStartDate(), post.getEndDate());

        // Đổi sang user khác thì user mới phải tồn tại và đang ACTIVE, như lúc tạo.
        boolean userChanged = dto.getUserId() != null && !dto.getUserId().equals(post.getUserId());
        UserDTO user = userChanged ? requireActiveUser(dto.getUserId()) : requireUser(post.getUserId());
        post.setUserId(user.getUserId());

        if (dto.getNumberOfPosts() != null) post.setNumberOfPosts(dto.getNumberOfPosts());
        if (dto.getNumberOfPosts() != null || userChanged) {
            checkNumberOfPosts(post.getNumberOfPosts(), user);
        }

        // Luôn tính lại theo giá hiện tại của user.
        post.setTotalCost(totalCost(user, post.getStartDate(), post.getEndDate()));
        return toDTO(postRepository.save(post));
    }

    @Override
    @Transactional(readOnly = true)
    public PostDetailDTO getById(Long postId) {
        Post post = findPost(postId);
        return toDetail(post, lookupUser(post.getUserId()));
    }

    @Override
    @Transactional
    public void cancel(Long postId) {
        Post post = findPost(postId);
        post.setStatus(PostStatus.CANCELLED);
        postRepository.save(post);
    }

    @Override
    @Transactional(readOnly = true)
    public PageDTO<PostDetailDTO> list(int page, int size, String advertiserName, String status) {
        if (page < 0) {
            throw ApiException.validation("page must be >= 0");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw ApiException.validation("size must be between 1 and " + MAX_PAGE_SIZE);
        }
        PostStatus statusFilter = parseStatus(status);
        String nameFilter = advertiserName == null || advertiserName.isBlank() ? null : advertiserName.trim();

        Page<Post> result = postRepository.search(nameFilter, statusFilter,
                PageRequest.of(page, size, Sort.by("postId").ascending()));

        // Mỗi user chỉ gọi User Service một lần trong một trang.
        Map<Long, UserDTO> users = new HashMap<>();
        List<PostDetailDTO> content = result.getContent().stream()
                .map(p -> toDetail(p, users.computeIfAbsent(p.getUserId(), this::lookupUser)))
                .toList();
        return PageDTO.of(result, content);
    }

    private Post findPost(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> ApiException.notFound("Post not found with id " + postId));
    }

    private void checkDates(Date start, Date end) {
        if (!DateUtils.toLocalDate(end).isAfter(DateUtils.toLocalDate(start))) {
            throw ApiException.validation("endDate must be after startDate");
        }
    }

    private void checkNumberOfPosts(int numberOfPosts, UserDTO user) {
        if (numberOfPosts > user.getMaxDailyPosts()) {
            throw ApiException.validation("numberOfPosts must not exceed the user's maxDailyPosts ("
                    + user.getMaxDailyPosts() + ")");
        }
    }

    private BigDecimal totalCost(UserDTO user, Date start, Date end) {
        return user.getRatePerPost().multiply(BigDecimal.valueOf(DateUtils.daysBetween(start, end)));
    }

    private UserDTO requireActiveUser(Long userId) {
        UserDTO user = requireUser(userId);
        if (!"ACTIVE".equals(user.getStatus())) {
            throw ApiException.userNotActive("User " + userId + " is not ACTIVE (status: " + user.getStatus() + ")");
        }
        return user;
    }

    private UserDTO requireUser(Long userId) {
        return fetchUser(userId)
                .orElseThrow(() -> ApiException.notFound("User not found with id " + userId));
    }

    /** Khi xem post: user không còn tồn tại thì trả user = null thay vì làm hỏng cả phản hồi. */
    private UserDTO lookupUser(Long userId) {
        Optional<UserDTO> user = fetchUser(userId);
        if (user.isEmpty()) {
            log.warn("User {} referenced by a post was not found in User Service", userId);
        }
        return user.orElse(null);
    }

    private Optional<UserDTO> fetchUser(Long userId) {
        try {
            return Optional.ofNullable(userClient.getUser(userId).getData());
        } catch (FeignException.NotFound ex) {
            return Optional.empty();
        }
    }

    private PostStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }
        try {
            return PostStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw ApiException.validation("status must be DRAFT, SCHEDULED, PUBLISHED or CANCELLED");
        }
    }

    private PostDTO toDTO(Post post) {
        PostDTO dto = new PostDTO();
        dto.setPostId(post.getPostId());
        dto.setAdvertiserName(post.getAdvertiserName());
        dto.setAdvertiserEmail(post.getAdvertiserEmail());
        dto.setAdvertiserPhone(post.getAdvertiserPhone());
        dto.setStartDate(post.getStartDate());
        dto.setEndDate(post.getEndDate());
        dto.setNumberOfPosts(post.getNumberOfPosts());
        dto.setTotalCost(post.getTotalCost());
        dto.setStatus(post.getStatus().name());
        dto.setUserId(post.getUserId());
        return dto;
    }

    private PostDetailDTO toDetail(Post post, UserDTO user) {
        PostDetailDTO dto = new PostDetailDTO();
        dto.setPostId(post.getPostId());
        dto.setAdvertiserName(post.getAdvertiserName());
        dto.setAdvertiserEmail(post.getAdvertiserEmail());
        dto.setAdvertiserPhone(post.getAdvertiserPhone());
        dto.setStartDate(post.getStartDate());
        dto.setEndDate(post.getEndDate());
        dto.setNumberOfPosts(post.getNumberOfPosts());
        dto.setTotalCost(post.getTotalCost());
        dto.setStatus(post.getStatus().name());
        dto.setUser(user);
        return dto;
    }
}
