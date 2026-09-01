package com.pheeeew.sigh.presentation;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.pheeeew.common.exception.GlobalExceptionHandler;
import com.pheeeew.sigh.application.SighSaveResult;
import com.pheeeew.sigh.application.SighService;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.client.RestTestClient;

@AutoConfigureRestTestClient
@Import(GlobalExceptionHandler.class)
@WebMvcTest(SighV2Controller.class)
class SighV2ControllerTest {

    private static final MediaType GEO_JSON = MediaType.parseMediaType("application/geo+json");
    private static final UUID REQUEST_ID = UUID.fromString("5d1ad34e-1e20-4f20-a20e-3825a095fe6b");
    private static final Instant CREATED_AT = Instant.parse("2026-09-01T12:00:00Z");

    private final RestTestClient client;

    @MockitoBean
    private SighService sighService;

    @Autowired
    SighV2ControllerTest(RestTestClient client) {
        this.client = client;
    }

    @Test
    void 메모가_있는_한숨을_최초_등록하면_201과_메모와_닉네임을_반환한다() {
        // given
        when(sighService.save(REQUEST_ID, 126.9780, 37.5664, "  오늘은 조금 지쳤다  "))
                .thenReturn(기본_저장_결과("오늘은 조금 지쳤다", true));

        // when
        RestTestClient.ResponseSpec result = 한숨을_등록한다("""
                {
                  "requestId": "5d1ad34e-1e20-4f20-a20e-3825a095fe6b",
                  "latitude": 37.5664,
                  "longitude": 126.9780,
                  "memo": "  오늘은 조금 지쳤다  "
                }
                """);

        // then
        result.expectStatus().isCreated()
                .expectHeader().contentType(GEO_JSON)
                .expectHeader().doesNotExist(HttpHeaders.LOCATION)
                .expectBody()
                .json(기본_GeoJSON("\"오늘은 조금 지쳤다\""), JsonCompareMode.STRICT);
        verify(sighService).save(REQUEST_ID, 126.9780, 37.5664, "  오늘은 조금 지쳤다  ");
    }

    @Test
    void 메모를_생략하면_null로_등록하고_반환한다() {
        // given
        when(sighService.save(REQUEST_ID, 126.9780, 37.5664, null))
                .thenReturn(기본_저장_결과(null, true));

        // when
        RestTestClient.ResponseSpec result = 한숨을_등록한다("""
                {
                  "requestId": "5d1ad34e-1e20-4f20-a20e-3825a095fe6b",
                  "latitude": 37.5664,
                  "longitude": 126.9780
                }
                """);

        // then
        result.expectStatus().isCreated()
                .expectHeader().contentType(GEO_JSON)
                .expectBody()
                .json(기본_GeoJSON("null"), JsonCompareMode.STRICT);
        verify(sighService).save(REQUEST_ID, 126.9780, 37.5664, null);
    }

    @Test
    void 공백으로만_이루어진_메모는_null로_등록한다() {
        // given
        when(sighService.save(REQUEST_ID, 126.9780, 37.5664, "   "))
                .thenReturn(기본_저장_결과(null, true));

        // when
        RestTestClient.ResponseSpec result = 한숨을_등록한다("""
                {
                  "requestId": "5d1ad34e-1e20-4f20-a20e-3825a095fe6b",
                  "latitude": 37.5664,
                  "longitude": 126.9780,
                  "memo": "   "
                }
                """);

        // then
        result.expectStatus().isCreated();
        verify(sighService).save(REQUEST_ID, 126.9780, 37.5664, "   ");
    }

    @Test
    void 같은_requestId로_재시도하면_200과_최초_메모와_닉네임을_반환한다() {
        // given
        when(sighService.save(REQUEST_ID, 129.0756, 35.1796, "재시도 메모"))
                .thenReturn(기본_저장_결과("최초 메모", false));

        // when
        RestTestClient.ResponseSpec result = 한숨을_등록한다("""
                {
                  "requestId": "5d1ad34e-1e20-4f20-a20e-3825a095fe6b",
                  "latitude": 35.1796,
                  "longitude": 129.0756,
                  "memo": "재시도 메모"
                }
                """);

        // then
        result.expectStatus().isOk()
                .expectHeader().contentType(GEO_JSON)
                .expectBody()
                .json(기본_GeoJSON("\"최초 메모\""), JsonCompareMode.STRICT);
        verify(sighService).save(REQUEST_ID, 129.0756, 35.1796, "재시도 메모");
    }

    @Test
    void 앞뒤_공백을_제외한_메모가_200자이면_등록한다() {
        // given
        String memo = "가".repeat(200);
        String requestedMemo = "  " + memo + "  ";
        when(sighService.save(REQUEST_ID, 126.9780, 37.5664, requestedMemo))
                .thenReturn(기본_저장_결과(memo, true));

        // when
        RestTestClient.ResponseSpec result = 한숨을_등록한다("""
                {
                  "requestId": "5d1ad34e-1e20-4f20-a20e-3825a095fe6b",
                  "latitude": 37.5664,
                  "longitude": 126.9780,
                  "memo": "%s"
                }
                """.formatted(requestedMemo));

        // then
        result.expectStatus().isCreated();
        verify(sighService).save(REQUEST_ID, 126.9780, 37.5664, requestedMemo);
    }

    @Test
    void 정규화된_메모가_200자를_초과하면_400을_반환한다() {
        // given
        String overlongMemo = "가".repeat(201);

        // when
        RestTestClient.ResponseSpec result = 한숨을_등록한다("""
                {
                  "requestId": "5d1ad34e-1e20-4f20-a20e-3825a095fe6b",
                  "latitude": 37.5664,
                  "longitude": 126.9780,
                  "memo": "%s"
                }
                """.formatted(overlongMemo));

        // then
        result.expectStatus().isBadRequest()
                .expectBody()
                .json("""
                        {"code":"COMMON-001","message":"요청 값이 올바르지 않습니다."}
                        """, JsonCompareMode.STRICT);
        verifyNoInteractions(sighService);
    }

    private RestTestClient.ResponseSpec 한숨을_등록한다(String body) {
        return client.post()
                .uri("/api/v2/sighs")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .exchange();
    }

    private SighSaveResult 기본_저장_결과(String memo, boolean created) {
        return new SighSaveResult(
                42L,
                126.9774,
                37.5669,
                CREATED_AT,
                memo,
                "날아가는 고라니",
                created
        );
    }

    private String 기본_GeoJSON(String memo) {
        return """
                {
                  "type": "Feature",
                  "id": 42,
                  "geometry": {
                    "type": "Point",
                    "coordinates": [126.9774, 37.5669]
                  },
                  "properties": {
                    "createdAt": "2026-09-01T12:00:00Z",
                    "memo": %s,
                    "nickname": "날아가는 고라니"
                  }
                }
                """.formatted(memo);
    }
}
