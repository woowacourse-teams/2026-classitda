package com.classitda.classes.application.instructor;

import com.classitda.studio.domain.MembershipStatus;
import com.classitda.studio.domain.PermissionCode;
import com.classitda.studio.domain.Studio;
import com.classitda.studio.domain.StudioMembership;
import com.classitda.studio.domain.repository.StudioMembershipRepository;
import com.classitda.studio.domain.repository.StudioRepository;
import com.classitda.studio.domain.repository.StudioRolePermissionRepository;
import com.classitda.studio.exception.StudioErrorCode;
import com.classitda.studio.exception.StudioException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class InstructorSessionAccessReader {

    private final StudioMembershipRepository studioMembershipRepository;
    private final StudioRepository studioRepository;
    private final StudioRolePermissionRepository studioRolePermissionRepository;

    public InstructorSessionScope read(Long memberId, Long studioId, boolean mineOnly) {
        Studio studio = getStudio(studioId);
        StudioMembership membership = getActiveMembership(studioId, memberId);
        validateStaffRole(membership);

        if (studio.isOwner(memberId) || hasPermission(
                membership,
                PermissionCode.CLASS_SESSION_MANAGE_ALL
        )) {
            return new InstructorSessionScope(
                    membership.getId(),
                    mineOnly ? membership.getId() : null
            );
        }

        if (hasPermission(membership, PermissionCode.CLASS_SESSION_MANAGE_OWN)) {
            return new InstructorSessionScope(membership.getId(), membership.getId());
        }

        throw new StudioException(StudioErrorCode.PERMISSION_DENIED);
    }

    private Studio getStudio(Long studioId) {
        return studioRepository.findById(studioId)
                .orElseThrow(() -> new StudioException(StudioErrorCode.NOT_FOUND));
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

    private void validateStaffRole(StudioMembership membership) {
        if (membership.isStudent()) {
            throw new StudioException(StudioErrorCode.PERMISSION_DENIED);
        }
    }

    private boolean hasPermission(
            StudioMembership membership,
            PermissionCode permissionCode
    ) {
        return studioRolePermissionRepository.existsByStudioRoleIdAndPermissionCode(
                membership.getStudioRole().getId(),
                permissionCode
        );
    }
}
