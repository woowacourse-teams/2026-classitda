package com.classitda.classes.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;

import com.classitda.classes.domain.ClassForm;
import com.classitda.classes.domain.session.ClassSession;
import com.classitda.classes.domain.session.ClassSessionClassType;
import com.classitda.classes.domain.template.ClassTemplate;
import com.classitda.classes.domain.template.ClassTemplateClassType;
import com.classitda.classes.domain.ClassType;
import com.classitda.classes.domain.repository.ClassTemplateClassTypeRepository;
import com.classitda.classes.domain.repository.ClassTemplateRepository;
import com.classitda.classes.domain.repository.ClassTypeRepository;
import com.classitda.classes.exception.ClassErrorCode;
import com.classitda.classes.exception.ClassException;
import com.classitda.classes.fixture.ClassTemplateFixture;
import com.classitda.classes.fixture.ClassTypeFixture;
import com.classitda.classes.presentation.dto.ClassTemplateUpdateRequest;
import com.classitda.common.exception.ClassitdaException;
import com.classitda.common.exception.CommonErrorCode;
import com.classitda.member.domain.Member;
import com.classitda.member.domain.repository.MemberRepository;
import com.classitda.studio.application.StudioPermissionService;
import com.classitda.studio.domain.MembershipStatus;
import com.classitda.studio.domain.Studio;
import com.classitda.studio.domain.StudioMembership;
import com.classitda.studio.domain.StudioRole;
import com.classitda.studio.domain.SystemRole;
import com.classitda.studio.domain.repository.StudioRepository;
import com.classitda.studio.exception.StudioErrorCode;
import com.classitda.studio.exception.StudioException;
import com.classitda.studio.fixture.StudioFixture;
import com.classitda.support.MySqlRepositoryTest;
import jakarta.persistence.EntityManager;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Import({ClassTemplateCommandService.class, StudioPermissionService.class})
@MySqlRepositoryTest
class ClassTemplateCommandServiceTest {

    private final ClassTemplateCommandService commandService;
    private final ClassTemplateRepository classTemplateRepository;
    private final ClassTypeRepository classTypeRepository;
    private final StudioRepository studioRepository;
    private final MemberRepository memberRepository;
    private final EntityManager entityManager;

    @MockitoSpyBean
    private ClassTemplateClassTypeRepository linkRepository;

    @Autowired
    ClassTemplateCommandServiceTest(
            ClassTemplateCommandService commandService,
            ClassTemplateRepository classTemplateRepository,
            ClassTypeRepository classTypeRepository,
            StudioRepository studioRepository,
            MemberRepository memberRepository,
            EntityManager entityManager
    ) {
        this.commandService = commandService;
        this.classTemplateRepository = classTemplateRepository;
        this.classTypeRepository = classTypeRepository;
        this.studioRepository = studioRepository;
        this.memberRepository = memberRepository;
        this.entityManager = entityManager;
    }

    @Test
    void 대표_강사가_수업_템플릿을_생성하면_내용과_선택한_수업_종류를_저장한다() {
        // given
        Member owner = 회원을_저장한다("template-command-owner");
        Studio studio = 시설을_저장한다(owner, "명령 시설");
        ClassType classType = 수업_종류를_저장한다(studio, "요가");

        // when
        commandService.save(owner.getId(), studio.getId(),
                ClassTemplateFixture.기본_수업_템플릿_생성_요청(classType.getId()));
        entityManager.flush();
        entityManager.clear();

        // then
        ClassTemplate saved = classTemplateRepository.findAllByStudioIdOrderByIdAsc(studio.getId()).getFirst();
        assertThat(saved.getStudioId()).isEqualTo(studio.getId());
        assertThat(saved.getName()).isEqualTo("저녁 요가");
        assertThat(saved.getDescription()).isEqualTo("퇴근 후 진행하는 수업");
        assertThat(saved.getClassForm()).isEqualTo(ClassForm.GROUP);
        assertThat(saved.getDurationMinutes()).isEqualTo(60);
        assertThat(saved.getStartTime()).isEqualTo(java.time.LocalTime.of(20, 0));
        assertThat(saved.getRecurringDays())
                .containsExactly(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY);
        assertThat(saved.getCapacity()).isEqualTo(12);
        assertThat(linkRepository.findAll())
                .filteredOn(link -> link.getClassTemplateId().equals(saved.getId()))
                .extracting(ClassTemplateClassType::getClassTypeId)
                .containsExactly(classType.getId());
    }

    @Test
    void 하나의_수업_템플릿에_여러_수업_종류를_연결할_수_없다() {
        // given
        Member owner = 회원을_저장한다("template-single-type-owner");
        Studio studio = 시설을_저장한다(owner, "단일 수업 종류 시설");
        ClassType yoga = 수업_종류를_저장한다(studio, "요가");
        ClassType pilates = 수업_종류를_저장한다(studio, "필라테스");
        ClassTemplate template = 템플릿을_저장한다(studio, "단일 종류 템플릿", null, Set.of());
        연결을_저장한다(template, yoga);

        // when / then
        assertThatThrownBy(() -> linkRepository.saveAndFlush(ClassTemplateClassType.builder()
                .classTemplateId(template.getId())
                .classTypeId(pilates.getId())
                .build()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void 반복_요일에_null이_포함되면_정확한_예외가_발생하고_아무것도_저장하지_않는다() {
        // given
        Member owner = 회원을_저장한다("template-null-day-owner");
        Studio studio = 시설을_저장한다(owner, "null 요일 시설");
        ClassType classType = 수업_종류를_저장한다(studio, "요가");
        Set<DayOfWeek> recurringDays = new LinkedHashSet<>();
        recurringDays.add(DayOfWeek.MONDAY);
        recurringDays.add(null);

        // when / then
        assertThatThrownBy(() -> commandService.save(owner.getId(), studio.getId(),
                ClassTemplateFixture.수업_템플릿_생성_요청(
                        "null 요일 템플릿", null, recurringDays, classType.getId())))
                .isInstanceOfSatisfying(ClassException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ClassErrorCode.INVALID_RECURRING_DAY));
        entityManager.flush();
        entityManager.clear();
        assertThat(classTemplateRepository.count()).isZero();
        assertThat(linkRepository.count()).isZero();
    }

    @Test
    void 관리_권한이_없는_회원은_수업_템플릿을_생성할_수_없다() {
        // given
        Member owner = 회원을_저장한다("template-permission-owner");
        Studio studio = 시설을_저장한다(owner, "권한 시설");
        Member student = 회원을_저장한다("template-student");
        소속을_저장한다(studio, student, SystemRole.STUDENT);
        ClassType classType = 수업_종류를_저장한다(studio, "요가");

        // when / then
        assertThatThrownBy(() -> commandService.save(student.getId(), studio.getId(),
                ClassTemplateFixture.기본_수업_템플릿_생성_요청(classType.getId())))
                .isInstanceOfSatisfying(StudioException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(StudioErrorCode.PERMISSION_DENIED));
        assertThat(classTemplateRepository.count()).isZero();
        assertThat(linkRepository.count()).isZero();
    }

    @Test
    void 다른_시설의_수업_종류이면_템플릿과_연결을_하나도_저장하지_않는다() {
        // given
        Member owner = 회원을_저장한다("template-cross-owner");
        Studio studio = 시설을_저장한다(owner, "요청 시설");
        Studio otherStudio = 시설을_저장한다(owner, "다른 시설");
        ClassType otherType = 수업_종류를_저장한다(otherStudio, "필라테스");

        // when / then
        assertThatThrownBy(() -> commandService.save(owner.getId(), studio.getId(),
                ClassTemplateFixture.기본_수업_템플릿_생성_요청(otherType.getId())))
                .isInstanceOfSatisfying(ClassException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ClassErrorCode.CLASS_TYPE_NOT_FOUND));
        assertThat(classTemplateRepository.count()).isZero();
        assertThat(linkRepository.count()).isZero();
    }

    @Test
    void 없는_시설이면_시설_없음_예외가_먼저_발생한다() {
        // when / then
        assertThatThrownBy(() -> commandService.save(1L, 999L,
                ClassTemplateFixture.기본_수업_템플릿_생성_요청(999L)))
                .isInstanceOfSatisfying(StudioException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(StudioErrorCode.NOT_FOUND));
    }

    @Test
    void 수업_템플릿을_수정하면_모든_기본_정보와_반복_요일을_교체한다() {
        // given
        Member owner = 회원을_저장한다("template-update-owner");
        Studio studio = 시설을_저장한다(owner, "수정 시설");
        ClassType yoga = 수업_종류를_저장한다(studio, "요가");
        ClassTemplate template = 템플릿을_저장한다(studio, "저녁 요가", "기존 설명",
                Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY));
        연결을_저장한다(template, yoga);

        // when
        commandService.update(owner.getId(), studio.getId(), template.getId(),
                ClassTemplateFixture.기본_수업_템플릿_수정_요청(yoga.getId()));
        entityManager.flush();
        entityManager.clear();

        // then
        ClassTemplate updated = classTemplateRepository.findById(template.getId()).orElseThrow();
        assertThat(updated.getStudioId()).isEqualTo(studio.getId());
        assertThat(updated.getName()).isEqualTo("아침 개인 필라테스");
        assertThat(updated.getDescription()).isEqualTo("개인별 자세 교정 수업");
        assertThat(updated.getClassForm()).isEqualTo(ClassForm.INDIVIDUAL);
        assertThat(updated.getDurationMinutes()).isEqualTo(50);
        assertThat(updated.getStartTime()).isEqualTo(java.time.LocalTime.of(9, 30));
        assertThat(updated.getRecurringDays()).containsExactly(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY);
        assertThat(updated.getCapacity()).isEqualTo(1);
    }

    @Test
    void description이_null이면_메모를_삭제하고_반복_요일이_null이면_모두_제거한다() {
        // given
        Member owner = 회원을_저장한다("template-clear-null-owner");
        Studio studio = 시설을_저장한다(owner, "null 초기화 시설");
        ClassType yoga = 수업_종류를_저장한다(studio, "요가");
        ClassTemplate template = 템플릿을_저장한다(studio, "저녁 요가", "삭제할 설명",
                Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY));
        연결을_저장한다(template, yoga);
        ClassTemplateUpdateRequest request = ClassTemplateFixture.수업_템플릿_수정_요청(
                "설명과 요일 초기화", null, ClassForm.GROUP, 60,
                java.time.LocalTime.of(20, 0), null, 12, yoga.getId());

        // when
        commandService.update(owner.getId(), studio.getId(), template.getId(), request);
        entityManager.flush();
        entityManager.clear();

        // then
        ClassTemplate updated = classTemplateRepository.findById(template.getId()).orElseThrow();
        assertThat(updated.getDescription()).isNull();
        assertThat(updated.getRecurringDays()).isEmpty();
    }

    @Test
    void 반복_요일이_빈_목록이면_기존_요일을_모두_제거한다() {
        // given
        Member owner = 회원을_저장한다("template-clear-empty-owner");
        Studio studio = 시설을_저장한다(owner, "빈 요일 시설");
        ClassType yoga = 수업_종류를_저장한다(studio, "요가");
        ClassTemplate template = 템플릿을_저장한다(studio, "저녁 요가", null,
                Set.of(DayOfWeek.FRIDAY));
        연결을_저장한다(template, yoga);
        ClassTemplateUpdateRequest request = ClassTemplateFixture.수업_템플릿_수정_요청(
                "요일 초기화", null, ClassForm.GROUP, 60,
                java.time.LocalTime.of(20, 0), Set.of(), 12, yoga.getId());

        // when
        commandService.update(owner.getId(), studio.getId(), template.getId(), request);
        entityManager.flush();
        entityManager.clear();

        // then
        assertThat(classTemplateRepository.findById(template.getId()).orElseThrow().getRecurringDays()).isEmpty();
    }

    @Test
    void 수업_종류가_같으면_기존_연결의_ID와_생성_시각을_보존한다() {
        // given
        Member owner = 회원을_저장한다("template-same-links-owner");
        Studio studio = 시설을_저장한다(owner, "연결 유지 시설");
        ClassType yoga = 수업_종류를_저장한다(studio, "요가");
        ClassTemplate template = 템플릿을_저장한다(studio, "기존 템플릿", null, Set.of());
        연결을_저장한다(template, yoga);
        entityManager.flush();
        entityManager.clear();
        List<ClassTemplateClassType> before = 템플릿_연결을_조회한다(template.getId());

        // when
        commandService.update(owner.getId(), studio.getId(), template.getId(),
                ClassTemplateFixture.기본_수업_템플릿_수정_요청(yoga.getId()));
        entityManager.flush();
        entityManager.clear();

        // then
        List<ClassTemplateClassType> after = 템플릿_연결을_조회한다(template.getId());
        assertThat(after).extracting(ClassTemplateClassType::getId)
                .containsExactly(before.getFirst().getId());
        assertThat(after).extracting(ClassTemplateClassType::getCreatedAt)
                .containsExactly(before.getFirst().getCreatedAt());
    }

    @Test
    void 수업_종류를_교체하면_기존_연결을_삭제하고_새_연결을_저장한다() {
        // given
        Member owner = 회원을_저장한다("template-link-delta-owner");
        Studio studio = 시설을_저장한다(owner, "연결 변경 시설");
        ClassType yoga = 수업_종류를_저장한다(studio, "요가");
        ClassType ballet = 수업_종류를_저장한다(studio, "발레");
        ClassTemplate template = 템플릿을_저장한다(studio, "기존 템플릿", null, Set.of());
        연결을_저장한다(template, yoga);
        entityManager.flush();
        entityManager.clear();
        ClassTemplateClassType before = 템플릿_연결을_조회한다(template.getId()).getFirst();

        // when
        commandService.update(owner.getId(), studio.getId(), template.getId(),
                ClassTemplateFixture.기본_수업_템플릿_수정_요청(ballet.getId()));
        entityManager.flush();
        entityManager.clear();

        // then
        List<ClassTemplateClassType> after = 템플릿_연결을_조회한다(template.getId());
        assertThat(after).singleElement().satisfies(link -> {
            assertThat(link.getId()).isNotEqualTo(before.getId());
            assertThat(link.getClassTypeId()).isEqualTo(ballet.getId());
        });
    }

    @Test
    void 없거나_다른_시설의_템플릿은_수정할_수_없다() {
        // given
        Member owner = 회원을_저장한다("template-not-found-owner");
        Studio studio = 시설을_저장한다(owner, "요청 시설");
        Studio otherStudio = 시설을_저장한다(owner, "다른 시설");
        ClassType yoga = 수업_종류를_저장한다(studio, "요가");
        ClassTemplate otherTemplate = 템플릿을_저장한다(otherStudio, "다른 템플릿", "기존 설명", Set.of());
        ClassTemplateUpdateRequest request = ClassTemplateFixture.기본_수업_템플릿_수정_요청(yoga.getId());

        // when / then
        assertThatThrownBy(() -> commandService.update(owner.getId(), studio.getId(), 999L, request))
                .isInstanceOfSatisfying(ClassException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ClassErrorCode.CLASS_TEMPLATE_NOT_FOUND));
        assertThatThrownBy(() -> commandService.update(owner.getId(), studio.getId(), otherTemplate.getId(), request))
                .isInstanceOfSatisfying(ClassException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ClassErrorCode.CLASS_TEMPLATE_NOT_FOUND));
        assertThat(classTemplateRepository.findById(otherTemplate.getId()).orElseThrow().getDescription())
                .isEqualTo("기존 설명");
    }

    @Test
    void 없거나_다른_시설의_수업_종류이면_템플릿과_연결을_변경하지_않는다() {
        // given
        Member owner = 회원을_저장한다("template-type-not-found-owner");
        Studio studio = 시설을_저장한다(owner, "요청 시설");
        Studio otherStudio = 시설을_저장한다(owner, "다른 시설");
        ClassType yoga = 수업_종류를_저장한다(studio, "요가");
        ClassType otherType = 수업_종류를_저장한다(otherStudio, "필라테스");
        ClassTemplate template = 템플릿을_저장한다(studio, "기존 템플릿", "기존 설명",
                Set.of(DayOfWeek.MONDAY));
        연결을_저장한다(template, yoga);
        ClassTemplateUpdateRequest missingTypeRequest = ClassTemplateFixture.기본_수업_템플릿_수정_요청(
                999L);
        ClassTemplateUpdateRequest crossStudioTypeRequest = ClassTemplateFixture.기본_수업_템플릿_수정_요청(
                otherType.getId());

        // when / then
        수업_종류_없음과_기존_상태를_검증한다(owner, studio, template, yoga, missingTypeRequest);
        수업_종류_없음과_기존_상태를_검증한다(owner, studio, template, yoga, crossStudioTypeRequest);
    }

    @Test
    void null이거나_양수가_아닌_수업_종류_ID는_거절하고_기존_상태를_보존한다() {
        // given
        Member owner = 회원을_저장한다("template-invalid-types-owner");
        Studio studio = 시설을_저장한다(owner, "유효성 시설");
        ClassType yoga = 수업_종류를_저장한다(studio, "요가");
        ClassTemplate template = 템플릿을_저장한다(studio, "기존 템플릿", "기존 설명",
                Set.of(DayOfWeek.MONDAY));
        연결을_저장한다(template, yoga);

        // when / then
        assertThatThrownBy(() -> commandService.update(owner.getId(), studio.getId(), template.getId(),
                ClassTemplateFixture.기본_수업_템플릿_수정_요청(null)))
                .isInstanceOfSatisfying(ClassitdaException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(CommonErrorCode.INVALID_INPUT));
        assertThatThrownBy(() -> commandService.update(owner.getId(), studio.getId(), template.getId(),
                ClassTemplateFixture.기본_수업_템플릿_수정_요청(0L)))
                .isInstanceOfSatisfying(ClassitdaException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(CommonErrorCode.INVALID_INPUT));
        기존_템플릿과_연결을_검증한다(template, yoga);
    }

    @Test
    void 도메인_검증에_실패하면_템플릿과_수업_종류_연결을_모두_롤백한다() {
        // given
        Member owner = 회원을_저장한다("template-rollback-owner");
        Studio studio = 시설을_저장한다(owner, "롤백 시설");
        ClassType yoga = 수업_종류를_저장한다(studio, "요가");
        ClassType pilates = 수업_종류를_저장한다(studio, "필라테스");
        ClassTemplate template = 템플릿을_저장한다(studio, "기존 템플릿", "기존 설명",
                Set.of(DayOfWeek.MONDAY));
        연결을_저장한다(template, yoga);
        ClassTemplateUpdateRequest invalidRequest = ClassTemplateFixture.수업_템플릿_수정_요청(
                " ", null, ClassForm.INDIVIDUAL, 50, java.time.LocalTime.of(9, 30),
                Set.of(DayOfWeek.TUESDAY), 1, pilates.getId());

        // when / then
        assertThatThrownBy(() -> commandService.update(
                owner.getId(), studio.getId(), template.getId(), invalidRequest))
                .isInstanceOfSatisfying(ClassException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ClassErrorCode.INVALID_CLASS_TEMPLATE_NAME));
        기존_템플릿과_연결을_검증한다(template, yoga);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void 연결_저장에_실패하면_앞서_변경한_템플릿과_삭제한_연결도_모두_롤백한다() {
        // given
        Member owner = memberRepository.saveAndFlush(StudioFixture.아이디가_다른_소유자(
                "template-persistence-rollback-owner"));
        Studio studio = studioRepository.saveAndFlush(Studio.builder()
                .owner(owner)
                .name("영속성 롤백 시설")
                .openTime(java.time.LocalTime.of(9, 0))
                .closeTime(java.time.LocalTime.of(22, 0))
                .address(StudioFixture.기본_주소())
                .build());
        ClassType yoga = 수업_종류를_저장한다(studio, "요가");
        ClassType pilates = 수업_종류를_저장한다(studio, "필라테스");
        ClassTemplate template = classTemplateRepository.saveAndFlush(ClassTemplate.builder()
                .studioId(studio.getId())
                .name("기존 템플릿")
                .description("기존 설명")
                .classForm(ClassForm.GROUP)
                .durationMinutes(60)
                .startTime(java.time.LocalTime.of(20, 0))
                .recurringDays(Set.of(DayOfWeek.MONDAY))
                .capacity(12)
                .build());
        ClassTemplateClassType originalLink = linkRepository.saveAndFlush(ClassTemplateClassType.builder()
                .classTemplateId(template.getId())
                .classTypeId(yoga.getId())
                .build());
        doThrow(new DataIntegrityViolationException("연결 저장 실패"))
                .when(linkRepository).save(any());

        // when / then
        try {
            assertThatThrownBy(() -> commandService.update(
                    owner.getId(),
                    studio.getId(),
                    template.getId(),
                    ClassTemplateFixture.기본_수업_템플릿_수정_요청(pilates.getId())
            )).isInstanceOf(DataIntegrityViolationException.class);

            reset(linkRepository);
            ClassTemplate unchanged = classTemplateRepository
                    .findAllByIdInOrderByIdAsc(List.of(template.getId()))
                    .getFirst();
            assertThat(unchanged.getName()).isEqualTo("기존 템플릿");
            assertThat(unchanged.getDescription()).isEqualTo("기존 설명");
            assertThat(unchanged.getClassForm()).isEqualTo(ClassForm.GROUP);
            assertThat(unchanged.getRecurringDays()).containsExactly(DayOfWeek.MONDAY);
            assertThat(템플릿_연결을_조회한다(template.getId()))
                    .extracting(ClassTemplateClassType::getId, ClassTemplateClassType::getClassTypeId)
                    .containsExactly(org.assertj.core.groups.Tuple.tuple(originalLink.getId(), yoga.getId()));
        } finally {
            reset(linkRepository);
            linkRepository.deleteAll(템플릿_연결을_조회한다(template.getId()));
            linkRepository.flush();
            classTemplateRepository.deleteById(template.getId());
            classTemplateRepository.flush();
            classTypeRepository.deleteAllById(List.of(yoga.getId(), pilates.getId()));
            classTypeRepository.flush();
            studioRepository.deleteById(studio.getId());
            studioRepository.flush();
            memberRepository.deleteById(owner.getId());
            memberRepository.flush();
        }
    }

    @Test
    void 같은_시설의_수업_템플릿을_물리_삭제하면_소유_행만_연쇄_삭제하고_수업_종류와_회차는_보존한다() {
        // given
        Member owner = 회원을_저장한다("template-delete-owner");
        Studio studio = 시설을_저장한다(owner, "삭제 시설");
        ClassType yoga = 수업_종류를_저장한다(studio, "삭제 검증 요가");
        ClassType pilates = 수업_종류를_저장한다(studio, "삭제 검증 필라테스");
        ClassTemplate template = 템플릿을_저장한다(studio, "삭제할 템플릿", "물리 삭제 검증",
                Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY));
        연결을_저장한다(template, yoga);

        Member instructor = 회원을_저장한다("template-delete-instructor");
        StudioMembership instructorMembership = 소속을_저장한다(studio, instructor, SystemRole.INSTRUCTOR);
        ClassSession session = ClassSession.builder()
                .studioId(studio.getId())
                .instructorMembership(instructorMembership)
                .name("독립 수업 회차")
                .description("템플릿과 독립된 스냅샷")
                .classForm(ClassForm.GROUP)
                .durationMinutes(60)
                .capacity(12)
                .startAt(LocalDateTime.of(2026, 8, 17, 20, 0))
                .build();
        entityManager.persist(session);
        entityManager.flush();
        entityManager.persist(ClassSessionClassType.builder()
                .classSessionId(session.getId())
                .classTypeId(yoga.getId())
                .build());
        entityManager.flush();
        Long templateId = template.getId();
        Long yogaId = yoga.getId();
        Long pilatesId = pilates.getId();
        Long sessionId = session.getId();

        // when
        commandService.delete(owner.getId(), studio.getId(), templateId);
        entityManager.flush();
        entityManager.clear();

        // then
        assertThat(테이블_행_수를_조회한다("class_template", "id", templateId)).isZero();
        assertThat(테이블_행_수를_조회한다("class_template_recurring_day", "class_template_id", templateId)).isZero();
        assertThat(테이블_행_수를_조회한다("class_template_class_type", "class_template_id", templateId)).isZero();
        assertThat(classTypeRepository.findAllById(List.of(yogaId, pilatesId)))
                .extracting(ClassType::getId)
                .containsExactlyInAnyOrder(yogaId, pilatesId);
        ClassSession preservedSession = entityManager.find(ClassSession.class, sessionId);
        assertThat(preservedSession).isNotNull();
        assertThat(preservedSession.getName()).isEqualTo("독립 수업 회차");
        assertThat(preservedSession.isCanceled()).isFalse();
        assertThat(테이블_행_수를_조회한다("class_session_class_type", "class_session_id", sessionId)).isEqualTo(1);
    }

    @Test
    void 관리_권한이_없으면_대상_존재_여부보다_권한_예외가_먼저_발생하고_데이터를_보존한다() {
        // given
        Member owner = 회원을_저장한다("template-delete-permission-owner");
        Studio studio = 시설을_저장한다(owner, "삭제 권한 시설");
        Member student = 회원을_저장한다("template-delete-student");
        소속을_저장한다(studio, student, SystemRole.STUDENT);
        ClassType classType = 수업_종류를_저장한다(studio, "삭제 권한 요가");
        ClassTemplate template = 템플릿을_저장한다(studio, "보존할 템플릿", null,
                Set.of(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY));
        연결을_저장한다(template, classType);
        Long templateId = template.getId();

        // when / then
        assertThatThrownBy(() -> commandService.delete(student.getId(), studio.getId(), templateId))
                .isInstanceOfSatisfying(StudioException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(StudioErrorCode.PERMISSION_DENIED));
        assertThatThrownBy(() -> commandService.delete(student.getId(), studio.getId(), Long.MAX_VALUE))
                .isInstanceOfSatisfying(StudioException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(StudioErrorCode.PERMISSION_DENIED));
        assertThat(테이블_행_수를_조회한다("class_template", "id", templateId)).isEqualTo(1);
        assertThat(테이블_행_수를_조회한다("class_template_recurring_day", "class_template_id", templateId)).isEqualTo(2);
        assertThat(테이블_행_수를_조회한다("class_template_class_type", "class_template_id", templateId)).isEqualTo(1);
    }

    @Test
    void 없거나_다른_시설의_수업_템플릿을_삭제하면_CLASS_TEMPLATE_007이_발생하고_다른_데이터를_보존한다() {
        // given
        Member owner = 회원을_저장한다("template-delete-not-found-owner");
        Studio requestedStudio = 시설을_저장한다(owner, "삭제 요청 시설");
        Studio otherStudio = 시설을_저장한다(owner, "삭제 대상 외 시설");
        ClassType requestedType = 수업_종류를_저장한다(requestedStudio, "요청 시설 요가");
        ClassType otherType = 수업_종류를_저장한다(otherStudio, "다른 시설 필라테스");
        ClassTemplate requestedTemplate = 템플릿을_저장한다(
                requestedStudio, "요청 시설 보존 템플릿", null, Set.of(DayOfWeek.MONDAY));
        ClassTemplate otherTemplate = 템플릿을_저장한다(
                otherStudio, "다른 시설 보존 템플릿", null, Set.of(DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY));
        연결을_저장한다(requestedTemplate, requestedType);
        연결을_저장한다(otherTemplate, otherType);
        Long requestedTemplateId = requestedTemplate.getId();
        Long otherTemplateId = otherTemplate.getId();

        // when / then
        assertThatThrownBy(() -> commandService.delete(owner.getId(), requestedStudio.getId(), Long.MAX_VALUE))
                .isInstanceOfSatisfying(ClassException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ClassErrorCode.CLASS_TEMPLATE_NOT_FOUND));
        assertThatThrownBy(() -> commandService.delete(owner.getId(), requestedStudio.getId(), otherTemplateId))
                .isInstanceOfSatisfying(ClassException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ClassErrorCode.CLASS_TEMPLATE_NOT_FOUND));
        assertThat(테이블_행_수를_조회한다("class_template", "id", requestedTemplateId)).isEqualTo(1);
        assertThat(테이블_행_수를_조회한다("class_template_recurring_day", "class_template_id", requestedTemplateId)).isEqualTo(1);
        assertThat(테이블_행_수를_조회한다("class_template_class_type", "class_template_id", requestedTemplateId)).isEqualTo(1);
        assertThat(테이블_행_수를_조회한다("class_template", "id", otherTemplateId)).isEqualTo(1);
        assertThat(테이블_행_수를_조회한다("class_template_recurring_day", "class_template_id", otherTemplateId)).isEqualTo(2);
        assertThat(테이블_행_수를_조회한다("class_template_class_type", "class_template_id", otherTemplateId)).isEqualTo(1);
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

    private ClassType 수업_종류를_저장한다(Studio studio, String name) {
        return classTypeRepository.saveAndFlush(ClassTypeFixture.이름이_다른_수업_종류(studio, name));
    }

    private ClassTemplate 템플릿을_저장한다(
            Studio studio,
            String name,
            String description,
            Set<DayOfWeek> recurringDays
    ) {
        ClassTemplate template = ClassTemplate.builder()
                .studioId(studio.getId())
                .name(name)
                .description(description)
                .classForm(ClassForm.GROUP)
                .durationMinutes(60)
                .startTime(java.time.LocalTime.of(20, 0))
                .recurringDays(recurringDays)
                .capacity(12)
                .build();
        return classTemplateRepository.saveAndFlush(template);
    }

    private void 연결을_저장한다(ClassTemplate template, ClassType classType) {
        linkRepository.saveAndFlush(ClassTemplateClassType.builder()
                .classTemplateId(template.getId())
                .classTypeId(classType.getId())
                .build());
    }

    private List<ClassTemplateClassType> 템플릿_연결을_조회한다(Long classTemplateId) {
        return linkRepository.findAll().stream()
                .filter(link -> link.getClassTemplateId().equals(classTemplateId))
                .toList();
    }

    private void 수업_종류_없음과_기존_상태를_검증한다(
            Member owner,
            Studio studio,
            ClassTemplate template,
            ClassType originalClassType,
            ClassTemplateUpdateRequest request
    ) {
        assertThatThrownBy(() -> commandService.update(
                owner.getId(), studio.getId(), template.getId(), request))
                .isInstanceOfSatisfying(ClassException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ClassErrorCode.CLASS_TYPE_NOT_FOUND));
        기존_템플릿과_연결을_검증한다(template, originalClassType);
    }

    private void 기존_템플릿과_연결을_검증한다(ClassTemplate template, ClassType classType) {
        entityManager.flush();
        entityManager.clear();
        ClassTemplate unchanged = classTemplateRepository.findById(template.getId()).orElseThrow();
        assertThat(unchanged.getName()).isEqualTo("기존 템플릿");
        assertThat(unchanged.getDescription()).isEqualTo("기존 설명");
        assertThat(unchanged.getRecurringDays()).containsExactly(DayOfWeek.MONDAY);
        assertThat(템플릿_연결을_조회한다(template.getId()))
                .extracting(ClassTemplateClassType::getClassTypeId)
                .containsExactly(classType.getId());
    }

    private StudioMembership 소속을_저장한다(Studio studio, Member member, SystemRole systemRole) {
        StudioRole role = systemRole.toStudioRole(studio);
        entityManager.persist(role);
        StudioMembership membership = StudioMembership.builder()
                .studio(studio)
                .member(member)
                .name(member.getName())
                .studioRole(role)
                .status(MembershipStatus.ACTIVE)
                .joinedAt(LocalDateTime.now())
                .build();
        entityManager.persist(membership);
        entityManager.flush();
        return membership;
    }

    private long 테이블_행_수를_조회한다(String tableName, String columnName, Long id) {
        return ((Number) entityManager.createNativeQuery(
                        "SELECT COUNT(*) FROM " + tableName + " WHERE " + columnName + " = :id")
                .setParameter("id", id)
                .getSingleResult()).longValue();
    }
}
