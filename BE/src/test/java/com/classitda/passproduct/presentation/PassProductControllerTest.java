package com.classitda.passproduct.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.classitda.authentication.presentation.resolver.CurrentMemberIdArgumentResolver;
import com.classitda.classes.domain.ClassForm;
import com.classitda.classes.presentation.dto.ClassTypeResponse;
import com.classitda.common.config.ApiVersionConfig;
import com.classitda.common.exception.GlobalExceptionHandler;
import com.classitda.passproduct.application.PassProductService;
import com.classitda.passproduct.domain.PassProductPeriodUnit;
import com.classitda.passproduct.exception.PassProductErrorCode;
import com.classitda.passproduct.exception.PassProductException;
import com.classitda.passproduct.fixture.PassProductFixture;
import com.classitda.passproduct.presentation.dto.PassProductCreateRequest;
import com.classitda.passproduct.presentation.dto.PassProductResponse;
import com.classitda.passproduct.presentation.dto.PassProductUpdateRequest;
import com.classitda.studio.exception.StudioErrorCode;
import com.classitda.studio.exception.StudioException;
import java.util.List;
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
@WebMvcTest(PassProductController.class)
class PassProductControllerTest {

    private final RestTestClient client;

    @MockitoBean
    private PassProductService passProductService;

    @MockitoBean
    private CurrentMemberIdArgumentResolver currentMemberIdArgumentResolver;

    @Autowired
    PassProductControllerTest(RestTestClient client) {
        this.client = client;
    }

    @BeforeEach
    void 인증된_회원_아이디를_주입한다() throws Exception {
        when(currentMemberIdArgumentResolver.supportsParameter(any())).thenReturn(true);
        when(currentMemberIdArgumentResolver.resolveArgument(any(), any(), any(), any())).thenReturn(1L);
    }

    @Test
    void 수강권을_등록하면_201과_정확한_수강권_정보를_반환한다() {
        // given
        PassProductCreateRequest request = PassProductFixture.기본_수강권_생성_요청();
        when(passProductService.save(anyLong(), anyLong(), any(PassProductCreateRequest.class)))
                .thenReturn(기본_수강권_응답());

        // when
        RestTestClient.ResponseSpec result = 수강권을_등록한다(7L, "1", request);

        // then
        result.expectStatus().isCreated()
                .expectBody()
                .json("""
                        {
                          "id": 1,
                          "name": "3개월 그룹 20회권",
                          "classForm": "GROUP",
                          "classTypes": [{"id": 3, "name": "요가"}],
                          "totalCount": 20,
                          "validPeriodAmount": 3,
                          "validPeriodUnit": "MONTH",
                          "totalHoldDays": 7,
                          "active": true
                        }
                        """, JsonCompareMode.STRICT);
        verify(passProductService).save(eq(1L), eq(7L), eq(request));
    }

    @Test
    void 무제한_수강권은_횟수와_유효기간을_null로_반환한다() {
        // given
        when(passProductService.save(anyLong(), anyLong(), any(PassProductCreateRequest.class)))
                .thenReturn(new PassProductResponse(
                        2L, "기한 없는 20회권", ClassForm.INDIVIDUAL, List.of(), 20, null, null, 0, true));

        // when
        RestTestClient.ResponseSpec result = 수강권을_등록한다(7L, "1", PassProductFixture.기본_수강권_생성_요청());

        // then
        result.expectStatus().isCreated()
                .expectBody()
                .json("""
                        {
                          "id": 2,
                          "name": "기한 없는 20회권",
                          "classForm": "INDIVIDUAL",
                          "classTypes": [],
                          "totalCount": 20,
                          "validPeriodAmount": null,
                          "validPeriodUnit": null,
                          "totalHoldDays": 0,
                          "active": true
                        }
                        """, JsonCompareMode.STRICT);
    }

    @Test
    void 수강권_이름이_비어_있으면_COMMON_001을_반환한다() {
        // given
        PassProductCreateRequest request = PassProductFixture.수강권_생성_요청(
                " ", ClassForm.GROUP, null, 20, 3, PassProductPeriodUnit.MONTH, 0);

        // when
        RestTestClient.ResponseSpec result = 수강권을_등록한다(1L, "1", request);

        // then
        오류를_검증한다(result, 400, "COMMON-001", "요청 값이 올바르지 않습니다.");
    }

    @Test
    void 수업_형태가_없으면_COMMON_001을_반환한다() {
        // given
        PassProductCreateRequest request = PassProductFixture.수강권_생성_요청(
                "이름", null, null, 20, 3, PassProductPeriodUnit.MONTH, 0);

        // when
        RestTestClient.ResponseSpec result = 수강권을_등록한다(1L, "1", request);

        // then
        오류를_검증한다(result, 400, "COMMON-001", "요청 값이 올바르지 않습니다.");
    }

    @Test
    void 홀딩_일수가_음수면_COMMON_001을_반환한다() {
        // given
        PassProductCreateRequest request = PassProductFixture.수강권_생성_요청(
                "이름", ClassForm.GROUP, null, 20, 3, PassProductPeriodUnit.MONTH, -1);

        // when
        RestTestClient.ResponseSpec result = 수강권을_등록한다(1L, "1", request);

        // then
        오류를_검증한다(result, 400, "COMMON-001", "요청 값이 올바르지 않습니다.");
    }

    @Test
    void 수업_종류를_지정하지_않으면_COMMON_001을_반환한다() {
        // given
        PassProductCreateRequest request = PassProductFixture.수업_종류를_지정한_수강권_생성_요청(List.of());

        // when
        RestTestClient.ResponseSpec result = 수강권을_등록한다(1L, "1", request);

        // then
        오류를_검증한다(result, 400, "COMMON-001", "요청 값이 올바르지 않습니다.");
    }

    @Test
    void 종료_조건이_없으면_PASS_PRODUCT_005를_반환한다() {
        // given
        when(passProductService.save(anyLong(), anyLong(), any(PassProductCreateRequest.class)))
                .thenThrow(new PassProductException(PassProductErrorCode.NO_EXPIRATION_CONDITION));

        // when
        RestTestClient.ResponseSpec result = 수강권을_등록한다(1L, "1", PassProductFixture.기본_수강권_생성_요청());

        // then
        오류를_검증한다(result, 400, "PASS_PRODUCT-005",
                "유효 기간과 수강 가능 횟수를 모두 무제한으로 지정할 수 없습니다.");
    }

    @Test
    void 등록_버전_헤더가_없으면_API_001을_반환한다() {
        // given / when
        RestTestClient.ResponseSpec result = client.post()
                .uri("/api/studios/1/pass-products")
                .contentType(MediaType.APPLICATION_JSON)
                .body(PassProductFixture.기본_수강권_생성_요청())
                .exchange();

        // then
        오류를_검증한다(result, 400, "API-001", "X-API-Version 헤더는 필수입니다.");
    }

    @Test
    void 등록_지원하지_않는_버전이면_API_002를_반환한다() {
        // given / when
        RestTestClient.ResponseSpec result = 수강권을_등록한다(1L, "2", PassProductFixture.기본_수강권_생성_요청());

        // then
        오류를_검증한다(result, 400, "API-002", "지원하지 않는 API 버전입니다.");
    }

    @Test
    void 수강권_목록을_조회하면_200과_최상위_배열을_반환한다() {
        // given
        when(passProductService.findAll(1L, 7L)).thenReturn(List.of(기본_수강권_응답()));

        // when
        RestTestClient.ResponseSpec result = 수강권_목록을_조회한다(7L, "1");

        // then
        result.expectStatus().isOk()
                .expectBody()
                .json("""
                        [{
                          "id": 1,
                          "name": "3개월 그룹 20회권",
                          "classForm": "GROUP",
                          "classTypes": [{"id": 3, "name": "요가"}],
                          "totalCount": 20,
                          "validPeriodAmount": 3,
                          "validPeriodUnit": "MONTH",
                          "totalHoldDays": 7,
                          "active": true
                        }]
                        """, JsonCompareMode.STRICT);
        verify(passProductService).findAll(1L, 7L);
        verify(currentMemberIdArgumentResolver).resolveArgument(any(), any(), any(), any());
    }

    @Test
    void 없는_시설의_수강권_목록을_조회하면_STUDIO_002를_반환한다() {
        // given
        when(passProductService.findAll(1L, 999L)).thenThrow(new StudioException(StudioErrorCode.NOT_FOUND));

        // when
        RestTestClient.ResponseSpec result = 수강권_목록을_조회한다(999L, "1");

        // then
        오류를_검증한다(result, 404, "STUDIO-002", "시설을 찾을 수 없습니다.");
    }

    @Test
    void 목록_조회_권한_예외들은_문서화된_403_응답으로_직렬화한다() {
        // given
        when(passProductService.findAll(1L, 7L)).thenThrow(
                new StudioException(StudioErrorCode.NOT_MEMBERSHIP),
                new StudioException(StudioErrorCode.MEMBERSHIP_INACTIVE),
                new StudioException(StudioErrorCode.PERMISSION_DENIED)
        );
        String[] codes = {"MEMBERSHIP-001", "MEMBERSHIP-002", "PERMISSION-001"};
        String[] messages = {
                "해당 시설의 소속이 아닙니다.",
                "이용이 정지된 소속입니다.",
                "이 작업을 수행할 권한이 없습니다."
        };

        // when / then
        for (int index = 0; index < codes.length; index++) {
            오류를_검증한다(수강권_목록을_조회한다(7L, "1"), 403, codes[index], messages[index]);
        }
        verify(passProductService, times(3)).findAll(1L, 7L);
    }

    @Test
    void 수강권을_수정하면_200과_수정된_정보를_반환한다() {
        // given
        PassProductUpdateRequest request = PassProductFixture.기본_수강권_수정_요청();
        when(passProductService.update(anyLong(), anyLong(), anyLong(), any(PassProductUpdateRequest.class)))
                .thenReturn(new PassProductResponse(
                        1L, "6개월 그룹 30회권", ClassForm.GROUP, List.of(), 30, 6, PassProductPeriodUnit.MONTH, 14, false));

        // when
        RestTestClient.ResponseSpec result = 수강권을_수정한다(7L, 1L, "1", request);

        // then
        result.expectStatus().isOk()
                .expectBody()
                .json("""
                        {
                          "id": 1,
                          "name": "6개월 그룹 30회권",
                          "classForm": "GROUP",
                          "classTypes": [],
                          "totalCount": 30,
                          "validPeriodAmount": 6,
                          "validPeriodUnit": "MONTH",
                          "totalHoldDays": 14,
                          "active": false
                        }
                        """, JsonCompareMode.STRICT);
        verify(passProductService).update(eq(1L), eq(7L), eq(1L), eq(request));
    }

    @Test
    void 판매_여부가_없으면_COMMON_001을_반환한다() {
        // given
        PassProductUpdateRequest request = PassProductFixture.수강권_수정_요청(
                "이름", ClassForm.GROUP, null, 20, 3, PassProductPeriodUnit.MONTH, 0, null);

        // when
        RestTestClient.ResponseSpec result = 수강권을_수정한다(7L, 1L, "1", request);

        // then
        오류를_검증한다(result, 400, "COMMON-001", "요청 값이 올바르지 않습니다.");
    }

    @Test
    void 없는_수강권을_수정하면_PASS_PRODUCT_008을_반환한다() {
        // given
        when(passProductService.update(anyLong(), anyLong(), anyLong(), any(PassProductUpdateRequest.class)))
                .thenThrow(new PassProductException(PassProductErrorCode.PASS_PRODUCT_NOT_FOUND));

        // when
        RestTestClient.ResponseSpec result = 수강권을_수정한다(7L, 999L, "1", PassProductFixture.기본_수강권_수정_요청());

        // then
        오류를_검증한다(result, 404, "PASS_PRODUCT-008", "수강권을 찾을 수 없습니다.");
    }

    @Test
    void 수정_버전_헤더가_없으면_API_001을_반환한다() {
        // given / when
        RestTestClient.ResponseSpec result = client.put()
                .uri("/api/studios/1/pass-products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .body(PassProductFixture.기본_수강권_수정_요청())
                .exchange();

        // then
        오류를_검증한다(result, 400, "API-001", "X-API-Version 헤더는 필수입니다.");
    }

    private PassProductResponse 기본_수강권_응답() {
        return new PassProductResponse(
                1L,
                PassProductFixture.기본_이름,
                ClassForm.GROUP,
                List.of(ClassTypeResponse.of(3L, "요가")),
                PassProductFixture.기본_횟수,
                PassProductFixture.기본_유효기간,
                PassProductPeriodUnit.MONTH,
                PassProductFixture.기본_홀딩_일수,
                true
        );
    }

    private RestTestClient.ResponseSpec 수강권_목록을_조회한다(Long studioId, String apiVersion) {
        return client.get()
                .uri("/api/studios/{studioId}/pass-products", studioId)
                .header("X-API-Version", apiVersion)
                .exchange();
    }

    private RestTestClient.ResponseSpec 수강권을_등록한다(
            Long studioId,
            String apiVersion,
            PassProductCreateRequest request
    ) {
        return client.post()
                .uri("/api/studios/{studioId}/pass-products", studioId)
                .header("X-API-Version", apiVersion)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange();
    }

    private RestTestClient.ResponseSpec 수강권을_수정한다(
            Long studioId,
            Long passProductId,
            String apiVersion,
            PassProductUpdateRequest request
    ) {
        return client.put()
                .uri("/api/studios/{studioId}/pass-products/{passProductId}", studioId, passProductId)
                .header("X-API-Version", apiVersion)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange();
    }

    private void 오류를_검증한다(
            RestTestClient.ResponseSpec result,
            int status,
            String code,
            String message
    ) {
        result.expectStatus().isEqualTo(status)
                .expectBody()
                .json("""
                        {"code":"%s","message":"%s"}
                        """.formatted(code, message), JsonCompareMode.STRICT);
    }
}
