package com.classitda.feature.student.mypage.holding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.classitda.core.time.CurrentDateTimeProvider
import com.classitda.domain.model.mypage.MyPassHoldingCalculator
import com.classitda.domain.repository.mypage.MyPassRepository
import com.classitda.feature.student.mypage.holding.model.MyPassHoldingCompletedUiModel
import com.classitda.feature.student.mypage.holding.model.MyPassHoldingUiState
import com.classitda.feature.student.mypage.holding.util.formatFullKoreanDate
import com.classitda.feature.student.mypage.holding.util.formatMonthDayKorean
import com.classitda.feature.student.mypage.holding.util.toCompletedUiModel
import com.classitda.feature.student.mypage.util.formatDateDot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

internal class MyPassHoldingRequestViewModel(
    args: MyPassHoldingRequestArgs,
    private val repository: MyPassRepository,
    currentDateProvider: CurrentDateTimeProvider,
) : ViewModel() {
    private val passId: String = args.passId
    private val currentExpireDate: LocalDate = args.currentExpireDate

    // TODO: 실제 날짜 선택 UI가 붙기 전까지의 임시 기본값
    private val startDate: LocalDate = currentDateProvider.now().date.plus(7, DateTimeUnit.DAY)
    private val endDate: LocalDate = startDate.plus(13, DateTimeUnit.DAY)

    private val _uiState = MutableStateFlow(buildInitialUiState(args.passName))
    val uiState: StateFlow<MyPassHoldingUiState> = _uiState.asStateFlow()

    private val _completedEvent = MutableSharedFlow<MyPassHoldingCompletedUiModel>(extraBufferCapacity = 1)
    val completedEvent: Flow<MyPassHoldingCompletedUiModel> = _completedEvent.asSharedFlow()

    private val _sideEffect = MutableSharedFlow<String>(extraBufferCapacity = 64)
    val sideEffect: Flow<String> = _sideEffect.asSharedFlow()

    fun onMemoChanged(memo: String) {
        _uiState.update { it.copy(memo = memo) }
    }

    fun onStartDateClick() {
        // TODO: 날짜 선택 다이얼로그 연동
    }

    fun onEndDateClick() {
        // TODO: 날짜 선택 다이얼로그 연동
    }

    fun onSubmitClick() {
        if (_uiState.value.isSubmitting) return
        _uiState.update {
            it.copy(
                confirmDialogDescription =
                    "${formatFullKoreanDate(startDate)}부터 ${formatMonthDayKorean(endDate)}까지\n" +
                        "수강권 이용이 중지됩니다.",
            )
        }
    }

    fun onDialogDismiss() {
        if (_uiState.value.isSubmitting) return
        _uiState.update { it.copy(confirmDialogDescription = null) }
    }

    fun onDialogConfirm() {
        if (_uiState.value.isSubmitting) return
        _uiState.update { it.copy(isSubmitting = true) }
        viewModelScope.launch {
            runCatching { repository.requestHolding(passId, startDate, endDate) }
                .onSuccess { receipt ->
                    _uiState.update { it.copy(isSubmitting = false, confirmDialogDescription = null) }
                    _completedEvent.emit(receipt.toCompletedUiModel())
                }.onFailure { error ->
                    _uiState.update { it.copy(isSubmitting = false, confirmDialogDescription = null) }
                    _sideEffect.emit(error.message ?: "홀딩 요청에 실패했어요. 다시 시도해 주세요.")
                }
        }
    }

    private fun buildInitialUiState(passName: String): MyPassHoldingUiState {
        val receiptPreview =
            MyPassHoldingCalculator.calculate(
                startDate = startDate,
                endDate = endDate,
                previousExpireDate = currentExpireDate,
            )
        return MyPassHoldingUiState(
            passName = passName,
            startDateLabel = formatDateDot(startDate),
            endDateLabel = formatDateDot(endDate),
            memo = "",
            totalHoldingDaysLabel = "${receiptPreview.totalHoldingDays}일",
            currentExpireDateLabel = formatDateDot(currentExpireDate),
            newExpireDateLabel = formatDateDot(receiptPreview.newExpireDate),
        )
    }
}
