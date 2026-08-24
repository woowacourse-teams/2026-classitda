package com.classitda.classes.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;

import com.classitda.classes.domain.template.ClassTemplate;
import com.classitda.classes.domain.template.ClassTemplateClassType;
import com.classitda.classes.domain.ClassType;
import com.classitda.classes.domain.repository.ClassTemplateClassTypeRepository;
import com.classitda.classes.domain.repository.ClassTemplateRepository;
import com.classitda.classes.domain.repository.ClassTypeRepository;
import com.classitda.classes.fixture.ClassTemplateFixture;
import com.classitda.classes.fixture.ClassTypeFixture;
import com.classitda.classes.presentation.dto.ClassTemplateResponse;
import com.classitda.classes.presentation.dto.ClassTypeResponse;
import com.classitda.member.domain.Member;
import com.classitda.studio.application.StudioPermissionService;
import com.classitda.studio.domain.Studio;
import com.classitda.studio.exception.StudioErrorCode;
import com.classitda.studio.exception.StudioException;
import com.classitda.studio.fixture.StudioFixture;
import com.classitda.support.MySqlRepositoryTest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "spring.jpa.properties.hibernate.generate_statistics=true")
@Import({ClassTemplateQueryService.class, StudioPermissionService.class})
@MySqlRepositoryTest
class ClassTemplateQueryServiceTest {

    private final ClassTemplateQueryService queryService;
    private final ClassTemplateRepository classTemplateRepository;
    private final ClassTemplateClassTypeRepository linkRepository;
    private final ClassTypeRepository classTypeRepository;
    private final EntityManager entityManager;
    private final Statistics statistics;

    @Autowired
    ClassTemplateQueryServiceTest(
            ClassTemplateQueryService queryService,
            ClassTemplateRepository classTemplateRepository,
            ClassTemplateClassTypeRepository linkRepository,
            ClassTypeRepository classTypeRepository,
            EntityManager entityManager,
            EntityManagerFactory entityManagerFactory
    ) {
        this.queryService = queryService;
        this.classTemplateRepository = classTemplateRepository;
        this.linkRepository = linkRepository;
        this.classTypeRepository = classTypeRepository;
        this.entityManager = entityManager;
        this.statistics = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
    }

    @Test
    void 템플릿이_없으면_빈_목록을_두_쿼리로_반환한다() {
        // given
        Member owner = 회원을_저장한다("empty-template-owner");
        Studio studio = 시설을_저장한다(owner, "빈 시설");
        측정을_준비한다();

        // when
        List<ClassTemplateResponse> responses = queryService.findAll(owner.getId(), studio.getId());
        long queryCount = statistics.getPrepareStatementCount();

        // then
        assertThat(responses).isEmpty();
        assertThat(queryCount).isEqualTo(2L);
    }

    @Test
    void 반복_요일이_없는_템플릿은_빈_요일_목록과_실제_수업_종류를_반환한다() {
        // given
        Member owner = 회원을_저장한다("no-day-template-owner");
        Studio studio = 시설을_저장한다(owner, "요일 없는 시설");
        ClassType classType = 수업_종류를_저장한다(studio, "요가");
        ClassTemplate template = 템플릿을_저장한다(studio, "요일 없음", Set.of());
        연결을_저장한다(template, classType);
        entityManager.flush();
        entityManager.clear();

        // when
        ClassTemplateResponse response = queryService.findAll(owner.getId(), studio.getId()).getFirst();

        // then
        assertThat(response.recurringDays()).isEmpty();
        assertThat(response.classTypes())
                .extracting(ClassTypeResponse::id, ClassTypeResponse::name)
                .containsExactly(tuple(classType.getId(), "요가"));
    }

    @Test
    void 템플릿과_수업_종류는_아이디순으로_조회하고_요일은_자연순으로_반환한다() {
        // given
        Member owner = 회원을_저장한다("ordered-template-owner");
        Studio studio = 시설을_저장한다(owner, "정렬 시설");
        ClassType firstType = 수업_종류를_저장한다(studio, "요가");
        ClassType secondType = 수업_종류를_저장한다(studio, "필라테스");
        ClassTemplate firstTemplate = 템플릿을_저장한다(
                studio, "첫 번째", Set.of(DayOfWeek.FRIDAY, DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY));
        ClassTemplate secondTemplate = 템플릿을_저장한다(studio, "두 번째", Set.of(DayOfWeek.SUNDAY));
        연결을_저장한다(firstTemplate, secondType);
        연결을_저장한다(firstTemplate, firstType);
        연결을_저장한다(secondTemplate, secondType);
        entityManager.flush();
        entityManager.clear();

        // when
        List<ClassTemplateResponse> responses = queryService.findAll(owner.getId(), studio.getId());

        // then
        assertThat(responses).extracting(ClassTemplateResponse::id)
                .containsExactly(firstTemplate.getId(), secondTemplate.getId());
        assertThat(responses.getFirst().recurringDays())
                .containsExactly(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY);
        assertThat(responses.getFirst().classTypes()).extracting(ClassTypeResponse::id)
                .containsExactly(firstType.getId(), secondType.getId());
        assertThat(responses.getFirst().classTypes()).extracting(ClassTypeResponse::name)
                .containsExactly("요가", "필라테스");
    }

    @Test
    void 비어_있지_않은_목록은_데이터_크기와_무관하게_같은_수의_네_개_이하_쿼리로_조회한다() {
        // given
        Member owner = 회원을_저장한다("query-count-owner");
        Studio smallStudio = 시설을_저장한다(owner, "작은 시설");
        ClassType smallType = 수업_종류를_저장한다(smallStudio, "소규모 요가");
        ClassTemplate smallTemplate = 템플릿을_저장한다(smallStudio, "작은 템플릿", Set.of());
        연결을_저장한다(smallTemplate, smallType);

        Studio largeStudio = 시설을_저장한다(owner, "큰 시설");
        ClassType firstType = 수업_종류를_저장한다(largeStudio, "대규모 요가");
        ClassType secondType = 수업_종류를_저장한다(largeStudio, "대규모 필라테스");
        ClassTemplate firstTemplate = 템플릿을_저장한다(
                largeStudio, "큰 템플릿 1", Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY));
        ClassTemplate secondTemplate = 템플릿을_저장한다(
                largeStudio, "큰 템플릿 2", Set.of(DayOfWeek.FRIDAY, DayOfWeek.SUNDAY));
        연결을_저장한다(firstTemplate, secondType);
        연결을_저장한다(firstTemplate, firstType);
        연결을_저장한다(secondTemplate, firstType);
        연결을_저장한다(secondTemplate, secondType);
        entityManager.flush();
        entityManager.clear();

        측정을_시작한다();
        List<ClassTemplateResponse> smallResponses = queryService.findAll(owner.getId(), smallStudio.getId());
        long smallQueryCount = statistics.getPrepareStatementCount();

        측정을_시작한다();
        List<ClassTemplateResponse> largeResponses = queryService.findAll(owner.getId(), largeStudio.getId());
        long largeQueryCount = statistics.getPrepareStatementCount();

        // then
        assertThat(smallResponses).hasSize(1);
        assertThat(largeResponses).hasSize(2);
        assertThat(largeResponses).allSatisfy(response -> {
            assertThat(response.recurringDays()).hasSize(2);
            assertThat(response.classTypes()).hasSize(2);
        });
        assertThat(smallQueryCount).isEqualTo(largeQueryCount);
        assertThat(smallQueryCount).isLessThanOrEqualTo(4L);
        assertThat(largeQueryCount).isLessThanOrEqualTo(4L);
    }

    @Test
    void 소속이_아닌_회원은_템플릿_목록을_조회할_수_없다() {
        // given
        Member owner = 회원을_저장한다("query-permission-owner");
        Studio studio = 시설을_저장한다(owner, "조회 권한 시설");
        Member stranger = 회원을_저장한다("query-stranger");

        // when / then
        assertThatThrownBy(() -> queryService.findAll(stranger.getId(), studio.getId()))
                .isInstanceOfSatisfying(StudioException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(StudioErrorCode.NOT_MEMBERSHIP));
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
                .openTime(LocalTime.of(9, 0))
                .closeTime(LocalTime.of(22, 0))
                .build();
        entityManager.persist(studio);
        entityManager.flush();
        return studio;
    }

    private ClassType 수업_종류를_저장한다(Studio studio, String name) {
        return classTypeRepository.saveAndFlush(ClassTypeFixture.이름이_다른_수업_종류(studio, name));
    }

    private ClassTemplate 템플릿을_저장한다(Studio studio, String name, Set<DayOfWeek> recurringDays) {
        return classTemplateRepository.saveAndFlush(
                ClassTemplateFixture.기본_수업_템플릿(studio.getId(), name, recurringDays));
    }

    private void 연결을_저장한다(ClassTemplate template, ClassType classType) {
        linkRepository.save(ClassTemplateClassType.builder()
                .classTemplateId(template.getId())
                .classTypeId(classType.getId())
                .build());
    }

    private void 측정을_준비한다() {
        entityManager.flush();
        entityManager.clear();
        측정을_시작한다();
    }

    private void 측정을_시작한다() {
        statistics.clear();
    }
}
