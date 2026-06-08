package com.kbt.backend.core.user.presentation;

import com.kbt.backend.core.auth.presentation.AccessToken;
import com.kbt.backend.core.auth.presentation.Authenticated;
import com.kbt.backend.core.auth.presentation.UserId;
import com.kbt.backend.core.user.application.UserApplicationService;
import com.kbt.backend.core.user.application.dto.UserMeResponse;
import com.kbt.backend.core.user.application.dto.UserSigninResponse;
import com.kbt.backend.core.user.application.dto.UserSignupResponse;
import com.kbt.backend.core.user.application.dto.UserUpdateResponse;
import com.kbt.backend.core.user.presentation.dto.UserPasswordUpdateRequest;
import com.kbt.backend.core.user.presentation.dto.UserSigninRequest;
import com.kbt.backend.core.user.presentation.dto.UserSignupRequest;
import com.kbt.backend.core.user.presentation.dto.UserUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserApplicationService userService;

    @PostMapping("/signup")
    public ResponseEntity<UserSignupResponse> signup(
            @Valid @RequestBody final UserSignupRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.signup(
                        request.email(),
                        request.password(),
                        request.nickname(),
                        request.profileImage()
                ));
    }

    @PostMapping("/signin")
    public ResponseEntity<UserSigninResponse> signin(
            @Valid @RequestBody final UserSigninRequest request
    ) {
        return ResponseEntity.ok(userService.signin(request.email(), request.password()));
    }

    @PostMapping("/signout")
    @Authenticated
    public ResponseEntity<Void> signout(
            @UserId final String userId,
            @AccessToken final String accessToken
    ) {
        userService.signout(userId, accessToken);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    @Authenticated
    public ResponseEntity<UserMeResponse> me(
            @UserId final String userId
    ) {
        return ResponseEntity.ok(userService.me(userId));
    }

    @DeleteMapping("/me")
    @Authenticated
    public ResponseEntity<Void> delete(
            @UserId final String userId
    ) {
        userService.delete(userId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/me")
    @Authenticated
    public ResponseEntity<UserUpdateResponse> update(
            @UserId final String userId,
            @Valid @RequestBody final UserUpdateRequest request
    ) {
        return ResponseEntity.ok(userService.update(userId, request.nickname(), request.profileImage()));
    }

    @PatchMapping("/me/password")
    @Authenticated
    public ResponseEntity<Void> updatePassword(
            @UserId final String userId,
            @Valid @RequestBody final UserPasswordUpdateRequest request
    ) {
        userService.updatePassword(userId, request.newPassword());
        return ResponseEntity.ok().build();
    }
}
