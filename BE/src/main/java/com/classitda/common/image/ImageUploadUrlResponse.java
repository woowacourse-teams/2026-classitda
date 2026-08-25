package com.classitda.common.image;

public record ImageUploadUrlResponse(String objectKey, String uploadUrl, String contentType) {

    public static ImageUploadUrlResponse from(ImageUploadUrl uploadUrl) {
        return new ImageUploadUrlResponse(
                uploadUrl.objectKey(),
                uploadUrl.uploadUrl(),
                uploadUrl.contentType()
        );
    }
}
