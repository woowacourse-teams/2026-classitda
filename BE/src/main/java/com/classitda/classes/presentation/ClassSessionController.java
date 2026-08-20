package com.classitda.classes.presentation;

import com.classitda.authentication.presentation.annotation.CurrentMemberId;
import com.classitda.classes.application.ClassSessionCommandService;
import com.classitda.classes.application.ClassSessionQueryService;
import com.classitda.classes.application.instructor.calendar.InstructorCalendarQueryService;
import com.classitda.classes.application.instructor.daily.InstructorDailyQueryService;
import com.classitda.classes.application.student.calendar.StudentCalendarQueryService;
import com.classitda.classes.application.student.daily.StudentDailyQueryService;
import com.classitda.classes.presentation.dto.ClassSessionCreateRequest;
import com.classitda.classes.presentation.dto.ClassSessionDetailResponse;
import com.classitda.classes.presentation.dto.InstructorCalendarListRequest;
import com.classitda.classes.presentation.dto.InstructorCalendarResponse;
import com.classitda.classes.presentation.dto.InstructorDailySessionListRequest;
import com.classitda.classes.presentation.dto.InstructorDailySessionResponse;
import com.classitda.classes.presentation.dto.MemberClassSessionListRequest;
import com.classitda.classes.presentation.dto.MemberClassSessionResponse;
import com.classitda.classes.presentation.dto.StudentCalendarListRequest;
import com.classitda.classes.presentation.dto.StudentCalendarResponse;
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
    private final StudentDailyQueryService studentDailyQueryService;
    private final StudentCalendarQueryService studentCalendarQueryService;
    private final InstructorDailyQueryService instructorDailyQueryService;
    private final InstructorCalendarQueryService instructorCalendarQueryService;

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
    // TODO(#68): 홀딩 기능 구현 후 홀딩 수강권 정보와 수업 목록을 함께 반환하는 응답 객체로 확장한다.
    public List<MemberClassSessionResponse> findAllForStudent(
            @CurrentMemberId Long memberId,
            @PathVariable Long studioId,
            @Valid @ModelAttribute MemberClassSessionListRequest request
    ) {
        return studentDailyQueryService.findAll(memberId, studioId, request.date()).stream()
                .map(MemberClassSessionResponse::from)
                .toList();
    }

    @Override
    @GetMapping(path = "/student/calendar", version = "1")
    public List<StudentCalendarResponse> findAllCalendarForStudent(
            @CurrentMemberId Long memberId,
            @PathVariable Long studioId,
            @Valid @ModelAttribute StudentCalendarListRequest request
    ) {
        return studentCalendarQueryService.findAll(
                        memberId,
                        studioId,
                        request.from(),
                        request.to()
                ).stream()
                .map(StudentCalendarResponse::from)
                .toList();
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
    @GetMapping(path = "/instructor/calendar", version = "1")
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
    @GetMapping(path = "/student/{classSessionId}", version = "1")
    public ClassSessionDetailResponse findOneForStudent(
            @CurrentMemberId Long memberId,
            @PathVariable Long studioId,
            @PathVariable Long classSessionId
    ) {
        return classSessionQueryService.findOne(memberId, studioId, classSessionId);
    }
}
