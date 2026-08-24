package com.classitda.classes.presentation;

import com.classitda.authentication.presentation.annotation.CurrentMemberId;
import com.classitda.classes.application.instructor.enrollment.ClassSessionInstructorEnrollmentCommandService;
import com.classitda.classes.presentation.dto.InstructorEnrollmentCreateRequest;
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
@RequestMapping("/api/studios/{studioId}/class-sessions/{classSessionId}/enrollments")
@RestController
public class InstructorEnrollmentController implements InstructorEnrollmentControllerApi {

    private final ClassSessionInstructorEnrollmentCommandService commandService;

    @Override
    @PostMapping(version = "1")
    public ResponseEntity<Void> save(
            @CurrentMemberId Long memberId,
            @PathVariable Long studioId,
            @PathVariable Long classSessionId,
            @Valid @RequestBody InstructorEnrollmentCreateRequest request
    ) {
        commandService.save(memberId, studioId, classSessionId, request.membershipId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    @DeleteMapping(path = "/{enrollmentId}", version = "1")
    public ResponseEntity<Void> cancel(
            @CurrentMemberId Long memberId,
            @PathVariable Long studioId,
            @PathVariable Long classSessionId,
            @PathVariable Long enrollmentId
    ) {
        commandService.cancel(memberId, studioId, classSessionId, enrollmentId);
        return ResponseEntity.noContent().build();
    }
}
