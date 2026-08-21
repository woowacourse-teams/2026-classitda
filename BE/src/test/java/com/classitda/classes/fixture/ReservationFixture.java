package com.classitda.classes.fixture;

import com.classitda.classes.domain.ClassForm;
import com.classitda.classes.domain.ClassGuest;
import com.classitda.classes.domain.ClassSession;
import com.classitda.classes.domain.ClassSessionStatus;
import com.classitda.classes.domain.Reservation;
import com.classitda.classes.presentation.dto.ReservationCreateRequest;
import com.classitda.studio.domain.MembershipStatus;
import com.classitda.studio.domain.Studio;
import com.classitda.studio.domain.StudioMembership;
import com.classitda.studio.domain.SystemRole;
import com.classitda.studio.fixture.StudioFixture;
import java.time.LocalDateTime;

public class ReservationFixture {

    public static final String 기본_비회원_이름 = "현장 방문 손님";
    public static final String 기본_비회원_연락처 = "01012345678";
    public static final LocalDateTime 기준_시각 = LocalDateTime.of(2026, 9, 1, 10, 0);
    public static final int 기본_정원 = 5;
    public static final int 기본_진행_시간 = 60;

    public static Studio 기본_시설() {
        return StudioFixture.기본_시설(StudioFixture.기본_소유자());
    }

    public static StudioMembership 기본_소속(Studio studio, SystemRole systemRole) {
        return 이름이_다른_소속(studio, systemRole, "김철수");
    }

    public static StudioMembership 이름이_다른_소속(Studio studio, SystemRole systemRole, String name) {
        return StudioMembership.builder()
                .studio(studio)
                .member(StudioFixture.기본_소유자())
                .name(name)
                .studioRole(systemRole.toStudioRole(studio))
                .status(MembershipStatus.ACTIVE)
                .joinedAt(기준_시각)
                .build();
    }

    public static ReservationCreateRequest 회원_등록_요청(Long membershipId) {
        return ReservationCreateRequest.forMember(membershipId);
    }

    public static ReservationCreateRequest 비회원_등록_요청() {
        return ReservationCreateRequest.forGuest(기본_비회원_이름, 기본_비회원_연락처);
    }

    public static ReservationCreateRequest 이름이_다른_비회원_등록_요청(String guestName) {
        return ReservationCreateRequest.forGuest(guestName, null);
    }

    public static ClassGuest 기본_비회원(Studio studio) {
        return 이름이_다른_비회원(studio, 기본_비회원_이름);
    }

    public static ClassGuest 이름이_다른_비회원(Studio studio, String name) {
        return ClassGuest.builder()
                .studio(studio)
                .name(name)
                .phoneNumber(기본_비회원_연락처)
                .build();
    }

    public static ClassSession 기본_수업(Long studioId, StudioMembership instructorMembership) {
        return 시작_시각이_다른_수업(studioId, instructorMembership, 기준_시각.plusDays(1));
    }

    public static ClassSession 시작_시각이_다른_수업(
            Long studioId,
            StudioMembership instructorMembership,
            LocalDateTime startAt
    ) {
        return 정원이_다른_수업(studioId, instructorMembership, startAt, 기본_정원);
    }

    public static ClassSession 정원이_다른_수업(
            Long studioId,
            StudioMembership instructorMembership,
            LocalDateTime startAt,
            int capacity
    ) {
        return ClassSession.builder()
                .studioId(studioId)
                .instructorMembership(instructorMembership)
                .name("아침 요가")
                .description("담당 강사 예약 관리 검증용")
                .classForm(ClassForm.GROUP)
                .durationMinutes(기본_진행_시간)
                .capacity(capacity)
                .startAt(startAt)
                .status(ClassSessionStatus.OPENED)
                .build();
    }

    public static Reservation 회원_예약(StudioMembership membership, ClassSession classSession) {
        return Reservation.builder()
                .membership(membership)
                .classSession(classSession)
                .reservedAt(기준_시각)
                .build();
    }

    public static Reservation 비회원_예약(ClassGuest classGuest, ClassSession classSession) {
        return Reservation.builder()
                .classGuest(classGuest)
                .classSession(classSession)
                .reservedAt(기준_시각)
                .build();
    }
}
