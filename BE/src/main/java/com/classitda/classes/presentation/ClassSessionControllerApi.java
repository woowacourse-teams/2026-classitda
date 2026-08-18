package com.classitda.classes.presentation;

import com.classitda.classes.presentation.dto.ClassSessionCreateRequest;
import com.classitda.classes.presentation.dto.ClassSessionDetailResponse;
import com.classitda.classes.presentation.dto.InstructorCalendarListRequest;
import com.classitda.classes.presentation.dto.InstructorCalendarResponse;
import com.classitda.classes.presentation.dto.InstructorDailySessionListRequest;
import com.classitda.classes.presentation.dto.InstructorDailySessionResponse;
import com.classitda.classes.presentation.dto.MemberClassSessionListRequest;
import com.classitda.classes.presentation.dto.MemberClassSessionResponse;
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
@Tag(name = "수업 회차", description = "시설의 수업 회차를 등록하고 회원·강사용 수업 정보를 조회합니다.")
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

    @Operation(
            summary = "회원용 일별 수업 목록 조회",
            description = """
                    ### 조회 기준

                    - date에 시작하는 수업 회차를 시작 일시와 회차 ID 오름차순으로 조회합니다.
                    - 날짜와 현재 시각은 한국 시간(Asia/Seoul)을 기준으로 합니다.
                    - 하루 전체 타임라인을 반환하므로 페이지네이션을 적용하지 않으며, 수업이 없으면 빈 배열을 반환합니다.
                    - 같은 시설의 활성 학생 역할(STUDENT)만 조회할 수 있으며 대표, 강사, 직원 역할은 사용할 수 없습니다.

                    ### 수강권 기준

                    - memberPassProductId는 로그인 회원이 같은 시설에서 보유한 활성 수강권이어야 합니다.
                    - 선택한 수강권의 그룹·개인 형태와 수업 종류에 맞는 회차만 반환합니다.
                    - 발급 당시 조건을 별도로 저장하지 않으므로 현재 수강권 상품에 연결된 형태와 수업 종류를 기준으로 판단합니다.
                    - 조회 날짜가 수강권 이용 기간 밖이면 빈 배열을 반환합니다.

                    ### 예약 상태

                    - 예약 가능, 대기 가능, 예약 완료, 대기 중, 빈자리 예약 제안, 마감, 수업 완료, 수업 취소를 구분합니다.
                    - 본인의 예약·대기 상태는 일반 마감보다 우선하고, 수업 완료와 수업 취소는 본인 상태보다 우선합니다.
                    - 표시 상태와 잔여석은 조회 시점의 정보이며 실제 예약 요청에서 다시 검증합니다.

                    ### local Swagger 테스트 데이터

                    - 회원 ID: 1
                    - 시설 ID: 1
                    - 보유 수강권 ID: 42
                    - 조회 날짜: 로컬 애플리케이션을 시작한 날짜의 다음 날
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "선택한 수강권으로 이용할 수 있는 해당 날짜의 수업 목록을 반환합니다.",
                    content = @Content(array = @ArraySchema(
                            schema = @Schema(implementation = MemberClassSessionResponse.class)
                    ))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "날짜나 보유 수강권 ID가 올바르지 않거나 API 버전 헤더가 유효하지 않습니다.",
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
                    description = "시설 소속이 아니거나 소속이 비활성 상태이거나 학생 역할이 아닙니다.",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "소속 아님", value = """
                                            {"code":"MEMBERSHIP-001","message":"해당 시설의 소속이 아닙니다."}"""),
                                    @ExampleObject(name = "비활성 소속", value = """
                                            {"code":"MEMBERSHIP-002","message":"이용이 정지된 소속입니다."}"""),
                                    @ExampleObject(name = "학생 역할 아님", value = """
                                            {"code":"PERMISSION-001","message":"이 작업을 수행할 권한이 없습니다."}""")
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "시설, 운영 정책 또는 로그인 회원의 해당 보유 수강권을 찾을 수 없습니다.",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "시설 없음", value = """
                                            {"code":"STUDIO-002","message":"시설을 찾을 수 없습니다."}"""),
                                    @ExampleObject(name = "운영 정책 없음", value = """
                                            {"code":"POLICY-001","message":"운영 정책을 찾을 수 없습니다."}"""),
                                    @ExampleObject(name = "보유 수강권 없음", value = """
                                            {"code":"PASS_PRODUCT-010","message":"보유 수강권을 찾을 수 없습니다."}""")
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "선택한 보유 수강권이 홀딩·만료·소진 등으로 현재 사용할 수 없습니다.",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "사용할 수 없는 수강권", value = """
                                    {"code":"PASS_PRODUCT-011","message":"현재 사용할 수 없는 수강권입니다."}""")
                    )
            )
    })
    List<MemberClassSessionResponse> findAllForStudent(
            @Parameter(hidden = true)
            Long memberId,
            @Parameter(description = "대상 시설을 식별하는 ID입니다.", required = true, example = "1")
            Long studioId,
            @ParameterObject
            MemberClassSessionListRequest request
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
                    - 회차 101~106은 mine=true, 회차 107은 mine=false로 반환됩니다.
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

                    - SCHEDULED_OPEN 또는 SCHEDULED_CLOSED 수업이 하나 이상 있으면 scheduled가 true입니다.
                    - COMPLETED 수업이 하나 이상 있으면 completed가 true입니다.
                    - IN_PROGRESS와 CANCELED는 응답에서 제외합니다.
                    - mineScheduled와 mineCompleted는 요청자가 담당하는 수업이 하나 이상 있을 때 true입니다.

                    ### local Swagger 테스트 데이터

                    - 회원 ID: 3 (강사)
                    - 시설 ID: 1
                    - from: 로컬 애플리케이션을 시작한 날짜의 전날
                    - to: 로컬 애플리케이션을 시작한 날짜의 다음 날
                    - 전날은 completed=true, mineCompleted=true입니다.
                    - 다음 날은 scheduled=true, mineScheduled=true입니다.
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
            summary = "회원용 수업 회차 상세 조회",
            description = """
                    ### 조회 대상

                    - 회원에게 공개되는 수업 정보를 조회합니다.
                    - 시설 대표와 같은 시설의 활성 회원, 강사, 관리자가 조회할 수 있습니다.
                    - 회원, 강사, 관리자에게 동일한 수업 회차 정보를 반환합니다.

                    ### 응답 범위

                    - 담당 강사, 수업 종류, 수업명, 수업 안내, 정원, 진행 시간, 시작·종료 일시와 상태를 반환합니다.
                    - 예약 회원 목록과 예약·대기 인원은 포함하지 않습니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "회원에게 공개되는 수업 회차 상세 정보를 반환합니다.",
                    content = @Content(schema = @Schema(implementation = ClassSessionDetailResponse.class))
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
                    description = "시설 소속이 아니거나 소속이 비활성 상태입니다.",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "소속 아님", value = """
                                            {"code":"MEMBERSHIP-001","message":"해당 시설의 소속이 아닙니다."}"""),
                                    @ExampleObject(name = "비활성 소속", value = """
                                            {"code":"MEMBERSHIP-002","message":"이용이 정지된 소속입니다."}""")
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "시설이나 수업 회차를 찾을 수 없습니다. 다른 시설의 수업 회차도 동일하게 처리합니다.",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "시설 없음", value = """
                                            {"code":"STUDIO-002","message":"시설을 찾을 수 없습니다."}"""),
                                    @ExampleObject(name = "수업 회차 없음", value = """
                                            {"code":"CLASS_SESSION-014","message":"수업 회차를 찾을 수 없습니다."}""")
                            }
                    )
            )
    })
    ClassSessionDetailResponse findOne(
            @Parameter(hidden = true)
            Long memberId,
            @Parameter(description = "대상 시설을 식별하는 ID입니다.", required = true, example = "1")
            Long studioId,
            @Parameter(description = "조회할 수업 회차 ID입니다.", required = true, example = "11")
            Long classSessionId
    );
}
