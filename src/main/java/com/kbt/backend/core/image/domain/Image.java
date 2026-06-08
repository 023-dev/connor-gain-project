package com.kbt.backend.core.image.domain;

import com.kbt.backend.common.exception.ApiException;
import com.kbt.backend.common.exception.ErrorType;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.springframework.util.StringUtils;

import java.util.Set;

@Getter
@Accessors(fluent = true)
public class Image {

    private static final long MAX_FILE_SIZE = 5L * 1024L * 1024L;
    private static final String PNG_CONTENT_TYPE = "image/png";
    private static final String JPEG_CONTENT_TYPE = "image/jpeg";
    private static final String PNG_EXTENSION = ".png";
    private static final String JPEG_EXTENSION = ".jpg";
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(PNG_CONTENT_TYPE, JPEG_CONTENT_TYPE);

    private final String filename;
    private final String contentType;
    private final long size;

    public Image(
            final String filename,
            final String contentType,
            final long size
    ) {
        validate(filename, contentType, size);
        this.filename = filename;
        this.contentType = contentType;
        this.size = size;
    }

    public String extension() {
        if (PNG_CONTENT_TYPE.equals(contentType)) {
            return PNG_EXTENSION;
        }

        return JPEG_EXTENSION;
    }

    private void validate(
            final String filename,
            final String contentType,
            final long size
    ) {
        if (!StringUtils.hasText(filename) || size <= 0 || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new ApiException(ErrorType.INVALID_REQUEST);
        }

        if (size > MAX_FILE_SIZE) {
            throw new ApiException(ErrorType.FILE_TOO_LARGE);
        }
    }
}
