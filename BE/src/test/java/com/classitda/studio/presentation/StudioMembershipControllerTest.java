package com.classitda.studio.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.classitda.authentication.presentation.resolver.CurrentMemberIdArgumentResolver;
import com.classitda.common.config.ApiVersionConfig;
import com.classitda.common.exception.GlobalExceptionHandler;
import com.classitda.common.pagination.CursorResponse;
import com.classitda.studio.application.StudioMembershipService;
import com.classitda.studio.domain.MembershipStatus;
import com.classitda.studio.exception.StudioErrorCode;
import com.classitda.studio.exception.StudioException;
import com.classitda.studio.presentation.dto.StudioMembershipUpdateRequest;
import com.classitda.studio.fixture.StudioMembershipFixture;
import com.classitda.studio.presentation.dto.StudioMembershipCreateRequest;
import com.classitda.studio.presentation.dto.StudioMembershipResponse;
import com.classitda.studio.presentation.dto.StudioRoleResponse;
import java.time.LocalDateTime;
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
@WebMvcTest(StudioMembershipController.class)
class StudioMembershipControllerTest {

    private static final LocalDateTime JOINED_AT = LocalDateTime.of(2026, 8, 14, 10, 0);

    private final RestTestClient client;

    @MockitoBean
    private StudioMembershipService studioMembershipService;

    @MockitoBean
    private CurrentMemberIdArgumentResolver currentMemberIdArgumentResolver;

    @Autowired
    StudioMembershipControllerTest(RestTestClient client) {
        this.client = client;
    }

    @BeforeEach
    void 인증된_회원_아이디를_주입한다() throws Exception {
        when(currentMemberIdArgumentResolver.supportsParameter(any())).thenReturn(true);
        when(currentMemberIdArgumentResolver.resolveArgument(any(), any(), any(), any())).thenReturn(1L);
    }

    @Test
    void 회원을_등록하면_본문_없이_201을_반환한다() {
        // given
        StudioMembershipCreateRequest request = StudioMembershipFixture.기본_소속_등록_요청();

        // when
        RestTestClient.ResponseSpec result = 회원을_등록한다(7L, "1", request);

        // then
        result.expectStatus().isCreated()
                .expectBody().isEmpty();
        verify(studioMembershipService).saveStudent(eq(1L), eq(7L), eq(request));
    }

    @Test
    void 강사를_등록하면_본문_없이_201을_반환한다() {
        // given
        StudioMembershipCreateRequest request = StudioMembershipFixture.기본_소속_등록_요청();

        // when
        RestTestClient.ResponseSpec result = 강사를_등록한다(7L, "1", request);

        // then
        result.expectStatus().isCreated()
                .expectBody().isEmpty();
        verify(studioMembershipService).saveInstructor(eq(1L), eq(7L), eq(request));
    }

    @Test
    void 역할_관리_권한이_없으면_강사_등록은_PERMISSION_001을_반환한다() {
        // given
        doThrow(new StudioException(StudioErrorCode.PERMISSION_DENIED))
                .when(studioMembershipService)
                .saveInstructor(anyLong(), anyLong(), any(StudioMembershipCreateRequest.class));

        // when
        RestTestClient.ResponseSpec result = 강사를_등록한다(7L, "1", StudioMembershipFixture.기본_소속_등록_요청());

        // then
        오류를_검증한다(result, 403, "PERMISSION-001", "이 작업을 수행할 권한이 없습니다.");
    }

    @Test
    void 소속_이름이_비어_있으면_COMMON_001을_반환한다() {
        // given
        StudioMembershipCreateRequest request = StudioMembershipFixture.소속_등록_요청(" ", StudioMembershipFixture.기본_전화번호);

        // when
        RestTestClient.ResponseSpec result = 회원을_등록한다(7L, "1", request);

        // then
        오류를_검증한다(result, 400, "COMMON-001", "요청 값이 올바르지 않습니다.");
    }

    @Test
    void 전화번호_형식이_올바르지_않으면_COMMON_001을_반환한다() {
        // given
        StudioMembershipCreateRequest request = StudioMembershipFixture.전화번호가_다른_소속_등록_요청("010-1234-5678");

        // when
        RestTestClient.ResponseSpec result = 회원을_등록한다(7L, "1", request);

        // then
        오류를_검증한다(result, 400, "COMMON-001", "요청 값이 올바르지 않습니다.");
    }

    @Test
    void 시설에_역할이_없으면_ROLE_001을_반환한다() {
        // given
        doThrow(new StudioException(StudioErrorCode.STUDIO_ROLE_NOT_FOUND))
                .when(studioMembershipService).saveStudent(anyLong(), anyLong(), any(StudioMembershipCreateRequest.class));

        // when
        RestTestClient.ResponseSpec result = 회원을_등록한다(7L, "1", StudioMembershipFixture.기본_소속_등록_요청());

        // then
        오류를_검증한다(result, 404, "ROLE-001", "시설 역할을 찾을 수 없습니다.");
    }

    @Test
    void 이미_등록된_회원이면_MEMBERSHIP_004를_반환한다() {
        // given
        doThrow(new StudioException(StudioErrorCode.MEMBERSHIP_ALREADY_EXISTS))
                .when(studioMembershipService).saveStudent(anyLong(), anyLong(), any(StudioMembershipCreateRequest.class));

        // when
        RestTestClient.ResponseSpec result = 회원을_등록한다(7L, "1", StudioMembershipFixture.기본_소속_등록_요청());

        // then
        오류를_검증한다(result, 409, "MEMBERSHIP-004", "이미 시설에 등록된 회원입니다.");
    }


    @Test
    void 회원_목록을_조회하면_200과_커서_응답을_반환한다() {
        // given
        when(studioMembershipService.findStudentsWithCursor(eq(1L), eq(7L), any(), anyInt()))
                .thenReturn(CursorResponse.of(List.of(기본_소속_응답()), true, "1"));

        // when
        RestTestClient.ResponseSpec result = 회원_목록을_조회한다(7L, "1");

        // then
        result.expectStatus().isOk()
                .expectBody()
                .json("""
                        {
                          "items": [{
                            "id": 1,
                            "name": "김철수",
                            "phoneNumber": "01012345678",
                            "studioRole": {"name": "회원", "instructor": false},
                            "registered": false,
                            "status": "ACTIVE",
                            "joinedAt": "2026-08-14T10:00:00"
                          }],
                          "hasNext": true,
                          "nextCursor": "1"
                        }
                        """, JsonCompareMode.STRICT);
        verify(studioMembershipService).findStudentsWithCursor(1L, 7L, null, 10);
    }

    @Test
    void 없는_시설의_회원_목록을_조회하면_STUDIO_002를_반환한다() {
        // given
        when(studioMembershipService.findStudentsWithCursor(anyLong(), anyLong(), any(), anyInt()))
                .thenThrow(new StudioException(StudioErrorCode.NOT_FOUND));

        // when
        RestTestClient.ResponseSpec result = 회원_목록을_조회한다(999L, "1");

        // then
        오류를_검증한다(result, 404, "STUDIO-002", "시설을 찾을 수 없습니다.");
    }

    @Test
    void 목록_조회_권한_예외들은_문서화된_403_응답으로_직렬화한다() {
        // given
        when(studioMembershipService.findStudentsWithCursor(anyLong(), anyLong(), any(), anyInt())).thenThrow(
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
            오류를_검증한다(회원_목록을_조회한다(7L, "1"), 403, codes[index], messages[index]);
        }
        verify(studioMembershipService, times(3)).findStudentsWithCursor(anyLong(), anyLong(), any(), anyInt());
    }

    @Test
    void 소속_단건을_조회하면_200을_반환한다() {
        // given
        when(studioMembershipService.findById(1L, 7L, 1L)).thenReturn(기본_소속_응답());

        // when
        RestTestClient.ResponseSpec result = client.get()
                .uri("/api/studios/{studioId}/memberships/{membershipId}", 7L, 1L)
                .header("X-API-Version", "1")
                .exchange();

        // then
        result.expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.name").isEqualTo("김철수");
        verify(studioMembershipService).findById(1L, 7L, 1L);
    }

    @Test
    void 없는_소속을_조회하면_MEMBERSHIP_005를_반환한다() {
        // given
        when(studioMembershipService.findById(anyLong(), anyLong(), anyLong()))
                .thenThrow(new StudioException(StudioErrorCode.MEMBERSHIP_NOT_FOUND));

        // when
        RestTestClient.ResponseSpec result = client.get()
                .uri("/api/studios/{studioId}/memberships/{membershipId}", 7L, 999L)
                .header("X-API-Version", "1")
                .exchange();

        // then
        오류를_검증한다(result, 404, "MEMBERSHIP-005", "시설 소속을 찾을 수 없습니다.");
    }

    private StudioMembershipResponse 기본_소속_응답() {
        return new StudioMembershipResponse(
                1L,
                StudioMembershipFixture.기본_이름,
                StudioMembershipFixture.기본_전화번호,
                new StudioRoleResponse("회원", false),
                false,
                MembershipStatus.ACTIVE,
                JOINED_AT
        );
    }

    private RestTestClient.ResponseSpec 회원을_등록한다(
            Long studioId,
            String apiVersion,
            StudioMembershipCreateRequest request
    ) {
        return client.post()
                .uri("/api/studios/{studioId}/memberships/students", studioId)
                .header("X-API-Version", apiVersion)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange();
    }

    private RestTestClient.ResponseSpec 강사를_등록한다(
            Long studioId,
            String apiVersion,
            StudioMembershipCreateRequest request
    ) {
        return client.post()
                .uri("/api/studios/{studioId}/memberships/instructors", studioId)
                .header("X-API-Version", apiVersion)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange();
    }

    private RestTestClient.ResponseSpec 회원_목록을_조회한다(Long studioId, String apiVersion) {
        return client.get()
                .uri("/api/studios/{studioId}/memberships/students", studioId)
                .header("X-API-Version", apiVersion)
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
    @Test
    void 회원_정보를_수정하면_204를_반환하고_서비스에_위임한다() {
        // when
        RestTestClient.ResponseSpec result = client.patch()
                .uri("/api/studios/1/memberships/2")
                .header("X-API-Version", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .body(StudioMembershipUpdateRequest.of("김민수", "01012345678"))
                .exchange();

        // then
        result.expectStatus().isNoContent();
        verify(studioMembershipService)
                .update(1L, 1L, 2L, StudioMembershipUpdateRequest.of("김민수", "01012345678"));
    }

    @Test
    void 전화번호_형식이_틀리면_COMMON_001을_반환한다() {
        // when
        RestTestClient.ResponseSpec result = client.patch()
                .uri("/api/studios/1/memberships/2")
                .header("X-API-Version", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .body(StudioMembershipUpdateRequest.of("김민수", "0212345678"))
                .exchange();

        // then
        result.expectStatus().isBadRequest()
                .expectBody()
                .json("""
                        {"code":"COMMON-001","message":"요청 값이 올바르지 않습니다."}
                        """, JsonCompareMode.STRICT);
    }

    @Test
    void 가입한_회원의_번호를_바꾸면_MEMBERSHIP_007을_반환한다() {
        // given
        doThrow(new StudioException(StudioErrorCode.MEMBERSHIP_PHONE_NUMBER_NOT_EDITABLE))
                .when(studioMembershipService)
                .update(anyLong(), anyLong(), anyLong(), any(StudioMembershipUpdateRequest.class));

        // when
        RestTestClient.ResponseSpec result = client.patch()
                .uri("/api/studios/1/memberships/2")
                .header("X-API-Version", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .body(StudioMembershipUpdateRequest.of("김민수", "01099998888"))
                .exchange();

        // then
        result.expectStatus().isEqualTo(409)
                .expectBody()
                .json("""
                        {"code":"MEMBERSHIP-007","message":"가입한 회원의 휴대전화 번호는 수정할 수 없습니다."}
                        """, JsonCompareMode.STRICT);
    }

    @Test
    void 회원을_삭제하면_204를_반환하고_서비스에_위임한다() {
        // when
        RestTestClient.ResponseSpec result = client.delete()
                .uri("/api/studios/1/memberships/2")
                .header("X-API-Version", "1")
                .exchange();

        // then
        result.expectStatus().isNoContent();
        verify(studioMembershipService).delete(1L, 1L, 2L);
    }

    @Test
    void 대표_강사를_삭제하면_MEMBERSHIP_009를_반환한다() {
        // given
        doThrow(new StudioException(StudioErrorCode.MEMBERSHIP_OWNER_NOT_DELETABLE))
                .when(studioMembershipService).delete(anyLong(), anyLong(), anyLong());

        // when
        RestTestClient.ResponseSpec result = client.delete()
                .uri("/api/studios/1/memberships/2")
                .header("X-API-Version", "1")
                .exchange();

        // then
        result.expectStatus().isEqualTo(409)
                .expectBody()
                .json("""
                        {"code":"MEMBERSHIP-009","message":"시설 대표는 삭제할 수 없습니다."}
                        """, JsonCompareMode.STRICT);
    }
}
