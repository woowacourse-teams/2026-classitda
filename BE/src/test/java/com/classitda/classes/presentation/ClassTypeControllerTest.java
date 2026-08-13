package com.classitda.classes.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.classitda.authentication.presentation.resolver.CurrentMemberIdArgumentResolver;
import com.classitda.classes.application.ClassTypeService;
import com.classitda.classes.exception.ClassErrorCode;
import com.classitda.classes.exception.ClassException;
import com.classitda.classes.fixture.ClassTypeFixture;
import com.classitda.classes.presentation.dto.ClassTypeCreateRequest;
import com.classitda.classes.presentation.dto.ClassTypeResponse;
import com.classitda.classes.presentation.dto.ClassTypeUpdateRequest;
import com.classitda.common.config.ApiVersionConfig;
import com.classitda.common.exception.GlobalExceptionHandler;
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
@WebMvcTest(ClassTypeController.class)
class ClassTypeControllerTest {

    private final RestTestClient client;

    @MockitoBean
    private ClassTypeService classTypeService;

    @MockitoBean
    private CurrentMemberIdArgumentResolver currentMemberIdArgumentResolver;

    @Autowired
    ClassTypeControllerTest(RestTestClient client) {
        this.client = client;
    }

    @BeforeEach
    void 인증된_회원_아이디를_주입한다() throws Exception {
        when(currentMemberIdArgumentResolver.supportsParameter(any())).thenReturn(true);
        when(currentMemberIdArgumentResolver.resolveArgument(any(), any(), any(), any())).thenReturn(1L);
    }

    @Test
    void 수업_종류를_등록하면_201과_빈_본문을_반환하고_서비스에_위임한다() {
        // given
        ClassTypeCreateRequest request = ClassTypeFixture.기본_수업_종류_생성_요청();

        // when
        RestTestClient.ResponseSpec result = 수업_종류를_등록한다(7L, "1", request);

        // then
        result.expectStatus().isCreated()
                .expectBody().isEmpty();
        verify(classTypeService).save(eq(1L), eq(7L), eq(request));
    }

    @Test
    void 수업_종류_이름이_비어_있으면_COMMON_001을_반환한다() {
        // given
        ClassTypeCreateRequest request = ClassTypeFixture.이름이_다른_수업_종류_생성_요청(" ");

        // when
        RestTestClient.ResponseSpec result = 수업_종류를_등록한다(1L, "1", request);

        // then
        오류를_검증한다(result, 400, "COMMON-001", "요청 값이 올바르지 않습니다.");
    }

    @Test
    void 수업_종류_이름이_51자면_COMMON_001을_반환한다() {
        // given
        ClassTypeCreateRequest request = ClassTypeFixture.이름이_다른_수업_종류_생성_요청("가".repeat(51));

        // when
        RestTestClient.ResponseSpec result = 수업_종류를_등록한다(1L, "1", request);

        // then
        오류를_검증한다(result, 400, "COMMON-001", "요청 값이 올바르지 않습니다.");
    }

    @Test
    void 이름이_중복되면_CLASS_TYPE_002를_반환한다() {
        // given
        doThrow(new ClassException(ClassErrorCode.CLASS_TYPE_NAME_DUPLICATED))
                .when(classTypeService).save(anyLong(), anyLong(), any(ClassTypeCreateRequest.class));

        // when
        RestTestClient.ResponseSpec result = 수업_종류를_등록한다(1L, "1", ClassTypeFixture.기본_수업_종류_생성_요청());

        // then
        오류를_검증한다(result, 409, "CLASS_TYPE-002", "이미 존재하는 수업 종류 이름입니다.");
    }

    @Test
    void 버전_헤더가_없으면_API_001을_반환한다() {
        // given / when
        RestTestClient.ResponseSpec result = client.post()
                .uri("/api/studios/1/class-types")
                .contentType(MediaType.APPLICATION_JSON)
                .body(ClassTypeFixture.기본_수업_종류_생성_요청())
                .exchange();

        // then
        오류를_검증한다(result, 400, "API-001", "X-API-Version 헤더는 필수입니다.");
    }

    @Test
    void 지원하지_않는_버전이면_API_002를_반환한다() {
        // given / when
        RestTestClient.ResponseSpec result = 수업_종류를_등록한다(1L, "2", ClassTypeFixture.기본_수업_종류_생성_요청());

        // then
        오류를_검증한다(result, 400, "API-002", "지원하지 않는 API 버전입니다.");
    }

    @Test
    void 수업_종류_목록을_조회하면_200과_정렬된_최상위_배열을_반환한다() {
        // given
        when(classTypeService.findAll(1L, 7L)).thenReturn(List.of(
                ClassTypeResponse.of(1L, "일반 요가"),
                ClassTypeResponse.of(4L, "리포머 요가")
        ));

        // when
        RestTestClient.ResponseSpec result = 수업_종류_목록을_조회한다(7L, "1");

        // then
        result.expectStatus().isOk()
                .expectBody()
                .json("""
                        [{"id":1,"name":"일반 요가"},{"id":4,"name":"리포머 요가"}]
                        """, JsonCompareMode.STRICT);
        verify(classTypeService).findAll(1L, 7L);
        verify(currentMemberIdArgumentResolver).resolveArgument(any(), any(), any(), any());
    }

    @Test
    void 없는_시설의_수업_종류_목록을_조회하면_STUDIO_002를_반환한다() {
        // given
        when(classTypeService.findAll(1L, 999L)).thenThrow(new StudioException(StudioErrorCode.NOT_FOUND));

        // when
        RestTestClient.ResponseSpec result = 수업_종류_목록을_조회한다(999L, "1");

        // then
        오류를_검증한다(result, 404, "STUDIO-002", "시설을 찾을 수 없습니다.");
    }

    @Test
    void 목록_조회_권한_예외들은_문서화된_403_응답으로_직렬화한다() {
        // given
        when(classTypeService.findAll(1L, 7L)).thenThrow(
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
            오류를_검증한다(수업_종류_목록을_조회한다(7L, "1"), 403, codes[index], messages[index]);
        }
        verify(classTypeService, times(3)).findAll(1L, 7L);
    }

    @Test
    void 수업_종류_목록_조회_버전_헤더가_없으면_API_001을_반환한다() {
        // given / when
        RestTestClient.ResponseSpec result = client.get()
                .uri("/api/studios/1/class-types")
                .exchange();

        // then
        오류를_검증한다(result, 400, "API-001", "X-API-Version 헤더는 필수입니다.");
    }

    @Test
    void 수업_종류_목록_조회_버전이_지원되지_않으면_API_002를_반환한다() {
        // given / when
        RestTestClient.ResponseSpec result = 수업_종류_목록을_조회한다(1L, "2");

        // then
        오류를_검증한다(result, 400, "API-002", "지원하지 않는 API 버전입니다.");
    }

    @Test
    void 수업_종류_이름을_수정하면_200과_정확한_수업_종류_정보를_반환하고_서비스에_위임한다() {
        // given
        ClassTypeUpdateRequest request = ClassTypeFixture.기본_수업_종류_수정_요청();
        when(classTypeService.update(anyLong(), anyLong(), anyLong(), any(ClassTypeUpdateRequest.class)))
                .thenReturn(ClassTypeResponse.of(13L, "리포머 요가"));

        // when
        RestTestClient.ResponseSpec result = 수업_종류_이름을_수정한다(7L, 13L, "1", request);

        // then
        result.expectStatus().isOk()
                .expectBody()
                .json("""
                        {"id":13,"name":"리포머 요가"}
                        """, JsonCompareMode.STRICT);
        verify(classTypeService).update(eq(1L), eq(7L), eq(13L), eq(request));
    }

    @Test
    void 수정할_수업_종류_이름이_비어_있으면_COMMON_001을_반환하고_서비스를_호출하지_않는다() {
        // given
        ClassTypeUpdateRequest request = ClassTypeFixture.이름이_다른_수업_종류_수정_요청(" ");

        // when
        RestTestClient.ResponseSpec result = 수업_종류_이름을_수정한다(7L, 13L, "1", request);

        // then
        오류를_검증한다(result, 400, "COMMON-001", "요청 값이 올바르지 않습니다.");
        verify(classTypeService, never()).update(anyLong(), anyLong(), anyLong(), any());
    }

    @Test
    void 수정할_수업_종류_이름이_51자면_COMMON_001을_반환하고_서비스를_호출하지_않는다() {
        // given
        ClassTypeUpdateRequest request = ClassTypeFixture.이름이_다른_수업_종류_수정_요청("가".repeat(51));

        // when
        RestTestClient.ResponseSpec result = 수업_종류_이름을_수정한다(7L, 13L, "1", request);

        // then
        오류를_검증한다(result, 400, "COMMON-001", "요청 값이 올바르지 않습니다.");
        verify(classTypeService, never()).update(anyLong(), anyLong(), anyLong(), any());
    }

    @Test
    void 없는_수업_종류를_수정하면_CLASS_TYPE_003을_정확히_반환한다() {
        // given
        when(classTypeService.update(anyLong(), anyLong(), anyLong(), any(ClassTypeUpdateRequest.class)))
                .thenThrow(new ClassException(ClassErrorCode.CLASS_TYPE_NOT_FOUND));

        // when
        RestTestClient.ResponseSpec result = 수업_종류_이름을_수정한다(
                7L, 999L, "1", ClassTypeFixture.기본_수업_종류_수정_요청());

        // then
        오류를_검증한다(result, 404, "CLASS_TYPE-003", "수업 종류를 찾을 수 없습니다.");
    }

    @Test
    void 중복된_이름으로_수업_종류를_수정하면_CLASS_TYPE_002를_정확히_반환한다() {
        // given
        when(classTypeService.update(anyLong(), anyLong(), anyLong(), any(ClassTypeUpdateRequest.class)))
                .thenThrow(new ClassException(ClassErrorCode.CLASS_TYPE_NAME_DUPLICATED));

        // when
        RestTestClient.ResponseSpec result = 수업_종류_이름을_수정한다(
                7L, 13L, "1", ClassTypeFixture.기본_수업_종류_수정_요청());

        // then
        오류를_검증한다(result, 409, "CLASS_TYPE-002", "이미 존재하는 수업 종류 이름입니다.");
    }

    @Test
    void 수업_종류_수정_버전_헤더가_없으면_API_001을_반환하고_서비스를_호출하지_않는다() {
        // given / when
        RestTestClient.ResponseSpec result = client.patch()
                .uri("/api/studios/7/class-types/13")
                .contentType(MediaType.APPLICATION_JSON)
                .body(ClassTypeFixture.기본_수업_종류_수정_요청())
                .exchange();

        // then
        오류를_검증한다(result, 400, "API-001", "X-API-Version 헤더는 필수입니다.");
        verify(classTypeService, never()).update(anyLong(), anyLong(), anyLong(), any());
    }

    @Test
    void 수업_종류_수정_버전이_지원되지_않으면_API_002를_반환하고_서비스를_호출하지_않는다() {
        // given / when
        RestTestClient.ResponseSpec result = 수업_종류_이름을_수정한다(
                7L, 13L, "2", ClassTypeFixture.기본_수업_종류_수정_요청());

        // then
        오류를_검증한다(result, 400, "API-002", "지원하지 않는 API 버전입니다.");
        verify(classTypeService, never()).update(anyLong(), anyLong(), anyLong(), any());
    }

    @Test
    void 수업_종류를_삭제하면_204와_빈_본문을_반환하고_서비스에_위임한다() {
        // given / when
        RestTestClient.ResponseSpec result = 수업_종류를_삭제한다(7L, 13L, "1");

        // then
        result.expectStatus().isNoContent()
                .expectBody().isEmpty();
        verify(classTypeService).delete(1L, 7L, 13L);
    }

    @Test
    void 없는_수업_종류를_삭제하면_CLASS_TYPE_003을_정확히_반환한다() {
        // given
        doThrow(new ClassException(ClassErrorCode.CLASS_TYPE_NOT_FOUND))
                .when(classTypeService).delete(anyLong(), anyLong(), anyLong());

        // when
        RestTestClient.ResponseSpec result = 수업_종류를_삭제한다(7L, 999L, "1");

        // then
        오류를_검증한다(result, 404, "CLASS_TYPE-003", "수업 종류를 찾을 수 없습니다.");
    }

    @Test
    void 수업_종류_삭제_버전_헤더가_없으면_API_001을_반환하고_서비스를_호출하지_않는다() {
        // given / when
        RestTestClient.ResponseSpec result = client.delete()
                .uri("/api/studios/7/class-types/13")
                .exchange();

        // then
        오류를_검증한다(result, 400, "API-001", "X-API-Version 헤더는 필수입니다.");
        verify(classTypeService, never()).delete(anyLong(), anyLong(), anyLong());
    }

    @Test
    void 수업_종류_삭제_버전이_지원되지_않으면_API_002를_반환하고_서비스를_호출하지_않는다() {
        // given / when
        RestTestClient.ResponseSpec result = 수업_종류를_삭제한다(7L, 13L, "2");

        // then
        오류를_검증한다(result, 400, "API-002", "지원하지 않는 API 버전입니다.");
        verify(classTypeService, never()).delete(anyLong(), anyLong(), anyLong());
    }

    private RestTestClient.ResponseSpec 수업_종류_목록을_조회한다(Long studioId, String apiVersion) {
        return client.get()
                .uri("/api/studios/{studioId}/class-types", studioId)
                .header("X-API-Version", apiVersion)
                .exchange();
    }

    private RestTestClient.ResponseSpec 수업_종류를_등록한다(
            Long studioId,
            String apiVersion,
            ClassTypeCreateRequest request
    ) {
        return client.post()
                .uri("/api/studios/{studioId}/class-types", studioId)
                .header("X-API-Version", apiVersion)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange();
    }

    private RestTestClient.ResponseSpec 수업_종류를_삭제한다(
            Long studioId,
            Long classTypeId,
            String apiVersion
    ) {
        return client.delete()
                .uri("/api/studios/{studioId}/class-types/{classTypeId}", studioId, classTypeId)
                .header("X-API-Version", apiVersion)
                .exchange();
    }

    private RestTestClient.ResponseSpec 수업_종류_이름을_수정한다(
            Long studioId,
            Long classTypeId,
            String apiVersion,
            ClassTypeUpdateRequest request
    ) {
        return client.patch()
                .uri("/api/studios/{studioId}/class-types/{classTypeId}", studioId, classTypeId)
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
