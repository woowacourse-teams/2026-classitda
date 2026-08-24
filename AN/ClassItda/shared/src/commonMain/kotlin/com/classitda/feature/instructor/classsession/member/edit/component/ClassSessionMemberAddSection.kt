package com.classitda.feature.instructor.classsession.member.edit.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.feature.instructor.classsession.member.edit.model.MemberAddType
import com.classitda.feature.instructor.management.lesson.create.component.OutlinedSegmentedToggle

@Composable
internal fun ClassSessionMemberAddSection(
    addType: MemberAddType,
    temporaryName: String,
    onAddTypeChange: (MemberAddType) -> Unit,
    onTemporaryNameChange: (String) -> Unit,
    onExistingAddClick: () -> Unit,
    onTemporaryAddClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    androidx.compose.foundation.layout.Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        Text(
            text = "회원 추가",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = InsColors.TextPrimary,
        )
        OutlinedSegmentedToggle(
            options = listOf("기존 회원", "임시 회원"),
            selectedIndex = addType.ordinal,
            onOptionSelected = { index -> onAddTypeChange(MemberAddType.entries[index]) },
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            if (addType == MemberAddType.EXISTING) {
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.weight(1f).clickable(onClick = onExistingAddClick),
                    placeholder = {
                        Text(
                            text = "회원 이름을 검색해 주세요",
                            style = MaterialTheme.typography.bodySmall,
                            color = InsColors.TextTertiary,
                        )
                    },
                    singleLine = true,
                    shape = AppShape.Card,
                    colors = memberEditTextFieldColors(),
                )
            } else {
                OutlinedTextField(
                    value = temporaryName,
                    onValueChange = onTemporaryNameChange,
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(
                            text = "이름을 입력하세요",
                            style = MaterialTheme.typography.bodySmall,
                            color = InsColors.TextTertiary,
                        )
                    },
                    singleLine = true,
                    shape = AppShape.Card,
                    keyboardOptions = KeyboardOptions.Default,
                    colors = memberEditTextFieldColors(),
                )
            }
            Button(
                onClick =
                    if (addType == MemberAddType.EXISTING) {
                        onExistingAddClick
                    } else {
                        onTemporaryAddClick
                    },
                enabled = addType == MemberAddType.EXISTING || temporaryName.isNotBlank(),
                modifier = Modifier.padding(top = 4.dp),
                shape = AppShape.Card,
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = InsColors.Purple,
                        contentColor = InsColors.White,
                        disabledContainerColor = InsColors.Gray200,
                        disabledContentColor = InsColors.TextTertiary,
                    ),
            ) {
                Text("추가")
            }
        }
    }
}

@Composable
private fun memberEditTextFieldColors() =
    OutlinedTextFieldDefaults.colors(
        focusedContainerColor = InsColors.White,
        unfocusedContainerColor = InsColors.White,
        focusedBorderColor = InsColors.Purple,
        unfocusedBorderColor = InsColors.Divider,
        cursorColor = InsColors.Purple,
    )

@Preview(name = "회원 추가 - 기존 회원", showBackground = true, widthDp = 350)
@Composable
private fun ClassSessionMemberAddSectionExistingPreview() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        ClassSessionMemberAddSection(
            addType = MemberAddType.EXISTING,
            temporaryName = "",
            onAddTypeChange = {},
            onTemporaryNameChange = {},
            onExistingAddClick = {},
            onTemporaryAddClick = {},
        )
    }
}

@Preview(name = "회원 추가 - 임시 회원", showBackground = true, widthDp = 350)
@Composable
private fun ClassSessionMemberAddSectionTemporaryPreview() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        ClassSessionMemberAddSection(
            addType = MemberAddType.TEMPORARY,
            temporaryName = "박지수",
            onAddTypeChange = {},
            onTemporaryNameChange = {},
            onExistingAddClick = {},
            onTemporaryAddClick = {},
        )
    }
}
