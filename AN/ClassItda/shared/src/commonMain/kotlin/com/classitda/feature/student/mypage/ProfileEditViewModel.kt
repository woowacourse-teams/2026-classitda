package com.classitda.feature.student.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.classitda.domain.model.student.mypage.MemberProfile
import com.classitda.domain.repository.student.mypage.MyPageRepository
import com.classitda.domain.repository.student.mypage.MyPageResult
import com.classitda.feature.student.mypage.contract.ProfileEditAction
import com.classitda.feature.student.mypage.contract.ProfileEditUiState
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

    private fun loadProfile() {
        if (isLoading || _uiState.value is ProfileEditUiState.Saving) return
        isLoading = true
        _uiState.value = ProfileEditUiState.Loading

        viewModelScope.launch {
            _uiState.value =
                when (val result = repository.getProfile()) {
                    is MyPageResult.Success -> result.value.toEditingState()
                    is MyPageResult.Failure -> ProfileEditUiState.Error(result.reason)
                }
            isLoading = false
        }
    }

    private fun changeName(name: String) {
        val state = _uiState.value
        val profile =
            when (state) {
                is ProfileEditUiState.Editing -> state.profile
                is ProfileEditUiState.SaveFailed -> state.profile
                else -> return
            }
        _uiState.value =
            ProfileEditUiState.Editing(
                profile = profile,
                draftName = name,
                canSave = name.isNotBlank() && name != profile.name,
                uiModel = mapper.mapProfile(profile),
            )
    }

    private fun saveName() {
        val state = _uiState.value
        val profile: MemberProfile
        val draftName: String
        val canSave: Boolean
        when (state) {
            is ProfileEditUiState.Editing -> {
                profile = state.profile
                draftName = state.draftName
                canSave = state.canSave
            }

            is ProfileEditUiState.SaveFailed -> {
                profile = state.profile
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
                profile = profile,
                draftName = draftName,
                uiModel = mapper.mapProfile(profile),
            )
        viewModelScope.launch {
            _uiState.value =
                when (val result = repository.updateProfileName(profile.id, draftName)) {
                    is MyPageResult.Success -> {
                        result.value.toEditingState()
                    }

                    is MyPageResult.Failure -> {
                        ProfileEditUiState.SaveFailed(
                            profile = profile,
                            draftName = draftName,
                            reason = result.reason,
                            uiModel = mapper.mapProfile(profile),
                        )
                    }
                }
        }
    }

    private fun MemberProfile.toEditingState(): ProfileEditUiState.Editing =
        ProfileEditUiState.Editing(
            profile = this,
            draftName = name,
            canSave = false,
            uiModel = mapper.mapProfile(this),
        )
}
