package com.classitda.classes.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.classitda.classes.domain.ClassForm;
import com.classitda.classes.domain.session.ClassSession;
import com.classitda.classes.domain.ClassType;
import com.classitda.classes.domain.session.SessionPhase;
import com.classitda.classes.domain.repository.ClassSessionClassTypeRepository;
import com.classitda.classes.domain.repository.ClassSessionRepository;
import com.classitda.classes.domain.repository.ClassTypeRepository;
import com.classitda.classes.exception.ClassErrorCode;
import com.classitda.classes.exception.ClassException;
import com.classitda.classes.fixture.ClassSessionFixture;
import com.classitda.classes.fixture.ClassTypeFixture;
import com.classitda.classes.presentation.dto.ClassSessionDetailResponse;
import com.classitda.classes.presentation.dto.ClassTypeResponse;
import com.classitda.member.domain.Member;
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
import org.junit.jupiter.params.provider.Arguments;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

@Import({
        ClassSessionQueryService.class,
        ClassSessionQueryServiceTest.FixedClockConfig.class
})
@MySqlRepositoryTest
class ClassSessionQueryServiceTest {

    private static final ZoneId SERVICE_ZONE_ID = ZoneId.of("Asia/Seoul");
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 8, 17, 10, 0);
    private static final LocalDate QUERY_DATE = LocalDate.of(2026, 8, 17);

    private final ClassSessionQueryService queryService;
    private final ClassSessionClassTypeRepository classSessionClassTypeRepository;
    private final ClassSessionRepository classSessionRepository;
    private final ClassTypeRepository classTypeRepository;
    private final EntityManager entityManager;

    @Autowired
    ClassSessionQueryServiceTest(
            ClassSessionQueryService queryService,
            ClassSessionClassTypeRepository classSessionClassTypeRepository,
            ClassSessionRepository classSessionRepository,
            ClassTypeRepository classTypeRepository,
            EntityManager entityManager
    ) {
        this.queryService = queryService;
        this.classSessionClassTypeRepository = classSessionClassTypeRepository;
        this.classSessionRepository = classSessionRepository;
        this.classTypeRepository = classTypeRepository;
        this.entityManager = entityManager;
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
                SessionPhase.SCHEDULED
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
                .address(StudioFixture.기본_주소())
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
                .name(member.getName())
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
                LocalDateTime.of(2026, 8, 17, 20, 0)
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
            LocalDateTime startAt
    ) {
        ClassSession classSession = classSessionRepository.saveAndFlush(ClassSessionFixture.수업_회차(
                studio.getId(),
                instructorMembership,
                name,
                name + " 안내",
                classForm,
                60,
                capacity,
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
        entityManager.flush();
    }

    private PassProduct 수강권을_저장한다(
            Studio studio,
            ClassForm classForm,
            List<ClassType> classTypes
    ) {
        PassProduct passProduct = PassProduct.builder()
                .studio(studio)
                .name(classForm + " 회원용 수강권")
                .classForm(classForm)
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
