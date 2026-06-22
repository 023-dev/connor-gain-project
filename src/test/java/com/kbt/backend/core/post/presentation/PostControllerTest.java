package com.kbt.backend.core.post.presentation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kbt.backend.core.post.presentation.comment.dto.CommentCreateRequest;
import com.kbt.backend.core.post.presentation.dto.PostCreateRequest;
import com.kbt.backend.core.post.presentation.dto.PostUpdateRequest;
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
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
class PostControllerTest {

    private static final String UUID_REGEX = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createFindOneFindAllEditAndDelete() throws Exception {
        final JsonNode userSession = signupAndSignin();
        final PostCreateRequest createRequest = new PostCreateRequest(
                "post title",
                "post content",
                "https://image.kr/post.jpg"
        );

        final String createContent = mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer " + accessToken(userSession))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.postId", matchesPattern(UUID_REGEX)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        final String postId = objectMapper.readTree(createContent).at("/postId").asText();

        mockMvc.perform(get("/api/posts/" + postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postId").value(postId))
                .andExpect(jsonPath("$.userId").value(userId(userSession)))
                .andExpect(jsonPath("$.nickname").value(nickname(userSession)))
                .andExpect(jsonPath("$.title").value(createRequest.title()))
                .andExpect(jsonPath("$.content").value(createRequest.content()))
                .andExpect(jsonPath("$.imageUrl").value(createRequest.imageUrl()))
                .andExpect(jsonPath("$.likeCount").value(0))
                .andExpect(jsonPath("$.commentCount").value(0))
                .andExpect(jsonPath("$.viewCount").value(1))
                .andExpect(jsonPath("$.isLiked").value(false));

        mockMvc.perform(get("/api/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts").isArray())
                .andExpect(jsonPath("$.posts[0].postId").value(postId))
                .andExpect(jsonPath("$.posts[0].likeCount").value(0))
                .andExpect(jsonPath("$.posts[0].commentCount").value(0))
                .andExpect(jsonPath("$.posts[0].viewCount").value(1))
                .andExpect(jsonPath("$.posts[0].isLiked").value(false))
                .andExpect(jsonPath("$.hasNext").value(false));

        mockMvc.perform(patch("/api/posts/" + postId)
                        .header("Authorization", "Bearer " + accessToken(userSession))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PostUpdateRequest(
                                "updated title",
                                "updated content",
                                "https://image.kr/updated-post.jpg"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postId").value(postId))
                .andExpect(jsonPath("$.title").value("updated title"))
                .andExpect(jsonPath("$.content").value("updated content"))
                .andExpect(jsonPath("$.imageUrl").value("https://image.kr/updated-post.jpg"));

        mockMvc.perform(delete("/api/posts/" + postId)
                        .header("Authorization", "Bearer " + accessToken(userSession)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/posts/" + postId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("존재하지 않는 게시글입니다."));
    }

    @Test
    void findAllSupportsCursorPaginationAndCounts() throws Exception {
        final JsonNode userSession = signupAndSignin();
        final String olderPostId = createPost(accessToken(userSession));
        final String targetPostId = createPost(accessToken(userSession));

        mockMvc.perform(post("/api/posts/" + targetPostId + "/likes")
                        .header("Authorization", "Bearer " + accessToken(userSession)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postId").value(targetPostId))
                .andExpect(jsonPath("$.likeCount").value(1))
                .andExpect(jsonPath("$.isLiked").value(true));

        mockMvc.perform(post("/api/posts/" + targetPostId + "/comments")
                        .header("Authorization", "Bearer " + accessToken(userSession))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CommentCreateRequest("nice post"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.commentId").exists());

        // Non-logged-in detail check (should be isLiked: false)
        mockMvc.perform(get("/api/posts/" + targetPostId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isLiked").value(false))
                .andExpect(jsonPath("$.viewCount").value(1));

        // Logged-in detail check (should be isLiked: true)
        mockMvc.perform(get("/api/posts/" + targetPostId)
                        .header("Authorization", "Bearer " + accessToken(userSession)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isLiked").value(true))
                .andExpect(jsonPath("$.viewCount").value(2));

        // Non-logged-in list check (should be isLiked: false)
        mockMvc.perform(get("/api/posts")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts[0].isLiked").value(false))
                .andExpect(jsonPath("$.posts[0].viewCount").value(2));

        // Logged-in list check (should be isLiked: true)
        final String firstPageContent = mockMvc.perform(get("/api/posts")
                        .header("Authorization", "Bearer " + accessToken(userSession))
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts[0].postId").value(targetPostId))
                .andExpect(jsonPath("$.posts[0].likeCount").value(1))
                .andExpect(jsonPath("$.posts[0].commentCount").value(1))
                .andExpect(jsonPath("$.posts[0].viewCount").value(2))
                .andExpect(jsonPath("$.posts[0].isLiked").value(true))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.nextCursor").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        final String nextCursor = objectMapper.readTree(firstPageContent).path("nextCursor").asText();
        assertFalse(nextCursor.isBlank());

        final String secondPageContent = mockMvc.perform(get("/api/posts")
                        .param("size", "1")
                        .param("cursor", nextCursor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts[0].postId").value(olderPostId))
                .andExpect(jsonPath("$.posts[0].likeCount").value(0))
                .andExpect(jsonPath("$.posts[0].commentCount").value(0))
                .andExpect(jsonPath("$.posts[0].viewCount").value(0))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.nextCursor").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        final String thirdCursor = objectMapper.readTree(secondPageContent).path("nextCursor").asText();
        assertFalse(thirdCursor.isBlank());

        mockMvc.perform(get("/api/posts")
                        .param("size", "1")
                        .param("cursor", thirdCursor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts[0].likeCount").value(0))
                .andExpect(jsonPath("$.posts[0].commentCount").value(1))
                .andExpect(jsonPath("$.posts[0].viewCount").value(0))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.nextCursor").value(nullValue()));
    }

    @Test
    void findAllTreatsInvalidBearerTokenAsAnonymous() throws Exception {
        mockMvc.perform(get("/api/posts")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts").isArray());
    }

    @Test
    void createRejectsInvalidBearerToken() throws Exception {
        mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer invalid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PostCreateRequest(
                                "post title",
                                "post content",
                                "https://image.kr/post.jpg"
                        ))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("로그인이 필요한 서비스입니다."));
    }

    @Test
    void findOneMasksDeletedWriterNickname() throws Exception {
        final JsonNode userSession = signupAndSignin();
        final String postId = createPost(accessToken(userSession));

        mockMvc.perform(delete("/api/users/me")
                        .header("Authorization", "Bearer " + accessToken(userSession)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/posts/" + postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.postId").value(postId))
                .andExpect(jsonPath("$.userId").value(userId(userSession)))
                .andExpect(jsonPath("$.nickname").value("알 수 없음"));
    }

    @Test
    void seedPostCountMatchesSeedComments() throws Exception {
        final String seedPostId = "22222222-2222-4222-8222-222222222222";

        mockMvc.perform(get("/api/posts/" + seedPostId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.commentCount").value(1));

        mockMvc.perform(get("/api/posts/" + seedPostId + "/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comments.length()").value(1));
    }

    @Test
    void findAllRejectsInvalidPaginationParams() throws Exception {
        mockMvc.perform(get("/api/posts")
                        .param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("올바르지 않은 요청 정보입니다."));

        mockMvc.perform(get("/api/posts")
                        .param("cursor", "bad-cursor"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("올바르지 않은 요청 정보입니다."));
    }

    @Test
    void createRejectsUnauthenticatedRequest() throws Exception {
        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PostCreateRequest(
                                "post title",
                                "post content",
                                "https://image.kr/post.jpg"
                        ))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("로그인이 필요한 서비스입니다."));
    }

    @Test
    void createRejectsInvalidRequest() throws Exception {
        final JsonNode userSession = signupAndSignin();

        mockMvc.perform(post("/api/posts")
                        .header("Authorization", "Bearer " + accessToken(userSession))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void editRejectsRequestWithoutEditValue() throws Exception {
        final JsonNode userSession = signupAndSignin();
        final String postId = createPost(accessToken(userSession));

        mockMvc.perform(patch("/api/posts/" + postId)
                        .header("Authorization", "Bearer " + accessToken(userSession))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("올바르지 않은 요청 정보입니다."));
    }

    @Test
    void editRejectsOtherUser() throws Exception {
        final JsonNode writer = signupAndSignin();
        final JsonNode other = signupAndSignin();
        final String postId = createPost(accessToken(writer));

        mockMvc.perform(patch("/api/posts/" + postId)
                        .header("Authorization", "Bearer " + accessToken(other))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PostUpdateRequest(
                                "other title",
                                null,
                                null
                        ))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("해당 권한이 없습니다."));
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
        final String suffix = "post-" + UUID.randomUUID();
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
