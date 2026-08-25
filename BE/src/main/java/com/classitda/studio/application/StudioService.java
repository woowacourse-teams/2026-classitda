package com.classitda.studio.application;

import com.classitda.classes.application.ClassTypeService;
import com.classitda.common.image.ImageProperties;
import com.classitda.member.domain.Member;
import com.classitda.member.domain.repository.MemberRepository;
import com.classitda.studio.domain.MembershipStatus;
import com.classitda.studio.domain.Permission;
import com.classitda.studio.domain.PermissionCode;
import com.classitda.studio.domain.Address;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
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
    private final ClassTypeService classTypeService;
    private final StudioPermissionService studioPermissionService;
    private final ImageProperties imageProperties;
    private final MemberRepository memberRepository;

    @Transactional
    public StudioResponse save(Long memberId, StudioCreateRequest request) {
        Member owner = getOwner(memberId);
        Studio studio = saveStudio(request.toEntity(owner));
        StudioRole ownerRole = saveSystemRoles(studio);
        saveOwnerMembership(studio, owner, ownerRole);
        classTypeService.saveDefaultClassTypes(studio);
        return toResponse(studio);
    }

    public StudioResponse findById(Long studioId) {
        return toResponse(getStudio(studioId));
    }

    public List<StudioResponse> findAllByMemberId(Long memberId) {
        return studioMembershipRepository.findAllStudiosByMemberId(memberId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public StudioResponse update(Long memberId, Long studioId, StudioUpdateRequest request) {
        Studio studio = getStudio(studioId);
        studioPermissionService.validate(studio, memberId, PermissionCode.STUDIO_UPDATE);
        studio.update(
                resolve(request.name(), studio.getName()),
                resolveAddress(request, studio),
                resolve(request.phoneNumber(), studio.getPhoneNumber()),
                resolve(request.image(), studio.getImageObjectKey()),
                resolve(request.description(), studio.getDescription()),
                resolve(request.openTime(), studio.getOpenTime()),
                resolve(request.closeTime(), studio.getCloseTime())
        );
        flushStudio();

        return toResponse(studio);
    }

    @Transactional
    public void deleteImage(Long memberId, Long studioId) {
        Studio studio = getStudio(studioId);
        studioPermissionService.validate(studio, memberId, PermissionCode.STUDIO_UPDATE);
        studio.removeImage();
    }

    private Studio getStudio(Long studioId) {
        return studioRepository.findById(studioId)
                .orElseThrow(() -> new StudioException(StudioErrorCode.NOT_FOUND));
    }

    private Member getOwner(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new StudioException(StudioErrorCode.MEMBER_NOT_FOUND));
    }

    private Address resolveAddress(StudioUpdateRequest request, Studio studio) {
        if (request.address() == null) {
            return studio.getAddress();
        }
        return request.address().toAddress();
    }

    private Studio saveStudio(Studio studio) {
        try {
            Studio saved = studioRepository.save(studio);
            studioRepository.flush();
            return saved;
        } catch (DataIntegrityViolationException exception) {
            throw new StudioException(StudioErrorCode.IMAGE_ALREADY_USED);
        }
    }

    private void flushStudio() {
        try {
            studioRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new StudioException(StudioErrorCode.IMAGE_ALREADY_USED);
        }
    }

    private StudioResponse toResponse(Studio studio) {
        return StudioResponse.of(studio, toPublicUrl(studio.getImageObjectKey()));
    }

    private String toPublicUrl(String objectKey) {
        if (objectKey == null) {
            return null;
        }
        return imageProperties.toPublicUrl(objectKey);
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
                .name(owner.getName())
                .status(MembershipStatus.ACTIVE)
                .joinedAt(LocalDateTime.now())
                .build());
    }
}
