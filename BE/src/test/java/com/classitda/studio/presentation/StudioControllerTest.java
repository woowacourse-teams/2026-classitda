package com.classitda.studio.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import com.classitda.common.config.ApiVersionConfig;
import com.classitda.common.exception.GlobalExceptionHandler;
import com.classitda.studio.application.StudioService;
import com.classitda.studio.exception.StudioErrorCode;
import com.classitda.studio.exception.StudioException;
import com.classitda.studio.fixture.StudioFixture;
import com.classitda.studio.presentation.dto.StudioCreateRequest;
import com.classitda.studio.presentation.dto.StudioResponse;
import com.classitda.studio.presentation.dto.StudioUpdateRequest;
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
@WebMvcTest(StudioController.class)
class StudioControllerTest {

    private final RestTestClient client;

    @MockitoBean
    private StudioService studioService;

    @Autowired
    StudioControllerTest(RestTestClient client) {
        this.client = client;
    }

    @Test
    void 시설을_생성하면_201과_시설_정보를_반환한다() {
        // given
        StudioCreateRequest request = StudioFixture.기본_시설_생성_요청();
        StudioResponse response = StudioResponse.from(StudioFixture.기본_시설(StudioFixture.기본_소유자()));
        when(studioService.save(anyLong(), any(StudioCreateRequest.class))).thenReturn(response);

        // when
        RestTestClient.ResponseSpec result = client.post()
                .uri("/api/studios")
                .header("X-API-Version", "1")
                .header("X-Member-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange();

        // then
        result.expectStatus().isCreated()
                .expectBody()
                .json("""
                        {"name":"클래스잇다 스튜디오","address":"서울시 강남구 테헤란로 1","openTime":"09:00:00","closeTime":"22:00:00"}
                        """, JsonCompareMode.LENIENT);
    }

    @Test
    void 시설명이_비어_있으면_COMMON_001을_반환한다() {
        // given
        StudioCreateRequest request = new StudioCreateRequest(
                " ",
                "서울시 강남구 테헤란로 1",
                "0212345678",
                StudioFixture.기본_시설_생성_요청().openTime(),
                StudioFixture.기본_시설_생성_요청().closeTime(),
                null,
                null
        );

        // when
        RestTestClient.ResponseSpec result = client.post()
                .uri("/api/studios")
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
    void 버전_헤더가_없으면_API_001을_반환한다() {
        // given
        StudioCreateRequest request = StudioFixture.기본_시설_생성_요청();

        // when
        RestTestClient.ResponseSpec result = client.post()
                .uri("/api/studios")
                .header("X-Member-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange();

        // then
        result.expectStatus().isBadRequest()
                .expectBody()
                .json("""
                        {"code":"API-001","message":"X-API-Version 헤더는 필수입니다."}
                        """, JsonCompareMode.STRICT);
    }
    @Test
    void 시설을_조회하면_200과_시설_정보를_반환한다() {
        // given
        StudioResponse response = StudioResponse.from(StudioFixture.기본_시설(StudioFixture.기본_소유자()));
        when(studioService.findById(anyLong())).thenReturn(response);

        // when
        RestTestClient.ResponseSpec result = client.get()
                .uri("/api/studios/1")
                .header("X-API-Version", "1")
                .exchange();

        // then
        result.expectStatus().isOk()
                .expectBody()
                .json("""
                        {"name":"클래스잇다 스튜디오"}
                        """, JsonCompareMode.LENIENT);
    }

    @Test
    void 없는_시설을_조회하면_STUDIO_002를_반환한다() {
        // given
        when(studioService.findById(anyLong()))
                .thenThrow(new StudioException(StudioErrorCode.NOT_FOUND));

        // when
        RestTestClient.ResponseSpec result = client.get()
                .uri("/api/studios/999")
                .header("X-API-Version", "1")
                .exchange();

        // then
        result.expectStatus().isNotFound()
                .expectBody()
                .json("""
                        {"code":"STUDIO-002","message":"시설을 찾을 수 없습니다."}
                        """, JsonCompareMode.STRICT);
    }

    @Test
    void 대표_강사가_아니면_STUDIO_003을_반환한다() {
        // given
        when(studioService.update(anyLong(), anyLong(), any(StudioUpdateRequest.class)))
                .thenThrow(new StudioException(StudioErrorCode.NOT_OWNER));

        // when
        RestTestClient.ResponseSpec result = client.patch()
                .uri("/api/studios/1")
                .header("X-API-Version", "1")
                .header("X-Member-Id", "2")
                .contentType(MediaType.APPLICATION_JSON)
                .body(StudioFixture.이름만_바꾸는_수정_요청("남의 스튜디오"))
                .exchange();

        // then
        result.expectStatus().isForbidden()
                .expectBody()
                .json("""
                        {"code":"STUDIO-003","message":"해당 시설의 대표 강사가 아닙니다."}
                        """, JsonCompareMode.STRICT);
    }
}
