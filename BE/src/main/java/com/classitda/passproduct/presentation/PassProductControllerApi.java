package com.classitda.passproduct.presentation;

import com.classitda.common.exception.ErrorResponse;
import com.classitda.passproduct.presentation.dto.PassProductCreateRequest;
import com.classitda.passproduct.presentation.dto.PassProductResponse;
import com.classitda.passproduct.presentation.dto.PassProductUpdateRequest;
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

@SecurityRequirement(name = "bearerAuth")
@Tag(name = "수강권", description = "시설이 판매하는 수강권 관리 API")
public interface PassProductControllerApi {

    @Operation(
            summary = "수강권 목록 조회",
            description = "시설이 등록한 모든 수강권을 id 오름차순으로 조회한다. "
                    + "판매를 중지한 수강권(active=false)도 함께 반환하며, 수강권 관리 권한이 필요하다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "수강권 전체 목록 조회 성공. 수강권이 없으면 빈 배열을 반환함",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = PassProductResponse.class)))
            ),
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
                    description = "시설을 찾을 수 없음",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "시설 없음", value = """
                                    {"code": "STUDIO-002", "message": "시설을 찾을 수 없습니다."}""")
                    )
            )
    })
    List<PassProductResponse> findAll(
            @Parameter(hidden = true)
            Long memberId,
            @Parameter(description = "시설 ID", required = true, example = "1")
            Long studioId
    );

    @Operation(
            summary = "수강권 등록",
            description = "시설에 수강권을 등록한다. 수강권 관리 권한이 필요하다. "
                    + "totalCount가 null이면 횟수 무제한, validPeriodAmount가 null이면 기간 무제한을 뜻하며 "
                    + "둘을 동시에 무제한으로 지정할 수 없다. "
                    + "classTypeIds는 하나 이상이어야 한다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "수강권 등록 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 값이 올바르지 않거나 수강권 제약을 위반했거나 API 버전 헤더가 없거나 지원하지 않는 버전임",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "요청 값 오류", value = """
                                            {"code": "COMMON-001", "message": "요청 값이 올바르지 않습니다."}"""),
                                    @ExampleObject(name = "이름 오류", value = """
                                            {"code": "PASS_PRODUCT-001", "message": "수강권 이름은 1자 이상 100자 이하여야 합니다."}"""),
                                    @ExampleObject(name = "수업 형태 누락", value = """
                                            {"code": "PASS_PRODUCT-002", "message": "수업 형태는 필수입니다."}"""),
                                    @ExampleObject(name = "유효 기간 오류", value = """
                                            {"code": "PASS_PRODUCT-003", "message": "유효 기간은 기간과 단위를 함께 지정해야 하며 1 이상이어야 합니다."}"""),
                                    @ExampleObject(name = "횟수 오류", value = """
                                            {"code": "PASS_PRODUCT-004", "message": "수강 가능 횟수는 1회 이상이어야 합니다."}"""),
                                    @ExampleObject(name = "종료 조건 없음", value = """
                                            {"code": "PASS_PRODUCT-005", "message": "유효 기간과 수강 가능 횟수를 모두 무제한으로 지정할 수 없습니다."}"""),
                                    @ExampleObject(name = "홀딩 일수 오류", value = """
                                            {"code": "PASS_PRODUCT-006", "message": "홀딩 가능 일수는 0일 이상이어야 합니다."}"""),
                                    @ExampleObject(name = "홀딩 불가", value = """
                                            {"code": "PASS_PRODUCT-007", "message": "유효 기간이 무제한이면 홀딩할 수 없습니다."}"""),
                                    @ExampleObject(name = "수업 종류 없음", value = """
                                            {"code": "PASS_PRODUCT-009", "message": "수업 종류를 하나 이상 지정해야 합니다."}"""),
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
                    description = "시설 또는 해당 시설의 수업 종류를 찾을 수 없음",
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
    ResponseEntity<PassProductResponse> save(
            @Parameter(hidden = true)
            Long memberId,
            @Parameter(description = "시설 ID", required = true, example = "1")
            Long studioId,
            PassProductCreateRequest request
    );

    @Operation(
            summary = "수강권 수정",
            description = "시설에 속한 수강권을 수정한다. 수강권 관리 권한이 필요하다. "
                    + "요청 본문은 수정 후의 전체 상태를 담는다 — null은 언제나 무제한을 뜻하며 '변경하지 않음'이 아니고, "
                    + "classTypeIds도 전달한 목록으로 통째로 교체되며 하나 이상이어야 한다. "
                    + "발급 이력 때문에 삭제 대신 active=false로 판매를 중지한다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수강권 수정 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 값이 올바르지 않거나 수강권 제약을 위반했거나 API 버전 헤더가 없거나 지원하지 않는 버전임",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "요청 값 오류", value = """
                                            {"code": "COMMON-001", "message": "요청 값이 올바르지 않습니다."}"""),
                                    @ExampleObject(name = "이름 오류", value = """
                                            {"code": "PASS_PRODUCT-001", "message": "수강권 이름은 1자 이상 100자 이하여야 합니다."}"""),
                                    @ExampleObject(name = "수업 형태 누락", value = """
                                            {"code": "PASS_PRODUCT-002", "message": "수업 형태는 필수입니다."}"""),
                                    @ExampleObject(name = "유효 기간 오류", value = """
                                            {"code": "PASS_PRODUCT-003", "message": "유효 기간은 기간과 단위를 함께 지정해야 하며 1 이상이어야 합니다."}"""),
                                    @ExampleObject(name = "횟수 오류", value = """
                                            {"code": "PASS_PRODUCT-004", "message": "수강 가능 횟수는 1회 이상이어야 합니다."}"""),
                                    @ExampleObject(name = "종료 조건 없음", value = """
                                            {"code": "PASS_PRODUCT-005", "message": "유효 기간과 수강 가능 횟수를 모두 무제한으로 지정할 수 없습니다."}"""),
                                    @ExampleObject(name = "홀딩 일수 오류", value = """
                                            {"code": "PASS_PRODUCT-006", "message": "홀딩 가능 일수는 0일 이상이어야 합니다."}"""),
                                    @ExampleObject(name = "홀딩 불가", value = """
                                            {"code": "PASS_PRODUCT-007", "message": "유효 기간이 무제한이면 홀딩할 수 없습니다."}"""),
                                    @ExampleObject(name = "수업 종류 없음", value = """
                                            {"code": "PASS_PRODUCT-009", "message": "수업 종류를 하나 이상 지정해야 합니다."}"""),
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
                    description = "시설, 해당 시설의 수강권 또는 수업 종류를 찾을 수 없음",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "시설 없음", value = """
                                            {"code": "STUDIO-002", "message": "시설을 찾을 수 없습니다."}"""),
                                    @ExampleObject(name = "수업 종류 없음", value = """
                                            {"code": "CLASS_TYPE-003", "message": "수업 종류를 찾을 수 없습니다."}"""),
                                    @ExampleObject(name = "수강권 없음", value = """
                                            {"code": "PASS_PRODUCT-008", "message": "수강권을 찾을 수 없습니다."}""")
                            }
                    )
            )
    })
    PassProductResponse update(
            @Parameter(hidden = true)
            Long memberId,
            @Parameter(description = "시설 ID", required = true, example = "1")
            Long studioId,
            @Parameter(description = "수강권 ID", required = true, example = "1")
            Long passProductId,
            PassProductUpdateRequest request
    );
}
