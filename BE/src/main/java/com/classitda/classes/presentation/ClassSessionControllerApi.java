package com.classitda.classes.presentation;

import com.classitda.classes.presentation.dto.ClassSessionCreateRequest;
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
@Tag(name = "수업 회차", description = "시설의 단일 또는 반복 수업 회차를 관리합니다.")
public interface ClassSessionControllerApi {

    @Operation(
            summary = "수업 회차 등록",
            description = """
                    - **단일 수업**: classDate에 한 회차를 생성합니다.

                    - **반복 수업**: 반복 기간 안에서 recurringDays에 해당하는 날짜마다 독립적인 회차를 생성합니다.

                    - **템플릿**: classTemplateId는 선택 값이며 템플릿의 시설 경계만 검증하고, 실제 회차에는 최종 요청 값을 저장합니다.

                    - **담당 강사**: instructorMembershipId로 같은 시설의 활성 강사 소속을 지정합니다.

                    - **권한**: 본인 수업 관리 권한이 있는 강사는 본인만 담당 강사로 지정할 수 있습니다. 대표 또는 전체 수업 관리 권한이 있는 사용자는 같은 시설의 다른 강사도 담당 강사로 지정할 수 있습니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "모든 수업 회차를 정상적으로 등록합니다."
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 값이나 반복 조건이 올바르지 않거나 API 버전 헤더가 유효하지 않습니다.",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "요청 값 오류", value = """
                                            {"code":"COMMON-001","message":"요청 값이 올바르지 않습니다."}"""),
                                    @ExampleObject(name = "반복 조건 오류", value = """
                                            {"code":"CLASS_SESSION-009","message":"반복 여부에 맞는 수업 일정 정보가 필요합니다."}"""),
                                    @ExampleObject(name = "반복 기간 오류", value = """
                                            {"code":"CLASS_SESSION-010","message":"올바른 반복 기간이 필요합니다."}"""),
                                    @ExampleObject(name = "반복 요일 오류", value = """
                                            {"code":"CLASS_SESSION-011","message":"반복 요일을 하나 이상 중복 없이 선택해야 합니다."}"""),
                                    @ExampleObject(name = "생성 날짜 없음", value = """
                                            {"code":"CLASS_SESSION-012","message":"생성할 수업 날짜가 없습니다."}"""),
                                    @ExampleObject(name = "버전 헤더 누락", value = """
                                            {"code":"API-001","message":"X-API-Version 헤더는 필수입니다."}"""),
                                    @ExampleObject(name = "지원하지 않는 버전", value = """
                                            {"code":"API-002","message":"지원하지 않는 API 버전입니다."}""")
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 정보가 없거나 유효하지 않습니다.",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "인증 실패", value = """
                                    {"code":"AUTH-001","message":"인증이 필요합니다."}""")
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "시설의 활성 소속이 아니거나 수업 회차 관리 권한이 없습니다.",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "소속 아님", value = """
                                            {"code":"MEMBERSHIP-001","message":"해당 시설의 소속이 아닙니다."}"""),
                                    @ExampleObject(name = "비활성 소속", value = """
                                            {"code":"MEMBERSHIP-002","message":"이용이 정지된 소속입니다."}"""),
                                    @ExampleObject(name = "권한 없음", value = """
                                            {"code":"PERMISSION-001","message":"이 작업을 수행할 권한이 없습니다."}""")
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "시설, 수업 템플릿 또는 수업 종류를 찾을 수 없거나, 담당 강사 지정 권한이 있는 요청에서 유효한 담당 강사 소속을 찾을 수 없습니다. 다른 시설의 자원도 동일하게 처리합니다.",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "시설 없음", value = """
                                            {"code":"STUDIO-002","message":"시설을 찾을 수 없습니다."}"""),
                                    @ExampleObject(name = "담당 강사 없음", value = """
                                            {"code":"CLASS_SESSION-017","message":"담당 가능한 강사 소속을 찾을 수 없습니다."}"""),
                                    @ExampleObject(name = "수업 템플릿 없음", value = """
                                            {"code":"CLASS_TEMPLATE-007","message":"수업 템플릿을 찾을 수 없습니다."}"""),
                                    @ExampleObject(name = "수업 종류 없음", value = """
                                            {"code":"CLASS_TYPE-003","message":"수업 종류를 찾을 수 없습니다."}""")
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "담당 강사의 기존 활성 수업과 시간이 겹칩니다.",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "수업 시간 충돌", value = """
                                    {"code":"CLASS_SESSION-015","message":"담당 강사의 기존 수업과 시간이 겹칩니다."}""")
                    )
            )
    })
    ResponseEntity<Void> save(
            @Parameter(hidden = true)
            Long memberId,
            @Parameter(description = "대상 시설을 식별하는 ID입니다.", required = true, example = "1")
            Long studioId,
            ClassSessionCreateRequest request
    );
}
