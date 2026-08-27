package com.classitda.feature.instructor.classsession.detail.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
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
import classitda.shared.generated.resources.ic_person
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.feature.instructor.classsession.detail.model.ClassSessionMemberUiModel
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun ClassSessionMemberRow(
    member: ClassSessionMemberUiModel,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShape.Card,
        color = InsColors.White,
    ) {
        Row(
            modifier = Modifier.padding(AppSpacing.md),
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
                style = MaterialTheme.typography.bodyMedium,
                color = InsColors.TextPrimary,
            )
        }
    }
}

@Preview(name = "예약 회원", showBackground = true, widthDp = 350)
@Composable
private fun ClassSessionMemberRowPreview() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        ClassSessionMemberRow(
            member = ClassSessionMemberUiModel(id = "member-1", name = "김민지"),
        )
    }
}
