package com.classitda.feature.instructor.classsession.edit.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
internal fun EditTextField(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    minLines: Int = 1,
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
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .then(
                        if (singleLine) {
                            Modifier.height(EditFieldDefaults.height)
                        } else {
                            Modifier.heightIn(min = EditFieldDefaults.height)
                        },
                    ),
            placeholder = {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodySmall,
                    color = InsColors.TextTertiary,
                )
            },
            singleLine = singleLine,
            minLines = minLines,
            shape = AppShape.Card,
            textStyle = MaterialTheme.typography.labelMedium,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = editTextFieldColors(),
        )
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

@Preview(name = "수정 화면 텍스트 입력란", showBackground = true)
@Composable
private fun EditTextFieldPreview() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        EditTextField(
            label = "수업명 *",
            value = "리포머 밸런스",
            placeholder = "수업명을 입력해 주세요",
            onValueChange = {},
        )
    }
}
