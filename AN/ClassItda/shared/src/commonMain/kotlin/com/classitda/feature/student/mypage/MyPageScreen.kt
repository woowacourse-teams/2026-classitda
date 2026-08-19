package com.classitda.feature.student.mypage

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.ic_arrow_forward
import classitda.shared.generated.resources.my_page_connected_facilities
import classitda.shared.generated.resources.my_page_contact_email
import classitda.shared.generated.resources.my_page_contact_email_address
import classitda.shared.generated.resources.my_page_instructor_signup
import classitda.shared.generated.resources.my_page_my_info
import classitda.shared.generated.resources.my_page_notification_settings
import classitda.shared.generated.resources.my_page_passes
import classitda.shared.generated.resources.my_page_privacy_policy
import classitda.shared.generated.resources.my_page_switch_to_instructor
import classitda.shared.generated.resources.my_page_title
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.appTypography
import com.classitda.domain.model.student.mypage.MemberId
import com.classitda.domain.model.student.mypage.MemberProfile
import com.classitda.domain.model.student.mypage.MyPageSummary
import com.classitda.feature.student.StudentBottomTab
import com.classitda.feature.student.StudentTab
import com.classitda.feature.student.mypage.contract.MyPageAction
import com.classitda.feature.student.mypage.contract.MyPageContentState
import com.classitda.feature.student.mypage.contract.MyPageUiState
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

private val myPagePrimaryColor = StuColors.TextPrimary.copy(alpha = 0.88f)
private val myPageSecondaryColor = StuColors.TextSecondary.copy(alpha = 0.8f)
private val myPageBannerColor = StuColors.Black.copy(alpha = 0.85f)

@Composable
fun MyPageScreen(
    uiState: MyPageUiState,
    onAction: (MyPageAction) -> Unit,
    onTabSelected: (StudentTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = StuColors.Background,
        bottomBar = {
            Column {
                HorizontalDivider(color = StuColors.Divider)
                StudentBottomTab(
                    selectedTab = StudentTab.MYPage,
                    onTabSelected = onTabSelected,
                )
            }
        },
    ) { innerPadding ->
        when (val content = uiState.content) {
            MyPageContentState.Loading -> {
                Box(modifier = Modifier.padding(innerPadding).fillMaxSize())
            }

            is MyPageContentState.Error -> {
                Box(modifier = Modifier.padding(innerPadding).fillMaxSize())
            }

            is MyPageContentState.Content -> {
                MyPageContent(
                    summary = content.summary,
                    onAction = onAction,
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }
    }
}

@Composable
private fun MyPageContent(
    summary: MyPageSummary,
    onAction: (MyPageAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
    ) {
        MyPageTitle()
        if (summary.isInstructorSignupBannerVisible) {
            InstructorSignupBanner(
                onClick = { onAction(MyPageAction.OpenInstructorSignup) },
            )
        } else {
            InstructorSwitchBar(
                onSwitch = { onAction(MyPageAction.SwitchToInstructor) },
            )
        }
        MyPageProfileSummary(
            profile = summary.profile,
            isEmailUnderlined = summary.isInstructorSignupBannerVisible,
            onClick = { onAction(MyPageAction.OpenProfile) },
        )
        HorizontalDivider(color = StuColors.Divider)
        MyPageMenuRow(
            title = stringResource(Res.string.my_page_passes),
            showDivider = true,
            onClick = { onAction(MyPageAction.OpenPasses) },
        )
        MyPageMenuRow(
            title = stringResource(Res.string.my_page_connected_facilities),
            showDivider = true,
            onClick = { onAction(MyPageAction.OpenConnectedFacilities) },
        )
        MyPageMenuRow(
            title = stringResource(Res.string.my_page_notification_settings),
            showDivider = true,
            onClick = { onAction(MyPageAction.OpenNotificationSettings) },
        )
        MyPageMenuRow(
            title = stringResource(Res.string.my_page_privacy_policy),
            showDivider = false,
            onClick = { onAction(MyPageAction.OpenPrivacyPolicy) },
        )
        MyPageContactEmail()
    }
}

@Composable
private fun MyPageTitle() {
    val typography = appTypography()

    Text(
        text = stringResource(Res.string.my_page_title),
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    start = AppSpacing.screenPadding,
                    top = AppSpacing.xxl,
                    end = AppSpacing.screenPadding,
                    bottom = AppSpacing.xl,
                ),
        style = typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
        color = myPagePrimaryColor,
    )
}

@Composable
private fun InstructorSignupBanner(onClick: () -> Unit) {
    val typography = appTypography()

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(AppSpacing.xxxl)
                .background(myPageBannerColor)
                .clickable(role = Role.Button, onClick = onClick)
                .padding(horizontal = AppSpacing.screenPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(Res.string.my_page_instructor_signup),
            modifier = Modifier.weight(1f),
            style = typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = StuColors.White,
        )
        Icon(
            painter = painterResource(Res.drawable.ic_arrow_forward),
            contentDescription = null,
            modifier = Modifier.size(AppSpacing.lg),
            tint = StuColors.White,
        )
    }
}

@Composable
private fun InstructorSwitchBar(onSwitch: () -> Unit) {
    val typography = appTypography()

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(AppSpacing.xxxl)
                .background(myPageBannerColor)
                .toggleable(
                    value = false,
                    role = Role.Switch,
                    onValueChange = { onSwitch() },
                ).padding(horizontal = AppSpacing.screenPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(Res.string.my_page_switch_to_instructor),
            modifier = Modifier.weight(1f),
            style = typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = StuColors.White,
        )
        Box(
            modifier =
                Modifier
                    .width(AppSpacing.xxxl + AppSpacing.md)
                    .height(AppSpacing.xxl)
                    .background(StuColors.SurfaceVariant, AppShape.Pill)
                    .padding(AppSpacing.xs / 2),
            contentAlignment = Alignment.CenterStart,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(AppSpacing.xl)
                        .background(StuColors.White, CircleShape),
            )
        }
    }
}

@Composable
private fun MyPageProfileSummary(
    profile: MemberProfile,
    isEmailUnderlined: Boolean,
    onClick: () -> Unit,
) {
    val typography = appTypography()

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(AppSpacing.xxxl * 3 + AppSpacing.xs)
                .clickable(role = Role.Button, onClick = onClick)
                .padding(horizontal = AppSpacing.screenPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.lg),
    ) {
        Box(
            modifier =
                Modifier
                    .size(AppSpacing.xxxl + AppSpacing.xxl)
                    .background(
                        color = StuColors.SurfaceVariant,
                        shape = CircleShape,
                    ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = profile.name.take(1),
                style = typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = myPagePrimaryColor,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = profile.name,
                style = typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = myPagePrimaryColor,
            )
            Text(
                text = profile.phoneNumber,
                style = typography.bodyLarge,
                color = myPageSecondaryColor,
            )
            Text(
                text = profile.email,
                style = typography.bodyLarge,
                color = myPageSecondaryColor,
                textDecoration =
                    if (isEmailUnderlined) {
                        TextDecoration.Underline
                    } else {
                        TextDecoration.None
                    },
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            Text(
                text = stringResource(Res.string.my_page_my_info),
                style = typography.bodyMedium,
                color = myPageSecondaryColor,
            )
            Icon(
                painter = painterResource(Res.drawable.ic_arrow_forward),
                contentDescription = null,
                modifier = Modifier.size(AppSpacing.lg),
                tint = myPageSecondaryColor,
            )
        }
    }
}

@Composable
private fun MyPageMenuRow(
    title: String,
    showDivider: Boolean,
    onClick: () -> Unit,
) {
    val typography = appTypography()

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(AppSpacing.xxxl * 2 - AppSpacing.xs / 2)
                    .clickable(role = Role.Button, onClick = onClick)
                    .padding(horizontal = AppSpacing.screenPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                style = typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = myPagePrimaryColor,
            )
            Icon(
                painter = painterResource(Res.drawable.ic_arrow_forward),
                contentDescription = null,
                modifier = Modifier.size(AppSpacing.lg),
                tint = myPageSecondaryColor,
            )
        }
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = AppSpacing.screenPadding),
                color = StuColors.Divider,
            )
        }
    }
}

@Composable
private fun MyPageContactEmail() {
    val typography = appTypography()

    Text(
        text =
            stringResource(
                Res.string.my_page_contact_email,
                stringResource(Res.string.my_page_contact_email_address),
            ),
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = AppSpacing.sm),
        style = typography.bodySmall,
        color = myPageSecondaryColor,
        textAlign = TextAlign.Center,
    )
}

@Preview(name = "F01 · Instructor signup", widthDp = 390, heightDp = 840)
@Composable
private fun MyPageScreenVisiblePreview() {
    AppTheme(theme = ThemeType.STUDENT) {
        MyPageScreen(
            uiState = MyPagePreviewFixture.instructorSignup,
            onAction = {},
            onTabSelected = {},
        )
    }
}

@Preview(name = "F02 · Instructor switch", widthDp = 392, heightDp = 840)
@Composable
private fun MyPageScreenInstructorSwitchPreview() {
    AppTheme(theme = ThemeType.STUDENT) {
        MyPageScreen(
            uiState = MyPagePreviewFixture.instructorSwitch,
            onAction = {},
            onTabSelected = {},
        )
    }
}

@Preview(name = "Interaction harness · last action", widthDp = 390, heightDp = 844)
@Composable
private fun MyPageScreenInteractionHarnessPreview() {
    var lastAction by remember { mutableStateOf("없음") }

    AppTheme(theme = ThemeType.STUDENT) {
        val typography = appTypography()

        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "마지막 행동: $lastAction",
                modifier = Modifier.padding(horizontal = AppSpacing.screenPadding, vertical = AppSpacing.sm),
                style = typography.labelLarge,
            )
            MyPageScreen(
                uiState = MyPagePreviewFixture.instructorSignup,
                modifier = Modifier.weight(1f),
                onAction = { action -> lastAction = action.previewLabel() },
                onTabSelected = { tab -> lastAction = "탭: ${tab.name}" },
            )
        }
    }
}

private fun MyPageAction.previewLabel(): String =
    when (this) {
        MyPageAction.Retry -> "다시 시도"
        MyPageAction.OpenProfile -> "내 정보"
        MyPageAction.OpenPasses -> "내 수강권"
        MyPageAction.OpenConnectedFacilities -> "연결된 시설"
        MyPageAction.OpenNotificationSettings -> "알림 설정"
        MyPageAction.OpenPrivacyPolicy -> "개인정보처리방침"
        MyPageAction.OpenInstructorSignup -> "강사로 가입하기"
        MyPageAction.SwitchToInstructor -> "강사로 전환"
    }

private object MyPagePreviewFixture {
    private val profile =
        MemberProfile(
            id = MemberId("member-preview"),
            name = "김민지",
            phoneNumber = "010-1234-5678",
            email = "class12345@gmail.com",
            profileImageUrl = null,
        )

    val instructorSignup =
        MyPageUiState(
            content =
                MyPageContentState.Content(
                    summary = MyPageSummary(profile = profile, isInstructorSignupBannerVisible = true),
                ),
        )

    val instructorSwitch =
        MyPageUiState(
            content =
                MyPageContentState.Content(
                    summary = MyPageSummary(profile = profile, isInstructorSignupBannerVisible = false),
                ),
        )
}
