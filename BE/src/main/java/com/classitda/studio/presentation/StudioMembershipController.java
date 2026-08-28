package com.classitda.studio.presentation;

import com.classitda.authentication.presentation.annotation.CurrentMemberId;
import com.classitda.common.pagination.CursorResponse;
import com.classitda.studio.application.StudioMembershipService;
import com.classitda.studio.presentation.dto.StudioMembershipCreateRequest;
import com.classitda.studio.presentation.dto.StudioMembershipUpdateRequest;
import com.classitda.studio.presentation.dto.StudioMembershipResponse;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/studios/{studioId}/memberships")
@RestController
public class StudioMembershipController implements StudioMembershipControllerApi {

    private final StudioMembershipService studioMembershipService;

    @Override
    @PostMapping(path = "/students", version = "1")
    public ResponseEntity<Void> saveStudent(
            @CurrentMemberId Long memberId,
            @PathVariable Long studioId,
            @Valid @RequestBody StudioMembershipCreateRequest request
    ) {
        studioMembershipService.saveStudent(memberId, studioId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    @PostMapping(path = "/instructors", version = "1")
    public ResponseEntity<Void> saveInstructor(
            @CurrentMemberId Long memberId,
            @PathVariable Long studioId,
            @Valid @RequestBody StudioMembershipCreateRequest request
    ) {
        studioMembershipService.saveInstructor(memberId, studioId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    @GetMapping(path = "/students", version = "1")
    public CursorResponse<StudioMembershipResponse> findStudentsWithCursor(
            @CurrentMemberId Long memberId,
            @PathVariable Long studioId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "10") int size
    ) {
        return studioMembershipService.findStudentsWithCursor(memberId, studioId, cursor, size);
    }

    @Override
    @GetMapping(path = "/instructors", version = "1")
    public CursorResponse<StudioMembershipResponse> findInstructorsWithCursor(
            @CurrentMemberId Long memberId,
            @PathVariable Long studioId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "10") int size
    ) {
        return studioMembershipService.findInstructorsWithCursor(memberId, studioId, cursor, size);
    }

    @Override
    @GetMapping(path = "/{membershipId}", version = "1")
    public StudioMembershipResponse findById(
            @CurrentMemberId Long memberId,
            @PathVariable Long studioId,
            @PathVariable Long membershipId
    ) {
        return studioMembershipService.findById(memberId, studioId, membershipId);
    }
    @Override
    @PatchMapping(path = "/{membershipId}", version = "1")
    public ResponseEntity<Void> update(
            @CurrentMemberId Long memberId,
            @PathVariable Long studioId,
            @PathVariable Long membershipId,
            @Valid @RequestBody StudioMembershipUpdateRequest request
    ) {
        studioMembershipService.update(memberId, studioId, membershipId, request);
        return ResponseEntity.noContent().build();
    }

    @Override
    @DeleteMapping(path = "/{membershipId}", version = "1")
    public ResponseEntity<Void> delete(
            @CurrentMemberId Long memberId,
            @PathVariable Long studioId,
            @PathVariable Long membershipId
    ) {
        studioMembershipService.delete(memberId, studioId, membershipId);
        return ResponseEntity.noContent().build();
    }
}
