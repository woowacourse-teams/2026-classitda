package com.classitda.studio.presentation;

import com.classitda.studio.application.StudioService;
import com.classitda.studio.presentation.dto.StudioCreateRequest;
import com.classitda.studio.presentation.dto.StudioResponse;
import com.classitda.studio.presentation.dto.StudioUpdateRequest;
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
@RequestMapping("/api/studios")
@RestController
public class StudioController implements StudioControllerApi {

    private final StudioService studioService;

    @Override
    @PostMapping(version = "1")
    public ResponseEntity<StudioResponse> save(
            @RequestHeader("X-Member-Id") Long memberId,
            @Valid @RequestBody StudioCreateRequest request
    ) {
        StudioResponse response = studioService.save(memberId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
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
    public StudioResponse update(
            @RequestHeader("X-Member-Id") Long memberId,
            @PathVariable Long studioId,
            @Valid @RequestBody StudioUpdateRequest request
    ) {
        return studioService.update(memberId, studioId, request);
    }
}
