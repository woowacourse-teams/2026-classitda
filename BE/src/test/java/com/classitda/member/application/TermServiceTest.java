package com.classitda.member.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import com.classitda.member.domain.TermCode;
import com.classitda.member.presentation.dto.TermResponse;
import com.classitda.support.MySqlDataJpaTest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@MySqlDataJpaTest
class TermServiceTest {

    private final TermService termService;

    @Autowired
    TermServiceTest(TermService termService) {
        this.termService = termService;
    }

    @Test
    void 저장된_약관_전체를_약관_코드_순서로_조회한다() {
        // given / when
        List<TermResponse> responses = termService.findAll();

        // then
        assertThat(responses).allSatisfy(response -> assertThat(response.id()).isPositive());
        assertThat(responses)
                .extracting(
                        TermResponse::code,
                        TermResponse::title,
                        TermResponse::url,
                        TermResponse::required,
                        TermResponse::version
                )
                .containsExactly(
                        tuple(
                                TermCode.SERVICE_TERMS,
                                "서비스 이용약관",
                                "https://example.invalid/terms/service-v1",
                                true,
                                1
                        ),
                        tuple(
                                TermCode.PRIVACY_POLICY,
                                "개인정보 처리방침",
                                "https://example.invalid/terms/privacy-v1",
                                true,
                                1
                        ),
                        tuple(
                                TermCode.MARKETING_CONSENT,
                                "마케팅 정보 수신 동의",
                                "https://example.invalid/terms/marketing-v1",
                                false,
                                1
                        )
                );
    }
}
