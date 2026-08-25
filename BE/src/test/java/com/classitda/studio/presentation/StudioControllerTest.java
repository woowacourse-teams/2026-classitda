package com.classitda.studio.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.classitda.authentication.presentation.resolver.CurrentMemberIdArgumentResolver;
import com.classitda.common.config.ApiVersionConfig;
import com.classitda.common.exception.ClassitdaException;
import com.classitda.common.exception.CommonErrorCode;
import com.classitda.common.exception.GlobalExceptionHandler;
import com.classitda.common.image.ImageUploadUrl;
import com.classitda.common.image.ImageUploadUrlRequest;
import com.classitda.studio.application.StudioImageService;
import com.classitda.studio.application.StudioService;
import com.classitda.studio.exception.StudioErrorCode;
import com.classitda.studio.exception.StudioException;
import com.classitda.studio.fixture.StudioFixture;
import com.classitda.studio.presentation.dto.StudioCreateRequest;
import com.classitda.studio.presentation.dto.StudioResponse;
import com.classitda.studio.presentation.dto.StudioUpdateRequest;
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
@WebMvcTest(StudioController.class)
class StudioControllerTest {

    private final RestTestClient client;

    @MockitoBean
    private StudioService studioService;

    @MockitoBean
    private StudioImageService studioImageService;

    @MockitoBean
    private CurrentMemberIdArgumentResolver currentMemberIdArgumentResolver;

    @Autowired
    StudioControllerTest(RestTestClient client) {
        this.client = client;
    }

    @BeforeEach
    void 인증된_회원_아이디를_주입한다() throws Exception {
        when(currentMemberIdArgumentResolver.supportsParameter(any())).thenReturn(true);
        when(currentMemberIdArgumentResolver.resolveArgument(any(), any(), any(), any())).thenReturn(1L);
    }

    @Test
    void 시설을_생성하면_201과_빈_본문을_반환하고_서비스에_위임한다() {
        // given
        StudioResponse response = StudioResponse.of(StudioFixture.기본_시설(StudioFixture.기본_소유자()), null);
        when(studioService.save(anyLong(), any(StudioCreateRequest.class))).thenReturn(response);

        // when
        RestTestClient.ResponseSpec result = client.post()
                .uri("/api/studios")
                .header("X-API-Version", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                          "name": "클래스잇다 스튜디오",
                          "address": {
                            "zonecode": "06234",
                            "roadAddress": "서울 강남구 테헤란로 1",
                            "jibunAddress": "서울 강남구 역삼동 823",
                            "buildingName": "클래스잇다 빌딩",
                            "detailAddress": "3층 301호"
                          },
                          "phoneNumber": "0212345678",
                          "openTime": "09:00",
                          "closeTime": "22:00"
                        }
                        """)
                .exchange();

        // then
        result.expectStatus().isCreated()
                .expectBody().isEmpty();
        verify(studioService).save(anyLong(), any(StudioCreateRequest.class));
    }

    @Test
    void 시설을_수정하면_204와_빈_본문을_반환하고_서비스에_위임한다() {
        // given / when
        RestTestClient.ResponseSpec result = client.patch()
                .uri("/api/studios/1")
                .header("X-API-Version", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .body(StudioFixture.이름만_바꾸는_수정_요청("바뀐 스튜디오"))
                .exchange();

        // then
        result.expectStatus().isNoContent()
                .expectBody().isEmpty();
        verify(studioService).update(anyLong(), anyLong(), any(StudioUpdateRequest.class));
    }

    @Test
    void 운영시간에_초가_붙으면_COMMON_001을_반환한다() {
        // when
        RestTestClient.ResponseSpec result = client.post()
                .uri("/api/studios")
                .header("X-API-Version", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                          "name": "클래스잇다 스튜디오",
                          "address": {
                            "zonecode": "06234",
                            "roadAddress": "서울 강남구 테헤란로 1",
                            "jibunAddress": "서울 강남구 역삼동 823",
                            "buildingName": "클래스잇다 빌딩",
                            "detailAddress": "3층 301호"
                          },
                          "phoneNumber": "0212345678",
                          "openTime": "09:00:00",
                          "closeTime": "22:00"
                        }
                        """)
                .exchange();

        // then
        result.expectStatus().isBadRequest()
                .expectBody()
                .json("""
                        {"code":"COMMON-001","message":"요청 값이 올바르지 않습니다."}
                        """, JsonCompareMode.STRICT);
    }

    @Test
    void 시설명이_비어_있으면_COMMON_001을_반환한다() {
        // given
        StudioCreateRequest request = new StudioCreateRequest(
                " ",
                StudioFixture.기본_주소_요청(),
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
        StudioResponse response = StudioResponse.of(StudioFixture.기본_시설(StudioFixture.기본_소유자()), null);
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
    void 권한이_없으면_PERMISSION_001을_반환한다() {
        // given
        when(studioService.update(anyLong(), anyLong(), any(StudioUpdateRequest.class)))
                .thenThrow(new StudioException(StudioErrorCode.PERMISSION_DENIED));

        // when
        RestTestClient.ResponseSpec result = client.patch()
                .uri("/api/studios/1")
                .header("X-API-Version", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .body(StudioFixture.이름만_바꾸는_수정_요청("남의 스튜디오"))
                .exchange();

        // then
        result.expectStatus().isForbidden()
                .expectBody()
                .json("""
                        {"code":"PERMISSION-001","message":"이 작업을 수행할 권한이 없습니다."}
                        """, JsonCompareMode.STRICT);
    }
    @Test
    void 업로드_URL을_발급하면_200과_objectKey와_uploadUrl을_반환한다() {
        // given
        when(studioImageService.issueUploadUrl(any(ImageUploadUrlRequest.class)))
                .thenReturn(ImageUploadUrl.of(
                        "studio-images/9f1c2b7e.jpg",
                        "https://images.test/upload?signature=abc",
                        "image/jpeg"
                ));

        // when
        RestTestClient.ResponseSpec result = client.post()
                .uri("/api/studios/image-upload-url")
                .header("X-API-Version", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .body(ImageUploadUrlRequest.of("jpg", 3_145_728L))
                .exchange();

        // then
        result.expectStatus().isOk()
                .expectBody()
                .json("""
                        {
                          "objectKey": "studio-images/9f1c2b7e.jpg",
                          "uploadUrl": "https://images.test/upload?signature=abc",
                          "contentType": "image/jpeg"
                        }
                        """, JsonCompareMode.STRICT);
    }

    @Test
    void 업로드_URL_요청에_확장자가_없으면_COMMON_001을_반환한다() {
        // when
        RestTestClient.ResponseSpec result = client.post()
                .uri("/api/studios/image-upload-url")
                .header("X-API-Version", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .body(ImageUploadUrlRequest.of(" ", 3_145_728L))
                .exchange();

        // then
        result.expectStatus().isBadRequest()
                .expectBody()
                .json("""
                        {"code":"COMMON-001","message":"요청 값이 올바르지 않습니다."}
                        """, JsonCompareMode.STRICT);
    }

    @Test
    void 업로드_URL_요청에_크기가_없으면_COMMON_001을_반환한다() {
        // when
        RestTestClient.ResponseSpec result = client.post()
                .uri("/api/studios/image-upload-url")
                .header("X-API-Version", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .body(ImageUploadUrlRequest.of("jpg", null))
                .exchange();

        // then
        result.expectStatus().isBadRequest()
                .expectBody()
                .json("""
                        {"code":"COMMON-001","message":"요청 값이 올바르지 않습니다."}
                        """, JsonCompareMode.STRICT);
    }

    @Test
    void 지원하지_않는_확장자면_IMAGE_001을_반환한다() {
        // given
        when(studioImageService.issueUploadUrl(any(ImageUploadUrlRequest.class)))
                .thenThrow(new ClassitdaException(CommonErrorCode.INVALID_IMAGE_EXTENSION));

        // when
        RestTestClient.ResponseSpec result = client.post()
                .uri("/api/studios/image-upload-url")
                .header("X-API-Version", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .body(ImageUploadUrlRequest.of("gif", 3_145_728L))
                .exchange();

        // then
        result.expectStatus().isBadRequest()
                .expectBody()
                .json("""
                        {"code":"IMAGE-001","message":"지원하지 않는 이미지 형식입니다."}
                        """, JsonCompareMode.STRICT);
    }
    @Test
    void 대표_이미지를_삭제하면_204를_반환하고_서비스에_위임한다() {
        // when
        RestTestClient.ResponseSpec result = client.delete()
                .uri("/api/studios/1/image")
                .header("X-API-Version", "1")
                .exchange();

        // then
        result.expectStatus().isNoContent();
        verify(studioService).deleteImage(1L, 1L);
    }

    @Test
    void 없는_시설의_대표_이미지를_삭제하면_STUDIO_002를_반환한다() {
        // given
        doThrow(new StudioException(StudioErrorCode.NOT_FOUND))
                .when(studioService).deleteImage(anyLong(), anyLong());

        // when
        RestTestClient.ResponseSpec result = client.delete()
                .uri("/api/studios/999/image")
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
    void 권한이_없으면_대표_이미지를_삭제할_수_없다() {
        // given
        doThrow(new StudioException(StudioErrorCode.PERMISSION_DENIED))
                .when(studioService).deleteImage(anyLong(), anyLong());

        // when
        RestTestClient.ResponseSpec result = client.delete()
                .uri("/api/studios/1/image")
                .header("X-API-Version", "1")
                .exchange();

        // then
        result.expectStatus().isForbidden()
                .expectBody()
                .json("""
                        {"code":"PERMISSION-001","message":"이 작업을 수행할 권한이 없습니다."}
                        """, JsonCompareMode.STRICT);
    }
}
