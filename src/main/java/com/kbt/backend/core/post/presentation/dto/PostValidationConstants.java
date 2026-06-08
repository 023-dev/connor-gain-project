package com.kbt.backend.core.post.presentation.dto;

public final class PostValidationConstants {

    public static final int TITLE_MAX_LENGTH = 100;
    public static final int CONTENT_MAX_LENGTH = 5000;
    public static final int IMAGE_URL_MAX_LENGTH = 2048;
    public static final String NOT_BLANK_IF_PRESENT_PATTERN = ".*\\S.*";

    private PostValidationConstants() {
    }
}
