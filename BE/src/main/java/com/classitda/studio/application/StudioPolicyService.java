package com.classitda.studio.application;

import com.classitda.studio.domain.Studio;
import com.classitda.studio.domain.StudioPolicy;
import com.classitda.studio.domain.repository.StudioPolicyRepository;
import com.classitda.studio.domain.repository.StudioRepository;
import com.classitda.studio.exception.StudioErrorCode;
import com.classitda.studio.exception.StudioException;
import com.classitda.studio.presentation.dto.StudioPolicyCreateRequest;
import com.classitda.studio.presentation.dto.StudioPolicyResponse;
import com.classitda.studio.presentation.dto.StudioPolicyUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class StudioPolicyService {

    private final StudioPolicyRepository studioPolicyRepository;
    private final StudioRepository studioRepository;

    @Transactional
    public StudioPolicyResponse save(Long memberId, Long studioId, StudioPolicyCreateRequest request) {
        Studio studio = getStudio(studioId);
        studio.validateOwner(memberId);
        if (studioPolicyRepository.existsByStudioId(studioId)) {
            throw new StudioException(StudioErrorCode.POLICY_ALREADY_EXISTS);
        }
        return StudioPolicyResponse.from(studioPolicyRepository.save(request.toEntity(studio)));
    }

    public StudioPolicyResponse findByStudioId(Long studioId) {
        getStudio(studioId);
        return StudioPolicyResponse.from(getPolicy(studioId));
    }

    @Transactional
    public StudioPolicyResponse update(Long memberId, Long studioId, StudioPolicyUpdateRequest request) {
        Studio studio = getStudio(studioId);
        studio.validateOwner(memberId);
        StudioPolicy policy = getPolicy(studioId);
        policy.update(
                resolve(request.reservationCloseMinutesBefore(), policy.getReservationCloseMinutesBefore()),
                resolve(request.freeCancelMinutesBefore(), policy.getFreeCancelMinutesBefore()),
                resolve(request.waitingOfferResponseMinutes(), policy.getWaitingOfferResponseMinutes())
        );
        return StudioPolicyResponse.from(policy);
    }

    private <T> T resolve(T requested, T current) {
        return requested != null ? requested : current;
    }

    private Studio getStudio(Long studioId) {
        return studioRepository.findById(studioId)
                .orElseThrow(() -> new StudioException(StudioErrorCode.NOT_FOUND));
    }

    private StudioPolicy getPolicy(Long studioId) {
        return studioPolicyRepository.findByStudioId(studioId)
                .orElseThrow(() -> new StudioException(StudioErrorCode.POLICY_NOT_FOUND));
    }
}
