package com.kbt.backend.core.post.presentation.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.util.StringUtils;

public record PostUpdateRequest(
        @Pattern(regexp = PostValidationConstants.NOT_BLANK_IF_PRESENT_PATTERN)
        @Size(max = PostValidationConstants.TITLE_MAX_LENGTH)
        String title,

        @Pattern(regexp = PostValidationConstants.NOT_BLANK_IF_PRESENT_PATTERN)
        @Size(max = PostValidationConstants.CONTENT_MAX_LENGTH)
        String content,

        @Pattern(regexp = PostValidationConstants.NOT_BLANK_IF_PRESENT_PATTERN)
        @Size(max = PostValidationConstants.IMAGE_URL_MAX_LENGTH)
        String imageUrl
) {

    @AssertTrue
    public boolean hasEditValue() {
        return StringUtils.hasText(title)
                || StringUtils.hasText(content)
                || StringUtils.hasText(imageUrl);
    }
}
