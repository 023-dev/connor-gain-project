package com.kbt.backend.core.post.presentation.like;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kbt.backend.core.post.presentation.dto.PostCreateRequest;
import com.kbt.backend.core.auth.presentation.dto.AuthLoginRequest;
import com.kbt.backend.core.user.presentation.dto.UserSignupRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class LikeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void likeAndUnlike() throws Exception {
        final JsonNode userSession = signupAndSignin();
        final String postId = createPost(accessToken(userSession));

        mockMvc.perform(post("/api/posts/" + postId + "/likes")
                        .header("Authorization", "Bearer " + accessToken(userSession)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postId").value(postId))
                .andExpect(jsonPath("$.likeCount").value(1))
                .andExpect(jsonPath("$.isLiked").value(true));

        mockMvc.perform(delete("/api/posts/" + postId + "/likes")
                        .header("Authorization", "Bearer " + accessToken(userSession)))
                .andExpect(status().isNoContent());
    }

    @Test
    void likeRejectsAlreadyLikedPost() throws Exception {
        final JsonNode userSession = signupAndSignin();
        final String postId = createPost(accessToken(userSession));

        mockMvc.perform(post("/api/posts/" + postId + "/likes")
                        .header("Authorization", "Bearer " + accessToken(userSession)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/posts/" + postId + "/likes")
                        .header("Authorization", "Bearer " + accessToken(userSession)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("이미 좋아요를 누른 게시글입니다."));
    }

    @Test
    void unlikeRejectsNotLikedPost() throws Exception {
        final JsonNode userSession = signupAndSignin();
        final String postId = createPost(accessToken(userSession));

        mockMvc.perform(delete("/api/posts/" + postId + "/likes")
                        .header("Authorization", "Bearer " + accessToken(userSession)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("좋아요를 누르지 않은 게시글입니다."));
    }

    @Test
    void likeRejectsUnauthenticatedRequest() throws Exception {
        final JsonNode userSession = signupAndSignin();
        final String postId = createPost(accessToken(userSession));

        mockMvc.perform(post("/api/posts/" + postId + "/likes"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("로그인이 필요한 서비스입니다."));
    }

    @Test
    void likeRejectsUnknownPost() throws Exception {
        final JsonNode userSession = signupAndSignin();

        mockMvc.perform(post("/api/posts/" + UUID.randomUUID() + "/likes")
                        .header("Authorization", "Bearer " + accessToken(userSession)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("존재하지 않는 게시글입니다."));
    }

    @Test
    void likeCanBeRecreatedAfterUnlike() throws Exception {
        final JsonNode userSession = signupAndSignin();
        final String postId = createPost(accessToken(userSession));

        mockMvc.perform(post("/api/posts/" + postId + "/likes")
                        .header("Authorization", "Bearer " + accessToken(userSession)))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/posts/" + postId + "/likes")
                        .header("Authorization", "Bearer " + accessToken(userSession)))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/posts/" + postId + "/likes")
                        .header("Authorization", "Bearer " + accessToken(userSession)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.likeCount").value(1))
                .andExpect(jsonPath("$.isLiked").value(true));
    }

    private String createPost(final String accessToken) throws Exception {
        final String createContent = mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PostCreateRequest(
                                "post title",
                                "post content",
                                "https://image.kr/post.jpg"
                        ))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(createContent).at("/postId").asText();
    }

    private JsonNode signupAndSignin() throws Exception {
        final String suffix = "post-like-" + UUID.randomUUID();
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

        final String signinContent = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AuthLoginRequest(
                                signupRequest.email(),
                                signupRequest.password()
                        ))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        final JsonNode signinJson = objectMapper.readTree(signinContent);
        return objectMapper.createObjectNode()
                .put("accessToken", signinJson.at("/accessToken").asText());
    }

    private String accessToken(final JsonNode userSession) {
        return userSession.at("/accessToken").asText();
    }
}
