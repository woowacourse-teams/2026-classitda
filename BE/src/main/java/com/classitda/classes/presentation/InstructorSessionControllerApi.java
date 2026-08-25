package com.classitda.classes.presentation;

import com.classitda.classes.presentation.dto.ClassSessionCreateV1Request;
import com.classitda.classes.presentation.dto.ClassSessionCreateV2Request;
import com.classitda.classes.presentation.dto.ClassSessionUpdateV1Request;
import com.classitda.classes.presentation.dto.ClassSessionUpdateV2Request;
import com.classitda.classes.presentation.dto.InstructorCalendarListRequest;
import com.classitda.classes.presentation.dto.InstructorCalendarResponse;
import com.classitda.classes.presentation.dto.InstructorDailySessionListRequest;
import com.classitda.classes.presentation.dto.InstructorDailySessionResponse;
import com.classitda.classes.presentation.dto.InstructorEnrollmentCandidateResponse;
import com.classitda.classes.presentation.dto.InstructorEnrollmentCreateRequest;
import com.classitda.classes.presentation.dto.InstructorSessionDetailResponse;
import com.classitda.common.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springdoc.core.annotations.ParameterObject;

@SecurityRequirement(name = "bearerAuth")
@Tag(name = "강사용 수업", description = "강사용 수업을 등록·수정·취소하고 일정·상세·예약 회원을 관리합니다.")
public interface InstructorSessionControllerApi {

    @Operation(
            summary = "수업 회차 등록(v1)",
            description = """
                    MVP용 수업 회차 등록 API입니다.

                    - **API 버전**: `X-API-Version` 헤더에 `1`을 전달합니다.
                    - 인증된 회원 본인을 담당 강사로 지정합니다.
                    - 요청에 담당 강사 소속 ID를 받지 않습니다.
                    - 단일 수업과 반복 수업을 등록할 수 있습니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "모든 수업 회차를 정상적으로 등록합니다."),
            @ApiResponse(responseCode = "400", description = "요청 값이나 반복 조건이 올바르지 않습니다."),
            @ApiResponse(responseCode = "401", description = "인증 정보가 없거나 유효하지 않습니다."),
            @ApiResponse(responseCode = "403", description = "활성 소속이 아니거나 관리 권한이 없습니다."),
            @ApiResponse(responseCode = "404", description = "시설, 본인의 강사 소속 또는 수업 종류를 찾을 수 없습니다."),
            @ApiResponse(responseCode = "409", description = "본인의 기존 활성 수업과 시간이 겹칩니다.")
    })
    ResponseEntity<Void> saveV1(
            @Parameter(hidden = true)
            Long memberId,
            @Parameter(description = "대상 시설을 식별하는 ID입니다.", required = true, example = "1")
            Long studioId,
            ClassSessionCreateV1Request request
    );

    @Operation(
            hidden = true,
            summary = "수업 회차 등록(v2)",
            description = """
                    - **API 버전**: `X-API-Version` 헤더에 `2`를 전달합니다.

                    - **단일 수업**: classDate에 한 회차를 생성합니다.

                    - **반복 수업**: 반복 기간 안에서 recurringDays에 해당하는 날짜마다 독립적인 회차를 생성합니다.

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
                    description = "시설 또는 수업 종류를 찾을 수 없거나, 담당 강사 지정 권한이 있는 요청에서 유효한 담당 강사 소속을 찾을 수 없습니다. 다른 시설의 자원도 동일하게 처리합니다.",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "시설 없음", value = """
                                            {"code":"STUDIO-002","message":"시설을 찾을 수 없습니다."}"""),
                                    @ExampleObject(name = "담당 강사 없음", value = """
                                            {"code":"CLASS_SESSION-017","message":"담당 가능한 강사 소속을 찾을 수 없습니다."}"""),
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
    ResponseEntity<Void> saveV2(
            @Parameter(hidden = true)
            Long memberId,
            @Parameter(description = "대상 시설을 식별하는 ID입니다.", required = true, example = "1")
            Long studioId,
            ClassSessionCreateV2Request request
    );

    @Operation(
            summary = "수업 회차 수정(v1)",
            description = """
                    MVP용 수업 회차 수정 API입니다.

                    - **API 버전**: `X-API-Version` 헤더에 `1`을 전달합니다.
                    - 생성된 수업 회차 한 건의 수정 가능한 정보를 요청 값으로 교체합니다.

                    - 반복 등록으로 생성된 다른 회차에는 변경 사항을 전파하지 않습니다.
                    - description을 제외한 모든 필드는 필수입니다.
                    - description은 null을 전달하면 기존 안내를 삭제합니다.
                    - 담당 강사는 변경하지 않습니다.
                    - 변경된 시작 일시와 진행 시간을 기준으로 종료 일시를 다시 계산합니다.
                    - 본인 수업 관리 권한자는 본인이 담당하는 회차만 수정할 수 있습니다.
                    - 대표 또는 전체 수업 관리 권한자는 시설의 모든 회차를 수정할 수 있습니다.
                    - 취소된 회차와 담당 강사의 다른 활성 수업에 시간 충돌이 생기는 변경은 거부합니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "수업 회차를 정상적으로 수정합니다."
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 값이나 API 버전 헤더가 올바르지 않습니다.",
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
                    description = "ACCESS 토큰이 아니거나 시설의 활성 소속이 아니거나 수업 회차 관리 권한이 없습니다.",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "접근 권한 없음", value = """
                                            {"code":"AUTH-002","message":"접근 권한이 없습니다."}"""),
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
                    description = "시설, 수업 회차 또는 수업 종류를 찾을 수 없습니다. 다른 시설의 자원도 동일하게 처리합니다.",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "시설 없음", value = """
                                            {"code":"STUDIO-002","message":"시설을 찾을 수 없습니다."}"""),
                                    @ExampleObject(name = "수업 회차 없음", value = """
                                            {"code":"CLASS_SESSION-014","message":"수업 회차를 찾을 수 없습니다."}"""),
                                    @ExampleObject(name = "수업 종류 없음", value = """
                                            {"code":"CLASS_TYPE-003","message":"수업 종류를 찾을 수 없습니다."}""")
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "취소된 회차이거나 담당 강사의 다른 활성 수업과 시간이 겹칩니다.",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "수업 시간 충돌", value = """
                                            {"code":"CLASS_SESSION-015","message":"담당 강사의 기존 수업과 시간이 겹칩니다."}"""),
                                    @ExampleObject(name = "취소된 수업", value = """
                                            {"code":"CLASS_SESSION-016","message":"취소된 수업은 수정할 수 없습니다."}""")
                            }
                    )
            )
    })
    ResponseEntity<Void> updateV1(
            @Parameter(hidden = true)
            Long memberId,
            @Parameter(description = "대상 시설을 식별하는 ID입니다.", required = true, example = "1")
            Long studioId,
            @Parameter(description = "수정할 수업 회차 ID입니다.", required = true, example = "101")
            Long classSessionId,
            ClassSessionUpdateV1Request request
    );

    @Operation(
            hidden = true,
            summary = "수업 회차 수정(v2)",
            description = """
                    - **API 버전**: `X-API-Version` 헤더에 `2`를 전달합니다.
                    - instructorMembershipId로 같은 시설의 활성 강사를 새 담당 강사로 지정합니다.
                    - 본인 수업 관리 권한자는 담당 강사를 변경할 수 없습니다.
                    - 대표 또는 전체 수업 관리 권한자는 담당 강사를 변경할 수 있습니다.
                    - 변경할 강사의 다른 활성 수업과 시간이 겹치면 요청을 거부합니다.
                    - 그 외 수정 규칙은 v1과 같습니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "수업 회차와 담당 강사를 정상적으로 수정합니다."),
            @ApiResponse(responseCode = "400", description = "요청 값이나 API 버전 헤더가 올바르지 않습니다."),
            @ApiResponse(responseCode = "401", description = "인증 정보가 없거나 유효하지 않습니다."),
            @ApiResponse(responseCode = "403", description = "시설의 활성 소속이 아니거나 담당 강사를 변경할 권한이 없습니다."),
            @ApiResponse(responseCode = "404", description = "시설, 수업 회차, 수업 종류 또는 활성 담당 강사를 찾을 수 없습니다."),
            @ApiResponse(responseCode = "409", description = "취소된 회차이거나 변경할 강사의 다른 활성 수업과 시간이 겹칩니다.")
    })
    ResponseEntity<Void> updateV2(
            @Parameter(hidden = true)
            Long memberId,
            @Parameter(description = "대상 시설을 식별하는 ID입니다.", required = true, example = "1")
            Long studioId,
            @Parameter(description = "수정할 수업 회차 ID입니다.", required = true, example = "101")
            Long classSessionId,
            ClassSessionUpdateV2Request request
    );

    @Operation(
            summary = "수업 회차 취소",
            description = """
                    수업 회차를 물리적으로 삭제하지 않고 취소 시각을 기록합니다.

                    - 수업 시작 전까지만 취소할 수 있습니다.
                    - 이미 취소된 회차를 다시 취소하면 요청을 거부합니다.
                    - 수업 신청과 출결 이력은 유지합니다.
                    - 이번 API에서는 신청·대기 상태를 일괄 변경하거나 예약 회원의 수강권 횟수를 복구하지 않습니다.
                    - 본인 수업 관리 권한자는 본인이 담당하는 회차만 취소할 수 있습니다.
                    - 대표 또는 전체 수업 관리 권한자는 시설의 모든 회차를 취소할 수 있습니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "수업 회차를 정상적으로 취소합니다."
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "API 버전 헤더가 올바르지 않습니다.",
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
                    description = "ACCESS 토큰이 아니거나 시설의 활성 소속이 아니거나 수업 회차 관리 권한이 없습니다.",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "접근 권한 없음", value = """
                                            {"code":"AUTH-002","message":"접근 권한이 없습니다."}"""),
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
                    description = "시설 또는 수업 회차를 찾을 수 없습니다. 다른 시설의 회차도 동일하게 처리합니다.",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "시설 없음", value = """
                                            {"code":"STUDIO-002","message":"시설을 찾을 수 없습니다."}"""),
                                    @ExampleObject(name = "수업 회차 없음", value = """
                                            {"code":"CLASS_SESSION-014","message":"수업 회차를 찾을 수 없습니다."}""")
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "이미 취소된 회차이거나 수업 시작 시각에 도달했습니다.",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "이미 취소된 수업", value = """
                                            {"code":"CLASS_SESSION-020","message":"이미 취소된 수업입니다."}"""),
                                    @ExampleObject(name = "이미 시작된 수업", value = """
                                            {"code":"CLASS_SESSION-021","message":"이미 시작된 수업은 취소할 수 없습니다."}""")
                            }
                    )
            )
    })
    ResponseEntity<Void> cancel(
            @Parameter(hidden = true)
            Long memberId,
            @Parameter(description = "대상 시설을 식별하는 ID입니다.", required = true, example = "1")
            Long studioId,
            @Parameter(description = "취소할 수업 회차 ID입니다.", required = true, example = "101")
            Long classSessionId
    );

    @Operation(
            summary = "강사용 일별 수업 목록 조회",
            description = """
                    ### 조회 기준

                    - date에 시작하는 시설 전체 수업 회차를 시작 일시와 회차 ID 오름차순으로 조회합니다.
                    - 날짜와 현재 시각은 한국 시간(Asia/Seoul)을 기준으로 합니다.
                    - 하루 전체 타임라인을 반환하므로 페이지네이션을 적용하지 않으며, 수업이 없으면 빈 배열을 반환합니다.

                    ### 조회 권한

                    - 같은 시설의 활성 강사, 대표, 본인 수업 관리 권한자, 전체 수업 관리 권한자가 조회할 수 있습니다.
                    - 학생, 권한이 없는 직원, 비활성 소속은 조회할 수 없습니다.

                    ### 응답 범위

                    - 담당 강사, 수업 종류, 예약·대기 인원, 수업 상태를 반환합니다.
                    - mine은 요청자가 담당하는 수업이면 true입니다.
                    - 수업 상태와 예약·대기 인원은 조회 시점의 정보입니다.

                    ### local Swagger 테스트 데이터

                    - 회원 ID: 3 (강사)
                    - 시설 ID: 1
                    - 조회 날짜: 로컬 애플리케이션을 시작한 날짜의 다음 날
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "해당 날짜에 시작하는 시설 전체 수업 목록을 반환합니다.",
                    content = @Content(array = @ArraySchema(
                            schema = @Schema(implementation = InstructorDailySessionResponse.class)
                    ))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "날짜가 올바르지 않거나 API 버전 헤더가 유효하지 않습니다.",
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
                    description = "시설 소속이 아니거나 소속이 비활성 상태이거나 수업 조회 권한이 없습니다.",
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
                    description = "시설을 찾을 수 없습니다.",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "시설 없음", value = """
                                    {"code":"STUDIO-002","message":"시설을 찾을 수 없습니다."}""")
                    )
            )
    })
    List<InstructorDailySessionResponse> findAllDailyForInstructor(
            @Parameter(hidden = true)
            Long memberId,
            @Parameter(description = "대상 시설을 식별하는 ID입니다.", required = true, example = "1")
            Long studioId,
            @ParameterObject
            InstructorDailySessionListRequest request
    );

    @Operation(
            summary = "강사용 수업 달력 조회",
            description = """
                    ### 조회 기준

                    - from과 to를 포함한 기간의 수업을 날짜 오름차순으로 집계합니다.
                    - 조회 기간은 최대 42일이며 페이지네이션을 적용하지 않습니다.
                    - 예정 또는 완료 수업이 없는 날짜는 응답에서 생략합니다.
                    - 날짜와 현재 시각은 한국 시간(Asia/Seoul)을 기준으로 합니다.

                    ### 조회 권한

                    - 같은 시설의 활성 강사, 대표, 본인 수업 관리 권한자, 전체 수업 관리 권한자가 조회할 수 있습니다.
                    - 학생, 권한이 없는 직원, 비활성 소속은 조회할 수 없습니다.

                    ### 집계 기준

                    - SCHEDULED_BOOKING_OPEN 또는 SCHEDULED_BOOKING_CLOSED 수업이 하나 이상 있으면 scheduled가 true입니다.
                    - COMPLETED 수업이 하나 이상 있으면 completed가 true입니다.
                    - IN_PROGRESS와 CANCELED는 응답에서 제외합니다.
                    - mineScheduled와 mineCompleted는 요청자가 담당하는 수업이 하나 이상 있을 때 true입니다.

                    ### local Swagger 테스트 데이터

                    - 회원 ID: 3 (강사)
                    - 시설 ID: 1
                    - from: 로컬 애플리케이션을 시작한 날짜의 전날
                    - to: 로컬 애플리케이션을 시작한 날짜의 다음 날
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "예정 또는 완료 수업이 있는 날짜별 존재 여부를 반환합니다.",
                    content = @Content(array = @ArraySchema(
                            schema = @Schema(implementation = InstructorCalendarResponse.class)
                    ))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "조회 기간이 올바르지 않거나 API 버전 헤더가 유효하지 않습니다.",
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
                    description = "시설 소속이 아니거나 소속이 비활성 상태이거나 수업 조회 권한이 없습니다.",
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
                    description = "시설 또는 운영 정책을 찾을 수 없습니다.",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "시설 없음", value = """
                                            {"code":"STUDIO-002","message":"시설을 찾을 수 없습니다."}"""),
                                    @ExampleObject(name = "운영 정책 없음", value = """
                                            {"code":"POLICY-001","message":"운영 정책을 찾을 수 없습니다."}""")
                            }
                    )
            )
    })
    List<InstructorCalendarResponse> findAllCalendarForInstructor(
            @Parameter(hidden = true)
            Long memberId,
            @Parameter(description = "대상 시설을 식별하는 ID입니다.", required = true, example = "1")
            Long studioId,
            @ParameterObject
            InstructorCalendarListRequest request
    );

    @Operation(
            summary = "강사용 수업 상세와 예약 회원 명단 조회",
            description = """
                    - **명단 범위**: 현재 `RESERVED` 상태인 회원만 반환합니다.

                    - **정렬**: 예약 시각 오름차순, 신청 ID 오름차순으로 반환합니다.

                    - **권한**: 대표 또는 전체 수업 관리 권한자는 모든 수업을 조회할 수 있습니다. 본인 수업 관리 권한자는 담당 수업만 조회할 수 있습니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "수업 상세와 예약 확정 회원 명단을 반환합니다.",
                    content = @Content(
                            schema = @Schema(implementation = InstructorSessionDetailResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "id":10,
                                      "instructorMembershipId":12,
                                      "instructorName":"이지은 강사",
                                      "classForm":"GROUP",
                                      "classType":{"id":3,"name":"리포머"},
                                      "className":"리포머 밸런스",
                                      "description":"체어룸에서 진행합니다.",
                                      "capacity":8,
                                      "reservedCount":1,
                                      "startAt":"2026-08-17T12:00:00",
                                      "endAt":"2026-08-17T13:00:00",
                                      "status":"SCHEDULED_BOOKING_OPEN",
                                      "mine":true,
                                      "reservedMembers":[{
                                        "enrollmentId":101,
                                        "membershipId":31,
                                        "name":"김민지",
                                        "profileImageUrl":"https://images.example.com/minji.png"
                                      }]
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "API 버전 헤더가 유효하지 않습니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 정보가 없거나 유효하지 않습니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "활성 소속이 아니거나 예약 조회 권한이 없습니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "시설 또는 조회 가능한 수업 회차를 찾을 수 없습니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    InstructorSessionDetailResponse findOne(
            @Parameter(hidden = true) Long memberId,
            @Parameter(description = "시설 ID", example = "1") Long studioId,
            @Parameter(description = "수업 회차 ID", example = "10") Long classSessionId
    );

    @Operation(
            summary = "회원 대리 예약 후보 전체 조회",
            description = """
                    - **대상**: 같은 시설에 현재 활성 상태로 등록된 학생 회원을 모두 반환합니다. 이미 이 수업을 예약한 회원도 포함합니다.

                    - **정렬**: 시설 회원 소속 ID 오름차순으로 반환합니다.

                    - **검색·페이지네이션**: 제공하지 않습니다. 이름 검색과 예약 여부 표시는 클라이언트에서 처리합니다.

                    - **권한**: 대표는 모든 수업을 관리할 수 있습니다. 그 외에는 예약 관리 권한과 본인 또는 전체 수업 관리 권한이 필요합니다.

                    - **로컬 테스트 데이터**
                      1. 회원 `3`: 이강사, `OWN` 권한
                      2. 수업 `101~106`, `108`, `110~123`: 이강사 담당
                      3. 수업 `107`, `109`: 박대표 담당
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "대리 예약 후보 회원 전체를 반환합니다.",
                    content = @Content(
                            array = @ArraySchema(schema = @Schema(
                                    implementation = InstructorEnrollmentCandidateResponse.class
                            )),
                            examples = @ExampleObject(value = """
                                    [{
                                      "membershipId":31,
                                      "name":"김민지",
                                      "profileImageUrl":"https://images.example.com/minji.png"
                                    },{
                                      "membershipId":32,
                                      "name":"최유진",
                                      "profileImageUrl":null
                                    }]
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "API 버전 헤더가 유효하지 않습니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 정보가 없거나 유효하지 않습니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "활성 소속이 아니거나 예약·수업 관리 권한이 없습니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "시설 또는 조회 가능한 수업 회차를 찾을 수 없습니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    List<InstructorEnrollmentCandidateResponse> findAllEnrollmentCandidates(
            @Parameter(hidden = true) Long memberId,
            @Parameter(description = "시설 ID", example = "1") Long studioId,
            @Parameter(description = "수업 회차 ID", example = "10") Long classSessionId
    );

    @Operation(
            summary = "회원 대리 예약",
            description = """
                    - **대상**: 같은 시설의 활성 소속 회원만 예약할 수 있습니다.

                    - **수강권**: 이 단계에서는 수강권을 사용하지 않고 예약을 확정합니다. 수강권 차감은 별도 기능에서 다룹니다.

                    - **회차 상태**: 아직 시작하지 않았고 취소되지 않은 회차에만 예약할 수 있습니다. 회원 셀프 예약 마감 시각은 적용하지 않습니다.

                    - **정원**: 남은 자리가 없으면 예약할 수 없습니다. 대기 등록은 지원하지 않습니다.

                    - **권한**: 대표는 모든 수업을 관리할 수 있습니다. 그 외에는 예약 관리 권한과 본인 또는 전체 수업 관리 권한이 필요합니다.
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
                    description = "시설의 활성 소속이 아니거나 예약·수업 관리 권한이 없습니다.",
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

                    - **권한**: 대표는 모든 수업을 관리할 수 있습니다. 그 외에는 예약 관리 권한과 본인 또는 전체 수업 관리 권한이 필요합니다.
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
                    description = "시설의 활성 소속이 아니거나 예약·수업 관리 권한이 없습니다.",
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
