package com.classitda.classes.presentation;

import com.classitda.classes.presentation.dto.InstructorEnrollmentCreateRequest;
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
@Tag(name = "수업 회차 대리 예약", description = "시설 관리자가 시설에 등록된 회원을 수업 회차에 예약하거나 예약을 취소합니다.")
public interface InstructorEnrollmentControllerApi {

    @Operation(
            summary = "회원 대리 예약",
            description = """
                    - **대상**: 같은 시설의 활성 소속 회원만 예약할 수 있습니다.

                    - **수강권**: 이 단계에서는 수강권을 사용하지 않고 예약을 확정합니다. 수강권 차감은 별도 기능에서 다룹니다.

                    - **회차 상태**: 아직 시작하지 않았고 취소되지 않은 회차에만 예약할 수 있습니다. 회원 셀프 예약 마감 시각은 적용하지 않습니다.

                    - **정원**: 남은 자리가 없으면 예약할 수 없습니다. 대기 등록은 지원하지 않습니다.

                    - **권한**: 대표이거나 예약 관리 권한이 있어야 합니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "회원을 수업 회차에 예약합니다."
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 값이 올바르지 않거나 API 버전 헤더가 유효하지 않습니다.",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "요청 값 오류", value = """
                                            {"code":"COMMON-001","message":"요청 값이 올바르지 않습니다."}"""),
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
                    description = "시설의 활성 소속이 아니거나 예약 관리 권한이 없습니다.",
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
                    description = "시설, 수업 회차 또는 예약 대상 회원 소속을 찾을 수 없습니다. 다른 시설의 자원도 동일하게 처리합니다.",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "시설 없음", value = """
                                            {"code":"STUDIO-002","message":"시설을 찾을 수 없습니다."}"""),
                                    @ExampleObject(name = "수업 회차 없음", value = """
                                            {"code":"CLASS_SESSION-014","message":"수업 회차를 찾을 수 없습니다."}"""),
                                    @ExampleObject(name = "회원 소속 없음", value = """
                                            {"code":"CLASS_SESSION_ENROLLMENT-010","message":"예약할 회원 소속을 찾을 수 없습니다."}""")
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "회차가 예정 상태가 아니거나, 정원이 찼거나, 이미 신청한 회원입니다.",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "예정 상태 아님", value = """
                                            {"code":"CLASS_SESSION_ENROLLMENT-011","message":"예정 상태인 수업 회차에만 예약할 수 있습니다."}"""),
                                    @ExampleObject(name = "정원 초과", value = """
                                            {"code":"CLASS_SESSION_ENROLLMENT-012","message":"수업 회차의 정원이 모두 찼습니다."}"""),
                                    @ExampleObject(name = "중복 신청", value = """
                                            {"code":"CLASS_SESSION_ENROLLMENT-013","message":"이미 해당 수업 회차에 신청한 회원입니다."}""")
                            }
                    )
            )
    })
    ResponseEntity<Void> save(
            @Parameter(hidden = true) Long memberId,
            @Parameter(description = "시설 ID", example = "1") Long studioId,
            @Parameter(description = "수업 회차 ID", example = "10") Long classSessionId,
            InstructorEnrollmentCreateRequest request
    );

    @Operation(
            summary = "회원 대리 예약 취소",
            description = """
                    - **대상**: 예약 상태인 신청만 취소할 수 있습니다. 신청 이력은 삭제하지 않고 취소 상태로 남깁니다.

                    - **제한**: 출결이 기록된 예약은 취소할 수 없습니다. 회원 무료 취소 마감 시각은 적용하지 않습니다.

                    - **권한**: 대표이거나 예약 관리 권한이 있어야 합니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "예약을 취소 상태로 변경합니다."
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "API 버전 헤더가 유효하지 않습니다.",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
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
                    description = "시설의 활성 소속이 아니거나 예약 관리 권한이 없습니다.",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "소속 아님", value = """
                                            {"code":"MEMBERSHIP-001","message":"해당 시설의 소속이 아닙니다."}"""),
                                    @ExampleObject(name = "권한 없음", value = """
                                            {"code":"PERMISSION-001","message":"이 작업을 수행할 권한이 없습니다."}""")
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "시설 또는 수업 신청을 찾을 수 없습니다. 다른 시설이나 다른 회차의 신청도 동일하게 처리합니다.",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "시설 없음", value = """
                                            {"code":"STUDIO-002","message":"시설을 찾을 수 없습니다."}"""),
                                    @ExampleObject(name = "수업 신청 없음", value = """
                                            {"code":"CLASS_SESSION_ENROLLMENT-009","message":"수업 신청을 찾을 수 없습니다."}""")
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "예약 상태가 아니거나 출결이 기록되어 취소할 수 없습니다.",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "취소 불가 상태", value = """
                                    {"code":"CLASS_SESSION_ENROLLMENT-001","message":"현재 수업 신청 상태에서는 요청한 상태 전이를 수행할 수 없습니다."}""")
                    )
            )
    })
    ResponseEntity<Void> cancel(
            @Parameter(hidden = true) Long memberId,
            @Parameter(description = "시설 ID", example = "1") Long studioId,
            @Parameter(description = "수업 회차 ID", example = "10") Long classSessionId,
            @Parameter(description = "수업 신청 ID", example = "100") Long enrollmentId
    );
}
