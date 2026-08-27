package com.classitda.feature.student.home

import com.classitda.feature.student.home.model.ConfirmedReservationUiModel
import com.classitda.feature.student.home.model.FacilityNoticeUiModel
import com.classitda.feature.student.home.model.MyPassUiModel
import com.classitda.feature.student.home.model.PendingReservationUiModel
import com.classitda.feature.student.home.model.UpcomingReservationUiModel

sealed interface HomeUiState {
    data object InitialLoading : HomeUiState

    data class Loading(
        val previous: HomeContentUiModel,
    ) : HomeUiState

    data class Success(
        val content: HomeContentUiModel,
    ) : HomeUiState

    data class Error(
        val message: String?,
    ) : HomeUiState
}

data class HomeContentUiModel(
    val pendingReservation: PendingReservationUiModel? = null,
    val upcomingReservation: UpcomingReservationUiModel? = null,
    val myPass: MyPassUiModel? = null,
    val facilityNotice: FacilityNoticeUiModel? = null,
    val showApprovalDialog: Boolean = false,
    val isApproving: Boolean = false,
    val confirmedReservation: ConfirmedReservationUiModel? = null,
)
