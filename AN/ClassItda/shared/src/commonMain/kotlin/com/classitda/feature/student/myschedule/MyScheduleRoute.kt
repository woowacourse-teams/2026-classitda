package com.classitda.feature.student.myschedule

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.classitda.domain.model.student.myschedule.ReservationId
import com.classitda.domain.model.student.myschedule.WaitlistId
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun MyScheduleRoute(
    viewModelKey: String,
    onOpenReservation: (ReservationId) -> Unit,
    onOpenWaitlist: (WaitlistId, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel = koinViewModel<MyScheduleViewModel>(key = viewModelKey)
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    MyScheduleScreen(
        state = state,
        onAction = viewModel::onAction,
        onOpenReservation = onOpenReservation,
        onOpenWaitlist = onOpenWaitlist,
        modifier = modifier,
    )
}
