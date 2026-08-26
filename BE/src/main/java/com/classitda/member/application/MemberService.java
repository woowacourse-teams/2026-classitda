package com.classitda.member.application;

import com.classitda.authentication.domain.AuthAccount;
import com.classitda.authentication.domain.OauthProvider;
import com.classitda.authentication.domain.repository.AuthAccountRepository;
import com.classitda.member.domain.Member;
import com.classitda.member.domain.repository.MemberRepository;
import com.classitda.member.exception.MemberErrorCode;
import com.classitda.member.exception.MemberException;
import com.classitda.member.presentation.dto.MyNameUpdateRequest;
import com.classitda.member.presentation.dto.MyProfileResponse;
import com.classitda.studio.application.StudioMembershipTerminationService;
import com.classitda.studio.domain.repository.StudioRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class MemberService {

    private static final OauthProvider PROFILE_EMAIL_PROVIDER = OauthProvider.GOOGLE;

    private final MemberRepository memberRepository;
    private final AuthAccountRepository authAccountRepository;
    private final StudioRepository studioRepository;
    private final StudioMembershipTerminationService studioMembershipTerminationService;
    private final Clock clock;

    public MyProfileResponse findMe(Long memberId) {
        Member member = getMember(memberId);
        String email = authAccountRepository
                .findByMemberIdAndProvider(memberId, PROFILE_EMAIL_PROVIDER)
                .map(AuthAccount::getProviderEmail)
                .orElse(null);

        return MyProfileResponse.of(member, email);
    }

    @Transactional
    public void updateName(Long memberId, MyNameUpdateRequest request) {
        Member member = getMember(memberId);

        member.updateName(request.name());
    }

    @Transactional
    public void withdraw(Long memberId) {
        Member member = getMember(memberId);
        if (studioRepository.existsByOwnerId(memberId)) {
            throw new MemberException(MemberErrorCode.MEMBER_WITHDRAWAL_BLOCKED_BY_OWNED_STUDIO);
        }

        member.withdraw(LocalDateTime.now(clock));
        studioMembershipTerminationService.terminateByMemberId(memberId);
    }

    private Member getMember(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));
    }
}
