package fu.se192641.user.common;

import fu.se192641.user.dto.ApiResponse;
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

import java.util.stream.Collectors;

/** Chuyển mọi lỗi về đúng cặp HTTP code / status của đề, luôn trả JSON ApiResponse. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse> handleApi(ApiException ex) {
        return ResponseEntity.status(ex.getHttpStatus()).body(ApiResponse.error(ex.getStatus(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleInvalidBody(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getDefaultMessage())
                .sorted()
                .collect(Collectors.joining("; "));
        return validation(message);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiResponse> handleUnreadable(Exception ex) {
        return validation("Request data has an invalid format");
    }

    @ExceptionHandler({ConstraintViolationException.class, HandlerMethodValidationException.class})
    public ResponseEntity<ApiResponse> handleParamValidation(Exception ex) {
        return validation(ex.getMessage());
    }

    /** Lưới an toàn khi hai request trùng username cùng lúc vượt qua bước kiểm tra trước. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse> handleIntegrity(DataIntegrityViolationException ex) {
        String detail = String.valueOf(ex.getMostSpecificCause().getMessage());
        if (detail.contains("UQ_Users_Username")) {
            return ResponseEntity.status(HttpStatus.IM_USED).body(ApiResponse.error(3, "Username already exists"));
        }
        log.error("Data integrity error", ex);
        return validation("Data violates a database constraint");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleOther(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(0, "Internal server error"));
    }

    private ResponseEntity<ApiResponse> validation(String message) {
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(ApiResponse.error(2, message));
    }
}
