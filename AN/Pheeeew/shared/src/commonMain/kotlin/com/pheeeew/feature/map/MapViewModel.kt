package com.pheeeew.feature.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pheeeew.domain.exception.ApiException
import com.pheeeew.domain.model.geo.Coordinate
import com.pheeeew.domain.repository.SighRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

class MapViewModel(
    private val sighRepository: SighRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<MapUiState>(MapUiState.Loading)
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private val _sighReleasedEvents = Channel<Unit>(Channel.BUFFERED)
    val sighReleasedEvents: Flow<Unit> = _sighReleasedEvents.receiveAsFlow()

    private var pendingRequestId: String? = null

    init {
        loadSighs()
    }

    fun loadSighs() {
        viewModelScope.launch {
            _uiState.value = MapUiState.Loading
            try {
                _uiState.value = MapUiState.Success(sighRepository.getSighs())
            } catch (e: ApiException) {
                _uiState.value = MapUiState.Error(e.message)
            }
        }
    }

    fun sendSigh(coordinate: Coordinate) {
        val current = _uiState.value as? MapUiState.Success ?: return
        val requestId = pendingRequestId ?: Uuid.random().toString().also { pendingRequestId = it }

        viewModelScope.launch {
            _uiState.value = current.copy(sighReleaseState = SighReleaseState.Submitting)
            try {
                val sighPin = sighRepository.registerSigh(requestId, coordinate)
                pendingRequestId = null
                _uiState.value =
                    current.copy(
                        sighs = current.sighs + sighPin,
                        sighReleaseState = SighReleaseState.Idle,
                    )
                _sighReleasedEvents.send(Unit)
            } catch (e: ApiException) {
                _uiState.value = current.copy(sighReleaseState = SighReleaseState.Error(e.message))
            }
        }
    }

    fun cancelSighRelease() {
        pendingRequestId = null
        val current = _uiState.value as? MapUiState.Success ?: return
        _uiState.value = current.copy(sighReleaseState = SighReleaseState.Idle)
    }
}
