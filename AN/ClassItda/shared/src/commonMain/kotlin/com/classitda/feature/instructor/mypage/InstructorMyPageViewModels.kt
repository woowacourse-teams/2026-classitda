package com.classitda.feature.instructor.mypage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.classitda.domain.model.instructor.mypage.FacilityRegistrationDraft
import com.classitda.domain.model.instructor.mypage.InstructorFacilityId
import com.classitda.domain.model.instructor.mypage.InstructorMemberId
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
import com.classitda.feature.instructor.mypage.contract.FacilityManagementAction
import com.classitda.feature.instructor.mypage.contract.FacilityManagementUiError
import com.classitda.feature.instructor.mypage.contract.FacilityManagementUiState
import com.classitda.feature.instructor.mypage.contract.FacilityRegistrationAction
import com.classitda.feature.instructor.mypage.contract.FacilityRegistrationField
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class InstructorMyPageViewModel(
    private val repository: InstructorMyPageRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<InstructorMyPageUiState>(InstructorMyPageUiState.Loading)
    val uiState: StateFlow<InstructorMyPageUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun onAction(action: InstructorMyPageAction) {
        if (action == InstructorMyPageAction.Retry) refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value =
                when (val result = repository.getSummary()) {
                    is InstructorMyPageResult.Success -> {
                        InstructorMyPageUiState.Content(
                            result.value.profile.toUiModel(),
                        )
                    }

                    is InstructorMyPageResult.Failure -> {
                        InstructorMyPageUiState.Error(result.reason.toMyPageError())
                    }
                }
        }
    }
}

internal class InstructorProfileViewModel(
    private val repository: InstructorMyPageRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<ProfileViewUiState>(ProfileViewUiState.Loading)
    val uiState: StateFlow<ProfileViewUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun onAction(action: ProfileViewAction) {
        if (action == ProfileViewAction.Retry) refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value =
                when (val result = repository.getProfile()) {
                    is InstructorMyPageResult.Success -> ProfileViewUiState.Content(result.value.toProfileUiModel())
                    is InstructorMyPageResult.Failure -> ProfileViewUiState.Error(result.reason.toProfileUiError())
                }
        }
    }
}

internal class InstructorProfileEditViewModel(
    private val repository: InstructorMyPageRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<ProfileEditUiState>(ProfileEditUiState.Loading)
    val uiState: StateFlow<ProfileEditUiState> = _uiState.asStateFlow()
    private var profile: com.classitda.domain.model.instructor.mypage.InstructorAccountProfile? = null

    init {
        refresh()
    }

    fun onAction(action: ProfileEditAction) {
        when (action) {
            ProfileEditAction.Retry -> refresh()
            is ProfileEditAction.NameChanged -> changeName(action.name)
            ProfileEditAction.Save -> save()
            else -> Unit
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value =
                when (val result = repository.getProfile()) {
                    is InstructorMyPageResult.Success -> result.value.toEditingState().also { profile = result.value }
                    is InstructorMyPageResult.Failure -> ProfileEditUiState.Error(result.reason.toProfileUiError())
                }
        }
    }

    private fun changeName(name: String) {
        val current = profile ?: return
        val state = _uiState.value as? ProfileEditUiState.Editing ?: return
        _uiState.value = state.copy(draftName = name, canSave = name.isNotBlank() && name != current.name)
    }

    private fun save() {
        val current = profile ?: return
        val state = _uiState.value as? ProfileEditUiState.Editing ?: return
        if (!state.canSave) return
        _uiState.value = ProfileEditUiState.Saving(state.profile, state.phoneNumber, state.draftName)
        viewModelScope.launch {
            _uiState.value =
                when (val result = repository.updateProfileName(state.draftName)) {
                    is InstructorMyPageResult.Success -> {
                        result.value.toEditingState().also { profile = result.value }
                    }

                    is InstructorMyPageResult.Failure -> {
                        ProfileEditUiState.SaveFailed(
                            state.profile,
                            current.phoneNumber,
                            state.draftName,
                            result.reason.toProfileUiError(),
                        )
                    }
                }
        }
    }
}

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

internal class MemberManagementViewModel(
    private val repository: InstructorMyPageRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<MemberManagementUiState>(MemberManagementUiState.Loading)
    val uiState: StateFlow<MemberManagementUiState> = _uiState.asStateFlow()
    private var query = ""
    private var sort = MemberSortOrder.RECENTLY_REGISTERED

    init {
        load()
    }

    fun onAction(action: MemberManagementAction) {
        when (action) {
            is MemberManagementAction.QueryChanged -> {
                query = action.query
                load()
            }

            is MemberManagementAction.SortOrderChanged -> {
                sort = action.sortOrder
                load()
            }

            MemberManagementAction.Retry -> {
                load()
            }

            else -> {
                Unit
            }
        }
    }

    private fun load() {
        _uiState.value =
            MemberManagementUiState.Loading
        viewModelScope.launch {
            _uiState.value =
                when (val result = repository.getMembers(query, sort)) {
                    is InstructorMyPageResult.Success -> {
                        when {
                            result.value.totalCount == 0 && query.isBlank() -> MemberManagementUiState.Empty
                            result.value.members.isEmpty() -> MemberManagementUiState.SearchEmpty(query)
                            else -> MemberManagementUiState.Content(result.value, query, sort)
                        }
                    }

                    is InstructorMyPageResult.Failure -> {
                        MemberManagementUiState.Error(result.reason.toListError())
                    }
                }
        }
    }
}

internal class MemberRegistrationViewModel(
    private val repository: InstructorMyPageRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<MemberRegistrationUiState>(editing(MemberRegistrationDraft()))
    val uiState: StateFlow<MemberRegistrationUiState> = _uiState.asStateFlow()

    fun onAction(action: MemberRegistrationAction) {
        when (action) {
            is MemberRegistrationAction.NameChanged -> {
                update { copy(name = action.name) }
            }

            is MemberRegistrationAction.PhoneNumberChanged -> {
                update { copy(phoneNumber = action.phoneNumber) }
            }

            MemberRegistrationAction.OpenConfirmation -> {
                if ((_uiState.value as? MemberRegistrationUiState.Editing)?.canSubmit ==
                    true
                ) {
                    _uiState.value =
                        MemberRegistrationUiState.Confirmation(
                            ((_uiState.value) as MemberRegistrationUiState.Editing).draft,
                        )
                }
            }

            MemberRegistrationAction.CancelConfirmation -> {
                (_uiState.value as? MemberRegistrationUiState.Confirmation)?.let {
                    _uiState.value =
                        editing(it.draft)
                }
            }

            MemberRegistrationAction.ConfirmRegistration -> {
                confirm()
            }

            MemberRegistrationAction.Retry -> {
                (_uiState.value as? MemberRegistrationUiState.Error)?.let {
                    _uiState.value =
                        MemberRegistrationUiState.Confirmation(it.draft)
                }
            }

            else -> {
                Unit
            }
        }
    }

    private fun update(change: MemberRegistrationDraft.() -> MemberRegistrationDraft) {
        val state = _uiState.value as? MemberRegistrationUiState.Editing ?: return
        _uiState.value = editing(state.draft.change())
    }

    private fun editing(draft: MemberRegistrationDraft) =
        MemberRegistrationUiState.Editing(
            draft,
            draft.name.isNotBlank() && draft.phoneNumber.isNotBlank(),
            buildSet {
                if (draft.name.isBlank()) add(MemberRegistrationField.NAME)
                if (draft.phoneNumber.isBlank()) add(MemberRegistrationField.PHONE_NUMBER)
            },
        )

    private fun confirm() {
        val draft =
            when (val state = _uiState.value) {
                is MemberRegistrationUiState.Confirmation -> state.draft
                else -> return
            }
        _uiState.value = MemberRegistrationUiState.Submitting(draft)
        viewModelScope.launch {
            _uiState.value =
                when (val result = repository.registerMember(draft)) {
                    is InstructorMyPageResult.Success -> {
                        MemberRegistrationUiState.Success(result.value)
                    }

                    is InstructorMyPageResult.Failure -> {
                        MemberRegistrationUiState.Error(
                            draft,
                            result.reason.toMemberRegistrationError(),
                        )
                    }
                }
        }
    }
}

internal class FacilityManagementViewModel(
    private val repository: InstructorMyPageRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<FacilityManagementUiState>(FacilityManagementUiState.Loading)
    val uiState: StateFlow<FacilityManagementUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun onAction(action: FacilityManagementAction) {
        if (action == FacilityManagementAction.Retry) load()
    }

    private fun load() {
        _uiState.value = FacilityManagementUiState.Loading
        viewModelScope.launch {
            _uiState.value =
                when (val result = repository.getFacilities()) {
                    is InstructorMyPageResult.Success -> {
                        if (result.value.facilities.isEmpty()) {
                            FacilityManagementUiState.Empty
                        } else {
                            FacilityManagementUiState
                                .Content(
                                    result.value,
                                )
                        }
                    }

                    is InstructorMyPageResult.Failure -> {
                        FacilityManagementUiState.Error(result.reason.toFacilityError())
                    }
                }
        }
    }
}

internal class FacilityRegistrationViewModel(
    private val repository: InstructorMyPageRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<FacilityRegistrationUiState>(editing(FacilityRegistrationDraft()))
    val uiState: StateFlow<FacilityRegistrationUiState> = _uiState.asStateFlow()

    fun onAction(action: FacilityRegistrationAction) {
        when (action) {
            is FacilityRegistrationAction.NameChanged -> {
                update { copy(name = action.name) }
            }

            is FacilityRegistrationAction.AddressChanged -> {
                update { copy(address = action.address) }
            }

            is FacilityRegistrationAction.DetailAddressChanged -> {
                update { copy(detailAddress = action.detailAddress) }
            }

            is FacilityRegistrationAction.PhoneNumberChanged -> {
                update { copy(phoneNumber = action.phoneNumber) }
            }

            is FacilityRegistrationAction.DescriptionChanged -> {
                update { copy(description = action.description) }
            }

            is FacilityRegistrationAction.ImagesSelected -> {
                update { copy(images = action.images.take(FacilityRegistrationDraft.MAX_IMAGE_COUNT)) }
            }

            is FacilityRegistrationAction.AddressSelected -> {
                update {
                    copy(
                        address = action.address,
                        detailAddress = action.detailAddress.ifBlank { detailAddress },
                    )
                }
            }

            FacilityRegistrationAction.Submit -> {
                submit()
            }

            FacilityRegistrationAction.Retry -> {
                (_uiState.value as? FacilityRegistrationUiState.Error)?.let {
                    _uiState.value =
                        editing(it.draft)
                }
            }

            else -> {
                Unit
            }
        }
    }

    private fun update(change: FacilityRegistrationDraft.() -> FacilityRegistrationDraft) {
        val state =
            _uiState.value as? FacilityRegistrationUiState.Editing ?: return
        _uiState.value = editing(state.draft.change())
    }

    private fun editing(draft: FacilityRegistrationDraft) =
        FacilityRegistrationUiState.Editing(
            draft,
            draft.name.isNotBlank() && draft.address.isNotBlank() && draft.phoneNumber.isNotBlank(),
            buildSet {
                if (draft.name.isBlank()) add(FacilityRegistrationField.NAME)
                if (draft.address.isBlank()) add(FacilityRegistrationField.ADDRESS)
                if (draft.phoneNumber.isBlank()) add(FacilityRegistrationField.PHONE_NUMBER)
            },
        )

    private fun submit() {
        val state =
            _uiState.value as? FacilityRegistrationUiState.Editing ?: return
        if (!state.canSubmit) return
        _uiState.value =
            FacilityRegistrationUiState.Submitting
        viewModelScope.launch {
            _uiState.value =
                when (val result = repository.registerFacility(state.draft)) {
                    is InstructorMyPageResult.Success -> {
                        FacilityRegistrationUiState.Success(result.value)
                    }

                    is InstructorMyPageResult.Failure -> {
                        FacilityRegistrationUiState.Error(
                            state.draft,
                            result.reason.toFacilityRegistrationError(),
                        )
                    }
                }
        }
    }
}

private fun com.classitda.domain.model.instructor.mypage.InstructorAccountProfile.toUiModel() =
    InstructorMyPageUiModel(
        name,
        maskPhoneNumber(phoneNumber),
        profileImageUrl,
        name.firstOrNull()?.toString() ?: "?",
    )

private fun com.classitda.domain.model.instructor.mypage.InstructorAccountProfile.toProfileUiModel() =
    MemberProfileUiModel(name, maskPhoneNumber(phoneNumber), email, profileImageUrl)

private fun com.classitda.domain.model.instructor.mypage.InstructorAccountProfile.toEditingState() =
    ProfileEditUiState.Editing(toProfileUiModel(), phoneNumber, name, false)

private fun maskPhoneNumber(value: String): String {
    val digits = value.filter(Char::isDigit)
    return if (digits.length >=
        8
    ) {
        "${digits.take(3)}-****-${digits.takeLast(4)}"
    } else {
        value
    }
}

private fun InstructorMyPageFailureReason.toMyPageError() =
    when (this) {
        InstructorMyPageFailureReason.NETWORK -> InstructorMyPageUiError.NETWORK
        InstructorMyPageFailureReason.NOT_FOUND -> InstructorMyPageUiError.NOT_FOUND
        else -> InstructorMyPageUiError.UNKNOWN
    }

private fun InstructorMyPageFailureReason.toProfileUiError() =
    when (this) {
        InstructorMyPageFailureReason.NETWORK -> ProfileUiError.NETWORK
        InstructorMyPageFailureReason.NOT_FOUND -> ProfileUiError.NOT_FOUND
        InstructorMyPageFailureReason.CONFLICT -> ProfileUiError.CONFLICT
        InstructorMyPageFailureReason.INVALID_REQUEST -> ProfileUiError.INVALID_REQUEST
        else -> ProfileUiError.UNKNOWN
    }

private fun InstructorMyPageFailureReason.toPhoneError() =
    when (this) {
        InstructorMyPageFailureReason.VERIFICATION_EXPIRED -> PhoneNumberChangeUiError.VERIFICATION_EXPIRED
        InstructorMyPageFailureReason.VERIFICATION_FAILED -> PhoneNumberChangeUiError.VERIFICATION_FAILED
        else -> PhoneNumberChangeUiError.REQUEST_FAILED
    }

private fun InstructorMyPageFailureReason.toListError() =
    when (this) {
        InstructorMyPageFailureReason.NETWORK -> MemberManagementUiError.NETWORK
        else -> MemberManagementUiError.UNKNOWN
    }

private fun InstructorMyPageFailureReason.toMemberRegistrationError() =
    when (this) {
        InstructorMyPageFailureReason.NETWORK -> MemberRegistrationUiError.NETWORK
        InstructorMyPageFailureReason.CONFLICT -> MemberRegistrationUiError.CONFLICT
        InstructorMyPageFailureReason.INVALID_REQUEST -> MemberRegistrationUiError.INVALID_REQUEST
        else -> MemberRegistrationUiError.UNKNOWN
    }

private fun InstructorMyPageFailureReason.toFacilityError() =
    when (this) {
        InstructorMyPageFailureReason.NETWORK -> FacilityManagementUiError.NETWORK
        else -> FacilityManagementUiError.UNKNOWN
    }

private fun InstructorMyPageFailureReason.toFacilityRegistrationError() =
    when (this) {
        InstructorMyPageFailureReason.NETWORK -> FacilityRegistrationUiError.NETWORK
        InstructorMyPageFailureReason.CONFLICT -> FacilityRegistrationUiError.CONFLICT
        InstructorMyPageFailureReason.INVALID_REQUEST -> FacilityRegistrationUiError.INVALID_REQUEST
        else -> FacilityRegistrationUiError.UNKNOWN
    }
