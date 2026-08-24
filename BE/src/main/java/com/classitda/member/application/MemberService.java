package com.classitda.member.application;

import com.classitda.member.domain.Member;
import com.classitda.member.domain.repository.MemberRepository;
import com.classitda.member.exception.MemberErrorCode;
import com.classitda.member.exception.MemberException;
import com.classitda.studio.domain.repository.StudioRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final StudioRepository studioRepository;
    private final Clock clock;

    public void withdraw(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));
        if (studioRepository.existsByOwnerId(memberId)) {
            throw new MemberException(MemberErrorCode.MEMBER_WITHDRAWAL_BLOCKED_BY_OWNED_STUDIO);
        }

        member.withdraw(LocalDateTime.now(clock));
    }
}
