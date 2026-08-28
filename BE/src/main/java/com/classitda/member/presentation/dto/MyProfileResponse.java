package com.classitda.member.presentation.dto;

import com.classitda.member.domain.Member;
import io.swagger.v3.oas.annotations.media.Schema;

public record MyProfileResponse(
        @Schema(description = "회원 이름", example = "김클래스")
        String name,

        @Schema(description = "휴대전화 번호", example = "01012345678")
        String phoneNumber,

        @Schema(description = "소셜 계정 이메일. 소셜 계정에 이메일이 없으면 null 입니다.", example = "member@example.com")
        String email
) {
    public static MyProfileResponse of(Member member, String email) {
        return new MyProfileResponse(member.getName(), member.getPhoneNumber(), email);
    }
}
