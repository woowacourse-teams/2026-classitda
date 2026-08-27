package com.classitda.feature.instructor.classsession.member.edit.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.ic_close
import classitda.shared.generated.resources.ic_person
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.feature.instructor.classsession.detail.model.ClassSessionMemberUiModel
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun ClassSessionMemberEditBookedRow(
    member: ClassSessionMemberUiModel,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShape.Card,
        color = InsColors.White,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            Surface(
                modifier = Modifier.size(40.dp).clip(CircleShape),
                shape = CircleShape,
                color = InsColors.SurfaceVariant,
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_person),
                    contentDescription = null,
                    tint = InsColors.TextTertiary,
                    modifier = Modifier.padding(9.dp),
                )
            }
            Text(
                text = member.name,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = InsColors.TextPrimary,
            )
            IconButton(
                onClick = onRemoveClick,
                modifier = Modifier.size(40.dp),
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_close),
                    contentDescription = "${member.name} 회원 제거",
                    tint = InsColors.TextSecondary,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Preview(name = "회원 수정 행", showBackground = true, widthDp = 350)
@Composable
private fun ClassSessionMemberEditBookedRowPreview() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        ClassSessionMemberEditBookedRow(
            member = ClassSessionMemberUiModel(id = "member-1", name = "김민지"),
            onRemoveClick = {},
        )
    }
}
