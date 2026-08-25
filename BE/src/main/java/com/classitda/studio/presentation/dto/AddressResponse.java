package com.classitda.studio.presentation.dto;

import com.classitda.studio.domain.Address;

public record AddressResponse(
        String zonecode,
        String roadAddress,
        String jibunAddress,
        String buildingName,
        String detailAddress
) {
    public static AddressResponse from(Address address) {
        return new AddressResponse(
                address.getZonecode(),
                address.getRoadAddress(),
                address.getJibunAddress(),
                address.getBuildingName(),
                address.getDetailAddress()
        );
    }
}
