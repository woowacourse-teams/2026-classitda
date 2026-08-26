package com.classitda.studio.application;

import com.classitda.studio.domain.MembershipStatus;
import com.classitda.studio.domain.PermissionCode;
import com.classitda.studio.domain.Studio;
import com.classitda.studio.domain.StudioMembership;
import com.classitda.studio.domain.repository.StudioMembershipRepository;
import com.classitda.studio.domain.repository.StudioRolePermissionRepository;
import com.classitda.studio.exception.StudioErrorCode;
import com.classitda.studio.exception.StudioException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class StudioPermissionService {

    private final StudioMembershipRepository studioMembershipRepository;
    private final StudioRolePermissionRepository studioRolePermissionRepository;

    public void validate(Studio studio, Long memberId, PermissionCode required) {
        if (studio.isOwner(memberId)) {
            return;
        }

        StudioMembership membership = getActiveMembership(studio.getId(), memberId);
        if (!studioRolePermissionRepository.existsByStudioRoleIdAndPermissionCode(
                membership.getStudioRole().getId(), required)) {
            throw new StudioException(StudioErrorCode.PERMISSION_DENIED);
        }
    }

    public void validateStaff(Studio studio, Long memberId) {
        if (studio.isOwner(memberId)) {
            return;
        }

        StudioMembership membership = getActiveMembership(studio.getId(), memberId);
        if (membership.isStudent()) {
            throw new StudioException(StudioErrorCode.PERMISSION_DENIED);
        }
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
