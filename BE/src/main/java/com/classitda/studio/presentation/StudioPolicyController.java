package com.classitda.studio.presentation;

import com.classitda.authentication.presentation.annotation.CurrentMemberId;
import com.classitda.studio.application.StudioPolicyService;
import com.classitda.studio.presentation.dto.StudioPolicyResponse;
import com.classitda.studio.presentation.dto.StudioPolicyUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/studios/{studioId}/policy")
@RestController
public class StudioPolicyController implements StudioPolicyControllerApi {

    private final StudioPolicyService studioPolicyService;

    @Override
    @GetMapping(version = "1")
    public StudioPolicyResponse findByStudioId(@PathVariable Long studioId) {
        return studioPolicyService.findByStudioId(studioId);
    }

    @Override
    @PatchMapping(version = "1")
    public ResponseEntity<Void> update(
            @CurrentMemberId Long memberId,
            @PathVariable Long studioId,
            @Valid @RequestBody StudioPolicyUpdateRequest request
    ) {
        studioPolicyService.update(memberId, studioId, request);
        return ResponseEntity.noContent().build();
    }
}
