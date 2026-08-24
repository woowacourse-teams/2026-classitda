package com.classitda.feature.instructor.mypage.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.classitda.domain.model.instructor.mypage.FacilityRegistrationDraft
import com.classitda.domain.model.instructor.mypage.InstructorFacilityId
import com.classitda.domain.model.instructor.mypage.InstructorMemberId
import com.classitda.domain.model.instructor.mypage.ManagedFacility
import com.classitda.domain.model.instructor.mypage.MemberRegistrationDraft
import com.classitda.domain.model.instructor.mypage.MemberSortOrder
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageFailureReason
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageRepository
import com.classitda.domain.repository.instructor.mypage.InstructorMyPageResult
import com.classitda.feature.common.profile.contract.MemberProfileUiModel
import com.classitda.feature.common.profile.contract.PhoneNumberChangeAction
import com.classitda.feature.common.profile.contract.PhoneNumberChangeUiError
import com.classitda.feature.common.profile.contract.PhoneNumberChangeUiState
import com.classitda.feature.common.profile.contract.ProfileEditAction
import com.classitda.feature.common.profile.contract.ProfileEditUiState
import com.classitda.feature.common.profile.contract.ProfileUiError
import com.classitda.feature.common.profile.contract.ProfileViewAction
import com.classitda.feature.common.profile.contract.ProfileViewUiState
import com.classitda.feature.instructor.mypage.contract.FacilityDeleteError
import com.classitda.feature.instructor.mypage.contract.FacilityDeleteState
import com.classitda.feature.instructor.mypage.contract.FacilityDetailAction
import com.classitda.feature.instructor.mypage.contract.FacilityDetailUiError
import com.classitda.feature.instructor.mypage.contract.FacilityDetailUiState
import com.classitda.feature.instructor.mypage.contract.FacilityEditAction
import com.classitda.feature.instructor.mypage.contract.FacilityEditUiError
import com.classitda.feature.instructor.mypage.contract.FacilityEditUiState
import com.classitda.feature.instructor.mypage.contract.FacilityManagementAction
import com.classitda.feature.instructor.mypage.contract.FacilityManagementUiError
import com.classitda.feature.instructor.mypage.contract.FacilityManagementUiState
import com.classitda.feature.instructor.mypage.contract.FacilityRegistrationAction
import com.classitda.feature.instructor.mypage.contract.FacilityRegistrationUiError
import com.classitda.feature.instructor.mypage.contract.FacilityRegistrationUiState
import com.classitda.feature.instructor.mypage.contract.InstructorMyPageAction
import com.classitda.feature.instructor.mypage.contract.InstructorMyPageUiError
import com.classitda.feature.instructor.mypage.contract.InstructorMyPageUiModel
import com.classitda.feature.instructor.mypage.contract.InstructorMyPageUiState
import com.classitda.feature.instructor.mypage.contract.MemberManagementAction
import com.classitda.feature.instructor.mypage.contract.MemberManagementUiError
import com.classitda.feature.instructor.mypage.contract.MemberManagementUiState
import com.classitda.feature.instructor.mypage.contract.MemberRegistrationAction
import com.classitda.feature.instructor.mypage.contract.MemberRegistrationField
import com.classitda.feature.instructor.mypage.contract.MemberRegistrationUiError
import com.classitda.feature.instructor.mypage.contract.MemberRegistrationUiState
import com.classitda.feature.instructor.mypage.contract.facilityRegistrationFieldErrors
import com.classitda.feature.instructor.mypage.contract.isFacilityRegistrationValid
import com.classitda.feature.instructor.mypage.contract.isMemberRegistrationValid
import com.classitda.feature.instructor.mypage.contract.memberRegistrationFieldErrors
import com.classitda.feature.instructor.mypage.toPhoneError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class InstructorPhoneNumberChangeViewModel(
    private val repository: InstructorMyPageRepository,
    initialPhoneNumber: String = "",
) : ViewModel() {
    private val _uiState =
        MutableStateFlow<PhoneNumberChangeUiState>(PhoneNumberChangeUiState.Editing(initialPhoneNumber, ""))
    val uiState: StateFlow<PhoneNumberChangeUiState> = _uiState.asStateFlow()
    private var verificationId:
        com.classitda.domain.model.instructor.mypage.InstructorPhoneVerificationId? = null

    fun onAction(action: PhoneNumberChangeAction) {
        when (action) {
            is PhoneNumberChangeAction.PhoneNumberChanged -> {
                _uiState.value =
                    PhoneNumberChangeUiState.Editing(action.phoneNumber, "")
            }

            is PhoneNumberChangeAction.VerificationCodeChanged -> {
                updateCode(action.verificationCode)
            }

            PhoneNumberChangeAction.RequestVerification -> {
                requestVerification()
            }

            PhoneNumberChangeAction.VerifyCode -> {
                verify()
            }

            PhoneNumberChangeAction.Retry -> {
                requestVerification()
            }

            else -> {
                Unit
            }
        }
    }

    private fun updateCode(code: String) {
        val state = _uiState.value
        if (state is PhoneNumberChangeUiState.CodeEntry || state is PhoneNumberChangeUiState.Error) {
            val phone =
                if (state is PhoneNumberChangeUiState.CodeEntry) {
                    state.phoneNumber
                } else {
                    (state as PhoneNumberChangeUiState.Error)
                        .phoneNumber
                }
            _uiState.value = PhoneNumberChangeUiState.CodeEntry(phone, code.filter(Char::isDigit).take(6), 180)
        }
    }

    private fun requestVerification() {
        val phone =
            when (val state = _uiState.value) {
                is PhoneNumberChangeUiState.Editing -> state.phoneNumber
                is PhoneNumberChangeUiState.Error -> state.phoneNumber
                else -> return
            }
        if (phone.isBlank()) return
        _uiState.value = PhoneNumberChangeUiState.Requesting(phone, "")
        viewModelScope.launch {
            _uiState.value =
                when (val result = repository.requestPhoneVerification(phone)) {
                    is InstructorMyPageResult.Success -> {
                        verificationId = result.value.verificationId
                        PhoneNumberChangeUiState.CodeEntry(phone, "", 180)
                    }

                    is InstructorMyPageResult.Failure -> {
                        PhoneNumberChangeUiState.Error(phone, "", result.reason.toPhoneError())
                    }
                }
        }
    }

    private fun verify() {
        val state = _uiState.value as? PhoneNumberChangeUiState.CodeEntry ?: return
        val id = verificationId ?: return
        if (state.verificationCode.length != 6) return
        _uiState.value =
            PhoneNumberChangeUiState.Verifying(state.phoneNumber, state.verificationCode, state.remainingSeconds)
        viewModelScope.launch {
            _uiState.value =
                when (val result = repository.verifyPhoneNumber(id, state.phoneNumber, state.verificationCode)) {
                    is InstructorMyPageResult.Success -> {
                        PhoneNumberChangeUiState.Verified(
                            result.value.phoneNumber,
                            state.verificationCode,
                        )
                    }

                    is InstructorMyPageResult.Failure -> {
                        PhoneNumberChangeUiState.Error(
                            state.phoneNumber,
                            state.verificationCode,
                            result.reason.toPhoneError(),
                            state.remainingSeconds,
                        )
                    }
                }
        }
    }
}
