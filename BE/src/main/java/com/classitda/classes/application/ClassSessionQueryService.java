package com.classitda.classes.application;

import com.classitda.classes.domain.ClassSession;
import com.classitda.classes.domain.ClassSessionClassType;
import com.classitda.classes.domain.ClassType;
import com.classitda.classes.domain.repository.ClassSessionClassTypeRepository;
import com.classitda.classes.domain.repository.ClassSessionRepository;
import com.classitda.classes.domain.repository.ClassTypeRepository;
import com.classitda.classes.exception.ClassErrorCode;
import com.classitda.classes.exception.ClassException;
import com.classitda.classes.presentation.dto.ClassSessionDetailResponse;
import com.classitda.studio.domain.MembershipStatus;
import com.classitda.studio.domain.Studio;
import com.classitda.studio.domain.StudioMembership;
import com.classitda.studio.domain.repository.StudioMembershipRepository;
import com.classitda.studio.domain.repository.StudioRepository;
import com.classitda.studio.exception.StudioErrorCode;
import com.classitda.studio.exception.StudioException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ClassSessionQueryService {

    private final ClassSessionClassTypeRepository classSessionClassTypeRepository;
    private final ClassSessionRepository classSessionRepository;
    private final ClassTypeRepository classTypeRepository;
    private final StudioMembershipRepository studioMembershipRepository;
    private final StudioRepository studioRepository;

    public ClassSessionDetailResponse findOne(
            Long memberId,
            Long studioId,
            Long classSessionId
    ) {
        Studio studio = getStudio(studioId);
        validateReadAccess(studio, memberId);

        ClassSession classSession = classSessionRepository
                .findByIdAndStudioId(classSessionId, studioId)
                .orElseThrow(() -> new ClassException(ClassErrorCode.CLASS_SESSION_NOT_FOUND));
        ClassType classType = getClassType(classSessionId, studioId);

        return ClassSessionDetailResponse.of(classSession, classType);
    }

    private Studio getStudio(Long studioId) {
        return studioRepository.findById(studioId)
                .orElseThrow(() -> new StudioException(StudioErrorCode.NOT_FOUND));
    }

    private void validateReadAccess(Studio studio, Long memberId) {
        if (studio.isOwner(memberId)) {
            return;
        }
        getActiveMembership(studio.getId(), memberId);
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

    private ClassType getClassType(Long classSessionId, Long studioId) {
        ClassSessionClassType classSessionClassType = classSessionClassTypeRepository
                .findByClassSessionId(classSessionId)
                .orElseThrow(() -> new ClassException(ClassErrorCode.CLASS_TYPE_NOT_FOUND));

        return classTypeRepository
                .findByIdAndStudioId(classSessionClassType.getClassTypeId(), studioId)
                .orElseThrow(() -> new ClassException(ClassErrorCode.CLASS_TYPE_NOT_FOUND));
    }
}
