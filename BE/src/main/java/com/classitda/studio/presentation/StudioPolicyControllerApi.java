package com.classitda.studio.presentation;

import com.classitda.common.exception.ErrorResponse;
import com.classitda.studio.presentation.dto.StudioPolicyResponse;
import com.classitda.studio.presentation.dto.StudioPolicyUpdateRequest;
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

@Tag(name = "시설 운영 정책", description = "시설의 예약 마감, 무료 취소, 예약 대기 규칙을 관리하는 API")
public interface StudioPolicyControllerApi {

    @Operation(
            summary = "운영 정책 조회",
            description = """
                    ### 조회 범위

                    - 시설의 운영 정책을 조회합니다. 회원도 예약 규칙을 알아야 하므로 권한 제한이 없습니다.
                    - **운영 정책은 시설을 만들 때 기본값으로 함께 생성됩니다.** 따로 등록하는 API 는 없습니다.

                    ### 기본값

                    | 항목 | 기본값 |
                    | --- | --- |
                    | `reservationCloseMinutesBefore` | 30분 전 |
                    | `freeCancelMinutesBefore` | 720분(12시간) 전 |
                    | `waitingOfferResponseMinutes` | 60분 |
                    | `maxHoldDays` | 0일 |
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = "API 버전 헤더가 없음",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "버전 헤더 누락", value = """
                                    {"code": "API-001", "message": "X-API-Version 헤더는 필수입니다."}""")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "시설 또는 운영 정책을 찾을 수 없음",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "시설 없음",
                                            value = """
                                                    {"code": "STUDIO-002", "message": "시설을 찾을 수 없습니다."}"""
                                    ),
                                    @ExampleObject(
                                            name = "정책 없음",
                                            value = """
                                                    {"code": "POLICY-001", "message": "운영 정책을 찾을 수 없습니다."}"""
                                    )
                            }
                    )
            )
    })
    StudioPolicyResponse findByStudioId(
            @Parameter(description = "시설 ID", required = true, example = "1")
            Long studioId
    );

    @Operation(
            summary = "운영 정책 수정",
            description = """
                    ### 수정 방식

                    - **전달한 필드만 변경되고, 생략하거나 `null` 로 보낸 필드는 기존 값을 유지합니다.**
                    - 운영 정책은 시설을 만들 때 함께 생성되므로, 이 API 로 값을 바꿉니다.

                    ### 항목

                    | 항목 | 뜻 | 범위 |
                    | --- | --- | --- |
                    | `reservationCloseMinutesBefore` | 수업 시작 몇 분 전에 예약을 마감할지 | 0 ~ 10080 |
                    | `freeCancelMinutesBefore` | 수업 시작 몇 분 전까지 무료 취소할 수 있는지 | 0 ~ 10080 |
                    | `waitingOfferResponseMinutes` | 대기 제안을 받고 몇 분 안에 응답해야 하는지 | 1 ~ 1440 |
                    | `maxHoldDays` | 수강권을 최대 며칠 홀드할 수 있는지 | 0 ~ 365 |

                    ### 권한

                    - 대표이거나 운영 정책 관리 권한이 있어야 합니다.
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "수정 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 값이 올바르지 않거나 API 버전 헤더가 없음",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "요청 값 오류",
                                            value = """
                                                    {"code": "COMMON-001", "message": "요청 값이 올바르지 않습니다."}"""
                                    ),
                                    @ExampleObject(
                                            name = "버전 헤더 누락",
                                            value = """
                                                    {"code": "API-001", "message": "X-API-Version 헤더는 필수입니다."}"""
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "권한이 없거나 시설 소속이 아님",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "권한 없음", value = """
                                            {"code": "PERMISSION-001", \
                                            "message": "이 작업을 수행할 권한이 없습니다."}"""),
                                    @ExampleObject(name = "소속 아님", value = """
                                            {"code": "MEMBERSHIP-001", "message": "해당 시설의 소속이 아닙니다."}""")
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "시설 또는 운영 정책을 찾을 수 없음",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "시설 없음",
                                            value = """
                                                    {"code": "STUDIO-002", "message": "시설을 찾을 수 없습니다."}"""
                                    ),
                                    @ExampleObject(
                                            name = "정책 없음",
                                            value = """
                                                    {"code": "POLICY-001", "message": "운영 정책을 찾을 수 없습니다."}"""
                                    )
                            }
                    )
            )
    })
    ResponseEntity<Void> update(
            @Parameter(hidden = true)
            Long memberId,
            @Parameter(description = "시설 ID", required = true, example = "1")
            Long studioId,
            StudioPolicyUpdateRequest request
    );
}
