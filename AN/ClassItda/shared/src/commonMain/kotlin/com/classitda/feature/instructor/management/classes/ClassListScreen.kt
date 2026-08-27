package com.classitda.feature.instructor.management.classes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FabPosition
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.component.NavigateBackTopBar
import com.classitda.domain.model.instructor.management.ClassSessionStatus
import com.classitda.feature.instructor.management.classes.component.ClassSessionCard
import com.classitda.feature.instructor.management.classes.component.ClassSessionDateHeader
import com.classitda.feature.instructor.management.classes.model.ClassSessionGroupUiModel
import com.classitda.feature.instructor.management.classes.model.ClassSessionUiModel
import com.classitda.feature.instructor.management.component.ClassCategoryFilter
import com.classitda.feature.instructor.management.component.ClassCategoryFilterRow
import com.classitda.feature.instructor.management.component.CreateFabButton

@Composable
internal fun ClassListScreen(
    sessionGroups: List<ClassSessionGroupUiModel>,
    customCategories: List<String>,
    selectedFilterLabel: String,
    isRefreshing: Boolean,
    snackbarHostState: SnackbarHostState,
    onFilterSelected: (String) -> Unit,
    onBackClick: () -> Unit,
    onCreateSessionClick: () -> Unit,
    onSessionCardClick: (String) -> Unit,
    bottomBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val visibleSessionGroups =
        remember(sessionGroups, selectedFilterLabel) {
            if (selectedFilterLabel == ClassCategoryFilter.ALL.label) {
                sessionGroups
            } else {
                sessionGroups
                    .map { group -> group.copy(sessions = group.sessions.filter { selectedFilterLabel in it.tags }) }
                    .filter { it.sessions.isNotEmpty() }
            }
        }

    Scaffold(
        modifier = modifier,
        containerColor = InsColors.Background,
        topBar = {
            NavigateBackTopBar(
                onNavigateBack = onBackClick,
                modifier = Modifier.background(InsColors.Surface),
                title = "수업 목록",
            )
        },
        bottomBar = bottomBar,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            CreateFabButton(onClick = onCreateSessionClick, contentDescription = "수업 추가")
        },
        floatingActionButtonPosition = FabPosition.End,
    ) { contentPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(contentPadding),
        ) {
            if (isRefreshing) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            if (sessionGroups.isEmpty()) {
                ClassListEmpty(
                    onCreateSessionClick = onCreateSessionClick,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                ClassCategoryFilterRow(
                    customCategories = customCategories,
                    selectedLabel = selectedFilterLabel,
                    onFilterSelected = onFilterSelected,
                    modifier =
                        Modifier.padding(
                            vertical = AppSpacing.md,
                        ),
                )

                LazyColumn(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(horizontal = AppSpacing.screenPadding),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
                    contentPadding = PaddingValues(bottom = AppSpacing.xxxl),
                ) {
                    visibleSessionGroups.forEach { group ->
                        item(key = group.dateText) {
                            ClassSessionDateHeader(dateText = group.dateText)
                        }
                        items(group.sessions, key = { it.id }) { session ->
                            ClassSessionCard(
                                session = session,
                                onClick = { onSessionCardClick(session.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ClassListEmpty(
    onCreateSessionClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(AppSpacing.screenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "생성된 수업이 없습니다.\n새로운 수업을 생성해주세요!",
            style = MaterialTheme.typography.bodyMedium,
            color = InsColors.TextSecondary,
            textAlign = TextAlign.Center,
        )
        Button(
            onClick = onCreateSessionClick,
            colors = ButtonDefaults.buttonColors(containerColor = InsColors.Primary),
            modifier = Modifier.padding(top = AppSpacing.lg),
        ) {
            Text(text = "생성하기")
        }
    }
}

@Composable
@Preview
private fun ClassListScreenPreview() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        var selectedFilterLabel by remember { mutableStateOf(ClassCategoryFilter.ALL.label) }

        ClassListScreen(
            selectedFilterLabel = selectedFilterLabel,
            isRefreshing = false,
            snackbarHostState = remember { SnackbarHostState() },
            onFilterSelected = { selectedFilterLabel = it },
            onBackClick = {},
            onCreateSessionClick = {},
            onSessionCardClick = {},
            bottomBar = {},
            customCategories = listOf("필라테스", "요가"),
            sessionGroups =
                listOf(
                    ClassSessionGroupUiModel(
                        dateText = "8월 12일 수요일",
                        sessions =
                            listOf(
                                ClassSessionUiModel(
                                    id = "1",
                                    tags = listOf("그룹 수업", "필라테스"),
                                    title = "리포머 밸런스",
                                    timeRangeText = "오후 7:30 ~ 8:20",
                                    reservedCount = 8,
                                    capacity = 10,
                                    status = ClassSessionStatus.SCHEDULED,
                                ),
                            ),
                    ),
                    ClassSessionGroupUiModel(
                        dateText = "8월 9일 일요일",
                        sessions =
                            listOf(
                                ClassSessionUiModel(
                                    id = "2",
                                    tags = listOf("그룹 수업", "요가"),
                                    title = "하타 요가",
                                    timeRangeText = "오전 11:00 ~ 11:50",
                                    reservedCount = 8,
                                    capacity = 10,
                                    status = ClassSessionStatus.CANCELLED,
                                ),
                            ),
                    ),
                    ClassSessionGroupUiModel(
                        dateText = "8월 8일 토요일",
                        sessions =
                            listOf(
                                ClassSessionUiModel(
                                    id = "3",
                                    tags = listOf("개인 수업", "요가"),
                                    title = "리포머 밸런스",
                                    timeRangeText = "오전 10:00 ~ 10:50",
                                    reservedCount = 1,
                                    capacity = 1,
                                    status = ClassSessionStatus.COMPLETED,
                                ),
                            ),
                    ),
                ),
        )
    }
}

@Composable
@Preview
private fun ClassListScreenEmptyPreview() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        ClassListScreen(
            selectedFilterLabel = ClassCategoryFilter.ALL.label,
            isRefreshing = false,
            snackbarHostState = remember { SnackbarHostState() },
            onFilterSelected = {},
            onBackClick = {},
            onCreateSessionClick = {},
            onSessionCardClick = {},
            bottomBar = {},
            customCategories = emptyList(),
            sessionGroups = emptyList(),
        )
    }
}
