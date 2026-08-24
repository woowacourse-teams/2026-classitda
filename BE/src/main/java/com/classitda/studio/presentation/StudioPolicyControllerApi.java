package com.classitda.studio.presentation;

import com.classitda.common.exception.ErrorResponse;
import com.classitda.studio.presentation.dto.StudioPolicyCreateRequest;
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
            summary = "운영 정책 등록",
            description = """
                    시설의 운영 정책을 등록한다. 대표 강사만 등록할 수 있다.
                    시설당 정책은 하나만 존재하며, 이미 등록되어 있으면 등록할 수 없다.
                    모든 시간은 수업 시작 시각 기준으로 몇 분 전인지를 뜻하며, 값이 클수록 이른 시각이다.
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "운영 정책 등록 성공"),
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
                    description = "시설을 찾을 수 없음",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "시설 없음", value = """
                                    {"code": "STUDIO-002", "message": "시설을 찾을 수 없습니다."}""")
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "이미 운영 정책이 등록된 시설",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "정책 중복", value = """
                                    {"code": "POLICY-002", "message": "이미 운영 정책이 등록된 시설입니다."}""")
                    )
            )
    })
    ResponseEntity<Void> save(
            @Parameter(hidden = true)
            Long memberId,
            @Parameter(description = "시설 ID", required = true, example = "1")
            Long studioId,
            StudioPolicyCreateRequest request
    );

    @Operation(
            summary = "운영 정책 조회",
            description = "시설의 운영 정책을 조회한다. 회원도 예약 규칙을 알아야 하므로 권한 제한이 없다."
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
                    시설의 운영 정책을 수정한다. 대표 강사만 수정할 수 있다.
                    전달한 필드만 변경되고, 보내지 않은 필드는 기존 값을 유지한다.
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
