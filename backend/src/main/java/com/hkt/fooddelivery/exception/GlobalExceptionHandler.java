package com.hkt.fooddelivery.exception;

import com.hkt.fooddelivery.dto.ApiError;
import com.hkt.fooddelivery.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Global Exception Handler - Format thống nhất (error, message, details, path, timestamps)
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException ex, HttpServletRequest request) {
        ApiError body = new ApiError(
                ex.getErrorCode(),
                ex.getMessage(),
                null,
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ApiResponse.error(body));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ApiError.FieldErrorDetail> details = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toFieldError)
                .collect(Collectors.toList());

        ApiError body = new ApiError(
                "Bad Request",
                "Validation failed",
                details,
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(body));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {

        ApiError body = new ApiError(
                "NotFound",
                ex.getMessage(),
                null,
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(body));
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<Void>> handleConflict(DuplicateResourceException ex, HttpServletRequest request) {

        ApiError body = new ApiError(
                "Conflict",
                ex.getMessage(),
                null,
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(body));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {

        ApiError body = new ApiError(
                "Unauthorized",
                "Username/Email hoặc mật khẩu không đúng",
                null,
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(body));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthentication(AuthenticationException ex, HttpServletRequest request) {

        ApiError body = new ApiError(
                "Unauthorized",
                ex.getMessage(),
                null,
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(body));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {

        ApiError body = new ApiError(
                "Forbidden",
                "Bạn không có quyền truy cập tài nguyên này",
                null,
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(body));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(Exception ex, HttpServletRequest request) {
        log.error("Unhandler error: ", ex);
        ApiError body = new ApiError(
                "Internal Server Error",
                "Đã xảy ra lỗi. Vui lòng thử lại sau.",
                null,
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(body));
    }

    private ApiError.FieldErrorDetail toFieldError (FieldError fe) {
        return new ApiError.FieldErrorDetail(
                fe.getField(),
                fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "Invalid"
        );
    }
}


