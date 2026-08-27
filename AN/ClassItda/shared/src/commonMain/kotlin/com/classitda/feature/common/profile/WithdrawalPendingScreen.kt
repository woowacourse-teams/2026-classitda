package com.classitda.feature.common.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.withdrawal_pending_contact
import classitda.shared.generated.resources.withdrawal_pending_title
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.StuColors
import com.classitda.core.designsystem.appTypography
import org.jetbrains.compose.resources.stringResource

@Composable
fun WithdrawalPendingScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = AppSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(Res.string.withdrawal_pending_title),
            style = appTypography().titleLarge,
            color = StuColors.TextPrimary,
        )
        Text(
            text = stringResource(Res.string.withdrawal_pending_contact),
            modifier = Modifier.padding(top = AppSpacing.sm),
            style = appTypography().bodyMedium,
            color = StuColors.TextSecondary,
        )
    }
}
