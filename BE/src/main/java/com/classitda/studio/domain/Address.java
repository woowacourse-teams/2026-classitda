package com.classitda.studio.domain;

import com.classitda.studio.exception.StudioErrorCode;
import com.classitda.studio.exception.StudioException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Embeddable
public class Address {

    private static final Pattern ZONECODE_PATTERN = Pattern.compile("\\d{5}");
    private static final int MAX_ADDRESS_LENGTH = 255;
    private static final int MAX_BUILDING_NAME_LENGTH = 100;
    private static final int MAX_DETAIL_ADDRESS_LENGTH = 100;

    @Column(nullable = false, length = 5)
    private String zonecode;

    @Column(nullable = false, length = MAX_ADDRESS_LENGTH)
    private String roadAddress;

    @Column(length = MAX_ADDRESS_LENGTH)
    private String jibunAddress;

    @Column(length = MAX_BUILDING_NAME_LENGTH)
    private String buildingName;

    @Column(length = MAX_DETAIL_ADDRESS_LENGTH)
    private String detailAddress;

    private Address(
            String zonecode,
            String roadAddress,
            String jibunAddress,
            String buildingName,
            String detailAddress
    ) {
        validateZonecode(zonecode);
        validateRoadAddress(roadAddress);
        validateOptionalLength(jibunAddress, MAX_ADDRESS_LENGTH);
        validateOptionalLength(buildingName, MAX_BUILDING_NAME_LENGTH);
        validateOptionalLength(detailAddress, MAX_DETAIL_ADDRESS_LENGTH);

        this.zonecode = zonecode;
        this.roadAddress = roadAddress;
        this.jibunAddress = blankToNull(jibunAddress);
        this.buildingName = blankToNull(buildingName);
        this.detailAddress = blankToNull(detailAddress);
    }

    public static Address of(
            String zonecode,
            String roadAddress,
            String jibunAddress,
            String buildingName,
            String detailAddress
    ) {
        return new Address(zonecode, roadAddress, jibunAddress, buildingName, detailAddress);
    }

    private void validateZonecode(String zonecode) {
        if (zonecode == null || !ZONECODE_PATTERN.matcher(zonecode).matches()) {
            throw new StudioException(StudioErrorCode.INVALID_ZONECODE);
        }
    }

    private void validateRoadAddress(String roadAddress) {
        if (roadAddress == null || roadAddress.isBlank() || roadAddress.length() > MAX_ADDRESS_LENGTH) {
            throw new StudioException(StudioErrorCode.INVALID_ROAD_ADDRESS);
        }
    }

    private void validateOptionalLength(String value, int maxLength) {
        if (value != null && value.length() > maxLength) {
            throw new StudioException(StudioErrorCode.INVALID_ADDRESS_LENGTH);
        }
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value;
    }
}
