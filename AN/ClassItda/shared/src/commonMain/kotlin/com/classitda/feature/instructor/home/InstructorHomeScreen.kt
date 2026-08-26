package com.classitda.feature.instructor.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.studio.InstructorStudioContext
import com.classitda.domain.model.instructor.management.ClassSession
import com.classitda.domain.model.instructor.management.ClassSessionStatus
import com.classitda.domain.model.studio.Studio
import com.classitda.feature.instructor.home.component.InstructorHomeHeader
import com.classitda.feature.instructor.home.component.InstructorHomeSummary
import com.classitda.feature.instructor.home.component.InstructorStudioSwitchSheet
import com.classitda.feature.instructor.home.component.InstructorTimeline
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun InstructorHomeRoute(
    onSessionClick: (String) -> Unit,
    onStudioChanged: () -> Unit = {},
    bottomBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: InstructorHomeViewModel = koinViewModel(),
) = InstructorHomeStateful(
    onSessionClick = onSessionClick,
    onStudioChanged = onStudioChanged,
    bottomBar = bottomBar,
    modifier = modifier,
    viewModel = viewModel,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun InstructorHomeStateful(
    onSessionClick: (String) -> Unit,
    onStudioChanged: () -> Unit = {},
    bottomBar: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: InstructorHomeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val studioContext = koinInject<InstructorStudioContext>()
    val scope = rememberCoroutineScope()
    var selectedStudio by remember { mutableStateOf<Studio?>(null) }
    var pendingStudio by remember { mutableStateOf<Studio?>(null) }
    var studios by remember { mutableStateOf<List<Studio>>(emptyList()) }
    var studioLoadError by remember { mutableStateOf<String?>(null) }
    var isStudioSheetVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        selectedStudio = runCatching { studioContext.getSelectedStudio() }.getOrNull()
    }
    Scaffold(
        modifier = modifier,
        containerColor = InsColors.Background,
        bottomBar = bottomBar,
    ) { contentPadding ->
        when (val state = uiState) {
            InstructorHomeUiState.Loading -> {
                LoadingContent(Modifier.padding(contentPadding))
            }

            is InstructorHomeUiState.Error -> {
                ErrorContent(
                    message = state.message,
                    onRetry = viewModel::retry,
                    modifier = Modifier.padding(contentPadding),
                )
            }

            is InstructorHomeUiState.Success -> {
                InstructorHomeStateless(
                    sessions = state.sessions,
                    studioName = selectedStudio?.name ?: "시설 선택",
                    onStudioClick = {
                        scope.launch {
                            studioLoadError = null
                            runCatching {
                                studioContext.getStudios()
                            }.onSuccess { loadedStudios ->
                                studios = loadedStudios
                                pendingStudio = selectedStudio
                            }.onFailure { error -> studioLoadError = error.message }
                            isStudioSheetVisible = true
                        }
                    },
                    onSessionClick = onSessionClick,
                    modifier = Modifier.padding(contentPadding),
                )
            }
        }

        if (isStudioSheetVisible) {
            InstructorStudioSwitchSheet(
                studios = studios,
                selectedStudioId = pendingStudio?.id?.value,
                errorMessage = studioLoadError,
                onRetry = {
                    scope.launch {
                        studioLoadError = null
                        runCatching {
                            studioContext.getStudios()
                        }.onSuccess { loadedStudios ->
                            studios = loadedStudios
                            pendingStudio = selectedStudio
                        }.onFailure { error -> studioLoadError = error.message }
                    }
                },
                onStudioClick = { studio ->
                    pendingStudio = studio
                },
                onConfirmClick = {
                    pendingStudio?.let { studio ->
                        scope.launch {
                            studioContext.selectStudio(studio.id.value)
                            viewModel.retry()
                            selectedStudio = studio
                            isStudioSheetVisible = false
                            onStudioChanged()
                        }
                    }
                },
                onDismissRequest = { isStudioSheetVisible = false },
            )
        }
    }
}

@Composable
internal fun InstructorHomeStateless(
    sessions: List<ClassSession>,
    onSessionClick: (String) -> Unit,
    studioName: String = "클래스잇다 요가&필라테스",
    onStudioClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val visibleSessions = sessions.sortedBy { it.startAt }

    LazyColumn(
        modifier = modifier.fillMaxSize().background(InsColors.Background),
        contentPadding = PaddingValues(bottom = AppSpacing.xxxl),
    ) {
        item {
            InstructorHomeHeader(
                studioName = studioName,
                onStudioClick = onStudioClick,
            )
        }
        item {
            InstructorHomeSummary(sessions)
        }
        item {
            InstructorTimeline(
                sessions = visibleSessions.take(4),
                onSessionClick = onSessionClick,
            )
        }
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(color = InsColors.Primary)
    }
}

@Composable
private fun ErrorContent(
    message: String?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = message ?: "일정을 불러오지 못했어요",
            color = InsColors.TextSecondary,
        )
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = InsColors.Primary),
        ) {
            Text("다시 시도")
        }
    }
}

@Preview(name = "강사 홈 - 수업 있음", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun InstructorHomeStatelessPreview() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        InstructorHomeStateless(
            sessions =
                listOf(
                    ClassSession(
                        id = "1",
                        tags = listOf("그룹 수업"),
                        title = "체어 밸런스",
                        startAt = LocalDateTime(2026, 8, 5, 14, 0),
                        endAt = LocalDateTime(2026, 8, 5, 14, 50),
                        reservedCount = 7,
                        capacity = 8,
                        status = ClassSessionStatus.COMPLETED,
                    ),
                    ClassSession(
                        id = "2",
                        tags = listOf("그룹 수업"),
                        title = "리포머 밸런스",
                        startAt = LocalDateTime(2026, 8, 5, 19, 30),
                        endAt = LocalDateTime(2026, 8, 5, 20, 20),
                        reservedCount = 6,
                        capacity = 6,
                        status = ClassSessionStatus.SCHEDULED,
                    ),
                    ClassSession(
                        id = "3",
                        tags = listOf("그룹 수업"),
                        title = "바렐 코어 테라피",
                        startAt = LocalDateTime(2026, 8, 5, 20, 30),
                        endAt = LocalDateTime(2026, 8, 5, 21, 20),
                        reservedCount = 6,
                        capacity = 8,
                        status = ClassSessionStatus.SCHEDULED,
                    ),
                    ClassSession(
                        id = "4",
                        tags = listOf("그룹 수업"),
                        title = "릴렉스 스트레칭",
                        startAt = LocalDateTime(2026, 8, 5, 21, 30),
                        endAt = LocalDateTime(2026, 8, 5, 22, 20),
                        reservedCount = 8,
                        capacity = 8,
                        status = ClassSessionStatus.SCHEDULED,
                    ),
                ),
            onSessionClick = {},
        )
    }
}
