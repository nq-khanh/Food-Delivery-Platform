package com.hkt.fooddelivery.security;

import com.hkt.fooddelivery.dto.ApiError;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Instant;

/**
 * Khi user đã đăng nhập (có JWT hợp lệ) nhưng lại gọi API (API này yêu cầu quyền ADMIN) trong khi user đó chỉ có quyền USER
 * -> Spring Security gọi CustomAccessDeniedHandler (-> AccessDeniedHandler) -> Trả 403 Forbidden + body JSON chuẩn ApiError
 */
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private static final Logger log = LoggerFactory.getLogger(CustomAccessDeniedHandler.class);

    private final ObjectMapper objectMapper;

    public CustomAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        log.debug("Access denied: {} - {}", request.getRequestURI(), accessDeniedException.getMessage());

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ApiError body = new ApiError(
                "Forbidden",
                "Bạn không có quyền truy cập tài nguyên này",
                null,
                request.getRequestURI(),
                Instant.now()
        );

        objectMapper.writeValue(response.getOutputStream(), body);
    }

}
