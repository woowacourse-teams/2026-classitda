package com.classitda.studio.presentation;

import com.classitda.common.exception.ErrorResponse;
import com.classitda.common.pagination.CursorResponse;
import com.classitda.studio.presentation.dto.StudioMembershipCreateRequest;
import com.classitda.studio.presentation.dto.StudioMembershipResponse;
import com.classitda.studio.presentation.dto.StudioMembershipUpdateRequest;
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
@Tag(name = "시설 소속", description = "시설에 속한 회원·강사 관리 API")
public interface StudioMembershipControllerApi {

    @Operation(
            summary = "회원 등록",
            description = "이름과 전화번호로 회원(수강생)을 시설에 등록한다. 회원 관리 권한(MEMBER_INVITE)이 필요하다. "
                    + "**회원가입 여부와 무관하게 등록할 수 있다** — 가입하지 않은 사람은 이름과 전화번호만으로 회원 정보를 만들고, "
                    + "나중에 그 번호로 가입하면 이 시설에 이미 등록된 상태가 된다. "
                    + "이름은 시설이 참고하는 용도이며 회원 본인의 이름과 별개로 관리된다. "
                    + "역할은 서버가 회원(STUDENT)으로 고정하므로 요청이 지정하지 않는다. "
                    + "강사를 등록하려면 `POST /api/studios/{studioId}/memberships/instructors` 를 쓴다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "회원 등록 성공. 응답 본문이 없다"),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 값이 올바르지 않거나 API 버전 헤더가 없거나 지원하지 않는 버전임",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "요청 값 오류", value = """
                                            {"code": "COMMON-001", "message": "요청 값이 올바르지 않습니다."}"""),
                                    @ExampleObject(name = "소속 이름 오류", value = """
                                            {"code": "MEMBERSHIP-003", "message": "소속 이름은 1자 이상 50자 이하여야 합니다."}"""),
                                    @ExampleObject(name = "버전 헤더 누락", value = """
                                            {"code": "API-001", "message": "X-API-Version 헤더는 필수입니다."}"""),
                                    @ExampleObject(name = "지원하지 않는 버전", value = """
                                            {"code": "API-002", "message": "지원하지 않는 API 버전입니다."}""")
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 정보가 없거나 유효하지 않음",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "인증 실패", value = """
                                    {"code": "AUTH-001", "message": "인증이 필요합니다."}""")
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "ACCESS 토큰이 아니거나 시설 소속이 아니거나 소속이 비활성 상태이거나 권한이 없음",
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
                    description = "시설 또는 시설의 회원 역할을 찾을 수 없음",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "시설 없음", value = """
                                            {"code": "STUDIO-002", "message": "시설을 찾을 수 없습니다."}"""),
                                    @ExampleObject(name = "역할 없음", value = """
                                            {"code": "ROLE-001", "message": "시설 역할을 찾을 수 없습니다."}""")
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "이미 시설에 등록된 회원",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "소속 중복", value = """
                                    {"code": "MEMBERSHIP-004", "message": "이미 시설에 등록된 전화번호입니다."}""")
                    )
            )
    })
    ResponseEntity<Void> saveStudent(
            @Parameter(hidden = true)
            Long memberId,
            @Parameter(description = "시설 ID", required = true, example = "1")
            Long studioId,
            StudioMembershipCreateRequest request
    );

    @Operation(
            summary = "강사 등록",
            description = "이름과 전화번호로 강사를 시설에 등록한다. "
                    + "회원 관리 권한(MEMBER_INVITE)과 역할 관리 권한(ROLE_MANAGE)이 **모두** 필요하다 — "
                    + "강사 역할에는 수업·예약 관리 권한이 딸려 있어 초대 권한만으로 부여할 수 없다. "
                    + "**회원가입 여부와 무관하게 등록할 수 있다** — 가입하지 않은 사람은 이름과 전화번호만으로 회원 정보를 만들고, "
                    + "나중에 그 번호로 가입하면 이 시설에 이미 등록된 상태가 된다. "
                    + "이름은 시설이 참고하는 용도이며 회원 본인의 이름과 별개로 관리된다. "
                    + "역할은 서버가 일반 강사(INSTRUCTOR)로 고정하므로 요청이 지정하지 않는다. "
                    + "대표 강사는 시설당 한 명이며 시설 생성 시에만 만들어지므로 이 API 로 만들 수 없다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "강사 등록 성공. 응답 본문이 없다"),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 값이 올바르지 않거나 API 버전 헤더가 없거나 지원하지 않는 버전임",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "요청 값 오류", value = """
                                            {"code": "COMMON-001", "message": "요청 값이 올바르지 않습니다."}"""),
                                    @ExampleObject(name = "소속 이름 오류", value = """
                                            {"code": "MEMBERSHIP-003", "message": "소속 이름은 1자 이상 50자 이하여야 합니다."}"""),
                                    @ExampleObject(name = "버전 헤더 누락", value = """
                                            {"code": "API-001", "message": "X-API-Version 헤더는 필수입니다."}"""),
                                    @ExampleObject(name = "지원하지 않는 버전", value = """
                                            {"code": "API-002", "message": "지원하지 않는 API 버전입니다."}""")
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 정보가 없거나 유효하지 않음",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "인증 실패", value = """
                                    {"code": "AUTH-001", "message": "인증이 필요합니다."}""")
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "ACCESS 토큰이 아니거나 시설 소속이 아니거나 소속이 비활성 상태이거나 권한이 없음",
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
                    description = "시설 또는 시설의 강사 역할을 찾을 수 없음",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "시설 없음", value = """
                                            {"code": "STUDIO-002", "message": "시설을 찾을 수 없습니다."}"""),
                                    @ExampleObject(name = "역할 없음", value = """
                                            {"code": "ROLE-001", "message": "시설 역할을 찾을 수 없습니다."}""")
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "이미 시설에 등록된 회원",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "소속 중복", value = """
                                    {"code": "MEMBERSHIP-004", "message": "이미 시설에 등록된 회원입니다."}""")
                    )
            )
    })
    ResponseEntity<Void> saveInstructor(
            @Parameter(hidden = true)
            Long memberId,
            @Parameter(description = "시설 ID", required = true, example = "1")
            Long studioId,
            StudioMembershipCreateRequest request
    );

    @Operation(
            summary = "회원 목록 조회",
            description = "시설에 속한 회원(수강생)을 id 오름차순 커서 페이지네이션으로 조회한다. 회원 조회 권한(MEMBER_READ)이 필요하다. "
                    + "강사는 포함되지 않는다 — `GET /api/studios/{studioId}/memberships/instructors` 를 쓴다. "
                    + "응답의 registered 는 앱 가입 여부다 — false 면 시설이 등록만 해둔 회원이라 예약을 대신 잡아줘야 한다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "회원 목록 조회 성공. 마지막 페이지면 hasNext 는 false, nextCursor 는 null",
                    content = @Content(schema = @Schema(implementation = CursorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "커서나 조회 개수가 올바르지 않거나 API 버전 헤더가 없거나 지원하지 않는 버전임",
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
                    description = "인증 정보가 없거나 유효하지 않음",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "인증 실패", value = """
                                    {"code": "AUTH-001", "message": "인증이 필요합니다."}""")
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "ACCESS 토큰이 아니거나 시설 소속이 아니거나 소속이 비활성 상태이거나 권한이 없음",
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
                    description = "시설을 찾을 수 없음",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "시설 없음", value = """
                                    {"code": "STUDIO-002", "message": "시설을 찾을 수 없습니다."}""")
                    )
            )
    })
    CursorResponse<StudioMembershipResponse> findStudentsWithCursor(
            @Parameter(hidden = true)
            Long memberId,
            @Parameter(description = "시설 ID", required = true, example = "1")
            Long studioId,
            @Parameter(description = "직전 페이지의 nextCursor. 첫 페이지는 생략한다", example = "20")
            String cursor,
            @Parameter(description = "조회 개수 (1~100)", example = "10")
            int size
    );

    @Operation(
            summary = "강사 목록 조회",
            description = "시설에 속한 강사를 id 오름차순 커서 페이지네이션으로 조회한다. 회원 조회 권한(MEMBER_READ)이 필요하다. "
                    + "대표 강사도 포함된다. 수강생은 `GET /api/studios/{studioId}/memberships/students` 를 쓴다. "
                    + "응답의 registered 는 앱 가입 여부다 — false 면 시설이 등록만 해둔 회원이라 예약을 대신 잡아줘야 한다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "강사 목록 조회 성공. 마지막 페이지면 hasNext 는 false, nextCursor 는 null",
                    content = @Content(schema = @Schema(implementation = CursorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "커서나 조회 개수가 올바르지 않거나 API 버전 헤더가 없거나 지원하지 않는 버전임",
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
                    description = "인증 정보가 없거나 유효하지 않음",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "인증 실패", value = """
                                    {"code": "AUTH-001", "message": "인증이 필요합니다."}""")
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "ACCESS 토큰이 아니거나 시설 소속이 아니거나 소속이 비활성 상태이거나 권한이 없음",
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
                    description = "시설을 찾을 수 없음",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "시설 없음", value = """
                                    {"code": "STUDIO-002", "message": "시설을 찾을 수 없습니다."}""")
                    )
            )
    })
    CursorResponse<StudioMembershipResponse> findInstructorsWithCursor(
            @Parameter(hidden = true)
            Long memberId,
            @Parameter(description = "시설 ID", required = true, example = "1")
            Long studioId,
            @Parameter(description = "직전 페이지의 nextCursor. 첫 페이지는 생략한다", example = "20")
            String cursor,
            @Parameter(description = "조회 개수 (1~100)", example = "10")
            int size
    );

    @Operation(
            summary = "시설 소속 단건 조회",
            description = "시설에 속한 회원 또는 강사를 조회한다. 회원 조회 권한(MEMBER_READ)이 필요하다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "소속 단건 조회 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = "API 버전 헤더가 없거나 지원하지 않는 버전임",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "버전 헤더 누락", value = """
                                            {"code": "API-001", "message": "X-API-Version 헤더는 필수입니다."}"""),
                                    @ExampleObject(name = "지원하지 않는 버전", value = """
                                            {"code": "API-002", "message": "지원하지 않는 API 버전입니다."}""")
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 정보가 없거나 유효하지 않음",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "인증 실패", value = """
                                    {"code": "AUTH-001", "message": "인증이 필요합니다."}""")
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "ACCESS 토큰이 아니거나 시설 소속이 아니거나 소속이 비활성 상태이거나 권한이 없음",
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
                    description = "시설 또는 해당 시설의 소속을 찾을 수 없음",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "시설 없음", value = """
                                            {"code": "STUDIO-002", "message": "시설을 찾을 수 없습니다."}"""),
                                    @ExampleObject(name = "소속 없음", value = """
                                            {"code": "MEMBERSHIP-005", "message": "시설 소속을 찾을 수 없습니다."}""")
                            }
                    )
            )
    })
    StudioMembershipResponse findById(
            @Parameter(hidden = true)
            Long memberId,
            @Parameter(description = "시설 ID", required = true, example = "1")
            Long studioId,
            @Parameter(description = "시설 소속 ID", required = true, example = "1")
            Long membershipId
    );
    @Operation(
            summary = "시설 회원 정보 수정",
            description = """
                    ### 수정 범위

                    - **시설이 관리하는 이름과 연락처만 바꿉니다.** 회원 계정의 이름과 번호는 바뀌지 않습니다.
                    - 이름은 시설 안에서 부르는 호칭입니다. 시설마다 다르게 둘 수 있습니다.

                    ### 연락처 제한

                    - **회원이 이미 가입했다면 연락처를 바꿀 수 없습니다.** 가입할 때 본인 인증을 거친 번호이기 때문입니다.
                    - 아직 가입하지 않은 회원은 연락처를 바꿀 수 있습니다. 등록할 때 잘못 입력한 번호를 고치는 용도입니다.
                    - 같은 시설에 이미 등록된 번호로는 바꿀 수 없습니다.

                    ### 권한

                    - 회원 관리 권한이 필요합니다.
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "수정 성공", content = @Content),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 값이 올바르지 않음",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"code":"COMMON-001","message":"요청 값이 올바르지 않습니다."}""")
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "회원 관리 권한이 없음",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"code":"PERMISSION-001","message":"이 작업을 수행할 권한이 없습니다."}""")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "시설 또는 소속을 찾을 수 없음",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"code":"MEMBERSHIP-005","message":"시설 소속을 찾을 수 없습니다."}""")
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "가입한 회원의 번호를 바꾸려 하거나 이미 등록된 번호",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "번호 수정 불가", value = """
                                            {"code":"MEMBERSHIP-007","message":"가입한 회원의 휴대전화 번호는 수정할 수 없습니다."}"""),
                                    @ExampleObject(name = "번호 중복", value = """
                                            {"code":"MEMBERSHIP-004","message":"이미 시설에 등록된 회원입니다."}""")
                            }
                    )
            )
    })
    ResponseEntity<Void> update(
            @Parameter(hidden = true) Long memberId,
            @Parameter(description = "시설 ID", required = true, example = "1") Long studioId,
            @Parameter(description = "소속 ID", required = true, example = "1") Long membershipId,
            StudioMembershipUpdateRequest request
    );

    @Operation(
            summary = "시설 회원 삭제",
            description = """
                    ### 삭제 범위

                    - **이 시설과의 관계만 끊습니다.** 회원 계정과 다른 시설의 소속은 그대로입니다.

                    ### 처리 방식

                    - 아직 시작하지 않은 수업의 예약과 대기는 함께 취소됩니다.
                    - **이미 지난 수강 이력과 정산 기록은 남습니다.**
                    - 수업, 예약, 수강권 이력이 전혀 없는 회원은 기록을 남기지 않고 지웁니다.

                    ### 재등록

                    - 이력이 있어 기록이 남은 회원은 같은 번호로 다시 등록하면 기존 소속이 되살아납니다.

                    ### 제한

                    - 시설 대표와 자기 자신은 삭제할 수 없습니다.
                    - 회원 관리 권한이 필요합니다.
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공", content = @Content),
            @ApiResponse(
                    responseCode = "403",
                    description = "회원 관리 권한이 없음",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"code":"PERMISSION-001","message":"이 작업을 수행할 권한이 없습니다."}""")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "시설 또는 소속을 찾을 수 없음",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {"code":"MEMBERSHIP-005","message":"시설 소속을 찾을 수 없습니다."}""")
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "삭제할 수 없는 소속",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "대표 강사", value = """
                                            {"code":"MEMBERSHIP-009","message":"시설 대표는 삭제할 수 없습니다."}"""),
                                    @ExampleObject(name = "자기 자신", value = """
                                            {"code":"MEMBERSHIP-010","message":"자기 자신은 삭제할 수 없습니다."}""")
                            }
                    )
            )
    })
    ResponseEntity<Void> delete(
            @Parameter(hidden = true) Long memberId,
            @Parameter(description = "시설 ID", required = true, example = "1") Long studioId,
            @Parameter(description = "소속 ID", required = true, example = "1") Long membershipId
    );
}
