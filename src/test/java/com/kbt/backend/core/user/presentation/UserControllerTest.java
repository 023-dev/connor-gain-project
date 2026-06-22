package com.kbt.backend.core.user.presentation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kbt.backend.core.auth.presentation.RefreshTokenCookie;
import com.kbt.backend.core.auth.presentation.dto.AuthLoginRequest;
import com.kbt.backend.core.user.presentation.dto.UserSignupRequest;
import com.kbt.backend.core.user.presentation.dto.UserUpdateRequest;
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
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    private static final String UUID_REGEX = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void signupSigninAndMe() throws Exception {
        final UserSignupRequest signupRequest = signupRequest();

        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId", matchesPattern(UUID_REGEX)));

        final MvcResult signinResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AuthLoginRequest(
                                signupRequest.email(),
                                signupRequest.password()
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", matchesPattern(UUID_REGEX)))
                .andExpect(jsonPath("$.nickname").value(signupRequest.nickname()))
                .andExpect(jsonPath("$.profileImage").value(signupRequest.profileImage()))
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andReturn();

        final Cookie refreshTokenCookie = signinResult.getResponse().getCookie(RefreshTokenCookie.NAME);
        assertThat(refreshTokenCookie).isNotNull();
        assertThat(refreshTokenCookie.isHttpOnly()).isTrue();

        final JsonNode signinJson = objectMapper.readTree(signinResult.getResponse().getContentAsString());
        final String userId = signinJson.at("/userId").asText();
        final String accessToken = signinJson.at("/accessToken").asText();

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.email").value(signupRequest.email()))
                .andExpect(jsonPath("$.nickname").value(signupRequest.nickname()))
                .andExpect(jsonPath("$.profileImage").value(signupRequest.profileImage()));
    }

    @Test
    void meRejectsUnauthenticatedRequest() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("로그인이 필요한 서비스입니다."));
    }

    @Test
    void meRejectsBlankBearerTokenAsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer "))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("로그인이 필요한 서비스입니다."));
    }

    @Test
    void signinRejectsUnknownEmailAsInvalidCredentials() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AuthLoginRequest(
                                "unknown-" + UUID.randomUUID() + "@startupcode.kr",
                                "test1234"
                        ))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("이메일 또는 비밀번호가 일치하지 않습니다."));
    }

    @Test
    void signoutRevokesAccessToken() throws Exception {
        final JsonNode signinJson = signupAndSignin(signupRequest());
        final String accessToken = signinJson.at("/accessToken").asText();

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("로그인이 필요한 서비스입니다."));
    }

    @Test
    void signoutRevokesRefreshToken() throws Exception {
        final MvcResult signinResult = signupAndSigninResult(signupRequest());
        final JsonNode signinJson = objectMapper.readTree(signinResult.getResponse().getContentAsString());
        final String accessToken = signinJson.at("/accessToken").asText();
        final String refreshToken = signinResult.getResponse().getCookie(RefreshTokenCookie.NAME).getValue();

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/auth/reissue")
                        .cookie(new Cookie(RefreshTokenCookie.NAME, refreshToken)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("유효하지 않은 인증 토큰입니다."));
    }

    @Test
    void signupRejectsInvalidRequest() throws Exception {
        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void signupRejectsDuplicateEmail() throws Exception {
        final UserSignupRequest signupRequest = signupRequest();

        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UserSignupRequest(
                                signupRequest.email(),
                                "test1234",
                                "duplicate-" + UUID.randomUUID(),
                                "https://image.kr/img.jpg"
                        ))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("이미 가입된 이메일입니다."));
    }

    @Test
    void signupRejectsNullBodyByBeanValidation() throws Exception {
        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("null"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("올바르지 않은 요청 정보입니다."));
    }

    @Test
    void updateRejectsBlankValueByDtoValidation() throws Exception {
        final UserSignupRequest signupRequest = signupRequest();
        final String accessToken = signupAndSignin(signupRequest).at("/accessToken").asText();

        mockMvc.perform(patch("/api/users/me")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UserUpdateRequest(
                                "   ",
                                "https://image.kr/img.jpg"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("닉네임은 공백일 수 없습니다."));
    }

    private UserSignupRequest signupRequest() {
        final String suffix = "controller-" + UUID.randomUUID();
        return new UserSignupRequest(
                suffix + "@startupcode.kr",
                "test1234",
                suffix,
                "https://image.kr/img.jpg"
        );
    }

    private JsonNode signupAndSignin(final UserSignupRequest signupRequest) throws Exception {
        return objectMapper.readTree(signupAndSigninResult(signupRequest).getResponse().getContentAsString());
    }

    private MvcResult signupAndSigninResult(final UserSignupRequest signupRequest) throws Exception {
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
                .andReturn();
    }
}
