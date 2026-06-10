package com.kbt.backend.common.utils;

import com.kbt.backend.common.exception.ApiException;
import com.kbt.backend.common.exception.ErrorType;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public final class CursorUtils {

    private CursorUtils() {
    }

    public static List<String> decode(final String cursor) {
        if (!StringUtils.hasText(cursor)) {
            return null;
        }

        try {
            final String decoded = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            return Arrays.asList(decoded.split("\\|", -1));
        } catch (Exception exception) {
            throw new ApiException(ErrorType.INVALID_REQUEST);
        }
    }

    public static String encode(final String... values) {
        if (values == null || values.length == 0) {
            return null;
        }

        final String value = String.join("|", values);
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }
}
