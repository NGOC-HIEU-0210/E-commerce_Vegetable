package com.hieu.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class UploadServiceTest {

    @InjectMocks
    private UploadService uploadService;

    @Test
    void save_withFile_returnsPath() {
        ReflectionTestUtils.setField(uploadService, "uploadDir", "target/test-uploads");
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.png", "image/png", new byte[]{1, 2, 3});

        String path = uploadService.save(file, "Product");

        assertNotNull(path);
        assertEquals(true, path.startsWith("/UploadFiles/Product/"));
    }

    @Test
    void save_withEmptyFile_returnsNull() {
        ReflectionTestUtils.setField(uploadService, "uploadDir", "target/test-uploads");
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.png", "image/png", new byte[0]);

        String path = uploadService.save(file, "Product");

        assertEquals(null, path);
    }
}
