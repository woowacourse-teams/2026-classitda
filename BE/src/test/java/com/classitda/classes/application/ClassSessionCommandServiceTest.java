package com.classitda.classes.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.classitda.classes.domain.ClassForm;
import com.classitda.classes.domain.ClassSession;
import com.classitda.classes.domain.ClassSessionClassType;
import com.classitda.classes.domain.ClassSessionStatus;
import com.classitda.classes.domain.ClassTemplate;
import com.classitda.classes.domain.ClassType;
import com.classitda.classes.domain.repository.ClassSessionClassTypeRepository;
import com.classitda.classes.domain.repository.ClassSessionRepository;
import com.classitda.classes.domain.repository.ClassTemplateRepository;
import com.classitda.classes.domain.repository.ClassTypeRepository;
import com.classitda.classes.exception.ClassErrorCode;
import com.classitda.classes.exception.ClassException;
import com.classitda.classes.fixture.ClassSessionFixture;
import com.classitda.classes.fixture.ClassTypeFixture;
import com.classitda.classes.presentation.dto.ClassSessionCreateRequest;
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
import com.classitda.support.MySqlRepositoryTest;
import jakarta.persistence.EntityManager;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Import(ClassSessionCommandService.class)
@MySqlRepositoryTest
class ClassSessionCommandServiceTest {

    private static final String LINK_FAILURE_CONSTRAINT = "ck_test_reject_class_session_class_type";

    private final ClassSessionCommandService commandService;
    private final ClassSessionClassTypeRepository classSessionClassTypeRepository;
    private final ClassSessionRepository classSessionRepository;
    private final ClassTemplateRepository classTemplateRepository;
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
            ClassSessionRepository classSessionRepository,
            ClassTemplateRepository classTemplateRepository,
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
        this.classSessionRepository = classSessionRepository;
        this.classTemplateRepository = classTemplateRepository;
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
    void 반복하지_않는_수업은_요청한_날짜에_선택한_강사_담당으로_한_건을_저장한다() {
        // given
        Member owner = 회원을_저장한다("single-owner");
        StudioContext context = 시설과_대표_소속을_저장한다(owner, "단일 수업 시설");
        ClassType classType = 수업_종류를_저장한다(context.studio(), "요가");

        // when
        commandService.save(owner.getId(), context.studio().getId(),
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
        assertThat(saved.getStatus()).isEqualTo(ClassSessionStatus.OPENED);
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
        commandService.save(owner.getId(), context.studio().getId(),
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
        List<ClassSessionCreateRequest> requests = List.of(
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
        for (ClassSessionCreateRequest request : requests) {
            assertClassError(
                    () -> commandService.save(owner.getId(), context.studio().getId(), request),
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
        List<ClassSessionCreateRequest> invalidDays = new ArrayList<>();
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
        for (ClassSessionCreateRequest request : invalidDays) {
            assertClassError(
                    () -> commandService.save(owner.getId(), context.studio().getId(), request),
                    ClassErrorCode.INVALID_CLASS_SESSION_RECURRING_DAYS
            );
        }
        assertClassError(
                () -> commandService.save(owner.getId(), context.studio().getId(),
                        요청(context.membership().getId(), classType.getId(), true, null,
                                List.of(DayOfWeek.MONDAY), null, monday)),
                ClassErrorCode.INVALID_CLASS_SESSION_REPEAT_PERIOD
        );
        assertClassError(
                () -> commandService.save(owner.getId(), context.studio().getId(),
                        요청(context.membership().getId(), classType.getId(), true, null,
                                List.of(DayOfWeek.MONDAY), monday, null)),
                ClassErrorCode.INVALID_CLASS_SESSION_REPEAT_PERIOD
        );
        assertClassError(
                () -> commandService.save(owner.getId(), context.studio().getId(),
                        요청(context.membership().getId(), classType.getId(), true, null,
                                List.of(DayOfWeek.MONDAY),
                                monday.plusDays(1), monday)),
                ClassErrorCode.INVALID_CLASS_SESSION_REPEAT_PERIOD
        );
        assertClassError(
                () -> commandService.save(owner.getId(), context.studio().getId(),
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
        commandService.save(instructor.getId(), context.studio().getId(),
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
        commandService.save(owner.getId(), context.studio().getId(),
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
        commandService.save(manager.getId(), context.studio().getId(),
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
        ClassSessionCreateRequest request = ClassSessionFixture.기본_단일_수업_회차_생성_요청(
                otherMembership.getId(), classType.getId());

        // when / then
        assertStudioError(
                () -> commandService.save(requester.getId(), context.studio().getId(), request),
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
        ClassSessionCreateRequest request = ClassSessionFixture.기본_단일_수업_회차_생성_요청(
                context.membership().getId(), classType.getId());

        // when / then
        assertStudioError(
                () -> commandService.save(inactive.getId(), context.studio().getId(), request),
                StudioErrorCode.MEMBERSHIP_INACTIVE
        );
        assertStudioError(
                () -> commandService.save(stranger.getId(), context.studio().getId(), request),
                StudioErrorCode.NOT_MEMBERSHIP
        );
        assertStudioError(
                () -> commandService.save(student.getId(), context.studio().getId(), request),
                StudioErrorCode.PERMISSION_DENIED
        );
        assertStudioError(
                () -> commandService.save(noPermission.getId(), context.studio().getId(), request),
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
                    () -> commandService.save(
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
    void 다른_시설이나_없는_템플릿과_수업_종류는_찾을_수_없다() {
        // given
        Member owner = 회원을_저장한다("boundary-owner");
        StudioContext requested = 시설과_대표_소속을_저장한다(owner, "요청 시설");
        Studio other = 시설을_저장한다(owner, "다른 시설");
        ClassType requestedType = 수업_종류를_저장한다(requested.studio(), "요가");
        ClassType otherType = 수업_종류를_저장한다(other, "필라테스");
        ClassTemplate otherTemplate = 템플릿을_저장한다(other, "다른 시설 템플릿");

        // when / then
        assertClassError(
                () -> commandService.save(owner.getId(), requested.studio().getId(),
                        단일_요청(requested.membership().getId(),
                                otherTemplate.getId(), requestedType.getId(), "템플릿 경계")),
                ClassErrorCode.CLASS_TEMPLATE_NOT_FOUND
        );
        assertClassError(
                () -> commandService.save(owner.getId(), requested.studio().getId(),
                        단일_요청(requested.membership().getId(),
                                Long.MAX_VALUE, requestedType.getId(), "없는 템플릿")),
                ClassErrorCode.CLASS_TEMPLATE_NOT_FOUND
        );
        assertClassError(
                () -> commandService.save(owner.getId(), requested.studio().getId(),
                        단일_요청(requested.membership().getId(),
                                null, otherType.getId(), "수업 종류 경계")),
                ClassErrorCode.CLASS_TYPE_NOT_FOUND
        );
        assertClassError(
                () -> commandService.save(owner.getId(), requested.studio().getId(),
                        단일_요청(requested.membership().getId(),
                                null, Long.MAX_VALUE, "없는 수업 종류")),
                ClassErrorCode.CLASS_TYPE_NOT_FOUND
        );
        assertThat(classSessionRepository.count()).isZero();
    }

    @Test
    void 템플릿은_검증만_하고_최종_요청값으로_수업을_저장하며_후속_변경에_영향받지_않는다() {
        // given
        Member owner = 회원을_저장한다("template-independent-owner");
        StudioContext context = 시설과_대표_소속을_저장한다(owner, "템플릿 독립 시설");
        ClassType classType = 수업_종류를_저장한다(context.studio(), "필라테스");
        ClassTemplate template = 템플릿을_저장한다(context.studio(), "원본 템플릿");
        ClassSessionCreateRequest request = ClassSessionFixture.수업_회차_생성_요청(
                template.getId(), context.membership().getId(),
                ClassForm.INDIVIDUAL, classType.getId(), "최종 개인 수업",
                1, 45, false, LocalTime.of(9, 30), "최종 메모",
                LocalDate.of(2026, 8, 18), null, null, null);

        // when
        commandService.save(owner.getId(), context.studio().getId(), request);
        template.updateDetails(
                "변경된 템플릿", null, ClassForm.GROUP, 120,
                LocalTime.of(6, 0), Set.of(DayOfWeek.SUNDAY), 30);
        classTemplateRepository.delete(template);
        entityManager.flush();
        entityManager.clear();

        // then
        ClassSession saved = classSessionRepository.findAll().getFirst();
        assertThat(saved.getName()).isEqualTo("최종 개인 수업");
        assertThat(saved.getDescription()).isEqualTo("최종 메모");
        assertThat(saved.getClassForm()).isEqualTo(ClassForm.INDIVIDUAL);
        assertThat(saved.getDurationMinutes()).isEqualTo(45);
        assertThat(saved.getCapacity()).isEqualTo(1);
        assertThat(saved.getStartAt()).isEqualTo(LocalDateTime.of(2026, 8, 18, 9, 30));
        assertThat(classTemplateRepository.findById(template.getId())).isEmpty();
    }

    @Test
    void 기존_활성_수업과_정확히_겹치거나_부분적으로_겹치면_생성하지_않는다() {
        // given
        Member owner = 회원을_저장한다("overlap-owner");
        StudioContext context = 시설과_대표_소속을_저장한다(owner, "충돌 시설");
        ClassType classType = 수업_종류를_저장한다(context.studio(), "요가");
        수업을_저장한다(context, classType, LocalDateTime.of(2026, 8, 17, 10, 0),
                60, ClassSessionStatus.OPENED, "기존 수업");

        // when / then
        assertClassError(
                () -> commandService.save(owner.getId(), context.studio().getId(),
                        단일_요청(context.membership().getId(), classType.getId(),
                                LocalDate.of(2026, 8, 17), LocalTime.of(10, 0), 60)),
                ClassErrorCode.CLASS_SESSION_TIME_CONFLICT
        );
        assertClassError(
                () -> commandService.save(owner.getId(), context.studio().getId(),
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
                ClassSessionStatus.OPENED,
                "선택 강사의 기존 수업"
        );

        // when / then
        assertClassError(
                () -> commandService.save(
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
                60, ClassSessionStatus.OPENED, "요청자의 기존 수업");

        // when
        commandService.save(
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
                60, ClassSessionStatus.OPENED, "활성 수업");
        수업을_저장한다(context, classType, LocalDateTime.of(2026, 8, 18, 10, 0),
                60, ClassSessionStatus.CANCELED, "취소 수업");

        // when
        commandService.save(owner.getId(), context.studio().getId(),
                단일_요청(context.membership().getId(), classType.getId(),
                        LocalDate.of(2026, 8, 17), LocalTime.of(11, 0), 60));
        commandService.save(owner.getId(), context.studio().getId(),
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
                60, ClassSessionStatus.OPENED, "나중 충돌 수업");

        // when / then
        assertClassError(
                () -> commandService.save(owner.getId(), context.studio().getId(),
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
        ClassSessionCreateRequest request = ClassSessionFixture.수업_회차_생성_요청(
                null, context.membership().getId(),
                ClassForm.GROUP, classType.getId(), "하루보다 긴 수업", 10, 1_441,
                true, LocalTime.MIDNIGHT, null, null, List.of(DayOfWeek.MONDAY),
                LocalDate.of(2026, 8, 17), LocalDate.of(2026, 8, 24));

        // when / then
        assertClassError(
                () -> commandService.save(owner.getId(), context.studio().getId(), request),
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
            assertThatThrownBy(() -> commandService.save(
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

    private ClassSessionCreateRequest 요청(
            Long instructorMembershipId,
            Long classTypeId,
            Boolean recurring,
            LocalDate classDate,
            List<DayOfWeek> recurringDays,
            LocalDate repeatStartDate,
            LocalDate repeatEndDate
    ) {
        return ClassSessionFixture.수업_회차_생성_요청(
                null, instructorMembershipId,
                ClassForm.GROUP, classTypeId, "요청 검증 수업", 10, 60,
                recurring, LocalTime.of(10, 0), null, classDate, recurringDays,
                repeatStartDate, repeatEndDate);
    }

    private ClassSessionCreateRequest 단일_요청(
            Long instructorMembershipId,
            Long templateId,
            Long classTypeId,
            String className
    ) {
        return ClassSessionFixture.수업_회차_생성_요청(
                templateId, instructorMembershipId,
                ClassForm.GROUP, classTypeId, className, 10, 60,
                false, LocalTime.of(10, 0), null, LocalDate.of(2026, 8, 17),
                null, null, null);
    }

    private ClassSessionCreateRequest 단일_요청(
            Long instructorMembershipId,
            Long classTypeId,
            LocalDate date,
            LocalTime startTime,
            int durationMinutes
    ) {
        return ClassSessionFixture.수업_회차_생성_요청(
                null, instructorMembershipId,
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
                .name(member.getName())
                .studioRole(role)
                .status(status)
                .joinedAt(LocalDateTime.of(2026, 8, 1, 9, 0))
                .build();
        entityManager.persist(membership);
        entityManager.flush();
        return membership;
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

    private ClassTemplate 템플릿을_저장한다(Studio studio, String name) {
        return classTemplateRepository.saveAndFlush(ClassTemplate.builder()
                .studioId(studio.getId())
                .name(name)
                .description("템플릿 메모")
                .classForm(ClassForm.GROUP)
                .durationMinutes(120)
                .startTime(LocalTime.of(6, 0))
                .recurringDays(Set.of(DayOfWeek.SUNDAY))
                .capacity(30)
                .build());
    }

    private void 수업을_저장한다(
            StudioContext context,
            ClassType classType,
            LocalDateTime startAt,
            int durationMinutes,
            ClassSessionStatus status,
            String name
    ) {
        수업을_저장한다(
                context,
                context.membership(),
                classType,
                startAt,
                durationMinutes,
                status,
                name
        );
    }

    private void 수업을_저장한다(
            StudioContext context,
            StudioMembership instructorMembership,
            ClassType classType,
            LocalDateTime startAt,
            int durationMinutes,
            ClassSessionStatus status,
            String name
    ) {
        ClassSession session = classSessionRepository.saveAndFlush(ClassSessionFixture.수업_회차(
                context.studio().getId(), instructorMembership, name, null, ClassForm.GROUP,
                durationMinutes, 10, startAt, status));
        classSessionClassTypeRepository.saveAndFlush(
                ClassSessionFixture.수업_종류_연결(session.getId(), classType.getId()));
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
}
