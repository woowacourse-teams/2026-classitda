package com.classitda.classes.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import com.classitda.classes.application.student.StudentBookingStatusResolver;
import com.classitda.classes.application.student.StudentSessionAccessReader;
import com.classitda.classes.application.student.StudentSessionAssembler;
import com.classitda.classes.application.student.StudentSessionQueryService;
import com.classitda.classes.application.student.StudentSessionScheduleReader;
import com.classitda.classes.domain.ClassForm;
import com.classitda.classes.domain.ClassSession;
import com.classitda.classes.domain.ClassSessionStatus;
import com.classitda.classes.domain.ClassType;
import com.classitda.classes.domain.Reservation;
import com.classitda.classes.domain.ReservationStatus;
import com.classitda.classes.domain.Waiting;
import com.classitda.classes.domain.WaitingStatus;
import com.classitda.classes.domain.repository.ClassSessionClassTypeRepository;
import com.classitda.classes.domain.repository.ClassSessionRepository;
import com.classitda.classes.domain.repository.ClassTypeRepository;
import com.classitda.classes.exception.ClassErrorCode;
import com.classitda.classes.exception.ClassException;
import com.classitda.classes.fixture.ClassSessionFixture;
import com.classitda.classes.fixture.ClassTypeFixture;
import com.classitda.classes.presentation.dto.ClassSessionDetailResponse;
import com.classitda.classes.presentation.dto.ClassTypeResponse;
import com.classitda.classes.presentation.dto.MemberClassSessionBookingStatus;
import com.classitda.classes.presentation.dto.MemberClassSessionResponse;
import com.classitda.member.domain.Member;
import com.classitda.passproduct.domain.ClassKind;
import com.classitda.passproduct.domain.MemberPassProduct;
import com.classitda.passproduct.domain.MemberPassProductStatus;
import com.classitda.passproduct.domain.PassProduct;
import com.classitda.passproduct.domain.PassProductPeriodUnit;
import com.classitda.passproduct.exception.PassProductErrorCode;
import com.classitda.passproduct.exception.PassProductException;
import com.classitda.studio.domain.MembershipStatus;
import com.classitda.studio.domain.Studio;
import com.classitda.studio.domain.StudioMembership;
import com.classitda.studio.domain.StudioPolicy;
import com.classitda.studio.domain.StudioRole;
import com.classitda.studio.domain.SystemRole;
import com.classitda.studio.exception.StudioErrorCode;
import com.classitda.studio.exception.StudioException;
import com.classitda.studio.fixture.StudioFixture;
import com.classitda.support.MySqlRepositoryTest;
import jakarta.persistence.EntityManager;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

@Import({
        ClassSessionQueryService.class,
        StudentSessionQueryService.class,
        StudentSessionAccessReader.class,
        StudentSessionScheduleReader.class,
        StudentSessionAssembler.class,
        StudentBookingStatusResolver.class,
        ClassSessionQueryServiceTest.FixedClockConfig.class
})
@MySqlRepositoryTest
class ClassSessionQueryServiceTest {

    private static final ZoneId SERVICE_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 8, 17, 10, 0);
    private static final LocalDate QUERY_DATE = LocalDate.of(2026, 8, 17);

    private final ClassSessionQueryService queryService;
    private final StudentSessionQueryService studentSessionQueryService;
    private final ClassSessionClassTypeRepository classSessionClassTypeRepository;
    private final ClassSessionRepository classSessionRepository;
    private final ClassTypeRepository classTypeRepository;
    private final EntityManager entityManager;

    @Autowired
    ClassSessionQueryServiceTest(
            ClassSessionQueryService queryService,
            StudentSessionQueryService studentSessionQueryService,
            ClassSessionClassTypeRepository classSessionClassTypeRepository,
            ClassSessionRepository classSessionRepository,
            ClassTypeRepository classTypeRepository,
            EntityManager entityManager
    ) {
        this.queryService = queryService;
        this.studentSessionQueryService = studentSessionQueryService;
        this.classSessionClassTypeRepository = classSessionClassTypeRepository;
        this.classSessionRepository = classSessionRepository;
        this.classTypeRepository = classTypeRepository;
        this.entityManager = entityManager;
    }

    @Test
    void 선택한_수강권으로_이용할_수_있는_일별_수업을_정렬하고_인원과_상태를_반환한다() {
        // given
        Member owner = 회원을_저장한다("daily-owner");
        Studio studio = 시설을_저장한다(owner, "회원용 일별 조회 시설");
        정책을_저장한다(studio, 30);
        StudioMembership memberMembership = 소속을_저장한다(
                studio,
                회원을_저장한다("daily-member"),
                SystemRole.STUDENT,
                MembershipStatus.ACTIVE
        );
        StudioMembership firstInstructor = 소속을_저장한다(
                studio,
                회원을_저장한다("daily-first-instructor"),
                SystemRole.INSTRUCTOR,
                MembershipStatus.ACTIVE
        );
        StudioMembership secondInstructor = 소속을_저장한다(
                studio,
                회원을_저장한다("daily-second-instructor"),
                SystemRole.INSTRUCTOR,
                MembershipStatus.ACTIVE
        );
        ClassType yoga = 수업_종류를_저장한다(studio, "요가");
        ClassType pilates = 수업_종류를_저장한다(studio, "필라테스");
        MemberPassProduct memberPassProduct = 보유_수강권을_저장한다(
                memberMembership,
                수강권을_저장한다(studio, ClassKind.GROUP, List.of(yoga)),
                MemberPassProductStatus.ACTIVE,
                10,
                QUERY_DATE.minusMonths(1),
                QUERY_DATE.plusMonths(1)
        );

        ClassSession laterSession = 수업_회차를_저장한다(
                studio, firstInstructor, yoga, "오후 요가", ClassForm.GROUP, 1,
                QUERY_DATE.atTime(14, 0), ClassSessionStatus.OPENED
        );
        ClassSession sameTimeFirst = 수업_회차를_저장한다(
                studio, firstInstructor, yoga, "정오 요가 A", ClassForm.GROUP, 3,
                QUERY_DATE.atTime(12, 0), ClassSessionStatus.OPENED
        );
        ClassSession sameTimeSecond = 수업_회차를_저장한다(
                studio, secondInstructor, yoga, "정오 요가 B", ClassForm.GROUP, 2,
                QUERY_DATE.atTime(12, 0), ClassSessionStatus.OPENED
        );
        수업_회차를_저장한다(
                studio, firstInstructor, pilates, "다른 종류", ClassForm.GROUP, 10,
                QUERY_DATE.atTime(13, 0), ClassSessionStatus.OPENED
        );
        수업_회차를_저장한다(
                studio, firstInstructor, yoga, "다른 형태", ClassForm.INDIVIDUAL, 1,
                QUERY_DATE.atTime(15, 0), ClassSessionStatus.OPENED
        );
        수업_회차를_저장한다(
                studio, firstInstructor, yoga, "다른 날짜", ClassForm.GROUP, 10,
                QUERY_DATE.plusDays(1).atStartOfDay(), ClassSessionStatus.OPENED
        );

        StudioMembership firstParticipant = 소속을_저장한다(
                studio,
                회원을_저장한다("daily-first-participant"),
                SystemRole.STUDENT,
                MembershipStatus.ACTIVE
        );
        StudioMembership secondParticipant = 소속을_저장한다(
                studio,
                회원을_저장한다("daily-second-participant"),
                SystemRole.STUDENT,
                MembershipStatus.ACTIVE
        );
        StudioMembership thirdParticipant = 소속을_저장한다(
                studio,
                회원을_저장한다("daily-third-participant"),
                SystemRole.STUDENT,
                MembershipStatus.ACTIVE
        );
        예약을_저장한다(sameTimeFirst, firstParticipant, ReservationStatus.RESERVED);
        예약을_저장한다(sameTimeFirst, secondParticipant, ReservationStatus.ATTENDED);
        예약을_저장한다(sameTimeFirst, thirdParticipant, ReservationStatus.CANCELED);
        대기를_저장한다(sameTimeFirst, firstParticipant, 1, WaitingStatus.WAITING);
        대기를_저장한다(sameTimeFirst, secondParticipant, 2, WaitingStatus.OFFERED);
        대기를_저장한다(sameTimeFirst, thirdParticipant, 3, WaitingStatus.CANCELED);
        예약을_저장한다(laterSession, firstParticipant, ReservationStatus.RESERVED);
        entityManager.flush();
        entityManager.clear();

        // when
        List<MemberClassSessionResponse> responses = studentSessionQueryService.findAll(
                memberMembership.getMember().getId(),
                studio.getId(),
                QUERY_DATE,
                memberPassProduct.getId()
        );

        // then
        assertThat(responses).containsExactly(
                new MemberClassSessionResponse(
                        sameTimeFirst.getId(),
                        firstInstructor.getId(),
                        firstInstructor.getMember().getName(),
                        ClassForm.GROUP,
                        ClassTypeResponse.of(yoga.getId(), yoga.getName()),
                        "정오 요가 A",
                        "정오 요가 A 안내",
                        3,
                        2,
                        1,
                        1,
                        QUERY_DATE.atTime(12, 0),
                        QUERY_DATE.atTime(13, 0),
                        MemberClassSessionBookingStatus.AVAILABLE
                ),
                new MemberClassSessionResponse(
                        sameTimeSecond.getId(),
                        secondInstructor.getId(),
                        secondInstructor.getMember().getName(),
                        ClassForm.GROUP,
                        ClassTypeResponse.of(yoga.getId(), yoga.getName()),
                        "정오 요가 B",
                        "정오 요가 B 안내",
                        2,
                        0,
                        2,
                        0,
                        QUERY_DATE.atTime(12, 0),
                        QUERY_DATE.atTime(13, 0),
                        MemberClassSessionBookingStatus.AVAILABLE
                ),
                new MemberClassSessionResponse(
                        laterSession.getId(),
                        firstInstructor.getId(),
                        firstInstructor.getMember().getName(),
                        ClassForm.GROUP,
                        ClassTypeResponse.of(yoga.getId(), yoga.getName()),
                        "오후 요가",
                        "오후 요가 안내",
                        1,
                        1,
                        0,
                        0,
                        QUERY_DATE.atTime(14, 0),
                        QUERY_DATE.atTime(15, 0),
                        MemberClassSessionBookingStatus.WAITING_AVAILABLE
                )
        );
    }

    @Test
    void 회원용_예약_상태는_수업과_본인_상태와_마감_우선순위로_결정한다() {
        // given
        Member owner = 회원을_저장한다("status-owner");
        Studio studio = 시설을_저장한다(owner, "상태 조회 시설");
        정책을_저장한다(studio, 30);
        StudioMembership memberMembership = 소속을_저장한다(
                studio,
                회원을_저장한다("status-member"),
                SystemRole.STUDENT,
                MembershipStatus.ACTIVE
        );
        StudioMembership instructor = 소속을_저장한다(
                studio,
                회원을_저장한다("status-instructor"),
                SystemRole.INSTRUCTOR,
                MembershipStatus.ACTIVE
        );
        ClassType classType = 수업_종류를_저장한다(studio, "상태 요가");
        MemberPassProduct memberPassProduct = 보유_수강권을_저장한다(
                memberMembership,
                수강권을_저장한다(studio, ClassKind.GROUP, List.of(classType)),
                MemberPassProductStatus.ACTIVE,
                10,
                QUERY_DATE.minusDays(1),
                QUERY_DATE.plusDays(1)
        );

        ClassSession canceled = 수업_회차를_저장한다(
                studio, instructor, classType, "취소 수업", ClassForm.GROUP, 5,
                QUERY_DATE.atTime(7, 0), ClassSessionStatus.CANCELED
        );
        ClassSession completed = 수업_회차를_저장한다(
                studio, instructor, classType, "완료 수업", ClassForm.GROUP, 5,
                QUERY_DATE.atTime(8, 30), ClassSessionStatus.OPENED
        );
        ClassSession reserved = 수업_회차를_저장한다(
                studio, instructor, classType, "예약 수업", ClassForm.GROUP, 5,
                QUERY_DATE.atTime(10, 10), ClassSessionStatus.OPENED
        );
        ClassSession offered = 수업_회차를_저장한다(
                studio, instructor, classType, "제안 수업", ClassForm.GROUP, 5,
                QUERY_DATE.atTime(10, 20), ClassSessionStatus.OPENED
        );
        ClassSession waiting = 수업_회차를_저장한다(
                studio, instructor, classType, "대기 수업", ClassForm.GROUP, 5,
                QUERY_DATE.atTime(10, 25), ClassSessionStatus.OPENED
        );
        ClassSession closedAtBoundary = 수업_회차를_저장한다(
                studio, instructor, classType, "마감 수업", ClassForm.GROUP, 5,
                QUERY_DATE.atTime(10, 30), ClassSessionStatus.OPENED
        );
        ClassSession closedByStatus = 수업_회차를_저장한다(
                studio, instructor, classType, "상태 마감 수업", ClassForm.GROUP, 5,
                QUERY_DATE.atTime(11, 0), ClassSessionStatus.CLOSED
        );
        예약을_저장한다(canceled, memberMembership, ReservationStatus.RESERVED);
        예약을_저장한다(completed, memberMembership, ReservationStatus.RESERVED);
        대기를_저장한다(completed, memberMembership, 1, WaitingStatus.OFFERED);
        예약을_저장한다(reserved, memberMembership, ReservationStatus.RESERVED);
        대기를_저장한다(offered, memberMembership, 1, WaitingStatus.OFFERED);
        대기를_저장한다(waiting, memberMembership, 1, WaitingStatus.WAITING);
        entityManager.flush();
        entityManager.clear();

        // when
        List<MemberClassSessionResponse> responses = studentSessionQueryService.findAll(
                memberMembership.getMember().getId(),
                studio.getId(),
                QUERY_DATE,
                memberPassProduct.getId()
        );

        // then
        assertThat(responses)
                .extracting(MemberClassSessionResponse::id, MemberClassSessionResponse::bookingStatus)
                .containsExactly(
                        tuple(canceled.getId(), MemberClassSessionBookingStatus.CANCELED),
                        tuple(completed.getId(), MemberClassSessionBookingStatus.COMPLETED),
                        tuple(reserved.getId(), MemberClassSessionBookingStatus.RESERVED),
                        tuple(offered.getId(), MemberClassSessionBookingStatus.OFFERED),
                        tuple(waiting.getId(), MemberClassSessionBookingStatus.WAITING),
                        tuple(closedAtBoundary.getId(), MemberClassSessionBookingStatus.CLOSED),
                        tuple(closedByStatus.getId(), MemberClassSessionBookingStatus.CLOSED)
                );
    }

    @Test
    void 시설_소속이_아니면_회원용_일별_목록을_조회할_수_없다() {
        // given
        Studio studio = 시설을_저장한다(회원을_저장한다("list-stranger-owner"), "목록 비소속 시설");
        Member stranger = 회원을_저장한다("list-stranger");

        // when / then
        assertStudioError(
                () -> studentSessionQueryService.findAll(stranger.getId(), studio.getId(), QUERY_DATE, 1L),
                StudioErrorCode.NOT_MEMBERSHIP
        );
    }

    @Test
    void 비활성_소속은_회원용_일별_목록을_조회할_수_없다() {
        // given
        Studio studio = 시설을_저장한다(회원을_저장한다("list-inactive-owner"), "목록 비활성 시설");
        Member inactiveMember = 회원을_저장한다("list-inactive-member");
        소속을_저장한다(studio, inactiveMember, SystemRole.STUDENT, MembershipStatus.INACTIVE);

        // when / then
        assertStudioError(
                () -> studentSessionQueryService.findAll(
                        inactiveMember.getId(),
                        studio.getId(),
                        QUERY_DATE,
                        1L
                ),
                StudioErrorCode.MEMBERSHIP_INACTIVE
        );
    }

    @ParameterizedTest
    @EnumSource(value = SystemRole.class, names = {"OWNER", "INSTRUCTOR"})
    void 대표나_강사는_학생용_일별_목록을_조회할_수_없다(SystemRole systemRole) {
        // given
        Studio studio = 시설을_저장한다(
                회원을_저장한다("student-list-role-owner-" + systemRole),
                "학생용 역할 제한 시설 " + systemRole
        );
        Member staff = 회원을_저장한다("student-list-role-staff-" + systemRole);
        소속을_저장한다(studio, staff, systemRole, MembershipStatus.ACTIVE);

        // when / then
        assertStudioError(
                () -> studentSessionQueryService.findAll(
                        staff.getId(),
                        studio.getId(),
                        QUERY_DATE,
                        1L
                ),
                StudioErrorCode.PERMISSION_DENIED
        );
    }

    @Test
    void 커스텀_직원_역할은_학생용_일별_목록을_조회할_수_없다() {
        // given
        Studio studio = 시설을_저장한다(
                회원을_저장한다("student-list-custom-role-owner"),
                "학생용 커스텀 역할 제한 시설"
        );
        StudioRole staffRole = StudioRole.builder()
                .studio(studio)
                .name("운영 관리자")
                .instructor(true)
                .build();
        entityManager.persist(staffRole);

        Member staff = 회원을_저장한다("student-list-custom-role-staff");
        StudioMembership membership = StudioMembership.builder()
                .studio(studio)
                .member(staff)
                .studioRole(staffRole)
                .status(MembershipStatus.ACTIVE)
                .joinedAt(LocalDateTime.of(2026, 8, 1, 9, 0))
                .build();
        entityManager.persist(membership);
        entityManager.flush();

        // when / then
        assertStudioError(
                () -> studentSessionQueryService.findAll(
                        staff.getId(),
                        studio.getId(),
                        QUERY_DATE,
                        1L
                ),
                StudioErrorCode.PERMISSION_DENIED
        );
    }

    @ParameterizedTest
    @MethodSource("허용되지_않은_보유_수강권")
    void 다른_회원이나_다른_시설의_보유_수강권은_찾을_수_없는_것으로_처리한다(boolean otherStudio) {
        // given
        Member owner = 회원을_저장한다("hidden-pass-owner-" + otherStudio);
        Studio studio = 시설을_저장한다(owner, "보유 수강권 숨김 시설 " + otherStudio);
        Member member = 회원을_저장한다("hidden-pass-member-" + otherStudio);
        소속을_저장한다(studio, member, SystemRole.STUDENT, MembershipStatus.ACTIVE);

        Studio passStudio = otherStudio
                ? 시설을_저장한다(회원을_저장한다("hidden-pass-other-owner"), "다른 보유 수강권 시설")
                : studio;
        Member passOwner = otherStudio ? member : 회원을_저장한다("hidden-pass-other-member");
        StudioMembership passMembership = 소속을_저장한다(
                passStudio,
                passOwner,
                SystemRole.STUDENT,
                MembershipStatus.ACTIVE
        );
        ClassType classType = 수업_종류를_저장한다(passStudio, "숨김 수업 종류");
        MemberPassProduct hiddenPass = 보유_수강권을_저장한다(
                passMembership,
                수강권을_저장한다(passStudio, ClassKind.GROUP, List.of(classType)),
                MemberPassProductStatus.ACTIVE,
                10,
                QUERY_DATE.minusDays(1),
                QUERY_DATE.plusDays(1)
        );
        entityManager.flush();
        entityManager.clear();

        // when / then
        assertPassProductError(
                () -> studentSessionQueryService.findAll(
                        member.getId(),
                        studio.getId(),
                        QUERY_DATE,
                        hiddenPass.getId()
                ),
                PassProductErrorCode.MEMBER_PASS_PRODUCT_NOT_FOUND
        );
    }

    @ParameterizedTest
    @MethodSource("사용할_수_없는_수강권")
    void 홀딩하거나_소진한_보유_수강권으로는_목록을_조회할_수_없다(
            MemberPassProductStatus status,
            int remainingCount
    ) {
        // given
        Studio studio = 시설을_저장한다(회원을_저장한다("unusable-pass-owner-" + status), "사용 불가 수강권 시설");
        Member member = 회원을_저장한다("unusable-pass-member-" + status);
        StudioMembership membership = 소속을_저장한다(
                studio,
                member,
                SystemRole.STUDENT,
                MembershipStatus.ACTIVE
        );
        ClassType classType = 수업_종류를_저장한다(studio, "사용 불가 수업 종류");
        MemberPassProduct memberPassProduct = 보유_수강권을_저장한다(
                membership,
                수강권을_저장한다(studio, ClassKind.GROUP, List.of(classType)),
                status,
                remainingCount,
                QUERY_DATE.minusDays(1),
                QUERY_DATE.plusDays(1)
        );
        entityManager.flush();
        entityManager.clear();

        // when / then
        assertPassProductError(
                () -> studentSessionQueryService.findAll(
                        member.getId(),
                        studio.getId(),
                        QUERY_DATE,
                        memberPassProduct.getId()
                ),
                PassProductErrorCode.MEMBER_PASS_PRODUCT_UNAVAILABLE
        );
    }

    @Test
    void 조회일이_보유_수강권_이용_기간_밖이면_빈_목록을_반환한다() {
        // given
        Studio studio = 시설을_저장한다(회원을_저장한다("period-pass-owner"), "기간 외 수강권 시설");
        Member member = 회원을_저장한다("period-pass-member");
        StudioMembership membership = 소속을_저장한다(
                studio,
                member,
                SystemRole.STUDENT,
                MembershipStatus.ACTIVE
        );
        ClassType classType = 수업_종류를_저장한다(studio, "기간 외 수업 종류");
        MemberPassProduct memberPassProduct = 보유_수강권을_저장한다(
                membership,
                수강권을_저장한다(studio, ClassKind.GROUP, List.of(classType)),
                MemberPassProductStatus.ACTIVE,
                10,
                QUERY_DATE.minusMonths(1),
                QUERY_DATE.minusDays(1)
        );
        entityManager.flush();
        entityManager.clear();

        // when
        List<MemberClassSessionResponse> responses = studentSessionQueryService.findAll(
                member.getId(),
                studio.getId(),
                QUERY_DATE,
                memberPassProduct.getId()
        );

        // then
        assertThat(responses).isEmpty();
    }

    @Test
    void 대표는_수업_회차_상세를_조회할_수_있다() {
        // given
        Member owner = 회원을_저장한다("detail-owner");
        Studio studio = 시설을_저장한다(owner, "상세 조회 시설");
        Member instructor = 회원을_저장한다("detail-instructor");
        StudioMembership instructorMembership = 소속을_저장한다(
                studio,
                instructor,
                SystemRole.INSTRUCTOR,
                MembershipStatus.ACTIVE
        );
        ClassType classType = 수업_종류를_저장한다(studio, "요가");
        ClassSession classSession = 수업_회차를_저장한다(
                studio,
                instructorMembership,
                classType,
                "저녁 요가"
        );
        entityManager.flush();
        entityManager.clear();

        // when
        ClassSessionDetailResponse response = queryService.findOne(
                owner.getId(),
                studio.getId(),
                classSession.getId()
        );

        // then
        assertThat(response).isEqualTo(new ClassSessionDetailResponse(
                classSession.getId(),
                instructorMembership.getId(),
                instructor.getName(),
                ClassForm.GROUP,
                ClassTypeResponse.of(classType.getId(), "요가"),
                "저녁 요가",
                "편한 복장과 개인 수건을 준비해 주세요.",
                12,
                60,
                LocalDateTime.of(2026, 8, 17, 20, 0),
                LocalDateTime.of(2026, 8, 17, 21, 0),
                ClassSessionStatus.OPENED
        ));
    }

    @Test
    void 활성_회원은_담당_강사가_아닌_수업도_조회할_수_있다() {
        // given
        Member owner = 회원을_저장한다("student-read-owner");
        Studio studio = 시설을_저장한다(owner, "회원 조회 시설");
        StudioMembership instructorMembership = 소속을_저장한다(
                studio,
                회원을_저장한다("student-read-instructor"),
                SystemRole.INSTRUCTOR,
                MembershipStatus.ACTIVE
        );
        Member student = 회원을_저장한다("student-reader");
        소속을_저장한다(studio, student, SystemRole.STUDENT, MembershipStatus.ACTIVE);
        ClassType classType = 수업_종류를_저장한다(studio, "필라테스");
        ClassSession classSession = 수업_회차를_저장한다(
                studio,
                instructorMembership,
                classType,
                "오전 필라테스"
        );

        // when
        ClassSessionDetailResponse response = queryService.findOne(
                student.getId(),
                studio.getId(),
                classSession.getId()
        );

        // then
        assertThat(response.id()).isEqualTo(classSession.getId());
    }

    @Test
    void 비활성_소속은_수업_회차_상세를_조회할_수_없다() {
        // given
        Member owner = 회원을_저장한다("inactive-read-owner");
        Studio studio = 시설을_저장한다(owner, "비활성 조회 시설");
        Member inactiveMember = 회원을_저장한다("inactive-reader");
        소속을_저장한다(studio, inactiveMember, SystemRole.STUDENT, MembershipStatus.INACTIVE);

        // when / then
        assertStudioError(
                () -> queryService.findOne(inactiveMember.getId(), studio.getId(), 1L),
                StudioErrorCode.MEMBERSHIP_INACTIVE
        );
    }

    @Test
    void 시설_소속이_아니면_수업_회차_상세를_조회할_수_없다() {
        // given
        Member owner = 회원을_저장한다("stranger-read-owner");
        Studio studio = 시설을_저장한다(owner, "비소속 조회 시설");
        Member stranger = 회원을_저장한다("stranger-reader");

        // when / then
        assertStudioError(
                () -> queryService.findOne(stranger.getId(), studio.getId(), 1L),
                StudioErrorCode.NOT_MEMBERSHIP
        );
    }

    @Test
    void 다른_시설의_수업_회차는_조회할_수_없다() {
        // given
        Member firstOwner = 회원을_저장한다("cross-read-first-owner");
        Studio firstStudio = 시설을_저장한다(firstOwner, "첫 번째 조회 시설");
        Member student = 회원을_저장한다("cross-read-student");
        소속을_저장한다(firstStudio, student, SystemRole.STUDENT, MembershipStatus.ACTIVE);

        Member secondOwner = 회원을_저장한다("cross-read-second-owner");
        Studio secondStudio = 시설을_저장한다(secondOwner, "두 번째 조회 시설");
        StudioMembership secondInstructor = 소속을_저장한다(
                secondStudio,
                회원을_저장한다("cross-read-instructor"),
                SystemRole.INSTRUCTOR,
                MembershipStatus.ACTIVE
        );
        ClassType secondClassType = 수업_종류를_저장한다(secondStudio, "발레");
        ClassSession secondClassSession = 수업_회차를_저장한다(
                secondStudio,
                secondInstructor,
                secondClassType,
                "저녁 발레"
        );

        // when / then
        assertClassError(
                () -> queryService.findOne(
                        student.getId(),
                        firstStudio.getId(),
                        secondClassSession.getId()
                ),
                ClassErrorCode.CLASS_SESSION_NOT_FOUND
        );
    }

    private Member 회원을_저장한다(String id) {
        Member member = StudioFixture.아이디가_다른_소유자(id);
        entityManager.persist(member);
        entityManager.flush();
        return member;
    }

    private Studio 시설을_저장한다(Member owner, String name) {
        Studio studio = Studio.builder()
                .owner(owner)
                .name(name)
                .openTime(java.time.LocalTime.of(9, 0))
                .closeTime(java.time.LocalTime.of(22, 0))
                .build();
        entityManager.persist(studio);
        entityManager.flush();
        return studio;
    }

    private StudioMembership 소속을_저장한다(
            Studio studio,
            Member member,
            SystemRole systemRole,
            MembershipStatus status
    ) {
        StudioRole role = 역할을_조회하거나_저장한다(studio, systemRole);

        StudioMembership membership = StudioMembership.builder()
                .studio(studio)
                .member(member)
                .studioRole(role)
                .status(status)
                .joinedAt(LocalDateTime.of(2026, 8, 1, 9, 0))
                .build();
        entityManager.persist(membership);
        entityManager.flush();
        return membership;
    }

    private StudioRole 역할을_조회하거나_저장한다(Studio studio, SystemRole systemRole) {
        List<StudioRole> roles = entityManager.createQuery("""
                        SELECT role
                        FROM StudioRole role
                        WHERE role.studio.id = :studioId
                          AND role.systemRole = :systemRole
                        """, StudioRole.class)
                .setParameter("studioId", studio.getId())
                .setParameter("systemRole", systemRole)
                .getResultList();
        if (!roles.isEmpty()) {
            return roles.getFirst();
        }

        StudioRole role = systemRole.toStudioRole(studio);
        entityManager.persist(role);
        entityManager.flush();
        return role;
    }

    private ClassType 수업_종류를_저장한다(Studio studio, String name) {
        return classTypeRepository.saveAndFlush(ClassTypeFixture.이름이_다른_수업_종류(studio, name));
    }

    private ClassSession 수업_회차를_저장한다(
            Studio studio,
            StudioMembership instructorMembership,
            ClassType classType,
            String name
    ) {
        ClassSession classSession = classSessionRepository.saveAndFlush(ClassSessionFixture.수업_회차(
                studio.getId(),
                instructorMembership,
                name,
                "편한 복장과 개인 수건을 준비해 주세요.",
                ClassForm.GROUP,
                60,
                12,
                LocalDateTime.of(2026, 8, 17, 20, 0),
                ClassSessionStatus.OPENED
        ));
        classSessionClassTypeRepository.saveAndFlush(
                ClassSessionFixture.수업_종류_연결(classSession.getId(), classType.getId())
        );
        return classSession;
    }

    private ClassSession 수업_회차를_저장한다(
            Studio studio,
            StudioMembership instructorMembership,
            ClassType classType,
            String name,
            ClassForm classForm,
            int capacity,
            LocalDateTime startAt,
            ClassSessionStatus status
    ) {
        ClassSession classSession = classSessionRepository.saveAndFlush(ClassSessionFixture.수업_회차(
                studio.getId(),
                instructorMembership,
                name,
                name + " 안내",
                classForm,
                60,
                capacity,
                startAt,
                status
        ));
        classSessionClassTypeRepository.saveAndFlush(
                ClassSessionFixture.수업_종류_연결(classSession.getId(), classType.getId())
        );
        return classSession;
    }

    private void 정책을_저장한다(Studio studio, int reservationCloseMinutesBefore) {
        entityManager.persist(StudioPolicy.builder()
                .studio(studio)
                .reservationCloseMinutesBefore(reservationCloseMinutesBefore)
                .freeCancelMinutesBefore(60)
                .waitingOfferResponseMinutes(10)
                .build());
        entityManager.flush();
    }

    private PassProduct 수강권을_저장한다(
            Studio studio,
            ClassKind classKind,
            List<ClassType> classTypes
    ) {
        PassProduct passProduct = PassProduct.builder()
                .studio(studio)
                .name(classKind + " 회원용 수강권")
                .classKind(classKind)
                .classTypes(classTypes)
                .totalCount(10)
                .validPeriodAmount(3)
                .validPeriodUnit(PassProductPeriodUnit.MONTH)
                .totalHoldDays(7)
                .build();
        entityManager.persist(passProduct);
        entityManager.flush();
        return passProduct;
    }

    private MemberPassProduct 보유_수강권을_저장한다(
            StudioMembership membership,
            PassProduct passProduct,
            MemberPassProductStatus status,
            int remainingCount,
            LocalDate startedAt,
            LocalDate expiresAt
    ) {
        MemberPassProduct memberPassProduct = MemberPassProduct.builder()
                .membership(membership)
                .passProduct(passProduct)
                .remainingCount(remainingCount)
                .remainingHoldDays(7)
                .status(status)
                .startedAt(startedAt)
                .expiresAt(expiresAt)
                .build();
        entityManager.persist(memberPassProduct);
        entityManager.flush();
        return memberPassProduct;
    }

    private void 예약을_저장한다(
            ClassSession classSession,
            StudioMembership membership,
            ReservationStatus status
    ) {
        entityManager.persist(Reservation.builder()
                .membership(membership)
                .classSession(classSession)
                .status(status)
                .reservedAt(NOW.minusDays(1))
                .canceledAt(status == ReservationStatus.CANCELED ? NOW.minusHours(1) : null)
                .build());
    }

    private void 대기를_저장한다(
            ClassSession classSession,
            StudioMembership membership,
            int sequence,
            WaitingStatus status
    ) {
        entityManager.persist(Waiting.builder()
                .membership(membership)
                .classSession(classSession)
                .sequence(sequence)
                .status(status)
                .offeredAt(status == WaitingStatus.OFFERED ? NOW.minusMinutes(5) : null)
                .build());
    }

    private void assertStudioError(Runnable action, StudioErrorCode errorCode) {
        assertThatThrownBy(action::run)
                .isInstanceOfSatisfying(StudioException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(errorCode));
    }

    private void assertClassError(Runnable action, ClassErrorCode errorCode) {
        assertThatThrownBy(action::run)
                .isInstanceOfSatisfying(ClassException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(errorCode));
    }

    private void assertPassProductError(Runnable action, PassProductErrorCode errorCode) {
        assertThatThrownBy(action::run)
                .isInstanceOfSatisfying(PassProductException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(errorCode));
    }

    private static Stream<Arguments> 허용되지_않은_보유_수강권() {
        return Stream.of(
                Arguments.of(false),
                Arguments.of(true)
        );
    }

    private static Stream<Arguments> 사용할_수_없는_수강권() {
        return Stream.of(
                Arguments.of(MemberPassProductStatus.HOLD, 10),
                Arguments.of(MemberPassProductStatus.ACTIVE, 0)
        );
    }

    @TestConfiguration
    static class FixedClockConfig {

        @Primary
        @Bean
        Clock clock() {
            return Clock.fixed(NOW.atZone(SERVICE_ZONE_ID).toInstant(), SERVICE_ZONE_ID);
        }
    }
}
