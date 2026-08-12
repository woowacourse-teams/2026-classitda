package com.classitda.studio.presentation;

import com.classitda.common.exception.ErrorResponse;
import com.classitda.studio.presentation.dto.StudioCreateRequest;
import com.classitda.studio.presentation.dto.StudioResponse;
import com.classitda.studio.presentation.dto.StudioUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "시설", description = "시설 생성과 관리 API")
public interface StudioControllerApi {

    @Operation(
            summary = "시설 생성",
            description = """
                    새 시설을 만든다. 생성자는 해당 시설의 대표 강사로 등록되고,
                    시스템 기본 역할(대표 강사, 일반 강사, 회원)이 함께 생성된다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "시설 생성 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 값이 올바르지 않거나, 운영 시간이 잘못되었거나, API 버전 헤더가 없음",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "요청 값 오류",
                                            value = """
                                                    {"code": "COMMON-001", "message": "요청 값이 올바르지 않습니다."}"""
                                    ),
                                    @ExampleObject(
                                            name = "운영 시간 오류",
                                            value = """
                                                    {"code": "STUDIO-001", \
                                                    "message": "운영 종료 시간은 시작 시간보다 늦어야 합니다."}"""
                                    ),
                                    @ExampleObject(
                                            name = "버전 헤더 누락",
                                            value = """
                                                    {"code": "API-001", "message": "X-API-Version 헤더는 필수입니다."}"""
                                    )
                            }
                    )
            )
    })
    ResponseEntity<StudioResponse> save(
            @Parameter(
                    description = "요청자 회원 ID. 인증 연동 전까지만 사용하는 임시 헤더다.",
                    required = true,
                    example = "1"
            )
            Long memberId,
            StudioCreateRequest request
    );

    @Operation(
            summary = "시설 정보 조회",
            description = "시설의 기본 정보를 조회한다. 권한 제한이 없다."
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
                    description = "시설을 찾을 수 없음",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "시설 없음", value = """
                                    {"code": "STUDIO-002", "message": "시설을 찾을 수 없습니다."}""")
                    )
            )
    })
    StudioResponse findById(
            @Parameter(description = "시설 ID", required = true, example = "1")
            Long studioId
    );

    @Operation(
            summary = "시설 정보 수정",
            description = """
                    시설의 기본 정보를 수정한다. 대표 강사만 수정할 수 있다.
                    전달한 필드만 변경되고, 보내지 않은 필드는 기존 값을 유지한다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 값이 올바르지 않거나, 운영 시간이 잘못되었거나, API 버전 헤더가 없음",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "요청 값 오류",
                                            value = """
                                                    {"code": "COMMON-001", "message": "요청 값이 올바르지 않습니다."}"""
                                    ),
                                    @ExampleObject(
                                            name = "운영 시간 오류",
                                            value = """
                                                    {"code": "STUDIO-001", \
                                                    "message": "운영 종료 시간은 시작 시간보다 늦어야 합니다."}"""
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
            )
    })
    StudioResponse update(
            @Parameter(
                    description = "요청자 회원 ID. 인증 연동 전까지만 사용하는 임시 헤더다.",
                    required = true,
                    example = "1"
            )
            Long memberId,
            @Parameter(description = "시설 ID", required = true, example = "1")
            Long studioId,
            StudioUpdateRequest request
    );
}
