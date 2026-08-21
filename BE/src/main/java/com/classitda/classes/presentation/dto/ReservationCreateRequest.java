package com.classitda.classes.presentation.dto;

import jakarta.validation.constraints.Size;

public record ReservationCreateRequest(
        Long membershipId,

        @Size(max = 50, message = "비회원 이름은 50자 이하여야 합니다.")
        String guestName,

        @Size(max = 20, message = "비회원 연락처는 20자 이하여야 합니다.")
        String guestPhoneNumber
) {
    public static ReservationCreateRequest forMember(Long membershipId) {
        return new ReservationCreateRequest(membershipId, null, null);
    }

    public static ReservationCreateRequest forGuest(String guestName, String guestPhoneNumber) {
        return new ReservationCreateRequest(null, guestName, guestPhoneNumber);
    }

    public boolean isGuestReservation() {
        return membershipId == null;
    }
}
