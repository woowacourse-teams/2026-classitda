package com.classitda.feature.instructor.management.classtemplates

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import com.classitda.domain.model.instructor.management.ClassForm
import com.classitda.domain.model.instructor.management.ClassType
import com.classitda.feature.instructor.management.classtemplates.component.ClassTemplateCard
import com.classitda.feature.instructor.management.classtemplates.component.ClassTemplateFilterRow
import com.classitda.feature.instructor.management.classtemplates.component.DeleteTemplateConfirmDialog
import com.classitda.feature.instructor.management.classtemplates.model.ClassScheduleUiModel
import com.classitda.feature.instructor.management.classtemplates.model.ClassTemplateUiModel
import com.classitda.feature.instructor.management.component.CategoryFilter
import com.classitda.feature.instructor.management.component.CreateFabButton
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ClassTemplateManagementScreen(
    templates: List<ClassTemplateUiModel>,
    classTypes: List<ClassType>,
    selectedFilter: CategoryFilter,
    isRefreshing: Boolean,
    snackbarHostState: SnackbarHostState,
    onFilterSelected: (CategoryFilter) -> Unit,
    onRefresh: () -> Unit,
    onBackClick: () -> Unit,
    onCreateTemplateClick: () -> Unit,
    onTemplateCardClick: (String) -> Unit,
    onTemplateEditClick: (String) -> Unit,
    onTemplateDeleteConfirmed: (String) -> Unit,
    bottomBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    var pendingDeleteTemplateId by remember { mutableStateOf<String?>(null) }

    val categoryFilters =
        remember(classTypes) {
            listOf(
                CategoryFilter.All,
                CategoryFilter.Form(ClassForm.INDIVIDUAL),
                CategoryFilter.Form(ClassForm.GROUP),
            ) + classTypes.map(CategoryFilter::Category)
        }

    val visibleTemplates =
        remember(templates, selectedFilter) {
            when (val filter = selectedFilter) {
                CategoryFilter.All -> templates
                is CategoryFilter.Form -> templates.filter { it.classForm == filter.classForm }
                is CategoryFilter.Category -> templates.filter { it.classTypeId == filter.classType.id }
            }
        }

    Scaffold(
        modifier = modifier,
        containerColor = InsColors.Background,
        topBar = {
            NavigateBackTopBar(
                onNavigateBack = onBackClick,
                modifier = Modifier.background(InsColors.Surface),
                title = "수업 템플릿 관리",
            )
        },
        bottomBar = bottomBar,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            CreateFabButton(onClick = onCreateTemplateClick, contentDescription = "수업 템플릿 추가")
        },
        floatingActionButtonPosition = FabPosition.End,
    ) { contentPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize().padding(contentPadding),
        ) {
            if (templates.isEmpty()) {
                ClassTemplateManagementEmpty(
                    onCreateTemplateClick = onCreateTemplateClick,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    ClassTemplateFilterRow(
                        categoryFilters = categoryFilters,
                        selectedFilter = selectedFilter,
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
                        items(visibleTemplates, key = { it.id }) { template ->
                            ClassTemplateCard(
                                template = template,
                                onClick = { onTemplateCardClick(template.id) },
                                onEditClick = { onTemplateEditClick(template.id) },
                                onDeleteClick = { pendingDeleteTemplateId = template.id },
                            )
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
private fun ClassTemplateManagementEmpty(
    onCreateTemplateClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(AppSpacing.screenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "생성된 템플릿이 없습니다.\n새로운 템플릿을 생성해주세요!",
            style = MaterialTheme.typography.bodyMedium,
            color = InsColors.TextSecondary,
            textAlign = TextAlign.Center,
        )
        Button(
            onClick = onCreateTemplateClick,
            colors = ButtonDefaults.buttonColors(containerColor = InsColors.Primary),
            modifier = Modifier.padding(top = AppSpacing.lg),
        ) {
            Text(text = "생성하기")
        }
    }
}

@Composable
@Preview
private fun ClassTemplateManagementScreenPreview() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        var selectedFilter by remember { mutableStateOf<CategoryFilter>(CategoryFilter.All) }

        ClassTemplateManagementScreen(
            selectedFilter = selectedFilter,
            isRefreshing = false,
            snackbarHostState = remember { SnackbarHostState() },
            onFilterSelected = { selectedFilter = it },
            onRefresh = {},
            onBackClick = {},
            onCreateTemplateClick = {},
            onTemplateCardClick = {},
            onTemplateEditClick = {},
            onTemplateDeleteConfirmed = {},
            bottomBar = {},
            classTypes =
                listOf(
                    ClassType(id = "1", name = "필라테스"),
                    ClassType(id = "2", name = "요가"),
                ),
            templates =
                listOf(
                    ClassTemplateUiModel(
                        id = "1",
                        classForm = ClassForm.GROUP,
                        classTypeId = "1",
                        categoryNames = listOf("필라테스"),
                        title = "리포머 밸런스",
                        durationText = "50분",
                        capacityText = "8명",
                        schedule =
                            ClassScheduleUiModel(
                                startTime = LocalTime(10, 0),
                                repeatDays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
                                timeRangeText = "10:00 ~ 10:50",
                                repeatDaysText = "월, 수",
                            ),
                    ),
                    ClassTemplateUiModel(
                        id = "2",
                        classForm = ClassForm.GROUP,
                        classTypeId = "1",
                        categoryNames = listOf("필라테스"),
                        title = "리포머 밸런스",
                        durationText = "50분",
                        capacityText = "8명",
                        schedule =
                            ClassScheduleUiModel(
                                startTime = LocalTime(12, 0),
                                repeatDays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY),
                                timeRangeText = "12:00 ~ 12:50",
                                repeatDaysText = "월, 수",
                            ),
                    ),
                    ClassTemplateUiModel(
                        id = "3",
                        classForm = ClassForm.INDIVIDUAL,
                        classTypeId = "2",
                        categoryNames = listOf("요가"),
                        title = "1:1 개인 수업",
                        durationText = "50분",
                        capacityText = "1명",
                        schedule = null,
                    ),
                ),
        )
    }
}

@Composable
@Preview
private fun ClassTemplateManagementScreenEmptyPreview() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        ClassTemplateManagementScreen(
            selectedFilter = CategoryFilter.All,
            isRefreshing = false,
            snackbarHostState = remember { SnackbarHostState() },
            onFilterSelected = {},
            onRefresh = {},
            onBackClick = {},
            onCreateTemplateClick = {},
            onTemplateCardClick = {},
            onTemplateEditClick = {},
            onTemplateDeleteConfirmed = {},
            bottomBar = {},
            classTypes = emptyList(),
            templates = emptyList(),
        )
    }
}
