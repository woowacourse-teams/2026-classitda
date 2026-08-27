package com.classitda.feature.student.reservation

import androidx.compose.runtime.Composable

@Composable
internal fun ReservationRoute(
    onClassReservationClick: (String, String) -> Unit,
    onWaitlistReservationClick: (String, String) -> Unit,
    bottomBar: @Composable () -> Unit = {},
) {
    ReservationScreen(
        onClassReservationClick = onClassReservationClick,
        onWaitlistReservationClick = onWaitlistReservationClick,
        bottomBar = bottomBar,
    )
}
