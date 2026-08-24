package com.classitda.member.presentation;

import com.classitda.authentication.presentation.annotation.CurrentMemberId;
import com.classitda.member.application.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/members")
@RestController
public class MemberController implements MemberControllerApi {

    private final MemberService memberService;

    @Override
    @DeleteMapping(value = "/me", version = "1")
    public ResponseEntity<Void> withdraw(
            @CurrentMemberId Long memberId
    ) {
        memberService.withdraw(memberId);
        return ResponseEntity.noContent().build();
    }
}
