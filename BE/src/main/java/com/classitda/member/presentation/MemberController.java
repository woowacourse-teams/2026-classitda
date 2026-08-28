package com.classitda.member.presentation;

import com.classitda.authentication.presentation.annotation.CurrentMemberId;
import com.classitda.member.application.MemberService;
import com.classitda.member.presentation.dto.MyNameUpdateRequest;
import com.classitda.member.presentation.dto.MyProfileResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/members")
@RestController
public class MemberController implements MemberControllerApi {

    private final MemberService memberService;

    @Override
    @GetMapping(value = "/me", version = "1")
    public MyProfileResponse findMe(
            @CurrentMemberId Long memberId
    ) {
        return memberService.findMe(memberId);
    }

    @Override
    @PatchMapping(value = "/me/name", version = "1")
    public ResponseEntity<Void> updateName(
            @CurrentMemberId Long memberId,
            @Valid @RequestBody MyNameUpdateRequest request
    ) {
        memberService.updateName(memberId, request);
        return ResponseEntity.noContent().build();
    }

    @Override
    @DeleteMapping(value = "/me", version = "1")
    public ResponseEntity<Void> withdraw(
            @CurrentMemberId Long memberId
    ) {
        memberService.withdraw(memberId);
        return ResponseEntity.noContent().build();
    }
}
