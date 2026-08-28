package com.classitda.studio.application;

import com.classitda.studio.domain.PermissionCode;
import com.classitda.studio.domain.Studio;
import com.classitda.studio.domain.StudioPolicy;
import com.classitda.studio.domain.repository.StudioPolicyRepository;
import com.classitda.studio.domain.repository.StudioRepository;
import com.classitda.studio.exception.StudioErrorCode;
import com.classitda.studio.exception.StudioException;
import com.classitda.studio.presentation.dto.StudioPolicyResponse;
import com.classitda.studio.presentation.dto.StudioPolicyUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class StudioPolicyService {

    public static final int DEFAULT_MAX_HOLD_DAYS = 0;

    private static final int DEFAULT_RESERVATION_CLOSE_MINUTES_BEFORE = 30;
    private static final int DEFAULT_FREE_CANCEL_MINUTES_BEFORE = 720;
    private static final int DEFAULT_WAITING_OFFER_RESPONSE_MINUTES = 60;

    private final StudioPolicyRepository studioPolicyRepository;
    private final StudioPermissionService studioPermissionService;
    private final StudioRepository studioRepository;

    @Transactional
    public void saveDefaultPolicy(Studio studio) {
        studioPolicyRepository.save(StudioPolicy.builder()
                .studio(studio)
                .reservationCloseMinutesBefore(DEFAULT_RESERVATION_CLOSE_MINUTES_BEFORE)
                .freeCancelMinutesBefore(DEFAULT_FREE_CANCEL_MINUTES_BEFORE)
                .waitingOfferResponseMinutes(DEFAULT_WAITING_OFFER_RESPONSE_MINUTES)
                .maxHoldDays(DEFAULT_MAX_HOLD_DAYS)
                .build());
    }

    public StudioPolicyResponse findByStudioId(Long studioId) {
        getStudio(studioId);
        return StudioPolicyResponse.from(getPolicy(studioId));
    }

    @Transactional
    public StudioPolicyResponse update(Long memberId, Long studioId, StudioPolicyUpdateRequest request) {
        Studio studio = getStudio(studioId);
        studioPermissionService.validate(studio, memberId, PermissionCode.POLICY_MANAGE);
        StudioPolicy policy = getPolicy(studioId);
        policy.update(
                resolve(request.reservationCloseMinutesBefore(), policy.getReservationCloseMinutesBefore()),
                resolve(request.freeCancelMinutesBefore(), policy.getFreeCancelMinutesBefore()),
                resolve(request.waitingOfferResponseMinutes(), policy.getWaitingOfferResponseMinutes()),
                resolve(request.maxHoldDays(), policy.getMaxHoldDays())
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
