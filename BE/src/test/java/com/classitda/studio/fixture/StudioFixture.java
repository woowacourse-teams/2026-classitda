package com.classitda.studio.fixture;

import com.classitda.member.domain.Member;
import com.classitda.studio.domain.Address;
import com.classitda.studio.domain.Studio;
import com.classitda.studio.presentation.dto.AddressRequest;
import com.classitda.studio.presentation.dto.StudioCreateRequest;
import com.classitda.studio.presentation.dto.StudioUpdateRequest;
import java.time.LocalTime;
import java.util.concurrent.atomic.AtomicLong;

public class StudioFixture {

    private static final LocalTime OPEN_TIME = LocalTime.of(9, 0);
    private static final LocalTime CLOSE_TIME = LocalTime.of(22, 0);
    private static final AtomicLong MEMBER_SEQUENCE = new AtomicLong(10_000_000L);
    private static final String ZONECODE = "06234";
    private static final String ROAD_ADDRESS = "서울 강남구 테헤란로 1";
    private static final String JIBUN_ADDRESS = "서울 강남구 역삼동 823";
    private static final String BUILDING_NAME = "클래스잇다 빌딩";
    private static final String DETAIL_ADDRESS = "3층 301호";

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
                .address(기본_주소())
                .phoneNumber("0212345678")
                .openTime(OPEN_TIME)
                .closeTime(CLOSE_TIME)
                .build();
    }

    public static Address 기본_주소() {
        return Address.of(ZONECODE, ROAD_ADDRESS, JIBUN_ADDRESS, BUILDING_NAME, DETAIL_ADDRESS);
    }

    public static AddressRequest 기본_주소_요청() {
        return AddressRequest.of(ZONECODE, ROAD_ADDRESS, JIBUN_ADDRESS, BUILDING_NAME, DETAIL_ADDRESS);
    }

    public static AddressRequest 우편번호가_다른_주소_요청(String zonecode) {
        return AddressRequest.of(zonecode, ROAD_ADDRESS, JIBUN_ADDRESS, BUILDING_NAME, DETAIL_ADDRESS);
    }

    public static StudioUpdateRequest 이름만_바꾸는_수정_요청(String name) {
        return new StudioUpdateRequest(name, null, null, null, null, null, null);
    }

    public static StudioCreateRequest 이미지가_있는_시설_생성_요청(String image) {
        return new StudioCreateRequest(
                "클래스잇다 스튜디오",
                기본_주소_요청(),
                "0212345678",
                OPEN_TIME,
                CLOSE_TIME,
                image,
                null
        );
    }

    public static StudioUpdateRequest 이미지만_바꾸는_수정_요청(String image) {
        return new StudioUpdateRequest(null, null, null, null, null, image, null);
    }

    public static StudioCreateRequest 기본_시설_생성_요청() {
        return new StudioCreateRequest(
                "클래스잇다 스튜디오",
                기본_주소_요청(),
                "0212345678",
                OPEN_TIME,
                CLOSE_TIME,
                null,
                null
        );
    }
}
