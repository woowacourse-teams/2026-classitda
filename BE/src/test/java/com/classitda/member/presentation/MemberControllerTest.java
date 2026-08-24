package com.classitda.member.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.classitda.authentication.presentation.resolver.CurrentMemberIdArgumentResolver;
import com.classitda.common.config.ApiVersionConfig;
import com.classitda.common.exception.GlobalExceptionHandler;
import com.classitda.member.application.MemberService;
import com.classitda.member.exception.MemberErrorCode;
import com.classitda.member.exception.MemberException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
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
}
