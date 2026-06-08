package com.kbt.backend.core.image.application;

import com.kbt.backend.common.utils.UuidGenerator;
import com.kbt.backend.core.image.domain.Image;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ImageCommandService {

    private static final String IMAGE_URL_PREFIX = "https://image.kr/uploaded/";

    public String upload(
            final String filename,
            final String contentType,
            final long size
    ) {
        final Image image = new Image(filename, contentType, size);
        return IMAGE_URL_PREFIX + UuidGenerator.generate() + image.extension();
    }
}
