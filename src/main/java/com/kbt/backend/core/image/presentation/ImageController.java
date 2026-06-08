package com.kbt.backend.core.image.presentation;

import com.kbt.backend.core.image.application.ImageApplicationService;
import com.kbt.backend.core.image.application.dto.ImageUploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class ImageController {

    private final ImageApplicationService imageService;

    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageUploadResponse> upload(
            @RequestPart("image") final MultipartFile image
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(imageService.upload(image.getOriginalFilename(), image.getContentType(), image.getSize()));
    }
}
