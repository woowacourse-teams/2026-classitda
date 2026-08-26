package com.classitda.studio.exception;

import com.classitda.common.exception.ErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum StudioErrorCode implements ErrorCode {
    // STUDIO
    INVALID_OPERATING_TIME("STUDIO-001", "운영 종료 시간은 시작 시간보다 늦어야 합니다.", HttpStatus.BAD_REQUEST),
    NOT_FOUND("STUDIO-002", "시설을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    MEMBER_NOT_FOUND("STUDIO-003", "회원을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INVALID_ZONECODE("STUDIO-004", "우편번호는 5자리 숫자여야 합니다.", HttpStatus.BAD_REQUEST),
    INVALID_ROAD_ADDRESS("STUDIO-005", "도로명 주소는 필수이며 255자를 넘을 수 없습니다.", HttpStatus.BAD_REQUEST),
    INVALID_ADDRESS_LENGTH("STUDIO-006", "주소 항목의 길이가 허용 범위를 넘었습니다.", HttpStatus.BAD_REQUEST),
    INVALID_IMAGE_OBJECT_KEY("STUDIO-007", "이미지 키가 올바르지 않습니다.", HttpStatus.BAD_REQUEST),
    IMAGE_ALREADY_USED("STUDIO-008", "이미 다른 시설에 사용된 이미지입니다.", HttpStatus.CONFLICT),

    // ROOM
    ROOM_NOT_FOUND("ROOM-001", "룸을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    ROOM_NAME_DUPLICATED("ROOM-002", "이미 사용 중인 룸 이름입니다.", HttpStatus.CONFLICT),

    // POLICY
    POLICY_NOT_FOUND("POLICY-001", "운영 정책을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    // MEMBERSHIP
    NOT_MEMBERSHIP("MEMBERSHIP-001", "해당 시설의 소속이 아닙니다.", HttpStatus.FORBIDDEN),
    MEMBERSHIP_INACTIVE("MEMBERSHIP-002", "이용이 정지된 소속입니다.", HttpStatus.FORBIDDEN),
    INVALID_MEMBERSHIP_NAME("MEMBERSHIP-003", "소속 이름은 1자 이상 50자 이하여야 합니다.", HttpStatus.BAD_REQUEST),
    MEMBERSHIP_ALREADY_EXISTS("MEMBERSHIP-004", "이미 시설에 등록된 회원입니다.", HttpStatus.CONFLICT),
    MEMBERSHIP_NOT_FOUND("MEMBERSHIP-005", "시설 소속을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INVALID_MEMBERSHIP_PHONE_NUMBER("MEMBERSHIP-006", "휴대전화 번호는 010으로 시작하는 숫자 11자리여야 합니다.", HttpStatus.BAD_REQUEST),
    MEMBERSHIP_PHONE_NUMBER_NOT_EDITABLE("MEMBERSHIP-007", "가입한 회원의 휴대전화 번호는 수정할 수 없습니다.", HttpStatus.CONFLICT),
    MEMBERSHIP_WITHDRAWAL_PENDING("MEMBERSHIP-008", "탈퇴 처리 중인 회원의 번호입니다.", HttpStatus.CONFLICT),
    MEMBERSHIP_OWNER_NOT_DELETABLE("MEMBERSHIP-009", "시설 대표는 삭제할 수 없습니다.", HttpStatus.CONFLICT),
    MEMBERSHIP_SELF_NOT_DELETABLE("MEMBERSHIP-010", "자기 자신은 삭제할 수 없습니다.", HttpStatus.CONFLICT),

    STUDIO_ROLE_NOT_FOUND("ROLE-001", "시설 역할을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    // PERMISSION
    PERMISSION_DENIED("PERMISSION-001", "이 작업을 수행할 권한이 없습니다.", HttpStatus.FORBIDDEN);

    private final String code;
    private final String message;
    private final HttpStatus status;

    StudioErrorCode(String code, String message, HttpStatus status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }
}
