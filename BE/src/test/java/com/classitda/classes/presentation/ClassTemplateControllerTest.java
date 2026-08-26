package com.classitda.classes.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.classitda.authentication.presentation.resolver.CurrentMemberIdArgumentResolver;
import com.classitda.classes.application.ClassTemplateCommandService;
import com.classitda.classes.application.ClassTemplateQueryService;
import com.classitda.classes.domain.ClassForm;
import com.classitda.classes.exception.ClassErrorCode;
import com.classitda.classes.exception.ClassException;
import com.classitda.classes.fixture.ClassTemplateFixture;
import com.classitda.classes.presentation.dto.ClassTemplateCreateRequest;
import com.classitda.classes.presentation.dto.ClassTemplateResponse;
import com.classitda.classes.presentation.dto.ClassTemplateUpdateRequest;
import com.classitda.classes.presentation.dto.ClassTypeResponse;
import com.classitda.common.config.ApiVersionConfig;
import com.classitda.common.exception.GlobalExceptionHandler;
import com.classitda.studio.exception.StudioErrorCode;
import com.classitda.studio.exception.StudioException;
import java.time.DayOfWeek;
import java.time.LocalTime;
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
@WebMvcTest(ClassTemplateController.class)
class ClassTemplateControllerTest {

    private final RestTestClient client;

    @MockitoBean
    private ClassTemplateCommandService commandService;

    @MockitoBean
    private ClassTemplateQueryService queryService;

    @MockitoBean
    private CurrentMemberIdArgumentResolver currentMemberIdArgumentResolver;

    @Autowired
    ClassTemplateControllerTest(RestTestClient client) {
        this.client = client;
    }

    @BeforeEach
    void 인증된_회원_아이디를_주입한다() throws Exception {
        when(currentMemberIdArgumentResolver.supportsParameter(any())).thenReturn(true);
        when(currentMemberIdArgumentResolver.resolveArgument(any(), any(), any(), any())).thenReturn(1L);
    }

    @Test
    void 수업_템플릿을_등록하면_201과_빈_본문을_반환하고_명령_서비스에_위임한다() {
        // given
        ClassTemplateCreateRequest request = ClassTemplateFixture.기본_수업_템플릿_생성_요청(1L);

        // when
        RestTestClient.ResponseSpec result = 수업_템플릿을_등록한다(7L, "1", request);

        // then
        result.expectStatus().isCreated().expectBody().isEmpty();
        verify(commandService).save(eq(1L), eq(7L), eq(request));
    }

    @Test
    void 필수_이름이_비어_있으면_COMMON_001을_반환하고_명령_서비스를_호출하지_않는다() {
        // given
        ClassTemplateCreateRequest request = ClassTemplateFixture.수업_템플릿_생성_요청(
                " ", null, null, 1L);

        // when
        RestTestClient.ResponseSpec result = 수업_템플릿을_등록한다(7L, "1", request);

        // then
        오류를_검증한다(result, 400, "COMMON-001", "요청 값이 올바르지 않습니다.");
        verify(commandService, never()).save(anyLong(), anyLong(), any());
    }

    @Test
    void 여러_수업_종류_ID_배열로_등록하면_COMMON_001을_반환한다() {
        // when
        RestTestClient.ResponseSpec result = client.post()
                .uri("/api/studios/7/class-templates")
                .header("X-API-Version", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {"name":"저녁 요가","classForm":"GROUP","durationMinutes":60,
                        "startTime":"20:00:00","capacity":12,"classTypeIds":[1,2]}
                        """)
                .exchange();

        // then
        오류를_검증한다(result, 400, "COMMON-001", "요청 값이 올바르지 않습니다.");
        verify(commandService, never()).save(anyLong(), anyLong(), any());
    }

    @Test
    void 등록_버전_헤더가_없거나_지원되지_않으면_서비스를_호출하지_않는다() {
        // given
        ClassTemplateCreateRequest request = ClassTemplateFixture.기본_수업_템플릿_생성_요청(1L);

        // when / then
        오류를_검증한다(client.post()
                .uri("/api/studios/7/class-templates")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange(), 400, "API-001", "X-API-Version 헤더는 필수입니다.");
        오류를_검증한다(수업_템플릿을_등록한다(7L, "3", request),
                400, "API-002", "지원하지 않는 API 버전입니다.");
        verify(commandService, never()).save(anyLong(), anyLong(), any());
    }

    @Test
    void 명령_서비스의_권한과_수업_종류_예외를_정확히_직렬화한다() {
        // given
        ClassTemplateCreateRequest request = ClassTemplateFixture.기본_수업_템플릿_생성_요청(1L);
        doThrow(new StudioException(StudioErrorCode.PERMISSION_DENIED),
                new ClassException(ClassErrorCode.CLASS_TYPE_NOT_FOUND))
                .when(commandService).save(1L, 7L, request);

        // when / then
        오류를_검증한다(수업_템플릿을_등록한다(7L, "1", request),
                403, "PERMISSION-001", "이 작업을 수행할 권한이 없습니다.");
        오류를_검증한다(수업_템플릿을_등록한다(7L, "1", request),
                404, "CLASS_TYPE-003", "수업 종류를 찾을 수 없습니다.");
    }

    @Test
    void 수업_템플릿을_전체_수정하면_204와_빈_본문을_반환하고_정확한_요청을_위임한다() {
        // given
        ClassTemplateUpdateRequest request = ClassTemplateFixture.기본_수업_템플릿_수정_요청(1L);

        // when
        RestTestClient.ResponseSpec result = 수업_템플릿을_수정한다(7L, 11L, "1", request);

        // then
        result.expectStatus().isNoContent().expectBody().isEmpty();
        verify(commandService).update(1L, 7L, 11L, request);
    }

    @Test
    void description과_반복_요일이_null이어도_전체_수정_요청으로_위임한다() {
        // given
        ClassTemplateUpdateRequest request = ClassTemplateFixture.수업_템플릿_수정_요청(
                "요일 없는 템플릿", null, ClassForm.GROUP, 60,
                LocalTime.of(20, 0), null, 12, 1L);

        // when
        RestTestClient.ResponseSpec result = 수업_템플릿을_수정한다(7L, 11L, "1", request);

        // then
        result.expectStatus().isNoContent().expectBody().isEmpty();
        verify(commandService).update(1L, 7L, 11L, request);
    }

    @Test
    void 반복_요일을_생략해도_null로_역직렬화하여_전체_수정_요청으로_위임한다() {
        // when
        RestTestClient.ResponseSpec result = client.put()
                .uri("/api/studios/7/class-templates/11")
                .header("X-API-Version", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {"name":"요일 없는 템플릿","description":null,"classForm":"GROUP",
                        "durationMinutes":60,"startTime":"20:00:00","capacity":12,"classTypeId":1}
                        """)
                .exchange();

        // then
        result.expectStatus().isNoContent().expectBody().isEmpty();
        verify(commandService).update(eq(1L), eq(7L), eq(11L),
                eq(ClassTemplateFixture.수업_템플릿_수정_요청(
                        "요일 없는 템플릿", null, ClassForm.GROUP, 60,
                        LocalTime.of(20, 0), null, 12, 1L)));
    }

    @Test
    void 등록_시작_시간에_초가_없으면_COMMON_001을_반환한다() {
        // when
        RestTestClient.ResponseSpec result = client.post()
                .uri("/api/studios/7/class-templates")
                .header("X-API-Version", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {"name":"저녁 요가","classForm":"GROUP","durationMinutes":60,
                        "startTime":"20:00","capacity":12,"classTypeId":1}
                        """)
                .exchange();

        // then
        오류를_검증한다(result, 400, "COMMON-001", "요청 값이 올바르지 않습니다.");
        verify(commandService, never()).save(anyLong(), anyLong(), any());
    }

    @Test
    void 수정_시작_시간에_초가_없으면_COMMON_001을_반환한다() {
        // when
        RestTestClient.ResponseSpec result = client.put()
                .uri("/api/studios/7/class-templates/11")
                .header("X-API-Version", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {"name":"저녁 요가","classForm":"GROUP","durationMinutes":60,
                        "startTime":"20:00","capacity":12,"classTypeId":1}
                        """)
                .exchange();

        // then
        오류를_검증한다(result, 400, "COMMON-001", "요청 값이 올바르지 않습니다.");
        verify(commandService, never()).update(anyLong(), anyLong(), anyLong(), any());
    }

    @Test
    void 전체_수정의_필수값이_누락되면_COMMON_001을_반환하고_서비스를_호출하지_않는다() {
        // given
        List<ClassTemplateUpdateRequest> invalidRequests = List.of(
                ClassTemplateFixture.수업_템플릿_수정_요청(
                        null, null, ClassForm.GROUP, 60, LocalTime.of(20, 0), null, 12, 1L),
                ClassTemplateFixture.수업_템플릿_수정_요청(
                        "템플릿", null, null, 60, LocalTime.of(20, 0), null, 12, 1L),
                ClassTemplateFixture.수업_템플릿_수정_요청(
                        "템플릿", null, ClassForm.GROUP, null, LocalTime.of(20, 0), null, 12, 1L),
                ClassTemplateFixture.수업_템플릿_수정_요청(
                        "템플릿", null, ClassForm.GROUP, 60, null, null, 12, 1L),
                ClassTemplateFixture.수업_템플릿_수정_요청(
                        "템플릿", null, ClassForm.GROUP, 60, LocalTime.of(20, 0), null, null, 1L),
                ClassTemplateFixture.수업_템플릿_수정_요청(
                        "템플릿", null, ClassForm.GROUP, 60, LocalTime.of(20, 0), null, 12, null)
        );

        // when / then
        invalidRequests.forEach(request -> 오류를_검증한다(
                수업_템플릿을_수정한다(7L, 11L, "1", request),
                400,
                "COMMON-001",
                "요청 값이 올바르지 않습니다."
        ));
        verify(commandService, never()).update(anyLong(), anyLong(), anyLong(), any());
    }

    @Test
    void 전체_수정_버전이_없거나_지원되지_않으면_서비스를_호출하지_않는다() {
        // given
        ClassTemplateUpdateRequest request = ClassTemplateFixture.기본_수업_템플릿_수정_요청(1L);

        // when / then
        오류를_검증한다(client.put()
                .uri("/api/studios/7/class-templates/11")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange(), 400, "API-001", "X-API-Version 헤더는 필수입니다.");
        오류를_검증한다(수업_템플릿을_수정한다(7L, 11L, "3", request),
                400, "API-002", "지원하지 않는 API 버전입니다.");
        verify(commandService, never()).update(anyLong(), anyLong(), anyLong(), any());
    }

    @Test
    void 전체_수정_서비스의_권한과_템플릿과_수업_종류_예외를_정확히_직렬화한다() {
        // given
        ClassTemplateUpdateRequest request = ClassTemplateFixture.기본_수업_템플릿_수정_요청(1L);
        doThrow(
                new StudioException(StudioErrorCode.PERMISSION_DENIED),
                new ClassException(ClassErrorCode.CLASS_TEMPLATE_NOT_FOUND),
                new ClassException(ClassErrorCode.CLASS_TYPE_NOT_FOUND)
        ).when(commandService).update(1L, 7L, 11L, request);

        // when / then
        오류를_검증한다(수업_템플릿을_수정한다(7L, 11L, "1", request),
                403, "PERMISSION-001", "이 작업을 수행할 권한이 없습니다.");
        오류를_검증한다(수업_템플릿을_수정한다(7L, 11L, "1", request),
                404, "CLASS_TEMPLATE-007", "수업 템플릿을 찾을 수 없습니다.");
        오류를_검증한다(수업_템플릿을_수정한다(7L, 11L, "1", request),
                404, "CLASS_TYPE-003", "수업 종류를 찾을 수 없습니다.");
    }

    @Test
    void 수업_템플릿을_삭제하면_204와_빈_본문을_반환하고_정확한_ID를_위임한다() {
        // when
        RestTestClient.ResponseSpec result = 수업_템플릿을_삭제한다(7L, 11L, "1");

        // then
        result.expectStatus().isNoContent().expectBody().isEmpty();
        verify(commandService).delete(1L, 7L, 11L);
    }

    @Test
    void 삭제_버전이_없거나_지원되지_않으면_명령_서비스를_호출하지_않는다() {
        // when / then
        오류를_검증한다(client.delete()
                .uri("/api/studios/7/class-templates/11")
                .exchange(), 400, "API-001", "X-API-Version 헤더는 필수입니다.");
        오류를_검증한다(수업_템플릿을_삭제한다(7L, 11L, "3"),
                400, "API-002", "지원하지 않는 API 버전입니다.");
        verify(commandService, never()).delete(anyLong(), anyLong(), anyLong());
    }

    @Test
    void 삭제_서비스의_권한과_템플릿_없음_예외를_정확히_직렬화한다() {
        // given
        doThrow(
                new StudioException(StudioErrorCode.PERMISSION_DENIED),
                new ClassException(ClassErrorCode.CLASS_TEMPLATE_NOT_FOUND)
        ).when(commandService).delete(1L, 7L, 11L);

        // when / then
        오류를_검증한다(수업_템플릿을_삭제한다(7L, 11L, "1"),
                403, "PERMISSION-001", "이 작업을 수행할 권한이 없습니다.");
        오류를_검증한다(수업_템플릿을_삭제한다(7L, 11L, "1"),
                404, "CLASS_TEMPLATE-007", "수업 템플릿을 찾을 수 없습니다.");
    }

    @Test
    void 수업_템플릿_목록은_200과_순서가_유지된_최상위_배열을_반환한다() {
        // given
        when(queryService.findAll(1L, 7L)).thenReturn(List.of(new ClassTemplateResponse(
                2L,
                "저녁 요가",
                null,
                ClassForm.GROUP,
                60,
                LocalTime.of(20, 0),
                List.of(DayOfWeek.MONDAY, DayOfWeek.FRIDAY),
                12,
                ClassTypeResponse.of(1L, "요가")
        )));

        // when
        RestTestClient.ResponseSpec result = 수업_템플릿_목록을_조회한다(7L, "1");

        // then
        result.expectStatus().isOk().expectBody().json("""
                [{"id":2,"name":"저녁 요가","description":null,"classForm":"GROUP",
                "durationMinutes":60,"startTime":"20:00:00","recurringDays":["MONDAY","FRIDAY"],
                "capacity":12,"classType":{"id":1,"name":"요가"}}]
                """, JsonCompareMode.STRICT);
        verify(queryService).findAll(1L, 7L);
    }

    @Test
    void 수업_템플릿이_없으면_200과_빈_배열을_반환한다() {
        // given
        when(queryService.findAll(1L, 7L)).thenReturn(List.of());

        // when
        RestTestClient.ResponseSpec result = 수업_템플릿_목록을_조회한다(7L, "1");

        // then
        result.expectStatus().isOk().expectBody().json("[]", JsonCompareMode.STRICT);
    }

    @Test
    void 목록_조회_버전이_없거나_지원되지_않으면_조회_서비스를_호출하지_않는다() {
        // when / then
        오류를_검증한다(client.get().uri("/api/studios/7/class-templates").exchange(),
                400, "API-001", "X-API-Version 헤더는 필수입니다.");
        오류를_검증한다(수업_템플릿_목록을_조회한다(7L, "3"),
                400, "API-002", "지원하지 않는 API 버전입니다.");
        verify(queryService, never()).findAll(anyLong(), anyLong());
    }

    private RestTestClient.ResponseSpec 수업_템플릿을_등록한다(
            Long studioId,
            String version,
            ClassTemplateCreateRequest request
    ) {
        return client.post()
                .uri("/api/studios/{studioId}/class-templates", studioId)
                .header("X-API-Version", version)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange();
    }

    private RestTestClient.ResponseSpec 수업_템플릿_목록을_조회한다(Long studioId, String version) {
        return client.get()
                .uri("/api/studios/{studioId}/class-templates", studioId)
                .header("X-API-Version", version)
                .exchange();
    }

    private RestTestClient.ResponseSpec 수업_템플릿을_수정한다(
            Long studioId,
            Long classTemplateId,
            String version,
            ClassTemplateUpdateRequest request
    ) {
        return client.put()
                .uri("/api/studios/{studioId}/class-templates/{classTemplateId}", studioId, classTemplateId)
                .header("X-API-Version", version)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange();
    }

    private RestTestClient.ResponseSpec 수업_템플릿을_삭제한다(
            Long studioId,
            Long classTemplateId,
            String version
    ) {
        return client.delete()
                .uri("/api/studios/{studioId}/class-templates/{classTemplateId}", studioId, classTemplateId)
                .header("X-API-Version", version)
                .exchange();
    }

    private void 오류를_검증한다(
            RestTestClient.ResponseSpec result,
            int status,
            String code,
            String message
    ) {
        result.expectStatus().isEqualTo(status).expectBody().json("""
                {"code":"%s","message":"%s"}
                """.formatted(code, message), JsonCompareMode.STRICT);
    }
}
