package com.classitda.studio.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.classitda.authentication.presentation.resolver.CurrentMemberIdArgumentResolver;
import com.classitda.common.config.ApiVersionConfig;
import com.classitda.common.exception.GlobalExceptionHandler;
import com.classitda.studio.application.StudioPolicyService;
import com.classitda.studio.exception.StudioErrorCode;
import com.classitda.studio.exception.StudioException;
import com.classitda.studio.fixture.StudioPolicyFixture;
import com.classitda.studio.presentation.dto.StudioPolicyResponse;
import com.classitda.studio.presentation.dto.StudioPolicyUpdateRequest;
import org.junit.jupiter.api.BeforeEach;
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

    @MockitoBean
    private CurrentMemberIdArgumentResolver currentMemberIdArgumentResolver;

    @Autowired
    StudioPolicyControllerTest(RestTestClient client) {
        this.client = client;
    }

    @BeforeEach
    void 인증된_회원_아이디를_주입한다() throws Exception {
        when(currentMemberIdArgumentResolver.supportsParameter(any())).thenReturn(true);
        when(currentMemberIdArgumentResolver.resolveArgument(any(), any(), any(), any())).thenReturn(1L);
    }

    @Test
    void 운영_정책을_조회하면_200과_정책_정보를_반환한다() {
        // given
        when(studioPolicyService.findByStudioId(anyLong()))
                .thenReturn(new StudioPolicyResponse(1L, 60, 1440, 30, 0));

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
                         "freeCancelMinutesBefore":1440,"waitingOfferResponseMinutes":30,
                         "maxHoldDays":0}
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
    void 운영_정책을_수정하면_204와_빈_본문을_반환하고_서비스에_위임한다() {
        // given
        when(studioPolicyService.update(anyLong(), anyLong(), any(StudioPolicyUpdateRequest.class)))
                .thenReturn(new StudioPolicyResponse(1L, 60, 180, 30, 0));

        // when
        RestTestClient.ResponseSpec result = client.patch()
                .uri("/api/studios/1/policy")
                .header("X-API-Version", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .body(StudioPolicyFixture.무료_취소_시간만_바꾸는_수정_요청(180))
                .exchange();

        // then
        result.expectStatus().isNoContent()
                .expectBody().isEmpty();
        verify(studioPolicyService).update(anyLong(), anyLong(), any(StudioPolicyUpdateRequest.class));
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


}
