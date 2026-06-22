package com.kbt.backend.core.image.presentation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ImageControllerTest {

    private static final int MAX_FILE_SIZE = 5 * 1024 * 1024;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void upload() throws Exception {
        final MockMultipartFile image = new MockMultipartFile(
                "image",
                "post.png",
                MediaType.IMAGE_PNG_VALUE,
                "image".getBytes()
        );

        mockMvc.perform(multipart("/images").file(image))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.imageUrl", startsWith("https://image.kr/uploaded/")))
                .andExpect(jsonPath("$.imageUrl", endsWith(".png")));
    }

    @Test
    void uploadRejectsUpperImagePartName() throws Exception {
        final MockMultipartFile image = new MockMultipartFile(
                "Image",
                "post.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "image".getBytes()
        );

        mockMvc.perform(multipart("/images").file(image))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("올바르지 않은 요청 정보입니다."));
    }

    @Test
    void uploadRejectsInvalidContentType() throws Exception {
        final MockMultipartFile image = new MockMultipartFile(
                "image",
                "post.gif",
                MediaType.IMAGE_GIF_VALUE,
                "image".getBytes()
        );

        mockMvc.perform(multipart("/images").file(image))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("올바르지 않은 요청 정보입니다."));
    }

    @Test
    void uploadRejectsEmptyFile() throws Exception {
        final MockMultipartFile image = new MockMultipartFile(
                "image",
                "post.png",
                MediaType.IMAGE_PNG_VALUE,
                new byte[0]
        );

        mockMvc.perform(multipart("/images").file(image))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("올바르지 않은 요청 정보입니다."));
    }

    @Test
    void uploadRejectsFileTooLarge() throws Exception {
        final MockMultipartFile image = new MockMultipartFile(
                "image",
                "post.png",
                MediaType.IMAGE_PNG_VALUE,
                new byte[MAX_FILE_SIZE + 1]
        );

        mockMvc.perform(multipart("/images").file(image))
                .andExpect(status().isPayloadTooLarge())
                .andExpect(jsonPath("$.message").value("업로드 가능한 파일 크기를 초과했습니다."));
    }

    @Test
    void uploadRejectsNonMultipartRequest() throws Exception {
        mockMvc.perform(post("/api/images")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("올바르지 않은 요청 정보입니다."));
    }
}
