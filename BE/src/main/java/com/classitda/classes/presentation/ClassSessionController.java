package com.classitda.classes.presentation;

import com.classitda.authentication.presentation.annotation.CurrentMemberId;
import com.classitda.classes.application.ClassSessionCommandService;
import com.classitda.classes.application.ClassSessionQueryService;
import com.classitda.classes.application.instructor.daily.InstructorDailyQueryService;
import com.classitda.classes.application.student.StudentSessionQueryService;
import com.classitda.classes.presentation.dto.ClassSessionCreateRequest;
import com.classitda.classes.presentation.dto.ClassSessionDetailResponse;
import com.classitda.classes.presentation.dto.InstructorDailySessionListRequest;
import com.classitda.classes.presentation.dto.InstructorDailySessionResponse;
import com.classitda.classes.presentation.dto.MemberClassSessionListRequest;
import com.classitda.classes.presentation.dto.MemberClassSessionResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/studios/{studioId}/class-sessions")
@RestController
public class ClassSessionController implements ClassSessionControllerApi {

    private final ClassSessionCommandService classSessionCommandService;
    private final ClassSessionQueryService classSessionQueryService;
    private final StudentSessionQueryService studentSessionQueryService;
    private final InstructorDailyQueryService instructorDailyQueryService;

    @Override
    @PostMapping(version = "1")
    public ResponseEntity<Void> save(
            @CurrentMemberId Long memberId,
            @PathVariable Long studioId,
            @Valid @RequestBody ClassSessionCreateRequest request
    ) {
        classSessionCommandService.save(memberId, studioId, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    @GetMapping(path = "/student/daily", version = "1")
    public List<MemberClassSessionResponse> findAllForStudent(
            @CurrentMemberId Long memberId,
            @PathVariable Long studioId,
            @Valid @ModelAttribute MemberClassSessionListRequest request
    ) {
        return studentSessionQueryService.findAll(
                memberId,
                studioId,
                request.date(),
                request.memberPassProductId()
        );
    }

    @Override
    @GetMapping(path = "/instructor/daily", version = "1")
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
    @GetMapping(path = "/{classSessionId}", version = "1")
    public ClassSessionDetailResponse findOne(
            @CurrentMemberId Long memberId,
            @PathVariable Long studioId,
            @PathVariable Long classSessionId
    ) {
        return classSessionQueryService.findOne(memberId, studioId, classSessionId);
    }
}
