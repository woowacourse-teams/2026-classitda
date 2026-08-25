package com.classitda.member.application;

import com.classitda.authentication.domain.repository.AuthAccountRepository;
import com.classitda.member.domain.Member;
import com.classitda.member.domain.repository.MemberRepository;
import com.classitda.studio.domain.StudioMembership;
import com.classitda.studio.domain.repository.StudioMembershipRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
@Service
public class MemberCleanupService {

    private final MemberRepository memberRepository;
    private final AuthAccountRepository authAccountRepository;
    private final StudioMembershipRepository studioMembershipRepository;
    private final Clock clock;

    public int cleanupDueMembers(int batchSize) {
        if (batchSize < 1) {
            throw new IllegalArgumentException("회원 개인정보 정리 배치 크기는 1 이상이어야 합니다.");
        }

        LocalDateTime occurredAt = LocalDateTime.now(clock);
        List<Member> members = memberRepository.findCleanupTargets(
                occurredAt,
                PageRequest.of(0, batchSize)
        );
        if (members.isEmpty()) {
            return 0;
        }

        List<Long> memberIds = members.stream()
                .map(Member::getId)
                .toList();
        List<StudioMembership> memberships = studioMembershipRepository.findAllByMemberIdIn(memberIds);

        authAccountRepository.deleteByMemberIdIn(memberIds);
        memberships.forEach(StudioMembership::clearPersonalInformation);
        members.forEach(member -> member.clearPersonalInformation(occurredAt));
        return members.size();
    }
}
