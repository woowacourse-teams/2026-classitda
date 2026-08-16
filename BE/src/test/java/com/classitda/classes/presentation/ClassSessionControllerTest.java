package com.classitda.classes.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.classitda.authentication.presentation.resolver.CurrentMemberIdArgumentResolver;
import com.classitda.classes.application.ClassSessionCommandService;
import com.classitda.classes.exception.ClassErrorCode;
import com.classitda.classes.exception.ClassException;
import com.classitda.classes.fixture.ClassSessionFixture;
import com.classitda.classes.presentation.dto.ClassSessionCreateRequest;
import com.classitda.common.config.ApiVersionConfig;
import com.classitda.common.exception.GlobalExceptionHandler;
import com.classitda.studio.exception.StudioErrorCode;
import com.classitda.studio.exception.StudioException;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
@WebMvcTest(ClassSessionController.class)
class ClassSessionControllerTest {

    private final RestTestClient client;

    @MockitoBean
    private ClassSessionCommandService commandService;

    @MockitoBean
    private CurrentMemberIdArgumentResolver currentMemberIdArgumentResolver;

    @Autowired
    ClassSessionControllerTest(RestTestClient client) {
        this.client = client;
    }

    @BeforeEach
    void 인증된_회원_아이디를_주입한다() throws Exception {
        when(currentMemberIdArgumentResolver.supportsParameter(any())).thenReturn(true);
        when(currentMemberIdArgumentResolver.resolveArgument(any(), any(), any(), any())).thenReturn(1L);
    }

    @Test
    void 수업_회차를_등록하면_201과_빈_본문을_반환하고_명령_서비스에_위임한다() {
        // given
        ClassSessionCreateRequest request = ClassSessionFixture.기본_단일_수업_회차_생성_요청(5L, 3L);

        // when
        RestTestClient.ResponseSpec result = 수업_회차를_등록한다(7L, "1", request);

        // then
        result.expectStatus().isCreated().expectBody().isEmpty();
        verify(commandService).save(eq(1L), eq(7L), eq(request));
    }

    @Test
    void 필수_요청값이_유효하지_않으면_COMMON_001을_반환하고_명령_서비스를_호출하지_않는다() {
        // given
        ClassSessionCreateRequest request = ClassSessionFixture.수업_회차_생성_요청(
                null, null, null, null, " ", 0, 0, null, null,
                null, null, null, null, null);

        // when
        RestTestClient.ResponseSpec result = 수업_회차를_등록한다(7L, "1", request);

        // then
        오류를_검증한다(result, 400, "COMMON-001", "요청 값이 올바르지 않습니다.");
        verify(commandService, never()).save(anyLong(), anyLong(), any());
    }

    @Test
    void 버전_헤더가_없으면_API_001을_반환하고_명령_서비스를_호출하지_않는다() {
        // given
        ClassSessionCreateRequest request = ClassSessionFixture.기본_단일_수업_회차_생성_요청(5L, 3L);

        // when
        RestTestClient.ResponseSpec result = client.post()
                .uri("/api/studios/7/class-sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange();

        // then
        오류를_검증한다(result, 400, "API-001", "X-API-Version 헤더는 필수입니다.");
        verify(commandService, never()).save(anyLong(), anyLong(), any());
    }

    @Test
    void 지원하지_않는_버전이면_API_002를_반환하고_명령_서비스를_호출하지_않는다() {
        // given
        ClassSessionCreateRequest request = ClassSessionFixture.기본_단일_수업_회차_생성_요청(5L, 3L);

        // when
        RestTestClient.ResponseSpec result = 수업_회차를_등록한다(7L, "2", request);

        // then
        오류를_검증한다(result, 400, "API-002", "지원하지 않는 API 버전입니다.");
        verify(commandService, never()).save(anyLong(), anyLong(), any());
    }

    @ParameterizedTest
    @MethodSource("수업_회차_생성_예외")
    void 명령_서비스_예외를_정확한_HTTP_응답으로_직렬화한다(
            RuntimeException exception,
            int status,
            String code,
            String message
    ) {
        // given
        ClassSessionCreateRequest request = ClassSessionFixture.기본_단일_수업_회차_생성_요청(5L, 3L);
        doThrow(exception).when(commandService).save(1L, 7L, request);

        // when
        RestTestClient.ResponseSpec result = 수업_회차를_등록한다(7L, "1", request);

        // then
        오류를_검증한다(result, status, code, message);
    }

    private RestTestClient.ResponseSpec 수업_회차를_등록한다(
            Long studioId,
            String version,
            ClassSessionCreateRequest request
    ) {
        return client.post()
                .uri("/api/studios/{studioId}/class-sessions", studioId)
                .header("X-API-Version", version)
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
        result.expectStatus().isEqualTo(status).expectBody().json("""
                {"code":"%s","message":"%s"}
                """.formatted(code, message), JsonCompareMode.STRICT);
    }

    private static Stream<Arguments> 수업_회차_생성_예외() {
        return Stream.of(
                Arguments.of(
                        new StudioException(StudioErrorCode.PERMISSION_DENIED),
                        403,
                        "PERMISSION-001",
                        "이 작업을 수행할 권한이 없습니다."
                ),
                Arguments.of(
                        new StudioException(StudioErrorCode.NOT_FOUND),
                        404,
                        "STUDIO-002",
                        "시설을 찾을 수 없습니다."
                ),
                Arguments.of(
                        new ClassException(ClassErrorCode.CLASS_TEMPLATE_NOT_FOUND),
                        404,
                        "CLASS_TEMPLATE-007",
                        "수업 템플릿을 찾을 수 없습니다."
                ),
                Arguments.of(
                        new ClassException(ClassErrorCode.CLASS_TYPE_NOT_FOUND),
                        404,
                        "CLASS_TYPE-003",
                        "수업 종류를 찾을 수 없습니다."
                ),
                Arguments.of(
                        new ClassException(ClassErrorCode.CLASS_SESSION_INSTRUCTOR_NOT_FOUND),
                        404,
                        "CLASS_SESSION-017",
                        "담당 가능한 강사 소속을 찾을 수 없습니다."
                ),
                Arguments.of(
                        new ClassException(ClassErrorCode.INVALID_CLASS_SESSION_RECURRENCE),
                        400,
                        "CLASS_SESSION-009",
                        "반복 여부에 맞는 수업 일정 정보가 필요합니다."
                ),
                Arguments.of(
                        new ClassException(ClassErrorCode.CLASS_SESSION_TIME_CONFLICT),
                        409,
                        "CLASS_SESSION-015",
                        "담당 강사의 기존 수업과 시간이 겹칩니다."
                )
        );
    }
}
