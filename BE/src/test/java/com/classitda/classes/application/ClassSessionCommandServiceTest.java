package com.classitda.classes.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.classitda.classes.domain.ClassForm;
import com.classitda.classes.domain.ClassType;
import com.classitda.classes.domain.enrollment.ClassSessionEnrollment;
import com.classitda.classes.domain.enrollment.EnrollmentStatus;
import com.classitda.classes.domain.repository.ClassSessionClassTypeRepository;
import com.classitda.classes.domain.repository.ClassSessionEnrollmentRepository;
import com.classitda.classes.domain.repository.ClassSessionRepository;
import com.classitda.classes.domain.repository.ClassTypeRepository;
import com.classitda.classes.domain.session.ClassSession;
import com.classitda.classes.domain.session.ClassSessionClassType;
import com.classitda.classes.exception.ClassErrorCode;
import com.classitda.classes.exception.ClassException;
import com.classitda.classes.fixture.ClassSessionFixture;
import com.classitda.classes.fixture.ClassTypeFixture;
import com.classitda.classes.presentation.dto.ClassSessionCreateV1Request;
import com.classitda.classes.presentation.dto.ClassSessionCreateV2Request;
import com.classitda.classes.presentation.dto.ClassSessionUpdateV1Request;
import com.classitda.classes.presentation.dto.ClassSessionUpdateV2Request;
import com.classitda.member.domain.Member;
import com.classitda.member.domain.repository.MemberRepository;
import com.classitda.studio.domain.MembershipStatus;
import com.classitda.studio.domain.Permission;
import com.classitda.studio.domain.PermissionCode;
import com.classitda.studio.domain.Studio;
import com.classitda.studio.domain.StudioMembership;
import com.classitda.studio.domain.StudioRole;
import com.classitda.studio.domain.StudioRolePermission;
import com.classitda.studio.domain.SystemRole;
import com.classitda.studio.domain.repository.PermissionRepository;
import com.classitda.studio.domain.repository.StudioMembershipRepository;
import com.classitda.studio.domain.repository.StudioRepository;
import com.classitda.studio.domain.repository.StudioRolePermissionRepository;
import com.classitda.studio.domain.repository.StudioRoleRepository;
import com.classitda.studio.exception.StudioErrorCode;
import com.classitda.studio.exception.StudioException;
import com.classitda.studio.fixture.StudioFixture;
import com.classitda.support.MySqlDataJpaTest;
import jakarta.persistence.EntityManager;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Import({
        ClassSessionCommandService.class,
        ClassSessionCommandServiceTest.FixedClockConfig.class
})
@MySqlDataJpaTest
class ClassSessionCommandServiceTest {

    private static final String LINK_FAILURE_CONSTRAINT = "ck_test_reject_class_session_class_type";
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 8, 17, 12, 0);
    private static final ZoneId SERVICE_ZONE_ID = ZoneId.of("Asia/Seoul");

    private final ClassSessionCommandService commandService;
    private final ClassSessionClassTypeRepository classSessionClassTypeRepository;
    private final ClassSessionEnrollmentRepository classSessionEnrollmentRepository;
    private final ClassSessionRepository classSessionRepository;
    private final ClassTypeRepository classTypeRepository;
    private final MemberRepository memberRepository;
    private final StudioMembershipRepository studioMembershipRepository;
    private final StudioRepository studioRepository;
    private final StudioRoleRepository studioRoleRepository;
    private final StudioRolePermissionRepository studioRolePermissionRepository;
    private final PermissionRepository permissionRepository;
    private final EntityManager entityManager;
    private final JdbcTemplate jdbcTemplate;
    private final TransactionTemplate transactionTemplate;

    @Autowired
    ClassSessionCommandServiceTest(
            ClassSessionCommandService commandService,
            ClassSessionClassTypeRepository classSessionClassTypeRepository,
            ClassSessionEnrollmentRepository classSessionEnrollmentRepository,
            ClassSessionRepository classSessionRepository,
            ClassTypeRepository classTypeRepository,
            MemberRepository memberRepository,
            StudioMembershipRepository studioMembershipRepository,
            StudioRepository studioRepository,
            StudioRoleRepository studioRoleRepository,
            StudioRolePermissionRepository studioRolePermissionRepository,
            PermissionRepository permissionRepository,
            EntityManager entityManager,
            JdbcTemplate jdbcTemplate,
            TransactionTemplate transactionTemplate
    ) {
        this.commandService = commandService;
        this.classSessionClassTypeRepository = classSessionClassTypeRepository;
        this.classSessionEnrollmentRepository = classSessionEnrollmentRepository;
        this.classSessionRepository = classSessionRepository;
        this.classTypeRepository = classTypeRepository;
        this.memberRepository = memberRepository;
        this.studioMembershipRepository = studioMembershipRepository;
        this.studioRepository = studioRepository;
        this.studioRoleRepository = studioRoleRepository;
        this.studioRolePermissionRepository = studioRolePermissionRepository;
        this.permissionRepository = permissionRepository;
        this.entityManager = entityManager;
        this.jdbcTemplate = jdbcTemplate;
        this.transactionTemplate = transactionTemplate;
    }

    @Test
    void V1은_요청한_회원_본인을_담당_강사로_수업을_저장한다() {
        // given
        Member owner = 회원을_저장한다("v1-owner");
        StudioContext context = 시설과_대표_소속을_저장한다(owner, "V1 수업 시설");
        ClassType classType = 수업_종류를_저장한다(context.studio(), "요가");
        ClassSessionCreateV1Request request =
                ClassSessionFixture.기본_단일_수업_회차_V1_생성_요청(classType.getId());

        // when
        commandService.saveV1(owner.getId(), context.studio().getId(), request);
        entityManager.flush();
        entityManager.clear();

        // then
        ClassSession saved = classSessionRepository.findAll().getFirst();
        assertThat(saved.getInstructorMembership().getId()).isEqualTo(context.membership().getId());
    }

    @Test
    void V1은_요청한_회원이_강사가_아니면_수업을_저장하지_않는다() {
        // given
        Member owner = 회원을_저장한다("v1-student-owner");
        StudioContext context = 시설과_대표_소속을_저장한다(owner, "V1 비강사 시설");
        StudioRole managerRole = 사용자_역할을_저장한다(context.studio(), "비강사 관리자", false);
        권한을_저장한다(managerRole, PermissionCode.CLASS_SESSION_MANAGE_ALL);
        Member manager = 회원을_저장한다("v1-student-manager");
        소속을_저장한다(context.studio(), manager, managerRole, MembershipStatus.ACTIVE);
        ClassType classType = 수업_종류를_저장한다(context.studio(), "요가");
        ClassSessionCreateV1Request request =
                ClassSessionFixture.기본_단일_수업_회차_V1_생성_요청(classType.getId());

        // when / then
        assertClassError(
                () -> commandService.saveV1(manager.getId(), context.studio().getId(), request),
                ClassErrorCode.CLASS_SESSION_INSTRUCTOR_NOT_FOUND
        );
        assertThat(classSessionRepository.findAll()).isEmpty();
    }

    @Test
    void 반복하지_않는_수업은_요청한_날짜에_선택한_강사_담당으로_한_건을_저장한다() {
        // given
        Member owner = 회원을_저장한다("single-owner");
        StudioContext context = 시설과_대표_소속을_저장한다(owner, "단일 수업 시설");
        ClassType classType = 수업_종류를_저장한다(context.studio(), "요가");

        // when
        commandService.saveV2(owner.getId(), context.studio().getId(),
                ClassSessionFixture.기본_단일_수업_회차_생성_요청(
                        context.membership().getId(), classType.getId()));
        entityManager.flush();
        entityManager.clear();

        // then
        ClassSession saved = classSessionRepository.findAll().getFirst();
        assertThat(saved.getStudioId()).isEqualTo(context.studio().getId());
        assertThat(saved.getInstructorMembership().getId()).isEqualTo(context.membership().getId());
        assertThat(saved.getStartAt()).isEqualTo(LocalDateTime.of(2026, 8, 17, 20, 0));
        assertThat(saved.getEndAt()).isEqualTo(LocalDateTime.of(2026, 8, 17, 21, 0));
        assertThat(saved.isCanceled()).isFalse();
        assertThat(classSessionClassTypeRepository.findAll())
                .extracting(ClassSessionClassType::getClassSessionId, ClassSessionClassType::getClassTypeId)
                .containsExactly(org.assertj.core.groups.Tuple.tuple(saved.getId(), classType.getId()));
    }

    @Test
    void 반복_기간의_양_끝을_포함하고_선택한_요일마다_독립_회차를_저장한다() {
        // given
        Member owner = 회원을_저장한다("recurring-owner");
        StudioContext context = 시설과_대표_소속을_저장한다(owner, "반복 수업 시설");
        ClassType classType = 수업_종류를_저장한다(context.studio(), "필라테스");

        // when
        commandService.saveV2(owner.getId(), context.studio().getId(),
                ClassSessionFixture.기본_반복_수업_회차_생성_요청(
                        context.membership().getId(), classType.getId()));
        entityManager.flush();
        entityManager.clear();

        // then
        List<ClassSession> sessions = classSessionRepository.findAll().stream()
                .sorted(Comparator.comparing(ClassSession::getStartAt))
                .toList();
        assertThat(sessions).extracting(session -> session.getStartAt().toLocalDate())
                .containsExactly(
                        LocalDate.of(2026, 8, 17),
                        LocalDate.of(2026, 8, 19),
                        LocalDate.of(2026, 8, 24)
                );
        assertThat(sessions).extracting(session -> session.getInstructorMembership().getId())
                .containsOnly(context.membership().getId());
        assertThat(classSessionClassTypeRepository.findAll()).hasSize(3);
        assertThat(classSessionClassTypeRepository.findAll())
                .allMatch(link -> link.getClassTypeId().equals(classType.getId()));
    }

    @Test
    void 반복_여부와_일정_필드_조합이_맞지_않으면_저장하지_않는다() {
        // given
        Member owner = 회원을_저장한다("shape-owner");
        StudioContext context = 시설과_대표_소속을_저장한다(owner, "조합 검증 시설");
        ClassType classType = 수업_종류를_저장한다(context.studio(), "발레");
        LocalDate date = LocalDate.of(2026, 8, 17);
        List<ClassSessionCreateV2Request> requests = List.of(
                요청(context.membership().getId(), classType.getId(), null, date, null, null, null),
                요청(context.membership().getId(), classType.getId(), false, null, null, null, null),
                요청(context.membership().getId(), classType.getId(), false, date,
                        List.of(DayOfWeek.MONDAY), null, null),
                요청(context.membership().getId(), classType.getId(), false, date, null, date, null),
                요청(context.membership().getId(), classType.getId(), false, date, null, null, date),
                요청(context.membership().getId(), classType.getId(), true, date,
                        List.of(DayOfWeek.MONDAY), date, date)
        );

        // when / then
        for (ClassSessionCreateV2Request request : requests) {
            assertClassError(
                    () -> commandService.saveV2(owner.getId(), context.studio().getId(), request),
                    ClassErrorCode.INVALID_CLASS_SESSION_RECURRENCE
            );
        }
        assertThat(classSessionRepository.count()).isZero();
        assertThat(classSessionClassTypeRepository.count()).isZero();
    }

    @Test
    void 반복_기간과_요일이_유효하지_않거나_생성할_날짜가_없으면_저장하지_않는다() {
        // given
        Member owner = 회원을_저장한다("recurrence-validation-owner");
        StudioContext context = 시설과_대표_소속을_저장한다(owner, "반복 검증 시설");
        ClassType classType = 수업_종류를_저장한다(context.studio(), "수영");
        LocalDate monday = LocalDate.of(2026, 8, 17);
        List<ClassSessionCreateV2Request> invalidDays = new ArrayList<>();
        invalidDays.add(요청(context.membership().getId(), classType.getId(),
                true, null, null, monday, monday));
        invalidDays.add(요청(context.membership().getId(), classType.getId(),
                true, null, List.of(), monday, monday));
        List<DayOfWeek> nullDays = new ArrayList<>();
        nullDays.add(DayOfWeek.MONDAY);
        nullDays.add(null);
        invalidDays.add(요청(context.membership().getId(), classType.getId(),
                true, null, nullDays, monday, monday));
        invalidDays.add(요청(context.membership().getId(), classType.getId(), true, null,
                List.of(DayOfWeek.MONDAY, DayOfWeek.MONDAY), monday, monday));

        // when / then
        for (ClassSessionCreateV2Request request : invalidDays) {
            assertClassError(
                    () -> commandService.saveV2(owner.getId(), context.studio().getId(), request),
                    ClassErrorCode.INVALID_CLASS_SESSION_RECURRING_DAYS
            );
        }
        assertClassError(
                () -> commandService.saveV2(owner.getId(), context.studio().getId(),
                        요청(context.membership().getId(), classType.getId(), true, null,
                                List.of(DayOfWeek.MONDAY), null, monday)),
                ClassErrorCode.INVALID_CLASS_SESSION_REPEAT_PERIOD
        );
        assertClassError(
                () -> commandService.saveV2(owner.getId(), context.studio().getId(),
                        요청(context.membership().getId(), classType.getId(), true, null,
                                List.of(DayOfWeek.MONDAY), monday, null)),
                ClassErrorCode.INVALID_CLASS_SESSION_REPEAT_PERIOD
        );
        assertClassError(
                () -> commandService.saveV2(owner.getId(), context.studio().getId(),
                        요청(context.membership().getId(), classType.getId(), true, null,
                                List.of(DayOfWeek.MONDAY),
                                monday.plusDays(1), monday)),
                ClassErrorCode.INVALID_CLASS_SESSION_REPEAT_PERIOD
        );
        assertClassError(
                () -> commandService.saveV2(owner.getId(), context.studio().getId(),
                        요청(context.membership().getId(), classType.getId(), true, null,
                                List.of(DayOfWeek.TUESDAY), monday, monday)),
                ClassErrorCode.CLASS_SESSION_DATES_EMPTY
        );
        assertThat(classSessionRepository.count()).isZero();
        assertThat(classSessionClassTypeRepository.count()).isZero();
    }

    @Test
    void 일반_강사는_본인_소속을_담당으로_수업을_생성한다() {
        // given
        Member owner = 회원을_저장한다("own-owner");
        StudioContext context = 시설과_대표_소속을_저장한다(owner, "일반 강사 시설");
        Member instructor = 회원을_저장한다("own-instructor");
        StudioRole role = 역할을_저장한다(context.studio(), SystemRole.INSTRUCTOR);
        권한을_저장한다(role, PermissionCode.CLASS_SESSION_MANAGE_OWN);
        StudioMembership membership = 소속을_저장한다(
                context.studio(), instructor, role, MembershipStatus.ACTIVE);
        ClassType classType = 수업_종류를_저장한다(context.studio(), "요가");

        // when
        commandService.saveV2(instructor.getId(), context.studio().getId(),
                ClassSessionFixture.기본_단일_수업_회차_생성_요청(
                        membership.getId(), classType.getId()));
        entityManager.flush();
        entityManager.clear();

        // then
        assertThat(classSessionRepository.findAll().getFirst().getInstructorMembership().getId())
                .isEqualTo(membership.getId());
    }

    @Test
    void 대표는_같은_시설의_다른_강사를_담당으로_지정한다() {
        // given
        Member owner = 회원을_저장한다("owner-assign-owner");
        StudioContext context = 시설과_대표_소속을_저장한다(owner, "대표 지정 시설");
        Member instructor = 회원을_저장한다("owner-assign-instructor");
        StudioRole instructorRole = 역할을_저장한다(context.studio(), SystemRole.INSTRUCTOR);
        StudioMembership instructorMembership = 소속을_저장한다(
                context.studio(), instructor, instructorRole, MembershipStatus.ACTIVE);
        ClassType classType = 수업_종류를_저장한다(context.studio(), "요가");

        // when
        commandService.saveV2(owner.getId(), context.studio().getId(),
                ClassSessionFixture.기본_단일_수업_회차_생성_요청(
                        instructorMembership.getId(), classType.getId()));
        entityManager.flush();
        entityManager.clear();

        // then
        assertThat(classSessionRepository.findAll().getFirst().getInstructorMembership().getId())
                .isEqualTo(instructorMembership.getId());
    }

    @Test
    void 전체_관리_권한이_있으면_같은_시설의_다른_강사를_담당으로_지정한다() {
        // given
        Member owner = 회원을_저장한다("all-owner");
        StudioContext context = 시설과_대표_소속을_저장한다(owner, "전체 권한 시설");
        Member manager = 회원을_저장한다("all-manager");
        Member other = 회원을_저장한다("all-other");
        StudioRole allRole = 사용자_역할을_저장한다(context.studio(), "전체 수업 관리자", false);
        StudioRole otherRole = 사용자_역할을_저장한다(context.studio(), "다른 강사", true);
        권한을_저장한다(allRole, PermissionCode.CLASS_SESSION_MANAGE_ALL);
        소속을_저장한다(
                context.studio(), manager, allRole, MembershipStatus.ACTIVE);
        StudioMembership otherMembership = 소속을_저장한다(
                context.studio(), other, otherRole, MembershipStatus.ACTIVE);
        ClassType classType = 수업_종류를_저장한다(context.studio(), "댄스");

        // when
        commandService.saveV2(manager.getId(), context.studio().getId(),
                ClassSessionFixture.기본_단일_수업_회차_생성_요청(
                        otherMembership.getId(), classType.getId()));
        entityManager.flush();
        entityManager.clear();

        // then
        assertThat(classSessionRepository.findAll().getFirst().getInstructorMembership().getId())
                .isEqualTo(otherMembership.getId());
    }

    @Test
    void 본인_관리_권한으로_다른_강사를_담당으로_지정할_수_없다() {
        // given
        Member owner = 회원을_저장한다("own-boundary-owner");
        StudioContext context = 시설과_대표_소속을_저장한다(owner, "본인 권한 시설");
        StudioRole instructorRole = 역할을_저장한다(context.studio(), SystemRole.INSTRUCTOR);
        권한을_저장한다(instructorRole, PermissionCode.CLASS_SESSION_MANAGE_OWN);
        Member requester = 회원을_저장한다("own-boundary-requester");
        소속을_저장한다(context.studio(), requester, instructorRole, MembershipStatus.ACTIVE);
        Member other = 회원을_저장한다("own-boundary-other");
        StudioMembership otherMembership = 소속을_저장한다(
                context.studio(), other, instructorRole, MembershipStatus.ACTIVE);
        ClassType classType = 수업_종류를_저장한다(context.studio(), "요가");
        ClassSessionCreateV2Request request = ClassSessionFixture.기본_단일_수업_회차_생성_요청(
                otherMembership.getId(), classType.getId());

        // when / then
        assertStudioError(
                () -> commandService.saveV2(requester.getId(), context.studio().getId(), request),
                StudioErrorCode.PERMISSION_DENIED
        );
        assertThat(classSessionRepository.count()).isZero();
    }

    @Test
    void 비활성_비소속_무권한_사용자는_수업을_생성할_수_없다() {
        // given
        Member owner = 회원을_저장한다("denied-owner");
        StudioContext context = 시설과_대표_소속을_저장한다(owner, "권한 거절 시설");
        ClassType classType = 수업_종류를_저장한다(context.studio(), "권투");
        Member inactive = 회원을_저장한다("inactive");
        StudioRole instructorRole = 역할을_저장한다(context.studio(), SystemRole.INSTRUCTOR);
        소속을_저장한다(context.studio(), inactive, instructorRole, MembershipStatus.INACTIVE);
        Member stranger = 회원을_저장한다("stranger");
        Member student = 회원을_저장한다("student");
        StudioRole studentRole = 역할을_저장한다(context.studio(), SystemRole.STUDENT);
        소속을_저장한다(context.studio(), student, studentRole, MembershipStatus.ACTIVE);
        Member noPermission = 회원을_저장한다("no-permission");
        StudioRole noPermissionRole = 사용자_역할을_저장한다(context.studio(), "무권한 강사", true);
        소속을_저장한다(context.studio(), noPermission, noPermissionRole, MembershipStatus.ACTIVE);
        ClassSessionCreateV2Request request = ClassSessionFixture.기본_단일_수업_회차_생성_요청(
                context.membership().getId(), classType.getId());

        // when / then
        assertStudioError(
                () -> commandService.saveV2(inactive.getId(), context.studio().getId(), request),
                StudioErrorCode.MEMBERSHIP_INACTIVE
        );
        assertStudioError(
                () -> commandService.saveV2(stranger.getId(), context.studio().getId(), request),
                StudioErrorCode.NOT_MEMBERSHIP
        );
        assertStudioError(
                () -> commandService.saveV2(student.getId(), context.studio().getId(), request),
                StudioErrorCode.PERMISSION_DENIED
        );
        assertStudioError(
                () -> commandService.saveV2(noPermission.getId(), context.studio().getId(), request),
                StudioErrorCode.PERMISSION_DENIED
        );
        assertThat(classSessionRepository.count()).isZero();
    }

    @Test
    void 다른_시설이거나_비활성_또는_학생인_소속은_담당_강사로_지정할_수_없다() {
        // given
        Member owner = 회원을_저장한다("instructor-boundary-owner");
        StudioContext context = 시설과_대표_소속을_저장한다(owner, "담당 강사 경계 시설");
        ClassType classType = 수업_종류를_저장한다(context.studio(), "요가");
        StudioRole instructorRole = 역할을_저장한다(context.studio(), SystemRole.INSTRUCTOR);
        Member inactiveInstructor = 회원을_저장한다("inactive-target");
        StudioMembership inactiveMembership = 소속을_저장한다(
                context.studio(), inactiveInstructor, instructorRole, MembershipStatus.INACTIVE);
        StudioRole studentRole = 역할을_저장한다(context.studio(), SystemRole.STUDENT);
        Member student = 회원을_저장한다("student-target");
        StudioMembership studentMembership = 소속을_저장한다(
                context.studio(), student, studentRole, MembershipStatus.ACTIVE);
        Member otherOwner = 회원을_저장한다("other-studio-owner");
        StudioContext otherContext = 시설과_대표_소속을_저장한다(otherOwner, "다른 시설");
        List<Long> invalidInstructorMembershipIds = List.of(
                inactiveMembership.getId(),
                studentMembership.getId(),
                otherContext.membership().getId(),
                Long.MAX_VALUE
        );

        // when / then
        for (Long instructorMembershipId : invalidInstructorMembershipIds) {
            assertClassError(
                    () -> commandService.saveV2(
                            owner.getId(),
                            context.studio().getId(),
                            ClassSessionFixture.기본_단일_수업_회차_생성_요청(
                                    instructorMembershipId, classType.getId())
                    ),
                    ClassErrorCode.CLASS_SESSION_INSTRUCTOR_NOT_FOUND
            );
        }
        assertThat(classSessionRepository.count()).isZero();
    }

    @Test
    void 다른_시설이나_없는_수업_종류는_찾을_수_없다() {
        // given
        Member owner = 회원을_저장한다("boundary-owner");
        StudioContext requested = 시설과_대표_소속을_저장한다(owner, "요청 시설");
        Studio other = 시설을_저장한다(owner, "다른 시설");
        ClassType otherType = 수업_종류를_저장한다(other, "필라테스");

        // when / then
        assertClassError(
                () -> commandService.saveV2(owner.getId(), requested.studio().getId(),
                        단일_요청(requested.membership().getId(),
                                otherType.getId(), "수업 종류 경계")),
                ClassErrorCode.CLASS_TYPE_NOT_FOUND
        );
        assertClassError(
                () -> commandService.saveV2(owner.getId(), requested.studio().getId(),
                        단일_요청(requested.membership().getId(),
                                Long.MAX_VALUE, "없는 수업 종류")),
                ClassErrorCode.CLASS_TYPE_NOT_FOUND
        );
        assertThat(classSessionRepository.count()).isZero();
    }

    @Test
    void 기존_활성_수업과_정확히_겹치거나_부분적으로_겹치면_생성하지_않는다() {
        // given
        Member owner = 회원을_저장한다("overlap-owner");
        StudioContext context = 시설과_대표_소속을_저장한다(owner, "충돌 시설");
        ClassType classType = 수업_종류를_저장한다(context.studio(), "요가");
        수업을_저장한다(context, classType, LocalDateTime.of(2026, 8, 17, 10, 0),
                60, "기존 수업");

        // when / then
        assertClassError(
                () -> commandService.saveV2(owner.getId(), context.studio().getId(),
                        단일_요청(context.membership().getId(), classType.getId(),
                                LocalDate.of(2026, 8, 17), LocalTime.of(10, 0), 60)),
                ClassErrorCode.CLASS_SESSION_TIME_CONFLICT
        );
        assertClassError(
                () -> commandService.saveV2(owner.getId(), context.studio().getId(),
                        단일_요청(context.membership().getId(), classType.getId(),
                                LocalDate.of(2026, 8, 17), LocalTime.of(10, 30), 60)),
                ClassErrorCode.CLASS_SESSION_TIME_CONFLICT
        );
        assertThat(classSessionRepository.count()).isEqualTo(1);
        assertThat(classSessionClassTypeRepository.count()).isEqualTo(1);
    }

    @Test
    void 전체_관리자가_다른_강사를_지정하면_선택된_강사의_시간_충돌을_검사한다() {
        // given
        Member owner = 회원을_저장한다("selected-overlap-owner");
        StudioContext context = 시설과_대표_소속을_저장한다(owner, "선택 강사 충돌 시설");
        Member manager = 회원을_저장한다("selected-overlap-manager");
        StudioRole managerRole = 사용자_역할을_저장한다(context.studio(), "전체 수업 관리자", false);
        권한을_저장한다(managerRole, PermissionCode.CLASS_SESSION_MANAGE_ALL);
        소속을_저장한다(context.studio(), manager, managerRole, MembershipStatus.ACTIVE);
        Member instructor = 회원을_저장한다("selected-overlap-instructor");
        StudioRole instructorRole = 사용자_역할을_저장한다(context.studio(), "담당 강사", true);
        StudioMembership instructorMembership = 소속을_저장한다(
                context.studio(), instructor, instructorRole, MembershipStatus.ACTIVE);
        ClassType classType = 수업_종류를_저장한다(context.studio(), "요가");
        수업을_저장한다(
                context,
                instructorMembership,
                classType,
                LocalDateTime.of(2026, 8, 17, 10, 0),
                60,
                "선택 강사의 기존 수업"
        );

        // when / then
        assertClassError(
                () -> commandService.saveV2(
                        manager.getId(),
                        context.studio().getId(),
                        단일_요청(instructorMembership.getId(), classType.getId(),
                                LocalDate.of(2026, 8, 17), LocalTime.of(10, 30), 60)
                ),
                ClassErrorCode.CLASS_SESSION_TIME_CONFLICT
        );
        assertThat(classSessionRepository.count()).isEqualTo(1);
    }

    @Test
    void 요청자의_수업과_겹쳐도_선택된_강사의_일정이_비어_있으면_생성한다() {
        // given
        Member owner = 회원을_저장한다("requester-overlap-owner");
        StudioContext context = 시설과_대표_소속을_저장한다(owner, "요청자 충돌 시설");
        Member instructor = 회원을_저장한다("requester-overlap-instructor");
        StudioRole instructorRole = 역할을_저장한다(context.studio(), SystemRole.INSTRUCTOR);
        StudioMembership instructorMembership = 소속을_저장한다(
                context.studio(), instructor, instructorRole, MembershipStatus.ACTIVE);
        ClassType classType = 수업_종류를_저장한다(context.studio(), "요가");
        수업을_저장한다(context, classType, LocalDateTime.of(2026, 8, 17, 10, 0),
                60, "요청자의 기존 수업");

        // when
        commandService.saveV2(
                owner.getId(),
                context.studio().getId(),
                단일_요청(instructorMembership.getId(), classType.getId(),
                        LocalDate.of(2026, 8, 17), LocalTime.of(10, 30), 60)
        );
        entityManager.flush();
        entityManager.clear();

        // then
        assertThat(classSessionRepository.findAll()).hasSize(2);
        assertThat(classSessionRepository.findAll())
                .filteredOn(session -> session.getName().equals("시간 검증 수업"))
                .extracting(session -> session.getInstructorMembership().getId())
                .containsExactly(instructorMembership.getId());
    }

    @Test
    void 기존_수업과_인접하거나_취소된_수업과_겹치면_생성할_수_있다() {
        // given
        Member owner = 회원을_저장한다("allowed-overlap-owner");
        StudioContext context = 시설과_대표_소속을_저장한다(owner, "인접 시설");
        ClassType classType = 수업_종류를_저장한다(context.studio(), "요가");
        수업을_저장한다(context, classType, LocalDateTime.of(2026, 8, 17, 10, 0),
                60, "활성 수업");
        ClassSession canceled = 수업을_저장한다(context, classType, LocalDateTime.of(2026, 8, 18, 10, 0),
                60, "취소 수업");
        canceled.cancel(canceled.getStartAt().minusMinutes(1));

        // when
        commandService.saveV2(owner.getId(), context.studio().getId(),
                단일_요청(context.membership().getId(), classType.getId(),
                        LocalDate.of(2026, 8, 17), LocalTime.of(11, 0), 60));
        commandService.saveV2(owner.getId(), context.studio().getId(),
                단일_요청(context.membership().getId(), classType.getId(),
                        LocalDate.of(2026, 8, 18), LocalTime.of(10, 30), 60));
        entityManager.flush();

        // then
        assertThat(classSessionRepository.count()).isEqualTo(4);
        assertThat(classSessionClassTypeRepository.count()).isEqualTo(4);
    }

    @Test
    void 반복_배치_중_나중_날짜에_충돌하면_새_수업과_연결을_하나도_저장하지_않는다() {
        // given
        Member owner = 회원을_저장한다("batch-conflict-owner");
        StudioContext context = 시설과_대표_소속을_저장한다(owner, "배치 롤백 시설");
        ClassType classType = 수업_종류를_저장한다(context.studio(), "요가");
        수업을_저장한다(context, classType, LocalDateTime.of(2026, 8, 19, 20, 0),
                60, "나중 충돌 수업");

        // when / then
        assertClassError(
                () -> commandService.saveV2(owner.getId(), context.studio().getId(),
                        ClassSessionFixture.기본_반복_수업_회차_생성_요청(
                                context.membership().getId(), classType.getId())),
                ClassErrorCode.CLASS_SESSION_TIME_CONFLICT
        );
        assertThat(classSessionRepository.count()).isEqualTo(1);
        assertThat(classSessionClassTypeRepository.count()).isEqualTo(1);
    }

    @Test
    void 반복_수업의_진행_시간이_1440분을_초과하면_모두_거절한다() {
        // given
        Member owner = 회원을_저장한다("self-overlap-owner");
        StudioContext context = 시설과_대표_소속을_저장한다(owner, "장기 수업 시설");
        ClassType classType = 수업_종류를_저장한다(context.studio(), "장기 수업");
        ClassSessionCreateV2Request request = ClassSessionFixture.수업_회차_생성_요청(
                context.membership().getId(),
                ClassForm.GROUP, classType.getId(), "하루보다 긴 수업", 10, 1_441,
                true, LocalTime.MIDNIGHT, null, null, List.of(DayOfWeek.MONDAY),
                LocalDate.of(2026, 8, 17), LocalDate.of(2026, 8, 24));

        // when / then
        assertClassError(
                () -> commandService.saveV2(owner.getId(), context.studio().getId(), request),
                ClassErrorCode.INVALID_CLASS_SESSION_DURATION_MINUTES
        );
        assertThat(classSessionRepository.count()).isZero();
        assertThat(classSessionClassTypeRepository.count()).isZero();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void 수업_종류_연결_저장에_실패하면_앞서_저장한_수업도_롤백한다() {
        // given
        PersistenceSetup setup = transactionTemplate.execute(status -> {
            Member owner = 회원을_저장한다("link-rollback-owner");
            StudioContext context = 시설과_대표_소속을_저장한다(owner, "연결 롤백 시설");
            ClassType classType = 수업_종류를_저장한다(context.studio(), "롤백 요가");
            return new PersistenceSetup(
                    owner.getId(), context.studio().getId(), context.membership().getId(),
                    context.role().getId(), classType.getId());
        });
        연결_저장_실패_제약을_삭제한다();
        연결_저장_실패_제약을_설치한다();

        try {
            // when / then
            assertThatThrownBy(() -> commandService.saveV2(
                    setup.memberId(), setup.studioId(),
                    ClassSessionFixture.기본_단일_수업_회차_생성_요청(
                            setup.membershipId(), setup.classTypeId())))
                    .isInstanceOf(DataAccessException.class);
            assertThat(classSessionRepository.findAll())
                    .noneMatch(session -> session.getStudioId().equals(setup.studioId()));
            assertThat(classSessionClassTypeRepository.count()).isZero();
        } finally {
            연결_저장_실패_제약을_삭제한다();
            assertThat(연결_저장_실패_제약_수()).isZero();
            transactionTemplate.executeWithoutResult(status -> 영속_설정을_정리한다(setup));
        }
    }

    @Test
    void 개별_수업_회차의_정보와_수업_종류를_수정하고_종료_시각을_다시_계산한다() {
        // given
        Member owner = 회원을_저장한다("update-owner");
        StudioContext context = 시설과_대표_소속을_저장한다(owner, "수정 시설");
        ClassType oldClassType = 수업_종류를_저장한다(context.studio(), "기존 요가");
        ClassType newClassType = 수업_종류를_저장한다(context.studio(), "변경 필라테스");
        ClassSession classSession = 수업을_저장한다(
                context,
                oldClassType,
                LocalDateTime.of(2026, 8, 17, 20, 0),
                60,
                "기존 수업"
        );
        Long originalInstructorMembershipId = classSession.getInstructorMembership().getId();
        ClassSessionUpdateV1Request request = ClassSessionFixture
                .기본_수업_회차_수정_요청(newClassType.getId());

        // when
        commandService.updateV1(
                owner.getId(), context.studio().getId(), classSession.getId(), request);
        entityManager.flush();
        entityManager.clear();

        // then
        ClassSession updated = classSessionRepository.findById(classSession.getId()).orElseThrow();
        assertThat(updated.getInstructorMembership().getId()).isEqualTo(originalInstructorMembershipId);
        assertThat(updated.getName()).isEqualTo("수정된 개인 수업");
        assertThat(updated.getDescription()).isEqualTo("수정된 수업 안내");
        assertThat(updated.getClassForm()).isEqualTo(ClassForm.INDIVIDUAL);
        assertThat(updated.getCapacity()).isEqualTo(1);
        assertThat(updated.getDurationMinutes()).isEqualTo(50);
        assertThat(updated.getStartAt()).isEqualTo(LocalDateTime.of(2026, 8, 18, 19, 30));
        assertThat(updated.getEndAt()).isEqualTo(LocalDateTime.of(2026, 8, 18, 20, 20));
        assertThat(classSessionClassTypeRepository.findByClassSessionId(classSession.getId()))
                .get()
                .extracting(ClassSessionClassType::getClassTypeId)
                .isEqualTo(newClassType.getId());
        assertThat(classSessionRepository.count()).isEqualTo(1);
    }

    @ParameterizedTest
    @EnumSource(value = EnrollmentStatus.class, names = {"WAITING", "OFFERED", "RESERVED"})
    void V2_활성_신청자가_있으면_수업을_수정할_수_없다(EnrollmentStatus enrollmentStatus) {
        // given
        Member owner = 회원을_저장한다("update-form-" + enrollmentStatus);
        StudioContext context = 시설과_대표_소속을_저장한다(owner, "수업 형태 변경 제한 시설");
        ClassType classType = 수업_종류를_저장한다(context.studio(), "요가");
        ClassSession classSession = 수업을_저장한다(
                context, classType, LocalDateTime.of(2026, 8, 17, 20, 0), 60, "그룹 수업");
        StudioMembership studentMembership = 학생_소속을_저장한다(
                context.studio(), "update-form-student-" + enrollmentStatus);
        신청을_저장한다(studentMembership, classSession, enrollmentStatus);

        ClassSessionUpdateV2Request request = ClassSessionUpdateV2Request.of(
                context.membership().getId(),
                ClassForm.INDIVIDUAL,
                classType.getId(),
                "신청 후 수정 수업",
                10,
                50,
                LocalDateTime.of(2026, 8, 18, 19, 30),
                "수정 안내"
        );

        // when / then
        assertClassError(
                () -> commandService.updateV2(
                        owner.getId(), context.studio().getId(), classSession.getId(), request),
                ClassErrorCode.CLASS_SESSION_HAS_ACTIVE_ENROLLMENT
        );
        assertThat(classSession.getClassForm()).isEqualTo(ClassForm.GROUP);
        assertThat(classSessionClassTypeRepository.findByClassSessionId(classSession.getId()))
                .get()
                .extracting(ClassSessionClassType::getClassTypeId)
                .isEqualTo(classType.getId());
    }

    @Test
    void 활성_신청자가_있으면_수업_종류와_형태를_유지해도_다른_정보를_수정할_수_없다() {
        // given
        Member owner = 회원을_저장한다("update-details-owner");
        StudioContext context = 시설과_대표_소속을_저장한다(owner, "신청 수업 정보 수정 시설");
        ClassType classType = 수업_종류를_저장한다(context.studio(), "요가");
        ClassSession classSession = 수업을_저장한다(
                context, classType, LocalDateTime.of(2026, 8, 17, 20, 0), 60, "기존 그룹 수업");
        StudioMembership studentMembership = 학생_소속을_저장한다(
                context.studio(), "update-details-student");
        신청을_저장한다(studentMembership, classSession, EnrollmentStatus.RESERVED);

        ClassSessionUpdateV1Request request = 수정_요청(ClassForm.GROUP, classType.getId());

        // when / then
        assertClassError(
                () -> commandService.updateV1(
                        owner.getId(), context.studio().getId(), classSession.getId(), request),
                ClassErrorCode.CLASS_SESSION_HAS_ACTIVE_ENROLLMENT
        );
        assertThat(classSession.getName()).isEqualTo("기존 그룹 수업");
        assertThat(classSession.getClassForm()).isEqualTo(ClassForm.GROUP);
    }

    @ParameterizedTest
    @EnumSource(value = EnrollmentStatus.class, names = {"CANCELED", "EXPIRED"})
    void 종료된_신청만_있으면_수업의_모든_정보를_변경할_수_있다(EnrollmentStatus enrollmentStatus) {
        // given
        Member owner = 회원을_저장한다("update-terminal-" + enrollmentStatus);
        StudioContext context = 시설과_대표_소속을_저장한다(owner, "종료 신청 수업 수정 시설");
        ClassType oldClassType = 수업_종류를_저장한다(context.studio(), "기존 요가");
        ClassType newClassType = 수업_종류를_저장한다(context.studio(), "변경 필라테스");
        ClassSession classSession = 수업을_저장한다(
                context, oldClassType, LocalDateTime.of(2026, 8, 17, 20, 0), 60, "그룹 수업");
        StudioMembership studentMembership = 학생_소속을_저장한다(
                context.studio(), "update-terminal-student-" + enrollmentStatus);
        신청을_저장한다(studentMembership, classSession, enrollmentStatus);

        ClassSessionUpdateV1Request request = 수정_요청(ClassForm.INDIVIDUAL, newClassType.getId());

        // when
        commandService.updateV1(
                owner.getId(), context.studio().getId(), classSession.getId(), request);

        // then
        assertThat(classSession.getClassForm()).isEqualTo(ClassForm.INDIVIDUAL);
        assertThat(classSessionClassTypeRepository.findByClassSessionId(classSession.getId()))
                .get()
                .extracting(ClassSessionClassType::getClassTypeId)
                .isEqualTo(newClassType.getId());
    }

    @Test
    void V2_대표가_수업_회차의_담당_강사를_변경한다() {
        // given
        Member owner = 회원을_저장한다("update-v2-owner");
        StudioContext context = 시설과_대표_소속을_저장한다(owner, "V2 강사 변경 시설");
        StudioRole instructorRole = 역할을_저장한다(context.studio(), SystemRole.INSTRUCTOR);
        Member nextInstructor = 회원을_저장한다("update-v2-next-instructor");
        StudioMembership nextInstructorMembership = 소속을_저장한다(
                context.studio(), nextInstructor, instructorRole, MembershipStatus.ACTIVE);
        ClassType classType = 수업_종류를_저장한다(context.studio(), "V2 필라테스");
        ClassSession classSession = 수업을_저장한다(
                context, classType, LocalDateTime.of(2026, 8, 17, 20, 0), 60, "기존 수업");
        ClassSessionUpdateV2Request request = ClassSessionFixture.기본_수업_회차_V2_수정_요청(
                nextInstructorMembership.getId(), classType.getId());

        // when
        commandService.updateV2(
                owner.getId(), context.studio().getId(), classSession.getId(), request);
        entityManager.flush();
        entityManager.clear();

        // then
        ClassSession updated = classSessionRepository.findById(classSession.getId()).orElseThrow();
        assertThat(updated.getInstructorMembership().getId()).isEqualTo(nextInstructorMembership.getId());
        assertThat(updated.getName()).isEqualTo("수정된 개인 수업");
    }

    @Test
    void V2_변경할_강사의_다른_수업과_시간이_겹치면_담당_강사를_변경하지_않는다() {
        // given
        Member owner = 회원을_저장한다("update-v2-conflict-owner");
        StudioContext context = 시설과_대표_소속을_저장한다(owner, "V2 강사 충돌 시설");
        StudioRole instructorRole = 역할을_저장한다(context.studio(), SystemRole.INSTRUCTOR);
        Member nextInstructor = 회원을_저장한다("update-v2-conflict-instructor");
        StudioMembership nextInstructorMembership = 소속을_저장한다(
                context.studio(), nextInstructor, instructorRole, MembershipStatus.ACTIVE);
        ClassType classType = 수업_종류를_저장한다(context.studio(), "V2 충돌 요가");
        ClassSession target = 수업을_저장한다(
                context, classType, LocalDateTime.of(2026, 8, 17, 10, 0), 60, "수정 대상");
        수업을_저장한다(
                context,
                nextInstructorMembership,
                classType,
                LocalDateTime.of(2026, 8, 18, 19, 0),
                60,
                "새 강사의 기존 수업"
        );
        ClassSessionUpdateV2Request request = ClassSessionFixture.기본_수업_회차_V2_수정_요청(
                nextInstructorMembership.getId(), classType.getId());
        Long originalInstructorMembershipId = target.getInstructorMembership().getId();

        // when / then
        assertClassError(
                () -> commandService.updateV2(
                        owner.getId(), context.studio().getId(), target.getId(), request),
                ClassErrorCode.CLASS_SESSION_TIME_CONFLICT
        );
        assertThat(target.getInstructorMembership().getId()).isEqualTo(originalInstructorMembershipId);
        assertThat(target.getName()).isEqualTo("수정 대상");
    }

    @Test
    void V2_활성_강사가_아닌_소속으로는_담당_강사를_변경할_수_없다() {
        // given
        Member owner = 회원을_저장한다("update-v2-invalid-instructor-owner");
        StudioContext context = 시설과_대표_소속을_저장한다(owner, "V2 강사 검증 시설");
        StudioRole studentRole = 역할을_저장한다(context.studio(), SystemRole.STUDENT);
        Member student = 회원을_저장한다("update-v2-invalid-instructor-student");
        StudioMembership studentMembership = 소속을_저장한다(
                context.studio(), student, studentRole, MembershipStatus.ACTIVE);
        ClassType classType = 수업_종류를_저장한다(context.studio(), "V2 강사 검증 요가");
        ClassSession target = 수업을_저장한다(
                context, classType, LocalDateTime.of(2026, 8, 17, 20, 0), 60, "수정 대상");
        ClassSessionUpdateV2Request request = ClassSessionFixture.기본_수업_회차_V2_수정_요청(
                studentMembership.getId(), classType.getId());

        // when / then
        assertClassError(
                () -> commandService.updateV2(
                        owner.getId(), context.studio().getId(), target.getId(), request),
                ClassErrorCode.CLASS_SESSION_INSTRUCTOR_NOT_FOUND
        );
        assertThat(target.getInstructorMembership().getId()).isEqualTo(context.membership().getId());
    }

    @Test
    void V2_본인_수업_관리_권한자는_담당_강사를_유지하면_자기_수업을_수정할_수_있다() {
        // given
        Member owner = 회원을_저장한다("update-v2-own-unchanged-owner");
        StudioContext context = 시설과_대표_소속을_저장한다(owner, "V2 본인 권한 유지 시설");
        StudioRole instructorRole = 역할을_저장한다(context.studio(), SystemRole.INSTRUCTOR);
        권한을_저장한다(instructorRole, PermissionCode.CLASS_SESSION_MANAGE_OWN);
        Member requester = 회원을_저장한다("update-v2-own-unchanged-requester");
        StudioMembership requesterMembership = 소속을_저장한다(
                context.studio(), requester, instructorRole, MembershipStatus.ACTIVE);
        ClassType classType = 수업_종류를_저장한다(context.studio(), "V2 본인 권한 유지 요가");
        ClassSession ownSession = 수업을_저장한다(
                context,
                requesterMembership,
                classType,
                LocalDateTime.of(2026, 8, 17, 20, 0),
                60,
                "본인 수업"
        );
        ClassSessionUpdateV2Request request = ClassSessionFixture.기본_수업_회차_V2_수정_요청(
                requesterMembership.getId(), classType.getId());

        // when
        commandService.updateV2(
                requester.getId(), context.studio().getId(), ownSession.getId(), request);

        // then
        assertThat(ownSession.getInstructorMembership().getId()).isEqualTo(requesterMembership.getId());
        assertThat(ownSession.getName()).isEqualTo("수정된 개인 수업");
    }

    @Test
    void V2_본인_수업_관리_권한자는_자기_수업을_다른_강사에게_변경할_수_없다() {
        // given
        Member owner = 회원을_저장한다("update-v2-own-target-owner");
        StudioContext context = 시설과_대표_소속을_저장한다(owner, "V2 본인 권한 대상 시설");
        StudioRole instructorRole = 역할을_저장한다(context.studio(), SystemRole.INSTRUCTOR);
        권한을_저장한다(instructorRole, PermissionCode.CLASS_SESSION_MANAGE_OWN);
        Member requester = 회원을_저장한다("update-v2-own-target-requester");
        StudioMembership requesterMembership = 소속을_저장한다(
                context.studio(), requester, instructorRole, MembershipStatus.ACTIVE);
        Member otherInstructor = 회원을_저장한다("update-v2-own-target-other");
        StudioMembership otherMembership = 소속을_저장한다(
                context.studio(), otherInstructor, instructorRole, MembershipStatus.ACTIVE);
        ClassType classType = 수업_종류를_저장한다(context.studio(), "V2 본인 권한 요가");
        ClassSession ownSession = 수업을_저장한다(
                context,
                requesterMembership,
                classType,
                LocalDateTime.of(2026, 8, 17, 20, 0),
                60,
                "본인 수업"
        );
        ClassSessionUpdateV2Request request = ClassSessionFixture.기본_수업_회차_V2_수정_요청(
                otherMembership.getId(), classType.getId());

        // when / then
        assertStudioError(
                () -> commandService.updateV2(
                        requester.getId(), context.studio().getId(), ownSession.getId(), request),
                StudioErrorCode.PERMISSION_DENIED
        );
        assertThat(ownSession.getInstructorMembership().getId()).isEqualTo(requesterMembership.getId());
    }

    @Test
    void V2_본인_수업_관리_권한자는_다른_강사의_수업을_자기에게_변경할_수_없다() {
        // given
        Member owner = 회원을_저장한다("update-v2-own-source-owner");
        StudioContext context = 시설과_대표_소속을_저장한다(owner, "V2 본인 권한 기존 시설");
        StudioRole instructorRole = 역할을_저장한다(context.studio(), SystemRole.INSTRUCTOR);
        권한을_저장한다(instructorRole, PermissionCode.CLASS_SESSION_MANAGE_OWN);
        Member requester = 회원을_저장한다("update-v2-own-source-requester");
        StudioMembership requesterMembership = 소속을_저장한다(
                context.studio(), requester, instructorRole, MembershipStatus.ACTIVE);
        ClassType classType = 수업_종류를_저장한다(context.studio(), "V2 본인 권한 기존 요가");
        ClassSession otherSession = 수업을_저장한다(
                context, classType, LocalDateTime.of(2026, 8, 17, 20, 0), 60, "다른 강사의 수업");
        ClassSessionUpdateV2Request request = ClassSessionFixture.기본_수업_회차_V2_수정_요청(
                requesterMembership.getId(), classType.getId());

        // when / then
        assertStudioError(
                () -> commandService.updateV2(
                        requester.getId(), context.studio().getId(), otherSession.getId(), request),
                StudioErrorCode.PERMISSION_DENIED
        );
        assertThat(otherSession.getInstructorMembership().getId()).isEqualTo(context.membership().getId());
    }

    @Test
    void V2_전체_수업_관리_권한자는_다른_강사의_수업을_새_강사에게_변경할_수_있다() {
        // given
        Member owner = 회원을_저장한다("update-v2-all-owner");
        StudioContext context = 시설과_대표_소속을_저장한다(owner, "V2 전체 권한 시설");
        StudioRole managerRole = 사용자_역할을_저장한다(
                context.studio(), "V2 전체 수업 관리자", false);
        권한을_저장한다(managerRole, PermissionCode.CLASS_SESSION_MANAGE_ALL);
        Member manager = 회원을_저장한다("update-v2-all-manager");
        소속을_저장한다(context.studio(), manager, managerRole, MembershipStatus.ACTIVE);
        StudioRole instructorRole = 역할을_저장한다(context.studio(), SystemRole.INSTRUCTOR);
        Member nextInstructor = 회원을_저장한다("update-v2-all-instructor");
        StudioMembership nextMembership = 소속을_저장한다(
                context.studio(), nextInstructor, instructorRole, MembershipStatus.ACTIVE);
        ClassType classType = 수업_종류를_저장한다(context.studio(), "V2 전체 권한 요가");
        ClassSession target = 수업을_저장한다(
                context, classType, LocalDateTime.of(2026, 8, 17, 20, 0), 60, "변경 대상");
        ClassSessionUpdateV2Request request = ClassSessionFixture.기본_수업_회차_V2_수정_요청(
                nextMembership.getId(), classType.getId());

        // when
        commandService.updateV2(
                manager.getId(), context.studio().getId(), target.getId(), request);

        // then
        assertThat(target.getInstructorMembership().getId()).isEqualTo(nextMembership.getId());
    }

    @Test
    void null을_전달하면_수업_안내를_삭제한다() {
        // given
        Member owner = 회원을_저장한다("clear-description-owner");
        StudioContext context = 시설과_대표_소속을_저장한다(owner, "수업 안내 삭제 시설");
        ClassType classType = 수업_종류를_저장한다(context.studio(), "수업 안내 삭제 요가");
        ClassSession classSession = 수업을_저장한다(
                context,
                classType,
                LocalDateTime.of(2026, 8, 17, 20, 0),
                60,
                "수업 안내 삭제 대상",
                "삭제할 수업 안내"
        );
        ClassSessionUpdateV1Request request = ClassSessionUpdateV1Request.of(
                ClassForm.GROUP,
                classType.getId(),
                "수업 안내 삭제 대상",
                10,
                60,
                LocalDateTime.of(2026, 8, 17, 20, 0),
                null
        );

        // when
        commandService.updateV1(
                owner.getId(), context.studio().getId(), classSession.getId(), request);
        entityManager.flush();
        entityManager.clear();

        // then
        ClassSession updated = classSessionRepository.findById(classSession.getId()).orElseThrow();
        assertThat(updated.getDescription()).isNull();
    }

    @Test
    void 본인_수업_관리_권한자는_자신이_담당하는_수업을_수정할_수_있다() {
        // given
        Member owner = 회원을_저장한다("update-own-allowed-owner");
        StudioContext context = 시설과_대표_소속을_저장한다(owner, "본인 수정 허용 시설");
        StudioRole instructorRole = 역할을_저장한다(context.studio(), SystemRole.INSTRUCTOR);
        권한을_저장한다(instructorRole, PermissionCode.CLASS_SESSION_MANAGE_OWN);
        Member requester = 회원을_저장한다("update-own-allowed-requester");
        StudioMembership requesterMembership = 소속을_저장한다(
                context.studio(), requester, instructorRole, MembershipStatus.ACTIVE);
        ClassType classType = 수업_종류를_저장한다(context.studio(), "요가");
        ClassSession ownSession = 수업을_저장한다(
                context,
                requesterMembership,
                classType,
                LocalDateTime.of(2026, 8, 17, 20, 0),
                60,
                "본인 수업"
        );
        ClassSessionUpdateV1Request request = ClassSessionFixture
                .기본_수업_회차_수정_요청(classType.getId());

        // when
        commandService.updateV1(
                requester.getId(), context.studio().getId(), ownSession.getId(), request);

        // then
        assertThat(ownSession.getName()).isEqualTo("수정된 개인 수업");
    }

    @Test
    void 본인_수업_관리_권한자는_다른_강사의_수업을_수정할_수_없다() {
        // given
        Member owner = 회원을_저장한다("update-own-owner");
        StudioContext context = 시설과_대표_소속을_저장한다(owner, "본인 수정 권한 시설");
        StudioRole instructorRole = 역할을_저장한다(context.studio(), SystemRole.INSTRUCTOR);
        권한을_저장한다(instructorRole, PermissionCode.CLASS_SESSION_MANAGE_OWN);
        Member requester = 회원을_저장한다("update-own-requester");
        소속을_저장한다(context.studio(), requester, instructorRole, MembershipStatus.ACTIVE);
        ClassType classType = 수업_종류를_저장한다(context.studio(), "요가");
        ClassSession otherSession = 수업을_저장한다(
                context,
                classType,
                LocalDateTime.of(2026, 8, 17, 20, 0),
                60,
                "다른 강사의 수업"
        );
        ClassSessionUpdateV1Request request = ClassSessionFixture
                .기본_수업_회차_수정_요청(classType.getId());

        // when / then
        assertStudioError(
                () -> commandService.updateV1(
                        requester.getId(), context.studio().getId(), otherSession.getId(), request),
                StudioErrorCode.PERMISSION_DENIED
        );
        assertThat(otherSession.getName()).isEqualTo("다른 강사의 수업");
    }

    @Test
    void 기존_시간대를_유지하는_수정은_자기_회차를_시간_충돌로_판단하지_않는다() {
        // given
        Member owner = 회원을_저장한다("update-self-overlap-owner");
        StudioContext context = 시설과_대표_소속을_저장한다(owner, "자기 회차 제외 시설");
        ClassType classType = 수업_종류를_저장한다(context.studio(), "요가");
        ClassSession target = 수업을_저장한다(
                context,
                classType,
                LocalDateTime.of(2026, 8, 17, 20, 0),
                60,
                "수정 전 이름"
        );
        ClassSessionUpdateV1Request request = ClassSessionUpdateV1Request.of(
                ClassForm.GROUP,
                classType.getId(),
                "수정 후 이름",
                10,
                60,
                LocalDateTime.of(2026, 8, 17, 20, 0),
                null
        );

        // when
        commandService.updateV1(
                owner.getId(), context.studio().getId(), target.getId(), request);

        // then
        assertThat(target.getName()).isEqualTo("수정 후 이름");
        assertThat(target.getStartAt()).isEqualTo(LocalDateTime.of(2026, 8, 17, 20, 0));
    }

    @Test
    void 수정_대상을_제외한_담당_강사의_활성_수업과_시간이_겹치면_수정하지_않는다() {
        // given
        Member owner = 회원을_저장한다("update-overlap-owner");
        StudioContext context = 시설과_대표_소속을_저장한다(owner, "수정 충돌 시설");
        ClassType classType = 수업_종류를_저장한다(context.studio(), "요가");
        ClassSession target = 수업을_저장한다(
                context,
                classType,
                LocalDateTime.of(2026, 8, 17, 10, 0),
                60,
                "수정 대상"
        );
        수업을_저장한다(
                context,
                classType,
                LocalDateTime.of(2026, 8, 18, 20, 0),
                60,
                "기존 수업"
        );
        ClassSessionUpdateV1Request request = ClassSessionUpdateV1Request.of(
                ClassForm.GROUP,
                classType.getId(),
                "충돌하는 변경",
                10,
                60,
                LocalDateTime.of(2026, 8, 18, 20, 30),
                null
        );

        // when / then
        assertClassError(
                () -> commandService.updateV1(
                        owner.getId(), context.studio().getId(), target.getId(), request),
                ClassErrorCode.CLASS_SESSION_TIME_CONFLICT
        );
        assertThat(target.getName()).isEqualTo("수정 대상");
        assertThat(target.getStartAt()).isEqualTo(LocalDateTime.of(2026, 8, 17, 10, 0));
    }

    @Test
    void 취소된_수업_회차는_수정할_수_없다() {
        // given
        Member owner = 회원을_저장한다("update-canceled-owner");
        StudioContext context = 시설과_대표_소속을_저장한다(owner, "취소 수정 시설");
        ClassType classType = 수업_종류를_저장한다(context.studio(), "요가");
        ClassSession canceled = 수업을_저장한다(
                context,
                classType,
                LocalDateTime.of(2026, 8, 17, 20, 0),
                60,
                "취소된 수업"
        );
        canceled.cancel(LocalDateTime.of(2026, 8, 17, 19, 0));
        ClassSessionUpdateV1Request request = ClassSessionFixture
                .기본_수업_회차_수정_요청(classType.getId());

        // when / then
        assertClassError(
                () -> commandService.updateV1(
                        owner.getId(), context.studio().getId(), canceled.getId(), request),
                ClassErrorCode.CLASS_SESSION_CANCELED
        );
    }

    @Test
    void 다른_시설의_수업_회차나_수업_종류로_수정할_수_없다() {
        // given
        Member owner = 회원을_저장한다("update-boundary-owner");
        StudioContext context = 시설과_대표_소속을_저장한다(owner, "수정 요청 시설");
        ClassType classType = 수업_종류를_저장한다(context.studio(), "요가");
        ClassSession classSession = 수업을_저장한다(
                context,
                classType,
                LocalDateTime.of(2026, 8, 17, 20, 0),
                60,
                "수정 대상"
        );
        Member otherOwner = 회원을_저장한다("update-boundary-other-owner");
        StudioContext otherContext = 시설과_대표_소속을_저장한다(otherOwner, "다른 시설");
        ClassType otherClassType = 수업_종류를_저장한다(otherContext.studio(), "다른 시설 요가");

        // when / then
        assertClassError(
                () -> commandService.updateV1(
                        otherOwner.getId(),
                        otherContext.studio().getId(),
                        classSession.getId(),
                        ClassSessionFixture.기본_수업_회차_수정_요청(otherClassType.getId())
                ),
                ClassErrorCode.CLASS_SESSION_NOT_FOUND
        );
        assertClassError(
                () -> commandService.updateV1(
                        owner.getId(),
                        context.studio().getId(),
                        classSession.getId(),
                        ClassSessionFixture.기본_수업_회차_수정_요청(otherClassType.getId())
                ),
                ClassErrorCode.CLASS_TYPE_NOT_FOUND
        );
    }

    @Test
    void 전체_수업_관리_권한자는_다른_강사의_수업을_수정할_수_있다() {
        // given
        Member owner = 회원을_저장한다("update-all-owner");
        StudioContext context = 시설과_대표_소속을_저장한다(owner, "전체 수정 권한 시설");
        StudioRole managerRole = 사용자_역할을_저장한다(
                context.studio(), "전체 수업 관리자", false);
        권한을_저장한다(managerRole, PermissionCode.CLASS_SESSION_MANAGE_ALL);
        Member manager = 회원을_저장한다("update-all-manager");
        소속을_저장한다(context.studio(), manager, managerRole, MembershipStatus.ACTIVE);
        ClassType classType = 수업_종류를_저장한다(context.studio(), "전체 수정 요가");
        ClassSession classSession = 수업을_저장한다(
                context,
                classType,
                LocalDateTime.of(2026, 8, 17, 20, 0),
                60,
                "다른 강사의 수업"
        );

        // when
        commandService.updateV1(
                manager.getId(),
                context.studio().getId(),
                classSession.getId(),
                ClassSessionFixture.기본_수업_회차_수정_요청(classType.getId())
        );

        // then
        assertThat(classSession.getName()).isEqualTo("수정된 개인 수업");
    }

    @Test
    void 대표는_시작_전_수업_회차를_삭제하지_않고_취소_시각을_기록한다() {
        // given
        Member owner = 회원을_저장한다("cancel-owner");
        StudioContext context = 시설과_대표_소속을_저장한다(owner, "취소 시설");
        ClassType classType = 수업_종류를_저장한다(context.studio(), "요가");
        ClassSession classSession = 수업을_저장한다(
                context,
                classType,
                LocalDateTime.of(2026, 8, 17, 20, 0),
                60,
                "취소 대상"
        );

        // when
        commandService.cancel(owner.getId(), context.studio().getId(), classSession.getId());
        entityManager.flush();
        entityManager.clear();

        // then
        ClassSession canceled = classSessionRepository.findById(classSession.getId()).orElseThrow();
        assertThat(canceled.getCanceledAt()).isEqualTo(NOW);
        assertThat(classSessionRepository.count()).isOne();
    }

    @Test
    void 이미_취소된_수업_회차는_다시_취소할_수_없다() {
        // given
        Member owner = 회원을_저장한다("recancel-owner");
        StudioContext context = 시설과_대표_소속을_저장한다(owner, "재취소 시설");
        ClassType classType = 수업_종류를_저장한다(context.studio(), "요가");
        ClassSession classSession = 수업을_저장한다(
                context,
                classType,
                LocalDateTime.of(2026, 8, 17, 20, 0),
                60,
                "이미 취소된 수업"
        );
        classSession.cancel(NOW.minusHours(1));

        // when / then
        assertClassError(
                () -> commandService.cancel(
                        owner.getId(), context.studio().getId(), classSession.getId()),
                ClassErrorCode.CLASS_SESSION_ALREADY_CANCELED
        );
    }

    @Test
    void 시작_시각에_도달한_수업_회차는_취소할_수_없다() {
        // given
        Member owner = 회원을_저장한다("started-cancel-owner");
        StudioContext context = 시설과_대표_소속을_저장한다(owner, "시작된 수업 시설");
        ClassType classType = 수업_종류를_저장한다(context.studio(), "요가");
        ClassSession classSession = 수업을_저장한다(
                context,
                classType,
                NOW,
                60,
                "시작된 수업"
        );

        // when / then
        assertClassError(
                () -> commandService.cancel(
                        owner.getId(), context.studio().getId(), classSession.getId()),
                ClassErrorCode.CLASS_SESSION_ALREADY_STARTED
        );
        assertThat(classSession.isCanceled()).isFalse();
    }

    @Test
    void 본인_수업_관리_권한자는_자신의_수업만_취소할_수_있다() {
        // given
        Member owner = 회원을_저장한다("cancel-own-owner");
        StudioContext context = 시설과_대표_소속을_저장한다(owner, "본인 취소 권한 시설");
        StudioRole instructorRole = 역할을_저장한다(context.studio(), SystemRole.INSTRUCTOR);
        권한을_저장한다(instructorRole, PermissionCode.CLASS_SESSION_MANAGE_OWN);
        Member requester = 회원을_저장한다("cancel-own-requester");
        StudioMembership requesterMembership = 소속을_저장한다(
                context.studio(), requester, instructorRole, MembershipStatus.ACTIVE);
        ClassType classType = 수업_종류를_저장한다(context.studio(), "요가");
        ClassSession ownSession = 수업을_저장한다(
                context,
                requesterMembership,
                classType,
                LocalDateTime.of(2026, 8, 17, 20, 0),
                60,
                "본인 수업"
        );
        ClassSession otherSession = 수업을_저장한다(
                context,
                classType,
                LocalDateTime.of(2026, 8, 17, 21, 0),
                60,
                "다른 강사의 수업"
        );

        // when
        commandService.cancel(
                requester.getId(), context.studio().getId(), ownSession.getId());

        // then
        assertThat(ownSession.getCanceledAt()).isEqualTo(NOW);
        assertStudioError(
                () -> commandService.cancel(
                        requester.getId(), context.studio().getId(), otherSession.getId()),
                StudioErrorCode.PERMISSION_DENIED
        );
        assertThat(otherSession.isCanceled()).isFalse();
    }

    @Test
    void 전체_수업_관리_권한자는_다른_강사의_수업을_취소할_수_있다() {
        // given
        Member owner = 회원을_저장한다("cancel-all-owner");
        StudioContext context = 시설과_대표_소속을_저장한다(owner, "전체 취소 권한 시설");
        StudioRole managerRole = 사용자_역할을_저장한다(
                context.studio(), "전체 수업 관리자", false);
        권한을_저장한다(managerRole, PermissionCode.CLASS_SESSION_MANAGE_ALL);
        Member manager = 회원을_저장한다("cancel-all-manager");
        소속을_저장한다(context.studio(), manager, managerRole, MembershipStatus.ACTIVE);
        ClassType classType = 수업_종류를_저장한다(context.studio(), "요가");
        ClassSession classSession = 수업을_저장한다(
                context,
                classType,
                LocalDateTime.of(2026, 8, 17, 20, 0),
                60,
                "다른 강사의 수업"
        );

        // when
        commandService.cancel(
                manager.getId(), context.studio().getId(), classSession.getId());

        // then
        assertThat(classSession.getCanceledAt()).isEqualTo(NOW);
    }

    @Test
    void 다른_시설의_수업_회차는_취소할_수_없다() {
        // given
        Member owner = 회원을_저장한다("cancel-boundary-owner");
        StudioContext context = 시설과_대표_소속을_저장한다(owner, "취소 요청 시설");
        ClassType classType = 수업_종류를_저장한다(context.studio(), "요가");
        ClassSession classSession = 수업을_저장한다(
                context,
                classType,
                LocalDateTime.of(2026, 8, 17, 20, 0),
                60,
                "취소 대상"
        );
        Member otherOwner = 회원을_저장한다("cancel-boundary-other-owner");
        StudioContext otherContext = 시설과_대표_소속을_저장한다(otherOwner, "다른 시설");

        // when / then
        assertClassError(
                () -> commandService.cancel(
                        otherOwner.getId(), otherContext.studio().getId(), classSession.getId()),
                ClassErrorCode.CLASS_SESSION_NOT_FOUND
        );
        assertThat(classSession.isCanceled()).isFalse();
    }

    private ClassSessionCreateV2Request 요청(
            Long instructorMembershipId,
            Long classTypeId,
            Boolean recurring,
            LocalDate classDate,
            List<DayOfWeek> recurringDays,
            LocalDate repeatStartDate,
            LocalDate repeatEndDate
    ) {
        return ClassSessionFixture.수업_회차_생성_요청(
                instructorMembershipId,
                ClassForm.GROUP, classTypeId, "요청 검증 수업", 10, 60,
                recurring, LocalTime.of(10, 0), null, classDate, recurringDays,
                repeatStartDate, repeatEndDate);
    }

    private ClassSessionUpdateV1Request 수정_요청(ClassForm classForm, Long classTypeId) {
        return ClassSessionUpdateV1Request.of(
                classForm,
                classTypeId,
                "신청 후 수정 수업",
                10,
                50,
                LocalDateTime.of(2026, 8, 18, 19, 30),
                "수정 안내"
        );
    }

    private ClassSessionCreateV2Request 단일_요청(
            Long instructorMembershipId,
            Long classTypeId,
            String className
    ) {
        return ClassSessionFixture.수업_회차_생성_요청(
                instructorMembershipId,
                ClassForm.GROUP, classTypeId, className, 10, 60,
                false, LocalTime.of(10, 0), null, LocalDate.of(2026, 8, 17),
                null, null, null);
    }

    private ClassSessionCreateV2Request 단일_요청(
            Long instructorMembershipId,
            Long classTypeId,
            LocalDate date,
            LocalTime startTime,
            int durationMinutes
    ) {
        return ClassSessionFixture.수업_회차_생성_요청(
                instructorMembershipId,
                ClassForm.GROUP, classTypeId, "시간 검증 수업", 10, durationMinutes,
                false, startTime, null, date, null, null, null);
    }

    private Member 회원을_저장한다(String providerId) {
        Member member = StudioFixture.아이디가_다른_소유자(providerId);
        entityManager.persist(member);
        entityManager.flush();
        return member;
    }

    private StudioContext 시설과_대표_소속을_저장한다(Member owner, String name) {
        Studio studio = 시설을_저장한다(owner, name);
        StudioRole role = 역할을_저장한다(studio, SystemRole.OWNER);
        StudioMembership membership = 소속을_저장한다(
                studio, owner, role, MembershipStatus.ACTIVE);
        return new StudioContext(studio, role, membership);
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

    private StudioRole 역할을_저장한다(Studio studio, SystemRole systemRole) {
        StudioRole role = systemRole.toStudioRole(studio);
        entityManager.persist(role);
        entityManager.flush();
        return role;
    }

    private StudioRole 사용자_역할을_저장한다(Studio studio, String name, boolean instructor) {
        StudioRole role = StudioRole.builder()
                .studio(studio)
                .name(name)
                .instructor(instructor)
                .build();
        entityManager.persist(role);
        entityManager.flush();
        return role;
    }

    private StudioMembership 소속을_저장한다(
            Studio studio,
            Member member,
            StudioRole role,
            MembershipStatus status
    ) {
        StudioMembership membership = StudioMembership.builder()
                .studio(studio)
                .member(member)
                .phoneNumber(member.getPhoneNumber())
                .name(member.getName())
                .studioRole(role)
                .status(status)
                .joinedAt(LocalDateTime.of(2026, 8, 1, 9, 0))
                .build();
        entityManager.persist(membership);
        entityManager.flush();
        return membership;
    }

    private StudioMembership 학생_소속을_저장한다(Studio studio, String providerId) {
        Member student = 회원을_저장한다(providerId);
        StudioRole studentRole = 역할을_저장한다(studio, SystemRole.STUDENT);
        return 소속을_저장한다(studio, student, studentRole, MembershipStatus.ACTIVE);
    }

    private void 신청을_저장한다(
            StudioMembership membership,
            ClassSession classSession,
            EnrollmentStatus enrollmentStatus
    ) {
        ClassSessionEnrollment enrollment = switch (enrollmentStatus) {
            case WAITING -> ClassSessionEnrollment.waiting(membership, classSession, NOW.minusHours(2));
            case OFFERED -> {
                ClassSessionEnrollment offered = ClassSessionEnrollment.waiting(
                        membership, classSession, NOW.minusHours(2));
                offered.offer(NOW.minusHours(1), NOW.plusHours(1));
                yield offered;
            }
            case RESERVED -> ClassSessionEnrollment.reservedWithoutPassProduct(
                    membership, classSession, NOW.minusHours(2));
            case CANCELED -> {
                ClassSessionEnrollment canceled = ClassSessionEnrollment.waiting(
                        membership, classSession, NOW.minusHours(2));
                canceled.cancelWaiting(NOW.minusHours(1));
                yield canceled;
            }
            case EXPIRED -> {
                ClassSessionEnrollment expired = ClassSessionEnrollment.waiting(
                        membership, classSession, NOW.minusHours(2));
                expired.expire(NOW.minusHours(1));
                yield expired;
            }
        };
        classSessionEnrollmentRepository.saveAndFlush(enrollment);
    }

    private void 권한을_저장한다(StudioRole role, PermissionCode code) {
        Permission permission = permissionRepository.findByCodeIn(List.of(code)).getFirst();
        studioRolePermissionRepository.saveAndFlush(StudioRolePermission.builder()
                .studioRole(role)
                .permission(permission)
                .build());
    }

    private ClassType 수업_종류를_저장한다(Studio studio, String name) {
        return classTypeRepository.saveAndFlush(ClassTypeFixture.이름이_다른_수업_종류(studio, name));
    }

    private ClassSession 수업을_저장한다(
            StudioContext context,
            ClassType classType,
            LocalDateTime startAt,
            int durationMinutes,
            String name
    ) {
        return 수업을_저장한다(
                context,
                context.membership(),
                classType,
                startAt,
                durationMinutes,
                name
        );
    }

    private ClassSession 수업을_저장한다(
            StudioContext context,
            ClassType classType,
            LocalDateTime startAt,
            int durationMinutes,
            String name,
            String description
    ) {
        return 수업을_저장한다(
                context,
                context.membership(),
                classType,
                startAt,
                durationMinutes,
                name,
                description
        );
    }

    private ClassSession 수업을_저장한다(
            StudioContext context,
            StudioMembership instructorMembership,
            ClassType classType,
            LocalDateTime startAt,
            int durationMinutes,
            String name
    ) {
        return 수업을_저장한다(
                context,
                instructorMembership,
                classType,
                startAt,
                durationMinutes,
                name,
                null
        );
    }

    private ClassSession 수업을_저장한다(
            StudioContext context,
            StudioMembership instructorMembership,
            ClassType classType,
            LocalDateTime startAt,
            int durationMinutes,
            String name,
            String description
    ) {
        ClassSession session = classSessionRepository.saveAndFlush(ClassSessionFixture.수업_회차(
                context.studio().getId(), instructorMembership, name, description, ClassForm.GROUP,
                durationMinutes, 10, startAt));
        classSessionClassTypeRepository.saveAndFlush(
                ClassSessionFixture.수업_종류_연결(session.getId(), classType.getId()));
        return session;
    }

    private void 영속_설정을_정리한다(PersistenceSetup setup) {
        List<ClassSession> sessions = classSessionRepository.findAll().stream()
                .filter(session -> session.getStudioId().equals(setup.studioId()))
                .toList();
        List<Long> sessionIds = sessions.stream().map(ClassSession::getId).toList();
        List<ClassSessionClassType> links = classSessionClassTypeRepository.findAll().stream()
                .filter(link -> sessionIds.contains(link.getClassSessionId()))
                .toList();
        classSessionClassTypeRepository.deleteAll(links);
        classSessionRepository.deleteAll(sessions);
        classTypeRepository.deleteById(setup.classTypeId());
        studioMembershipRepository.deleteById(setup.membershipId());
        studioRoleRepository.deleteById(setup.roleId());
        studioRepository.deleteById(setup.studioId());
        memberRepository.deleteById(setup.memberId());
    }

    private void 연결_저장_실패_제약을_설치한다() {
        jdbcTemplate.execute("""
                ALTER TABLE class_session_class_type
                ADD CONSTRAINT ck_test_reject_class_session_class_type
                CHECK (class_session_id < 0)
                """);
    }

    private void 연결_저장_실패_제약을_삭제한다() {
        if (연결_저장_실패_제약_수() > 0) {
            jdbcTemplate.execute("ALTER TABLE class_session_class_type DROP CHECK " + LINK_FAILURE_CONSTRAINT);
        }
    }

    private int 연결_저장_실패_제약_수() {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM information_schema.table_constraints
                WHERE constraint_schema = DATABASE()
                  AND table_name = 'class_session_class_type'
                  AND constraint_name = ?
                """, Integer.class, LINK_FAILURE_CONSTRAINT);
        return count == null ? 0 : count;
    }

    private void assertClassError(Runnable action, ClassErrorCode errorCode) {
        assertThatThrownBy(action::run)
                .isInstanceOfSatisfying(ClassException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(errorCode));
    }

    private void assertStudioError(Runnable action, StudioErrorCode errorCode) {
        assertThatThrownBy(action::run)
                .isInstanceOfSatisfying(StudioException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(errorCode));
    }

    private record StudioContext(Studio studio, StudioRole role, StudioMembership membership) {
    }

    private record PersistenceSetup(
            Long memberId,
            Long studioId,
            Long membershipId,
            Long roleId,
            Long classTypeId
    ) {
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class FixedClockConfig {

        @Bean
        Clock clock() {
            return Clock.fixed(NOW.atZone(SERVICE_ZONE_ID).toInstant(), SERVICE_ZONE_ID);
        }
    }
}
