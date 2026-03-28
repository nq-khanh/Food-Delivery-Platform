package com.hkt.fooddelivery.service;

import com.hkt.fooddelivery.dto.EmbeddingRequest;
import com.hkt.fooddelivery.dto.EmbeddingResponse;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

@Service
public class EmbeddingService {

    private final WebClient webClient;

    public EmbeddingService(WebClient.Builder webClientBuilder,
            @Value("${embedding.api.url:http://localhost:8000}") String apiUrl) {
        this.webClient = webClientBuilder.baseUrl(apiUrl).build();
    }

    public List<Double> getVector(String text) {
        return webClient.post()
                .uri("/embedding")
                .bodyValue(new EmbeddingRequest(text))
                .retrieve()
                // Xử lý lỗi nếu API Python sập
                .onStatus(HttpStatusCode::isError, response -> Mono.error(new RuntimeException("Embedding API Error")))
                .bodyToMono(EmbeddingResponse.class)
                .map(EmbeddingResponse::embedding)
                .block(); // .block() để lấy dữ liệu đồng bộ (nếu cần)
    }
}