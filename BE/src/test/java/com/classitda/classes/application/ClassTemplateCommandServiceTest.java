package com.classitda.classes.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.classitda.classes.domain.ClassForm;
import com.classitda.classes.domain.ClassTemplate;
import com.classitda.classes.domain.ClassTemplateClassType;
import com.classitda.classes.domain.ClassType;
import com.classitda.classes.domain.repository.ClassTemplateClassTypeRepository;
import com.classitda.classes.domain.repository.ClassTemplateRepository;
import com.classitda.classes.domain.repository.ClassTypeRepository;
import com.classitda.classes.exception.ClassErrorCode;
import com.classitda.classes.exception.ClassException;
import com.classitda.classes.fixture.ClassTemplateFixture;
import com.classitda.classes.fixture.ClassTypeFixture;
import com.classitda.member.domain.Member;
import com.classitda.studio.application.StudioPermissionService;
import com.classitda.studio.domain.MembershipStatus;
import com.classitda.studio.domain.Studio;
import com.classitda.studio.domain.StudioMembership;
import com.classitda.studio.domain.StudioRole;
import com.classitda.studio.domain.SystemRole;
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

@Import({ClassTemplateCommandService.class, StudioPermissionService.class})
@MySqlRepositoryTest
class ClassTemplateCommandServiceTest {

    private final ClassTemplateCommandService commandService;
    private final ClassTemplateRepository classTemplateRepository;
    private final ClassTemplateClassTypeRepository linkRepository;
    private final ClassTypeRepository classTypeRepository;
    private final EntityManager entityManager;

    @Autowired
    ClassTemplateCommandServiceTest(
            ClassTemplateCommandService commandService,
            ClassTemplateRepository classTemplateRepository,
            ClassTemplateClassTypeRepository linkRepository,
            ClassTypeRepository classTypeRepository,
            EntityManager entityManager
    ) {
        this.commandService = commandService;
        this.classTemplateRepository = classTemplateRepository;
        this.linkRepository = linkRepository;
        this.classTypeRepository = classTypeRepository;
        this.entityManager = entityManager;
    }

    @Test
    void 대표_강사가_수업_템플릿을_생성하면_내용과_선택한_수업_종류를_모두_저장한다() {
        // given
        Member owner = 회원을_저장한다("template-command-owner");
        Studio studio = 시설을_저장한다(owner, "명령 시설");
        ClassType first = 수업_종류를_저장한다(studio, "요가");
        ClassType second = 수업_종류를_저장한다(studio, "필라테스");

        // when
        commandService.save(owner.getId(), studio.getId(),
                ClassTemplateFixture.기본_수업_템플릿_생성_요청(List.of(second.getId(), first.getId())));
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
                .containsExactlyInAnyOrder(first.getId(), second.getId());
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
                        "null 요일 템플릿", null, recurringDays, List.of(classType.getId()))))
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
                ClassTemplateFixture.기본_수업_템플릿_생성_요청(List.of(classType.getId()))))
                .isInstanceOfSatisfying(StudioException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(StudioErrorCode.PERMISSION_DENIED));
        assertThat(classTemplateRepository.count()).isZero();
        assertThat(linkRepository.count()).isZero();
    }

    @Test
    void 다른_시설의_수업_종류가_포함되면_템플릿과_연결을_하나도_저장하지_않는다() {
        // given
        Member owner = 회원을_저장한다("template-cross-owner");
        Studio studio = 시설을_저장한다(owner, "요청 시설");
        Studio otherStudio = 시설을_저장한다(owner, "다른 시설");
        ClassType ownType = 수업_종류를_저장한다(studio, "요가");
        ClassType otherType = 수업_종류를_저장한다(otherStudio, "필라테스");

        // when / then
        assertThatThrownBy(() -> commandService.save(owner.getId(), studio.getId(),
                ClassTemplateFixture.기본_수업_템플릿_생성_요청(
                        List.of(ownType.getId(), otherType.getId()))))
                .isInstanceOfSatisfying(ClassException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ClassErrorCode.CLASS_TYPE_NOT_FOUND));
        assertThat(classTemplateRepository.count()).isZero();
        assertThat(linkRepository.count()).isZero();
    }

    @Test
    void 없는_시설이면_시설_없음_예외가_먼저_발생한다() {
        // when / then
        assertThatThrownBy(() -> commandService.save(1L, 999L,
                ClassTemplateFixture.기본_수업_템플릿_생성_요청(List.of(999L))))
                .isInstanceOfSatisfying(StudioException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(StudioErrorCode.NOT_FOUND));
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

    private ClassType 수업_종류를_저장한다(Studio studio, String name) {
        return classTypeRepository.saveAndFlush(ClassTypeFixture.이름이_다른_수업_종류(studio, name));
    }

    private void 소속을_저장한다(Studio studio, Member member, SystemRole systemRole) {
        StudioRole role = systemRole.toStudioRole(studio);
        entityManager.persist(role);
        entityManager.persist(StudioMembership.builder()
                .studio(studio)
                .member(member)
                .studioRole(role)
                .status(MembershipStatus.ACTIVE)
                .joinedAt(LocalDateTime.now())
                .build());
        entityManager.flush();
    }
}
