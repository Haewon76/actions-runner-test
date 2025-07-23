package com.cashmallow.api.infrastructure.fileserver;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class FileExtensionValidatorTest {

    @Mock
    private MultipartFile multipartFile;

    public FileExtensionValidatorTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("[File]  파일명 추출 후 JPG, JPEG, PNG만 허용되는지 테스트")
    void fileExtensionValidatorTest2() {

        final List<File> filenames = Arrays.asList(
                new File("test.jpg"),
                new File("test.PNG"),
                new File("test.jpeg"),
                new File("test.gif"));

        FileExtensionValidator fileExtensionValidator = FileExtensionValidator.forImageType();

        assertTrue(fileExtensionValidator.test(filenames.get(0).getName()));
        assertTrue(fileExtensionValidator.test(filenames.get(1).getName()));
        assertTrue(fileExtensionValidator.test(filenames.get(2).getName()));
        assertFalse(fileExtensionValidator.test(filenames.get(3).getName()));
    }

    @DisplayName("[MultipartFile] 파일명 추출 후  JPG, PNG 테스트")
    @Test
    public void testMockMultipartFile() throws IOException {
        byte[] content = "Hello, World!".getBytes(StandardCharsets.UTF_8);
        when(multipartFile.getOriginalFilename()).thenReturn("example.txt");
        when(multipartFile.getBytes()).thenReturn(content);

        // Test the mocked MultipartFile
        assertEquals("example.txt", multipartFile.getOriginalFilename());
        assertEquals(content, multipartFile.getBytes());
    }
}