package com.kbt.backend.core.image.application;

import com.kbt.backend.core.image.application.dto.ImageUploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ImageApplicationService {

    private final ImageCommandService imageCommandService;

    public ImageUploadResponse upload(
            final String filename,
            final String contentType,
            final long size
    ) {
        final String imageUrl = imageCommandService.upload(filename, contentType, size);
        return new ImageUploadResponse(imageUrl);
    }
}
