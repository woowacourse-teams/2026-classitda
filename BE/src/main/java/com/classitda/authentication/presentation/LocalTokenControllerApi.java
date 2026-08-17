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
            description = """
                    일반 인증 절차를 생략하고 입력한 회원 ID로 Access Token과 Refresh Token을 발급합니다.
                    local 프로필에서만 사용할 수 있습니다.

                    ### 회원용 수업 목록 테스트 순서

                    1. 회원 ID 1로 이 API를 호출합니다.
                    2. 응답의 accessToken을 Swagger 상단 Authorize에 Bearer 토큰으로 입력합니다.
                    3. 시설 ID 1, 보유 수강권 ID 42로 회원용 일별 수업 목록을 호출합니다.
                    4. date에는 로컬 애플리케이션을 시작한 날짜의 다음 날을 입력합니다.

                    ### 로컬 테스트 계정

                    - 1: 김회원 - 회원용 목록·상세 조회
                    - 2: 박대표 - 시설 전체 ClassSession 관리
                    - 3: 이강사 - 본인 ClassSession 관리
                    """
    )
    @ApiResponse(
            responseCode = "200",
            description = "로그인 토큰 발급 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = LoginTokenResponse.class),
                    examples = @ExampleObject(
                            value = "{\"accessToken\":\"access-token\",\"accessTokenExpiresIn\":900,"
                                    + "\"refreshToken\":\"refresh-token\","
                                    + "\"refreshTokenExpiresIn\":2592000}"
                    )
            )
    )
    LoginTokenResponse issueTokens(
            @Parameter(description = "토큰 subject로 사용할 회원 ID", example = "1", required = true) Long memberId
    );
}
