package com.classitda.studio.application;

import com.classitda.member.domain.Member;
import com.classitda.studio.domain.MembershipStatus;
import com.classitda.studio.domain.Studio;
import com.classitda.studio.domain.StudioMembership;
import com.classitda.studio.domain.StudioRole;
import com.classitda.studio.domain.SystemRole;
import com.classitda.studio.domain.repository.StudioMembershipRepository;
import com.classitda.studio.domain.repository.StudioRepository;
import com.classitda.studio.domain.repository.StudioRoleRepository;
import com.classitda.studio.exception.StudioErrorCode;
import com.classitda.studio.exception.StudioException;
import com.classitda.studio.presentation.dto.StudioCreateRequest;
import com.classitda.studio.presentation.dto.StudioResponse;
import com.classitda.studio.presentation.dto.StudioUpdateRequest;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.Arrays;
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
    private final EntityManager entityManager;

    @Transactional
    public StudioResponse save(Long memberId, StudioCreateRequest request) {
        Member owner = getOwnerReference(memberId);
        Studio studio = studioRepository.save(request.toEntity(owner));
        saveNonOwnerSystemRoles(studio);
        StudioRole ownerRole = studioRoleRepository.save(SystemRole.OWNER.toStudioRole(studio));
        saveOwnerMembership(studio, owner, ownerRole);
        return StudioResponse.from(studio);
    }

    public StudioResponse findById(Long studioId) {
        Studio studio = studioRepository.findById(studioId)
                .orElseThrow(() -> new StudioException(StudioErrorCode.NOT_FOUND));
        return StudioResponse.from(studio);
    }

    @Transactional
    public StudioResponse update(Long memberId, Long studioId, StudioUpdateRequest request) {
        Studio studio = studioRepository.findById(studioId)
                .orElseThrow(() -> new StudioException(StudioErrorCode.NOT_FOUND));
        studio.validateOwner(memberId);
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

    // TODO 회원가입 기능의 MemberRepository가 추가되면 조회로 바꿔 회원 존재를 검증한다
    private Member getOwnerReference(Long memberId) {
        return entityManager.getReference(Member.class, memberId);
    }

    private <T> T resolve(T requested, T current) {
        return requested != null ? requested : current;
    }

    private void saveNonOwnerSystemRoles(Studio studio) {
        Arrays.stream(SystemRole.values())
                .filter(systemRole -> systemRole != SystemRole.OWNER)
                .forEach(systemRole -> studioRoleRepository.save(systemRole.toStudioRole(studio)));
    }

    private void saveOwnerMembership(Studio studio, Member owner, StudioRole ownerRole) {
        studioMembershipRepository.save(StudioMembership.builder()
                .studio(studio)
                .member(owner)
                .studioRole(ownerRole)
                .instructor(true)
                .customer(false)
                .status(MembershipStatus.ACTIVE)
                .joinedAt(LocalDateTime.now())
                .build());
    }
}
