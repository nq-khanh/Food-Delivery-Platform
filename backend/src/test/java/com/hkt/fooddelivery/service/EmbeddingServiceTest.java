package com.hkt.fooddelivery.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hkt.fooddelivery.dto.EmbeddingResponse;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EmbeddingServiceTest {

    private static MockWebServer mockWebServer;
    private EmbeddingService embeddingService;
    private ObjectMapper objectMapper;

    @BeforeAll
    static void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @BeforeEach
void initialize() {
    // MockWebServer sẽ chạy trên một port ngẫu nhiên tại localhost
    String mockUrl = String.format("http://localhost:%s", mockWebServer.getPort());
    
    WebClient.Builder webClientBuilder = WebClient.builder();
    
    // TRUYỀN mockUrl vào constructor của service thay vì để nó dùng default
    embeddingService = new EmbeddingService(webClientBuilder, mockUrl);
    
    objectMapper = new ObjectMapper();
}

    @Test
    @DisplayName("Nên trả về danh sách vector khi API Python phản hồi thành công")
    void shouldReturnVectorWhenApiIsOk() throws Exception {
        // Giả lập dữ liệu trả về từ Python
        List<Double> mockVector = List.of(0.1, 0.2, 0.3);
        EmbeddingResponse mockResponse = new EmbeddingResponse("Phở bò", mockVector);

        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(mockResponse))
                .addHeader("Content-Type", "application/json"));

        // Gọi service
        List<Double> result = embeddingService.getVector("Phở bò");

        // Kiểm tra
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(0.1, result.get(0));
    }

    @Test
    @DisplayName("Nên ném ra ngoại lệ khi API Python báo lỗi 500")
    void shouldThrowExceptionWhenApiFails() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        assertThrows(RuntimeException.class, () -> {
            embeddingService.getVector("Lỗi rồi");
        }, "Embedding API Error");
    }
}