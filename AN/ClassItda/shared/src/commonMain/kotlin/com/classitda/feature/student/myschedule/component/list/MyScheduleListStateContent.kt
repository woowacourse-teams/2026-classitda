package com.classitda.feature.student.myschedule.component.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.my_schedule_empty_history_action
import classitda.shared.generated.resources.my_schedule_empty_history_description
import classitda.shared.generated.resources.my_schedule_empty_history_title
import classitda.shared.generated.resources.my_schedule_empty_upcoming_action
import classitda.shared.generated.resources.my_schedule_empty_upcoming_description
import classitda.shared.generated.resources.my_schedule_empty_upcoming_title
import classitda.shared.generated.resources.my_schedule_load_error_description
import classitda.shared.generated.resources.my_schedule_load_error_title
import classitda.shared.generated.resources.my_schedule_loading
import classitda.shared.generated.resources.my_schedule_refresh_error
import classitda.shared.generated.resources.my_schedule_refreshing
import classitda.shared.generated.resources.my_schedule_retry
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.StuColors
import com.classitda.core.designsystem.appTypography
import com.classitda.feature.student.myschedule.component.common.MySchedulePrimaryButton
import com.classitda.feature.student.myschedule.component.common.MyScheduleSecondaryButton
import com.classitda.feature.student.myschedule.contract.MyScheduleListErrorUiModel
import com.classitda.feature.student.myschedule.contract.MyScheduleRefreshState
import com.classitda.feature.student.myschedule.contract.MyScheduleTab
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun MyScheduleLoadingContent(modifier: Modifier = Modifier) {
    val loadingDescription = stringResource(Res.string.my_schedule_loading)

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .semantics { contentDescription = loadingDescription },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(color = StuColors.Green)
        Text(
            text = loadingDescription,
            modifier = Modifier.padding(top = AppSpacing.lg),
            style = appTypography().bodyMedium,
            color = StuColors.TextSecondary,
        )
    }
}

@Composable
internal fun MyScheduleEmptyContent(
    tab: MyScheduleTab,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val content = tab.emptyContent()

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(
                    horizontal = AppSpacing.screenPadding,
                    vertical = AppSpacing.sectionGap,
                ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(content.title),
            modifier = Modifier.semantics { heading() },
            style = appTypography().titleLarge.copy(fontWeight = FontWeight.SemiBold),
            color = StuColors.TextPrimary,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(content.description),
            modifier = Modifier.padding(top = AppSpacing.sm),
            style = appTypography().bodyMedium,
            color = StuColors.TextSecondary,
            textAlign = TextAlign.Center,
        )
        MyScheduleSecondaryButton(
            text = stringResource(content.action),
            onClick = onAction,
            modifier = Modifier.padding(top = AppSpacing.sectionGap),
        )
    }
}

@Composable
internal fun MyScheduleInitialErrorContent(
    error: MyScheduleListErrorUiModel,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(
                    horizontal = AppSpacing.screenPadding,
                    vertical = AppSpacing.sectionGap,
                ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(Res.string.my_schedule_load_error_title),
            modifier = Modifier.semantics { heading() },
            style = appTypography().titleLarge.copy(fontWeight = FontWeight.SemiBold),
            color = StuColors.TextPrimary,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(error.descriptionResource()),
            modifier = Modifier.padding(top = AppSpacing.sm),
            style = appTypography().bodyMedium,
            color = StuColors.TextSecondary,
            textAlign = TextAlign.Center,
        )
        MySchedulePrimaryButton(
            text = stringResource(Res.string.my_schedule_retry),
            onClick = onRetry,
            modifier = Modifier.padding(top = AppSpacing.sectionGap),
        )
    }
}

@Composable
internal fun MyScheduleRefreshStatus(
    state: MyScheduleRefreshState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state) {
        MyScheduleRefreshState.Idle -> {
            // 정상 콘텐츠에는 별도의 상태 표시를 추가하지 않는다.
        }

        MyScheduleRefreshState.Refreshing -> {
            val refreshingDescription = stringResource(Res.string.my_schedule_refreshing)
            LinearProgressIndicator(
                modifier =
                    modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = refreshingDescription },
                color = StuColors.Green,
            )
        }

        is MyScheduleRefreshState.Failed -> {
            Surface(
                modifier =
                    modifier
                        .fillMaxWidth()
                        .semantics { liveRegion = LiveRegionMode.Polite },
                color = StuColors.RedLight,
                contentColor = StuColors.Red,
            ) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = AppSpacing.screenPadding),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(Res.string.my_schedule_refresh_error),
                        modifier = Modifier.weight(1f),
                        style = appTypography().bodyMedium,
                    )
                    TextButton(
                        onClick = onRetry,
                        colors = ButtonDefaults.textButtonColors(contentColor = StuColors.Red),
                    ) {
                        Text(
                            text = stringResource(Res.string.my_schedule_retry),
                            style = appTypography().labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        )
                    }
                }
            }
        }
    }
}

private data class EmptyContentResources(
    val title: StringResource,
    val description: StringResource,
    val action: StringResource,
)

private fun MyScheduleTab.emptyContent(): EmptyContentResources =
    when (this) {
        MyScheduleTab.UPCOMING -> {
            EmptyContentResources(
                title = Res.string.my_schedule_empty_upcoming_title,
                description = Res.string.my_schedule_empty_upcoming_description,
                action = Res.string.my_schedule_empty_upcoming_action,
            )
        }

        MyScheduleTab.HISTORY -> {
            EmptyContentResources(
                title = Res.string.my_schedule_empty_history_title,
                description = Res.string.my_schedule_empty_history_description,
                action = Res.string.my_schedule_empty_history_action,
            )
        }
    }

private fun MyScheduleListErrorUiModel.descriptionResource(): StringResource =
    when (this) {
        MyScheduleListErrorUiModel.NETWORK -> Res.string.my_schedule_load_error_description
        MyScheduleListErrorUiModel.UNKNOWN -> Res.string.my_schedule_load_error_description
    }
