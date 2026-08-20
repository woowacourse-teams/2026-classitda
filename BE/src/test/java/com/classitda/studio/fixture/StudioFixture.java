package com.classitda.studio.fixture;

import com.classitda.member.domain.Member;
import com.classitda.studio.domain.Studio;
import com.classitda.studio.presentation.dto.StudioCreateRequest;
import com.classitda.studio.presentation.dto.StudioUpdateRequest;
import java.time.LocalTime;
import java.util.concurrent.atomic.AtomicLong;

public class StudioFixture {

    private static final LocalTime OPEN_TIME = LocalTime.of(9, 0);
    private static final LocalTime CLOSE_TIME = LocalTime.of(22, 0);
    private static final AtomicLong MEMBER_SEQUENCE = new AtomicLong(10_000_000L);

    public static Member 기본_소유자() {
        return 아이디가_다른_소유자("owner");
    }

    public static Member 아이디가_다른_소유자(String providerId) {
        return Member.builder()
                .name("김강사-" + providerId)
                .phoneNumber("010%08d".formatted(MEMBER_SEQUENCE.getAndIncrement()))
                .build();
    }

    public static Studio 기본_시설(Member owner) {
        return Studio.builder()
                .owner(owner)
                .name("클래스잇다 스튜디오")
                .address("서울시 강남구 테헤란로 1")
                .phoneNumber("0212345678")
                .openTime(OPEN_TIME)
                .closeTime(CLOSE_TIME)
                .build();
    }

    public static StudioUpdateRequest 이름만_바꾸는_수정_요청(String name) {
        return new StudioUpdateRequest(name, null, null, null, null, null, null);
    }

    public static StudioCreateRequest 기본_시설_생성_요청() {
        return new StudioCreateRequest(
                "클래스잇다 스튜디오",
                "서울시 강남구 테헤란로 1",
                "0212345678",
                OPEN_TIME,
                CLOSE_TIME,
                null,
                null
        );
    }
}
