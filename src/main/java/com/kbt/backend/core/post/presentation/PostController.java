package com.kbt.backend.core.post.presentation;

import com.kbt.backend.core.auth.presentation.Authenticated;
import com.kbt.backend.core.auth.presentation.UserId;
import java.util.Optional;
import com.kbt.backend.core.post.application.PostApplicationService;
import com.kbt.backend.core.post.application.dto.PostCreateResponse;
import com.kbt.backend.core.post.application.dto.PostResponse;
import com.kbt.backend.core.post.application.dto.PostsResponse;
import com.kbt.backend.core.post.application.dto.PostUpdateResponse;
import com.kbt.backend.core.post.presentation.dto.PostCreateRequest;
import com.kbt.backend.core.post.presentation.dto.PostUpdateRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
@Validated
public class PostController {

    private final PostApplicationService postService;

    @PostMapping
    @Authenticated
    public ResponseEntity<PostCreateResponse> create(
            @UserId final String userId,
            @Valid @RequestBody final PostCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(postService.create(
                        userId,
                        request.title(),
                        request.content(),
                        request.imageUrl()
                ));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostResponse> findOne(
            @UserId final Optional<String> userId,
            @PathVariable final String postId
    ) {
        return ResponseEntity.ok(postService.findOne(userId, postId));
    }

    @GetMapping
    public ResponseEntity<PostsResponse> findAll(
            @UserId final Optional<String> userId,
            @RequestParam(required = false) final String cursor,
            @RequestParam(required = false, defaultValue = "10") @Min(1) @Max(value = 20) final int size
    ) {
        return ResponseEntity.ok(postService.findAll(userId, cursor, size));
    }

    @PatchMapping("/{postId}")
    @Authenticated
    public ResponseEntity<PostUpdateResponse> edit(
            @UserId final String userId,
            @PathVariable final String postId,
            @Valid @RequestBody final PostUpdateRequest request
    ) {
        return ResponseEntity.ok(postService.edit(
                userId,
                postId,
                request.title(),
                request.content(),
                request.imageUrl()
        ));
    }

    @DeleteMapping("/{postId}")
    @Authenticated
    public ResponseEntity<Void> delete(
            @UserId final String userId,
            @PathVariable final String postId
    ) {
        postService.delete(userId, postId);
        return ResponseEntity.ok().build();
    }
}
