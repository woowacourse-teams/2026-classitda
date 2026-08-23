package com.classitda.feature.instructor.mypage

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.ic_arrow_back
import classitda.shared.generated.resources.ic_arrow_forward
import classitda.shared.generated.resources.ic_expand_more
import classitda.shared.generated.resources.ic_person_add
import classitda.shared.generated.resources.ic_search
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
import classitda.shared.generated.resources.instructor_member_management_sort_recent
import classitda.shared.generated.resources.instructor_member_management_title
import classitda.shared.generated.resources.instructor_member_management_total_count
import classitda.shared.generated.resources.instructor_member_management_total_label
import coil3.compose.SubcomposeAsyncImage
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.appTypography
import com.classitda.domain.model.instructor.mypage.InstructorMemberId
import com.classitda.domain.model.instructor.mypage.ManagedMember
import com.classitda.domain.model.instructor.mypage.MemberListPage
import com.classitda.domain.model.instructor.mypage.MemberSortOrder
import com.classitda.feature.instructor.mypage.contract.MemberManagementAction
import com.classitda.feature.instructor.mypage.contract.MemberManagementUiError
import com.classitda.feature.instructor.mypage.contract.MemberManagementUiState
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun MemberManagementScreen(
    uiState: MemberManagementUiState,
    onAction: (MemberManagementAction) -> Unit,
    modifier: Modifier = Modifier,
) {
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

            MemberManagementUiState.Empty -> {
                MemberManagementListContent(
                    totalCount = 0,
                    query = "",
                    members = emptyList(),
                    sortOrder = MemberSortOrder.RECENTLY_REGISTERED,
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
                    onAction = onAction,
                    modifier = Modifier.padding(innerPadding),
                )
            }

            is MemberManagementUiState.SearchEmpty -> {
                MemberManagementListContent(
                    totalCount = null,
                    query = uiState.query,
                    members = emptyList(),
                    sortOrder = MemberSortOrder.RECENTLY_REGISTERED,
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
}

@Composable
private fun MemberManagementTopBar(
    onBack: () -> Unit,
    onAdd: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
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
    members: List<ManagedMember>,
    sortOrder: MemberSortOrder,
    emptyState: MemberListEmptyState?,
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
            MemberListHeader(sortOrder = sortOrder)
        }
        if (emptyState == null) {
            items(
                items = members,
                key = { member -> member.id.value },
            ) { member ->
                ManagedMemberCard(
                    member = member,
                    onClick = { onAction(MemberManagementAction.OpenMember(member.id)) },
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
private fun MemberListHeader(sortOrder: MemberSortOrder) {
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
}

@Composable
private fun ManagedMemberCard(
    member: ManagedMember,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(role = Role.Button, onClick = onClick),
        shape = AppShape.Card,
        color = InsColors.Surface,
    ) {
        Row(
            modifier = Modifier.padding(AppSpacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MemberAvatar(member = member)
            Spacer(modifier = Modifier.width(AppSpacing.lg))
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
                    text = maskPhoneNumber(member.phoneNumber),
                    style = appTypography().bodyLarge,
                    color = InsColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Icon(
                painter = painterResource(Res.drawable.ic_arrow_forward),
                contentDescription = null,
                tint = InsColors.TextTertiary,
                modifier = Modifier.size(AppSpacing.xxl),
            )
        }
    }
}

@Composable
private fun MemberAvatar(
    member: ManagedMember,
    modifier: Modifier = Modifier,
) {
    val avatarModifier =
        modifier
            .size(AppSpacing.xxxl + AppSpacing.lg)
            .clip(CircleShape)
            .background(InsColors.PurpleLight)

    if (member.profileImageUrl.isNullOrBlank()) {
        MemberAvatarFallback(
            name = member.name,
            modifier = avatarModifier,
        )
    } else {
        SubcomposeAsyncImage(
            model = member.profileImageUrl,
            contentDescription = null,
            modifier = avatarModifier,
            loading = {
                MemberAvatarFallback(name = member.name)
            },
            error = {
                MemberAvatarFallback(name = member.name)
            },
        )
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
private fun MemberSortOrder.labelResource(): String =
    when (this) {
        MemberSortOrder.RECENTLY_REGISTERED -> stringResource(Res.string.instructor_member_management_sort_recent)
    }

private fun maskPhoneNumber(phoneNumber: String): String {
    val digits = phoneNumber.filter { it in '0'..'9' }
    return when {
        digits.length == 11 -> "${digits.take(3)}-${digits.substring(3, 5)}**-${digits.takeLast(4)}"
        digits.length == 10 -> "${digits.take(3)}-${digits.substring(3, 4)}**-${digits.takeLast(4)}"
        digits.length > 7 -> "${digits.take(3)}-****-${digits.takeLast(4)}"
        else -> "****"
    }
}

private val memberManagementPreviewPage =
    MemberListPage(
        totalCount = 128,
        members =
            listOf(
                ManagedMember(InstructorMemberId("member-1"), "김민지", "01012345678"),
                ManagedMember(InstructorMemberId("member-2"), "이서윤", "01098765432"),
                ManagedMember(InstructorMemberId("member-3"), "박지수", "01055556666"),
                ManagedMember(InstructorMemberId("member-4"), "정유나", "01011112222"),
            ),
    )

private val memberManagementLongNamePage =
    MemberListPage(
        totalCount = 1,
        members =
            listOf(
                ManagedMember(
                    id = InstructorMemberId("member-long"),
                    name = "김민지 필라테스 스튜디오 대표 회원 이름이 아주 깁니다",
                    phoneNumber = "01012345678",
                ),
            ),
    )

private val memberManagementManyMembersPage =
    MemberListPage(
        totalCount = 128,
        members =
            List(12) { index ->
                ManagedMember(
                    id = InstructorMemberId("member-${index + 1}"),
                    name = listOf("김민지", "이서윤", "박지수", "정유나")[index % 4],
                    phoneNumber = "010${12340000 + index}".take(11),
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
            uiState = MemberManagementUiState.Empty,
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
