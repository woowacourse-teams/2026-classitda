package com.classitda.feature.instructor.classsession.member.edit.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.ic_search
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.ThemeType
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun ClassSessionMemberAddSection(
    onExistingAddClick: () -> Unit,
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
        OutlinedTextField(
            value = "",
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth().clickable(onClick = onExistingAddClick),
            leadingIcon = {
                androidx.compose.material3.Icon(
                    painter = painterResource(Res.drawable.ic_search),
                    contentDescription = null,
                    tint = InsColors.TextSecondary,
                )
            },
            placeholder = {
                Text(
                    text = "회원 이름 검색",
                    style = MaterialTheme.typography.bodySmall,
                    color = InsColors.TextTertiary,
                )
            },
            singleLine = true,
            shape = AppShape.Card,
            colors = memberEditTextFieldColors(),
        )
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
            onExistingAddClick = {},
        )
    }
}
