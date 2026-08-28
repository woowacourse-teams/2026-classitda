package com.classitda.feature.instructor.classsession.edit.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.ThemeType

@Composable
internal fun EditUnitTextField(
    label: String,
    value: String,
    placeholder: String,
    unit: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(EditFieldDefaults.labelFieldGap),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = InsColors.TextPrimary,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f).height(EditFieldDefaults.height),
                placeholder = {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodySmall,
                        color = InsColors.TextTertiary,
                    )
                },
                singleLine = true,
                shape = AppShape.Card,
                textStyle = MaterialTheme.typography.bodySmall,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                colors = editTextFieldColors(),
            )
            Text(
                text = unit,
                modifier = Modifier.padding(start = AppSpacing.sm),
                style = MaterialTheme.typography.labelSmall,
                color = InsColors.TextSecondary,
            )
        }
    }
}

@Composable
private fun editTextFieldColors() =
    OutlinedTextFieldDefaults.colors(
        focusedContainerColor = InsColors.Surface,
        unfocusedContainerColor = InsColors.Surface,
        focusedBorderColor = InsColors.Black,
        unfocusedBorderColor = InsColors.Divider,
        cursorColor = InsColors.Black,
    )

@Preview(name = "수정 화면 단위 입력란", showBackground = true)
@Composable
private fun EditUnitTextFieldPreview() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        EditUnitTextField(
            label = "기본 정원 *",
            value = "8",
            placeholder = "0",
            unit = "명",
            onValueChange = {},
            keyboardType = KeyboardType.Number,
        )
    }
}
