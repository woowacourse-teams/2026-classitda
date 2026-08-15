package com.classitda.classes.fixture;

import com.classitda.classes.domain.ClassForm;
import com.classitda.classes.domain.ClassSession;
import com.classitda.classes.domain.ClassSessionClassType;
import com.classitda.classes.domain.ClassSessionStatus;
import com.classitda.classes.presentation.dto.ClassSessionCreateRequest;
import com.classitda.member.domain.Member;
import com.classitda.member.fixture.MemberFixture;
import com.classitda.studio.domain.MembershipStatus;
import com.classitda.studio.domain.Studio;
import com.classitda.studio.domain.StudioMembership;
import com.classitda.studio.domain.SystemRole;
import com.classitda.studio.fixture.StudioFixture;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public final class ClassSessionFixture {

    private ClassSessionFixture() {
    }

    public static ClassSession 기본_수업_회차() {
        return 상태가_다른_수업_회차(ClassSessionStatus.OPENED);
    }

    public static ClassSession 상태가_다른_수업_회차(ClassSessionStatus status) {
        return 수업_회차(
                1L,
                기본_담당_강사_소속(),
                "저녁 요가",
                "퇴근 후 진행하는 수업",
                ClassForm.GROUP,
                60,
                12,
                LocalDateTime.of(2026, 8, 17, 20, 0),
                status
        );
    }

    public static ClassSession 수업_회차(
            Long studioId,
            StudioMembership instructorMembership,
            String name,
            String description,
            ClassForm classForm,
            int durationMinutes,
            int capacity,
            LocalDateTime startAt,
            ClassSessionStatus status
    ) {
        return ClassSession.builder()
                .studioId(studioId)
                .instructorMembership(instructorMembership)
                .name(name)
                .description(description)
                .classForm(classForm)
                .durationMinutes(durationMinutes)
                .capacity(capacity)
                .startAt(startAt)
                .status(status)
                .build();
    }

    public static StudioMembership 기본_담당_강사_소속() {
        Member instructor = MemberFixture.기본_회원();
        Studio studio = StudioFixture.기본_시설(instructor);
        return StudioMembership.builder()
                .studio(studio)
                .member(instructor)
                .studioRole(SystemRole.INSTRUCTOR.toStudioRole(studio))
                .status(MembershipStatus.ACTIVE)
                .joinedAt(LocalDateTime.of(2026, 8, 1, 9, 0))
                .build();
    }

    public static ClassSessionClassType 수업_종류_연결(Long classSessionId, Long classTypeId) {
        return ClassSessionClassType.builder()
                .classSessionId(classSessionId)
                .classTypeId(classTypeId)
                .build();
    }

    public static ClassSessionCreateRequest 기본_단일_수업_회차_생성_요청(Long classTypeId) {
        return 수업_회차_생성_요청(
                null,
                ClassForm.GROUP,
                classTypeId,
                "저녁 요가",
                12,
                60,
                false,
                LocalTime.of(20, 0),
                "퇴근 후 진행하는 수업",
                LocalDate.of(2026, 8, 17),
                null,
                null,
                null
        );
    }

    public static ClassSessionCreateRequest 기본_반복_수업_회차_생성_요청(Long classTypeId) {
        return 수업_회차_생성_요청(
                null,
                ClassForm.GROUP,
                classTypeId,
                "저녁 요가",
                12,
                60,
                true,
                LocalTime.of(20, 0),
                "퇴근 후 진행하는 수업",
                null,
                List.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
                LocalDate.of(2026, 8, 17),
                LocalDate.of(2026, 8, 24)
        );
    }

    public static ClassSessionCreateRequest 수업_회차_생성_요청(
            Long classTemplateId,
            ClassForm classForm,
            Long classTypeId,
            String name,
            Integer capacity,
            Integer durationMinutes,
            Boolean recurring,
            LocalTime startTime,
            String memo,
            LocalDate classDate,
            List<DayOfWeek> recurringDays,
            LocalDate repeatStartDate,
            LocalDate repeatEndDate
    ) {
        return ClassSessionCreateRequest.of(
                classTemplateId,
                classForm,
                classTypeId,
                name,
                capacity,
                durationMinutes,
                recurring,
                startTime,
                memo,
                classDate,
                recurringDays,
                repeatStartDate,
                repeatEndDate
        );
    }
}
