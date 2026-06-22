package com.kbt.backend.core.auth.presentation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kbt.backend.core.auth.presentation.dto.AuthLoginRequest;
import com.kbt.backend.core.user.presentation.dto.UserSignupRequest;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void reissueRotatesRefreshToken() throws Exception {
        final MvcResult signinResult = signupAndSignin();
        final String refreshToken = refreshToken(signinResult);

        final MvcResult reissueResult = mockMvc.perform(post("/api/auth/reissue")
                        .cookie(new Cookie(RefreshTokenCookie.NAME, refreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andReturn();

        final JsonNode reissueJson = objectMapper.readTree(reissueResult.getResponse().getContentAsString());
        final String newAccessToken = reissueJson.at("/accessToken").asText();
        final String newRefreshToken = refreshToken(reissueResult);

        mockMvc.perform(post("/api/auth/reissue")
                        .cookie(new Cookie(RefreshTokenCookie.NAME, refreshToken)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("유효하지 않은 인증 토큰입니다."));

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + newAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").isString());

        mockMvc.perform(post("/api/auth/reissue")
                        .cookie(new Cookie(RefreshTokenCookie.NAME, newRefreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.refreshToken").doesNotExist());
    }

    @Test
    void reissueRejectsMissingRefreshToken() throws Exception {
        mockMvc.perform(post("/api/auth/reissue"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("유효하지 않은 인증 토큰입니다."));
    }

    @Test
    void reissueRejectsInvalidToken() throws Exception {
        mockMvc.perform(post("/api/auth/reissue")
                        .cookie(new Cookie(RefreshTokenCookie.NAME, "invalid-token")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("유효하지 않은 인증 토큰입니다."));
    }

    private MvcResult signupAndSignin() throws Exception {
        final String suffix = "auth-" + UUID.randomUUID();
        final UserSignupRequest signupRequest = new UserSignupRequest(
                suffix + "@startupcode.kr",
                "test1234",
                suffix,
                "https://image.kr/img.jpg"
        );

        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isCreated());

        return mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AuthLoginRequest(
                                signupRequest.email(),
                                signupRequest.password()
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andReturn();
    }

    private String refreshToken(final MvcResult result) {
        final Cookie cookie = result.getResponse().getCookie(RefreshTokenCookie.NAME);
        assertThat(cookie).isNotNull();
        assertThat(cookie.isHttpOnly()).isTrue();
        return cookie.getValue();
    }
}
