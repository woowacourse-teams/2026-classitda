package com.classitda.classes.application.student.enrollment;

import com.classitda.studio.fixture.StudioFixture;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import com.classitda.classes.application.student.StudentSessionAccessReader;
import com.classitda.classes.domain.ClassForm;
import com.classitda.classes.domain.session.ClassSession;
import com.classitda.classes.domain.enrollment.ClassSessionEnrollment;
import com.classitda.classes.domain.ClassType;
import com.classitda.classes.exception.ClassErrorCode;
import com.classitda.classes.exception.ClassException;
import com.classitda.member.domain.Member;
import com.classitda.passproduct.domain.MemberPassProduct;
import com.classitda.passproduct.domain.MemberPassProductStatus;
import com.classitda.passproduct.domain.PassProduct;
import com.classitda.passproduct.domain.PassProductPeriodUnit;
import com.classitda.studio.domain.MembershipStatus;
import com.classitda.studio.domain.Studio;
import com.classitda.studio.domain.StudioMembership;
import com.classitda.studio.domain.StudioRole;
import com.classitda.studio.domain.SystemRole;
import com.classitda.studio.exception.StudioErrorCode;
import com.classitda.studio.exception.StudioException;
import com.classitda.support.MySqlDataJpaTest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@Import({
        StudentEnrollmentDetailQueryService.class,
        StudentSessionAccessReader.class
})
@MySqlDataJpaTest
class StudentEnrollmentDetailQueryServiceTest {

    private static final AtomicLong PHONE_SEQUENCE = new AtomicLong(30_000_000L);
    private static final LocalDateTime ENROLLED_AT = LocalDateTime.of(2026, 8, 1, 15, 20);
    private static final LocalDateTime SESSION_START_AT = LocalDateTime.of(2026, 8, 4, 18, 30);
    private static final LocalDate PASS_STARTED_AT = LocalDate.of(2026, 6, 30);
    private static final LocalDate PASS_EXPIRES_AT = LocalDate.of(2026, 8, 20);

    private final StudentEnrollmentDetailQueryService queryService;
    private final EntityManager entityManager;
    private final Statistics statistics;

    @Autowired
    StudentEnrollmentDetailQueryServiceTest(
            StudentEnrollmentDetailQueryService queryService,
            EntityManager entityManager,
            EntityManagerFactory entityManagerFactory
    ) {
        this.queryService = queryService;
        this.entityManager = entityManager;
        this.statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
    }

    @Test
    void 일곱_가지_학생_신청_상태와_상세_정보를_반환한다() {
        // given
        DetailContext context = 기본_환경("status");
        ClassSession reservedSession = context.classSession();
        ClassSession waitingSession = 수업을_저장한다(context, "대기 수업", SESSION_START_AT.plusDays(1));
        ClassSession offeredSession = 수업을_저장한다(context, "제안 수업", SESSION_START_AT.plusDays(2));
        ClassSession attendedSession = 수업을_저장한다(context, "출석 수업", SESSION_START_AT.plusDays(3));
        ClassSession absentSession = 수업을_저장한다(context, "결석 수업", SESSION_START_AT.plusDays(4));
        ClassSession reservationCanceledSession = 수업을_저장한다(
                context,
                "예약 취소 수업",
                SESSION_START_AT.plusDays(5)
        );
        ClassSession sessionCanceledSession = 수업을_저장한다(
                context,
                "수업 취소 수업",
                SESSION_START_AT.plusDays(6)
        );

        ClassSessionEnrollment reserved = 신청을_저장한다(ClassSessionEnrollment.reserved(
                context.studentMembership(),
                reservedSession,
                context.memberPassProduct(),
                ENROLLED_AT
        ));
        ClassSessionEnrollment waiting = 신청을_저장한다(ClassSessionEnrollment.waiting(
                context.studentMembership(),
                waitingSession,
                ENROLLED_AT.plusMinutes(1)
        ));
        ClassSessionEnrollment offered = ClassSessionEnrollment.waiting(
                context.studentMembership(),
                offeredSession,
                ENROLLED_AT.plusMinutes(2)
        );
        LocalDateTime offeredAt = ENROLLED_AT.plusMinutes(3);
        LocalDateTime offerExpiresAt = offeredAt.plusHours(1);
        offered.offer(offeredAt, offerExpiresAt);
        신청을_저장한다(offered);

        ClassSessionEnrollment attended = ClassSessionEnrollment.reserved(
                context.studentMembership(),
                attendedSession,
                context.memberPassProduct(),
                ENROLLED_AT.plusMinutes(4)
        );
        LocalDateTime attendedAt = attendedSession.getEndAt();
        attended.markAttended(attendedAt);
        신청을_저장한다(attended);

        ClassSessionEnrollment absent = ClassSessionEnrollment.reserved(
                context.studentMembership(),
                absentSession,
                context.memberPassProduct(),
                ENROLLED_AT.plusMinutes(5)
        );
        LocalDateTime absentAt = absentSession.getEndAt();
        absent.markAbsent(absentAt);
        신청을_저장한다(absent);

        ClassSessionEnrollment reservationCanceled = ClassSessionEnrollment.reserved(
                context.studentMembership(),
                reservationCanceledSession,
                context.memberPassProduct(),
                ENROLLED_AT.plusMinutes(6)
        );
        LocalDateTime reservationCanceledAt = ENROLLED_AT.plusMinutes(7);
        reservationCanceled.cancelReservation(reservationCanceledAt);
        신청을_저장한다(reservationCanceled);
        reservationCanceledSession.cancel(ENROLLED_AT.plusMinutes(8));

        ClassSessionEnrollment sessionCanceled = 신청을_저장한다(ClassSessionEnrollment.reserved(
                context.studentMembership(),
                sessionCanceledSession,
                context.memberPassProduct(),
                ENROLLED_AT.plusMinutes(9)
        ));
        LocalDateTime sessionCanceledAt = ENROLLED_AT.plusMinutes(10);
        sessionCanceledSession.cancel(sessionCanceledAt);
        entityManager.flush();
        entityManager.refresh(reserved);

        LocalDateTime reservedCreatedAt = reserved.getCreatedAt();
        Long memberId = context.studentMembership().getMember().getId();
        Long studioId = context.studio().getId();
        entityManager.clear();

        // when
        StudentEnrollmentDetailView reservedView = queryService.findOne(
                memberId, studioId, reservedSession.getId(), reserved.getId());
        StudentEnrollmentDetailView waitingView = queryService.findOne(
                memberId, studioId, waitingSession.getId(), waiting.getId());
        StudentEnrollmentDetailView offeredView = queryService.findOne(
                memberId, studioId, offeredSession.getId(), offered.getId());
        StudentEnrollmentDetailView attendedView = queryService.findOne(
                memberId, studioId, attendedSession.getId(), attended.getId());
        StudentEnrollmentDetailView absentView = queryService.findOne(
                memberId, studioId, absentSession.getId(), absent.getId());
        StudentEnrollmentDetailView reservationCanceledView = queryService.findOne(
                memberId,
                studioId,
                reservationCanceledSession.getId(),
                reservationCanceled.getId()
        );
        StudentEnrollmentDetailView sessionCanceledView = queryService.findOne(
                memberId,
                studioId,
                sessionCanceledSession.getId(),
                sessionCanceled.getId()
        );

        // then
        assertThat(reservedView).isEqualTo(new StudentEnrollmentDetailView(
                reserved.getId(),
                StudentEnrollmentDetailStatus.RESERVED,
                reservedCreatedAt,
                ENROLLED_AT,
                null,
                null,
                null,
                new StudentEnrollmentDetailView.ClassSessionDetails(
                        reservedSession.getId(),
                        reservedSession.getName(),
                        reservedSession.getDescription(),
                        reservedSession.getStartAt(),
                        reservedSession.getEndAt(),
                        null
                ),
                new StudentEnrollmentDetailView.UsedPass(
                        context.memberPassProduct().getId(),
                        context.memberPassProduct().getPassProduct().getName(),
                        PASS_STARTED_AT,
                        PASS_EXPIRES_AT,
                        14
                ),
                new StudentEnrollmentDetailView.Instructor(
                        context.instructorMembership().getId(),
                        "박소연 강사",
                        "https://images.example.com/instructor-status.png",
                        context.studio().getName()
                )
        ));
        assertThat(List.of(
                waitingView,
                offeredView,
                attendedView,
                absentView,
                reservationCanceledView,
                sessionCanceledView
        )).extracting(
                StudentEnrollmentDetailView::status,
                StudentEnrollmentDetailView::waitingPosition,
                StudentEnrollmentDetailView::attendanceRecordedAt
        ).containsExactly(
                tuple(StudentEnrollmentDetailStatus.WAITING, 1L, null),
                tuple(StudentEnrollmentDetailStatus.OFFERED, 0L, null),
                tuple(StudentEnrollmentDetailStatus.ATTENDED, null, attendedAt),
                tuple(StudentEnrollmentDetailStatus.ABSENT, null, absentAt),
                tuple(StudentEnrollmentDetailStatus.RESERVATION_CANCELED, null, null),
                tuple(StudentEnrollmentDetailStatus.SESSION_CANCELED, null, null)
        );
        assertThat(waitingView.usedPass()).isNull();
        assertThat(offeredView.usedPass()).isNull();
        assertThat(offeredView.offerExpiresAt()).isEqualTo(offerExpiresAt);
        assertThat(reservationCanceledView.usedPass()).isNotNull();
        assertThat(reservationCanceledView.classSession().canceledAt())
                .isEqualTo(ENROLLED_AT.plusMinutes(8));
        assertThat(sessionCanceledView.classSession().canceledAt()).isEqualTo(sessionCanceledAt);
    }

    @Test
    void 대기_순번은_현재_WAITING만_상태_변경_시각과_ID_순서로_계산한다() {
        // given
        DetailContext context = 기본_환경("queue");
        LocalDateTime targetChangedAt = ENROLLED_AT.plusHours(1);

        StudioMembership earlierMembership = 학생_소속을_저장한다(context.studio(), "앞선 대기");
        신청을_저장한다(ClassSessionEnrollment.waiting(
                earlierMembership,
                context.classSession(),
                targetChangedAt.minusMinutes(1)
        ));
        StudioMembership sameTimeMembership = 학생_소속을_저장한다(context.studio(), "동일 시각 대기");
        신청을_저장한다(ClassSessionEnrollment.waiting(
                sameTimeMembership,
                context.classSession(),
                targetChangedAt
        ));

        StudioMembership offeredMembership = 학생_소속을_저장한다(context.studio(), "제안됨");
        ClassSessionEnrollment offered = ClassSessionEnrollment.waiting(
                offeredMembership,
                context.classSession(),
                targetChangedAt.minusMinutes(3)
        );
        offered.offer(targetChangedAt.minusMinutes(2), targetChangedAt.plusHours(1));
        신청을_저장한다(offered);

        StudioMembership reservedMembership = 학생_소속을_저장한다(context.studio(), "예약됨");
        MemberPassProduct reservedPass = 수강권을_저장한다(
                reservedMembership,
                context.studio(),
                "queue-reserved"
        );
        신청을_저장한다(ClassSessionEnrollment.reserved(
                reservedMembership,
                context.classSession(),
                reservedPass,
                targetChangedAt.minusMinutes(3)
        ));

        StudioMembership canceledMembership = 학생_소속을_저장한다(context.studio(), "취소됨");
        ClassSessionEnrollment canceled = ClassSessionEnrollment.waiting(
                canceledMembership,
                context.classSession(),
                targetChangedAt.minusMinutes(3)
        );
        canceled.cancelWaiting(targetChangedAt.minusMinutes(2));
        신청을_저장한다(canceled);

        StudioMembership expiredMembership = 학생_소속을_저장한다(context.studio(), "만료됨");
        ClassSessionEnrollment expired = ClassSessionEnrollment.waiting(
                expiredMembership,
                context.classSession(),
                targetChangedAt.minusMinutes(3)
        );
        expired.expire(targetChangedAt.minusMinutes(2));
        신청을_저장한다(expired);

        ClassSession otherSession = 수업을_저장한다(
                context,
                "다른 수업",
                SESSION_START_AT.plusDays(1)
        );
        StudioMembership otherSessionMembership = 학생_소속을_저장한다(context.studio(), "다른 수업 대기");
        신청을_저장한다(ClassSessionEnrollment.waiting(
                otherSessionMembership,
                otherSession,
                targetChangedAt.minusMinutes(3)
        ));

        ClassSessionEnrollment target = 신청을_저장한다(ClassSessionEnrollment.waiting(
                context.studentMembership(),
                context.classSession(),
                targetChangedAt
        ));
        StudioMembership laterMembership = 학생_소속을_저장한다(context.studio(), "뒤 대기");
        신청을_저장한다(ClassSessionEnrollment.waiting(
                laterMembership,
                context.classSession(),
                targetChangedAt.plusMinutes(1)
        ));

        Long targetMemberId = context.studentMembership().getMember().getId();
        Long offeredMemberId = offeredMembership.getMember().getId();
        Long studioId = context.studio().getId();
        entityManager.flush();
        entityManager.clear();
        statistics.clear();

        // when
        StudentEnrollmentDetailView targetView = queryService.findOne(
                targetMemberId,
                studioId,
                context.classSession().getId(),
                target.getId()
        );
        long waitingQueryCount = statistics.getPrepareStatementCount();

        entityManager.clear();
        statistics.clear();
        StudentEnrollmentDetailView offeredView = queryService.findOne(
                offeredMemberId,
                studioId,
                context.classSession().getId(),
                offered.getId()
        );
        long offeredQueryCount = statistics.getPrepareStatementCount();

        // then
        assertThat(targetView.waitingPosition()).isEqualTo(3L);
        assertThat(offeredView.waitingPosition()).isZero();
        assertThat(waitingQueryCount).isEqualTo(5L);
        assertThat(offeredQueryCount).isEqualTo(4L);
    }

    @Test
    void 숨긴_이력과_조회_범위를_벗어난_신청은_같은_NOT_FOUND로_처리한다() {
        // given
        DetailContext context = 기본_환경("not-found");
        ClassSessionEnrollment ownedEnrollment = 신청을_저장한다(ClassSessionEnrollment.reserved(
                context.studentMembership(),
                context.classSession(),
                context.memberPassProduct(),
                ENROLLED_AT.minusMinutes(1)
        ));
        ClassSessionEnrollment waitingCanceled = ClassSessionEnrollment.waiting(
                context.studentMembership(),
                context.classSession(),
                ENROLLED_AT
        );
        waitingCanceled.cancelWaiting(ENROLLED_AT.plusMinutes(1));
        신청을_저장한다(waitingCanceled);

        ClassSessionEnrollment expired = ClassSessionEnrollment.waiting(
                context.studentMembership(),
                context.classSession(),
                ENROLLED_AT.plusMinutes(2)
        );
        expired.expire(ENROLLED_AT.plusMinutes(3));
        신청을_저장한다(expired);

        StudioMembership otherMembership = 학생_소속을_저장한다(context.studio(), "다른 회원");
        ClassSessionEnrollment otherEnrollment = 신청을_저장한다(ClassSessionEnrollment.waiting(
                otherMembership,
                context.classSession(),
                ENROLLED_AT.plusMinutes(4)
        ));

        ClassSession otherSession = 수업을_저장한다(
                context,
                "다른 수업",
                SESSION_START_AT.plusDays(1)
        );

        DetailContext otherStudioContext = 기본_환경("other-studio");
        ClassSessionEnrollment otherStudioEnrollment = 신청을_저장한다(ClassSessionEnrollment.waiting(
                otherStudioContext.studentMembership(),
                otherStudioContext.classSession(),
                ENROLLED_AT.plusMinutes(5)
        ));
        ClassSessionEnrollment crossStudioEnrollment = 신청을_저장한다(ClassSessionEnrollment.waiting(
                context.studentMembership(),
                otherStudioContext.classSession(),
                ENROLLED_AT.plusMinutes(6)
        ));

        Long memberId = context.studentMembership().getMember().getId();
        Long studioId = context.studio().getId();
        entityManager.flush();
        entityManager.clear();

        // when / then
        Long classSessionId = context.classSession().getId();
        assertEnrollmentNotFound(() -> queryService.findOne(
                memberId, studioId, classSessionId, Long.MAX_VALUE));
        assertEnrollmentNotFound(() -> queryService.findOne(
                memberId, studioId, classSessionId, waitingCanceled.getId()));
        assertEnrollmentNotFound(() -> queryService.findOne(
                memberId, studioId, classSessionId, expired.getId()));
        assertEnrollmentNotFound(() -> queryService.findOne(
                memberId, studioId, classSessionId, otherEnrollment.getId()));
        assertEnrollmentNotFound(() -> queryService.findOne(
                memberId, studioId, otherSession.getId(), ownedEnrollment.getId()));
        assertEnrollmentNotFound(() -> queryService.findOne(
                memberId,
                studioId,
                otherStudioContext.classSession().getId(),
                otherStudioEnrollment.getId()
        ));
        assertEnrollmentNotFound(() -> queryService.findOne(
                memberId,
                studioId,
                otherStudioContext.classSession().getId(),
                crossStudioEnrollment.getId()
        ));
    }

    @Test
    void 활성_학생이_아니면_신청_상세를_조회할_수_없다() {
        // given
        DetailContext context = 기본_환경("access");
        Member stranger = 회원을_저장한다("비소속", null);
        StudioMembership inactiveStudent = 소속을_저장한다(
                context.studio(),
                회원을_저장한다("비활성 학생", null),
                SystemRole.STUDENT,
                MembershipStatus.INACTIVE,
                "비활성 학생"
        );
        StudioMembership ownerRole = 소속을_저장한다(
                context.studio(),
                회원을_저장한다("대표", null),
                SystemRole.OWNER,
                MembershipStatus.ACTIVE,
                "대표"
        );
        StudioRole customRole = StudioRole.builder()
                .studio(context.studio())
                .name("운영 관리자")
                .instructor(true)
                .build();
        entityManager.persist(customRole);
        StudioMembership customStaff = 소속을_저장한다(
                context.studio(),
                회원을_저장한다("운영자", null),
                customRole,
                MembershipStatus.ACTIVE,
                "운영자"
        );

        Long studioId = context.studio().getId();
        entityManager.flush();
        entityManager.clear();

        // when / then
        assertStudioError(
                () -> queryService.findOne(stranger.getId(), studioId, 1L, 1L),
                StudioErrorCode.NOT_MEMBERSHIP
        );
        assertStudioError(
                () -> queryService.findOne(inactiveStudent.getMember().getId(), studioId, 1L, 1L),
                StudioErrorCode.MEMBERSHIP_INACTIVE
        );
        assertStudioError(
                () -> queryService.findOne(ownerRole.getMember().getId(), studioId, 1L, 1L),
                StudioErrorCode.PERMISSION_DENIED
        );
        assertStudioError(
                () -> queryService.findOne(
                        context.instructorMembership().getMember().getId(),
                        studioId,
                        1L,
                        1L
                ),
                StudioErrorCode.PERMISSION_DENIED
        );
        assertStudioError(
                () -> queryService.findOne(customStaff.getMember().getId(), studioId, 1L, 1L),
                StudioErrorCode.PERMISSION_DENIED
        );
    }

    private DetailContext 기본_환경(String suffix) {
        Member owner = 회원을_저장한다("대표-" + suffix, null);
        Studio studio = 시설을_저장한다(owner, "클래스잇다 " + suffix);
        StudioMembership studentMembership = 학생_소속을_저장한다(studio, "김회원-" + suffix);
        StudioMembership instructorMembership = 소속을_저장한다(
                studio,
                회원을_저장한다(
                        "강사 회원-" + suffix,
                        "https://images.example.com/instructor-" + suffix + ".png"
                ),
                SystemRole.INSTRUCTOR,
                MembershipStatus.ACTIVE,
                "박소연 강사"
        );
        MemberPassProduct memberPassProduct = 수강권을_저장한다(
                studentMembership,
                studio,
                suffix
        );
        ClassSession classSession = 수업을_저장한다(
                studio,
                instructorMembership,
                "체어 밸런스-" + suffix,
                SESSION_START_AT
        );
        return new DetailContext(
                studio,
                studentMembership,
                instructorMembership,
                memberPassProduct,
                classSession
        );
    }

    private Member 회원을_저장한다(String name, String profileImageUrl) {
        Member member = Member.builder()
                .name(name)
                .phoneNumber("010%08d".formatted(PHONE_SEQUENCE.getAndIncrement()))
                .profileImageUrl(profileImageUrl)
                .build();
        entityManager.persist(member);
        entityManager.flush();
        return member;
    }

    private Studio 시설을_저장한다(Member owner, String name) {
        Studio studio = Studio.builder()
                .owner(owner)
                .name(name)
                .openTime(LocalTime.of(9, 0))
                .closeTime(LocalTime.of(22, 0))
                .address(StudioFixture.기본_주소())
                .build();
        entityManager.persist(studio);
        entityManager.flush();
        return studio;
    }

    private StudioMembership 학생_소속을_저장한다(Studio studio, String name) {
        return 소속을_저장한다(
                studio,
                회원을_저장한다(name, null),
                SystemRole.STUDENT,
                MembershipStatus.ACTIVE,
                name
        );
    }

    private StudioMembership 소속을_저장한다(
            Studio studio,
            Member member,
            SystemRole systemRole,
            MembershipStatus status,
            String name
    ) {
        return 소속을_저장한다(
                studio,
                member,
                역할을_조회하거나_저장한다(studio, systemRole),
                status,
                name
        );
    }

    private StudioMembership 소속을_저장한다(
            Studio studio,
            Member member,
            StudioRole role,
            MembershipStatus status,
            String name
    ) {
        StudioMembership membership = StudioMembership.builder()
                .studio(studio)
                .member(member)
                .phoneNumber(member.getPhoneNumber())
                .studioRole(role)
                .name(name)
                .status(status)
                .joinedAt(LocalDateTime.of(2026, 6, 1, 9, 0))
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

    private MemberPassProduct 수강권을_저장한다(
            StudioMembership membership,
            Studio studio,
            String suffix
    ) {
        ClassType classType = ClassType.builder()
                .studio(studio)
                .name("상세 수업 종류-" + suffix)
                .build();
        entityManager.persist(classType);
        PassProduct passProduct = PassProduct.builder()
                .studio(studio)
                .name("[8:1] 그룹 레슨 20회권-" + suffix)
                .classForm(ClassForm.GROUP)
                .classTypes(List.of(classType))
                .totalCount(20)
                .validPeriodAmount(3)
                .validPeriodUnit(PassProductPeriodUnit.MONTH)
                .totalHoldDays(7)
                .build();
        entityManager.persist(passProduct);

        MemberPassProduct memberPassProduct = MemberPassProduct.builder()
                .membership(membership)
                .passProduct(passProduct)
                .remainingCount(14)
                .remainingHoldDays(7)
                .status(MemberPassProductStatus.ACTIVE)
                .startedAt(PASS_STARTED_AT)
                .expiresAt(PASS_EXPIRES_AT)
                .build();
        entityManager.persist(memberPassProduct);
        entityManager.flush();
        return memberPassProduct;
    }

    private ClassSession 수업을_저장한다(
            DetailContext context,
            String name,
            LocalDateTime startAt
    ) {
        return 수업을_저장한다(
                context.studio(),
                context.instructorMembership(),
                name,
                startAt
        );
    }

    private ClassSession 수업을_저장한다(
            Studio studio,
            StudioMembership instructorMembership,
            String name,
            LocalDateTime startAt
    ) {
        ClassSession classSession = ClassSession.builder()
                .studioId(studio.getId())
                .instructorMembership(instructorMembership)
                .name(name)
                .description(name + " 안내")
                .classForm(ClassForm.GROUP)
                .durationMinutes(50)
                .capacity(20)
                .startAt(startAt)
                .build();
        entityManager.persist(classSession);
        entityManager.flush();
        return classSession;
    }

    private ClassSessionEnrollment 신청을_저장한다(ClassSessionEnrollment enrollment) {
        entityManager.persist(enrollment);
        entityManager.flush();
        return enrollment;
    }

    private void assertEnrollmentNotFound(Runnable action) {
        assertThatThrownBy(action::run)
                .isInstanceOfSatisfying(ClassException.class, exception ->
                        assertThat(exception.getErrorCode())
                                .isEqualTo(ClassErrorCode.CLASS_SESSION_ENROLLMENT_NOT_FOUND));
    }

    private void assertStudioError(Runnable action, StudioErrorCode errorCode) {
        assertThatThrownBy(action::run)
                .isInstanceOfSatisfying(StudioException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(errorCode));
    }

    private record DetailContext(
            Studio studio,
            StudioMembership studentMembership,
            StudioMembership instructorMembership,
            MemberPassProduct memberPassProduct,
            ClassSession classSession
    ) {
    }
}
