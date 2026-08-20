package com.classitda.classes.application.student;

import com.classitda.studio.domain.MembershipStatus;
import com.classitda.studio.domain.Studio;
import com.classitda.studio.domain.StudioMembership;
import com.classitda.studio.domain.repository.StudioMembershipRepository;
import com.classitda.studio.domain.repository.StudioRepository;
import com.classitda.studio.exception.StudioErrorCode;
import com.classitda.studio.exception.StudioException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class StudentSessionAccessReader {

    private final StudioMembershipRepository studioMembershipRepository;
    private final StudioRepository studioRepository;

    public Long readMembershipId(Long memberId, Long studioId) {
        Studio studio = studioRepository.findById(studioId)
                .orElseThrow(() -> new StudioException(StudioErrorCode.NOT_FOUND));

        StudioMembership membership = getActiveMembership(studio.getId(), memberId);

        if (!membership.isStudent()) {
            throw new StudioException(StudioErrorCode.PERMISSION_DENIED);
        }

        return membership.getId();
    }

    private StudioMembership getActiveMembership(Long studioId, Long memberId) {
        StudioMembership membership = studioMembershipRepository
                .findByStudioIdAndMemberId(studioId, memberId)
                .orElseThrow(() -> new StudioException(StudioErrorCode.NOT_MEMBERSHIP));

        if (membership.getStatus() != MembershipStatus.ACTIVE) {
            throw new StudioException(StudioErrorCode.MEMBERSHIP_INACTIVE);
        }

        return membership;
    }
}
