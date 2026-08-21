package com.classitda.classes.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.classitda.authentication.presentation.resolver.CurrentMemberIdArgumentResolver;
import com.classitda.classes.application.ReservationService;
import com.classitda.classes.domain.ReservationStatus;
import com.classitda.classes.exception.ClassErrorCode;
import com.classitda.classes.exception.ClassException;
import com.classitda.classes.presentation.dto.ReservationResponse;
import com.classitda.common.config.ApiVersionConfig;
import com.classitda.common.exception.GlobalExceptionHandler;
import java.time.LocalDateTime;
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
@WebMvcTest(ReservationController.class)
class ReservationControllerTest {

    private final RestTestClient client;

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private CurrentMemberIdArgumentResolver currentMemberIdArgumentResolver;

    @Autowired
    ReservationControllerTest(RestTestClient client) {
        this.client = client;
    }

    @BeforeEach
    void 인증된_회원_아이디를_주입한다() throws Exception {
        when(currentMemberIdArgumentResolver.supportsParameter(any())).thenReturn(true);
        when(currentMemberIdArgumentResolver.resolveArgument(any(), any(), any(), any())).thenReturn(1L);
    }

    @Test
    void 회원을_등록하면_201과_예약_정보를_반환한다() {
        // given
        when(reservationService.save(anyLong(), anyLong(), anyLong(), any()))
                .thenReturn(new ReservationResponse(
                        100L, 10L, 5L, null, "김회원",
                        ReservationStatus.RESERVED, LocalDateTime.of(2026, 9, 1, 10, 0)));

        // when
        RestTestClient.ResponseSpec result = client.post()
                .uri("/api/studios/1/class-sessions/10/reservations")
                .header("X-API-Version", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {"membershipId": 5}""")
                .exchange();

        // then
        result.expectStatus().isCreated()
                .expectBody().json("""
                        {"id":100,"classSessionId":10,"membershipId":5,"classGuestId":null,
                         "attendeeName":"김회원","status":"RESERVED","reservedAt":"2026-09-01T10:00:00"}
                        """, JsonCompareMode.STRICT);
        verify(reservationService, times(1)).save(anyLong(), anyLong(), anyLong(), any());
    }

    @Test
    void 비회원을_이름만으로_등록할_수_있다() {
        // given
        when(reservationService.save(anyLong(), anyLong(), anyLong(), any()))
                .thenReturn(new ReservationResponse(
                        101L, 10L, null, 7L, "현장 손님",
                        ReservationStatus.RESERVED, LocalDateTime.of(2026, 9, 1, 10, 0)));

        // when
        RestTestClient.ResponseSpec result = client.post()
                .uri("/api/studios/1/class-sessions/10/reservations")
                .header("X-API-Version", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {"guestName": "현장 손님"}""")
                .exchange();

        // then
        result.expectStatus().isCreated()
                .expectBody().jsonPath("$.classGuestId").isEqualTo(7)
                .jsonPath("$.attendeeName").isEqualTo("현장 손님")
                .jsonPath("$.membershipId").doesNotExist();
    }

    @Test
    void 비회원_이름이_오십자를_넘으면_400을_반환한다() {
        // given
        String tooLongName = "가".repeat(51);

        // when
        RestTestClient.ResponseSpec result = client.post()
                .uri("/api/studios/1/class-sessions/10/reservations")
                .header("X-API-Version", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {"guestName": "%s"}""".formatted(tooLongName))
                .exchange();

        // then
        result.expectStatus().isBadRequest();
        verify(reservationService, never()).save(anyLong(), anyLong(), anyLong(), any());
    }

    @Test
    void API_버전_헤더가_없으면_400을_반환한다() {
        // when
        RestTestClient.ResponseSpec result = client.post()
                .uri("/api/studios/1/class-sessions/10/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {"membershipId": 5}""")
                .exchange();

        // then
        result.expectStatus().isBadRequest();
        verify(reservationService, never()).save(anyLong(), anyLong(), anyLong(), any());
    }

    @Test
    void 담당_강사가_아니면_403을_반환한다() {
        // given
        when(reservationService.save(anyLong(), anyLong(), anyLong(), any()))
                .thenThrow(new ClassException(ClassErrorCode.RESERVATION_SESSION_NOT_MANAGEABLE));

        // when
        RestTestClient.ResponseSpec result = client.post()
                .uri("/api/studios/1/class-sessions/10/reservations")
                .header("X-API-Version", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {"membershipId": 5}""")
                .exchange();

        // then
        result.expectStatus().isForbidden()
                .expectBody().jsonPath("$.code").isEqualTo("RESERVATION-009");
    }

    @Test
    void 시간이_겹치면_409를_반환한다() {
        // given
        when(reservationService.save(anyLong(), anyLong(), anyLong(), any()))
                .thenThrow(new ClassException(ClassErrorCode.RESERVATION_TIME_OVERLAPPED));

        // when
        RestTestClient.ResponseSpec result = client.post()
                .uri("/api/studios/1/class-sessions/10/reservations")
                .header("X-API-Version", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {"membershipId": 5}""")
                .exchange();

        // then
        result.expectStatus().isEqualTo(409)
                .expectBody().jsonPath("$.code").isEqualTo("RESERVATION-007");
    }

    @Test
    void 예약을_취소하면_204를_반환한다() {
        // when
        RestTestClient.ResponseSpec result = client.delete()
                .uri("/api/studios/1/class-sessions/10/reservations/100")
                .header("X-API-Version", "1")
                .exchange();

        // then
        result.expectStatus().isNoContent();
        verify(reservationService, times(1)).cancel(anyLong(), anyLong(), anyLong(), anyLong());
    }

    @Test
    void 없는_예약을_취소하면_404를_반환한다() {
        // given
        doThrow(new ClassException(ClassErrorCode.RESERVATION_NOT_FOUND))
                .when(reservationService).cancel(anyLong(), anyLong(), anyLong(), anyLong());

        // when
        RestTestClient.ResponseSpec result = client.delete()
                .uri("/api/studios/1/class-sessions/10/reservations/999")
                .header("X-API-Version", "1")
                .exchange();

        // then
        result.expectStatus().isNotFound()
                .expectBody().jsonPath("$.code").isEqualTo("RESERVATION-004");
    }
}
