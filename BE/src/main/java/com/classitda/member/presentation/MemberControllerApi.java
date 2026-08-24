package com.classitda.member.presentation;

import com.classitda.authentication.presentation.annotation.CurrentMemberId;
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

@Tag(name = "회원", description = "회원 정보 관리 API")
public interface MemberControllerApi {

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
