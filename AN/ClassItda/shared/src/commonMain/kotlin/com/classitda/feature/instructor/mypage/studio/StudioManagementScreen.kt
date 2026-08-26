package com.classitda.feature.instructor.mypage.studio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.ic_arrow_back
import classitda.shared.generated.resources.ic_check_circle
import classitda.shared.generated.resources.ic_edit
import classitda.shared.generated.resources.ic_home
import classitda.shared.generated.resources.instructor_studio_detail_operating_hours
import classitda.shared.generated.resources.instructor_studio_management_add
import classitda.shared.generated.resources.instructor_studio_management_back
import classitda.shared.generated.resources.instructor_studio_management_count
import classitda.shared.generated.resources.instructor_studio_management_detail
import classitda.shared.generated.resources.instructor_studio_management_edit
import classitda.shared.generated.resources.instructor_studio_management_empty_description
import classitda.shared.generated.resources.instructor_studio_management_empty_title
import classitda.shared.generated.resources.instructor_studio_management_error_description
import classitda.shared.generated.resources.instructor_studio_management_error_title
import classitda.shared.generated.resources.instructor_studio_management_loading
import classitda.shared.generated.resources.instructor_studio_management_notice
import classitda.shared.generated.resources.instructor_studio_management_retry
import classitda.shared.generated.resources.instructor_studio_management_title
import coil3.compose.SubcomposeAsyncImage
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.appTypography
import com.classitda.domain.model.instructor.mypage.InstructorStudioId
import com.classitda.feature.instructor.mypage.contract.StudioListUiModel
import com.classitda.feature.instructor.mypage.contract.StudioManagementAction
import com.classitda.feature.instructor.mypage.contract.StudioManagementUiState
import com.classitda.feature.instructor.mypage.contract.StudioSuccessNotice
import com.classitda.feature.instructor.mypage.contract.StudioUiModel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun StudioManagementScreen(
    uiState: StudioManagementUiState,
    onAction: (StudioManagementAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = InsColors.Background,
        topBar = {
            StudioManagementTopBar(
                onBack = { onAction(StudioManagementAction.Back) },
            )
        },
        bottomBar = {
            if (uiState !is StudioManagementUiState.Loading) {
                StudioManagementBottomBar(
                    onAdd = { onAction(StudioManagementAction.OpenStudioRegistration) },
                )
            }
        },
    ) { innerPadding ->
        when (uiState) {
            StudioManagementUiState.Loading -> {
                StudioManagementLoading(Modifier.padding(innerPadding))
            }

            StudioManagementUiState.Empty -> {
                StudioManagementEmpty(
                    modifier = Modifier.padding(innerPadding),
                )
            }

            is StudioManagementUiState.Content -> {
                StudioManagementContent(
                    page = uiState.page,
                    showSuccessNotice = uiState.successNotice == StudioSuccessNotice.Visible,
                    onAction = onAction,
                    modifier = Modifier.padding(innerPadding),
                )
            }

            is StudioManagementUiState.Error -> {
                StudioManagementError(
                    onRetry = { onAction(StudioManagementAction.Retry) },
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }
    }
}

@Composable
private fun StudioManagementTopBar(onBack: () -> Unit) {
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
                contentDescription = stringResource(Res.string.instructor_studio_management_back),
                tint = InsColors.TextPrimary,
            )
        }
        Text(
            text = stringResource(Res.string.instructor_studio_management_title),
            modifier = Modifier.semantics { heading() },
            style = appTypography().headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = InsColors.TextPrimary,
        )
    }
}

@Composable
private fun StudioManagementContent(
    page: StudioListUiModel,
    showSuccessNotice: Boolean,
    onAction: (StudioManagementAction) -> Unit,
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
        if (showSuccessNotice) {
            item { StudioSuccessBanner() }
        }
        item { StudioCount(count = page.totalCount) }
        if (page.studios.isEmpty()) {
            item {
                StudioEmptyMessage(modifier = Modifier.fillMaxWidth())
            }
        } else {
            items(
                items = page.studios,
                key = { studio -> studio.id.value },
            ) { studio ->
                StudioCard(
                    studio = studio,
                    onEdit = { onAction(StudioManagementAction.EditStudio(studio.id)) },
                    onDetail = { onAction(StudioManagementAction.OpenStudioDetail(studio.id)) },
                )
            }
        }
    }
}

@Composable
private fun StudioSuccessBanner(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite },
        shape = AppShape.Card,
        color = InsColors.PurpleLight,
    ) {
        Row(
            modifier = Modifier.padding(AppSpacing.xxl),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.lg),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(AppSpacing.xxxl)
                        .clip(CircleShape)
                        .background(InsColors.Purple),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_check_circle),
                    contentDescription = null,
                    tint = InsColors.White,
                    modifier = Modifier.size(AppSpacing.xxl),
                )
            }
            Text(
                text = stringResource(Res.string.instructor_studio_management_notice),
                style = appTypography().titleMedium.copy(fontWeight = FontWeight.Bold),
                color = InsColors.Purple,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun StudioCount(count: Int) {
    Surface(
        shape = AppShape.Pill,
        color = InsColors.Primary,
    ) {
        Text(
            text = stringResource(Res.string.instructor_studio_management_count, count),
            modifier = Modifier.padding(horizontal = AppSpacing.xxl, vertical = AppSpacing.md),
            style = appTypography().titleMedium.copy(fontWeight = FontWeight.Bold),
            color = InsColors.White,
        )
    }
}

@Composable
private fun StudioCard(
    studio: StudioUiModel,
    onEdit: () -> Unit,
    onDetail: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = AppShape.Card,
        color = InsColors.Surface,
    ) {
        Column {
            Row(
                modifier = Modifier.padding(AppSpacing.xl),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StudioImage(
                    reference = studio.image?.previewReference,
                    modifier = Modifier.size(AppSpacing.xxxl * 3),
                )
                Spacer(modifier = Modifier.width(AppSpacing.lg))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                ) {
                    Text(
                        text = studio.name,
                        style = appTypography().titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = InsColors.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = studio.address.displayAddress,
                        style = appTypography().bodyLarge,
                        color = InsColors.TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    val operatingHoursLabel =
                        stringResource(Res.string.instructor_studio_detail_operating_hours)
                    Text(
                        text = "$operatingHoursLabel ${formatStudioOperatingHours(studio)}",
                        style = appTypography().bodyMedium,
                        color = InsColors.TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().background(InsColors.SurfaceVariant),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f).heightIn(min = AppSpacing.xxxl + AppSpacing.lg),
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_edit),
                        contentDescription = null,
                        tint = InsColors.TextSecondary,
                        modifier = Modifier.size(AppSpacing.lg),
                    )
                    Text(
                        text = stringResource(Res.string.instructor_studio_management_edit),
                        modifier = Modifier.padding(start = AppSpacing.xs),
                        style = appTypography().bodyLarge,
                        color = InsColors.TextSecondary,
                    )
                }
                Box(
                    modifier = Modifier.size(width = AppSpacing.xs / 2, height = AppSpacing.xxl),
                    contentAlignment = Alignment.Center,
                ) {
                    Surface(color = InsColors.Divider, modifier = Modifier.fillMaxSize()) {}
                }
                TextButton(
                    onClick = onDetail,
                    modifier = Modifier.weight(1f).heightIn(min = AppSpacing.xxxl + AppSpacing.lg),
                ) {
                    Text(
                        text = stringResource(Res.string.instructor_studio_management_detail),
                        style = appTypography().bodyLarge,
                        color = InsColors.TextSecondary,
                    )
                }
            }
        }
    }
}

private fun formatStudioOperatingHours(studio: StudioUiModel): String =
    listOf(studio.openingTime, studio.closingTime)
        .filter(String::isNotBlank)
        .joinToString(" - ")
        .ifBlank { "-" }

@Composable
private fun StudioImage(
    reference: String?,
    modifier: Modifier = Modifier,
) {
    val imageModifier = modifier.clip(AppShape.Card)
    if (reference.isNullOrBlank()) {
        StudioImageFallback(imageModifier)
    } else {
        SubcomposeAsyncImage(
            model = reference,
            contentDescription = null,
            modifier = imageModifier,
            loading = { StudioImageFallback(Modifier.fillMaxSize()) },
            error = { StudioImageFallback(Modifier.fillMaxSize()) },
        )
    }
}

@Composable
private fun StudioImageFallback(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(InsColors.Gray200),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_home),
            contentDescription = null,
            tint = InsColors.TextTertiary,
            modifier = Modifier.size(AppSpacing.xxxl),
        )
    }
}

@Composable
private fun StudioManagementBottomBar(onAdd: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().navigationBarsPadding(),
        color = InsColors.Background,
    ) {
        StudioAddButton(
            onClick = onAdd,
            modifier = Modifier.padding(horizontal = AppSpacing.screenPadding, vertical = AppSpacing.lg),
        )
    }
}

@Composable
private fun StudioAddButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().heightIn(min = AppSpacing.xxxl + AppSpacing.lg),
        shape = AppShape.Card,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = InsColors.Primary,
                contentColor = InsColors.White,
            ),
    ) {
        Text(
            text = stringResource(Res.string.instructor_studio_management_add),
            style = appTypography().bodyLarge.copy(fontWeight = FontWeight.Bold),
        )
    }
}

@Composable
private fun StudioManagementEmpty(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(AppSpacing.screenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        StudioEmptyMessage()
    }
}

@Composable
private fun StudioEmptyMessage(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        Text(
            text = stringResource(Res.string.instructor_studio_management_empty_title),
            style = appTypography().titleLarge.copy(fontWeight = FontWeight.Bold),
            color = InsColors.TextPrimary,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(Res.string.instructor_studio_management_empty_description),
            style = appTypography().bodyMedium,
            color = InsColors.TextSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun StudioManagementLoading(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().semantics { liveRegion = LiveRegionMode.Polite },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(color = InsColors.Purple)
        Text(
            text = stringResource(Res.string.instructor_studio_management_loading),
            modifier = Modifier.padding(top = AppSpacing.lg),
            style = appTypography().bodyMedium,
            color = InsColors.TextSecondary,
        )
    }
}

@Composable
private fun StudioManagementError(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(AppSpacing.screenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(Res.string.instructor_studio_management_error_title),
            style = appTypography().titleLarge.copy(fontWeight = FontWeight.Bold),
            color = InsColors.TextPrimary,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(Res.string.instructor_studio_management_error_description),
            modifier = Modifier.padding(top = AppSpacing.sm),
            style = appTypography().bodyMedium,
            color = InsColors.TextSecondary,
            textAlign = TextAlign.Center,
        )
        Row(
            modifier = Modifier.padding(top = AppSpacing.lg),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            TextButton(onClick = onRetry) {
                Text(stringResource(Res.string.instructor_studio_management_retry))
            }
        }
    }
}

private val studioManagementFixture =
    StudioListUiModel(
        totalCount = 2,
        studios =
            listOf(
                StudioUiModel(
                    id = InstructorStudioId("studio-1"),
                    name = "더 에이치 휘트니스 강남점",
                    address =
                        com.classitda.domain.model.instructor.mypage.StudioAddress(
                            roadAddress = "서울 강남구 테헤란로 123",
                        ),
                    phoneNumber = "02-1234-5678",
                ),
                StudioUiModel(
                    id = InstructorStudioId("studio-2"),
                    name = "린 필라테스 스튜디오",
                    address =
                        com.classitda.domain.model.instructor.mypage.StudioAddress(
                            roadAddress = "서울 강남구 압구정로 45",
                        ),
                    phoneNumber = "02-9876-5432",
                ),
            ),
    )

@Preview(
    name = "Content · Success visible · Instructor",
    group = "Screen/StudioManagement",
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun StudioManagementScreenPreview_Content() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        StudioManagementScreen(
            uiState =
                StudioManagementUiState.Content(
                    page = studioManagementFixture,
                    successNotice = StudioSuccessNotice.Visible,
                ),
            onAction = {},
        )
    }
}

@Preview(
    name = "Content · Hidden · Long values · Instructor",
    group = "Screen/StudioManagement",
    widthDp = 320,
    heightDp = 720,
    fontScale = 1.3f,
)
@Composable
private fun StudioManagementScreenPreview_LongValues() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        StudioManagementScreen(
            uiState =
                StudioManagementUiState.Content(
                    page =
                        StudioListUiModel(
                            totalCount = 1,
                            studios =
                                listOf(
                                    StudioUiModel(
                                        id = InstructorStudioId("studio-long"),
                                        name = "정말 긴 시설 이름이 들어와도 카드 너비를 벗어나지 않는 테스트 시설",
                                        address =
                                            com.classitda.domain.model.instructor.mypage.StudioAddress(
                                                roadAddress =
                                                    "서울특별시 강남구 테헤란로를 따라 이어지는 아주 긴 시설 주소 테스트",
                                            ),
                                        phoneNumber = "02-1234-5678",
                                    ),
                                ),
                        ),
                ),
            onAction = {},
        )
    }
}

@Preview(
    name = "Loading · Instructor",
    group = "Screen/StudioManagement",
)
@Composable
private fun StudioManagementScreenPreview_Loading() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        StudioManagementScreen(uiState = StudioManagementUiState.Loading, onAction = {})
    }
}

@Preview(
    name = "Empty · Instructor",
    group = "Screen/StudioManagement",
)
@Composable
private fun StudioManagementScreenPreview_Empty() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        StudioManagementScreen(uiState = StudioManagementUiState.Empty, onAction = {})
    }
}

@Preview(
    name = "Error · Instructor",
    group = "Screen/StudioManagement",
)
@Composable
private fun StudioManagementScreenPreview_Error() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        StudioManagementScreen(
            uiState =
                StudioManagementUiState.Error(
                    reason = com.classitda.feature.instructor.mypage.contract.StudioManagementUiError.NETWORK,
                ),
            onAction = {},
        )
    }
}
