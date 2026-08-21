package com.classitda.classes.presentation;

import com.classitda.classes.presentation.dto.ReservationCreateRequest;
import com.classitda.classes.presentation.dto.ReservationResponse;
import com.classitda.common.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@SecurityRequirement(name = "bearerAuth")
@Tag(name = "수업 예약", description = "담당 강사가 자기 수업의 참여자를 등록·취소하는 API")
public interface ReservationControllerApi {

    @Operation(
            summary = "수업 참여자 등록",
            description = "담당 강사가 자기 수업에 참여자를 등록한다. 예약 관리 권한(RESERVATION_MANAGE)이 필요하며, "
                    + "**해당 수업의 담당 강사만** 등록할 수 있다. "
                    + "회원을 등록할 때는 `membershipId` 를, 시설에 등록되지 않은 사람을 등록할 때는 `guestName` 을 보낸다. "
                    + "둘을 동시에 보내거나 둘 다 비우면 400이다. "
                    + "회원은 같은 시간에 진행되는 다른 수업에 이미 예약되어 있으면 등록할 수 없다. "
                    + "비회원은 이름만으로 식별하므로 시간 겹침을 검사하지 않는다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "등록 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 값이 올바르지 않거나 강사를 참여자로 지정함",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"code":"RESERVATION-001","message":"회원 또는 비회원 중 한 명을 지정해야 합니다."}""")
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "권한이 없거나 담당 강사가 아님",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"code":"RESERVATION-009","message":"담당 강사만 이 수업의 예약을 관리할 수 있습니다."}""")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "시설·수업·소속을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "정원 초과, 같은 수업 중복 예약, 같은 시간 다른 수업과 겹침, 종료·취소된 수업",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"code":"RESERVATION-007","message":"같은 시간에 진행되는 다른 수업에 이미 예약되어 있습니다."}""")
                    )
            )
    })
    ResponseEntity<ReservationResponse> save(
            @Parameter(hidden = true) Long memberId,
            @Parameter(description = "시설 아이디", example = "1") Long studioId,
            @Parameter(description = "수업 아이디", example = "10") Long classSessionId,
            ReservationCreateRequest request
    );

    @Operation(
            summary = "수업 참여자 취소",
            description = "담당 강사가 자기 수업의 예약을 취소한다. 예약 관리 권한(RESERVATION_MANAGE)이 필요하며, "
                    + "**해당 수업의 담당 강사만** 취소할 수 있다. "
                    + "예약을 삭제하지 않고 상태를 CANCELED 로 바꾸므로 이력이 남는다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "취소 성공. 응답 본문이 없다"),
            @ApiResponse(
                    responseCode = "403",
                    description = "권한이 없거나 담당 강사가 아님",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "시설·수업·예약을 찾을 수 없음",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"code":"RESERVATION-004","message":"예약을 찾을 수 없습니다."}""")
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "이미 취소된 예약이거나 종료·취소된 수업",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    ResponseEntity<Void> cancel(
            @Parameter(hidden = true) Long memberId,
            @Parameter(description = "시설 아이디", example = "1") Long studioId,
            @Parameter(description = "수업 아이디", example = "10") Long classSessionId,
            @Parameter(description = "예약 아이디", example = "100") Long reservationId
    );
}
