package com.classitda.common.image;

public record ImageUploadUrl(String objectKey, String uploadUrl, String contentType) {

    public static ImageUploadUrl of(String objectKey, String uploadUrl, String contentType) {
        return new ImageUploadUrl(objectKey, uploadUrl, contentType);
    }
}
