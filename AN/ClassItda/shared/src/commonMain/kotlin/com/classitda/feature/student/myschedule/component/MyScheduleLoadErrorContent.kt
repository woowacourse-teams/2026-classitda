package com.classitda.feature.student.myschedule.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.my_schedule_go_home
import classitda.shared.generated.resources.my_schedule_load_error_description
import classitda.shared.generated.resources.my_schedule_load_error_title
import classitda.shared.generated.resources.my_schedule_retry
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.appTypography
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MyScheduleLoadErrorContent(
    onRetry: () -> Unit,
    onGoHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val typography = appTypography()

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(
                    horizontal = AppSpacing.screenPadding,
                    vertical = AppSpacing.sectionGap,
                ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        MyScheduleLoadErrorIcon()

        Column(
            modifier = Modifier.padding(top = AppSpacing.sectionGap),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Text(
                text = stringResource(Res.string.my_schedule_load_error_title),
                modifier = Modifier.semantics { heading() },
                style = typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                color = StuColors.TextPrimary,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(Res.string.my_schedule_load_error_description),
                modifier = Modifier.padding(horizontal = AppSpacing.xxxl),
                style = typography.bodyMedium,
                color = StuColors.TextSecondary,
                textAlign = TextAlign.Center,
            )
        }

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = AppSpacing.sectionGap),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
        ) {
            MySchedulePrimaryButton(
                text = stringResource(Res.string.my_schedule_retry),
                onClick = onRetry,
            )
            MyScheduleSecondaryButton(
                text = stringResource(Res.string.my_schedule_go_home),
                onClick = onGoHome,
            )
        }
    }
}

@Preview(
    name = "Load error content / Student / Default",
    group = "Component/MySchedule",
    showBackground = true,
    widthDp = 390,
    heightDp = 704,
)
@Composable
private fun `MyScheduleLoadErrorContentPreview_Default_STUDENT_Default`() {
    AppTheme(theme = ThemeType.STUDENT) {
        MyScheduleLoadErrorContent(
            onRetry = {},
            onGoHome = {},
            modifier = Modifier.fillMaxSize(),
        )
    }
}
