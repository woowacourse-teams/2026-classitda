package com.classitda.feature.instructor.classsession.member.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.component.NavigateBackTopBar
import com.classitda.domain.model.instructor.management.ClassSessionStatus
import com.classitda.feature.instructor.classsession.detail.component.ClassSessionDetailInfoCard
import com.classitda.feature.instructor.classsession.detail.model.ClassSessionDetailUiModel
import com.classitda.feature.instructor.classsession.detail.model.ClassSessionMemberUiModel
import com.classitda.feature.instructor.classsession.member.edit.component.ClassSessionMemberAddSection
import com.classitda.feature.instructor.classsession.member.edit.component.ClassSessionMemberEditBookedRow
import com.classitda.feature.instructor.classsession.member.edit.component.ExistingMemberBottomSheet
import com.classitda.feature.instructor.classsession.member.edit.model.ClassSessionMemberEditUiModel
import com.classitda.feature.instructor.classsession.member.edit.model.MemberAddType
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun ClassSessionMemberEditRoute(
    sessionId: String,
    onBackClick: () -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ClassSessionMemberEditViewModel = koinViewModel(),
) {
    LaunchedEffect(sessionId) {
        viewModel.load(sessionId)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    when (val state = uiState) {
        ClassSessionMemberEditUiState.Loading -> {
            ClassSessionMemberEditLoading(modifier)
        }

        is ClassSessionMemberEditUiState.Error -> {
            ClassSessionMemberEditError(
                message = state.message,
                onRetry = { viewModel.load(sessionId) },
                modifier = modifier,
            )
        }

        is ClassSessionMemberEditUiState.Success -> {
            ClassSessionMemberEditStateful(
                content = state.content,
                onBackClick = onBackClick,
                onSave = { members -> viewModel.saveMembers(sessionId, members, onSaved) },
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun ClassSessionMemberEditStateful(
    content: ClassSessionMemberEditUiModel,
    onBackClick: () -> Unit,
    onSave: (List<ClassSessionMemberUiModel>) -> Unit,
    modifier: Modifier = Modifier,
) {
    var bookedMembers by remember(content.detail.id) { mutableStateOf(content.detail.members) }
    var temporaryMemberSequence by remember(content.detail.id) { mutableIntStateOf(0) }
    var addType by remember { mutableStateOf(MemberAddType.EXISTING) }
    var temporaryName by remember { mutableStateOf("") }
    var isExistingSheetVisible by remember { mutableStateOf(false) }
    var memberQuery by remember { mutableStateOf("") }
    var selectedMemberIds by remember { mutableStateOf(emptySet<String>()) }
    val remainingCapacity = (content.detail.capacity - bookedMembers.size).coerceAtLeast(0)

    val selectableMembers =
        content.availableMembers
            .filterNot { candidate -> bookedMembers.any { it.id == candidate.id } }
            .filter { it.name.contains(memberQuery, ignoreCase = true) }

    ClassSessionMemberEditStateless(
        detail = content.detail.copy(members = bookedMembers, reservedCount = bookedMembers.size),
        addType = addType,
        temporaryName = temporaryName,
        onBackClick = onBackClick,
        onAddTypeChange = { addType = it },
        onTemporaryNameChange = { temporaryName = it },
        onRemoveMember = { memberId -> bookedMembers = bookedMembers.filterNot { it.id == memberId } },
        onExistingAddClick = {
            selectedMemberIds = emptySet()
            memberQuery = ""
            isExistingSheetVisible = true
        },
        onTemporaryAddClick = {
            if (temporaryName.isNotBlank() && bookedMembers.size < content.detail.capacity) {
                val temporaryMemberId = "temporary-${temporaryMemberSequence++}"
                bookedMembers =
                    bookedMembers +
                    ClassSessionMemberUiModel(
                        id = temporaryMemberId,
                        name = temporaryName.trim(),
                        isTemporary = true,
                    )
                temporaryName = ""
            }
        },
        onSaveClick = { onSave(bookedMembers) },
        modifier = modifier,
    )

    if (isExistingSheetVisible) {
        ExistingMemberBottomSheet(
            members = selectableMembers,
            query = memberQuery,
            selectedMemberIds = selectedMemberIds,
            onQueryChange = { memberQuery = it },
            onMemberClick = { memberId ->
                selectedMemberIds =
                    if (memberId in selectedMemberIds) {
                        selectedMemberIds - memberId
                    } else if (selectedMemberIds.size < remainingCapacity) {
                        selectedMemberIds + memberId
                    } else {
                        selectedMemberIds
                    }
            },
            onConfirmClick = {
                bookedMembers =
                    bookedMembers +
                    content.availableMembers
                        .filter { it.id in selectedMemberIds }
                        .take(remainingCapacity)
                selectedMemberIds = emptySet()
                memberQuery = ""
                isExistingSheetVisible = false
            },
            onDismissRequest = { isExistingSheetVisible = false },
        )
    }
}

@Composable
internal fun ClassSessionMemberEditStateless(
    detail: ClassSessionDetailUiModel,
    addType: MemberAddType,
    temporaryName: String,
    onBackClick: () -> Unit,
    onAddTypeChange: (MemberAddType) -> Unit,
    onTemporaryNameChange: (String) -> Unit,
    onRemoveMember: (String) -> Unit,
    onExistingAddClick: () -> Unit,
    onTemporaryAddClick: () -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = InsColors.Background,
        topBar = {
            NavigateBackTopBar(
                onNavigateBack = onBackClick,
                modifier = Modifier.background(InsColors.Surface),
                title = "회원 수정",
            )
        },
        bottomBar = {
            Button(
                onClick = onSaveClick,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = AppSpacing.screenPadding, vertical = AppSpacing.sm),
                shape = AppShape.Card,
                colors = ButtonDefaults.buttonColors(containerColor = InsColors.Primary),
            ) {
                Text("수정 완료")
            }
        },
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(contentPadding),
            contentPadding =
                PaddingValues(
                    horizontal = AppSpacing.screenPadding,
                    vertical = AppSpacing.lg,
                ),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            item {
                Text(
                    text = detail.dateText,
                    style = MaterialTheme.typography.titleSmall,
                    color = InsColors.TextPrimary,
                )
            }
            item {
                ClassSessionDetailInfoCard(detail = detail)
            }
            item {
                RowSectionTitle(
                    title = "예약 회원",
                    count = "${detail.members.size}명 선택됨",
                )
            }
            items(detail.members, key = { it.id }) { member ->
                ClassSessionMemberEditBookedRow(
                    member = member,
                    onRemoveClick = { onRemoveMember(member.id) },
                )
            }
            item {
                ClassSessionMemberAddSection(
                    addType = addType,
                    temporaryName = temporaryName,
                    onAddTypeChange = onAddTypeChange,
                    onTemporaryNameChange = onTemporaryNameChange,
                    onExistingAddClick = onExistingAddClick,
                    onTemporaryAddClick = onTemporaryAddClick,
                    modifier = Modifier.padding(top = AppSpacing.md),
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
    androidx.compose.foundation.layout.Row(
        modifier = modifier.fillMaxWidth(),
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
private fun ClassSessionMemberEditLoading(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize().background(InsColors.Background),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = InsColors.Purple)
    }
}

@Composable
private fun ClassSessionMemberEditError(
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

@Preview(name = "회원 수정", showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun ClassSessionMemberEditStatelessPreview() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        ClassSessionMemberEditStateless(
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
                            ClassSessionMemberUiModel(id = "3", name = "박지수", isTemporary = true),
                        ),
                ),
            addType = MemberAddType.EXISTING,
            temporaryName = "",
            onBackClick = {},
            onAddTypeChange = {},
            onTemporaryNameChange = {},
            onRemoveMember = {},
            onExistingAddClick = {},
            onTemporaryAddClick = {},
            onSaveClick = {},
        )
    }
}
