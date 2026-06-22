package com.kbt.backend.core.post.presentation.comment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kbt.backend.core.post.presentation.comment.dto.CommentCreateRequest;
import com.kbt.backend.core.post.presentation.comment.dto.CommentUpdateRequest;
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

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class CommentControllerTest {

    private static final String UUID_REGEX = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createFindOneFindAllEditAndDelete() throws Exception {
        final JsonNode userSession = signupAndSignin();
        final String postId = createPost(accessToken(userSession));
        final CommentCreateRequest createRequest = new CommentCreateRequest("comment content");

        final String createContent = mockMvc.perform(post("/api/posts/" + postId + "/comments")
                        .header("Authorization", "Bearer " + accessToken(userSession))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.commentId", matchesPattern(UUID_REGEX)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        final String commentId = objectMapper.readTree(createContent).at("/commentId").asText();

        mockMvc.perform(get("/api/posts/" + postId + "/comments/" + commentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.commentId").value(commentId))
                .andExpect(jsonPath("$.postId").value(postId))
                .andExpect(jsonPath("$.userId").value(userId(userSession)))
                .andExpect(jsonPath("$.nickname").value(nickname(userSession)))
                .andExpect(jsonPath("$.content").value(createRequest.content()));

        mockMvc.perform(get("/api/posts/" + postId + "/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comments").isArray());

        mockMvc.perform(patch("/api/posts/" + postId + "/comments/" + commentId)
                        .header("Authorization", "Bearer " + accessToken(userSession))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CommentUpdateRequest("updated comment"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.commentId").value(commentId))
                .andExpect(jsonPath("$.content").value("updated comment"));

        mockMvc.perform(delete("/api/posts/" + postId + "/comments/" + commentId)
                        .header("Authorization", "Bearer " + accessToken(userSession)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/posts/" + postId + "/comments/" + commentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("존재하지 않는 댓글입니다."));
    }

    @Test
    void createRejectsUnauthenticatedRequest() throws Exception {
        final JsonNode userSession = signupAndSignin();
        final String postId = createPost(accessToken(userSession));

        mockMvc.perform(post("/api/posts/" + postId + "/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CommentCreateRequest("comment content"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("로그인이 필요한 서비스입니다."));
    }

    @Test
    void createRejectsUnknownPost() throws Exception {
        final JsonNode userSession = signupAndSignin();

        mockMvc.perform(post("/api/posts/" + UUID.randomUUID() + "/comments")
                        .header("Authorization", "Bearer " + accessToken(userSession))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CommentCreateRequest("comment content"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("존재하지 않는 게시글입니다."));
    }

    @Test
    void createRejectsInvalidRequest() throws Exception {
        final JsonNode userSession = signupAndSignin();
        final String postId = createPost(accessToken(userSession));

        mockMvc.perform(post("/api/posts/" + postId + "/comments")
                        .header("Authorization", "Bearer " + accessToken(userSession))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("댓글 내용은 필수 입력 항목입니다."));
    }

    @Test
    void findOneRejectsCommentFromOtherPost() throws Exception {
        final JsonNode userSession = signupAndSignin();
        final String postId = createPost(accessToken(userSession));
        final String otherPostId = createPost(accessToken(userSession));
        final String commentId = createComment(accessToken(userSession), postId);

        mockMvc.perform(get("/api/posts/" + otherPostId + "/comments/" + commentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("존재하지 않는 댓글입니다."));
    }

    @Test
    void editRejectsOtherUser() throws Exception {
        final JsonNode writer = signupAndSignin();
        final JsonNode other = signupAndSignin();
        final String postId = createPost(accessToken(writer));
        final String commentId = createComment(accessToken(writer), postId);

        mockMvc.perform(patch("/api/posts/" + postId + "/comments/" + commentId)
                        .header("Authorization", "Bearer " + accessToken(other))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CommentUpdateRequest("other comment"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("해당 권한이 없습니다."));
    }

    @Test
    void deleteRejectsOtherUserWithoutDecreasingCommentCount() throws Exception {
        final JsonNode writer = signupAndSignin();
        final JsonNode other = signupAndSignin();
        final String postId = createPost(accessToken(writer));
        final String commentId = createComment(accessToken(writer), postId);

        mockMvc.perform(delete("/api/posts/" + postId + "/comments/" + commentId)
                        .header("Authorization", "Bearer " + accessToken(other)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("해당 권한이 없습니다."));

        mockMvc.perform(get("/api/posts/" + postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.commentCount").value(1));
    }

    @Test
    void findOneMasksDeletedWriterNickname() throws Exception {
        final JsonNode userSession = signupAndSignin();
        final String postId = createPost(accessToken(userSession));
        final String commentId = createComment(accessToken(userSession), postId);

        mockMvc.perform(delete("/api/users/me")
                        .header("Authorization", "Bearer " + accessToken(userSession)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/posts/" + postId + "/comments/" + commentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.commentId").value(commentId))
                .andExpect(jsonPath("$.userId").value(userId(userSession)))
                .andExpect(jsonPath("$.nickname").value("알 수 없음"));
    }

    private String createComment(
            final String accessToken,
            final String postId
    ) throws Exception {
        final String createContent = mockMvc.perform(post("/api/posts/" + postId + "/comments")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CommentCreateRequest("comment content"))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(createContent).at("/commentId").asText();
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
        final String suffix = "comment-" + UUID.randomUUID();
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
                .put("userId", signinJson.at("/userId").asText())
                .put("nickname", signupRequest.nickname())
                .put("accessToken", signinJson.at("/accessToken").asText());
    }

    private String userId(final JsonNode userSession) {
        return userSession.at("/userId").asText();
    }

    private String nickname(final JsonNode userSession) {
        return userSession.at("/nickname").asText();
    }

    private String accessToken(final JsonNode userSession) {
        return userSession.at("/accessToken").asText();
    }
}
