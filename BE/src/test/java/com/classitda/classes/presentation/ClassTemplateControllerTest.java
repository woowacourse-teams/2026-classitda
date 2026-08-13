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
        ClassTemplateCreateRequest request = ClassTemplateFixture.기본_수업_템플릿_생성_요청(List.of(3L, 1L));

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
                " ", null, null, List.of(1L));

        // when
        RestTestClient.ResponseSpec result = 수업_템플릿을_등록한다(7L, "1", request);

        // then
        오류를_검증한다(result, 400, "COMMON-001", "요청 값이 올바르지 않습니다.");
        verify(commandService, never()).save(anyLong(), anyLong(), any());
    }

    @Test
    void 등록_버전_헤더가_없거나_지원되지_않으면_서비스를_호출하지_않는다() {
        // given
        ClassTemplateCreateRequest request = ClassTemplateFixture.기본_수업_템플릿_생성_요청(List.of(1L));

        // when / then
        오류를_검증한다(client.post()
                .uri("/api/studios/7/class-templates")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange(), 400, "API-001", "X-API-Version 헤더는 필수입니다.");
        오류를_검증한다(수업_템플릿을_등록한다(7L, "2", request),
                400, "API-002", "지원하지 않는 API 버전입니다.");
        verify(commandService, never()).save(anyLong(), anyLong(), any());
    }

    @Test
    void 명령_서비스의_권한과_수업_종류_예외를_정확히_직렬화한다() {
        // given
        ClassTemplateCreateRequest request = ClassTemplateFixture.기본_수업_템플릿_생성_요청(List.of(1L));
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
                List.of(ClassTypeResponse.of(1L, "요가"), ClassTypeResponse.of(3L, "필라테스"))
        )));

        // when
        RestTestClient.ResponseSpec result = 수업_템플릿_목록을_조회한다(7L, "1");

        // then
        result.expectStatus().isOk().expectBody().json("""
                [{"id":2,"name":"저녁 요가","description":null,"classForm":"GROUP",
                "durationMinutes":60,"startTime":"20:00:00","recurringDays":["MONDAY","FRIDAY"],
                "capacity":12,"classTypes":[{"id":1,"name":"요가"},{"id":3,"name":"필라테스"}]}]
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
        오류를_검증한다(수업_템플릿_목록을_조회한다(7L, "2"),
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
