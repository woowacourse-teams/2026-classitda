package com.classitda.feature.instructor.classsession.member.edit.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.ic_add
import classitda.shared.generated.resources.ic_check
import classitda.shared.generated.resources.ic_person
import classitda.shared.generated.resources.ic_search
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.feature.instructor.classsession.detail.model.ClassSessionMemberUiModel
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ExistingMemberBottomSheet(
    members: List<ClassSessionMemberUiModel>,
    query: String,
    selectedMemberIds: Set<String>,
    onQueryChange: (String) -> Unit,
    onMemberClick: (String) -> Unit,
    onConfirmClick: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = InsColors.White,
        modifier = modifier,
    ) {
        ExistingMemberBottomSheetContent(
            members = members,
            query = query,
            selectedMemberIds = selectedMemberIds,
            onQueryChange = onQueryChange,
            onMemberClick = onMemberClick,
            onConfirmClick = onConfirmClick,
        )
    }
}

@Composable
internal fun ExistingMemberBottomSheetContent(
    members: List<ClassSessionMemberUiModel>,
    query: String,
    selectedMemberIds: Set<String>,
    onQueryChange: (String) -> Unit,
    onMemberClick: (String) -> Unit,
    onConfirmClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = AppSpacing.screenPadding)
                .padding(bottom = AppSpacing.xl),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        Text(
            text = "기존 회원 추가",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = InsColors.TextPrimary,
        )
        Text(
            text = "수업에 추가할 회원을 선택하세요.",
            style = MaterialTheme.typography.bodySmall,
            color = InsColors.TextSecondary,
        )
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(
                    painter = painterResource(Res.drawable.ic_search),
                    contentDescription = null,
                    tint = InsColors.TextSecondary,
                )
            },
            placeholder = { Text("회원 이름 검색") },
            singleLine = true,
            shape = AppShape.Card,
            keyboardOptions = KeyboardOptions.Default,
            colors =
                OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = InsColors.White,
                    unfocusedContainerColor = InsColors.White,
                    focusedBorderColor = InsColors.Purple,
                    unfocusedBorderColor = InsColors.Divider,
                    cursorColor = InsColors.Purple,
                ),
        )
        LazyColumn(
            modifier = Modifier.fillMaxWidth().heightIn(max = 240.dp),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            items(members, key = { it.id }) { member ->
                ExistingMemberRow(
                    member = member,
                    isSelected = member.id in selectedMemberIds,
                    onClick = { onMemberClick(member.id) },
                )
            }
        }
        Button(
            onClick = onConfirmClick,
            enabled = selectedMemberIds.isNotEmpty(),
            modifier = Modifier.fillMaxWidth(),
            shape = AppShape.Card,
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = InsColors.Primary,
                    contentColor = InsColors.White,
                    disabledContainerColor = InsColors.Gray200,
                    disabledContentColor = InsColors.TextTertiary,
                ),
        ) {
            Text("회원 추가")
        }
    }
}

@Composable
private fun ExistingMemberRow(
    member: ClassSessionMemberUiModel,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = AppShape.Card,
        color = InsColors.White,
    ) {
        Row(
            modifier = Modifier.padding(vertical = AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            Surface(
                modifier = Modifier.size(36.dp).clip(CircleShape),
                shape = CircleShape,
                color = InsColors.SurfaceVariant,
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_person),
                    contentDescription = null,
                    tint = InsColors.TextTertiary,
                    modifier = Modifier.padding(8.dp),
                )
            }
            Text(
                text = member.name,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = InsColors.TextPrimary,
            )
            Icon(
                painter =
                    painterResource(
                        if (isSelected) Res.drawable.ic_check else Res.drawable.ic_add,
                    ),
                contentDescription = if (isSelected) "선택됨" else "회원 선택",
                tint = if (isSelected) InsColors.Purple else InsColors.TextPrimary,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Preview(name = "기존 회원 선택 행", showBackground = true, widthDp = 350)
@Composable
private fun ExistingMemberRowPreview() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        ExistingMemberRow(
            member = ClassSessionMemberUiModel(id = "member-4", name = "최유진"),
            isSelected = true,
            onClick = {},
        )
    }
}

@Preview(name = "기존 회원 추가 바텀시트", showBackground = true, widthDp = 390, heightDp = 600)
@Composable
private fun ExistingMemberBottomSheetPreview() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        ExistingMemberBottomSheetContent(
            members =
                listOf(
                    ClassSessionMemberUiModel(id = "member-4", name = "최유진"),
                    ClassSessionMemberUiModel(id = "member-5", name = "정하늘"),
                    ClassSessionMemberUiModel(id = "member-6", name = "김서연"),
                ),
            query = "",
            selectedMemberIds = setOf("member-4"),
            onQueryChange = {},
            onMemberClick = {},
            onConfirmClick = {},
        )
    }
}
