package com.classitda.studio.application;

import com.classitda.member.domain.Member;
import com.classitda.studio.domain.MembershipStatus;
import com.classitda.studio.domain.Permission;
import com.classitda.studio.domain.PermissionCode;
import com.classitda.studio.domain.Studio;
import com.classitda.studio.domain.StudioMembership;
import com.classitda.studio.domain.StudioRole;
import com.classitda.studio.domain.StudioRolePermission;
import com.classitda.studio.domain.SystemRole;
import com.classitda.studio.domain.repository.PermissionRepository;
import com.classitda.studio.domain.repository.StudioMembershipRepository;
import com.classitda.studio.domain.repository.StudioRepository;
import com.classitda.studio.domain.repository.StudioRolePermissionRepository;
import com.classitda.studio.domain.repository.StudioRoleRepository;
import com.classitda.studio.exception.StudioErrorCode;
import com.classitda.studio.exception.StudioException;
import com.classitda.studio.presentation.dto.StudioCreateRequest;
import com.classitda.studio.presentation.dto.StudioResponse;
import com.classitda.studio.presentation.dto.StudioUpdateRequest;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class StudioService {

    private final StudioRepository studioRepository;
    private final StudioRoleRepository studioRoleRepository;
    private final StudioMembershipRepository studioMembershipRepository;
    private final StudioRolePermissionRepository studioRolePermissionRepository;
    private final PermissionRepository permissionRepository;
    private final StudioPermissionService studioPermissionService;
    private final EntityManager entityManager;

    @Transactional
    public StudioResponse save(Long memberId, StudioCreateRequest request) {
        Member owner = getOwner(memberId);
        Studio studio = studioRepository.save(request.toEntity(owner));
        StudioRole ownerRole = saveSystemRoles(studio);
        saveOwnerMembership(studio, owner, ownerRole);
        return StudioResponse.from(studio);
    }

    public StudioResponse findById(Long studioId) {
        return StudioResponse.from(getStudio(studioId));
    }

    @Transactional
    public StudioResponse update(Long memberId, Long studioId, StudioUpdateRequest request) {
        Studio studio = getStudio(studioId);
        studioPermissionService.validate(studio, memberId, PermissionCode.STUDIO_UPDATE);
        studio.update(
                resolve(request.name(), studio.getName()),
                resolve(request.address(), studio.getAddress()),
                resolve(request.phoneNumber(), studio.getPhoneNumber()),
                resolve(request.imageUrl(), studio.getImageUrl()),
                resolve(request.description(), studio.getDescription()),
                resolve(request.openTime(), studio.getOpenTime()),
                resolve(request.closeTime(), studio.getCloseTime())
        );
        return StudioResponse.from(studio);
    }

    private Studio getStudio(Long studioId) {
        return studioRepository.findById(studioId)
                .orElseThrow(() -> new StudioException(StudioErrorCode.NOT_FOUND));
    }

    // TODO 회원가입 기능이 붙으면 MemberRepository 조회로 바꾼다
    private Member getOwner(Long memberId) {
        Member owner = entityManager.find(Member.class, memberId);
        if (owner == null) {
            throw new StudioException(StudioErrorCode.MEMBER_NOT_FOUND);
        }
        return owner;
    }

    private <T> T resolve(T requested, T current) {
        return requested != null ? requested : current;
    }

    private StudioRole saveSystemRoles(Studio studio) {
        Map<PermissionCode, Permission> permissions = permissionRepository.findAll().stream()
                .collect(java.util.stream.Collectors.toMap(Permission::getCode, permission -> permission));
        StudioRole ownerRole = null;
        for (SystemRole systemRole : SystemRole.values()) {
            StudioRole role = studioRoleRepository.save(systemRole.toStudioRole(studio));
            saveRolePermissions(role, systemRole.getDefaultPermissions(), permissions);
            if (systemRole == SystemRole.OWNER) {
                ownerRole = role;
            }
        }
        return ownerRole;
    }

    private void saveRolePermissions(
            StudioRole role,
            Set<PermissionCode> codes,
            Map<PermissionCode, Permission> permissions
    ) {
        List<StudioRolePermission> rolePermissions = codes.stream()
                .map(code -> StudioRolePermission.builder()
                        .studioRole(role)
                        .permission(permissions.get(code))
                        .build())
                .toList();
        studioRolePermissionRepository.saveAll(rolePermissions);
    }

    private void saveOwnerMembership(Studio studio, Member owner, StudioRole ownerRole) {
        studioMembershipRepository.save(StudioMembership.builder()
                .studio(studio)
                .member(owner)
                .studioRole(ownerRole)
                .status(MembershipStatus.ACTIVE)
                .joinedAt(LocalDateTime.now())
                .build());
    }
}
