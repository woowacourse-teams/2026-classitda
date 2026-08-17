package com.classitda.classes.application.student;

import com.classitda.passproduct.domain.MemberPassProduct;
import com.classitda.passproduct.domain.repository.MemberPassProductRepository;
import com.classitda.passproduct.exception.PassProductErrorCode;
import com.classitda.passproduct.exception.PassProductException;
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

    private final MemberPassProductRepository memberPassProductRepository;
    private final StudioMembershipRepository studioMembershipRepository;
    private final StudioRepository studioRepository;

    StudentSessionAccess read(Long memberId, Long studioId, Long memberPassProductId) {
        Studio studio = getStudio(studioId);
        StudioMembership membership = getActiveMembership(studio.getId(), memberId);
        validateStudent(membership);

        MemberPassProduct memberPassProduct = getMemberPassProduct(
                memberPassProductId,
                membership.getId(),
                studioId
        );
        validateUsable(memberPassProduct);

        return new StudentSessionAccess(membership.getId(), memberPassProduct);
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

    private void validateStudent(StudioMembership membership) {
        if (!membership.isStudent()) {
            throw new StudioException(StudioErrorCode.PERMISSION_DENIED);
        }
    }

    private MemberPassProduct getMemberPassProduct(
            Long memberPassProductId,
            Long membershipId,
            Long studioId
    ) {
        return memberPassProductRepository.findOwnedWithProductAndClassTypes(
                        memberPassProductId,
                        membershipId,
                        studioId
                )
                .orElseThrow(() -> new PassProductException(
                        PassProductErrorCode.MEMBER_PASS_PRODUCT_NOT_FOUND
                ));
    }

    private void validateUsable(MemberPassProduct memberPassProduct) {
        if (!memberPassProduct.isUsable()) {
            throw new PassProductException(PassProductErrorCode.MEMBER_PASS_PRODUCT_UNAVAILABLE);
        }
    }
}
