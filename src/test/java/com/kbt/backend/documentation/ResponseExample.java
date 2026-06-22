package com.kbt.backend.documentation;

record ResponseExample(
        String name,
        String method,
        String path,
        int status,
        String body,
        String refreshTokenCookie
) {
}
