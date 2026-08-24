package com.classitda.feature.instructor.management.`class`.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.ic_check
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.domain.model.instructor.management.ClassSessionStatus
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun ClassSessionStatusBadge(
    status: ClassSessionStatus,
    modifier: Modifier = Modifier,
) {
    when (status) {
        ClassSessionStatus.SCHEDULED -> {
            Surface(
                modifier = modifier,
                shape = AppShape.Pill,
                color = InsColors.PurpleLight,
                contentColor = InsColors.Purple,
            ) {
                Row(
                    modifier =
                        Modifier.padding(
                            horizontal = AppSpacing.pillChipHorizontalPadding,
                            vertical = AppSpacing.pillChipVerticalPadding,
                        ),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_check),
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = "수업 예정",
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }

        ClassSessionStatus.CANCELLED -> {
            Text(
                text = "취소됨",
                modifier = modifier,
                style = MaterialTheme.typography.labelMedium,
                color = InsColors.Red,
            )
        }

        ClassSessionStatus.COMPLETED -> {
            Text(
                text = "완료",
                modifier = modifier,
                style = MaterialTheme.typography.labelMedium,
                color = InsColors.TextTertiary,
            )
        }
    }
}

@Composable
@Preview
private fun ClassSessionStatusBadgePreview() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.lg)) {
            ClassSessionStatusBadge(status = ClassSessionStatus.SCHEDULED)
            ClassSessionStatusBadge(status = ClassSessionStatus.CANCELLED)
            ClassSessionStatusBadge(status = ClassSessionStatus.COMPLETED)
        }
    }
}
