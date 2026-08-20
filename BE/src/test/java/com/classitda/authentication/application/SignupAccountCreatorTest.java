package com.classitda.authentication.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;

import com.classitda.authentication.application.session.SignupSession;
import com.classitda.authentication.domain.AuthAccount;
import com.classitda.authentication.domain.OauthProvider;
import com.classitda.authentication.domain.repository.AuthAccountRepository;
import com.classitda.authentication.fixture.AuthAccountFixture;
import com.classitda.authentication.presentation.dto.signup.SignupRequest;
import com.classitda.member.domain.Member;
import com.classitda.member.domain.MemberTermAgreement;
import com.classitda.member.domain.Term;
import com.classitda.member.domain.TermCode;
import com.classitda.member.domain.repository.MemberRepository;
import com.classitda.member.domain.repository.MemberTermAgreementRepository;
import com.classitda.member.domain.repository.TermRepository;
import com.classitda.member.exception.MemberErrorCode;
import com.classitda.member.exception.MemberException;
import com.classitda.member.fixture.MemberFixture;
import com.classitda.member.fixture.TermFixture;
import com.classitda.support.MySqlRepositoryTest;
import jakarta.persistence.EntityManager;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Import(SignupAccountCreator.class)
@MySqlRepositoryTest
class SignupAccountCreatorTest {

    private static final String PHONE_NUMBER = "01012345678";
    private static final SignupSession SESSION = new SignupSession(
            OauthProvider.GOOGLE,
            "server-provider-subject",
            "server-provider@example.com"
    );

    private final SignupAccountCreator signupAccountCreator;
    private final TermRepository termRepository;
    private final MemberRepository memberRepository;
    private final AuthAccountRepository authAccountRepository;
    private final EntityManager entityManager;

    @MockitoSpyBean
    private MemberTermAgreementRepository memberTermAgreementRepository;

    @Autowired
    SignupAccountCreatorTest(
            SignupAccountCreator signupAccountCreator,
            TermRepository termRepository,
            MemberRepository memberRepository,
            AuthAccountRepository authAccountRepository,
            EntityManager entityManager
    ) {
        this.signupAccountCreator = signupAccountCreator;
        this.termRepository = termRepository;
        this.memberRepository = memberRepository;
        this.authAccountRepository = authAccountRepository;
        this.entityManager = entityManager;
    }

    @AfterEach
    void tearDown() {
        memberTermAgreementRepository.deleteAll();
        authAccountRepository.deleteAll();
        memberRepository.deleteAll();
    }

    @Test
    void 현재_필수와_선택_약관에_동의하면_서버_신원으로_세_엔티티를_저장한다() {
        // given
        List<Term> currentTerms = currentTerms();
        SignupRequest request = SignupRequest.of(
                "가입회원",
                currentTerms.stream().map(Term::getId).toList()
        );

        // when
        Long memberId = signupAccountCreator.create(request, SESSION, PHONE_NUMBER);
        entityManager.flush();
        entityManager.clear();

        // then
        Member member = memberRepository.findById(memberId).orElseThrow();
        assertThat(member.getName()).isEqualTo("가입회원");
        assertThat(member.getPhoneNumber()).isEqualTo(PHONE_NUMBER);

        AuthAccount authAccount = authAccountRepository.findByProviderAndProviderSubject(
                OauthProvider.GOOGLE,
                SESSION.providerSubject()
        ).orElseThrow();
        assertThat(authAccount.getMemberId()).isEqualTo(memberId);
        assertThat(authAccount.getProviderEmail()).isEqualTo(SESSION.providerEmail());

        List<MemberTermAgreement> agreements = memberTermAgreementRepository.findAll();
        assertThat(agreements).hasSize(3)
                .allSatisfy(agreement -> {
                    assertThat(agreement.getMember().getId()).isEqualTo(memberId);
                    assertThat(agreement.isAgreed()).isTrue();
                });
        assertThat(agreements)
                .extracting(
                        agreement -> agreement.getTerm().getCode(),
                        agreement -> agreement.getTerm().getVersion()
                )
                .containsExactlyInAnyOrder(
                        tuple(TermCode.SERVICE_TERMS, 1),
                        tuple(TermCode.PRIVACY_POLICY, 1),
                        tuple(TermCode.MARKETING_CONSENT, 1)
                );
    }

    @Test
    void 시설이_미리_등록해_둔_번호로_가입하면_기존_회원에_인증_계정을_붙이고_이름을_덮어쓴다() {
        // given
        Member preRegistered = memberRepository.saveAndFlush(Member.builder()
                .name("시설이적어둔이름")
                .phoneNumber(PHONE_NUMBER)
                .build());
        Long preRegisteredId = preRegistered.getId();
        long memberCountBefore = memberRepository.count();

        // when
        Long memberId = signupAccountCreator.create(validRequest(), SESSION, PHONE_NUMBER);
        entityManager.flush();
        entityManager.clear();

        // then
        assertThat(memberId).isEqualTo(preRegisteredId);
        assertThat(memberRepository.count()).isEqualTo(memberCountBefore);

        Member member = memberRepository.findById(memberId).orElseThrow();
        assertThat(member.getName()).isEqualTo("가입회원");
        assertThat(member.getPhoneNumber()).isEqualTo(PHONE_NUMBER);
        assertThat(authAccountRepository.existsByMemberId(memberId)).isTrue();
    }

    @Test
    void 선택_약관에_동의하지_않으면_필수_약관_행만_저장한다() {
        // given
        List<Long> requiredTermIds = currentTerms().stream()
                .filter(Term::isRequired)
                .map(Term::getId)
                .toList();

        // when
        Long memberId = signupAccountCreator.create(
                SignupRequest.of("가입회원", requiredTermIds),
                SESSION,
                PHONE_NUMBER
        );
        entityManager.flush();
        entityManager.clear();

        // then
        assertThat(memberId).isPositive();
        assertThat(memberTermAgreementRepository.findAll())
                .hasSize(2)
                .extracting(agreement -> agreement.getTerm().getCode())
                .containsExactlyInAnyOrder(TermCode.SERVICE_TERMS, TermCode.PRIVACY_POLICY);
    }

    @Test
    void 필수_약관이_누락되면_TERM_001이고_아무_가입_데이터도_저장하지_않는다() {
        // given
        Long serviceTermId = currentTerm(TermCode.SERVICE_TERMS).getId();

        // when / then
        assertMemberError(
                () -> signupAccountCreator.create(
                        SignupRequest.of("가입회원", List.of(serviceTermId)),
                        SESSION,
                        PHONE_NUMBER
                ),
                MemberErrorCode.REQUIRED_TERM_AGREEMENT_MISSING
        );
        assertNoSignupRows();
    }

    @Test
    void 존재하지_않는_약관_ID가_포함되면_TERM_002이고_아무_가입_데이터도_저장하지_않는다() {
        // given
        List<Long> agreedTermIds = new java.util.ArrayList<>(requiredTermIds());
        agreedTermIds.add(Long.MAX_VALUE);

        // when / then
        assertMemberError(
                () -> signupAccountCreator.create(
                        SignupRequest.of("가입회원", agreedTermIds),
                        SESSION,
                        PHONE_NUMBER
                ),
                MemberErrorCode.TERM_NOT_FOUND
        );
        assertNoSignupRows();
    }

    @Test
    void 중복된_약관_ID가_포함되면_TERM_003이고_아무_가입_데이터도_저장하지_않는다() {
        // given
        List<Long> requiredTermIds = requiredTermIds();
        List<Long> duplicated = List.of(
                requiredTermIds.get(0),
                requiredTermIds.get(1),
                requiredTermIds.get(1)
        );

        // when / then
        assertMemberError(
                () -> signupAccountCreator.create(
                        SignupRequest.of("가입회원", duplicated),
                        SESSION,
                        PHONE_NUMBER
                ),
                MemberErrorCode.TERM_ID_DUPLICATED
        );
        assertNoSignupRows();
    }

    @Test
    void 최신_버전이_아닌_약관_ID가_포함되면_TERM_004이고_아무_가입_데이터도_저장하지_않는다() {
        // given
        Long staleServiceTermId = currentTerm(TermCode.SERVICE_TERMS).getId();
        termRepository.saveAndFlush(TermFixture.약관(TermCode.SERVICE_TERMS, true, 2));
        Long privacyTermId = currentTerm(TermCode.PRIVACY_POLICY).getId();

        // when / then
        assertMemberError(
                () -> signupAccountCreator.create(
                        SignupRequest.of("가입회원", List.of(staleServiceTermId, privacyTermId)),
                        SESSION,
                        PHONE_NUMBER
                ),
                MemberErrorCode.TERM_STALE
        );
        assertNoSignupRows();
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Test
    void 약관_저장에_실패하면_회원_저장_후라도_전체_트랜잭션을_롤백한다() {
        // given
        doThrow(new IllegalStateException("forced agreement persistence failure"))
                .when(memberTermAgreementRepository).saveAllAndFlush(anyList());

        // when / then
        assertThatThrownBy(() -> signupAccountCreator.create(validRequest(), SESSION, PHONE_NUMBER))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("forced agreement persistence failure");
        assertNoSignupRows();
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Test
    void MySQL_전화번호_unique_제약은_중복_회원을_거부한다() {
        // given
        memberRepository.saveAndFlush(MemberFixture.회원("첫회원", PHONE_NUMBER));

        // when / then
        assertThatThrownBy(() -> memberRepository.saveAndFlush(MemberFixture.회원("둘째회원", PHONE_NUMBER)))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasRootCauseInstanceOf(java.sql.SQLIntegrityConstraintViolationException.class);
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Test
    void MySQL_소셜계정_unique_제약은_중복_계정을_거부한다() {
        // given
        Member firstMember = memberRepository.saveAndFlush(
                MemberFixture.회원("첫회원", "01033333333")
        );
        Member secondMember = memberRepository.saveAndFlush(
                MemberFixture.회원("둘째회원", "01044444444")
        );
        authAccountRepository.saveAndFlush(AuthAccountFixture.인증_계정(
                firstMember.getId(),
                SESSION.providerSubject(),
                "first@example.com"
        ));

        // when / then
        assertThatThrownBy(() -> authAccountRepository.saveAndFlush(AuthAccountFixture.인증_계정(
                secondMember.getId(),
                SESSION.providerSubject(),
                "second@example.com"
        )))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasRootCauseInstanceOf(java.sql.SQLIntegrityConstraintViolationException.class);
    }

    private SignupRequest validRequest() {
        return SignupRequest.of("가입회원", requiredTermIds());
    }

    private List<Long> requiredTermIds() {
        return currentTerms().stream()
                .filter(Term::isRequired)
                .map(Term::getId)
                .toList();
    }

    private List<Term> currentTerms() {
        return List.of(
                currentTerm(TermCode.SERVICE_TERMS),
                currentTerm(TermCode.PRIVACY_POLICY),
                currentTerm(TermCode.MARKETING_CONSENT)
        );
    }

    private Term currentTerm(TermCode code) {
        return termRepository.findAll().stream()
                .filter(term -> term.getCode() == code)
                .max(Comparator.comparingInt(Term::getVersion))
                .orElseThrow();
    }

    private void assertNoSignupRows() {
        assertThat(memberRepository.count()).isZero();
        assertThat(authAccountRepository.count()).isZero();
        assertThat(memberTermAgreementRepository.count()).isZero();
    }

    private void assertMemberError(Runnable action, MemberErrorCode expected) {
        assertThatThrownBy(action::run)
                .isInstanceOf(MemberException.class)
                .extracting(exception -> ((MemberException) exception).getErrorCode())
                .isEqualTo(expected);
    }
}
