package com.classitda.common.image;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "images")
public record ImageProperties(
        String bucket,
        String keyPrefix,
        String baseUrl,
        String region,
        Duration uploadUrlTtl
) {
    public ImageProperties {
        if (bucket == null || bucket.isBlank()) {
            throw new IllegalArgumentException("이미지 버킷 설정은 필수입니다.");
        }
        if (keyPrefix == null || keyPrefix.isBlank()) {
            throw new IllegalArgumentException("이미지 키 접두사 설정은 필수입니다.");
        }
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("이미지 base URL 설정은 필수입니다.");
        }
        if (region == null || region.isBlank()) {
            throw new IllegalArgumentException("이미지 리전 설정은 필수입니다.");
        }
        if (uploadUrlTtl == null || uploadUrlTtl.isNegative() || uploadUrlTtl.isZero()) {
            throw new IllegalArgumentException("업로드 URL 유효 시간 설정은 필수입니다.");
        }
    }

    public String toStorageKey(String objectKey) {
        return "%s/%s".formatted(trimSlash(keyPrefix), objectKey);
    }

    public String toPublicUrl(String objectKey) {
        return "%s/%s".formatted(trimSlash(baseUrl), objectKey);
    }

    private String trimSlash(String value) {
        if (value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }
}
