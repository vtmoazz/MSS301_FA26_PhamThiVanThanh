package fu.se192641.post.common;

import feign.FeignException;
import fu.se192641.post.dto.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

/** Chuyển mọi lỗi về đúng cặp HTTP code / status của đề, luôn trả JSON ApiResponse. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleApi(ApiException ex) {
        return ResponseEntity.status(ex.getHttpStatus()).body(ApiResponse.error(ex.getStatus(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidBody(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getDefaultMessage())
                .sorted()
                .collect(Collectors.joining("; "));
        return validation(message);
    }

    /** Body sai JSON, ngày không đúng dd/MM/yyyy, hoặc tham số sai kiểu. */
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiResponse<Void>> handleUnreadable(Exception ex) {
        return validation("Request data has an invalid format (dates must be dd/MM/yyyy)");
    }

    @ExceptionHandler({ConstraintViolationException.class, HandlerMethodValidationException.class})
    public ResponseEntity<ApiResponse<Void>> handleParamValidation(Exception ex) {
        return validation(ex.getMessage());
    }

    /** Lưới an toàn khi dữ liệu lọt qua kiểm tra nhưng vi phạm CHECK/FK trong database. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleIntegrity(DataIntegrityViolationException ex) {
        String detail = String.valueOf(ex.getMostSpecificCause().getMessage());
        if (detail.contains("FK_Posts_User")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(4, "User not found"));
        }
        log.error("Data integrity error", ex);
        return validation("Data violates a database constraint");
    }

    /** User Service lỗi hoặc không kết nối được (NotFound đã được service xử lý thành 404). */
    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ApiResponse<Void>> handleFeign(FeignException ex) {
        log.error("User Service call failed", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(0, "Cannot get user data from User Service"));
    }

    /** Đường dẫn không tồn tại: trả 404 thay vì rơi xuống lỗi 500 chung. */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResource(NoResourceFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(4, "Resource not found: /" + ex.getResourcePath()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleOther(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(0, "Internal server error"));
    }

    private ResponseEntity<ApiResponse<Void>> validation(String message) {
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(ApiResponse.error(2, message));
    }
}
