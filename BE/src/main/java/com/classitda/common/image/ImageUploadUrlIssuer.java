package com.classitda.common.image;

import com.classitda.common.exception.ClassitdaException;
import com.classitda.common.exception.CommonErrorCode;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@RequiredArgsConstructor
@Component
public class ImageUploadUrlIssuer {

    private static final Map<String, String> ALLOWED_CONTENT_TYPES = Map.of(
            "jpg", "image/jpeg",
            "jpeg", "image/jpeg",
            "png", "image/png",
            "webp", "image/webp"
    );

    private static final long MAX_UPLOAD_SIZE_BYTES = 5L * 1024 * 1024;

    private final S3Presigner s3Presigner;
    private final ImageProperties imageProperties;

    public ImageUploadUrl issue(String namespace, String extension, long size) {
        validateNamespace(namespace);
        validateSize(size);
        String normalizedExtension = normalize(extension);
        String contentType = ALLOWED_CONTENT_TYPES.get(normalizedExtension);
        String objectKey = "%s/%s.%s".formatted(namespace, UUID.randomUUID(), normalizedExtension);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(imageProperties.bucket())
                .key(imageProperties.toStorageKey(objectKey))
                .contentType(contentType)
                .contentLength(size)
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(
                PutObjectPresignRequest.builder()
                        .signatureDuration(imageProperties.uploadUrlTtl())
                        .putObjectRequest(putObjectRequest)
                        .build()
        );

        return ImageUploadUrl.of(objectKey, presignedRequest.url().toString(), contentType);
    }

    private void validateNamespace(String namespace) {
        if (namespace == null || namespace.isBlank() || namespace.contains("/")) {
            throw new ClassitdaException(CommonErrorCode.INVALID_IMAGE_NAMESPACE);
        }
    }

    private void validateSize(long size) {
        if (size <= 0 || size > MAX_UPLOAD_SIZE_BYTES) {
            throw new ClassitdaException(CommonErrorCode.IMAGE_SIZE_EXCEEDED);
        }
    }

    private String normalize(String extension) {
        if (extension == null || extension.isBlank()) {
            throw new ClassitdaException(CommonErrorCode.INVALID_IMAGE_EXTENSION);
        }

        String normalized = extension.trim().toLowerCase(Locale.ROOT);
        if (!ALLOWED_CONTENT_TYPES.containsKey(normalized)) {
            throw new ClassitdaException(CommonErrorCode.INVALID_IMAGE_EXTENSION);
        }

        return normalized;
    }
}
