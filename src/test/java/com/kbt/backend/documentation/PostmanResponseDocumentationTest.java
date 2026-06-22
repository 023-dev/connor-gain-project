package com.kbt.backend.documentation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kbt.backend.core.auth.presentation.RefreshTokenCookie;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
class PostmanResponseDocumentationTest {

    private static final Path OUTPUT_DIR = Path.of("build/postman-responses");
    private static final String ACCESS_TOKEN_FIELD = "accessToken";
    private static final String POST_ID_FIELD = "postId";
    private static final String COMMENT_ID_FIELD = "commentId";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void generatePostmanResponseExamples() throws Exception {
        Files.createDirectories(OUTPUT_DIR);

        final List<ResponseExample> examples = new ArrayList<>();
        final String suffix = String.valueOf(Instant.now().toEpochMilli());
        final String email = "postman-" + suffix + "@startupcode.kr";
        final String password = "test1234";
        final String newPassword = "test12345";
        final String nickname = "postman-" + suffix;
        final String updatedNickname = "postman-updated-" + suffix;

        examples.add(executeJson(
                "01-signup",
                "회원가입",
                "POST",
                "/users/signup",
                """
                        {"email":"%s","password":"%s","nickname":"%s","profileImage":"https://image.kr/img.jpg"}
                        """.formatted(email, password, nickname),
                null
        ));

        final ResponseExample signin = executeJson(
                "02-signin",
                "로그인",
                "POST",
                "/users/signin",
                """
                        {"email":"%s","password":"%s"}
                        """.formatted(email, password),
                null
        );
        examples.add(signin);
        String accessToken = value(signin, ACCESS_TOKEN_FIELD);
        String refreshToken = refreshToken(signin);

        final ResponseExample reissue = executeJson(
                "03-reissue",
                "액세스 토큰 재발급",
                "POST",
                "/auth/reissue",
                null,
                null,
                refreshToken
        );
        examples.add(reissue);
        accessToken = value(reissue, ACCESS_TOKEN_FIELD);

        examples.add(executeJson("04-me", "회원정보조회", "GET", "/users/me", null, accessToken));
        examples.add(executeJson(
                "05-update-user",
                "회원정보수정",
                "PATCH",
                "/users/me",
                """
                        {"nickname":"%s","profileImage":"https://image.kr/updated.jpg"}
                        """.formatted(updatedNickname),
                accessToken
        ));
        examples.add(executeJson(
                "06-update-password",
                "비밀번호 수정",
                "PATCH",
                "/users/me/password",
                """
                        {"newPassword":"%s"}
                        """.formatted(newPassword),
                accessToken
        ));

        final ResponseExample signinUpdatedPassword = executeJson(
                "07-signin-updated-password",
                "변경 비밀번호 로그인",
                "POST",
                "/users/signin",
                """
                        {"email":"%s","password":"%s"}
                        """.formatted(email, newPassword),
                null
        );
        examples.add(signinUpdatedPassword);
        accessToken = value(signinUpdatedPassword, ACCESS_TOKEN_FIELD);

        final ResponseExample imageUpload = executeImageUpload();
        examples.add(imageUpload);
        final String imageUrl = value(imageUpload, "imageUrl");

        final ResponseExample postCreate = executeJson(
                "09-create-post",
                "게시글 추가",
                "POST",
                "/posts",
                """
                        {"title":"제목입니다.","content":"내용입니다.","imageUrl":"%s"}
                        """.formatted(imageUrl),
                accessToken
        );
        examples.add(postCreate);
        final String postId = value(postCreate, POST_ID_FIELD);

        examples.add(executeJson("10-find-post", "게시글 상세조회", "GET", "/posts/" + postId, null, null));
        examples.add(executeJson("11-find-posts", "게시글 목록조회", "GET", "/posts", null, null));
        examples.add(executeJson("12-like-post", "게시글 좋아요", "POST", "/posts/" + postId + "/likes", null, accessToken));
        examples.add(executeJson("13-unlike-post", "게시글 좋아요 취소", "DELETE", "/posts/" + postId + "/likes", null, accessToken));

        final ResponseExample commentCreate = executeJson(
                "14-create-comment",
                "댓글 추가",
                "POST",
                "/posts/" + postId + "/comments",
                """
                        {"content":"댓글 내용입니다."}
                        """,
                accessToken
        );
        examples.add(commentCreate);
        final String commentId = value(commentCreate, COMMENT_ID_FIELD);

        examples.add(executeJson("15-find-comment", "댓글 상세조회", "GET", "/posts/" + postId + "/comments/" + commentId, null, null));
        examples.add(executeJson("16-find-comments", "댓글 목록조회", "GET", "/posts/" + postId + "/comments", null, null));
        examples.add(executeJson(
                "17-edit-comment",
                "댓글 수정",
                "PATCH",
                "/posts/" + postId + "/comments/" + commentId,
                """
                        {"content":"수정된 댓글 내용입니다."}
                        """,
                accessToken
        ));
        examples.add(executeJson("18-delete-comment", "댓글 삭제", "DELETE", "/posts/" + postId + "/comments/" + commentId, null, accessToken));
        examples.add(executeJson(
                "19-edit-post",
                "게시글 수정",
                "PATCH",
                "/posts/" + postId,
                """
                        {"title":"수정된 제목입니다.","content":"수정된 내용입니다.","imageUrl":"%s"}
                        """.formatted(imageUrl),
                accessToken
        ));
        examples.add(executeJson("20-delete-post", "게시글 삭제", "DELETE", "/posts/" + postId, null, accessToken));
        examples.add(executeJson("21-signout", "로그아웃", "POST", "/users/signout", null, accessToken));

        final ResponseExample signinForDelete = executeJson(
                "22-signin-for-delete",
                "탈퇴용 재로그인",
                "POST",
                "/users/signin",
                """
                        {"email":"%s","password":"%s"}
                        """.formatted(email, newPassword),
                null
        );
        examples.add(signinForDelete);
        accessToken = value(signinForDelete, ACCESS_TOKEN_FIELD);

        examples.add(executeJson("23-delete-user", "회원탈퇴", "DELETE", "/users/me", null, accessToken));
        writeOutputs(examples);
    }

    private ResponseExample executeJson(
            final String fileKey,
            final String name,
            final String method,
            final String path,
            final String body,
            final String accessToken
    ) throws Exception {
        return executeJson(fileKey, name, method, path, body, accessToken, null);
    }

    private ResponseExample executeJson(
            final String fileKey,
            final String name,
            final String method,
            final String path,
            final String body,
            final String accessToken,
            final String refreshToken
    ) throws Exception {
        final MockHttpServletRequestBuilder request = switch (method) {
            case "GET" -> get(path);
            case "POST" -> post(path);
            case "PATCH" -> patch(path);
            case "DELETE" -> delete(path);
            default -> throw new IllegalArgumentException("Unsupported method: " + method);
        };

        request.headers(headers(accessToken));
        if (body != null) {
            request.contentType(MediaType.APPLICATION_JSON)
                    .content(body);
        }
        if (refreshToken != null) {
            request.cookie(new Cookie(RefreshTokenCookie.NAME, refreshToken));
        }

        final MvcResult result = mockMvc.perform(request)
                .andReturn();
        return save(fileKey, name, method, path, result);
    }

    private ResponseExample executeImageUpload() throws Exception {
        final MockMultipartFile image = new MockMultipartFile(
                "image",
                "post.png",
                MediaType.IMAGE_PNG_VALUE,
                "image".getBytes()
        );

        final MvcResult result = mockMvc.perform(multipart("/images").file(image))
                .andReturn();

        return save("08-upload-image", "이미지 업로드", "POST", "/images", result);
    }

    private org.springframework.http.HttpHeaders headers(final String accessToken) {
        final org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        if (accessToken != null) {
            headers.setBearerAuth(accessToken);
        }
        return headers;
    }

    private ResponseExample save(
            final String fileKey,
            final String name,
            final String method,
            final String path,
            final MvcResult result
    ) throws IOException {
        final int status = result.getResponse().getStatus();
        final String body = result.getResponse().getContentAsString();
        final Cookie refreshTokenCookie = result.getResponse().getCookie(RefreshTokenCookie.NAME);
        final String refreshToken = refreshTokenCookie == null ? null : refreshTokenCookie.getValue();
        Files.writeString(OUTPUT_DIR.resolve(fileKey + ".json"), body);
        return new ResponseExample(name, method, path, status, body, refreshToken);
    }

    private String value(
            final ResponseExample example,
            final String fieldName
    ) throws IOException {
        final JsonNode node = objectMapper.readTree(example.body());
        return node.path(fieldName).asText();
    }

    private String refreshToken(final ResponseExample example) {
        return example.refreshTokenCookie();
    }

    private void writeOutputs(final List<ResponseExample> examples) throws IOException {
        final StringBuilder markdown = new StringBuilder();
        markdown.append("# Postman Response Examples\n\n");
        markdown.append("- Success body는 현재 코드 기준으로 wrapper 없이 DTO 그대로 반환합니다.\n");
        markdown.append("- Refresh Token은 응답 body가 아니라 `HttpOnly` cookie로 전달합니다.\n");
        markdown.append("- Error body는 `{ \"message\": \"...\" }` 형식입니다.\n\n");

        final StringBuilder tsv = new StringBuilder();
        tsv.append("요청\tRequest method\turl\tResponse status code\tresponse\n");

        for (final ResponseExample example : examples) {
            final String response = pretty(example.body());
            markdown.append("## ").append(example.name()).append("\n\n");
            markdown.append("`").append(example.method()).append(" ").append(example.path()).append("` -> `")
                    .append(example.status()).append("`\n\n");

            if (response.isBlank()) {
                markdown.append("응답 body 없음\n\n");
            } else {
                markdown.append("```json\n").append(response).append("\n```\n\n");
            }

            tsv.append(example.name()).append('\t')
                    .append(example.method()).append('\t')
                    .append(example.path()).append('\t')
                    .append(example.status()).append('\t')
                    .append(tsvCell(response.isBlank() ? "응답 body 없음" : response))
                    .append('\n');
        }

        Files.writeString(OUTPUT_DIR.resolve("postman-responses.md"), markdown);
        Files.writeString(OUTPUT_DIR.resolve("postman-responses.tsv"), tsv);
    }

    private String pretty(final String body) throws IOException {
        if (body == null || body.isBlank()) {
            return "";
        }
        return objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(objectMapper.readTree(body));
    }

    private String tsvCell(final String value) {
        if (!value.contains("\t") && !value.contains("\n") && !value.contains("\"")) {
            return value;
        }

        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
