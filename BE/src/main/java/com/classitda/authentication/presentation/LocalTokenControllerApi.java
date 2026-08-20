package com.classitda.authentication.presentation;

import com.classitda.authentication.presentation.dto.token.LoginTokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

// TODO : 로컬 개발 끝나면 삭제 예정
@Tag(name = "로컬 인증", description = "로컬 Swagger 테스트 전용 인증 API")
public interface LocalTokenControllerApi {

    @Operation(
            summary = "로컬 테스트용 로그인 토큰 발급",
            description = "일반 인증 절차를 생략하고 입력한 회원 ID로 Access Token과 Refresh Token을 발급합니다. "
                    + "local 프로필에서만 사용할 수 있으며 기본 테스트 회원 ID는 1입니다."
    )
    @ApiResponse(
            responseCode = "200",
            description = "로그인 토큰 발급 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = LoginTokenResponse.class),
                    examples = @ExampleObject(
                            value = "{\"accessToken\":\"access-token\",\"accessTokenExpiresIn\":3600,"
                                    + "\"refreshToken\":\"refresh-token\","
                                    + "\"refreshTokenExpiresIn\":2592000}"
                    )
            )
    )
    LoginTokenResponse issueTokens(
            @Parameter(description = "토큰 subject로 사용할 회원 ID", example = "1", required = true) Long memberId
    );
}
