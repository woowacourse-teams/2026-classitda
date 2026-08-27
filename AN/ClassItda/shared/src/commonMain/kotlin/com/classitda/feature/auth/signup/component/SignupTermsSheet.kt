package com.classitda.feature.auth.signup.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.component.PrimaryButton

@Composable
internal fun SignupTermsSheet(
    allTermsAgreed: Boolean,
    termsAgreed: Boolean = allTermsAgreed,
    privacyPolicyAgreed: Boolean = allTermsAgreed,
    onToggleAllTerms: () -> Unit,
    onToggleTerms: () -> Unit = {},
    onTogglePrivacyPolicy: () -> Unit = {},
    onComplete: () -> Unit,
    onDismiss: () -> Unit,
    onTermsClick: () -> Unit = {},
    onPrivacyPolicyClick: () -> Unit = {},
) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(StuColors.Dim)
                .clickable(onClick = onDismiss),
    ) {
        Surface(
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .clickable(enabled = true, onClick = {}),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            color = StuColors.Surface,
        ) {
            Column(
                modifier =
                    Modifier.padding(
                        start = AppSpacing.lg,
                        top = AppSpacing.lg,
                        end = AppSpacing.lg,
                        bottom = AppSpacing.md,
                    ),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().clickable(onClick = onToggleAllTerms),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SignupCheckBox(
                        checked = allTermsAgreed,
                        onClick = onToggleAllTerms,
                    )
                    Spacer(modifier = Modifier.width(AppSpacing.sm))
                    Text(
                        text = "약관 전체 동의",
                        style =
                            MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                            ),
                        color = StuColors.TextPrimary,
                    )
                }
                HorizontalDivider(
                    modifier = Modifier.padding(top = AppSpacing.md, start = AppSpacing.md, end = AppSpacing.md),
                    thickness = 1.dp,
                    color = StuColors.Divider,
                )
                Column(
                    modifier = Modifier.padding(top = AppSpacing.md),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
                ) {
                    SignupTermRow(
                        text = "[필수] 이용약관 동의",
                        checked = termsAgreed,
                        onCheckClick = onToggleTerms,
                        onViewClick = onTermsClick,
                    )
                    SignupTermRow(
                        text = "[필수] 개인정보 수집 및 이용 동의",
                        checked = privacyPolicyAgreed,
                        onCheckClick = onTogglePrivacyPolicy,
                        onViewClick = onPrivacyPolicyClick,
                    )
                }
                Spacer(modifier = Modifier.height(44.dp))
                PrimaryButton(
                    text = "가입 완료",
                    onClick = onComplete,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Preview(name = "Signup terms sheet", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun SignupTermsSheetPreview() {
    AppTheme(theme = ThemeType.STUDENT) {
        var termsAgreed by remember { mutableStateOf(false) }
        var privacyPolicyAgreed by remember { mutableStateOf(false) }
        val allTermsAgreed = termsAgreed && privacyPolicyAgreed

        SignupTermsSheet(
            allTermsAgreed = allTermsAgreed,
            termsAgreed = termsAgreed,
            privacyPolicyAgreed = privacyPolicyAgreed,
            onToggleAllTerms = {
                val nextValue = !allTermsAgreed
                termsAgreed = nextValue
                privacyPolicyAgreed = nextValue
            },
            onToggleTerms = { termsAgreed = !termsAgreed },
            onTogglePrivacyPolicy = { privacyPolicyAgreed = !privacyPolicyAgreed },
            onComplete = {},
            onDismiss = {},
        )
    }
}
