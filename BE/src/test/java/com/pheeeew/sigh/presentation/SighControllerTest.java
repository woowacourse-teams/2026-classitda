package com.pheeeew.sigh.presentation;

import static com.pheeeew.sigh.fixture.SighFixture.저장된_기본_한숨;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pheeeew.common.exception.GlobalExceptionHandler;
import com.pheeeew.sigh.application.SighSaveResult;
import com.pheeeew.sigh.application.SighService;
import com.pheeeew.sigh.domain.Sigh;
import com.pheeeew.sigh.exception.SighErrorCode;
import com.pheeeew.sigh.exception.SighException;
import com.pheeeew.sigh.presentation.dto.SighCreateRequest;
import java.time.Instant;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
@WebMvcTest(SighController.class)
class SighControllerTest {

    private static final MediaType GEO_JSON = MediaType.parseMediaType("application/geo+json");
    private static final UUID REQUEST_ID = UUID.fromString("5d1ad34e-1e20-4f20-a20e-3825a095fe6b");
    private static final Instant CREATED_AT = Instant.parse("2026-08-31T10:30:00Z");

    private final RestTestClient client;

    @MockitoBean
    private SighService sighService;

    @Autowired
    SighControllerTest(RestTestClient client) {
        this.client = client;
    }

    @Test
    void 한숨을_최초_등록하면_201과_GeoJSON_Feature를_반환한다() {
        // given
        SighCreateRequest request = 기본_요청();
        Sigh sigh = 기본_한숨();
        when(sighService.save(REQUEST_ID, 126.9780, 37.5664))
                .thenReturn(SighSaveResult.of(sigh, true));

        // when
        RestTestClient.ResponseSpec result = 한숨을_등록한다(request);

        // then
        result.expectStatus().isCreated()
                .expectHeader().contentType(GEO_JSON)
                .expectHeader().doesNotExist(HttpHeaders.LOCATION)
                .expectBody()
                .json(기본_GeoJSON(), JsonCompareMode.STRICT);
        verify(sighService).save(REQUEST_ID, 126.9780, 37.5664);
    }

    @Test
    void 같은_requestId로_재시도하면_200과_최초_GeoJSON_Feature를_반환한다() {
        // given
        SighCreateRequest request = SighCreateRequest.of(REQUEST_ID, 35.1796, 129.0756);
        Sigh sigh = 기본_한숨();
        when(sighService.save(REQUEST_ID, 129.0756, 35.1796))
                .thenReturn(SighSaveResult.of(sigh, false));

        // when
        RestTestClient.ResponseSpec result = 한숨을_등록한다(request);

        // then
        result.expectStatus().isOk()
                .expectHeader().contentType(GEO_JSON)
                .expectBody()
                .json(기본_GeoJSON(), JsonCompareMode.STRICT);
        verify(sighService).save(REQUEST_ID, 129.0756, 35.1796);
    }

    @ParameterizedTest
    @MethodSource("올바르지_않은_요청들")
    void 필수값이_없거나_좌표_범위를_벗어나면_400을_반환한다(SighCreateRequest request) {
        // given / when
        RestTestClient.ResponseSpec result = 한숨을_등록한다(request);

        // then
        오류를_검증한다(result, 400, "COMMON-001", "요청 값이 올바르지 않습니다.");
    }

    @Test
    void 요청_본문이_없으면_400을_반환한다() {
        // given / when
        RestTestClient.ResponseSpec result = client.post()
                .uri("/api/v1/sighs")
                .exchange();

        // then
        오류를_검증한다(result, 400, "COMMON-001", "요청 값이 올바르지 않습니다.");
    }

    @Test
    void requestId가_UUID_형식이_아니면_400을_반환한다() {
        // given / when
        RestTestClient.ResponseSpec result = client.post()
                .uri("/api/v1/sighs")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                          "requestId": "not-a-uuid",
                          "latitude": 37.5664,
                          "longitude": 126.9780
                        }
                        """)
                .exchange();

        // then
        오류를_검증한다(result, 400, "COMMON-001", "요청 값이 올바르지 않습니다.");
    }

    @Test
    void 한숨_도메인_예외는_정의된_상태와_코드로_반환한다() {
        // given
        when(sighService.save(REQUEST_ID, 126.9780, 37.5664))
                .thenThrow(new SighException(SighErrorCode.SIGH_SAVE_FAILED, new IllegalStateException()));

        // when
        RestTestClient.ResponseSpec result = 한숨을_등록한다(기본_요청());

        // then
        오류를_검증한다(result, 500, "SIGH-001", "한숨을 저장하지 못했습니다.");
    }

    @Test
    void 예상하지_못한_예외는_내부_메시지를_노출하지_않는다() {
        // given
        when(sighService.save(REQUEST_ID, 126.9780, 37.5664))
                .thenThrow(new IllegalStateException("외부에 노출되면 안 되는 메시지"));

        // when
        RestTestClient.ResponseSpec result = 한숨을_등록한다(기본_요청());

        // then
        오류를_검증한다(result, 500, "COMMON-002", "서버 내부 오류가 발생했습니다.");
    }

    private SighCreateRequest 기본_요청() {
        return SighCreateRequest.of(REQUEST_ID, 37.5664, 126.9780);
    }

    private Sigh 기본_한숨() {
        return 저장된_기본_한숨(42L, CREATED_AT);
    }

    private String 기본_GeoJSON() {
        return """
                {
                  "type": "Feature",
                  "id": 42,
                  "geometry": {
                    "type": "Point",
                    "coordinates": [126.9774, 37.5669]
                  },
                  "properties": {
                    "createdAt": "2026-08-31T10:30:00Z"
                  }
                }
                """;
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

    private RestTestClient.ResponseSpec 한숨을_등록한다(SighCreateRequest request) {
        return client.post()
                .uri("/api/v1/sighs")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange();
    }

    private static Stream<Arguments> 올바르지_않은_요청들() {
        return Stream.of(
                Arguments.of(SighCreateRequest.of(null, 37.5664, 126.9780)),
                Arguments.of(SighCreateRequest.of(REQUEST_ID, null, 126.9780)),
                Arguments.of(SighCreateRequest.of(REQUEST_ID, 90.0001, 126.9780)),
                Arguments.of(SighCreateRequest.of(REQUEST_ID, 37.5664, -180.0001))
        );
    }
}
