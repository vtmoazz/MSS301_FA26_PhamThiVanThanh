package fu.se192641.post.dto;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Khung phản hồi chung, dùng cả cho controller lẫn khi đọc phản hồi của User Service qua Feign.
 * status: 1 OK, 2 validation, 4 không tìm thấy, 5 user không ACTIVE, 0 lỗi server.
 * timestamp theo ISO 8601 dạng YYYY-MM-DDThh:mm:ssZ.
 */
public class ApiResponse<T> {

    private int status;
    private String message;
    private T data;
    private String timestamp;

    public ApiResponse() {
    }

    public ApiResponse(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.timestamp = Instant.now().truncatedTo(ChronoUnit.SECONDS).toString();
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(1, message, data);
    }

    public static ApiResponse<Void> error(int status, String message) {
        return new ApiResponse<>(status, message, null);
    }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
