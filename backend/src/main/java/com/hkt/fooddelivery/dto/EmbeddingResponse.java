package com.hkt.fooddelivery.dto;

import java.util.List;

public record EmbeddingResponse(
        String text,
        List<Double> embedding
) {}
