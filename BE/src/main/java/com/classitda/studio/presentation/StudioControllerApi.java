package com.classitda.studio.presentation;

import com.classitda.common.exception.ErrorResponse;
import com.classitda.studio.presentation.dto.StudioCreateRequest;
import com.classitda.studio.presentation.dto.StudioResponse;
import com.classitda.studio.presentation.dto.StudioUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
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
                    description = "요청 값이 올바르지 않거나 운영 시간이 잘못됨",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
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
                    responseCode = "404",
                    description = "시설을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
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
                    description = "요청 값이 올바르지 않거나 운영 시간이 잘못됨",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "해당 시설의 대표 강사가 아님",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "시설을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
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
