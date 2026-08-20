package com.classitda.feature.student.mypage.mypassdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.classitda.domain.repository.mypage.MyPassRepository
import com.classitda.feature.student.mypage.mypassdetail.model.MyPassDetailUiState
import com.classitda.feature.student.mypage.mypassdetail.util.toDetailUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class MyPassDetailViewModel(
    private val passId: String,
    private val repository: MyPassRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<MyPassDetailUiState>(MyPassDetailUiState.Loading)
    val uiState: StateFlow<MyPassDetailUiState> = _uiState.asStateFlow()

    init {
        loadDetail()
    }

    fun onRetry() {
        if (_uiState.value !is MyPassDetailUiState.Error) return
        loadDetail()
    }

    private fun loadDetail() {
        _uiState.value = MyPassDetailUiState.Loading
        viewModelScope.launch {
            val passResult = runCatching { repository.getMyPass(passId) }
            val pass = passResult.getOrNull()

            _uiState.value =
                when {
                    passResult.isFailure -> {
                        MyPassDetailUiState.Error(passResult.exceptionOrNull()?.message)
                    }

                    pass == null -> {
                        MyPassDetailUiState.NotFound
                    }

                    else -> {
                        runCatching { repository.getMyPassHistory(passId) }
                            .fold(
                                onSuccess = { history -> MyPassDetailUiState.Content(pass.toDetailUiModel(history)) },
                                onFailure = { error -> MyPassDetailUiState.Error(error.message) },
                            )
                    }
                }
        }
    }
}
