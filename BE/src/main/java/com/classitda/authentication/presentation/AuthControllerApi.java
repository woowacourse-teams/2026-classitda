package com.classitda.authentication.presentation;

import com.classitda.authentication.presentation.dto.GoogleLoginRequest;
import com.classitda.authentication.presentation.dto.LoginResponse;
import com.classitda.authentication.presentation.dto.RegisteredLoginResponse;
import com.classitda.authentication.presentation.dto.RegistrationRequiredLoginResponse;
import com.classitda.common.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "인증", description = "소셜 로그인과 인증 토큰 발급 API")
public interface AuthControllerApi {

    @Operation(
            summary = "Google 로그인",
            description = "Google ID 토큰을 검증합니다. 가입된 회원에게는 Access Token과 Refresh Token을, "
                    + "가입하지 않은 사용자에게는 회원가입에 사용할 Signup Token을 발급합니다.",
            requestBody = @RequestBody(
                    required = true,
                    description = "Google에서 발급받은 ID 토큰",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = GoogleLoginRequest.class),
                            examples = @ExampleObject(value = "{\"idToken\":\"google-id-token\"}")
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Google 로그인 처리 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(oneOf = {
                                    RegisteredLoginResponse.class,
                                    RegistrationRequiredLoginResponse.class
                            }),
                            examples = {
                                    @ExampleObject(
                                            name = "REGISTERED",
                                            summary = "가입된 회원",
                                            value = "{\"status\":\"REGISTERED\",\"accessToken\":\"access-token\","
                                                    + "\"accessTokenExpiresIn\":900,\"refreshToken\":\"refresh-token\","
                                                    + "\"refreshTokenExpiresIn\":2592000}"
                                    ),
                                    @ExampleObject(
                                            name = "REGISTRATION_REQUIRED",
                                            summary = "회원가입이 필요한 사용자",
                                            value = "{\"status\":\"REGISTRATION_REQUIRED\","
                                                    + "\"signupToken\":\"signup-token\","
                                                    + "\"signupTokenExpiresIn\":1800}"
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청 값 또는 API 버전이 올바르지 않음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = {
                                    @ExampleObject(
                                            name = "INVALID_INPUT",
                                            value = "{\"code\":\"COMMON-001\",\"message\":\"요청 값이 올바르지 않습니다.\"}"
                                    ),
                                    @ExampleObject(
                                            name = "API_VERSION_REQUIRED",
                                            value = "{\"code\":\"API-001\",\"message\":\"X-API-Version 헤더는 필수입니다.\"}"
                                    ),
                                    @ExampleObject(
                                            name = "API_VERSION_UNSUPPORTED",
                                            value = "{\"code\":\"API-002\",\"message\":\"지원하지 않는 API 버전입니다.\"}"
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Google ID 토큰이 유효하지 않음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(
                                    value = "{\"code\":\"AUTH-006\",\"message\":\"Google ID 토큰이 유효하지 않습니다.\"}"
                            )
                    )
            )
    })
    LoginResponse loginWithGoogle(GoogleLoginRequest request);
}
