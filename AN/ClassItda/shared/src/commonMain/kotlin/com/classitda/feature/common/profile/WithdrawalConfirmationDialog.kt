package com.classitda.feature.common.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.withdrawal_cancel
import classitda.shared.generated.resources.withdrawal_confirm
import classitda.shared.generated.resources.withdrawal_confirm_message
import classitda.shared.generated.resources.withdrawal_confirm_title
import classitda.shared.generated.resources.withdrawal_submitting
import com.classitda.core.designsystem.StuColors
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun WithdrawalConfirmationDialog(
    isSubmitting: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = { Text(stringResource(Res.string.withdrawal_confirm_title)) },
        text = {
            Column {
                Text(stringResource(Res.string.withdrawal_confirm_message))
                errorMessage?.let {
                    Text(text = it, color = StuColors.Red)
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !isSubmitting,
                onClick = onConfirm,
            ) {
                Text(
                    text =
                        if (isSubmitting) {
                            stringResource(Res.string.withdrawal_submitting)
                        } else {
                            stringResource(Res.string.withdrawal_confirm)
                        },
                )
            }
        },
        dismissButton = {
            TextButton(
                enabled = !isSubmitting,
                onClick = onDismiss,
            ) {
                Text(stringResource(Res.string.withdrawal_cancel))
            }
        },
    )
}
