package fu.se192641.post.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import fu.se192641.post.common.DateUtils;
import fu.se192641.post.common.OnCreate;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Dùng chung cho tạo mới và cập nhật từng phần.
 * Ràng buộc định dạng luôn áp dụng khi trường có giá trị; ràng buộc bắt buộc chỉ áp dụng khi tạo mới.
 * Các luật cần dữ liệu khác (endDate sau startDate, numberOfPosts ≤ maxDailyPosts) kiểm tra ở service.
 */
public class PostDTO {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long postId;

    @NotBlank(groups = OnCreate.class, message = "advertiserName is required")
    @Size(max = 100, message = "advertiserName must be at most 100 characters")
    private String advertiserName;

    @NotBlank(groups = OnCreate.class, message = "advertiserEmail is required")
    @Size(max = 100, message = "advertiserEmail must be at most 100 characters")
    @Email(regexp = "^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$", message = "advertiserEmail is not valid")
    private String advertiserEmail;

    @NotBlank(groups = OnCreate.class, message = "advertiserPhone is required")
    @Size(max = 20, message = "advertiserPhone must be at most 20 characters")
    private String advertiserPhone;

    @NotNull(groups = OnCreate.class, message = "startDate is required")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateUtils.PATTERN, timezone = DateUtils.ZONE)
    private Date startDate;

    @NotNull(groups = OnCreate.class, message = "endDate is required")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateUtils.PATTERN, timezone = DateUtils.ZONE)
    private Date endDate;

    @NotNull(groups = OnCreate.class, message = "numberOfPosts is required")
    @Min(value = 1, message = "numberOfPosts must be >= 1")
    private Integer numberOfPosts;

    /** Luôn do server tính, bỏ qua giá trị client gửi lên. */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private BigDecimal totalCost;

    @Pattern(regexp = "DRAFT|SCHEDULED|PUBLISHED|CANCELLED",
            message = "status must be DRAFT, SCHEDULED, PUBLISHED or CANCELLED")
    private String status;

    @NotNull(groups = OnCreate.class, message = "userId is required")
    @Positive(message = "userId must be positive")
    private Long userId;

    public PostDTO() {
    }

    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }

    public String getAdvertiserName() { return advertiserName; }
    public void setAdvertiserName(String advertiserName) { this.advertiserName = advertiserName; }

    public String getAdvertiserEmail() { return advertiserEmail; }
    public void setAdvertiserEmail(String advertiserEmail) { this.advertiserEmail = advertiserEmail; }

    public String getAdvertiserPhone() { return advertiserPhone; }
    public void setAdvertiserPhone(String advertiserPhone) { this.advertiserPhone = advertiserPhone; }

    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }

    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }

    public Integer getNumberOfPosts() { return numberOfPosts; }
    public void setNumberOfPosts(Integer numberOfPosts) { this.numberOfPosts = numberOfPosts; }

    public BigDecimal getTotalCost() { return totalCost; }
    public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}
