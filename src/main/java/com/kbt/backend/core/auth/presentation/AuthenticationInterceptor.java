package com.kbt.backend.core.auth.presentation;

import com.kbt.backend.common.exception.ApiException;
import com.kbt.backend.common.exception.ErrorType;
import com.kbt.backend.core.auth.application.AuthTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Component
@RequiredArgsConstructor
public class AuthenticationInterceptor implements HandlerInterceptor {

    public static final String AUTH_USER_ID = "AUTH_USER_ID";
    public static final String AUTH_ACCESS_TOKEN = "AUTH_ACCESS_TOKEN";
    private final AuthTokenService authTokenService;

    @Override
    public boolean preHandle(
            @NonNull final HttpServletRequest request,
            @NonNull final HttpServletResponse response,
            @NonNull final Object handler
    ) {
        if (!(handler instanceof final HandlerMethod handlerMethod)) {
            return true;
        }

        final String authorizationHeader = request.getHeader(AUTHORIZATION);

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            final String token = authorizationHeader.substring("Bearer ".length());
            if (StringUtils.hasText(token)) {
                try {
                    request.setAttribute(AUTH_USER_ID, authTokenService.parseAccessToken(token));
                    request.setAttribute(AUTH_ACCESS_TOKEN, token);
                } catch (Exception exception) {
                    // Ignore exception for anonymous endpoints
                }
            }
        }

        if (handlerMethod.hasMethodAnnotation(Authenticated.class)) {
            if (request.getAttribute(AUTH_USER_ID) == null) {
                throw new ApiException(ErrorType.UNAUTHORIZED);
            }
        }

        return true;
    }
}
