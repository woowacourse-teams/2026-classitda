package com.classitda.feature.student.myschedule.component.detail.reservation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.StuColors
import com.classitda.core.designsystem.appTypography
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun ReservationDetailInformationRow(
    icon: DrawableResource,
    label: String,
    content: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.lg),
        verticalAlignment = Alignment.Top,
    ) {
        Surface(
            shape = AppShape.Card,
            color = StuColors.Background,
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                modifier =
                    Modifier
                        .padding(AppSpacing.md)
                        .size(AppSpacing.xxl),
                tint = StuColors.TextTertiary,
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            Text(
                text = label,
                style = appTypography().bodySmall,
                color = StuColors.TextTertiary,
            )
            content()
        }
    }
}
