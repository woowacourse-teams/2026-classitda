package com.classitda.member.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.classitda.authentication.presentation.resolver.CurrentMemberIdArgumentResolver;
import com.classitda.common.config.ApiVersionConfig;
import com.classitda.common.exception.GlobalExceptionHandler;
import com.classitda.member.application.MemberService;
import com.classitda.member.exception.MemberErrorCode;
import com.classitda.member.exception.MemberException;
import com.classitda.member.fixture.MemberFixture;
import com.classitda.member.presentation.dto.MyNameUpdateRequest;
import com.classitda.member.presentation.dto.MyProfileResponse;
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
@WebMvcTest(MemberController.class)
class MemberControllerTest {

    private final RestTestClient client;

    @MockitoBean
    private MemberService memberService;

    @MockitoBean
    private CurrentMemberIdArgumentResolver currentMemberIdArgumentResolver;

    @Autowired
    MemberControllerTest(RestTestClient client) {
        this.client = client;
    }

    @BeforeEach
    void 인증된_회원_아이디를_주입한다() throws Exception {
        when(currentMemberIdArgumentResolver.supportsParameter(any())).thenReturn(true);
        when(currentMemberIdArgumentResolver.resolveArgument(any(), any(), any(), any())).thenReturn(1L);
    }

    @Test
    void 내_정보를_조회하면_200과_이름_번호_이메일을_반환한다() {
        // given
        when(memberService.findMe(1L)).thenReturn(
                MyProfileResponse.of(MemberFixture.회원("김클래스", "01012345678"), "member@example.com"));

        // when
        RestTestClient.ResponseSpec result = client.get()
                .uri("/api/members/me")
                .header("X-API-Version", "1")
                .exchange();

        // then
        result.expectStatus().isOk()
                .expectBody()
                .json("""
                        {
                          "name": "김클래스",
                          "phoneNumber": "01012345678",
                          "email": "member@example.com"
                        }
                        """, JsonCompareMode.STRICT);
    }

    @Test
    void 소셜_이메일이_없으면_이메일을_null_로_반환한다() {
        // given
        when(memberService.findMe(1L)).thenReturn(
                MyProfileResponse.of(MemberFixture.회원("김클래스", "01012345678"), null));

        // when
        RestTestClient.ResponseSpec result = client.get()
                .uri("/api/members/me")
                .header("X-API-Version", "1")
                .exchange();

        // then
        result.expectStatus().isOk()
                .expectBody()
                .json("""
                        {
                          "name": "김클래스",
                          "phoneNumber": "01012345678",
                          "email": null
                        }
                        """, JsonCompareMode.STRICT);
    }

    @Test
    void 없는_회원을_조회하면_MEMBER_008을_반환한다() {
        // given
        when(memberService.findMe(1L))
                .thenThrow(new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        // when
        RestTestClient.ResponseSpec result = client.get()
                .uri("/api/members/me")
                .header("X-API-Version", "1")
                .exchange();

        // then
        result.expectStatus().isNotFound()
                .expectBody()
                .json("""
                        {"code":"MEMBER-008","message":"회원을 찾을 수 없습니다."}
                        """, JsonCompareMode.STRICT);
    }

    @Test
    void 이름을_수정하면_204를_반환하고_서비스에_위임한다() {
        // when
        RestTestClient.ResponseSpec result = updateName("이클래스");

        // then
        result.expectStatus().isNoContent()
                .expectBody().isEmpty();
        verify(memberService).updateName(1L, MyNameUpdateRequest.from("이클래스"));
    }

    @Test
    void 이름이_비어_있으면_COMMON_001을_반환한다() {
        // when
        RestTestClient.ResponseSpec result = updateName(" ");

        // then
        result.expectStatus().isBadRequest()
                .expectBody()
                .json("""
                        {"code":"COMMON-001","message":"요청 값이 올바르지 않습니다."}
                        """, JsonCompareMode.STRICT);
    }

    @Test
    void 이름이_오십자를_넘으면_COMMON_001을_반환한다() {
        // when
        RestTestClient.ResponseSpec result = updateName("가".repeat(51));

        // then
        result.expectStatus().isBadRequest()
                .expectBody()
                .json("""
                        {"code":"COMMON-001","message":"요청 값이 올바르지 않습니다."}
                        """, JsonCompareMode.STRICT);
    }

    @Test
    void 없는_회원의_이름을_수정하면_MEMBER_008을_반환한다() {
        // given
        doThrow(new MemberException(MemberErrorCode.MEMBER_NOT_FOUND))
                .when(memberService).updateName(any(), any(MyNameUpdateRequest.class));

        // when
        RestTestClient.ResponseSpec result = updateName("이클래스");

        // then
        result.expectStatus().isNotFound()
                .expectBody()
                .json("""
                        {"code":"MEMBER-008","message":"회원을 찾을 수 없습니다."}
                        """, JsonCompareMode.STRICT);
    }

    @Test
    void 회원이_탈퇴하면_204를_반환한다() {
        // when
        RestTestClient.ResponseSpec result = withdraw();

        // then
        result.expectStatus().isNoContent()
                .expectBody().isEmpty();
        verify(memberService).withdraw(1L);
    }

    @Test
    void 시설_대표가_탈퇴하면_MEMBER_009를_반환한다() {
        // given
        willThrow(new MemberException(MemberErrorCode.MEMBER_WITHDRAWAL_BLOCKED_BY_OWNED_STUDIO))
                .given(memberService).withdraw(1L);

        // when
        RestTestClient.ResponseSpec result = withdraw();

        // then
        result.expectStatus().isEqualTo(409)
                .expectBody()
                .json("""
                        {"code":"MEMBER-009","message":"시설 대표는 탈퇴할 수 없습니다."}
                        """, JsonCompareMode.STRICT);
    }

    private RestTestClient.ResponseSpec withdraw() {
        return client.delete()
                .uri("/api/members/me")
                .header("X-API-Version", "1")
                .exchange();
    }
    private RestTestClient.ResponseSpec updateName(String name) {
        return client.patch()
                .uri("/api/members/me/name")
                .header("X-API-Version", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .body(MyNameUpdateRequest.from(name))
                .exchange();
    }
}
