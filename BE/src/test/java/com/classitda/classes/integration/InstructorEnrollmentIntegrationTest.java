package com.classitda.classes.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.classitda.authentication.application.token.LoginTokenIssuer;
import com.classitda.classes.domain.ClassForm;
import com.classitda.classes.domain.enrollment.ClassSessionEnrollment;
import com.classitda.classes.domain.enrollment.EnrollmentStatus;
import com.classitda.classes.domain.repository.ClassSessionEnrollmentRepository;
import com.classitda.classes.domain.session.ClassSession;
import com.classitda.classes.fixture.ClassSessionFixture;
import com.classitda.classes.presentation.dto.InstructorEnrollmentCreateRequest;
import com.classitda.member.domain.Member;
import com.classitda.studio.application.StudioService;
import com.classitda.studio.domain.MembershipStatus;
import com.classitda.studio.domain.StudioMembership;
import com.classitda.studio.domain.StudioRole;
import com.classitda.studio.domain.SystemRole;
import com.classitda.studio.domain.repository.StudioMembershipRepository;
import com.classitda.studio.domain.repository.StudioRoleRepository;
import com.classitda.studio.fixture.StudioFixture;
import com.classitda.support.AuthenticationIntegrationTestConfiguration;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.transaction.support.TransactionTemplate;

@AutoConfigureRestTestClient
@Import(AuthenticationIntegrationTestConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.sql.init.mode=always"
})
class InstructorEnrollmentIntegrationTest {

    @Autowired
    private RestTestClient client;

    @Autowired
    private StudioService studioService;

    @Autowired
    private LoginTokenIssuer loginTokenIssuer;

    @Autowired
    private ClassSessionEnrollmentRepository enrollmentRepository;

    @Autowired
    private StudioMembershipRepository studioMembershipRepository;

    @Autowired
    private StudioRoleRepository studioRoleRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private EntityManager entityManager;

    @Test
    void 강사가_회원을_예약하면_201이고_수강권_없는_예약이_저장된다() {
        // given
        예약_환경 환경 = 예약_환경을_만든다("e2e-reserve");

        // when
        RestTestClient.ResponseSpec result = 예약한다(환경, 환경.accessToken(), "1");

        // then
        result.expectStatus().isCreated();
        ClassSessionEnrollment enrollment = 활성_신청을_읽는다(환경);
        assertThat(enrollment.getEnrollmentStatus()).isEqualTo(EnrollmentStatus.RESERVED);
        assertThat(enrollment.getMemberPassProduct()).isNull();
    }

    @Test
    void 같은_회원을_다시_예약하면_409를_반환한다() {
        // given
        예약_환경 환경 = 예약_환경을_만든다("e2e-duplicate");
        예약한다(환경, 환경.accessToken(), "1").expectStatus().isCreated();

        // when
        RestTestClient.ResponseSpec result = 예약한다(환경, 환경.accessToken(), "1");

        // then
        result.expectStatus().isEqualTo(409);
    }

    @Test
    void 예약을_취소하면_204이고_취소_상태로_남는다() {
        // given
        예약_환경 환경 = 예약_환경을_만든다("e2e-cancel");
        예약한다(환경, 환경.accessToken(), "1").expectStatus().isCreated();
        Long enrollmentId = 활성_신청을_읽는다(환경).getId();

        // when
        RestTestClient.ResponseSpec result = 예약을_취소한다(환경, enrollmentId, 환경.accessToken(), "1");

        // then
        result.expectStatus().isNoContent();
        ClassSessionEnrollment canceled = transactionTemplate.execute(
                status -> enrollmentRepository.findById(enrollmentId).orElseThrow());
        assertThat(canceled.getEnrollmentStatus()).isEqualTo(EnrollmentStatus.CANCELED);
    }

    @Test
    void 취소한_뒤_같은_회원을_다시_예약할_수_있다() {
        // given
        예약_환경 환경 = 예약_환경을_만든다("e2e-rebook");
        예약한다(환경, 환경.accessToken(), "1").expectStatus().isCreated();
        Long enrollmentId = 활성_신청을_읽는다(환경).getId();
        예약을_취소한다(환경, enrollmentId, 환경.accessToken(), "1").expectStatus().isNoContent();

        // when
        RestTestClient.ResponseSpec result = 예약한다(환경, 환경.accessToken(), "1");

        // then
        result.expectStatus().isCreated();
        assertThat(enrollmentRepository.countOccupied(환경.classSessionId())).isEqualTo(1L);
    }

    @Test
    void 인증_토큰이_없으면_401을_반환한다() {
        // given
        예약_환경 환경 = 예약_환경을_만든다("e2e-unauthenticated");

        // when
        RestTestClient.ResponseSpec result = client.post()
                .uri(
                        "/api/studios/{studioId}/class-sessions/instructor/{classSessionId}/enrollments",
                        환경.studioId(),
                        환경.classSessionId()
                )
                .header("X-API-Version", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .body(InstructorEnrollmentCreateRequest.from(환경.studentMembershipId()))
                .exchange();

        // then
        result.expectStatus().isUnauthorized();
        assertThat(enrollmentRepository.countOccupied(환경.classSessionId())).isZero();
    }

    @Test
    void 버전_헤더가_없으면_400을_반환한다() {
        // given
        예약_환경 환경 = 예약_환경을_만든다("e2e-no-version");

        // when
        RestTestClient.ResponseSpec result = client.post()
                .uri(
                        "/api/studios/{studioId}/class-sessions/instructor/{classSessionId}/enrollments",
                        환경.studioId(),
                        환경.classSessionId()
                )
                .header("Authorization", "Bearer " + 환경.accessToken())
                .contentType(MediaType.APPLICATION_JSON)
                .body(InstructorEnrollmentCreateRequest.from(환경.studentMembershipId()))
                .exchange();

        // then
        result.expectStatus().isBadRequest();
        assertThat(enrollmentRepository.countOccupied(환경.classSessionId())).isZero();
    }

    @Test
    void OpenAPI_문서에_대리_예약과_취소_경로가_노출된다() {
        // given / when
        String document = client.get()
                .uri("/v3/api-docs")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        // then
        assertThat(document)
                .contains("/api/studios/{studioId}/class-sessions/instructor/{classSessionId}/enrollments")
                .contains("/api/studios/{studioId}/class-sessions/instructor/{classSessionId}/enrollments/{enrollmentId}")
                .contains("회원 대리 예약")
                .contains("회원 대리 예약 취소");
    }

    private RestTestClient.ResponseSpec 예약한다(예약_환경 환경, String accessToken, String apiVersion) {
        return client.post()
                .uri(
                        "/api/studios/{studioId}/class-sessions/instructor/{classSessionId}/enrollments",
                        환경.studioId(),
                        환경.classSessionId()
                )
                .header("X-API-Version", apiVersion)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(InstructorEnrollmentCreateRequest.from(환경.studentMembershipId()))
                .exchange();
    }

    private RestTestClient.ResponseSpec 예약을_취소한다(
            예약_환경 환경,
            Long enrollmentId,
            String accessToken,
            String apiVersion
    ) {
        return client.delete()
                .uri(
                        "/api/studios/{studioId}/class-sessions/instructor/{classSessionId}/enrollments/{enrollmentId}",
                        환경.studioId(),
                        환경.classSessionId(),
                        enrollmentId
                )
                .header("X-API-Version", apiVersion)
                .header("Authorization", "Bearer " + accessToken)
                .exchange();
    }

    private ClassSessionEnrollment 활성_신청을_읽는다(예약_환경 환경) {
        return transactionTemplate.execute(status -> enrollmentRepository.findAll().stream()
                .filter(enrollment -> enrollment.getClassSession().getId().equals(환경.classSessionId()))
                .filter(enrollment -> enrollment.getEnrollmentStatus() != EnrollmentStatus.CANCELED)
                .findFirst()
                .orElseThrow());
    }

    private 예약_환경 예약_환경을_만든다(String providerId) {
        Long ownerId = 회원을_저장한다(providerId + "-owner");
        Long studioId = studioService.save(ownerId, StudioFixture.기본_시설_생성_요청()).id();
        Long studentMemberId = 회원을_저장한다(providerId + "-student");
        Long studentMembershipId = 학생_소속을_저장한다(studioId, studentMemberId);
        Long classSessionId = 수업_회차를_저장한다(studioId, ownerId);
        String accessToken = loginTokenIssuer.issueAccessToken(ownerId).accessToken();
        return new 예약_환경(studioId, classSessionId, studentMembershipId, accessToken);
    }

    private Long 회원을_저장한다(String providerId) {
        return transactionTemplate.execute(status -> {
            Member member = StudioFixture.아이디가_다른_소유자(providerId);
            entityManager.persist(member);
            entityManager.flush();
            return member.getId();
        });
    }

    private Long 학생_소속을_저장한다(Long studioId, Long memberId) {
        return transactionTemplate.execute(status -> {
            StudioRole role = studioRoleRepository
                    .findByStudioIdAndSystemRole(studioId, SystemRole.STUDENT)
                    .orElseThrow();
            Member member = entityManager.find(Member.class, memberId);
            StudioMembership membership = StudioMembership.builder()
                    .studio(role.getStudio())
                    .member(member)
                    .name(member.getName())
                    .studioRole(role)
                    .status(MembershipStatus.ACTIVE)
                    .joinedAt(LocalDateTime.now().minusDays(1))
                    .build();
            entityManager.persist(membership);
            entityManager.flush();
            return membership.getId();
        });
    }

    private Long 수업_회차를_저장한다(Long studioId, Long ownerId) {
        return transactionTemplate.execute(status -> {
            StudioMembership instructorMembership = studioMembershipRepository
                    .findByStudioIdAndMemberId(studioId, ownerId)
                    .orElseThrow();
            ClassSession classSession = ClassSessionFixture.수업_회차(
                    studioId,
                    instructorMembership,
                    "종단 검증 수업",
                    "실제 서버로 검증하는 수업",
                    ClassForm.GROUP,
                    60,
                    12,
                    LocalDateTime.now().plusDays(1).withNano(0)
            );
            entityManager.persist(classSession);
            entityManager.flush();
            return classSession.getId();
        });
    }

    private record 예약_환경(
            Long studioId,
            Long classSessionId,
            Long studentMembershipId,
            String accessToken
    ) {
    }
}
