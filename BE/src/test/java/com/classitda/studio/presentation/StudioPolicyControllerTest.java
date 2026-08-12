package com.classitda.studio.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import com.classitda.common.config.ApiVersionConfig;
import com.classitda.common.exception.GlobalExceptionHandler;
import com.classitda.studio.application.StudioPolicyService;
import com.classitda.studio.exception.StudioErrorCode;
import com.classitda.studio.exception.StudioException;
import com.classitda.studio.fixture.StudioPolicyFixture;
import com.classitda.studio.presentation.dto.StudioPolicyCreateRequest;
import com.classitda.studio.presentation.dto.StudioPolicyResponse;
import com.classitda.studio.presentation.dto.StudioPolicyUpdateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.client.RestTestClient;

@AutoConfigureRestTestClient
@Import({ApiVersionConfig.class, GlobalExceptionHandler.class})
@WebMvcTest(StudioPolicyController.class)
class StudioPolicyControllerTest {

    private final RestTestClient client;

    @MockitoBean
    private StudioPolicyService studioPolicyService;

    @Autowired
    StudioPolicyControllerTest(RestTestClient client) {
        this.client = client;
    }

    @Test
    void 운영_정책을_등록하면_201과_정책_정보를_반환한다() {
        // given
        when(studioPolicyService.save(anyLong(), anyLong(), any(StudioPolicyCreateRequest.class)))
                .thenReturn(new StudioPolicyResponse(1L, 60, 1440, 30));

        // when
        RestTestClient.ResponseSpec result = client.post()
                .uri("/api/studios/1/policy")
                .header("X-API-Version", "1")
                .header("X-Member-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .body(StudioPolicyFixture.기본_정책_생성_요청())
                .exchange();

        // then
        result.expectStatus().isCreated()
                .expectBody()
                .json("""
                        {"id":1,"reservationCloseMinutesBefore":60,
                         "freeCancelMinutesBefore":1440,"waitingOfferResponseMinutes":30}
                        """, JsonCompareMode.STRICT);
    }

    @Test
    void 필수_값이_없으면_COMMON_001을_반환한다() {
        // given
        StudioPolicyCreateRequest request = new StudioPolicyCreateRequest(60, 1440, null);

        // when
        RestTestClient.ResponseSpec result = client.post()
                .uri("/api/studios/1/policy")
                .header("X-API-Version", "1")
                .header("X-Member-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange();

        // then
        result.expectStatus().isBadRequest()
                .expectBody()
                .json("""
                        {"code":"COMMON-001","message":"요청 값이 올바르지 않습니다."}
                        """, JsonCompareMode.STRICT);
    }

    @Test
    void 예약_대기_응답_시간이_0이면_COMMON_001을_반환한다() {
        // given
        StudioPolicyCreateRequest request = new StudioPolicyCreateRequest(60, 1440, 0);

        // when
        RestTestClient.ResponseSpec result = client.post()
                .uri("/api/studios/1/policy")
                .header("X-API-Version", "1")
                .header("X-Member-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange();

        // then
        result.expectStatus().isBadRequest()
                .expectBody()
                .json("""
                        {"code":"COMMON-001","message":"요청 값이 올바르지 않습니다."}
                        """, JsonCompareMode.STRICT);
    }

    @Test
    void 상한을_넘는_값이면_COMMON_001을_반환한다() {
        // given
        StudioPolicyCreateRequest request = new StudioPolicyCreateRequest(10081, 1440, 30);

        // when
        RestTestClient.ResponseSpec result = client.post()
                .uri("/api/studios/1/policy")
                .header("X-API-Version", "1")
                .header("X-Member-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange();

        // then
        result.expectStatus().isBadRequest()
                .expectBody()
                .json("""
                        {"code":"COMMON-001","message":"요청 값이 올바르지 않습니다."}
                        """, JsonCompareMode.STRICT);
    }

    @Test
    void 이미_정책이_있으면_POLICY_002를_반환한다() {
        // given
        when(studioPolicyService.save(anyLong(), anyLong(), any(StudioPolicyCreateRequest.class)))
                .thenThrow(new StudioException(StudioErrorCode.POLICY_ALREADY_EXISTS));

        // when
        RestTestClient.ResponseSpec result = client.post()
                .uri("/api/studios/1/policy")
                .header("X-API-Version", "1")
                .header("X-Member-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .body(StudioPolicyFixture.기본_정책_생성_요청())
                .exchange();

        // then
        result.expectStatus().isEqualTo(409)
                .expectBody()
                .json("""
                        {"code":"POLICY-002","message":"이미 운영 정책이 등록된 시설입니다."}
                        """, JsonCompareMode.STRICT);
    }

    @Test
    void 운영_정책을_조회하면_200과_정책_정보를_반환한다() {
        // given
        when(studioPolicyService.findByStudioId(anyLong()))
                .thenReturn(new StudioPolicyResponse(1L, 60, 1440, 30));

        // when
        RestTestClient.ResponseSpec result = client.get()
                .uri("/api/studios/1/policy")
                .header("X-API-Version", "1")
                .exchange();

        // then
        result.expectStatus().isOk()
                .expectBody()
                .json("""
                        {"id":1,"reservationCloseMinutesBefore":60,
                         "freeCancelMinutesBefore":1440,"waitingOfferResponseMinutes":30}
                        """, JsonCompareMode.STRICT);
    }

    @Test
    void 정책이_없으면_POLICY_001을_반환한다() {
        // given
        when(studioPolicyService.findByStudioId(anyLong()))
                .thenThrow(new StudioException(StudioErrorCode.POLICY_NOT_FOUND));

        // when
        RestTestClient.ResponseSpec result = client.get()
                .uri("/api/studios/1/policy")
                .header("X-API-Version", "1")
                .exchange();

        // then
        result.expectStatus().isNotFound()
                .expectBody()
                .json("""
                        {"code":"POLICY-001","message":"운영 정책을 찾을 수 없습니다."}
                        """, JsonCompareMode.STRICT);
    }

    @Test
    void 운영_정책을_수정하면_200과_정책_정보를_반환한다() {
        // given
        when(studioPolicyService.update(anyLong(), anyLong(), any(StudioPolicyUpdateRequest.class)))
                .thenReturn(new StudioPolicyResponse(1L, 60, 180, 30));

        // when
        RestTestClient.ResponseSpec result = client.patch()
                .uri("/api/studios/1/policy")
                .header("X-API-Version", "1")
                .header("X-Member-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .body(StudioPolicyFixture.무료_취소_시간만_바꾸는_수정_요청(180))
                .exchange();

        // then
        result.expectStatus().isOk()
                .expectBody()
                .json("""
                        {"id":1,"reservationCloseMinutesBefore":60,
                         "freeCancelMinutesBefore":180,"waitingOfferResponseMinutes":30}
                        """, JsonCompareMode.STRICT);
    }

    @Test
    void 권한이_없으면_PERMISSION_001을_반환한다() {
        // given
        when(studioPolicyService.update(anyLong(), anyLong(), any(StudioPolicyUpdateRequest.class)))
                .thenThrow(new StudioException(StudioErrorCode.PERMISSION_DENIED));

        // when
        RestTestClient.ResponseSpec result = client.patch()
                .uri("/api/studios/1/policy")
                .header("X-API-Version", "1")
                .header("X-Member-Id", "2")
                .contentType(MediaType.APPLICATION_JSON)
                .body(StudioPolicyFixture.무료_취소_시간만_바꾸는_수정_요청(180))
                .exchange();

        // then
        result.expectStatus().isForbidden()
                .expectBody()
                .json("""
                        {"code":"PERMISSION-001","message":"이 작업을 수행할 권한이 없습니다."}
                        """, JsonCompareMode.STRICT);
    }

    @Test
    void 버전_헤더가_없으면_API_001을_반환한다() {
        // given / when
        RestTestClient.ResponseSpec result = client.get()
                .uri("/api/studios/1/policy")
                .exchange();

        // then
        result.expectStatus().isBadRequest()
                .expectBody()
                .json("""
                        {"code":"API-001","message":"X-API-Version 헤더는 필수입니다."}
                        """, JsonCompareMode.STRICT);
    }
}
