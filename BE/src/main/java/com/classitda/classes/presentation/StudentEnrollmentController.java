package com.classitda.classes.presentation;

import com.classitda.authentication.presentation.annotation.CurrentMemberId;
import com.classitda.classes.application.student.enrollment.StudentEnrollmentDetailQueryService;
import com.classitda.classes.presentation.dto.StudentEnrollmentDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/studios/{studioId}/student/class-sessions")
@RestController
public class StudentEnrollmentController implements StudentEnrollmentControllerApi {

    private final StudentEnrollmentDetailQueryService queryService;

    @Override
    @GetMapping(path = "/{classSessionId}/enrollments/{enrollmentId}", version = "1")
    public StudentEnrollmentDetailResponse findOne(
            @CurrentMemberId Long memberId,
            @PathVariable Long studioId,
            @PathVariable Long classSessionId,
            @PathVariable Long enrollmentId
    ) {
        return StudentEnrollmentDetailResponse.from(queryService.findOne(memberId, studioId, classSessionId, enrollmentId));
    }
}
