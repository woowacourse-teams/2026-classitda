package com.classitda.studio.fixture;

import com.classitda.studio.presentation.dto.StudioPolicyCreateRequest;
import com.classitda.studio.presentation.dto.StudioPolicyUpdateRequest;

public class StudioPolicyFixture {

    public static final int DEFAULT_RESERVATION_CLOSE_MINUTES = 60;
    public static final int DEFAULT_FREE_CANCEL_MINUTES = 1440;
    public static final int DEFAULT_WAITING_RESPONSE_MINUTES = 30;

    public static StudioPolicyCreateRequest 기본_정책_생성_요청() {
        return new StudioPolicyCreateRequest(
                DEFAULT_RESERVATION_CLOSE_MINUTES,
                DEFAULT_FREE_CANCEL_MINUTES,
                DEFAULT_WAITING_RESPONSE_MINUTES
        );
    }

    public static StudioPolicyUpdateRequest 무료_취소_시간만_바꾸는_수정_요청(int freeCancelMinutesBefore) {
        return new StudioPolicyUpdateRequest(null, freeCancelMinutesBefore, null);
    }

    public static StudioPolicyUpdateRequest 아무것도_바꾸지_않는_수정_요청() {
        return new StudioPolicyUpdateRequest(null, null, null);
    }
}
