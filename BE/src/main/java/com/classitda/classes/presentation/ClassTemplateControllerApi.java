package com.classitda.classes.presentation;

import com.classitda.classes.presentation.dto.ClassTemplateCreateRequest;
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
@Tag(name = "수업 템플릿", description = "시설에 속한 수업 템플릿을 관리합니다.")
public interface ClassTemplateControllerApi {

    @Operation(
            summary = "수업 템플릿 등록",
            description = "시설에서 반복 사용하는 수업 정보를 템플릿으로 등록합니다. 수업 템플릿 관리 권한이 필요합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "수업 템플릿이 정상적으로 등록됩니다."
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 값이 올바르지 않거나 API 버전 헤더가 없거나 지원하지 않는 버전입니다.",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "요청 값 오류", value = """
                                            {"code": "COMMON-001", "message": "요청 값이 올바르지 않습니다."}"""),
                                    @ExampleObject(name = "버전 헤더 누락", value = """
                                            {"code": "API-001", "message": "X-API-Version 헤더는 필수입니다."}"""),
                                    @ExampleObject(name = "지원하지 않는 버전", value = """
                                            {"code": "API-002", "message": "지원하지 않는 API 버전입니다."}""")
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 정보가 없거나 유효하지 않습니다.",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "인증 실패", value = """
                                    {"code": "AUTH-001", "message": "인증이 필요합니다."}""")
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "ACCESS 토큰이 아니거나 시설 소속이 아니거나 소속이 비활성 상태이거나 권한이 없습니다.",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "접근 권한 없음", value = """
                                            {"code": "AUTH-002", "message": "접근 권한이 없습니다."}"""),
                                    @ExampleObject(name = "소속 아님", value = """
                                            {"code": "MEMBERSHIP-001", "message": "해당 시설의 소속이 아닙니다."}"""),
                                    @ExampleObject(name = "비활성 소속", value = """
                                            {"code": "MEMBERSHIP-002", "message": "이용이 정지된 소속입니다."}"""),
                                    @ExampleObject(name = "권한 없음", value = """
                                            {"code": "PERMISSION-001", "message": "이 작업을 수행할 권한이 없습니다."}""")
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "시설 또는 수업 종류를 찾을 수 없습니다. 다른 시설의 자원도 동일하게 처리합니다.",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "시설 없음", value = """
                                            {"code": "STUDIO-002", "message": "시설을 찾을 수 없습니다."}"""),
                                    @ExampleObject(name = "수업 종류 없음", value = """
                                            {"code": "CLASS_TYPE-003", "message": "수업 종류를 찾을 수 없습니다."}""")
                            }
                    )
            )
    })
    ResponseEntity<Void> save(
            @Parameter(hidden = true)
            Long memberId,
            @Parameter(description = "대상 시설을 식별하는 ID입니다.", required = true, example = "1")
            Long studioId,
            ClassTemplateCreateRequest request
    );
}
