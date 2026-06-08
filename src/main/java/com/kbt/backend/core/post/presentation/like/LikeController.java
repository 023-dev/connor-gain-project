package com.kbt.backend.core.post.presentation.like;

import com.kbt.backend.core.auth.presentation.Authenticated;
import com.kbt.backend.core.auth.presentation.UserId;
import com.kbt.backend.core.post.application.like.LikeApplicationService;
import com.kbt.backend.core.post.application.like.dto.LikeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/posts/{postId}/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeApplicationService likeService;

    @PostMapping
    @Authenticated
    public ResponseEntity<LikeResponse> like(
            @UserId final String userId,
            @PathVariable final String postId
    ) {
        return ResponseEntity.ok(likeService.like(userId, postId));
    }

    @DeleteMapping
    @Authenticated
    public ResponseEntity<LikeResponse> unlike(
            @UserId final String userId,
            @PathVariable final String postId
    ) {
        return ResponseEntity.ok(likeService.unlike(userId, postId));
    }
}
