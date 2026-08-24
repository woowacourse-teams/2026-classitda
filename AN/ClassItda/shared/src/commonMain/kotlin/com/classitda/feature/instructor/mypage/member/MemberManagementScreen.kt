package com.classitda.feature.instructor.mypage.member

import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.ic_arrow_back
import classitda.shared.generated.resources.ic_arrow_forward
import classitda.shared.generated.resources.ic_close
import classitda.shared.generated.resources.ic_expand_more
import classitda.shared.generated.resources.ic_person_add
import classitda.shared.generated.resources.ic_search
import classitda.shared.generated.resources.instructor_member_delete_cancel
import classitda.shared.generated.resources.instructor_member_delete_confirm
import classitda.shared.generated.resources.instructor_member_delete_failed
import classitda.shared.generated.resources.instructor_member_delete_message
import classitda.shared.generated.resources.instructor_member_delete_name_error
import classitda.shared.generated.resources.instructor_member_delete_pane_title
import classitda.shared.generated.resources.instructor_member_delete_placeholder
import classitda.shared.generated.resources.instructor_member_delete_submitting
import classitda.shared.generated.resources.instructor_member_delete_success
import classitda.shared.generated.resources.instructor_member_delete_success_title
import classitda.shared.generated.resources.instructor_member_delete_title
import classitda.shared.generated.resources.instructor_member_detail_delete
import classitda.shared.generated.resources.instructor_member_detail_edit
import classitda.shared.generated.resources.instructor_member_management_add
import classitda.shared.generated.resources.instructor_member_management_back
import classitda.shared.generated.resources.instructor_member_management_empty_description
import classitda.shared.generated.resources.instructor_member_management_empty_title
import classitda.shared.generated.resources.instructor_member_management_error_description
import classitda.shared.generated.resources.instructor_member_management_error_title
import classitda.shared.generated.resources.instructor_member_management_list_title
import classitda.shared.generated.resources.instructor_member_management_loading
import classitda.shared.generated.resources.instructor_member_management_retry
import classitda.shared.generated.resources.instructor_member_management_search_empty_description
import classitda.shared.generated.resources.instructor_member_management_search_empty_title
import classitda.shared.generated.resources.instructor_member_management_search_label
import classitda.shared.generated.resources.instructor_member_management_search_placeholder
import classitda.shared.generated.resources.instructor_member_management_sort_name
import classitda.shared.generated.resources.instructor_member_management_sort_recent
import classitda.shared.generated.resources.instructor_member_management_title
import classitda.shared.generated.resources.instructor_member_management_total_count
import classitda.shared.generated.resources.instructor_member_management_total_label
import classitda.shared.generated.resources.instructor_member_registration_success_confirm
import classitda.shared.generated.resources.phone_number_change_close
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.appTypography
import com.classitda.domain.model.instructor.mypage.InstructorMemberId
import com.classitda.feature.instructor.mypage.contract.MemberListUiModel
import com.classitda.feature.instructor.mypage.contract.MemberManagementAction
import com.classitda.feature.instructor.mypage.contract.MemberManagementActionState
import com.classitda.feature.instructor.mypage.contract.MemberManagementDeleteError
import com.classitda.feature.instructor.mypage.contract.MemberManagementUiError
import com.classitda.feature.instructor.mypage.contract.MemberManagementUiState
import com.classitda.feature.instructor.mypage.contract.MemberSortOption
import com.classitda.feature.instructor.mypage.contract.MemberUiModel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun MemberManagementScreen(
    uiState: MemberManagementUiState,
    onAction: (MemberManagementAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    var actionMember by remember { mutableStateOf<MemberUiModel?>(null) }
    var deleteSuccessVisible by remember { mutableStateOf(false) }
    var deleteSuccessPresented by remember { mutableStateOf(false) }
    LaunchedEffect(uiState) {
        val content = uiState as? MemberManagementUiState.Content
        if (content?.actionState is MemberManagementActionState.Deleted) {
            deleteSuccessPresented = true
            deleteSuccessVisible = true
        }
    }
    LaunchedEffect(deleteSuccessVisible) {
        val content = uiState as? MemberManagementUiState.Content
        val deleted = content?.actionState as? MemberManagementActionState.Deleted
        if (deleteSuccessPresented && !deleteSuccessVisible && deleted != null) {
            onAction(MemberManagementAction.DeleteAcknowledged)
        }
    }
    Scaffold(
        modifier = modifier,
        containerColor = InsColors.Background,
        topBar = {
            MemberManagementTopBar(
                onBack = { onAction(MemberManagementAction.Back) },
                onAdd = { onAction(MemberManagementAction.OpenMemberRegistration) },
            )
        },
    ) { innerPadding ->
        when (uiState) {
            MemberManagementUiState.Loading -> {
                MemberManagementLoading(
                    modifier = Modifier.padding(innerPadding),
                )
            }

            is MemberManagementUiState.Empty -> {
                MemberManagementListContent(
                    totalCount = 0,
                    query = "",
                    members = emptyList(),
                    sortOrder = uiState.sortOrder,
                    emptyState = MemberListEmptyState.Empty,
                    onAction = onAction,
                    modifier = Modifier.padding(innerPadding),
                )
            }

            is MemberManagementUiState.Content -> {
                MemberManagementListContent(
                    totalCount = uiState.page.totalCount,
                    query = uiState.query,
                    members = uiState.page.members,
                    sortOrder = uiState.sortOrder,
                    emptyState = if (uiState.page.members.isEmpty()) MemberListEmptyState.Empty else null,
                    onLongPress = { actionMember = it },
                    onAction = onAction,
                    modifier = Modifier.padding(innerPadding),
                )
            }

            is MemberManagementUiState.SearchEmpty -> {
                MemberManagementListContent(
                    totalCount = null,
                    query = uiState.query,
                    members = emptyList(),
                    sortOrder = uiState.sortOrder,
                    emptyState = MemberListEmptyState.SearchEmpty,
                    onAction = onAction,
                    modifier = Modifier.padding(innerPadding),
                )
            }

            is MemberManagementUiState.Error -> {
                MemberManagementError(
                    onRetry = { onAction(MemberManagementAction.Retry) },
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }
    }
    actionMember?.let { member ->
        MemberActionDialog(
            onDismiss = { actionMember = null },
            onEdit = {
                actionMember = null
                onAction(MemberManagementAction.EditMember(member.id))
            },
            onDelete = {
                actionMember = null
                onAction(MemberManagementAction.RequestDelete(member.id))
            },
        )
    }
    val content = uiState as? MemberManagementUiState.Content
    val deleteState = content?.actionState
    val deleteMember =
        content?.page?.members?.firstOrNull { member ->
            when (deleteState) {
                is MemberManagementActionState.Confirming -> member.id == deleteState.memberId
                is MemberManagementActionState.Submitting -> member.id == deleteState.memberId
                is MemberManagementActionState.Failed -> member.id == deleteState.memberId
                is MemberManagementActionState.Deleted -> member.id == deleteState.memberId
                else -> false
            }
        }
    if (deleteMember != null && deleteState != null) {
        MemberDeleteDialog(deleteMember.name, deleteState, onAction)
    }
    if (deleteSuccessVisible && deleteState is MemberManagementActionState.Deleted) {
        MemberDeleteSuccessDialog(onClose = { deleteSuccessVisible = false })
    }
}

@Composable
private fun MemberManagementTopBar(
    onBack: () -> Unit,
    onAdd: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxWidth().statusBarsPadding(),
        contentAlignment = Alignment.Center,
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.CenterStart),
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_arrow_back),
                contentDescription = stringResource(Res.string.instructor_member_management_back),
                tint = InsColors.TextPrimary,
            )
        }
        Text(
            text = stringResource(Res.string.instructor_member_management_title),
            modifier = Modifier.semantics { heading() },
            style = appTypography().headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = InsColors.TextPrimary,
        )
        IconButton(
            onClick = onAdd,
            modifier = Modifier.align(Alignment.CenterEnd),
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_person_add),
                contentDescription = stringResource(Res.string.instructor_member_management_add),
                tint = InsColors.TextPrimary,
            )
        }
    }
}

@Composable
private fun MemberManagementListContent(
    totalCount: Int?,
    query: String,
    members: List<MemberUiModel>,
    sortOrder: MemberSortOption,
    emptyState: MemberListEmptyState?,
    onLongPress: (MemberUiModel) -> Unit = {},
    onAction: (MemberManagementAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding =
            PaddingValues(
                start = AppSpacing.screenPadding,
                top = AppSpacing.xxl,
                end = AppSpacing.screenPadding,
                bottom = AppSpacing.sectionGap,
            ),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
    ) {
        if (totalCount != null) {
            item {
                MemberTotal(
                    count = totalCount,
                )
            }
        }
        item {
            MemberSearchField(
                query = query,
                onQueryChanged = { onAction(MemberManagementAction.QueryChanged(it)) },
            )
        }
        item {
            MemberListHeader(
                sortOrder = sortOrder,
                onSortOrderChanged = { onAction(MemberManagementAction.SortOrderChanged(it)) },
            )
        }
        if (emptyState == null) {
            items(
                items = members,
                key = { member -> member.id.value },
            ) { member ->
                MemberCard(
                    member = member,
                    onLongPress = { onLongPress(member) },
                )
            }
        } else {
            item {
                MemberListEmpty(
                    state = emptyState,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun MemberTotal(count: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        Text(
            text = stringResource(Res.string.instructor_member_management_total_label),
            style = appTypography().titleMedium.copy(fontWeight = FontWeight.Bold),
            color = InsColors.TextSecondary,
        )
        Text(
            text = stringResource(Res.string.instructor_member_management_total_count, count),
            style = appTypography().headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = InsColors.TextPrimary,
        )
    }
}

@Composable
private fun MemberSearchField(
    query: String,
    onQueryChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    androidx.compose.foundation.text.BasicTextField(
        value = query,
        onValueChange = onQueryChanged,
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(min = AppSpacing.xxxl + AppSpacing.lg),
        singleLine = true,
        textStyle = appTypography().bodyLarge.copy(color = InsColors.TextPrimary),
        decorationBox = { innerTextField ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = AppShape.Card,
                color = InsColors.Surface,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_search),
                        contentDescription = stringResource(Res.string.instructor_member_management_search_label),
                        tint = InsColors.TextTertiary,
                        modifier = Modifier.size(AppSpacing.xxl),
                    )
                    Box(modifier = Modifier.weight(1f)) {
                        if (query.isEmpty()) {
                            Text(
                                text = stringResource(Res.string.instructor_member_management_search_placeholder),
                                style = appTypography().bodyLarge,
                                color = InsColors.TextTertiary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        innerTextField()
                    }
                }
            }
        },
    )
}

@Composable
private fun MemberListHeader(
    sortOrder: MemberSortOption,
    onSortOrderChanged: (MemberSortOption) -> Unit,
) {
    var isMenuExpanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(Res.string.instructor_member_management_list_title),
            style = appTypography().titleMedium,
            color = InsColors.TextPrimary,
        )
        Spacer(modifier = Modifier.weight(1f))
        Box {
            Row(
                modifier =
                    Modifier
                        .clickable(role = Role.Button) { isMenuExpanded = true }
                        .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            ) {
                Text(
                    text = sortOrder.labelResource(),
                    style = appTypography().titleMedium,
                    color = InsColors.TextSecondary,
                )
                Icon(
                    painter = painterResource(Res.drawable.ic_expand_more),
                    contentDescription = null,
                    tint = InsColors.TextSecondary,
                    modifier = Modifier.size(AppSpacing.xxl),
                )
            }
            DropdownMenu(
                expanded = isMenuExpanded,
                onDismissRequest = { isMenuExpanded = false },
            ) {
                memberSortOptions.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option.labelResource(),
                                color =
                                    if (option == sortOrder) {
                                        InsColors.Purple
                                    } else {
                                        InsColors.TextPrimary
                                    },
                            )
                        },
                        onClick = {
                            isMenuExpanded = false
                            if (option != sortOrder) onSortOrderChanged(option)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun MemberCard(
    member: MemberUiModel,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier =
            modifier
                .fillMaxWidth()
                .combinedClickable(
                    role = Role.Button,
                    onClick = {},
                    onLongClick = onLongPress,
                ),
        shape = AppShape.Card,
        color = InsColors.Surface,
    ) {
        Row(
            modifier = Modifier.padding(AppSpacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            ) {
                Text(
                    text = member.name,
                    style = appTypography().titleLarge,
                    color = InsColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = member.phoneNumber,
                    style = appTypography().bodyLarge,
                    color = InsColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun MemberActionDialog(
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(horizontal = AppSpacing.xxxl),
            shape = AppShape.Card,
            color = InsColors.Surface,
        ) {
            Column(
                modifier = Modifier.padding(AppSpacing.xxl),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
            ) {
                TextButton(onClick = onEdit, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(Res.string.instructor_member_detail_edit))
                }
                TextButton(onClick = onDelete, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(Res.string.instructor_member_detail_delete), color = InsColors.Red)
                }
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(Res.string.instructor_member_delete_cancel))
                }
            }
        }
    }
}

@Composable
private fun MemberDeleteDialog(
    memberName: String,
    state: MemberManagementActionState,
    onAction: (MemberManagementAction) -> Unit,
) {
    if (
        state !is MemberManagementActionState.Confirming &&
        state !is MemberManagementActionState.Submitting &&
        state !is MemberManagementActionState.Failed
    ) {
        return
    }
    val isSubmitting = state is MemberManagementActionState.Submitting
    val typedName =
        when (state) {
            is MemberManagementActionState.Confirming -> state.typedName
            is MemberManagementActionState.Failed -> state.typedName
            is MemberManagementActionState.Submitting -> state.typedName
            else -> ""
        }
    val inputError =
        (typedName.isNotBlank() && typedName != memberName) ||
            when (state) {
                is MemberManagementActionState.Confirming -> state.error == MemberManagementDeleteError.NAME_MISMATCH
                is MemberManagementActionState.Failed -> state.reason == MemberManagementDeleteError.NAME_MISMATCH
                else -> false
            }
    val errorText = stringResource(Res.string.instructor_member_delete_name_error)
    val paneTitle = stringResource(Res.string.instructor_member_delete_pane_title)
    Dialog(
        onDismissRequest = { if (!isSubmitting) onAction(MemberManagementAction.CancelDelete) },
        properties =
            DialogProperties(
                dismissOnBackPress = !isSubmitting,
                dismissOnClickOutside = !isSubmitting,
                usePlatformDefaultWidth = false,
            ),
    ) {
        Surface(
            modifier =
                Modifier.fillMaxWidth().padding(horizontal = AppSpacing.xxxl).semantics {
                    this.paneTitle =
                        paneTitle
                },
            shape = AppShape.Card,
            color = InsColors.Surface,
        ) {
            Column(
                modifier = Modifier.padding(AppSpacing.xxl),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
            ) {
                Text(
                    stringResource(Res.string.instructor_member_delete_title),
                    style = appTypography().titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = InsColors.TextPrimary,
                )
                Text(
                    stringResource(Res.string.instructor_member_delete_message),
                    style = appTypography().bodyMedium,
                    color = InsColors.TextSecondary,
                )
                Text(
                    memberName,
                    style = appTypography().titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = InsColors.TextPrimary,
                )
                OutlinedTextField(
                    value = typedName,
                    onValueChange = { onAction(MemberManagementAction.DeleteNameChanged(it)) },
                    enabled = !isSubmitting,
                    modifier = Modifier.fillMaxWidth().semantics { if (inputError) error(errorText) },
                    placeholder = { Text(stringResource(Res.string.instructor_member_delete_placeholder)) },
                    isError = inputError,
                    singleLine = true,
                    colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = InsColors.Primary,
                            unfocusedBorderColor = InsColors.Divider,
                            errorBorderColor = InsColors.Red,
                        ),
                )
                if (inputError) {
                    Text(
                        errorText,
                        modifier =
                            Modifier.semantics {
                                liveRegion = LiveRegionMode.Assertive
                            },
                        style = appTypography().bodySmall,
                        color = InsColors.Red,
                    )
                }
                if (state is MemberManagementActionState.Failed &&
                    state.reason != MemberManagementDeleteError.NAME_MISMATCH
                ) {
                    Text(
                        stringResource(Res.string.instructor_member_delete_failed),
                        modifier =
                            Modifier.semantics {
                                liveRegion =
                                    LiveRegionMode.Assertive
                            },
                        style = appTypography().bodySmall,
                        color = InsColors.Red,
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                    TextButton(onClick = {
                        onAction(MemberManagementAction.CancelDelete)
                    }, enabled = !isSubmitting, modifier = Modifier.weight(1f)) {
                        Text(stringResource(Res.string.instructor_member_delete_cancel))
                    }
                    Button(
                        onClick = { onAction(MemberManagementAction.ConfirmDelete) },
                        enabled = !isSubmitting && typedName == memberName,
                        modifier = Modifier.weight(1f),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = InsColors.Red,
                                contentColor = InsColors.White,
                            ),
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(AppSpacing.lg),
                                color = InsColors.White,
                                strokeWidth =
                                    AppSpacing.xs / 2,
                            )
                        } else {
                            Text(stringResource(Res.string.instructor_member_delete_confirm))
                        }
                    }
                }
                if (isSubmitting) {
                    Text(
                        stringResource(Res.string.instructor_member_delete_submitting),
                        modifier =
                            Modifier.semantics {
                                liveRegion =
                                    LiveRegionMode.Polite
                            },
                        style = appTypography().bodySmall,
                        color = InsColors.TextSecondary,
                    )
                }
            }
        }
    }
}

@Composable
private fun MemberDeleteSuccessDialog(onClose: () -> Unit) {
    val paneTitle = stringResource(Res.string.instructor_member_delete_success_title)
    Dialog(
        onDismissRequest = {},
        properties =
            DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false,
            ),
    ) {
        Surface(
            modifier =
                Modifier.fillMaxWidth().padding(horizontal = AppSpacing.xxxl).semantics {
                    this.paneTitle = paneTitle
                },
            shape = AppShape.Card,
            color = InsColors.Surface,
        ) {
            Column(
                modifier = Modifier.padding(AppSpacing.xxl),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = paneTitle,
                        modifier = Modifier.align(Alignment.Center),
                        style = appTypography().titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = InsColors.TextPrimary,
                    )
                    IconButton(onClick = onClose, modifier = Modifier.align(Alignment.TopEnd)) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_close),
                            contentDescription = stringResource(Res.string.phone_number_change_close),
                            tint = InsColors.TextSecondary,
                        )
                    }
                }
                Text(
                    text = stringResource(Res.string.instructor_member_delete_success),
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Assertive },
                    style = appTypography().bodyLarge,
                    color = InsColors.TextSecondary,
                )
                Button(
                    onClick = onClose,
                    modifier = Modifier.fillMaxWidth(),
                    shape = AppShape.Card,
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = InsColors.Primary,
                            contentColor = InsColors.White,
                        ),
                ) {
                    Text(stringResource(Res.string.instructor_member_registration_success_confirm))
                }
            }
        }
    }
}

@Composable
private fun MemberAvatarFallback(
    name: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = name.firstOrNull()?.toString().orEmpty(),
            style = appTypography().headlineSmall,
            color = InsColors.TextPrimary,
        )
    }
}

@Composable
private fun MemberManagementLoading(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .semantics { liveRegion = LiveRegionMode.Polite },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
        ) {
            CircularProgressIndicator(color = InsColors.Purple)
            Text(
                text = stringResource(Res.string.instructor_member_management_loading),
                style = appTypography().bodyMedium,
                color = InsColors.TextSecondary,
            )
        }
    }
}

@Composable
private fun MemberListEmpty(
    state: MemberListEmptyState,
    modifier: Modifier = Modifier,
) {
    val title =
        when (state) {
            MemberListEmptyState.Empty -> {
                stringResource(Res.string.instructor_member_management_empty_title)
            }

            MemberListEmptyState.SearchEmpty -> {
                stringResource(Res.string.instructor_member_management_search_empty_title)
            }
        }
    val description =
        when (state) {
            MemberListEmptyState.Empty -> {
                stringResource(Res.string.instructor_member_management_empty_description)
            }

            MemberListEmptyState.SearchEmpty -> {
                stringResource(Res.string.instructor_member_management_search_empty_description)
            }
        }
    Column(
        modifier = modifier.padding(vertical = AppSpacing.sectionGap),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Text(
            text = title,
            style = appTypography().titleLarge,
            color = InsColors.TextPrimary,
        )
        Text(
            text = description,
            style = appTypography().bodyMedium,
            color = InsColors.TextSecondary,
        )
    }
}

@Composable
private fun MemberManagementError(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .semantics { liveRegion = LiveRegionMode.Assertive },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = AppSpacing.screenPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Text(
                text = stringResource(Res.string.instructor_member_management_error_title),
                style = appTypography().titleLarge,
                color = InsColors.TextPrimary,
            )
            Text(
                text = stringResource(Res.string.instructor_member_management_error_description),
                style = appTypography().bodyMedium,
                color = InsColors.TextSecondary,
            )
            androidx.compose.material3.TextButton(onClick = onRetry) {
                Text(
                    text = stringResource(Res.string.instructor_member_management_retry),
                    color = InsColors.Purple,
                )
            }
        }
    }
}

private enum class MemberListEmptyState {
    Empty,
    SearchEmpty,
}

@Composable
private fun MemberSortOption.labelResource(): String =
    when (this) {
        MemberSortOption.RECENTLY_REGISTERED -> stringResource(Res.string.instructor_member_management_sort_recent)
        MemberSortOption.NAME_ASC -> stringResource(Res.string.instructor_member_management_sort_name)
    }

private val memberSortOptions =
    listOf(
        MemberSortOption.RECENTLY_REGISTERED,
        MemberSortOption.NAME_ASC,
    )

private val memberManagementPreviewPage =
    MemberListUiModel(
        totalCount = 128,
        members =
            listOf(
                MemberUiModel(InstructorMemberId("member-1"), "김민지", "010-****-5678", "김"),
                MemberUiModel(InstructorMemberId("member-2"), "이서윤", "010-****-5432", "이"),
                MemberUiModel(InstructorMemberId("member-3"), "박지수", "010-****-6666", "박"),
                MemberUiModel(InstructorMemberId("member-4"), "정유나", "010-****-1222", "정"),
            ),
    )

private val memberManagementLongNamePage =
    MemberListUiModel(
        totalCount = 1,
        members =
            listOf(
                MemberUiModel(
                    id = InstructorMemberId("member-long"),
                    name = "김민지 필라테스 스튜디오 대표 회원 이름이 아주 깁니다",
                    phoneNumber = "010-****-5678",
                    avatarFallback = "김",
                ),
            ),
    )

private val memberManagementManyMembersPage =
    MemberListUiModel(
        totalCount = 128,
        members =
            List(12) { index ->
                MemberUiModel(
                    id = InstructorMemberId("member-${index + 1}"),
                    name = listOf("김민지", "이서윤", "박지수", "정유나")[index % 4],
                    phoneNumber = "010-****-${(4000 + index).toString().padStart(4, '0')}",
                    avatarFallback = "회",
                )
            },
    )

@Preview(
    name = "Content · Instructor",
    group = "Screen/MemberManagement",
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun MemberManagementScreenPreview_Content() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        MemberManagementScreen(
            uiState = MemberManagementUiState.Content(memberManagementPreviewPage),
            onAction = {},
        )
    }
}

@Preview(
    name = "Content · Long member name",
    group = "Screen/MemberManagement",
    widthDp = 320,
    heightDp = 568,
    fontScale = 1.5f,
)
@Composable
private fun MemberManagementScreenPreview_LongName() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        MemberManagementScreen(
            uiState = MemberManagementUiState.Content(memberManagementLongNamePage),
            onAction = {},
        )
    }
}

@Preview(
    name = "Content · Many members",
    group = "Screen/MemberManagement",
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun MemberManagementScreenPreview_ManyMembers() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        MemberManagementScreen(
            uiState = MemberManagementUiState.Content(memberManagementManyMembersPage),
            onAction = {},
        )
    }
}

@Preview(
    name = "Loading · Instructor",
    group = "Screen/MemberManagement",
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun MemberManagementScreenPreview_Loading() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        MemberManagementScreen(
            uiState = MemberManagementUiState.Loading,
            onAction = {},
        )
    }
}

@Preview(
    name = "Empty · Instructor",
    group = "Screen/MemberManagement",
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun MemberManagementScreenPreview_Empty() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        MemberManagementScreen(
            uiState = MemberManagementUiState.Empty(),
            onAction = {},
        )
    }
}

@Preview(
    name = "Search empty · Instructor",
    group = "Screen/MemberManagement",
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun MemberManagementScreenPreview_SearchEmpty() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        MemberManagementScreen(
            uiState = MemberManagementUiState.SearchEmpty(query = "없는 회원"),
            onAction = {},
        )
    }
}

@Preview(
    name = "Error · Instructor",
    group = "Screen/MemberManagement",
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun MemberManagementScreenPreview_Error() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        MemberManagementScreen(
            uiState = MemberManagementUiState.Error(MemberManagementUiError.NETWORK),
            onAction = {},
        )
    }
}
