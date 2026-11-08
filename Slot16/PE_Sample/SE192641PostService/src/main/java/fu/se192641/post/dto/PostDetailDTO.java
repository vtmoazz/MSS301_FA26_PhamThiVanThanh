package fu.se192641.post.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import fu.se192641.post.common.DateUtils;

import java.math.BigDecimal;
import java.util.Date;

/** Giống PostDTO nhưng thay userId bằng object user lấy từ User Service. */
public class PostDetailDTO {

    private long postId;
    private String advertiserName;
    private String advertiserEmail;
    private String advertiserPhone;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateUtils.PATTERN, timezone = DateUtils.ZONE)
    private Date startDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateUtils.PATTERN, timezone = DateUtils.ZONE)
    private Date endDate;

    private int numberOfPosts;
    private BigDecimal totalCost;
    private String status;
    private UserDTO user;

    public PostDetailDTO() {
    }

    public long getPostId() { return postId; }
    public void setPostId(long postId) { this.postId = postId; }

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

    public int getNumberOfPosts() { return numberOfPosts; }
    public void setNumberOfPosts(int numberOfPosts) { this.numberOfPosts = numberOfPosts; }

    public BigDecimal getTotalCost() { return totalCost; }
    public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public UserDTO getUser() { return user; }
    public void setUser(UserDTO user) { this.user = user; }
}
