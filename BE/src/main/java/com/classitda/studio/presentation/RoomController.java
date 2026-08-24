package com.classitda.studio.presentation;

import com.classitda.common.pagination.CursorResponse;
import com.classitda.authentication.presentation.annotation.CurrentMemberId;
import com.classitda.studio.application.RoomService;
import com.classitda.studio.presentation.dto.RoomCreateRequest;
import com.classitda.studio.presentation.dto.RoomResponse;
import com.classitda.studio.presentation.dto.RoomUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/studios/{studioId}/rooms")
@RestController
public class RoomController implements RoomControllerApi {

    private final RoomService roomService;

    @Override
    @PostMapping(version = "1")
    public ResponseEntity<Void> save(
            @CurrentMemberId Long memberId,
            @PathVariable Long studioId,
            @Valid @RequestBody RoomCreateRequest request
    ) {
        roomService.save(memberId, studioId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    @GetMapping(version = "1")
    public CursorResponse<RoomResponse> findWithCursor(
            @PathVariable Long studioId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "10") int size
    ) {
        return roomService.findWithCursor(studioId, cursor, size);
    }

    @Override
    @PatchMapping(path = "/{roomId}", version = "1")
    public ResponseEntity<Void> update(
            @CurrentMemberId Long memberId,
            @PathVariable Long studioId,
            @PathVariable Long roomId,
            @Valid @RequestBody RoomUpdateRequest request
    ) {
        roomService.update(memberId, studioId, roomId, request);
        return ResponseEntity.noContent().build();
    }
}
