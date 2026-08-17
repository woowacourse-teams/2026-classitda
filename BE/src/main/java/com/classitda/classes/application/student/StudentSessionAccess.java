package com.classitda.classes.application.student;

import com.classitda.passproduct.domain.MemberPassProduct;

record StudentSessionAccess(
        Long membershipId,
        MemberPassProduct memberPassProduct
) {
}
