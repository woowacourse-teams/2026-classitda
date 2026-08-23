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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.instructor_member_registration_harness_back
import classitda.shared.generated.resources.instructor_member_registration_harness_default
import classitda.shared.generated.resources.instructor_member_registration_harness_disabled
import classitda.shared.generated.resources.instructor_member_registration_harness_errors
import classitda.shared.generated.resources.instructor_member_registration_harness_failed
import classitda.shared.generated.resources.instructor_member_registration_harness_input
import classitda.shared.generated.resources.instructor_member_registration_harness_last_action
import classitda.shared.generated.resources.instructor_member_registration_harness_name_changed
import classitda.shared.generated.resources.instructor_member_registration_harness_no_action
import classitda.shared.generated.resources.instructor_member_registration_harness_open_confirmation
import classitda.shared.generated.resources.instructor_member_registration_harness_other_action
import classitda.shared.generated.resources.instructor_member_registration_harness_phone_changed
import classitda.shared.generated.resources.instructor_member_registration_harness_submitting
import classitda.shared.generated.resources.instructor_member_registration_harness_waiting
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.appTypography
import com.classitda.domain.model.instructor.mypage.MemberRegistrationDraft
import com.classitda.feature.instructor.mypage.MemberRegistrationScreen
import com.classitda.feature.instructor.mypage.contract.MemberRegistrationAction
import com.classitda.feature.instructor.mypage.contract.MemberRegistrationField
import com.classitda.feature.instructor.mypage.contract.MemberRegistrationUiState
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MemberRegistrationInteractionHarness(modifier: Modifier = Modifier) {
    val actions = remember { mutableStateListOf<MemberRegistrationAction>() }
    val emptyDraft = MemberRegistrationDraft()
    var uiState by remember {
        mutableStateOf<MemberRegistrationUiState>(
            MemberRegistrationUiState.Editing(
                draft = emptyDraft,
                canSubmit = false,
            ),
        )
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        MemberRegistrationScreen(
            uiState = uiState,
            onAction = { action ->
                actions += action
                when (action) {
                    is MemberRegistrationAction.NameChanged -> {
                        val currentDraft = uiState.draftOrEmpty()
                        val nextDraft = currentDraft.copy(name = action.name)
                        uiState = editingState(nextDraft)
                    }

                    is MemberRegistrationAction.PhoneNumberChanged -> {
                        val currentDraft = uiState.draftOrEmpty()
                        val nextDraft = currentDraft.copy(phoneNumber = action.phoneNumber)
                        uiState = editingState(nextDraft)
                    }

                    MemberRegistrationAction.OpenConfirmation -> {
                        val draft = uiState.draftOrEmpty()
                        if (draft.name.isNotBlank() && draft.phoneNumber.isNotBlank()) {
                            uiState = MemberRegistrationUiState.Confirmation(draft)
                        }
                    }

                    MemberRegistrationAction.ConfirmRegistration -> {
                        val draft = uiState.draftOrEmpty()
                        uiState = MemberRegistrationUiState.Submitting(draft)
                    }

                    MemberRegistrationAction.CancelConfirmation -> {
                        uiState = editingState(uiState.draftOrEmpty())
                    }

                    MemberRegistrationAction.Retry -> {
                        uiState = MemberRegistrationUiState.Confirmation(uiState.draftOrEmpty())
                    }

                    else -> {}
                }
            },
            modifier = Modifier.weight(1f),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            TextButton(onClick = { uiState = editingState(emptyDraft) }) {
                Text(stringResource(Res.string.instructor_member_registration_harness_default))
            }
            TextButton(
                onClick = {
                    uiState =
                        editingState(
                            MemberRegistrationDraft(name = "김민지", phoneNumber = "01012345678"),
                        )
                },
            ) {
                Text(stringResource(Res.string.instructor_member_registration_harness_input))
            }
            TextButton(
                onClick = {
                    uiState =
                        MemberRegistrationUiState.Editing(
                            draft = MemberRegistrationDraft(phoneNumber = "010"),
                            canSubmit = false,
                            fieldErrors =
                                setOf(
                                    MemberRegistrationField.NAME,
                                    MemberRegistrationField.PHONE_NUMBER,
                                ),
                        )
                },
            ) {
                Text(stringResource(Res.string.instructor_member_registration_harness_errors))
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            TextButton(onClick = { uiState = editingState(MemberRegistrationDraft()) }) {
                Text(stringResource(Res.string.instructor_member_registration_harness_disabled))
            }
            TextButton(
                onClick = {
                    uiState =
                        MemberRegistrationUiState.Confirmation(
                            MemberRegistrationDraft(name = "김민지", phoneNumber = "01012345678"),
                        )
                },
            ) {
                Text(stringResource(Res.string.instructor_member_registration_harness_waiting))
            }
            TextButton(
                onClick = {
                    uiState =
                        MemberRegistrationUiState.Submitting(
                            MemberRegistrationDraft(name = "김민지", phoneNumber = "01012345678"),
                        )
                },
            ) {
                Text(stringResource(Res.string.instructor_member_registration_harness_submitting))
            }
            TextButton(
                onClick = {
                    uiState =
                        MemberRegistrationUiState.Error(
                            draft = MemberRegistrationDraft(name = "김민지", phoneNumber = "01012345678"),
                            reason = com.classitda.feature.instructor.mypage.contract.MemberRegistrationUiError.NETWORK,
                        )
                },
            ) {
                Text(stringResource(Res.string.instructor_member_registration_harness_failed))
            }
        }
        val lastActionText =
            when (val action = actions.lastOrNull()) {
                null -> {
                    stringResource(Res.string.instructor_member_registration_harness_no_action)
                }

                MemberRegistrationAction.Back -> {
                    stringResource(Res.string.instructor_member_registration_harness_back)
                }

                is MemberRegistrationAction.NameChanged -> {
                    stringResource(
                        Res.string.instructor_member_registration_harness_name_changed,
                        action.name,
                    )
                }

                is MemberRegistrationAction.PhoneNumberChanged -> {
                    stringResource(
                        Res.string.instructor_member_registration_harness_phone_changed,
                        action.phoneNumber,
                    )
                }

                MemberRegistrationAction.OpenConfirmation -> {
                    stringResource(Res.string.instructor_member_registration_harness_open_confirmation)
                }

                else -> {
                    stringResource(Res.string.instructor_member_registration_harness_other_action)
                }
            }
        Text(
            text = stringResource(Res.string.instructor_member_registration_harness_last_action, lastActionText),
            modifier = Modifier.padding(horizontal = AppSpacing.screenPadding),
            style = appTypography().bodyMedium,
            color = InsColors.TextSecondary,
        )
    }
}

private fun MemberRegistrationUiState.draftOrEmpty(): MemberRegistrationDraft =
    when (this) {
        is MemberRegistrationUiState.Editing -> draft
        is MemberRegistrationUiState.Error -> draft
        is MemberRegistrationUiState.Confirmation -> draft
        is MemberRegistrationUiState.Submitting -> draft
        else -> MemberRegistrationDraft()
    }

private fun editingState(draft: MemberRegistrationDraft): MemberRegistrationUiState =
    MemberRegistrationUiState.Editing(
        draft = draft,
        canSubmit = draft.name.isNotBlank() && draft.phoneNumber.isNotBlank(),
    )

@Preview(
    name = "Interaction harness · Instructor",
    group = "Screen/MemberRegistration",
)
@Composable
private fun MemberRegistrationInteractionHarnessPreview() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        MemberRegistrationInteractionHarness()
    }
}
