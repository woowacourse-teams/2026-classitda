package com.classitda.studio.presentation;

import com.classitda.studio.application.StudioPolicyService;
import com.classitda.studio.presentation.dto.StudioPolicyCreateRequest;
import com.classitda.studio.presentation.dto.StudioPolicyResponse;
import com.classitda.studio.presentation.dto.StudioPolicyUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/studios/{studioId}/policy")
@RestController
public class StudioPolicyController implements StudioPolicyControllerApi {

    private final StudioPolicyService studioPolicyService;

    @Override
    @PostMapping(version = "1")
    public ResponseEntity<StudioPolicyResponse> save(
            @RequestHeader("X-Member-Id") Long memberId,
            @PathVariable Long studioId,
            @Valid @RequestBody StudioPolicyCreateRequest request
    ) {
        StudioPolicyResponse response = studioPolicyService.save(memberId, studioId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    @Override
    @GetMapping(version = "1")
    public StudioPolicyResponse findByStudioId(@PathVariable Long studioId) {
        return studioPolicyService.findByStudioId(studioId);
    }

    @Override
    @PatchMapping(version = "1")
    public StudioPolicyResponse update(
            @RequestHeader("X-Member-Id") Long memberId,
            @PathVariable Long studioId,
            @Valid @RequestBody StudioPolicyUpdateRequest request
    ) {
        return studioPolicyService.update(memberId, studioId, request);
    }
}
