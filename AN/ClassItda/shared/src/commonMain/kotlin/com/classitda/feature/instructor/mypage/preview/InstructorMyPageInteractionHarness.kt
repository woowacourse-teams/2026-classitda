package com.classitda.feature.instructor.mypage.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.instructor_facility_management_title
import classitda.shared.generated.resources.instructor_member_management_title
import classitda.shared.generated.resources.instructor_my_page_title
import classitda.shared.generated.resources.my_page_privacy_policy
import classitda.shared.generated.resources.profile_view_title
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.appTypography
import com.classitda.domain.model.instructor.mypage.InstructorFacilityId
import com.classitda.domain.model.instructor.mypage.InstructorMemberId
import com.classitda.feature.common.privacypolicy.PrivacyPolicyAction
import com.classitda.feature.common.privacypolicy.PrivacyPolicyScreen
import com.classitda.feature.common.privacypolicy.PrivacyPolicyUiState
import com.classitda.feature.common.profile.PhoneNumberChangeScreen
import com.classitda.feature.common.profile.ProfileEditScreen
import com.classitda.feature.common.profile.ProfileViewScreen
import com.classitda.feature.common.profile.contract.MemberProfileUiModel
import com.classitda.feature.common.profile.contract.PhoneNumberChangeAction
import com.classitda.feature.common.profile.contract.PhoneNumberChangeUiState
import com.classitda.feature.common.profile.contract.ProfileEditAction
import com.classitda.feature.common.profile.contract.ProfileEditUiState
import com.classitda.feature.common.profile.contract.ProfileViewAction
import com.classitda.feature.common.profile.contract.ProfileViewUiState
import com.classitda.feature.instructor.mypage.InstructorMyPageScreen
import com.classitda.feature.instructor.mypage.contract.FacilityInputUiModel
import com.classitda.feature.instructor.mypage.contract.FacilityListUiModel
import com.classitda.feature.instructor.mypage.contract.FacilityManagementAction
import com.classitda.feature.instructor.mypage.contract.FacilityManagementUiState
import com.classitda.feature.instructor.mypage.contract.FacilityRegistrationAction
import com.classitda.feature.instructor.mypage.contract.FacilityRegistrationUiState
import com.classitda.feature.instructor.mypage.contract.FacilityUiModel
import com.classitda.feature.instructor.mypage.contract.InstructorMyPageAction
import com.classitda.feature.instructor.mypage.contract.InstructorMyPageUiModel
import com.classitda.feature.instructor.mypage.contract.InstructorMyPageUiState
import com.classitda.feature.instructor.mypage.contract.MemberInputUiModel
import com.classitda.feature.instructor.mypage.contract.MemberListUiModel
import com.classitda.feature.instructor.mypage.contract.MemberManagementAction
import com.classitda.feature.instructor.mypage.contract.MemberManagementUiState
import com.classitda.feature.instructor.mypage.contract.MemberRegistrationAction
import com.classitda.feature.instructor.mypage.contract.MemberRegistrationUiState
import com.classitda.feature.instructor.mypage.contract.MemberUiModel
import com.classitda.feature.instructor.mypage.facility.FacilityManagementScreen
import com.classitda.feature.instructor.mypage.facility.FacilityRegistrationScreen
import com.classitda.feature.instructor.mypage.member.MemberManagementScreen
import com.classitda.feature.instructor.mypage.member.MemberRegistrationScreen
import org.jetbrains.compose.resources.stringResource

/** Deterministic no-DI harness: it verifies Screen state/callback contracts only. */
@Composable
internal fun InstructorMyPageInteractionHarness(modifier: Modifier = Modifier) {
    var destination by remember { mutableStateOf(HarnessDestination.F01) }
    var lastEvent by remember { mutableStateOf("-") }
    var profileViewState by remember { mutableStateOf<ProfileViewUiState>(profileFixture) }
    var profileEditState by remember { mutableStateOf<ProfileEditUiState>(profileEditFixture) }
    var phoneState by remember {
        mutableStateOf<PhoneNumberChangeUiState>(PhoneNumberChangeUiState.Verified("01012345678", "123456"))
    }
    var memberState by remember {
        mutableStateOf<MemberManagementUiState>(MemberManagementUiState.Content(memberPageFixture))
    }
    var registrationState by remember {
        mutableStateOf<MemberRegistrationUiState>(MemberRegistrationUiState.Editing(MemberInputUiModel(), false))
    }
    var facilityState by remember {
        mutableStateOf<FacilityManagementUiState>(FacilityManagementUiState.Content(facilityPageFixture))
    }
    var facilityRegistrationState by remember {
        mutableStateOf<FacilityRegistrationUiState>(
            FacilityRegistrationUiState.Editing(FacilityInputUiModel(), false),
        )
    }

    Column(modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        when (destination) {
            HarnessDestination.F01 -> {
                InstructorMyPageScreen(InstructorMyPageUiState.Content(instructorProfileFixture), onAction = { action ->
                    when (action) {
                        InstructorMyPageAction.OpenProfile -> {
                            destination = HarnessDestination.F02
                            lastEvent =
                                "OpenProfile"
                        }

                        InstructorMyPageAction.OpenMemberManagement -> {
                            destination = HarnessDestination.F05
                            lastEvent =
                                "OpenMemberManagement"
                        }

                        InstructorMyPageAction.OpenFacilityManagement -> {
                            destination = HarnessDestination.F08
                            lastEvent =
                                "OpenFacilityManagement"
                        }

                        InstructorMyPageAction.OpenPrivacyPolicy -> {
                            destination = HarnessDestination.PRIVACY_POLICY
                            lastEvent = "OpenPrivacyPolicy"
                        }

                        InstructorMyPageAction.Retry -> {
                            lastEvent = "Retry:F01"
                        }
                    }
                }, modifier = Modifier.weight(1f))
            }

            HarnessDestination.PRIVACY_POLICY -> {
                PrivacyPolicyScreen(
                    uiState = PrivacyPolicyUiState.Content(),
                    onAction = { action ->
                        when (action) {
                            PrivacyPolicyAction.Back -> {
                                destination = HarnessDestination.F01
                                lastEvent = "Back:PrivacyPolicy"
                            }

                            PrivacyPolicyAction.Retry -> {
                                lastEvent = "Retry:PrivacyPolicy"
                            }

                            PrivacyPolicyAction.DismissBlockedNavigationNotice -> {
                                lastEvent = "DismissBlockedNavigationNotice:PrivacyPolicy"
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    content = {
                        Text(
                            text = stringResource(Res.string.my_page_privacy_policy),
                            modifier = Modifier.fillMaxSize().padding(AppSpacing.screenPadding),
                        )
                    },
                )
            }

            HarnessDestination.F02 -> {
                ProfileViewScreen(profileViewState, onAction = { action ->
                    when (action) {
                        ProfileViewAction.Back -> {
                            destination = HarnessDestination.F01
                            lastEvent = "Back:F02"
                        }

                        ProfileViewAction.OpenEdit -> {
                            destination = HarnessDestination.F03
                            lastEvent = "OpenEdit"
                        }

                        ProfileViewAction.RequestLogout -> {
                            lastEvent = "RequestLogout"
                        }

                        ProfileViewAction.RequestWithdrawal -> {
                            lastEvent = "RequestWithdrawal"
                        }

                        ProfileViewAction.Retry -> {
                            lastEvent = "Retry:F02"
                        }
                    }
                }, modifier = Modifier.weight(1f))
            }

            HarnessDestination.F03 -> {
                ProfileEditScreen(profileEditState, onAction = { action ->
                    when (action) {
                        ProfileEditAction.Back -> {
                            destination = HarnessDestination.F02
                            lastEvent = "Back:F03"
                        }

                        ProfileEditAction.OpenPhoneNumberChange -> {
                            destination = HarnessDestination.F04
                            lastEvent =
                                "OpenPhoneNumberChange"
                        }

                        ProfileEditAction.Save -> {
                            lastEvent = "Save:F03→refresh:F02"
                        }

                        ProfileEditAction.RequestPhotoChange -> {
                            lastEvent = "RequestPhotoChange"
                        }

                        is ProfileEditAction.NameChanged -> {
                            profileEditState = profileEditState.editName(action.name)
                            lastEvent =
                                "NameChanged"
                        }

                        ProfileEditAction.Retry -> {
                            lastEvent = "Retry:F03"
                        }
                    }
                }, modifier = Modifier.weight(1f))
            }

            HarnessDestination.F04 -> {
                PhoneNumberChangeScreen(phoneState, onAction = { action ->
                    when (action) {
                        PhoneNumberChangeAction.Back -> {
                            destination = HarnessDestination.F03
                            lastEvent = "Back:F04"
                        }

                        PhoneNumberChangeAction.Complete -> {
                            destination = HarnessDestination.F02
                            lastEvent =
                                "Complete:F04→refresh:F02"
                        }

                        else -> {
                            lastEvent = action::class.simpleName ?: "PhoneAction"
                        }
                    }
                }, modifier = Modifier.weight(1f))
            }

            HarnessDestination.F05 -> {
                MemberManagementScreen(memberState, onAction = { action ->
                    when (action) {
                        MemberManagementAction.Back -> {
                            destination = HarnessDestination.F01
                            lastEvent = "Back:F05"
                        }

                        is MemberManagementAction.EditMember -> {
                            lastEvent = "EditMember:${action.memberId.value}"
                        }

                        is MemberManagementAction.RequestDelete -> {
                            lastEvent = "RequestDelete:${action.memberId.value}"
                        }

                        is MemberManagementAction.DeleteNameChanged -> {
                            lastEvent = "DeleteNameChanged"
                        }

                        MemberManagementAction.CancelDelete -> {
                            lastEvent = "CancelDelete"
                        }

                        MemberManagementAction.ConfirmDelete -> {
                            lastEvent = "ConfirmDelete"
                        }

                        MemberManagementAction.DeleteAcknowledged -> {
                            lastEvent = "DeleteAcknowledged"
                        }

                        MemberManagementAction.OpenMemberRegistration -> {
                            destination = HarnessDestination.F06
                            lastEvent =
                                "OpenMemberRegistration"
                        }

                        is MemberManagementAction.QueryChanged -> {
                            lastEvent = "QueryChanged:${action.query}"
                        }

                        is MemberManagementAction.SortOrderChanged -> {
                            lastEvent = "SortOrderChanged"
                        }

                        MemberManagementAction.Retry -> {
                            lastEvent = "Retry:F05"
                        }
                    }
                }, modifier = Modifier.weight(1f))
            }

            HarnessDestination.F06 -> {
                MemberRegistrationScreen(registrationState, onAction = { action ->
                    when (action) {
                        MemberRegistrationAction.Back -> {
                            destination = HarnessDestination.F05
                            lastEvent = "Back:F06"
                        }

                        MemberRegistrationAction.OpenConfirmation -> {
                            registrationState =
                                MemberRegistrationUiState.Confirmation(
                                    (registrationState as? MemberRegistrationUiState.Editing)?.draft
                                        ?: MemberInputUiModel(),
                                )
                            lastEvent =
                                "OpenConfirmation:F07"
                        }

                        MemberRegistrationAction.CancelConfirmation -> {
                            registrationState =
                                MemberRegistrationUiState.Editing(
                                    MemberInputUiModel("홍길동", "01012345678"),
                                    true,
                                )
                            lastEvent =
                                "CancelConfirmation:preserveDraft"
                        }

                        MemberRegistrationAction.ConfirmRegistration -> {
                            lastEvent = "ConfirmRegistration"
                        }

                        MemberRegistrationAction.Retry -> {
                            lastEvent = "Retry:F07"
                        }

                        is MemberRegistrationAction.SuccessAcknowledged -> {
                            destination = HarnessDestination.F05
                            lastEvent = "SuccessAcknowledged:${action.memberId.value}"
                        }

                        is MemberRegistrationAction.NameChanged -> {
                            registrationState =
                                MemberRegistrationUiState.Editing(
                                    MemberInputUiModel(action.name, "01012345678"),
                                    action.name.isNotBlank(),
                                )
                            lastEvent =
                                "NameChanged"
                        }

                        is MemberRegistrationAction.PhoneNumberChanged -> {
                            lastEvent = "PhoneNumberChanged"
                        }
                    }
                }, modifier = Modifier.weight(1f))
            }

            HarnessDestination.F08 -> {
                FacilityManagementScreen(facilityState, onAction = { action ->
                    when (action) {
                        FacilityManagementAction.Back -> {
                            destination = HarnessDestination.F01
                            lastEvent = "Back:F08"
                        }

                        is FacilityManagementAction.EditFacility -> {
                            lastEvent = "EditFacility:${action.facilityId.value}"
                        }

                        is FacilityManagementAction.OpenFacilityDetail -> {
                            lastEvent =
                                "OpenFacilityDetail:${action.facilityId.value}"
                        }

                        FacilityManagementAction.OpenFacilityRegistration -> {
                            destination = HarnessDestination.F09
                            lastEvent =
                                "OpenFacilityRegistration"
                        }

                        FacilityManagementAction.Retry -> {
                            lastEvent = "Retry:F08"
                        }
                    }
                }, modifier = Modifier.weight(1f))
            }

            HarnessDestination.F09 -> {
                FacilityRegistrationScreen(facilityRegistrationState, onAction = { action ->
                    when (action) {
                        FacilityRegistrationAction.Back -> {
                            destination = HarnessDestination.F08
                            lastEvent = "Back:F09"
                        }

                        FacilityRegistrationAction.RequestImageSource -> {
                            lastEvent = "RequestImageSource"
                        }

                        FacilityRegistrationAction.RequestAddressSearch -> {
                            lastEvent = "RequestAddressSearch"
                        }

                        is FacilityRegistrationAction.ImageSelected -> {
                            lastEvent = "ImageSelected"
                        }

                        is FacilityRegistrationAction.AddressSelected -> {
                            lastEvent = "AddressSelected:${action.address}"
                            facilityRegistrationState =
                                FacilityRegistrationUiState.Editing(
                                    FacilityInputUiModel(
                                        address = action.address,
                                    ),
                                    false,
                                )
                        }

                        FacilityRegistrationAction.Submit -> {
                            lastEvent = "Submit:F09"
                        }

                        else -> {
                            lastEvent = action::class.simpleName ?: "FacilityAction"
                        }
                    }
                }, modifier = Modifier.weight(1f))
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
            TextButton(onClick = {
                destination = HarnessDestination.F01
            }) { Text(stringResource(Res.string.instructor_my_page_title)) }
            TextButton(
                onClick = { destination = HarnessDestination.F02 },
            ) { Text(stringResource(Res.string.profile_view_title)) }
            TextButton(onClick = {
                destination = HarnessDestination.F05
            }) { Text(stringResource(Res.string.instructor_member_management_title)) }
            TextButton(onClick = {
                destination = HarnessDestination.F08
            }) { Text(stringResource(Res.string.instructor_facility_management_title)) }
        }
        Text(
            "last callback: $lastEvent",
            Modifier.padding(horizontal = AppSpacing.screenPadding),
            style = appTypography().bodyMedium,
            color = InsColors.TextSecondary,
        )
    }
}

private enum class HarnessDestination { F01, F02, F03, F04, F05, F06, F08, F09, PRIVACY_POLICY }

private val instructorProfileFixture = InstructorMyPageUiModel("이지은 강사", "010-****-5678", null, "이")
private val profileFixture =
    ProfileViewUiState.Content(
        MemberProfileUiModel("이지은 강사", "010-****-5678", "instructor@classitda.com", null),
    )
private val profileEditFixture =
    ProfileEditUiState.Editing(
        profileFixture.profile,
        "01012345678",
        "이지은 강사",
        false,
    )
private val memberPageFixture =
    MemberListUiModel(
        totalCount = 2,
        members = listOf(MemberUiModel(InstructorMemberId("member-1"), "김민지", "010-1234-5678", "김")),
    )
private val facilityPageFixture =
    FacilityListUiModel(
        totalCount = 1,
        facilities =
            listOf(
                FacilityUiModel(
                    InstructorFacilityId("facility-1"),
                    "클래스잇다 스튜디오",
                    com.classitda.domain.model.instructor.mypage.FacilityAddress(
                        roadAddress = "서울특별시 강남구 테헤란로",
                    ),
                ),
            ),
    )

private fun ProfileEditUiState.editName(name: String) =
    when (this) {
        is ProfileEditUiState.Editing -> {
            copy(
                draftName = name,
                canSave =
                    name.isNotBlank() && name != profile.name,
            )
        }

        else -> {
            this
        }
    }

@Preview(name = "Interaction harness · Instructor", group = "Screen/InstructorMyPage")
@Composable
private fun InstructorMyPageInteractionHarnessPreview() {
    AppTheme(theme = ThemeType.INSTRUCTOR) { InstructorMyPageInteractionHarness() }
}
