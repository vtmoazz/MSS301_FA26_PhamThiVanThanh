package fu.se192641.user.common;

import org.springframework.http.HttpStatus;

/** Lỗi nghiệp vụ mang sẵn mã status của đề và HTTP code tương ứng. */
public class ApiException extends RuntimeException {

    private final int status;
    private final HttpStatus httpStatus;

    public ApiException(int status, HttpStatus httpStatus, String message) {
        super(message);
        this.status = status;
        this.httpStatus = httpStatus;
    }

    public static ApiException validation(String message) {
        return new ApiException(2, HttpStatus.NOT_ACCEPTABLE, message);
    }

    public static ApiException duplicate(String message) {
        return new ApiException(3, HttpStatus.IM_USED, message);
    }

    public static ApiException notFound(String message) {
        return new ApiException(4, HttpStatus.NOT_FOUND, message);
    }

    public int getStatus() { return status; }

    public HttpStatus getHttpStatus() { return httpStatus; }
}
