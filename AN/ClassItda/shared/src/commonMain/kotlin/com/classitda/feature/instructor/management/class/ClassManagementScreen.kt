package com.classitda.feature.instructor.management.`class`

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.component.NavigateBackTopBar
import com.classitda.domain.model.instructor.management.ClassSessionStatus
import com.classitda.feature.instructor.management.`class`.component.ClassCategoryFilter
import com.classitda.feature.instructor.management.`class`.component.ClassCategoryFilterRow
import com.classitda.feature.instructor.management.`class`.component.ClassManagementAddButton
import com.classitda.feature.instructor.management.`class`.component.ClassManagementTopTab
import com.classitda.feature.instructor.management.`class`.component.ClassManagementTopTabSelector
import com.classitda.feature.instructor.management.`class`.component.ClassSessionCard
import com.classitda.feature.instructor.management.`class`.component.ClassSessionDateHeader
import com.classitda.feature.instructor.management.`class`.component.ClassTemplateCard
import com.classitda.feature.instructor.management.`class`.component.DeleteTemplateConfirmDialog
import com.classitda.feature.instructor.management.`class`.model.ClassScheduleUiModel
import com.classitda.feature.instructor.management.`class`.model.ClassSessionGroupUiModel
import com.classitda.feature.instructor.management.`class`.model.ClassSessionUiModel
import com.classitda.feature.instructor.management.`class`.model.ClassTemplateUiModel

@Composable
internal fun ClassManagementScreen(
    templates: List<ClassTemplateUiModel>,
    sessionGroups: List<ClassSessionGroupUiModel>,
    customCategories: List<String>,
    selectedTopTab: ClassManagementTopTab,
    selectedFilterLabel: String,
    onTopTabSelected: (ClassManagementTopTab) -> Unit,
    onFilterSelected: (String) -> Unit,
    onBackClick: () -> Unit,
    onCreateTemplateClick: () -> Unit,
    onCreateSessionClick: () -> Unit,
    onTemplateCardClick: (String) -> Unit,
    onTemplateEditClick: (String) -> Unit,
    onTemplateDeleteConfirmed: (String) -> Unit,
    onSessionCardClick: (String) -> Unit,
    bottomBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    var pendingDeleteTemplateId by remember { mutableStateOf<String?>(null) }

    val visibleTemplates =
        remember(templates, selectedFilterLabel) {
            if (selectedFilterLabel == ClassCategoryFilter.ALL.label) {
                templates
            } else {
                templates.filter { selectedFilterLabel in it.tags }
            }
        }

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
                title = "수업 관리",
            )
        },
        bottomBar = bottomBar,
        floatingActionButton = {
            ClassManagementAddButton(
                onCreateTemplateClick = onCreateTemplateClick,
                onCreateSessionClick = onCreateSessionClick,
            )
        },
        floatingActionButtonPosition = FabPosition.End,
    ) { contentPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(contentPadding),
        ) {
            ClassManagementTopTabSelector(
                selectedTab = selectedTopTab,
                onTabSelected = onTopTabSelected,
                modifier =
                    Modifier.padding(
                        horizontal = AppSpacing.screenPadding,
                        vertical = AppSpacing.md,
                    ),
            )

            ClassCategoryFilterRow(
                customCategories = customCategories,
                selectedLabel = selectedFilterLabel,
                onFilterSelected = onFilterSelected,
                modifier = Modifier.padding(bottom = AppSpacing.md),
            )

            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = AppSpacing.screenPadding),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.cardGap),
                contentPadding = PaddingValues(bottom = AppSpacing.xxxl),
            ) {
                when (selectedTopTab) {
                    ClassManagementTopTab.TEMPLATE -> {
                        items(visibleTemplates, key = { it.id }) { template ->
                            ClassTemplateCard(
                                template = template,
                                onClick = { onTemplateCardClick(template.id) },
                                onEditClick = { onTemplateEditClick(template.id) },
                                onDeleteClick = { pendingDeleteTemplateId = template.id },
                            )
                        }
                    }

                    ClassManagementTopTab.MY_CLASS -> {
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

    pendingDeleteTemplateId?.let { templateId ->
        DeleteTemplateConfirmDialog(
            onDismissRequest = { pendingDeleteTemplateId = null },
            onConfirm = {
                pendingDeleteTemplateId = null
                onTemplateDeleteConfirmed(templateId)
            },
        )
    }
}

@Composable
@Preview
private fun ClassManagementScreenPreview() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        var selectedTopTab by remember { mutableStateOf(ClassManagementTopTab.TEMPLATE) }
        var selectedFilterLabel by remember { mutableStateOf(ClassCategoryFilter.ALL.label) }

        ClassManagementScreen(
            selectedTopTab = selectedTopTab,
            selectedFilterLabel = selectedFilterLabel,
            onTopTabSelected = { selectedTopTab = it },
            onFilterSelected = { selectedFilterLabel = it },
            onBackClick = {},
            onCreateTemplateClick = {},
            onCreateSessionClick = {},
            onTemplateCardClick = {},
            onTemplateEditClick = {},
            onTemplateDeleteConfirmed = {},
            onSessionCardClick = {},
            bottomBar = {},
            customCategories = listOf("필라테스", "요가"),
            templates =
                listOf(
                    ClassTemplateUiModel(
                        id = "1",
                        tags = listOf("그룹 수업", "필라테스"),
                        title = "리포머 밸런스",
                        durationText = "50분",
                        capacityText = "8명",
                        schedule = ClassScheduleUiModel(timeRangeText = "10:00 ~ 10:50", repeatDaysText = "월, 수"),
                    ),
                    ClassTemplateUiModel(
                        id = "2",
                        tags = listOf("그룹 수업", "필라테스"),
                        title = "리포머 밸런스",
                        durationText = "50분",
                        capacityText = "8명",
                        schedule = ClassScheduleUiModel(timeRangeText = "12:00 ~ 12:50", repeatDaysText = "월, 수"),
                    ),
                    ClassTemplateUiModel(
                        id = "3",
                        tags = listOf("개인 수업", "요가"),
                        title = "1:1 개인 수업",
                        durationText = "50분",
                        capacityText = "1명",
                        schedule = null,
                    ),
                ),
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
