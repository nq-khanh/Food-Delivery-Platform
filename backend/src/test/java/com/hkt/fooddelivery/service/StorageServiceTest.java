package com.hkt.fooddelivery.service;

import com.hkt.fooddelivery.config.MinioProperties;
import com.hkt.fooddelivery.exception.FileSizeExceededException;
import com.hkt.fooddelivery.exception.InvalidFileTypeException;
import com.hkt.fooddelivery.exception.StorageDeleteException;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
        import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StorageServiceTest {

    @Mock
    private MinioClient minioClient;

    @Mock
    private MinioProperties properties;

    @InjectMocks
    private StorageService storageService;

    @BeforeEach
    void setUp() {
        lenient().when(properties.getBucketName()).thenReturn("food-bucket");
        lenient().when(properties.getEndpoint()).thenReturn("http://localhost:9000");
        lenient().when(properties.getMaxFileSizeBytes()).thenReturn(5 * 1024 * 1024L); // 5MB
        lenient().when(properties.getAllowedContentTypes()).thenReturn(List.of("image/jpeg", "image/png"));
    }

    @Test
    @DisplayName("Upload thành công - Trả về URL đúng định dạng")
    void upload_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", "content".getBytes());

        String url = storageService.upload(file, "avatars");

        assertNotNull(url);
        assertTrue(url.contains("http://localhost:9000/food-bucket/avatars/"));
        assertTrue(url.endsWith("_test.jpg"));
        verify(minioClient, times(1)).putObject(any(PutObjectArgs.class));
    }

    @Test
    @DisplayName("Upload thất bại - File sai định dạng (vô lý)")
    void upload_InvalidFileType() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.txt", "text/plain", "content".getBytes());

        assertThrows(InvalidFileTypeException.class, () -> storageService.upload(file, "products"));
        verifyNoInteractions(minioClient);
    }

    @Test
    @DisplayName("Upload thất bại - File quá lớn")
    void upload_FileTooLarge() {
        byte[] largeContent = new byte[6 * 1024 * 1024]; // 6MB
        MockMultipartFile file = new MockMultipartFile(
                "file", "large.png", "image/png", largeContent);

        assertThrows(FileSizeExceededException.class, () -> storageService.upload(file, "products"));
    }

    @Test
    @DisplayName("Xóa thành công - URL hợp lệ")
    void deleteByUrl_Success() throws Exception {
        String url = "http://localhost:9000/food-bucket/products/2026/01/01/uuid_p.jpg";

        storageService.deleteByUrl(url);

        verify(minioClient, times(1)).removeObject(any(RemoveObjectArgs.class));
    }

    @Test
    @DisplayName("Xóa thất bại - URL không thuộc bucket cấu hình")
    void deleteByUrl_WrongBucket() {
        String wrongUrl = "http://other-site.com/wrong-bucket/image.jpg";

        assertThrows(StorageDeleteException.class, () -> storageService.deleteByUrl(wrongUrl));
        verifyNoInteractions(minioClient);
    }

    @Test
    @DisplayName("Parse Object Key - Trích xuất đúng key từ URL")
    void parseObjectKey_ReturnsCorrectPath() {
        String url = "http://minio:9000/food-bucket/subfolder/image.png";

        try {
            storageService.deleteByUrl(url);
            verify(minioClient).removeObject(argThat(args ->
                    args.object().equals("subfolder/image.png")
            ));
        } catch (Exception e) {
            fail("Should not throw exception");
        }
    }
}