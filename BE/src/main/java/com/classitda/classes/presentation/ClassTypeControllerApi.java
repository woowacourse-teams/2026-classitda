package com.classitda.classes.presentation;

import com.classitda.classes.presentation.dto.ClassTypeCreateRequest;
import com.classitda.classes.presentation.dto.ClassTypeResponse;
import com.classitda.common.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;

@Tag(name = "수업 종류", description = "시설에 속한 수업 종류 관리 API")
public interface ClassTypeControllerApi {

    @Operation(
            summary = "수업 종류 목록 조회",
            description = "시설에 속한 모든 수업 종류를 id 오름차순으로 조회한다. "
                    + "드롭다운 전체 옵션용 데이터로 페이지네이션을 적용하지 않으며, 권한 제한이 없다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "수업 종류 전체 목록 조회 성공. 수업 종류가 없으면 빈 배열을 반환함",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ClassTypeResponse.class)))
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
                    responseCode = "404",
                    description = "시설을 찾을 수 없음",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "시설 없음", value = """
                                    {"code": "STUDIO-002", "message": "시설을 찾을 수 없습니다."}""")
                    )
            )
    })
    List<ClassTypeResponse> findAll(
            @Parameter(description = "시설 ID", required = true, example = "1")
            Long studioId
    );

    @Operation(
            summary = "수업 종류 등록",
            description = "시설에 수업 종류를 등록한다. 대표 강사만 등록할 수 있고, 같은 시설 안에서 이름은 중복될 수 없다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "수업 종류 등록 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 값이 올바르지 않거나 API 버전 헤더가 없거나 지원하지 않는 버전임",
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
                    responseCode = "403",
                    description = "시설 소속이 아니거나 소속이 비활성 상태이거나 권한이 없음",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
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
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "이미 존재하는 수업 종류 이름",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "수업 종류 이름 중복", value = """
                                    {"code": "CLASS_TYPE-002", "message": "이미 존재하는 수업 종류 이름입니다."}""")
                    )
            )
    })
    ResponseEntity<ClassTypeResponse> save(
            @Parameter(hidden = true)
            Long memberId,
            @Parameter(description = "시설 ID", required = true, example = "1")
            Long studioId,
            ClassTypeCreateRequest request
    );

    @Operation(
            summary = "수업 종류 삭제",
            description = "시설에 속한 수업 종류를 삭제한다. 수업 종류 관리 권한이 필요하다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "수업 종류 삭제 성공"),
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
    ResponseEntity<Void> delete(
            @Parameter(hidden = true)
            Long memberId,
            @Parameter(description = "시설 ID", required = true, example = "1")
            Long studioId,
            @Parameter(description = "수업 종류 ID", required = true, example = "1")
            Long classTypeId
    );
}
