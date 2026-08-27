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
import androidx.compose.ui.tooling.preview.Preview
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.domain.model.instructor.management.ClassSession
import com.classitda.domain.model.instructor.management.ClassSessionStatus
import kotlinx.datetime.LocalDateTime

@Composable
internal fun InstructorHomeSummary(
    sessions: List<ClassSession>,
    modifier: Modifier = Modifier,
) {
    val completedCount = sessions.count { it.status == ClassSessionStatus.COMPLETED }
    val remainingCount = sessions.count { it.status == ClassSessionStatus.SCHEDULED }

    Column(modifier.padding(horizontal = AppSpacing.screenPadding)) {
        Text(
            text = "오늘 수업 ${sessions.size}개",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = InsColors.TextPrimary,
        )
        Spacer(Modifier.height(AppSpacing.xs))
        Text(
            text = "완료 ${completedCount}개 · 남은 수업 ${remainingCount}개",
            style = MaterialTheme.typography.bodyMedium,
            color = InsColors.TextSecondary,
        )
    }
}

@Preview(name = "강사 홈 수업 요약", showBackground = true, widthDp = 390)
@Composable
private fun InstructorHomeSummaryPreview() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        InstructorHomeSummary(
            sessions =
                listOf(
                    ClassSession(
                        id = "session-1",
                        classTypeId = "session-1",
                        tags = listOf("그룹 수업"),
                        title = "체어 밸런스",
                        startAt = LocalDateTime(2026, 8, 5, 14, 0),
                        endAt = LocalDateTime(2026, 8, 5, 14, 50),
                        reservedCount = 7,
                        capacity = 8,
                        status = ClassSessionStatus.COMPLETED,
                    ),
                    ClassSession(
                        id = "session-2",
                        classTypeId = "session-2",
                        tags = listOf("필라테스"),
                        title = "리포머 밸런스",
                        startAt = LocalDateTime(2026, 8, 5, 19, 30),
                        endAt = LocalDateTime(2026, 8, 5, 20, 20),
                        reservedCount = 6,
                        capacity = 8,
                        status = ClassSessionStatus.SCHEDULED,
                    ),
                ),
        )
    }
}
