package com.kbt.backend.core.post.presentation.comment;

import com.kbt.backend.core.auth.presentation.Authenticated;
import com.kbt.backend.core.auth.presentation.UserId;
import com.kbt.backend.core.post.application.comment.CommentApplicationService;
import com.kbt.backend.core.post.application.comment.dto.CommentCreateResponse;
import com.kbt.backend.core.post.application.comment.dto.CommentResponse;
import com.kbt.backend.core.post.application.comment.dto.CommentUpdateResponse;
import com.kbt.backend.core.post.application.comment.dto.CommentsResponse;
import com.kbt.backend.core.post.presentation.comment.dto.CommentCreateRequest;
import com.kbt.backend.core.post.presentation.comment.dto.CommentUpdateRequest;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/posts/{postId}/comments")
@RequiredArgsConstructor
@Validated
public class CommentController {

    private final CommentApplicationService commentService;

    @PostMapping
    @Authenticated
    public ResponseEntity<CommentCreateResponse> create(
            @UserId final String userId,
            @PathVariable final String postId,
            @Valid @RequestBody final CommentCreateRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.create(userId, postId, request.content()));
    }

    @GetMapping("/{commentId}")
    public ResponseEntity<CommentResponse> findOne(
            @PathVariable final String postId,
            @PathVariable final String commentId
    ) {
        return ResponseEntity.ok(commentService.findOne(postId, commentId));
    }

    @GetMapping
    public ResponseEntity<CommentsResponse> findAllByPostId(
            @PathVariable final String postId
    ) {
        return ResponseEntity.ok(commentService.findAllByPostId(postId));
    }

    @PatchMapping("/{commentId}")
    @Authenticated
    public ResponseEntity<CommentUpdateResponse> edit(
            @UserId final String userId,
            @PathVariable final String postId,
            @PathVariable final String commentId,
            @Valid @RequestBody final CommentUpdateRequest request
    ) {
        return ResponseEntity.ok(commentService.edit(userId, postId, commentId, request.content()));
    }

    @DeleteMapping("/{commentId}")
    @Authenticated
    public ResponseEntity<Void> delete(
            @UserId final String userId,
            @PathVariable final String postId,
            @PathVariable final String commentId
    ) {
        commentService.delete(userId, postId, commentId);
        return ResponseEntity.ok().build();
    }
}
