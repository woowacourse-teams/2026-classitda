package com.classitda.feature.student.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.classitda.domain.model.student.mypage.MemberProfile
import com.classitda.domain.repository.student.mypage.MyPageFailureReason
import com.classitda.domain.repository.student.mypage.MyPageRepository
import com.classitda.domain.repository.student.mypage.MyPageResult
import com.classitda.feature.common.profile.contract.ProfileEditAction
import com.classitda.feature.common.profile.contract.ProfileEditUiState
import com.classitda.feature.common.profile.contract.ProfileUiError
import com.classitda.feature.student.mypage.mapper.MyPageUiMapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class ProfileEditViewModel(
    private val repository: MyPageRepository,
    private val mapper: MyPageUiMapper,
) : ViewModel() {
    private val _uiState = MutableStateFlow<ProfileEditUiState>(ProfileEditUiState.Loading)
    val uiState: StateFlow<ProfileEditUiState> = _uiState.asStateFlow()

    private var isLoading = false
    private var currentProfile: MemberProfile? = null

    init {
        loadProfile()
    }

    fun onAction(action: ProfileEditAction) {
        when (action) {
            ProfileEditAction.Retry -> loadProfile()

            is ProfileEditAction.NameChanged -> changeName(action.name)

            ProfileEditAction.Save -> saveName()

            ProfileEditAction.Back,
            ProfileEditAction.RequestPhotoChange,
            ProfileEditAction.OpenPhoneNumberChange,
            -> Unit
        }
    }

    fun refresh() {
        loadProfile()
    }

    private fun loadProfile() {
        if (isLoading || _uiState.value is ProfileEditUiState.Saving) return
        isLoading = true
        _uiState.value = ProfileEditUiState.Loading

        viewModelScope.launch {
            _uiState.value =
                when (val result = repository.getProfile()) {
                    is MyPageResult.Success -> result.value.toEditingState()
                    is MyPageResult.Failure -> ProfileEditUiState.Error(result.reason.toProfileUiError())
                }
            isLoading = false
        }
    }

    private fun changeName(name: String) {
        val state = _uiState.value
        val profile = currentProfile ?: return
        val uiModel =
            when (state) {
                is ProfileEditUiState.Editing -> state.profile
                is ProfileEditUiState.SaveFailed -> state.profile
                else -> return
            }
        val phoneNumber =
            when (state) {
                is ProfileEditUiState.Editing -> state.phoneNumber
                is ProfileEditUiState.SaveFailed -> state.phoneNumber
            }
        _uiState.value =
            ProfileEditUiState.Editing(
                profile = uiModel,
                phoneNumber = phoneNumber,
                draftName = name,
                canSave = name.isNotBlank() && name != profile.name,
            )
    }

    private fun saveName() {
        val state = _uiState.value
        val profile = currentProfile ?: return
        val draftName: String
        val canSave: Boolean
        when (state) {
            is ProfileEditUiState.Editing -> {
                draftName = state.draftName
                canSave = state.canSave
            }

            is ProfileEditUiState.SaveFailed -> {
                draftName = state.draftName
                canSave = draftName.isNotBlank() && draftName != profile.name
            }

            else -> {
                return
            }
        }
        if (!canSave) return

        _uiState.value =
            ProfileEditUiState.Saving(
                profile = mapper.mapProfile(profile),
                phoneNumber = profile.phoneNumber,
                draftName = draftName,
            )
        viewModelScope.launch {
            _uiState.value =
                when (val result = repository.updateProfileName(profile.id, draftName)) {
                    is MyPageResult.Success -> {
                        result.value.toEditingState()
                    }

                    is MyPageResult.Failure -> {
                        ProfileEditUiState.SaveFailed(
                            profile = mapper.mapProfile(profile),
                            phoneNumber = profile.phoneNumber,
                            draftName = draftName,
                            reason = result.reason.toProfileUiError(),
                        )
                    }
                }
        }
    }

    private fun MemberProfile.toEditingState(): ProfileEditUiState.Editing {
        currentProfile = this
        return ProfileEditUiState.Editing(
            profile = mapper.mapProfile(this),
            phoneNumber = phoneNumber,
            draftName = name,
            canSave = false,
        )
    }
}

private fun MyPageFailureReason.toProfileUiError(): ProfileUiError =
    when (this) {
        MyPageFailureReason.NETWORK -> ProfileUiError.NETWORK
        MyPageFailureReason.NOT_FOUND -> ProfileUiError.NOT_FOUND
        MyPageFailureReason.CONFLICT -> ProfileUiError.CONFLICT
        MyPageFailureReason.INVALID_REQUEST -> ProfileUiError.INVALID_REQUEST
        else -> ProfileUiError.UNKNOWN
    }
