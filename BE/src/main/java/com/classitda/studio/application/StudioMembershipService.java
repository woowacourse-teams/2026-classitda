package com.classitda.studio.application;

import com.classitda.authentication.domain.repository.AuthAccountRepository;
import com.classitda.common.exception.ClassitdaException;
import com.classitda.common.exception.CommonErrorCode;
import com.classitda.common.pagination.CursorResponse;
import com.classitda.member.domain.Member;
import com.classitda.member.domain.repository.MemberRepository;
import com.classitda.studio.domain.MembershipStatus;
import com.classitda.studio.domain.PermissionCode;
import com.classitda.studio.domain.Studio;
import com.classitda.studio.domain.StudioMembership;
import com.classitda.studio.domain.StudioRole;
import com.classitda.studio.domain.SystemRole;
import com.classitda.studio.domain.repository.StudioMembershipRepository;
import com.classitda.studio.domain.repository.StudioRepository;
import com.classitda.studio.domain.repository.StudioRoleRepository;
import com.classitda.studio.exception.StudioErrorCode;
import com.classitda.studio.exception.StudioException;
import com.classitda.studio.presentation.dto.StudioMembershipCreateRequest;
import com.classitda.studio.presentation.dto.StudioMembershipResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class StudioMembershipService {

    private static final Long FIRST_PAGE_CURSOR = 0L;
    private static final int MIN_SIZE = 1;
    private static final int MAX_SIZE = 100;

    private final StudioMembershipRepository studioMembershipRepository;
    private final StudioRoleRepository studioRoleRepository;
    private final StudioRepository studioRepository;
    private final MemberRepository memberRepository;
    private final AuthAccountRepository authAccountRepository;
    private final StudioPermissionService studioPermissionService;

    @Transactional
    public void saveStudent(Long memberId, Long studioId, StudioMembershipCreateRequest request) {
        save(memberId, studioId, request, SystemRole.STUDENT);
    }

    @Transactional
    public void saveInstructor(Long memberId, Long studioId, StudioMembershipCreateRequest request) {
        save(memberId, studioId, request, SystemRole.INSTRUCTOR);
    }

    public CursorResponse<StudioMembershipResponse> findStudentsWithCursor(
            Long memberId,
            Long studioId,
            String cursor,
            int size
    ) {
        return findWithCursor(memberId, studioId, cursor, size, false);
    }

    public CursorResponse<StudioMembershipResponse> findInstructorsWithCursor(
            Long memberId,
            Long studioId,
            String cursor,
            int size
    ) {
        return findWithCursor(memberId, studioId, cursor, size, true);
    }

    private CursorResponse<StudioMembershipResponse> findWithCursor(
            Long memberId,
            Long studioId,
            String cursor,
            int size,
            boolean instructor
    ) {
        validateSize(size);
        Studio studio = getStudio(studioId);
        studioPermissionService.validate(studio, memberId, PermissionCode.MEMBER_READ);

        Slice<StudioMembership> slice = studioMembershipRepository.findWithCursorByStudioIdAndInstructor(
                studioId, instructor, toCursorId(cursor), PageRequest.ofSize(size));
        List<StudioMembership> studioMemberships = slice.getContent();
        Set<Long> registeredMemberIds = getRegisteredMemberIds(studioMemberships);
        List<StudioMembershipResponse> items = studioMemberships.stream()
                .map(studioMembership -> StudioMembershipResponse.of(
                        studioMembership,
                        registeredMemberIds.contains(studioMembership.getMember().getId())
                ))
                .toList();

        return CursorResponse.of(items, slice.hasNext(), toNextCursor(studioMemberships, slice.hasNext()));
    }

    public StudioMembershipResponse findById(Long memberId, Long studioId, Long membershipId) {
        Studio studio = getStudio(studioId);
        studioPermissionService.validate(studio, memberId, PermissionCode.MEMBER_READ);

        StudioMembership studioMembership = studioMembershipRepository
                .findWithMemberByIdAndStudioId(membershipId, studioId)
                .orElseThrow(() -> new StudioException(StudioErrorCode.MEMBERSHIP_NOT_FOUND));

        return StudioMembershipResponse.of(
                studioMembership,
                authAccountRepository.existsByMemberId(studioMembership.getMember().getId())
        );
    }

    private void save(
            Long memberId,
            Long studioId,
            StudioMembershipCreateRequest request,
            SystemRole systemRole
    ) {
        Studio studio = getStudio(studioId);
        studioPermissionService.validate(studio, memberId, PermissionCode.MEMBER_INVITE);
        if (systemRole.isInstructor()) {
            studioPermissionService.validate(studio, memberId, PermissionCode.ROLE_MANAGE);
        }

        StudioRole studioRole = getStudioRole(studioId, systemRole);
        Member member = getOrCreateMember(request.name(), request.phoneNumber());
        validateNotRegistered(studioId, member.getId());

        saveStudioMembership(studio, member, studioRole, request.name());
    }

    private Studio getStudio(Long studioId) {
        return studioRepository.findById(studioId)
                .orElseThrow(() -> new StudioException(StudioErrorCode.NOT_FOUND));
    }

    private StudioRole getStudioRole(Long studioId, SystemRole systemRole) {
        return studioRoleRepository.findByStudioIdAndSystemRole(studioId, systemRole)
                .orElseThrow(() -> new StudioException(StudioErrorCode.STUDIO_ROLE_NOT_FOUND));
    }

    private Member getOrCreateMember(String name, String phoneNumber) {
        return memberRepository.findByPhoneNumber(phoneNumber)
                .orElseGet(() -> saveMember(name, phoneNumber));
    }

    private Member saveMember(String name, String phoneNumber) {
        try {
            return memberRepository.saveAndFlush(Member.builder()
                    .name(name)
                    .phoneNumber(phoneNumber)
                    .build());
        } catch (DataIntegrityViolationException exception) {
            return memberRepository.findByPhoneNumber(phoneNumber)
                    .orElseThrow(() -> new StudioException(StudioErrorCode.MEMBER_NOT_FOUND));
        }
    }

    private void validateNotRegistered(Long studioId, Long memberId) {
        if (studioMembershipRepository.existsByStudioIdAndMemberId(studioId, memberId)) {
            throw new StudioException(StudioErrorCode.MEMBERSHIP_ALREADY_EXISTS);
        }
    }

    private void saveStudioMembership(Studio studio, Member member, StudioRole studioRole, String name
    ) {
        try {
            studioMembershipRepository.saveAndFlush(StudioMembership.builder()
                    .studio(studio)
                    .member(member)
                    .studioRole(studioRole)
                    .name(name)
                    .status(MembershipStatus.ACTIVE)
                    .joinedAt(LocalDateTime.now())
                    .build());
        } catch (DataIntegrityViolationException exception) {
            throw new StudioException(StudioErrorCode.MEMBERSHIP_ALREADY_EXISTS);
        }
    }

    private void validateSize(int size) {
        if (size < MIN_SIZE || size > MAX_SIZE) {
            throw new ClassitdaException(CommonErrorCode.INVALID_INPUT);
        }
    }

    private Long toCursorId(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return FIRST_PAGE_CURSOR;
        }
        try {
            return Long.parseLong(cursor);
        } catch (NumberFormatException exception) {
            throw new ClassitdaException(CommonErrorCode.INVALID_INPUT);
        }
    }

    private Set<Long> getRegisteredMemberIds(List<StudioMembership> studioMemberships) {
        if (studioMemberships.isEmpty()) {
            return Set.of();
        }

        List<Long> memberIds = studioMemberships.stream()
                .map(studioMembership -> studioMembership.getMember().getId())
                .toList();

        return Set.copyOf(authAccountRepository.findMemberIdsByMemberIdIn(memberIds));
    }

    private String toNextCursor(List<StudioMembership> studioMemberships, boolean hasNext) {
        if (!hasNext || studioMemberships.isEmpty()) {
            return null;
        }

        return String.valueOf(studioMemberships.getLast().getId());
    }
}
