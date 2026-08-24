package com.classitda.feature.instructor.home.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.InsColors
import com.classitda.domain.model.instructor.management.ClassSession
import com.classitda.domain.model.instructor.management.ClassSessionStatus

@Composable
internal fun InstructorHomeSummary(
    sessions: List<ClassSession>,
    modifier: Modifier = Modifier,
) {
    val completedCount = sessions.count { it.status == ClassSessionStatus.COMPLETED }
    val remainingCount = sessions.count { it.status == ClassSessionStatus.SCHEDULED }

    Column(modifier.padding(horizontal = AppSpacing.screenPadding)) {
        Text("오늘 수업 ${sessions.size}개", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(AppSpacing.xs))
        Text("완료 ${completedCount}개 · 남은 수업 ${remainingCount}개", color = InsColors.TextSecondary)
    }
}
