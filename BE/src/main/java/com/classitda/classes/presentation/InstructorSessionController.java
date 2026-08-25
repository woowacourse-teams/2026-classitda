package com.classitda.classes.presentation;

import com.classitda.authentication.presentation.annotation.CurrentMemberId;
import com.classitda.classes.application.ClassSessionCommandService;
import com.classitda.classes.application.instructor.calendar.InstructorCalendarQueryService;
import com.classitda.classes.application.instructor.daily.InstructorDailyQueryService;
import com.classitda.classes.application.instructor.enrollment.ClassSessionInstructorEnrollmentCommandService;
import com.classitda.classes.application.instructor.enrollment.InstructorSessionQueryService;
import com.classitda.classes.presentation.dto.ClassSessionCreateV1Request;
import com.classitda.classes.presentation.dto.ClassSessionCreateV2Request;
import com.classitda.classes.presentation.dto.ClassSessionUpdateV1Request;
import com.classitda.classes.presentation.dto.ClassSessionUpdateV2Request;
import com.classitda.classes.presentation.dto.InstructorCalendarListRequest;
import com.classitda.classes.presentation.dto.InstructorCalendarResponse;
import com.classitda.classes.presentation.dto.InstructorDailySessionListRequest;
import com.classitda.classes.presentation.dto.InstructorDailySessionResponse;
import com.classitda.classes.presentation.dto.StudioStudentResponse;
import com.classitda.classes.presentation.dto.InstructorEnrollmentCreateRequest;
import com.classitda.classes.presentation.dto.InstructorSessionDetailResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/studios/{studioId}/instructor/class-sessions")
@RestController
public class InstructorSessionController implements InstructorSessionControllerApi {

    private final ClassSessionCommandService classSessionCommandService;
    private final InstructorDailyQueryService instructorDailyQueryService;
    private final InstructorCalendarQueryService instructorCalendarQueryService;
    private final InstructorSessionQueryService instructorSessionQueryService;
    private final ClassSessionInstructorEnrollmentCommandService instructorEnrollmentCommandService;

    @Override
    @PostMapping(version = "1")
    public ResponseEntity<Void> saveV1(
            @CurrentMemberId Long memberId,
            @PathVariable Long studioId,
            @Valid @RequestBody ClassSessionCreateV1Request request
    ) {
        classSessionCommandService.saveV1(memberId, studioId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    @PostMapping(version = "2")
    public ResponseEntity<Void> saveV2(
            @CurrentMemberId Long memberId,
            @PathVariable Long studioId,
            @Valid @RequestBody ClassSessionCreateV2Request request
    ) {
        classSessionCommandService.saveV2(memberId, studioId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    @PutMapping(path = "/{classSessionId}", version = "1")
    public ResponseEntity<Void> updateV1(
            @CurrentMemberId Long memberId,
            @PathVariable Long studioId,
            @PathVariable Long classSessionId,
            @Valid @RequestBody ClassSessionUpdateV1Request request
    ) {
        classSessionCommandService.updateV1(memberId, studioId, classSessionId, request);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PutMapping(path = "/{classSessionId}", version = "2")
    public ResponseEntity<Void> updateV2(
            @CurrentMemberId Long memberId,
            @PathVariable Long studioId,
            @PathVariable Long classSessionId,
            @Valid @RequestBody ClassSessionUpdateV2Request request
    ) {
        classSessionCommandService.updateV2(memberId, studioId, classSessionId, request);
        return ResponseEntity.noContent().build();
    }

    @Override
    @DeleteMapping(path = "/{classSessionId}", version = "1")
    public ResponseEntity<Void> cancel(
            @CurrentMemberId Long memberId,
            @PathVariable Long studioId,
            @PathVariable Long classSessionId
    ) {
        classSessionCommandService.cancel(memberId, studioId, classSessionId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @GetMapping(path = "/daily", version = "1")
    public List<InstructorDailySessionResponse> findAllDailyForInstructor(
            @CurrentMemberId Long memberId,
            @PathVariable Long studioId,
            @Valid @ModelAttribute InstructorDailySessionListRequest request
    ) {
        return instructorDailyQueryService.findAll(memberId, studioId, request.date()).stream()
                .map(InstructorDailySessionResponse::from)
                .toList();
    }

    @Override
    @GetMapping(path = "/calendar", version = "1")
    public List<InstructorCalendarResponse> findAllCalendarForInstructor(
            @CurrentMemberId Long memberId,
            @PathVariable Long studioId,
            @Valid @ModelAttribute InstructorCalendarListRequest request
    ) {
        return instructorCalendarQueryService.findAll(memberId, studioId, request.from(), request.to()).stream()
                .map(InstructorCalendarResponse::from)
                .toList();
    }

    @Override
    @GetMapping(path = "/{classSessionId}", version = "1")
    public InstructorSessionDetailResponse findDetail(
            @CurrentMemberId Long memberId,
            @PathVariable Long studioId,
            @PathVariable Long classSessionId
    ) {
        return InstructorSessionDetailResponse.from(
                instructorSessionQueryService.findDetail(memberId, studioId, classSessionId)
        );
    }

    @Override
    @GetMapping(path = "/{classSessionId}/studio-students", version = "1")
    public List<StudioStudentResponse> findAllStudioStudents(
            @CurrentMemberId Long memberId,
            @PathVariable Long studioId,
            @PathVariable Long classSessionId
    ) {
        return instructorSessionQueryService.findAllStudioStudents(memberId, studioId, classSessionId).stream()
                .map(StudioStudentResponse::from)
                .toList();
    }

    @Override
    @PostMapping(path = "/{classSessionId}/enrollments", version = "1")
    public ResponseEntity<Void> save(
            @CurrentMemberId Long memberId,
            @PathVariable Long studioId,
            @PathVariable Long classSessionId,
            @Valid @RequestBody InstructorEnrollmentCreateRequest request
    ) {
        instructorEnrollmentCommandService.save(memberId, studioId, classSessionId, request.membershipId());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    @DeleteMapping(path = "/{classSessionId}/enrollments/{enrollmentId}", version = "1")
    public ResponseEntity<Void> cancel(
            @CurrentMemberId Long memberId,
            @PathVariable Long studioId,
            @PathVariable Long classSessionId,
            @PathVariable Long enrollmentId
    ) {
        instructorEnrollmentCommandService.cancel(memberId, studioId, classSessionId, enrollmentId);
        return ResponseEntity.noContent().build();
    }
}
