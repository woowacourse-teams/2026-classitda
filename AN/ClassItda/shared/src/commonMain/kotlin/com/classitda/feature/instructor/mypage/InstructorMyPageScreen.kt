package com.classitda.feature.instructor.mypage

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.ic_arrow_forward
import classitda.shared.generated.resources.instructor_my_page_edit_profile
import classitda.shared.generated.resources.instructor_my_page_description
import classitda.shared.generated.resources.instructor_my_page_error_description
import classitda.shared.generated.resources.instructor_my_page_error_title
import classitda.shared.generated.resources.instructor_my_page_loading
import classitda.shared.generated.resources.instructor_my_page_member_management
import classitda.shared.generated.resources.instructor_my_page_retry
import classitda.shared.generated.resources.instructor_my_page_studio_management
import classitda.shared.generated.resources.instructor_my_page_title
import classitda.shared.generated.resources.my_page_privacy_policy
import coil3.compose.SubcomposeAsyncImage
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.appTypography
import com.classitda.feature.instructor.mypage.contract.InstructorMyPageAction
import com.classitda.feature.instructor.mypage.contract.InstructorMyPageUiError
import com.classitda.feature.instructor.mypage.contract.InstructorMyPageUiModel
import com.classitda.feature.instructor.mypage.contract.InstructorMyPageUiState
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun InstructorMyPageScreen(
    uiState: InstructorMyPageUiState,
    onAction: (InstructorMyPageAction) -> Unit,
    bottomBar: @Composable () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = InsColors.Background,
        bottomBar = bottomBar,
    ) { innerPadding ->
        when (uiState) {
            InstructorMyPageUiState.Loading -> {
                InstructorMyPageLoading(
                    modifier = Modifier.padding(innerPadding),
                )
            }

            is InstructorMyPageUiState.Content -> {
                InstructorMyPageContent(
                    profile = uiState.profile,
                    onAction = onAction,
                    modifier = Modifier.padding(innerPadding),
                )
            }

            is InstructorMyPageUiState.Error -> {
                InstructorMyPageError(
                    onRetry = { onAction(InstructorMyPageAction.Retry) },
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }
    }
}

@Composable
private fun InstructorMyPageContent(
    profile: InstructorMyPageUiModel,
    onAction: (InstructorMyPageAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(AppSpacing.screenPadding),
    ) {
        Text(
            text = stringResource(Res.string.instructor_my_page_title),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .semantics { heading() },
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = InsColors.TextPrimary,
        )
        Text(
            text = stringResource(Res.string.instructor_my_page_description),
            modifier = Modifier.padding(top = AppSpacing.xs, bottom = AppSpacing.lg),
            style = MaterialTheme.typography.bodyMedium,
            color = InsColors.TextSecondary,
        )

        ProfileSummary(
            profile = profile,
            onClick = { onAction(InstructorMyPageAction.OpenProfile) },
        )

        Spacer(modifier = Modifier.size(AppSpacing.sectionGap))

        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            InstructorMyPageMenuRow(
                title = stringResource(Res.string.instructor_my_page_member_management),
                onClick = { onAction(InstructorMyPageAction.OpenMemberManagement) },
            )
            InstructorMyPageMenuRow(
                title = stringResource(Res.string.instructor_my_page_studio_management),
                onClick = { onAction(InstructorMyPageAction.OpenStudioManagement) },
            )
            InstructorMyPageMenuRow(
                title = stringResource(Res.string.my_page_privacy_policy),
                onClick = { onAction(InstructorMyPageAction.OpenPrivacyPolicy) },
            )
        }
    }
}

@Composable
private fun ProfileSummary(
    profile: InstructorMyPageUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(role = Role.Button, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = AppShape.Card,
            color = InsColors.Surface,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(AppSpacing.cardPadding),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                InstructorAvatar(profile = profile)
                Spacer(modifier = Modifier.width(AppSpacing.lg))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                ) {
                    Text(
                        text = profile.name,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = InsColors.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = profile.phoneNumberLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = InsColors.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    text = stringResource(Res.string.instructor_my_page_edit_profile),
                    style = MaterialTheme.typography.bodyMedium,
                    color = InsColors.TextSecondary,
                )
                Spacer(modifier = Modifier.width(AppSpacing.sm))
                Icon(
                    painter = painterResource(Res.drawable.ic_arrow_forward),
                    contentDescription = null,
                    modifier = Modifier.size(AppSpacing.lg),
                    tint = InsColors.TextSecondary,
                )
            }
        }
    }
}

@Composable
private fun InstructorAvatar(
    profile: InstructorMyPageUiModel,
    modifier: Modifier = Modifier,
) {
    val avatarModifier =
        modifier
            .size(AppSpacing.xxxl + AppSpacing.xxl)
            .clip(CircleShape)
            .background(
                color = InsColors.PurpleLight,
                shape = CircleShape,
            )

    if (profile.profileImageUrl.isNullOrBlank()) {
        AvatarFallback(
            fallback = profile.avatarFallback,
            modifier = avatarModifier,
        )
    } else {
        SubcomposeAsyncImage(
            model = profile.profileImageUrl,
            contentDescription = null,
            modifier = avatarModifier,
            loading = {
                AvatarFallback(fallback = profile.avatarFallback)
            },
            error = {
                AvatarFallback(fallback = profile.avatarFallback)
            },
        )
    }
}

@Composable
private fun AvatarFallback(
    fallback: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = fallback,
            style = appTypography().headlineMedium,
            color = InsColors.TextPrimary,
        )
    }
}

@Composable
private fun InstructorMyPageMenuRow(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(role = Role.Button, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = AppShape.Card,
            color = InsColors.Surface,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(AppSpacing.cardPadding),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = InsColors.TextPrimary,
                )
                Icon(
                    painter = painterResource(Res.drawable.ic_arrow_forward),
                    contentDescription = null,
                    modifier = Modifier.size(AppSpacing.lg),
                    tint = InsColors.TextSecondary,
                )
            }
        }
    }
}

@Composable
private fun InstructorMyPageLoading(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .semantics { liveRegion = LiveRegionMode.Polite },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
        ) {
            CircularProgressIndicator(color = InsColors.Purple)
            Text(
                text = stringResource(Res.string.instructor_my_page_loading),
                style = appTypography().bodyMedium,
                color = InsColors.TextSecondary,
            )
        }
    }
}

@Composable
private fun InstructorMyPageError(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .semantics { liveRegion = LiveRegionMode.Assertive },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = AppSpacing.screenPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Text(
                text = stringResource(Res.string.instructor_my_page_error_title),
                style = appTypography().titleLarge,
                color = InsColors.TextPrimary,
            )
            Text(
                text = stringResource(Res.string.instructor_my_page_error_description),
                style = appTypography().bodyMedium,
                color = InsColors.TextSecondary,
            )
            TextButton(onClick = onRetry) {
                Text(
                    text = stringResource(Res.string.instructor_my_page_retry),
                    color = InsColors.Purple,
                )
            }
        }
    }
}

private val instructorMyPagePreviewProfile =
    InstructorMyPageUiModel(
        name = "이지은",
        phoneNumberLabel = "010-****-5678",
        profileImageUrl = null,
        avatarFallback = "이",
    )

@Preview(
    name = "Content · Instructor · Default",
    group = "Screen/InstructorMyPage",
)
@Composable
private fun InstructorMyPageScreenPreview_Content_Instructor_Default() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        InstructorMyPageScreen(
            uiState = InstructorMyPageUiState.Content(profile = instructorMyPagePreviewProfile),
            onAction = {},
        )
    }
}

private val instructorMyPageLongTextPreviewProfile =
    InstructorMyPageUiModel(
        name = "이지은 필라테스 스튜디오 대표 강사",
        phoneNumberLabel = "010-****-5678 (대한민국 국가번호 포함)",
        profileImageUrl = null,
        avatarFallback = "이",
    )

@Preview(
    name = "Content · Instructor · Long text",
    group = "Screen/InstructorMyPage",
    widthDp = 320,
    heightDp = 568,
    fontScale = 1.5f,
)
@Composable
private fun InstructorMyPageScreenPreview_Content_Instructor_LongText() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        InstructorMyPageScreen(
            uiState = InstructorMyPageUiState.Content(instructorMyPageLongTextPreviewProfile),
            onAction = {},
        )
    }
}

@Preview(
    name = "Content · Instructor · Small height",
    group = "Screen/InstructorMyPage",
    widthDp = 320,
    heightDp = 360,
    fontScale = 1.5f,
)
@Composable
private fun InstructorMyPageScreenPreview_Content_Instructor_SmallHeight() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        InstructorMyPageScreen(
            uiState = InstructorMyPageUiState.Content(instructorMyPageLongTextPreviewProfile),
            onAction = {},
        )
    }
}

@Preview(
    name = "Loading · Instructor",
    group = "Screen/InstructorMyPage",
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun InstructorMyPageScreenPreview_Loading_Instructor() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        InstructorMyPageScreen(
            uiState = InstructorMyPageUiState.Loading,
            onAction = {},
        )
    }
}

@Preview(
    name = "Error · Instructor",
    group = "Screen/InstructorMyPage",
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun InstructorMyPageScreenPreview_Error_Instructor() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        InstructorMyPageScreen(
            uiState = InstructorMyPageUiState.Error(InstructorMyPageUiError.NETWORK),
            onAction = {},
        )
    }
}
