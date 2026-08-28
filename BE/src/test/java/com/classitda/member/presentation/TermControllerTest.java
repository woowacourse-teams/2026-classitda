package com.classitda.member.presentation;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.classitda.common.config.ApiVersionConfig;
import com.classitda.member.application.TermService;
import com.classitda.member.domain.Term;
import com.classitda.member.domain.TermCode;
import com.classitda.member.presentation.dto.TermResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.test.web.servlet.client.RestTestClient;

@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestTestClient
@Import(ApiVersionConfig.class)
@WebMvcTest(controllers = TermController.class)
class TermControllerTest {

    private final RestTestClient client;

    @MockitoBean
    private TermService termService;

    @Autowired
    TermControllerTest(RestTestClient client) {
        this.client = client;
    }

    @Test
    void 약관_목록을_조회한다() {
        // given
        Term term = mock(Term.class);
        given(term.getId()).willReturn(1L);
        given(term.getCode()).willReturn(TermCode.SERVICE_TERMS);
        given(term.getTitle()).willReturn("서비스 이용약관");
        given(term.getUrl()).willReturn("https://example.invalid/terms/service-v1");
        given(term.isRequired()).willReturn(true);
        given(term.getVersion()).willReturn(1);
        TermResponse termResponse = TermResponse.from(term);
        given(termService.findAll()).willReturn(List.of(termResponse));

        // when
        RestTestClient.ResponseSpec response = client.get()
                .uri("/api/terms")
                .header("X-API-Version", "1")
                .exchange();

        // then
        response.expectStatus().isOk()
                .expectBody()
                .json("""
                        [{
                          "id": 1,
                          "code": "SERVICE_TERMS",
                          "title": "서비스 이용약관",
                          "url": "https://example.invalid/terms/service-v1",
                          "required": true,
                          "version": 1
                        }]
                        """, JsonCompareMode.STRICT);
        verify(termService).findAll();
    }
}
