package com.classitda.common.image;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ImageUploadUrlRequest(
        @Schema(description = "이미지 확장자", example = "jpg")
        @NotBlank(message = "이미지 확장자는 필수입니다.")
        String extension,

        @Schema(description = "이미지 크기(바이트)", example = "3145728")
        @NotNull(message = "이미지 크기는 필수입니다.")
        @Positive(message = "이미지 크기는 1바이트 이상이어야 합니다.")
        Long size
) {
    public static ImageUploadUrlRequest of(String extension, Long size) {
        return new ImageUploadUrlRequest(extension, size);
    }
}
