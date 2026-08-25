package com.classitda.classes.presentation;

import com.classitda.authentication.presentation.annotation.CurrentMemberId;
import com.classitda.classes.application.ClassSessionQueryService;
import com.classitda.classes.application.student.calendar.StudentCalendarQueryService;
import com.classitda.classes.application.student.daily.StudentDailyQueryService;
import com.classitda.classes.presentation.dto.ClassSessionDetailResponse;
import com.classitda.classes.presentation.dto.MemberClassSessionListRequest;
import com.classitda.classes.presentation.dto.MemberClassSessionResponse;
import com.classitda.classes.presentation.dto.StudentCalendarListRequest;
import com.classitda.classes.presentation.dto.StudentCalendarResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/studios/{studioId}/class-sessions")
@RestController
public class ClassSessionController implements ClassSessionControllerApi {

    private final ClassSessionQueryService classSessionQueryService;
    private final StudentDailyQueryService studentDailyQueryService;
    private final StudentCalendarQueryService studentCalendarQueryService;

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
        return studentCalendarQueryService.findAll(memberId, studioId, request.from(), request.to()).stream()
                .map(StudentCalendarResponse::from)
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
