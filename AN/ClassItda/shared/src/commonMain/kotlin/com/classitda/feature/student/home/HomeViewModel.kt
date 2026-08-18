package com.classitda.feature.student.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.classitda.domain.model.home.FacilityNotice
import com.classitda.domain.model.home.Pass
import com.classitda.domain.model.home.PendingReservation
import com.classitda.domain.model.home.UpcomingReservation
import com.classitda.domain.repository.home.NoticeRepository
import com.classitda.domain.repository.home.PassRepository
import com.classitda.domain.repository.home.ReservationRepository
import com.classitda.feature.student.home.model.ConfirmedReservationUiModel
import com.classitda.feature.student.home.util.toUiModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlin.time.Clock
import kotlin.time.Instant

class HomeViewModel(
    private val reservationRepository: ReservationRepository,
    private val passRepository: PassRepository,
    private val noticeRepository: NoticeRepository,
) : ViewModel() {
    private val timeZone = TimeZone.currentSystemDefault()

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.InitialLoading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _sideEffect = MutableSharedFlow<String>(extraBufferCapacity = 64)
    val sideEffect: Flow<String> = _sideEffect.asSharedFlow()

    init {
        loadHome()
    }

    fun onRefresh() {
        val current = _uiState.value
        if (current !is HomeUiState.Success) return
        loadHome(previous = current.content)
    }

    fun onRetry() {
        val current = _uiState.value
        if (current !is HomeUiState.Error) return
        loadHome()
    }

    fun onPendingReservationApproveClick() {
        updateContent { it.copy(showApprovalDialog = true) }
    }

    fun onApprovalDialogDismiss() {
        updateContent { it.copy(showApprovalDialog = false, isApproving = false) }
    }

    fun onConfirmedDialogDismiss() {
        updateContent { it.copy(confirmedReservation = null) }
    }

    fun onApproveReservation() {
        val current = _uiState.value
        if (current !is HomeUiState.Success) return
        if (current.content.isApproving) return
        val pending = current.content.pendingReservation ?: return

        updateContent { it.copy(isApproving = true) }

        viewModelScope.launch {
            runCatching { reservationRepository.approveReservation(pending.reservationId) }
                .onSuccess {
                    // 그 사이 다이얼로그가 닫혔거나 새로고침으로 다른 예약이 떴다면 결과를 버린다.
                    if (!isApprovingSameReservation(pending.reservationId)) return@onSuccess
                    updateContent {
                        it.copy(
                            isApproving = false,
                            showApprovalDialog = false,
                            pendingReservation = null,
                            confirmedReservation =
                                ConfirmedReservationUiModel(
                                    className = pending.className,
                                    dateText = pending.dateText,
                                    timeRangeText = pending.timeRangeText,
                                ),
                        )
                    }
                }.onFailure { error ->
                    // 승인 실패 시에는 다이얼로그를 유지해 사용자가 다시 시도할 수 있도록 둔다.
                    if (!isApprovingSameReservation(pending.reservationId)) return@onFailure
                    updateContent { it.copy(isApproving = false) }
                    _sideEffect.emit(error.message ?: "예약 승인에 실패했어요")
                }
        }
    }

    private fun isApprovingSameReservation(reservationId: String): Boolean {
        val content = currentContentOrNull() ?: return false
        return content.isApproving && content.pendingReservation?.reservationId == reservationId
    }

    private fun currentContentOrNull(): HomeContentUiModel? =
        when (val state = _uiState.value) {
            is HomeUiState.Success -> state.content
            is HomeUiState.Loading -> state.previous
            is HomeUiState.InitialLoading, is HomeUiState.Error -> null
        }

    private fun updateContent(transform: (HomeContentUiModel) -> HomeContentUiModel) {
        _uiState.update { state ->
            when (state) {
                is HomeUiState.Success -> state.copy(content = transform(state.content))
                is HomeUiState.Loading -> state.copy(previous = transform(state.previous))
                is HomeUiState.InitialLoading, is HomeUiState.Error -> state
            }
        }
    }

    private fun loadHome(previous: HomeContentUiModel? = null) {
        _uiState.update {
            if (previous != null) HomeUiState.Loading(previous) else HomeUiState.InitialLoading
        }
        viewModelScope.launch {
            runCatching { fetchHomeSnapshot() }
                .onSuccess { snapshot ->
                    val freshPending = snapshot.pending?.toUiModel(snapshot.now, timeZone)
                    _uiState.update {
                        HomeUiState.Success(
                            HomeContentUiModel(
                                pendingReservation = freshPending,
                                upcomingReservation = snapshot.upcoming?.toUiModel(snapshot.now, timeZone),
                                myPass = snapshot.pass?.toUiModel(),
                                facilityNotice = snapshot.notice?.toUiModel(),
                                // 새로고침(previous != null)이면 진행 중이던 다이얼로그/승인 상태를 그대로 이어간다.
                                showApprovalDialog = (previous?.showApprovalDialog ?: false) && freshPending != null,
                                isApproving = previous?.isApproving ?: false,
                                confirmedReservation = previous?.confirmedReservation,
                            ),
                        )
                    }
                }.onFailure { error ->
                    if (previous != null) {
                        _uiState.update { HomeUiState.Success(previous) }
                        _sideEffect.emit(error.message ?: "새로고침에 실패했습니다.")
                    } else {
                        _uiState.update { HomeUiState.Error(error.message) }
                    }
                }
        }
    }

    private suspend fun fetchHomeSnapshot(): HomeSnapshot =
        coroutineScope {
            val now = Clock.System.now()
            val pendingDeferred = async { reservationRepository.getPendingReservation() }
            val upcomingDeferred = async { reservationRepository.getNextUpcomingReservation() }
            val passDeferred = async { passRepository.getPrimaryPass() }
            val noticeDeferred = async { noticeRepository.getLatestNotice() }
            HomeSnapshot(
                now = now,
                pending = pendingDeferred.await(),
                upcoming = upcomingDeferred.await(),
                pass = passDeferred.await(),
                notice = noticeDeferred.await(),
            )
        }

    private data class HomeSnapshot(
        val now: Instant,
        val pending: PendingReservation?,
        val upcoming: UpcomingReservation?,
        val pass: Pass?,
        val notice: FacilityNotice?,
    )
}
