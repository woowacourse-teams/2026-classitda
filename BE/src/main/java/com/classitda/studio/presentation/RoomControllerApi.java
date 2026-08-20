package com.classitda.studio.presentation;

import com.classitda.common.exception.ErrorResponse;
import com.classitda.common.pagination.CursorResponse;
import com.classitda.studio.presentation.dto.RoomCreateRequest;
import com.classitda.studio.presentation.dto.RoomResponse;
import com.classitda.studio.presentation.dto.RoomUpdateRequest;
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

@Tag(name = "룸", description = "시설에 속한 룸 관리 API")
public interface RoomControllerApi {

    @Operation(
            summary = "룸 등록",
            description = "시설에 룸을 등록한다. 대표 강사만 등록할 수 있고, 같은 시설 안에서 룸 이름은 중복될 수 없다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "룸 등록 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 값이 올바르지 않거나 API 버전 헤더가 없음",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "요청 값 오류",
                                            value = """
                                                    {"code": "COMMON-001", "message": "요청 값이 올바르지 않습니다."}"""
                                    ),
                                    @ExampleObject(
                                            name = "버전 헤더 누락",
                                            value = """
                                                    {"code": "API-001", "message": "X-API-Version 헤더는 필수입니다."}"""
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "권한이 없거나 시설 소속이 아님",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "권한 없음", value = """
                                            {"code": "PERMISSION-001", \
                                            "message": "이 작업을 수행할 권한이 없습니다."}"""),
                                    @ExampleObject(name = "소속 아님", value = """
                                            {"code": "MEMBERSHIP-001", "message": "해당 시설의 소속이 아닙니다."}""")
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
                    description = "이미 사용 중인 룸 이름",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "룸 이름 중복", value = """
                                    {"code": "ROOM-002", "message": "이미 사용 중인 룸 이름입니다."}""")
                    )
            )
    })
    ResponseEntity<RoomResponse> save(
            @Parameter(hidden = true)
            Long memberId,
            @Parameter(description = "시설 ID", required = true, example = "1")
            Long studioId,
            RoomCreateRequest request
    );

    @Operation(
            summary = "룸 목록 조회",
            description = """
                    시설에 속한 룸을 id 오름차순으로 조회한다. 권한 제한이 없다.
                    cursor를 생략하면 첫 페이지를 반환하고, 다음 페이지는 응답의 nextCursor를 그대로 전달한다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = "cursor나 size가 올바르지 않거나 API 버전 헤더가 없음",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "요청 값 오류",
                                            value = """
                                                    {"code": "COMMON-001", "message": "요청 값이 올바르지 않습니다."}"""
                                    ),
                                    @ExampleObject(
                                            name = "버전 헤더 누락",
                                            value = """
                                                    {"code": "API-001", "message": "X-API-Version 헤더는 필수입니다."}"""
                                    )
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
    CursorResponse<RoomResponse> findWithCursor(
            @Parameter(description = "시설 ID", required = true, example = "1")
            Long studioId,
            @Parameter(
                    description = "이전 응답의 nextCursor를 그대로 전달한다. 첫 페이지는 생략한다.",
                    schema = @Schema(type = "string", example = "10")
            )
            String cursor,
            @Parameter(description = "한 번에 가져올 개수. 1 이상 100 이하여야 한다.", example = "10")
            int size
    );

    @Operation(
            summary = "룸 정보 수정",
            description = """
                    룸의 이름을 수정한다. 대표 강사만 수정할 수 있다.
                    같은 시설 안에서 룸 이름은 중복될 수 없다.
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 값이 올바르지 않거나 API 버전 헤더가 없음",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "요청 값 오류",
                                            value = """
                                                    {"code": "COMMON-001", "message": "요청 값이 올바르지 않습니다."}"""
                                    ),
                                    @ExampleObject(
                                            name = "버전 헤더 누락",
                                            value = """
                                                    {"code": "API-001", "message": "X-API-Version 헤더는 필수입니다."}"""
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "권한이 없거나 시설 소속이 아님",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "권한 없음", value = """
                                            {"code": "PERMISSION-001", \
                                            "message": "이 작업을 수행할 권한이 없습니다."}"""),
                                    @ExampleObject(name = "소속 아님", value = """
                                            {"code": "MEMBERSHIP-001", "message": "해당 시설의 소속이 아닙니다."}""")
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "룸을 찾을 수 없음. 시설이 없거나 다른 시설의 룸일 때도 이 응답이 나간다.",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "룸 없음", value = """
                                    {"code": "ROOM-001", "message": "룸을 찾을 수 없습니다."}""")
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "이미 사용 중인 룸 이름",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "룸 이름 중복", value = """
                                    {"code": "ROOM-002", "message": "이미 사용 중인 룸 이름입니다."}""")
                    )
            )
    })
    RoomResponse update(
            @Parameter(hidden = true)
            Long memberId,
            @Parameter(description = "시설 ID", required = true, example = "1")
            Long studioId,
            @Parameter(description = "룸 ID", required = true, example = "1")
            Long roomId,
            RoomUpdateRequest request
    );
}
