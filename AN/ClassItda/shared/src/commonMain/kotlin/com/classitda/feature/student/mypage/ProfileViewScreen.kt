package com.classitda.feature.student.mypage

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.ic_arrow_back
import classitda.shared.generated.resources.ic_edit
import classitda.shared.generated.resources.profile_view_back
import classitda.shared.generated.resources.profile_view_edit
import classitda.shared.generated.resources.profile_view_email
import classitda.shared.generated.resources.profile_view_error_description
import classitda.shared.generated.resources.profile_view_error_title
import classitda.shared.generated.resources.profile_view_loading
import classitda.shared.generated.resources.profile_view_logout
import classitda.shared.generated.resources.profile_view_name
import classitda.shared.generated.resources.profile_view_phone_number
import classitda.shared.generated.resources.profile_view_retry
import classitda.shared.generated.resources.profile_view_title
import classitda.shared.generated.resources.profile_view_withdrawal
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.appTypography
import com.classitda.domain.model.student.mypage.MemberId
import com.classitda.domain.model.student.mypage.MemberProfile
import com.classitda.domain.repository.student.mypage.MyPageFailureReason
import com.classitda.feature.student.mypage.contract.ProfileViewAction
import com.classitda.feature.student.mypage.contract.ProfileViewUiState
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun ProfileViewScreen(
    uiState: ProfileViewUiState,
    onAction: (ProfileViewAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = StuColors.Background,
        topBar = {
            ProfileViewTopBar(
                onBack = { onAction(ProfileViewAction.Back) },
                onEdit = { onAction(ProfileViewAction.OpenEdit) },
            )
        },
    ) { innerPadding ->
        when (uiState) {
            ProfileViewUiState.Loading -> {
                ProfileViewLoadingContent(
                    modifier = Modifier.padding(innerPadding),
                )
            }

            is ProfileViewUiState.Content -> {
                ProfileViewContent(
                    profile = uiState.profile,
                    onLogout = { onAction(ProfileViewAction.RequestLogout) },
                    onWithdrawal = { onAction(ProfileViewAction.RequestWithdrawal) },
                    modifier = Modifier.padding(innerPadding),
                )
            }

            is ProfileViewUiState.Error -> {
                ProfileViewErrorContent(
                    onRetry = { onAction(ProfileViewAction.Retry) },
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }
    }
}

@Composable
private fun ProfileViewTopBar(
    onBack: () -> Unit,
    onEdit: () -> Unit,
) {
    val typography = appTypography()

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.xs, vertical = AppSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                painter = painterResource(Res.drawable.ic_arrow_back),
                contentDescription = stringResource(Res.string.profile_view_back),
                modifier = Modifier.size(AppSpacing.xxl),
                tint = StuColors.TextPrimary,
            )
        }
        Text(
            text = stringResource(Res.string.profile_view_title),
            modifier =
                Modifier
                    .weight(1f)
                    .semantics { heading() },
            style = typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = StuColors.TextPrimary,
        )
        IconButton(onClick = onEdit) {
            Icon(
                painter = painterResource(Res.drawable.ic_edit),
                contentDescription = stringResource(Res.string.profile_view_edit),
                modifier = Modifier.size(AppSpacing.xxl),
                tint = StuColors.TextPrimary,
            )
        }
    }
}

@Composable
private fun ProfileViewContent(
    profile: MemberProfile,
    onLogout: () -> Unit,
    onWithdrawal: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val typography = appTypography()

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppSpacing.screenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(AppSpacing.sm))
        ProfileAvatar(name = profile.name)
        Spacer(modifier = Modifier.height(AppSpacing.xxxl * 2))
        ReadOnlyProfileField(
            label = stringResource(Res.string.profile_view_name),
            value = profile.name,
            isPrimary = true,
        )
        Spacer(modifier = Modifier.height(AppSpacing.xl))
        ReadOnlyProfileField(
            label = stringResource(Res.string.profile_view_phone_number),
            value = profile.phoneNumber,
        )
        Spacer(modifier = Modifier.height(AppSpacing.xl))
        ReadOnlyProfileField(
            label = stringResource(Res.string.profile_view_email),
            value = profile.email,
        )
        Spacer(modifier = Modifier.height(AppSpacing.xxl))
        TextButton(onClick = onLogout) {
            Text(
                text = stringResource(Res.string.profile_view_logout),
                style = typography.bodyMedium,
                color = StuColors.TextSecondary,
            )
        }
        Spacer(modifier = Modifier.height(AppSpacing.xxxl * 5))
        TextButton(onClick = onWithdrawal) {
            Text(
                text = stringResource(Res.string.profile_view_withdrawal),
                style = typography.bodyMedium,
                color = StuColors.Red,
            )
        }
        Spacer(modifier = Modifier.height(AppSpacing.xxl))
    }
}

@Composable
private fun ProfileAvatar(name: String) {
    val typography = appTypography()

    Box(
        modifier =
            Modifier
                .size(AppSpacing.xxxl * 3)
                .background(
                    color = StuColors.SurfaceVariant,
                    shape = CircleShape,
                ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = name.first { !it.isWhitespace() }.toString(),
            style = typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = StuColors.TextPrimary,
        )
    }
}

@Composable
private fun ReadOnlyProfileField(
    label: String,
    value: String,
    isPrimary: Boolean = false,
) {
    val typography = appTypography()

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Text(
            text = label,
            style = typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = StuColors.TextPrimary,
        )
        Surface(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .semantics(mergeDescendants = true) {},
            shape = AppShape.Card,
            color = StuColors.SurfaceVariant,
            border =
                BorderStroke(
                    width = AppSpacing.xs / 4,
                    color = StuColors.Divider,
                ),
        ) {
            Text(
                text = value,
                modifier = Modifier.padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md),
                style = typography.bodyLarge,
                color =
                    if (isPrimary) {
                        StuColors.TextPrimary
                    } else {
                        StuColors.TextSecondary
                    },
            )
        }
    }
}

@Composable
private fun ProfileViewLoadingContent(modifier: Modifier = Modifier) {
    val typography = appTypography()

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(color = StuColors.Green)
        Spacer(modifier = Modifier.height(AppSpacing.lg))
        Text(
            text = stringResource(Res.string.profile_view_loading),
            style = typography.bodyMedium,
            color = StuColors.TextSecondary,
        )
    }
}

@Composable
private fun ProfileViewErrorContent(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val typography = appTypography()

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = AppSpacing.screenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(Res.string.profile_view_error_title),
            modifier = Modifier.fillMaxWidth(),
            style = typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = StuColors.TextPrimary,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(AppSpacing.sm))
        Text(
            text = stringResource(Res.string.profile_view_error_description),
            modifier = Modifier.fillMaxWidth(),
            style = typography.bodyMedium,
            color = StuColors.TextSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(AppSpacing.xxl))
        OutlinedButton(onClick = onRetry) {
            Text(text = stringResource(Res.string.profile_view_retry))
        }
    }
}

private fun ProfileViewAction.previewLabel(): String =
    when (this) {
        ProfileViewAction.Back -> "Back"
        ProfileViewAction.Retry -> "Retry"
        ProfileViewAction.OpenEdit -> "OpenEdit"
        ProfileViewAction.RequestLogout -> "RequestLogout"
        ProfileViewAction.RequestWithdrawal -> "RequestWithdrawal"
    }

private object ProfileViewPreviewFixture {
    private val profile =
        MemberProfile(
            id = MemberId("member-profile-view-preview"),
            name = "김민지",
            phoneNumber = "010-1234-5678",
            email = "class12345@gmail.com",
            profileImageUrl = null,
        )

    val content = ProfileViewUiState.Content(profile)
    val loading = ProfileViewUiState.Loading
    val error = ProfileViewUiState.Error(MyPageFailureReason.NETWORK)
}

@Preview(
    name = "Content · Student · Default",
    group = "Screen/ProfileView",
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun ProfileViewScreenPreview_Content_Student_Default() {
    AppTheme(theme = ThemeType.STUDENT) {
        ProfileViewScreen(
            uiState = ProfileViewPreviewFixture.content,
            onAction = {},
        )
    }
}

@Preview(
    name = "Loading · Student · Default",
    group = "Screen/ProfileView",
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun ProfileViewScreenPreview_Loading_Student_Default() {
    AppTheme(theme = ThemeType.STUDENT) {
        ProfileViewScreen(
            uiState = ProfileViewPreviewFixture.loading,
            onAction = {},
        )
    }
}

@Preview(
    name = "Error · Student · Default",
    group = "Screen/ProfileView",
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun ProfileViewScreenPreview_Error_Student_Default() {
    AppTheme(theme = ThemeType.STUDENT) {
        ProfileViewScreen(
            uiState = ProfileViewPreviewFixture.error,
            onAction = {},
        )
    }
}

@Preview(
    name = "Actions · Student · Interactive",
    group = "Harness/ProfileView",
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun ProfileViewScreenPreview_Actions_Student_Interactive() {
    var lastAction by remember { mutableStateOf("None") }

    AppTheme(theme = ThemeType.STUDENT) {
        val typography = appTypography()

        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "마지막 행동: $lastAction",
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppSpacing.screenPadding, vertical = AppSpacing.sm),
                style = typography.labelLarge,
            )
            ProfileViewScreen(
                uiState = ProfileViewPreviewFixture.content,
                onAction = { lastAction = it.previewLabel() },
                modifier = Modifier.weight(1f),
            )
        }
    }
}
