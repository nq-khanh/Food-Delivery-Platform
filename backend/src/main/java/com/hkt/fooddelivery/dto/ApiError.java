package com.hkt.fooddelivery.dto;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

//Format lỗi thống nhất
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
    String error,
    String message,
    List<FieldErrorDetail> details,
    String path,
    Instant timestamp
) {
    public record FieldErrorDetail(String field, String message) {}
}
