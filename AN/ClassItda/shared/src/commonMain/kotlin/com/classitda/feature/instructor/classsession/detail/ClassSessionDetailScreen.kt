package com.classitda.feature.instructor.classsession.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.domain.model.instructor.management.ClassSessionStatus
import com.classitda.feature.instructor.classsession.detail.component.ClassSessionDetailInfoCard
import com.classitda.feature.instructor.classsession.detail.component.ClassSessionDetailTopBar
import com.classitda.feature.instructor.classsession.detail.component.ClassSessionMemberRow
import com.classitda.feature.instructor.classsession.detail.model.ClassSessionDetailUiModel
import com.classitda.feature.instructor.classsession.detail.model.ClassSessionMemberUiModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun ClassSessionDetailRoute(
    sessionId: String,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onMemberEditClick: () -> Unit,
    refreshKey: Int = 0,
    modifier: Modifier = Modifier,
    viewModel: ClassSessionDetailViewModel = koinViewModel(),
) {
    LaunchedEffect(sessionId, refreshKey) {
        viewModel.load(sessionId)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ClassSessionDetailStateful(
        uiState = uiState,
        onBackClick = onBackClick,
        onRetry = { viewModel.load(sessionId) },
        onEditClick = onEditClick,
        onMemberEditClick = onMemberEditClick,
        modifier = modifier,
    )
}

@Composable
private fun ClassSessionDetailStateful(
    uiState: ClassSessionDetailUiState,
    onBackClick: () -> Unit,
    onRetry: () -> Unit,
    onEditClick: () -> Unit,
    onMemberEditClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (uiState) {
        ClassSessionDetailUiState.Loading -> {
            ClassSessionDetailLoading(modifier = modifier)
        }

        is ClassSessionDetailUiState.Error -> {
            ClassSessionDetailError(
                message = uiState.message,
                onRetry = onRetry,
                modifier = modifier,
            )
        }

        is ClassSessionDetailUiState.Success -> {
            var isMenuExpanded by remember { mutableStateOf(false) }

            ClassSessionDetailStateless(
                detail = uiState.detail,
                isMenuExpanded = isMenuExpanded,
                onBackClick = onBackClick,
                onMoreClick = { isMenuExpanded = true },
                onDismissMenu = { isMenuExpanded = false },
                onEditClick = {
                    isMenuExpanded = false
                    onEditClick()
                },
                onMemberEditClick = {
                    isMenuExpanded = false
                    onMemberEditClick()
                },
                modifier = modifier,
            )
        }
    }
}

@Composable
internal fun ClassSessionDetailStateless(
    detail: ClassSessionDetailUiModel,
    isMenuExpanded: Boolean,
    onBackClick: () -> Unit,
    onMoreClick: () -> Unit,
    onDismissMenu: () -> Unit,
    onEditClick: () -> Unit,
    onMemberEditClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = InsColors.Background,
        topBar = {
            ClassSessionDetailTopBar(
                isMenuExpanded = isMenuExpanded,
                onBackClick = onBackClick,
                onMoreClick = onMoreClick,
                onDismissMenu = onDismissMenu,
                onEditClick = onEditClick,
                onMemberEditClick = onMemberEditClick,
            )
        },
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(contentPadding),
            contentPadding = PaddingValues(bottom = AppSpacing.xxxl),
        ) {
            item {
                Text(
                    text = detail.dateText,
                    style = MaterialTheme.typography.titleMedium,
                    color = InsColors.TextPrimary,
                    modifier =
                        Modifier.padding(
                            start = AppSpacing.screenPadding,
                            top = AppSpacing.lg,
                            bottom = AppSpacing.md,
                        ),
                )
            }
            item {
                ClassSessionDetailInfoCard(
                    detail = detail,
                    modifier =
                        Modifier.padding(
                            horizontal = AppSpacing.screenPadding,
                        ),
                )
            }
            item {
                RowSectionTitle(
                    title = "예약 회원",
                    count = "${detail.members.size}명",
                    modifier = Modifier.padding(top = AppSpacing.xxl),
                )
            }
            items(detail.members, key = { it.id }) { member ->
                ClassSessionMemberRow(
                    member = member,
                    modifier =
                        Modifier.padding(
                            horizontal = AppSpacing.screenPadding,
                            vertical = AppSpacing.xs,
                        ),
                )
            }
            item {
                Text(
                    text = "예약 회원은 수업 상세에서 확인할 수 있어요.",
                    style = MaterialTheme.typography.bodySmall,
                    color = InsColors.TextTertiary,
                    modifier =
                        Modifier.padding(
                            start = AppSpacing.screenPadding,
                            end = AppSpacing.screenPadding,
                            top = AppSpacing.md,
                        ),
                )
            }
        }
    }
}

@Composable
private fun RowSectionTitle(
    title: String,
    count: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = AppSpacing.screenPadding),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = InsColors.TextPrimary,
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = count,
            style = MaterialTheme.typography.bodySmall,
            color = InsColors.TextTertiary,
        )
    }
}

@Composable
private fun ClassSessionDetailLoading(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize().background(InsColors.Background),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = InsColors.Purple)
    }
}

@Composable
private fun ClassSessionDetailError(
    message: String?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().background(InsColors.Background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = message ?: "수업 정보를 불러오지 못했어요",
            color = InsColors.TextSecondary,
        )
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = InsColors.Primary),
            modifier = Modifier.padding(top = AppSpacing.lg),
        ) {
            Text("다시 시도")
        }
    }
}

@Preview(name = "수업 상세", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun ClassSessionDetailStatelessPreview() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        ClassSessionDetailStateless(
            detail =
                ClassSessionDetailUiModel(
                    id = "session-1",
                    dateText = "2026.08.05 (수)",
                    tags = listOf("그룹 수업", "필라테스"),
                    title = "리포머 밸런스",
                    timeText = "오후 7:30 ~ 8:40",
                    reservedCount = 3,
                    capacity = 8,
                    description = "체어룸에서 할 예정",
                    location = "체어룸",
                    status = ClassSessionStatus.SCHEDULED,
                    members =
                        listOf(
                            ClassSessionMemberUiModel(id = "1", name = "김민지"),
                            ClassSessionMemberUiModel(id = "2", name = "이서윤"),
                            ClassSessionMemberUiModel(id = "3", name = "박지수"),
                        ),
                ),
            isMenuExpanded = false,
            onBackClick = {},
            onMoreClick = {},
            onDismissMenu = {},
            onEditClick = {},
            onMemberEditClick = {},
        )
    }
}

@Preview(name = "수업 상세 - 더보기", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun ClassSessionDetailStatelessMenuPreview() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        ClassSessionDetailStateless(
            detail =
                ClassSessionDetailUiModel(
                    id = "session-1",
                    dateText = "2026.08.05 (수)",
                    tags = listOf("그룹 수업", "필라테스"),
                    title = "리포머 밸런스",
                    timeText = "오후 7:30 ~ 8:40",
                    reservedCount = 3,
                    capacity = 8,
                    description = "체어룸에서 할 예정",
                    location = "체어룸",
                    status = ClassSessionStatus.SCHEDULED,
                    members = listOf(ClassSessionMemberUiModel(id = "1", name = "김민지")),
                ),
            isMenuExpanded = true,
            onBackClick = {},
            onMoreClick = {},
            onDismissMenu = {},
            onEditClick = {},
            onMemberEditClick = {},
        )
    }
}
