package com.classitda.member.presentation;

import com.classitda.authentication.presentation.annotation.CurrentMemberId;
import com.classitda.common.exception.ErrorResponse;
import com.classitda.member.presentation.dto.MyNameUpdateRequest;
import com.classitda.member.presentation.dto.MyProfileResponse;
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

@Tag(name = "회원", description = "회원 정보 관리 API")
public interface MemberControllerApi {

    @Operation(
            summary = "내 정보 조회",
            description = """
                    ### 반환하는 값

                    - `name` 과 `phoneNumber` 는 회원 계정의 값입니다. 시설마다 다르게 부르는 이름과는 다릅니다.
                    - `email` 은 소셜 계정에서 받은 값이며, 소셜 계정에 이메일이 없으면 `null` 입니다.

                    ### 소셜 계정

                    - 현재는 구글 계정의 이메일만 반환합니다.
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(
                    responseCode = "400",
                    description = "API 버전 헤더가 없거나 지원하지 않는 버전",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "ACCESS 토큰이 없거나 유효하지 않음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "ACCESS 권한이 없는 토큰 사용",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "회원을 찾을 수 없음",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = "{\"code\":\"MEMBER-008\",\"message\":\"회원을 찾을 수 없습니다.\"}"
                            )
                    )
            )
    })
    MyProfileResponse findMe(
            @Parameter(hidden = true) @CurrentMemberId Long memberId
    );

    @Operation(
            summary = "내 이름 수정",
            description = """
                    ### 수정 범위

                    - 회원 계정의 이름만 바꿉니다. **시설에 등록된 이름(`studio_membership.name`)은 바뀌지 않습니다.**
                    - 이름은 1자 이상 50자 이하입니다.

                    ### 응답

                    - 바뀐 값이 필요하면 내 정보 조회 API 를 다시 호출합니다.
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "수정 성공", content = @Content),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 값이 올바르지 않거나, API 버전 헤더가 없음",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "요청 값 오류",
                                            value = "{\"code\":\"COMMON-001\",\"message\":\"요청 값이 올바르지 않습니다.\"}"
                                    ),
                                    @ExampleObject(
                                            name = "버전 헤더 누락",
                                            value = "{\"code\":\"API-001\",\"message\":\"X-API-Version 헤더는 필수입니다.\"}"
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "ACCESS 토큰이 없거나 유효하지 않음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "ACCESS 권한이 없는 토큰 사용",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "회원을 찾을 수 없음",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = "{\"code\":\"MEMBER-008\",\"message\":\"회원을 찾을 수 없습니다.\"}"
                            )
                    )
            )
    })
    ResponseEntity<Void> updateName(
            @Parameter(hidden = true) @CurrentMemberId Long memberId,
            MyNameUpdateRequest request
    );

    @Operation(
            summary = "회원 탈퇴",
            description = "탈퇴를 요청하고 7일 후 개인정보 정리를 예약합니다. 중복 요청은 최초 일정을 유지합니다.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "탈퇴 요청 성공", content = @Content),
            @ApiResponse(
                    responseCode = "400",
                    description = "API 버전 헤더가 없거나 지원하지 않는 버전",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "ACCESS 토큰이 없거나 유효하지 않음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "ACCESS 권한이 없는 토큰 사용",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "회원을 찾을 수 없음",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = "{\"code\":\"MEMBER-008\",\"message\":\"회원을 찾을 수 없습니다.\"}"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "시설을 소유한 회원은 탈퇴할 수 없음",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = "{\"code\":\"MEMBER-009\",\"message\":\"시설 대표는 탈퇴할 수 없습니다.\"}"
                            )
                    )
            )
    })
    ResponseEntity<Void> withdraw(
            @Parameter(hidden = true) @CurrentMemberId Long memberId
    );
}
