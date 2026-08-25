package com.classitda.studio.application;

import com.classitda.common.image.ImageUploadUrl;
import com.classitda.common.image.ImageUploadUrlIssuer;
import com.classitda.common.image.ImageUploadUrlRequest;
import com.classitda.studio.domain.Studio;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class StudioImageService {

    private final ImageUploadUrlIssuer imageUploadUrlIssuer;

    public ImageUploadUrl issueUploadUrl(ImageUploadUrlRequest request) {
        return imageUploadUrlIssuer.issue(
                Studio.IMAGE_KEY_NAMESPACE, request.extension(), request.size());
    }
}
