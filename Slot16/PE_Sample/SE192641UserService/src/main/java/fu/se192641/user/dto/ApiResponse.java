package fu.se192641.user.dto;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Khung phản hồi chung. status: 1 OK, 2 validation, 3 trùng username, 4 không tìm thấy, 0 lỗi server.
 * timestamp theo ISO 8601 dạng YYYY-MM-DDThh:mm:ssZ.
 */
public class ApiResponse {

    private int status;
    private String message;
    private Object data;
    private String timestamp;

    public ApiResponse() {
    }

    public ApiResponse(int status, String message, Object data) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.timestamp = Instant.now().truncatedTo(ChronoUnit.SECONDS).toString();
    }

    public static ApiResponse ok(String message, Object data) {
        return new ApiResponse(1, message, data);
    }

    public static ApiResponse error(int status, String message) {
        return new ApiResponse(status, message, null);
    }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
