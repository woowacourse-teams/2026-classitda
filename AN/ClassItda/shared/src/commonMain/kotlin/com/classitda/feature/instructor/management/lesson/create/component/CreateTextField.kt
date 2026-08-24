package com.classitda.feature.instructor.management.lesson.create.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.unit.dp
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.ThemeType

@Composable
internal fun CreateTextField(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    trailingText: String? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = InsColors.TextPrimary,
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            placeholder = {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodySmall,
                    color = InsColors.TextTertiary,
                )
            },
            trailingIcon =
                trailingText?.let {
                    {
                        Text(
                            text = it,
                            modifier = Modifier.padding(end = AppSpacing.md),
                            style = MaterialTheme.typography.labelSmall,
                            color = InsColors.TextSecondary,
                        )
                    }
                },
            singleLine = singleLine,
            minLines = minLines,
            shape = AppShape.Card,
            textStyle = MaterialTheme.typography.bodySmall,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = createTextFieldColors(),
        )
    }
}

@Composable
private fun createTextFieldColors() =
    OutlinedTextFieldDefaults.colors(
        focusedContainerColor = InsColors.Surface,
        unfocusedContainerColor = InsColors.Surface,
        focusedBorderColor = InsColors.Black,
        unfocusedBorderColor = InsColors.Divider,
        cursorColor = InsColors.Black,
    )

@Composable
@Preview
private fun CreateTextFieldPreview() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        CreateTextField(
            label = "수업명 *",
            value = "",
            placeholder = "예: 리포머 비기너 클래스",
            onValueChange = {},
        )
    }
}
