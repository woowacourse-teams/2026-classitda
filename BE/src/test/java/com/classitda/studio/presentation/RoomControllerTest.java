package com.classitda.studio.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;

import com.classitda.common.config.ApiVersionConfig;
import com.classitda.common.exception.GlobalExceptionHandler;
import com.classitda.common.pagination.CursorResponse;
import com.classitda.studio.application.RoomService;
import com.classitda.studio.exception.StudioErrorCode;
import com.classitda.studio.exception.StudioException;
import com.classitda.studio.fixture.RoomFixture;
import com.classitda.studio.presentation.dto.RoomCreateRequest;
import com.classitda.studio.presentation.dto.RoomResponse;
import com.classitda.studio.presentation.dto.RoomUpdateRequest;
import java.util.List;
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
@WebMvcTest(RoomController.class)
class RoomControllerTest {

    private final RestTestClient client;

    @MockitoBean
    private RoomService roomService;

    @Autowired
    RoomControllerTest(RestTestClient client) {
        this.client = client;
    }

    @Test
    void 룸을_등록하면_201과_룸_정보를_반환한다() {
        // given
        when(roomService.save(anyLong(), anyLong(), any(RoomCreateRequest.class)))
                .thenReturn(new RoomResponse(1L, "A룸"));

        // when
        RestTestClient.ResponseSpec result = client.post()
                .uri("/api/studios/1/rooms")
                .header("X-API-Version", "1")
                .header("X-Member-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .body(RoomFixture.기본_룸_생성_요청())
                .exchange();

        // then
        result.expectStatus().isCreated()
                .expectBody()
                .json("""
                        {"id":1,"name":"A룸"}
                        """, JsonCompareMode.STRICT);
    }

    @Test
    void 룸_이름이_비어_있으면_COMMON_001을_반환한다() {
        // given
        RoomCreateRequest request = new RoomCreateRequest(" ");

        // when
        RestTestClient.ResponseSpec result = client.post()
                .uri("/api/studios/1/rooms")
                .header("X-API-Version", "1")
                .header("X-Member-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange();

        // then
        result.expectStatus().isBadRequest()
                .expectBody()
                .json("""
                        {"code":"COMMON-001","message":"요청 값이 올바르지 않습니다."}
                        """, JsonCompareMode.STRICT);
    }

    @Test
    void 이름이_중복되면_ROOM_002를_반환한다() {
        // given
        when(roomService.save(anyLong(), anyLong(), any(RoomCreateRequest.class)))
                .thenThrow(new StudioException(StudioErrorCode.ROOM_NAME_DUPLICATED));

        // when
        RestTestClient.ResponseSpec result = client.post()
                .uri("/api/studios/1/rooms")
                .header("X-API-Version", "1")
                .header("X-Member-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .body(RoomFixture.기본_룸_생성_요청())
                .exchange();

        // then
        result.expectStatus().isEqualTo(409)
                .expectBody()
                .json("""
                        {"code":"ROOM-002","message":"이미 사용 중인 룸 이름입니다."}
                        """, JsonCompareMode.STRICT);
    }

    @Test
    void 룸_목록을_조회하면_200과_커서_응답을_반환한다() {
        // given
        when(roomService.findWithCursor(anyLong(), nullable(String.class), anyInt()))
                .thenReturn(CursorResponse.of(List.of(new RoomResponse(1L, "A룸")), true, "1"));

        // when
        RestTestClient.ResponseSpec result = client.get()
                .uri("/api/studios/1/rooms")
                .header("X-API-Version", "1")
                .exchange();

        // then
        result.expectStatus().isOk()
                .expectBody()
                .json("""
                        {"items":[{"id":1,"name":"A룸"}],"hasNext":true,"nextCursor":"1"}
                        """, JsonCompareMode.STRICT);
    }

    @Test
    void 없는_시설의_룸_목록을_조회하면_STUDIO_002를_반환한다() {
        // given
        when(roomService.findWithCursor(anyLong(), nullable(String.class), anyInt()))
                .thenThrow(new StudioException(StudioErrorCode.NOT_FOUND));

        // when
        RestTestClient.ResponseSpec result = client.get()
                .uri("/api/studios/999/rooms")
                .header("X-API-Version", "1")
                .exchange();

        // then
        result.expectStatus().isNotFound()
                .expectBody()
                .json("""
                        {"code":"STUDIO-002","message":"시설을 찾을 수 없습니다."}
                        """, JsonCompareMode.STRICT);
    }

    @Test
    void 룸을_수정하면_200과_룸_정보를_반환한다() {
        // given
        when(roomService.update(anyLong(), anyLong(), anyLong(), any(RoomUpdateRequest.class)))
                .thenReturn(new RoomResponse(1L, "B룸"));

        // when
        RestTestClient.ResponseSpec result = client.patch()
                .uri("/api/studios/1/rooms/1")
                .header("X-API-Version", "1")
                .header("X-Member-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .body(RoomFixture.이름만_바꾸는_수정_요청("B룸"))
                .exchange();

        // then
        result.expectStatus().isOk()
                .expectBody()
                .json("""
                        {"id":1,"name":"B룸"}
                        """, JsonCompareMode.STRICT);
    }

    @Test
    void 수정_시_룸_이름이_비어_있으면_COMMON_001을_반환한다() {
        // given
        RoomUpdateRequest request = new RoomUpdateRequest(" ");

        // when
        RestTestClient.ResponseSpec result = client.patch()
                .uri("/api/studios/1/rooms/1")
                .header("X-API-Version", "1")
                .header("X-Member-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .exchange();

        // then
        result.expectStatus().isBadRequest()
                .expectBody()
                .json("""
                        {"code":"COMMON-001","message":"요청 값이 올바르지 않습니다."}
                        """, JsonCompareMode.STRICT);
    }

    @Test
    void 권한이_없으면_PERMISSION_001을_반환한다() {
        // given
        when(roomService.update(anyLong(), anyLong(), anyLong(), any(RoomUpdateRequest.class)))
                .thenThrow(new StudioException(StudioErrorCode.PERMISSION_DENIED));

        // when
        RestTestClient.ResponseSpec result = client.patch()
                .uri("/api/studios/1/rooms/1")
                .header("X-API-Version", "1")
                .header("X-Member-Id", "2")
                .contentType(MediaType.APPLICATION_JSON)
                .body(RoomFixture.이름만_바꾸는_수정_요청("B룸"))
                .exchange();

        // then
        result.expectStatus().isForbidden()
                .expectBody()
                .json("""
                        {"code":"PERMISSION-001","message":"이 작업을 수행할 권한이 없습니다."}
                        """, JsonCompareMode.STRICT);
    }

    @Test
    void 없는_룸을_수정하면_ROOM_001을_반환한다() {
        // given
        when(roomService.update(anyLong(), anyLong(), anyLong(), any(RoomUpdateRequest.class)))
                .thenThrow(new StudioException(StudioErrorCode.ROOM_NOT_FOUND));

        // when
        RestTestClient.ResponseSpec result = client.patch()
                .uri("/api/studios/1/rooms/999")
                .header("X-API-Version", "1")
                .header("X-Member-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .body(RoomFixture.이름만_바꾸는_수정_요청("B룸"))
                .exchange();

        // then
        result.expectStatus().isNotFound()
                .expectBody()
                .json("""
                        {"code":"ROOM-001","message":"룸을 찾을 수 없습니다."}
                        """, JsonCompareMode.STRICT);
    }

    @Test
    void 수정_시_이름이_중복되면_ROOM_002를_반환한다() {
        // given
        when(roomService.update(anyLong(), anyLong(), anyLong(), any(RoomUpdateRequest.class)))
                .thenThrow(new StudioException(StudioErrorCode.ROOM_NAME_DUPLICATED));

        // when
        RestTestClient.ResponseSpec result = client.patch()
                .uri("/api/studios/1/rooms/1")
                .header("X-API-Version", "1")
                .header("X-Member-Id", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .body(RoomFixture.이름만_바꾸는_수정_요청("B룸"))
                .exchange();

        // then
        result.expectStatus().isEqualTo(409)
                .expectBody()
                .json("""
                        {"code":"ROOM-002","message":"이미 사용 중인 룸 이름입니다."}
                        """, JsonCompareMode.STRICT);
    }

    @Test
    void 버전_헤더가_없으면_API_001을_반환한다() {
        // given / when
        RestTestClient.ResponseSpec result = client.get()
                .uri("/api/studios/1/rooms")
                .exchange();

        // then
        result.expectStatus().isBadRequest()
                .expectBody()
                .json("""
                        {"code":"API-001","message":"X-API-Version 헤더는 필수입니다."}
                        """, JsonCompareMode.STRICT);
    }
}
