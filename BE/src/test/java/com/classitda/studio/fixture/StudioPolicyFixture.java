package com.classitda.studio.fixture;

import com.classitda.studio.presentation.dto.StudioPolicyUpdateRequest;

public class StudioPolicyFixture {

    public static StudioPolicyUpdateRequest 무료_취소_시간만_바꾸는_수정_요청(int freeCancelMinutesBefore) {
        return new StudioPolicyUpdateRequest(null, freeCancelMinutesBefore, null, null);
    }

    public static StudioPolicyUpdateRequest 최대_홀드_일수만_바꾸는_수정_요청(int maxHoldDays) {
        return new StudioPolicyUpdateRequest(null, null, null, maxHoldDays);
    }

    public static StudioPolicyUpdateRequest 아무것도_바꾸지_않는_수정_요청() {
        return new StudioPolicyUpdateRequest(null, null, null, null);
    }
}
