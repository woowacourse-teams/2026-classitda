package com.classitda.studio.presentation;

import com.classitda.common.exception.ErrorResponse;
import com.classitda.studio.presentation.dto.StudioCreateRequest;
import com.classitda.common.image.ImageUploadUrlRequest;
import com.classitda.common.image.ImageUploadUrlResponse;
import com.classitda.studio.presentation.dto.StudioResponse;
import com.classitda.studio.presentation.dto.StudioUpdateRequest;
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
@Tag(name = "시설", description = "시설 생성과 관리 API")
public interface StudioControllerApi {

    @Operation(
            summary = "시설 대표 이미지 업로드 URL 발급",
            description = """
                    - 확장자와 파일 크기(바이트)를 보내면 대표 이미지 한 장에 대한 presigned URL 과 `objectKey` 를 발급합니다.

                    - 클라이언트는 받은 `uploadUrl` 로 파일을 직접 PUT 한 뒤, 시설 생성이나 수정 요청의 `image` 에 `objectKey` 를 담아 보냅니다.

                    - `uploadUrl` 은 짧은 시간 뒤 만료되며, 서명한 `contentType` 과 다른 형식으로 업로드하면 거부됩니다.

                    - 지원 확장자는 `jpg`, `jpeg`, `png`, `webp` 이고, 최대 5MB 입니다.

                    - **파일 크기는 서명에 포함됩니다.** 발급받을 때 알린 크기와 다른 파일을 올리면 S3 가 거부합니다.

                    - **권한**: 대표이거나 시설 수정 권한이 있어야 합니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "업로드 URL 발급 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 값이 올바르지 않거나 지원하지 않는 확장자입니다.",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(name = "요청 값 오류", value = """
                                            {"code":"COMMON-001","message":"요청 값이 올바르지 않습니다."}"""),
                                    @ExampleObject(name = "지원하지 않는 확장자", value = """
                                            {"code":"IMAGE-001","message":"지원하지 않는 이미지 형식입니다."}"""),
                                    @ExampleObject(name = "크기 초과", value = """
                                            {"code":"IMAGE-003","message":"이미지는 5MB 를 넘을 수 없습니다."}""")
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "시설 수정 권한이 없습니다.",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "권한 없음", value = """
                                    {"code":"PERMISSION-001","message":"이 작업을 수행할 권한이 없습니다."}""")
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
    ImageUploadUrlResponse issueImageUploadUrl(ImageUploadUrlRequest request);

    @Operation(
            summary = "시설 생성",
            description = """
                    ### 생성 방식

                    - 생성자는 해당 시설의 대표 강사로 등록되고, 시스템 기본 역할(대표 강사, 일반 강사, 회원)이 함께 생성됩니다.

                    ### 주소

                    - `address` 는 카카오 우편번호 서비스가 돌려준 값을 그대로 담습니다.
                    - `zonecode` 와 `roadAddress` 는 필수이고, `jibunAddress`, `buildingName`, `detailAddress` 는 생략할 수 있습니다.
                    - `detailAddress` 는 사용자가 직접 입력하는 상세 주소입니다.

                    ### 대표 이미지

                    - `image` 는 선택이며 한 장만 등록할 수 있습니다.
                    - 업로드 URL 발급 API 로 받은 `objectKey` 를 그대로 담습니다. 파일이나 URL 을 보내면 안 됩니다.
                    - 이미 다른 시설이 쓰고 있는 `objectKey` 는 등록할 수 없습니다.

                    ### 운영 시간

                    - `openTime` 과 `closeTime` 은 `HH:mm` 형식이고, `closeTime` 이 `openTime` 보다 늦어야 합니다.
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "시설 생성 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 값이 올바르지 않거나, 운영 시간이 잘못되었거나, API 버전 헤더가 없음",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "요청 값 오류",
                                            value = """
                                                    {"code": "COMMON-001", "message": "요청 값이 올바르지 않습니다."}"""
                                    ),
                                    @ExampleObject(
                                            name = "운영 시간 오류",
                                            value = """
                                                    {"code": "STUDIO-001", \
                                                    "message": "운영 종료 시간은 시작 시간보다 늦어야 합니다."}"""
                                    ),
                                    @ExampleObject(
                                            name = "이미지 키 오류",
                                            value = """
                                                    {"code": "STUDIO-007", "message": "이미지 키가 올바르지 않습니다."}"""
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
                    responseCode = "409",
                    description = "이미 다른 시설에 사용된 이미지",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "이미지 중복", value = """
                                    {"code": "STUDIO-008", "message": "이미 다른 시설에 사용된 이미지입니다."}""")
                    )
            )
    })
    ResponseEntity<Void> save(
            @Parameter(hidden = true)
            Long memberId,
            StudioCreateRequest request
    );

    @Operation(
            summary = "내 시설 목록 조회",
            description = "로그인한 회원이 속한 시설을 id 오름차순으로 조회한다. "
                    + "로그인 후 시설을 고르는 화면에서 쓰이므로 별도 권한이 필요 없고, "
                    + "개인이 속한 시설은 소수라 페이지네이션을 적용하지 않는다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "내 시설 전체 조회 성공. 속한 시설이 없으면 빈 배열을 반환함",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = StudioResponse.class)))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "API 버전 헤더가 없음",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "버전 헤더 누락", value = """
                                    {"code": "API-001", "message": "X-API-Version 헤더는 필수입니다."}""")
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
                    description = "ACCESS 토큰이 아님",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "접근 권한 없음", value = """
                                    {"code": "AUTH-002", "message": "접근 권한이 없습니다."}""")
                    )
            )
    })
    List<StudioResponse> findAllByMemberId(
            @Parameter(hidden = true)
            Long memberId
    );

    @Operation(
            summary = "시설 정보 조회",
            description = "시설의 기본 정보를 조회한다. 권한 제한이 없다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = "API 버전 헤더가 없음",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "버전 헤더 누락", value = """
                                    {"code": "API-001", "message": "X-API-Version 헤더는 필수입니다."}""")
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
    StudioResponse findById(
            @Parameter(description = "시설 ID", required = true, example = "1")
            Long studioId
    );

    @Operation(
            summary = "시설 정보 수정",
            description = """
                    ### 수정 방식

                    - 대표이거나 시설 수정 권한이 있어야 합니다.
                    - **전달한 필드만 변경되고, 생략하거나 `null` 로 보낸 필드는 기존 값을 유지합니다.**
                    - `address` 는 항목별로 합쳐지지 않습니다. 보내면 주소 전체가 교체되므로 바뀌지 않는 항목도 함께 담아야 합니다.

                    ### 대표 이미지

                    - `image` 를 보내면 기존 대표 이미지를 새 `objectKey` 로 교체합니다.
                    - 업로드 URL 발급 API 로 받은 `objectKey` 를 그대로 담습니다.
                    - 이미 다른 시설이 쓰고 있는 `objectKey` 로는 교체할 수 없습니다.

                    ### 운영 시간

                    - `openTime` 이나 `closeTime` 을 바꿀 때도 `closeTime` 이 `openTime` 보다 늦어야 합니다.
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "수정 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 값이 올바르지 않거나, 운영 시간이 잘못되었거나, API 버전 헤더가 없음",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "요청 값 오류",
                                            value = """
                                                    {"code": "COMMON-001", "message": "요청 값이 올바르지 않습니다."}"""
                                    ),
                                    @ExampleObject(
                                            name = "운영 시간 오류",
                                            value = """
                                                    {"code": "STUDIO-001", \
                                                    "message": "운영 종료 시간은 시작 시간보다 늦어야 합니다."}"""
                                    ),
                                    @ExampleObject(
                                            name = "이미지 키 오류",
                                            value = """
                                                    {"code": "STUDIO-007", "message": "이미지 키가 올바르지 않습니다."}"""
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
                    description = "이미 다른 시설에 사용된 이미지",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(name = "이미지 중복", value = """
                                    {"code": "STUDIO-008", "message": "이미 다른 시설에 사용된 이미지입니다."}""")
                    )
            )
    })
    ResponseEntity<Void> update(
            @Parameter(hidden = true)
            Long memberId,
            @Parameter(description = "시설 ID", required = true, example = "1")
            Long studioId,
            StudioUpdateRequest request
    );
}
