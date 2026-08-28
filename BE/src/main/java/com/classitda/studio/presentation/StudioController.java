package com.classitda.studio.presentation;

import com.classitda.authentication.presentation.annotation.CurrentMemberId;
import com.classitda.studio.application.StudioImageService;
import com.classitda.studio.application.StudioService;
import com.classitda.studio.presentation.dto.StudioCreateRequest;
import com.classitda.common.image.ImageUploadUrlRequest;
import com.classitda.common.image.ImageUploadUrlResponse;
import com.classitda.studio.presentation.dto.StudioResponse;
import com.classitda.studio.presentation.dto.StudioUpdateRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/studios")
@RestController
public class StudioController implements StudioControllerApi {

    private final StudioService studioService;
    private final StudioImageService studioImageService;

    @Override
    @PostMapping(version = "1")
    public ResponseEntity<Void> save(
            @CurrentMemberId Long memberId,
            @Valid @RequestBody StudioCreateRequest request
    ) {
        studioService.save(memberId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    @PostMapping(path = "/image-upload-url", version = "1")
    public ImageUploadUrlResponse issueImageUploadUrl(
            @Valid @RequestBody ImageUploadUrlRequest request
    ) {
        return ImageUploadUrlResponse.from(studioImageService.issueUploadUrl(request));
    }

    @Override
    @GetMapping(path = "/me", version = "1")
    public List<StudioResponse> findAllByMemberId(
            @CurrentMemberId Long memberId
    ) {
        return studioService.findAllByMemberId(memberId);
    }

    @Override
    @GetMapping(path = "/{studioId}", version = "1")
    public StudioResponse findById(
            @PathVariable Long studioId
    ) {
        return studioService.findById(studioId);
    }

    @Override
    @PatchMapping(path = "/{studioId}", version = "1")
    public ResponseEntity<Void> update(
            @CurrentMemberId Long memberId,
            @PathVariable Long studioId,
            @Valid @RequestBody StudioUpdateRequest request
    ) {
        studioService.update(memberId, studioId, request);
        return ResponseEntity.noContent().build();
    }

    @Override
    @DeleteMapping(path = "/{studioId}/image", version = "1")
    public ResponseEntity<Void> deleteImage(
            @CurrentMemberId Long memberId,
            @PathVariable Long studioId
    ) {
        studioService.deleteImage(memberId, studioId);
        return ResponseEntity.noContent().build();
    }
}
