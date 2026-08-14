package com.classitda.authentication.application;

import com.classitda.authentication.application.session.SignupSession;
import com.classitda.authentication.domain.AuthAccount;
import com.classitda.authentication.domain.repository.AuthAccountRepository;
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
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
@Service
public class SignupAccountCreator {

    private final TermRepository termRepository;
    private final MemberRepository memberRepository;
    private final AuthAccountRepository authAccountRepository;
    private final MemberTermAgreementRepository memberTermAgreementRepository;

    public Long create(SignupRequest request, SignupSession signupSession, String verifiedPhoneNumber) {
        List<Term> agreedTerms = findAndValidateAgreedTerms(request.agreedTermIds());

        Member member = saveOrUpdateMember(request.name(), verifiedPhoneNumber);
        saveAuthAccount(member.getId(), signupSession);
        saveAgreements(member, agreedTerms);
        return member.getId();
    }

    private List<Term> findAndValidateAgreedTerms(List<Long> agreedTermIds) {
        Set<Long> agreedTermIdSet = new HashSet<>(agreedTermIds);
        validateNoDuplicateTermIds(agreedTermIds, agreedTermIdSet);

        List<Term> allTerms = termRepository.findAll();
        List<Term> agreedTerms = allTerms.stream()
                .filter(term -> agreedTermIdSet.contains(term.getId()))
                .toList();

        validateAllTermsExist(agreedTerms, agreedTermIdSet);

        Map<TermCode, Term> currentTermsByCode = findCurrentTermsByCode(allTerms);

        validateAllTermsCurrent(agreedTerms, currentTermsByCode);
        validateAllRequiredTermsAgreed(agreedTermIdSet, currentTermsByCode);

        return agreedTerms;
    }

    private void validateNoDuplicateTermIds(List<Long> agreedTermIds, Set<Long> agreedTermIdSet) {
        if (agreedTermIdSet.size() != agreedTermIds.size()) {
            throw new MemberException(MemberErrorCode.TERM_ID_DUPLICATED);
        }
    }

    private Map<TermCode, Term> findCurrentTermsByCode(List<Term> allTerms) {
        Map<TermCode, Term> currentTermsByCode = new EnumMap<>(TermCode.class);

        for (Term term : allTerms) {
            Term currentTerm = currentTermsByCode.get(term.getCode());
            if (currentTerm == null || term.getVersion() > currentTerm.getVersion()) {
                currentTermsByCode.put(term.getCode(), term);
            }
        }

        return currentTermsByCode;
    }

    private void validateAllTermsExist(List<Term> agreedTerms, Set<Long> agreedTermIdSet) {
        if (agreedTerms.size() != agreedTermIdSet.size()) {
            throw new MemberException(MemberErrorCode.TERM_NOT_FOUND);
        }
    }

    private void validateAllTermsCurrent(List<Term> agreedTerms, Map<TermCode, Term> currentTermsByCode) {
        boolean staleTermExists = agreedTerms.stream()
                .anyMatch(term -> !term.getId().equals(currentTermsByCode.get(term.getCode()).getId()));
        if (staleTermExists) {
            throw new MemberException(MemberErrorCode.TERM_STALE);
        }
    }

    private void validateAllRequiredTermsAgreed(
            Set<Long> agreedTermIdSet,
            Map<TermCode, Term> currentTermsByCode
    ) {
        boolean requiredTermMissing = currentTermsByCode.values().stream()
                .filter(Term::isRequired)
                .anyMatch(term -> !agreedTermIdSet.contains(term.getId()));

        if (requiredTermMissing) {
            throw new MemberException(MemberErrorCode.REQUIRED_TERM_AGREEMENT_MISSING);
        }
    }

    private Member saveOrUpdateMember(String name, String verifiedPhoneNumber) {
        return memberRepository.findByPhoneNumber(verifiedPhoneNumber)
                .map(member -> updateMemberName(member, name))
                .orElseGet(() -> saveMember(name, verifiedPhoneNumber));
    }

    private Member updateMemberName(Member member, String name) {
        member.updateName(name);
        return memberRepository.saveAndFlush(member);
    }

    private Member saveMember(String name, String verifiedPhoneNumber) {
        Member member = Member.builder()
                .name(name)
                .phoneNumber(verifiedPhoneNumber)
                .build();

        return memberRepository.saveAndFlush(member);
    }

    private void saveAuthAccount(Long memberId, SignupSession signupSession) {
        AuthAccount authAccount = AuthAccount.builder()
                .memberId(memberId)
                .provider(signupSession.provider())
                .providerSubject(signupSession.providerSubject())
                .providerEmail(signupSession.providerEmail())
                .build();

        authAccountRepository.save(authAccount);
    }

    private void saveAgreements(Member member, List<Term> agreedTerms) {
        List<MemberTermAgreement> agreements = agreedTerms.stream()
                .map(term -> MemberTermAgreement.builder()
                        .member(member)
                        .term(term)
                        .agreed(true)
                        .build())
                .toList();

        memberTermAgreementRepository.saveAllAndFlush(agreements);
    }
}
