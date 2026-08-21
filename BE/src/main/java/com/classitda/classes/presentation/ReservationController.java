package com.classitda.classes.presentation;

import com.classitda.authentication.presentation.annotation.CurrentMemberId;
import com.classitda.classes.application.ReservationService;
import com.classitda.classes.presentation.dto.ReservationCreateRequest;
import com.classitda.classes.presentation.dto.ReservationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/studios/{studioId}/class-sessions/{classSessionId}/reservations")
@RestController
public class ReservationController implements ReservationControllerApi {

    private final ReservationService reservationService;

    @Override
    @PostMapping(version = "1")
    public ResponseEntity<ReservationResponse> save(
            @CurrentMemberId Long memberId,
            @PathVariable Long studioId,
            @PathVariable Long classSessionId,
            @Valid @RequestBody ReservationCreateRequest request
    ) {
        ReservationResponse response = reservationService.save(memberId, studioId, classSessionId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Override
    @DeleteMapping(path = "/{reservationId}", version = "1")
    public ResponseEntity<Void> cancel(
            @CurrentMemberId Long memberId,
            @PathVariable Long studioId,
            @PathVariable Long classSessionId,
            @PathVariable Long reservationId
    ) {
        reservationService.cancel(memberId, studioId, classSessionId, reservationId);
        return ResponseEntity.noContent().build();
    }
}
