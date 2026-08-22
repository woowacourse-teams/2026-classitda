package com.classitda.classes.presentation;

import com.classitda.classes.presentation.dto.StudentEnrollmentDetailResponse;
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

@SecurityRequirement(name = "bearerAuth")
@Tag(name = "학생 수업 신청", description = "학생 본인의 예약·대기·출결 생명주기를 조회합니다.")
public interface StudentEnrollmentControllerApi {

    @Operation(
            summary = "학생 신청 상세 조회",
            description = """
                    ### 조회 대상

                    - 로그인한 학생 본인의 특정 수업 신청 생명주기를 enrollmentId로 조회합니다.
                    - 같은 수업에 다시 신청해 종료된 이력이 여러 개여도 각 신청을 구분합니다.
                    - 시설의 활성 학생만 사용할 수 있으며 대표, 강사, 직원 역할은 사용할 수 없습니다.

                    ### 표시 상태

                    - RESERVED, WAITING, OFFERED, ATTENDED, ABSENT, RESERVATION_CANCELED, SESSION_CANCELED 중 하나를 반환합니다.
                    - 예약 취소와 수업 취소가 함께 존재하면 RESERVATION_CANCELED를 우선합니다.
                    - WAITING은 현재 유효 순번, OFFERED는 0번을 반환하며 나머지 상태의 waitingPosition은 null입니다.
                    - offerExpiresAt은 OFFERED에서만 반환합니다.

                    ### 숨긴 신청

                    - 수강권이 연결되지 않은 CANCELED 신청과 EXPIRED 신청은 상세에 노출하지 않습니다.
                    - 숨긴 신청, 다른 회원의 신청, 존재하지 않는 신청은 모두 같은 404를 반환합니다.

                    ### local Swagger 테스트 데이터

                    - 김회원(ID 1)의 ACCESS 토큰을 Swagger Authorize에 입력합니다.
                    - studioId는 1을 사용합니다.
                    - enrollmentId별 기대 결과는 다음과 같습니다.

                    | enrollmentId | 기대 결과 |
                    |---:|---|
                    | 4 | 예약 완료(RESERVED) |
                    | 16 | 대기 중(WAITING) |
                    | 19 | 승인 필요(OFFERED) |
                    | 6 | 출석 완료(ATTENDED) |
                    | 25 | 결석(ABSENT) |
                    | 22 | 예약 취소(RESERVATION_CANCELED) |
                    | 29 | 수업 취소(SESSION_CANCELED) |
                    | 23 | 대기 취소 이력이므로 404 |
                    | 24 | 제안 만료 이력이므로 404 |
                    | 1 | 다른 회원의 신청이므로 404 |
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "학생 본인의 신청 상세 정보를 반환합니다.",
                    content = @Content(schema = @Schema(implementation = StudentEnrollmentDetailResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "API 버전 헤더가 없거나 지원하지 않는 버전입니다.",
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
                    description = "시설의 활성 학생 소속이 아닙니다.",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "소속 아님", value = """
                                            {"code":"MEMBERSHIP-001","message":"해당 시설의 소속이 아닙니다."}"""),
                                    @ExampleObject(name = "비활성 소속", value = """
                                            {"code":"MEMBERSHIP-002","message":"이용이 정지된 소속입니다."}"""),
                                    @ExampleObject(name = "학생 권한 없음", value = """
                                            {"code":"PERMISSION-001","message":"이 작업을 수행할 권한이 없습니다."}""")
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "시설 또는 조회 가능한 본인 신청을 찾을 수 없습니다. 숨긴 신청과 다른 회원의 신청도 동일하게 처리합니다.",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "시설 없음", value = """
                                            {"code":"STUDIO-002","message":"시설을 찾을 수 없습니다."}"""),
                                    @ExampleObject(name = "신청 없음", value = """
                                            {"code":"CLASS_SESSION_ENROLLMENT-009","message":"수업 신청을 찾을 수 없습니다."}""")
                            }
                    )
            )
    })
    StudentEnrollmentDetailResponse findOne(
            @Parameter(hidden = true)
            Long memberId,
            @Parameter(description = "대상 시설을 식별하는 ID입니다.", required = true, example = "1")
            Long studioId,
            @Parameter(description = "조회할 수업 신청 ID입니다.", required = true, example = "19")
            Long enrollmentId
    );
}
