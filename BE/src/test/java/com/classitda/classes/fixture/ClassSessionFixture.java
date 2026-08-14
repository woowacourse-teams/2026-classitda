package com.classitda.classes.fixture;

import com.classitda.classes.domain.ClassForm;
import com.classitda.classes.domain.ClassSession;
import com.classitda.classes.domain.ClassSessionClassType;
import com.classitda.classes.domain.ClassSessionStatus;
import com.classitda.member.domain.Member;
import com.classitda.member.fixture.MemberFixture;
import com.classitda.studio.domain.MembershipStatus;
import com.classitda.studio.domain.Studio;
import com.classitda.studio.domain.StudioMembership;
import com.classitda.studio.domain.SystemRole;
import com.classitda.studio.fixture.StudioFixture;
import java.time.LocalDateTime;

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
}
