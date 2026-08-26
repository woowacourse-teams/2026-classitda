package com.classitda.classes.application.instructor.daily;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.classitda.classes.application.instructor.InstructorSessionAccessReader;
import com.classitda.classes.application.instructor.InstructorSessionStatus;
import com.classitda.classes.domain.ClassForm;
import com.classitda.classes.domain.ClassType;
import com.classitda.classes.domain.enrollment.ClassSessionEnrollment;
import com.classitda.classes.domain.repository.ClassSessionClassTypeRepository;
import com.classitda.classes.domain.repository.ClassSessionRepository;
import com.classitda.classes.domain.repository.ClassTypeRepository;
import com.classitda.classes.domain.session.ClassSession;
import com.classitda.classes.fixture.ClassSessionFixture;
import com.classitda.classes.fixture.ClassTypeFixture;
import com.classitda.common.exception.ClassitdaException;
import com.classitda.common.exception.CommonErrorCode;
import com.classitda.common.pagination.CursorResponse;
import com.classitda.member.domain.Member;
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
import jakarta.persistence.EntityManagerFactory;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.TestPropertySource;

@Import({
        InstructorDailyQueryService.class,
        InstructorSessionAccessReader.class,
        InstructorScheduleReader.class,
        InstructorDailyCursorQueryTest.FixedClockConfig.class
})
@TestPropertySource(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@MySqlRepositoryTest
class InstructorDailyCursorQueryTest {

    private static final ZoneId SERVICE_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 8, 17, 10, 0);

    private final InstructorDailyQueryService queryService;
    private final ClassSessionClassTypeRepository classSessionClassTypeRepository;
    private final ClassSessionRepository classSessionRepository;
    private final ClassTypeRepository classTypeRepository;
    private final EntityManager entityManager;
    private final Statistics statistics;

    @Autowired
    InstructorDailyCursorQueryTest(
            InstructorDailyQueryService queryService,
            ClassSessionClassTypeRepository classSessionClassTypeRepository,
            ClassSessionRepository classSessionRepository,
            ClassTypeRepository classTypeRepository,
            EntityManager entityManager,
            EntityManagerFactory entityManagerFactory
    ) {
        this.queryService = queryService;
        this.classSessionClassTypeRepository = classSessionClassTypeRepository;
        this.classSessionRepository = classSessionRepository;
        this.classTypeRepository = classTypeRepository;
        this.entityManager = entityManager;
        this.statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
    }

    @Test
    void 같은_시작_시각의_수업도_누락과_중복_없이_다음_페이지로_조회한다() {
        // given
        Member owner = 회원을_저장한다("session-list-owner");
        Studio studio = 시설을_저장한다(owner, "전체 수업 조회 시설");
        StudioMembership ownerMembership = 소속을_저장한다(studio, owner, SystemRole.OWNER);
        StudioMembership otherInstructor = 소속을_저장한다(
                studio,
                회원을_저장한다("session-list-other-instructor"),
                SystemRole.INSTRUCTOR
        );
        StudioMembership canceledSessionInstructor = 소속을_저장한다(
                studio,
                회원을_저장한다("session-list-canceled-instructor"),
                SystemRole.INSTRUCTOR
        );
        ClassType classType = 수업_종류를_저장한다(studio, "필라테스");
        정책을_저장한다(studio, 30);

        LocalDateTime sameStartAt = LocalDateTime.of(2026, 8, 20, 10, 0);
        ClassSession first = 수업을_저장한다(
                studio, otherInstructor, classType, "동시각 첫 수업", ClassForm.GROUP, sameStartAt);
        ClassSession second = 수업을_저장한다(
                studio, ownerMembership, classType, "동시각 둘째 수업", ClassForm.GROUP, sameStartAt);
        ClassSession canceled = 수업을_저장한다(
                studio, canceledSessionInstructor, classType, "동시각 취소 수업", ClassForm.GROUP, sameStartAt);
        canceled.cancel(NOW);
        ClassSession completed = 수업을_저장한다(
                studio,
                ownerMembership,
                classType,
                "완료 수업",
                ClassForm.GROUP,
                LocalDateTime.of(2026, 8, 16, 10, 0)
        );

        StudioMembership waitingMember = 소속을_저장한다(
                studio, 회원을_저장한다("session-list-waiting"), SystemRole.STUDENT);
        StudioMembership offeredMember = 소속을_저장한다(
                studio, 회원을_저장한다("session-list-offered"), SystemRole.STUDENT);
        대기_신청을_저장한다(second, waitingMember);
        제안_신청을_저장한다(second, offeredMember);
        entityManager.flush();
        entityManager.clear();

        // when
        CursorResponse<InstructorDailySessionView> firstPage = queryService.findWithCursor(
                owner.getId(), studio.getId(), null, 2, null, null);
        CursorResponse<InstructorDailySessionView> secondPage = queryService.findWithCursor(
                owner.getId(), studio.getId(), firstPage.nextCursor(), 2, null, null);

        // then
        assertThat(firstPage.items()).extracting(InstructorDailySessionView::id)
                .containsExactly(canceled.getId(), second.getId());
        assertThat(firstPage.hasNext()).isTrue();
        assertThat(firstPage.nextCursor()).isNotBlank();
        assertThat(firstPage.items().getFirst().status()).isEqualTo(InstructorSessionStatus.CANCELED);
        assertThat(firstPage.items().get(1).reservedCount()).isEqualTo(1);
        assertThat(firstPage.items().get(1).waitingCount()).isEqualTo(1);
        assertThat(firstPage.items().get(1).mine()).isTrue();

        assertThat(secondPage.items()).extracting(InstructorDailySessionView::id)
                .containsExactly(first.getId(), completed.getId());
        assertThat(secondPage.items().getFirst().mine()).isFalse();
        assertThat(secondPage.items().get(1).status()).isEqualTo(InstructorSessionStatus.COMPLETED);
        assertThat(secondPage.hasNext()).isFalse();
        assertThat(secondPage.nextCursor()).isNull();
    }

    @Test
    void 수업_형태와_수업_종류를_각각_또는_함께_선택해_필터링한다() {
        // given
        Member owner = 회원을_저장한다("session-list-filter-owner");
        Studio studio = 시설을_저장한다(owner, "전체 수업 필터 시설");
        StudioMembership ownerMembership = 소속을_저장한다(studio, owner, SystemRole.OWNER);
        ClassType yoga = 수업_종류를_저장한다(studio, "요가");
        ClassType pilates = 수업_종류를_저장한다(studio, "필라테스");
        정책을_저장한다(studio, 30);

        ClassSession groupYoga = 수업을_저장한다(
                studio, ownerMembership, yoga, "그룹 요가", ClassForm.GROUP, NOW.plusDays(3));
        ClassSession individualPilates = 수업을_저장한다(
                studio, ownerMembership, pilates, "개인 필라테스", ClassForm.INDIVIDUAL, NOW.plusDays(2));
        ClassSession groupPilates = 수업을_저장한다(
                studio, ownerMembership, pilates, "그룹 필라테스", ClassForm.GROUP, NOW.plusDays(1));
        entityManager.flush();
        entityManager.clear();

        // when
        CursorResponse<InstructorDailySessionView> individualResponse = queryService.findWithCursor(
                owner.getId(), studio.getId(), null, 20, ClassForm.INDIVIDUAL, null);
        CursorResponse<InstructorDailySessionView> yogaResponse = queryService.findWithCursor(
                owner.getId(), studio.getId(), null, 20, null, yoga.getId());
        CursorResponse<InstructorDailySessionView> combinedResponse = queryService.findWithCursor(
                owner.getId(), studio.getId(), null, 20, ClassForm.GROUP, pilates.getId());
        CursorResponse<InstructorDailySessionView> emptyCombinedResponse = queryService.findWithCursor(
                owner.getId(), studio.getId(), null, 20, ClassForm.INDIVIDUAL, yoga.getId());

        // then
        assertThat(individualResponse.items()).extracting(InstructorDailySessionView::id)
                .containsExactly(individualPilates.getId());
        assertThat(yogaResponse.items()).extracting(InstructorDailySessionView::id)
                .containsExactly(groupYoga.getId());
        assertThat(combinedResponse.items()).extracting(InstructorDailySessionView::id)
                .containsExactly(groupPilates.getId());
        assertThat(emptyCombinedResponse.items()).isEmpty();
        assertThat(emptyCombinedResponse.hasNext()).isFalse();
        assertThat(emptyCombinedResponse.nextCursor()).isNull();
    }

    @Test
    void 조회하는_수업_수가_늘어나도_페이지_조회_쿼리_수는_증가하지_않는다() {
        // given
        Member owner = 회원을_저장한다("session-list-query-count-owner");
        Studio studio = 시설을_저장한다(owner, "전체 수업 쿼리 수 시설");
        StudioMembership ownerMembership = 소속을_저장한다(studio, owner, SystemRole.OWNER);
        ClassType classType = 수업_종류를_저장한다(studio, "필라테스");
        정책을_저장한다(studio, 30);
        for (int index = 1; index <= 5; index++) {
            수업을_저장한다(
                    studio,
                    ownerMembership,
                    classType,
                    "쿼리 수 확인 수업 " + index,
                    ClassForm.GROUP,
                    NOW.plusDays(index)
            );
        }
        entityManager.flush();
        entityManager.clear();

        // when
        statistics.clear();
        CursorResponse<InstructorDailySessionView> smallPage = queryService.findWithCursor(
                owner.getId(), studio.getId(), null, 1, null, null);
        long smallPageQueryCount = statistics.getPrepareStatementCount();

        entityManager.clear();
        statistics.clear();
        CursorResponse<InstructorDailySessionView> largePage = queryService.findWithCursor(
                owner.getId(), studio.getId(), null, 20, null, null);
        long largePageQueryCount = statistics.getPrepareStatementCount();

        // then
        assertThat(smallPage.items()).hasSize(1);
        assertThat(largePage.items()).hasSize(5);
        assertThat(smallPageQueryCount).isEqualTo(largePageQueryCount);
    }

    @Test
    void 잘못된_페이지_크기와_커서는_거부한다() {
        // given
        Member owner = 회원을_저장한다("session-list-invalid-owner");
        Studio studio = 시설을_저장한다(owner, "전체 수업 잘못된 요청 시설");
        소속을_저장한다(studio, owner, SystemRole.OWNER);
        entityManager.flush();
        entityManager.clear();

        // when & then
        assertCommonInvalidInput(() -> queryService.findWithCursor(
                owner.getId(), studio.getId(), null, 0, null, null));
        assertCommonInvalidInput(() -> queryService.findWithCursor(
                owner.getId(), studio.getId(), null, 101, null, null));
        assertCommonInvalidInput(() -> queryService.findWithCursor(
                owner.getId(), studio.getId(), "invalid-cursor", 20, null, null));
    }

    @Test
    void 학생은_강사용_전체_수업을_조회할_수_없다() {
        // given
        Member owner = 회원을_저장한다("session-list-access-owner");
        Studio studio = 시설을_저장한다(owner, "전체 수업 접근 시설");
        소속을_저장한다(studio, owner, SystemRole.OWNER);
        Member student = 회원을_저장한다("session-list-student");
        소속을_저장한다(studio, student, SystemRole.STUDENT);
        entityManager.flush();
        entityManager.clear();

        // when & then
        assertThatThrownBy(() -> queryService.findWithCursor(
                student.getId(), studio.getId(), null, 20, null, null))
                .isInstanceOfSatisfying(StudioException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(StudioErrorCode.PERMISSION_DENIED));
    }

    @Test
    void 조회할_수업이_없으면_시설_정책이_없어도_빈_페이지를_반환한다() {
        // given
        Member owner = 회원을_저장한다("session-list-empty-owner");
        Studio studio = 시설을_저장한다(owner, "전체 수업 빈 시설");
        소속을_저장한다(studio, owner, SystemRole.OWNER);
        entityManager.flush();
        entityManager.clear();

        // when
        CursorResponse<InstructorDailySessionView> response = queryService.findWithCursor(
                owner.getId(), studio.getId(), null, 20, null, null);

        // then
        assertThat(response.items()).isEmpty();
        assertThat(response.hasNext()).isFalse();
        assertThat(response.nextCursor()).isNull();
    }

    private Member 회원을_저장한다(String id) {
        Member member = StudioFixture.아이디가_다른_소유자(id);
        entityManager.persist(member);
        return member;
    }

    private Studio 시설을_저장한다(Member owner, String name) {
        Studio studio = Studio.builder()
                .owner(owner)
                .name(name)
                .openTime(java.time.LocalTime.of(9, 0))
                .closeTime(java.time.LocalTime.of(22, 0))
                .address(StudioFixture.기본_주소())
                .build();
        entityManager.persist(studio);
        return studio;
    }

    private StudioMembership 소속을_저장한다(Studio studio, Member member, SystemRole systemRole) {
        StudioRole role = 역할을_조회하거나_저장한다(studio, systemRole);
        StudioMembership membership = StudioMembership.builder()
                .studio(studio)
                .member(member)
                .name(member.getName())
                .studioRole(role)
                .status(MembershipStatus.ACTIVE)
                .joinedAt(LocalDateTime.of(2026, 8, 1, 9, 0))
                .build();
        entityManager.persist(membership);
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

    private ClassSession 수업을_저장한다(
            Studio studio,
            StudioMembership instructorMembership,
            ClassType classType,
            String name,
            ClassForm classForm,
            LocalDateTime startAt
    ) {
        ClassSession classSession = classSessionRepository.saveAndFlush(ClassSessionFixture.수업_회차(
                studio.getId(),
                instructorMembership,
                name,
                name + " 안내",
                classForm,
                60,
                classForm == ClassForm.INDIVIDUAL ? 1 : 12,
                startAt
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
    }

    private void 대기_신청을_저장한다(ClassSession classSession, StudioMembership membership) {
        entityManager.persist(ClassSessionEnrollment.waiting(
                membership,
                classSession,
                NOW.minusDays(1)
        ));
    }

    private void 제안_신청을_저장한다(ClassSession classSession, StudioMembership membership) {
        ClassSessionEnrollment enrollment = ClassSessionEnrollment.waiting(
                membership,
                classSession,
                NOW.minusDays(1)
        );
        enrollment.offer(NOW.minusMinutes(5), NOW.plusMinutes(5));
        entityManager.persist(enrollment);
    }

    private void assertCommonInvalidInput(Runnable action) {
        assertThatThrownBy(action::run)
                .isInstanceOfSatisfying(ClassitdaException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(CommonErrorCode.INVALID_INPUT));
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
