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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.ic_arrow_forward
import classitda.shared.generated.resources.instructor_my_page_edit_profile
import classitda.shared.generated.resources.instructor_my_page_error_description
import classitda.shared.generated.resources.instructor_my_page_error_title
import classitda.shared.generated.resources.instructor_my_page_facility_management
import classitda.shared.generated.resources.instructor_my_page_loading
import classitda.shared.generated.resources.instructor_my_page_member_management
import classitda.shared.generated.resources.instructor_my_page_retry
import classitda.shared.generated.resources.instructor_my_page_title
import classitda.shared.generated.resources.my_page_privacy_policy
import coil3.compose.SubcomposeAsyncImage
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.ThemeType
import com.classitda.feature.instructor.mypage.contract.InstructorMyPageAction
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
        containerColor = MaterialTheme.colorScheme.background,
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
                .verticalScroll(rememberScrollState()),
    ) {
        Text(
            text = stringResource(Res.string.instructor_my_page_title),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        start = AppSpacing.screenPadding,
                        top = AppSpacing.xxl,
                        end = AppSpacing.screenPadding,
                        bottom = AppSpacing.xxl,
                    ).semantics { heading() },
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
        )

        ProfileSummary(
            profile = profile,
            onClick = { onAction(InstructorMyPageAction.OpenProfile) },
        )

        Spacer(modifier = Modifier.size(AppSpacing.xxxl + AppSpacing.xs))

        InstructorMyPageMenuRow(
            title = stringResource(Res.string.instructor_my_page_member_management),
            onClick = { onAction(InstructorMyPageAction.OpenMemberManagement) },
        )
        InstructorMyPageMenuRow(
            title = stringResource(Res.string.instructor_my_page_facility_management),
            onClick = { onAction(InstructorMyPageAction.OpenFacilityManagement) },
        )
        InstructorMyPageMenuRow(
            title = stringResource(Res.string.my_page_privacy_policy),
            onClick = { onAction(InstructorMyPageAction.OpenPrivacyPolicy) },
        )
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
                .clickable(role = Role.Button, onClick = onClick)
                .padding(
                    horizontal = AppSpacing.screenPadding,
                    vertical = AppSpacing.sm,
                ),
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
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = profile.phoneNumberLabel,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = stringResource(Res.string.instructor_my_page_edit_profile),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.width(AppSpacing.sm))
        Icon(
            painter = painterResource(Res.drawable.ic_arrow_forward),
            contentDescription = null,
            modifier = Modifier.size(AppSpacing.lg),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
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
                color = MaterialTheme.colorScheme.primaryContainer,
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
            contentDescription = profile.name,
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
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
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
                .clickable(role = Role.Button, onClick = onClick)
                .padding(
                    horizontal = AppSpacing.screenPadding,
                    vertical = AppSpacing.lg,
                ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Icon(
            painter = painterResource(Res.drawable.ic_arrow_forward),
            contentDescription = null,
            modifier = Modifier.size(AppSpacing.lg),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun InstructorMyPageLoading(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Text(
                text = stringResource(Res.string.instructor_my_page_loading),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = AppSpacing.screenPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Text(
                text = stringResource(Res.string.instructor_my_page_error_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(Res.string.instructor_my_page_error_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TextButton(onClick = onRetry) {
                Text(
                    text = stringResource(Res.string.instructor_my_page_retry),
                    color = MaterialTheme.colorScheme.primary,
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
