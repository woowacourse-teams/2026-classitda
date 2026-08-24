package com.classitda.feature.instructor.mypage

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
import classitda.shared.generated.resources.instructor_facility_detail_operating_hours
import classitda.shared.generated.resources.instructor_facility_management_add
import classitda.shared.generated.resources.instructor_facility_management_back
import classitda.shared.generated.resources.instructor_facility_management_count
import classitda.shared.generated.resources.instructor_facility_management_detail
import classitda.shared.generated.resources.instructor_facility_management_edit
import classitda.shared.generated.resources.instructor_facility_management_empty_description
import classitda.shared.generated.resources.instructor_facility_management_empty_title
import classitda.shared.generated.resources.instructor_facility_management_error_description
import classitda.shared.generated.resources.instructor_facility_management_error_title
import classitda.shared.generated.resources.instructor_facility_management_loading
import classitda.shared.generated.resources.instructor_facility_management_notice
import classitda.shared.generated.resources.instructor_facility_management_retry
import classitda.shared.generated.resources.instructor_facility_management_title
import coil3.compose.SubcomposeAsyncImage
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.appTypography
import com.classitda.domain.model.instructor.mypage.InstructorFacilityId
import com.classitda.domain.model.instructor.mypage.ManagedFacility
import com.classitda.domain.repository.instructor.mypage.FacilityList
import com.classitda.feature.instructor.mypage.contract.FacilityManagementAction
import com.classitda.feature.instructor.mypage.contract.FacilityManagementUiState
import com.classitda.feature.instructor.mypage.contract.FacilitySuccessNotice
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun FacilityManagementScreen(
    uiState: FacilityManagementUiState,
    onAction: (FacilityManagementAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = InsColors.Background,
        topBar = {
            FacilityManagementTopBar(
                onBack = { onAction(FacilityManagementAction.Back) },
            )
        },
    ) { innerPadding ->
        when (uiState) {
            FacilityManagementUiState.Loading -> {
                FacilityManagementLoading(Modifier.padding(innerPadding))
            }

            FacilityManagementUiState.Empty -> {
                FacilityManagementEmpty(
                    onAdd = { onAction(FacilityManagementAction.OpenFacilityRegistration) },
                    modifier = Modifier.padding(innerPadding),
                )
            }

            is FacilityManagementUiState.Content -> {
                FacilityManagementContent(
                    page = uiState.page,
                    showSuccessNotice = uiState.successNotice == FacilitySuccessNotice.Visible,
                    onAction = onAction,
                    modifier = Modifier.padding(innerPadding),
                )
            }

            is FacilityManagementUiState.Error -> {
                FacilityManagementError(
                    onRetry = { onAction(FacilityManagementAction.Retry) },
                    onAdd = { onAction(FacilityManagementAction.OpenFacilityRegistration) },
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }
    }
}

@Composable
private fun FacilityManagementTopBar(onBack: () -> Unit) {
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
                contentDescription = stringResource(Res.string.instructor_facility_management_back),
                tint = InsColors.TextPrimary,
            )
        }
        Text(
            text = stringResource(Res.string.instructor_facility_management_title),
            modifier = Modifier.semantics { heading() },
            style = appTypography().headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = InsColors.TextPrimary,
        )
    }
}

@Composable
private fun FacilityManagementContent(
    page: FacilityList,
    showSuccessNotice: Boolean,
    onAction: (FacilityManagementAction) -> Unit,
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
            item { FacilitySuccessBanner() }
        }
        item { FacilityCount(count = page.totalCount) }
        if (page.facilities.isEmpty()) {
            item {
                FacilityEmptyMessage(
                    onAdd = { onAction(FacilityManagementAction.OpenFacilityRegistration) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        } else {
            items(
                items = page.facilities,
                key = { facility -> facility.id.value },
            ) { facility ->
                ManagedFacilityCard(
                    facility = facility,
                    onEdit = { onAction(FacilityManagementAction.EditFacility(facility.id)) },
                    onDetail = { onAction(FacilityManagementAction.OpenFacilityDetail(facility.id)) },
                )
            }
        }
        item {
            FacilityAddButton(
                onClick = { onAction(FacilityManagementAction.OpenFacilityRegistration) },
            )
        }
    }
}

@Composable
private fun FacilitySuccessBanner(modifier: Modifier = Modifier) {
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
                text = stringResource(Res.string.instructor_facility_management_notice),
                style = appTypography().headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = InsColors.Purple,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun FacilityCount(count: Int) {
    Surface(
        shape = AppShape.Pill,
        color = InsColors.Primary,
    ) {
        Text(
            text = stringResource(Res.string.instructor_facility_management_count, count),
            modifier = Modifier.padding(horizontal = AppSpacing.xxl, vertical = AppSpacing.md),
            style = appTypography().titleLarge.copy(fontWeight = FontWeight.Bold),
            color = InsColors.White,
        )
    }
}

@Composable
private fun ManagedFacilityCard(
    facility: ManagedFacility,
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
                FacilityImage(
                    reference = facility.representativeImageReference,
                    modifier = Modifier.size(AppSpacing.xxxl * 3),
                )
                Spacer(modifier = Modifier.width(AppSpacing.lg))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                ) {
                    Text(
                        text = facility.name,
                        style = appTypography().titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = InsColors.TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = facility.address,
                        style = appTypography().bodyLarge,
                        color = InsColors.TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    val operatingHoursLabel =
                        stringResource(Res.string.instructor_facility_detail_operating_hours)
                    Text(
                        text = "$operatingHoursLabel ${formatFacilityOperatingHours(facility)}",
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
                        text = stringResource(Res.string.instructor_facility_management_edit),
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
                        text = stringResource(Res.string.instructor_facility_management_detail),
                        style = appTypography().bodyLarge,
                        color = InsColors.TextSecondary,
                    )
                }
            }
        }
    }
}

private fun formatFacilityOperatingHours(facility: ManagedFacility): String =
    listOf(facility.openingTime, facility.closingTime)
        .filter(String::isNotBlank)
        .joinToString(" - ")
        .ifBlank { "-" }

@Composable
private fun FacilityImage(
    reference: String?,
    modifier: Modifier = Modifier,
) {
    val imageModifier = modifier.clip(AppShape.Card)
    if (reference.isNullOrBlank()) {
        FacilityImageFallback(imageModifier)
    } else {
        SubcomposeAsyncImage(
            model = reference,
            contentDescription = null,
            modifier = imageModifier,
            loading = { FacilityImageFallback(Modifier.fillMaxSize()) },
            error = { FacilityImageFallback(Modifier.fillMaxSize()) },
        )
    }
}

@Composable
private fun FacilityImageFallback(modifier: Modifier = Modifier) {
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
private fun FacilityAddButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().heightIn(min = AppSpacing.xxxl + AppSpacing.lg),
        shape = AppShape.Card,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = InsColors.Primary,
                contentColor = InsColors.White,
            ),
    ) {
        Text(
            text = stringResource(Res.string.instructor_facility_management_add),
            style = appTypography().bodyLarge.copy(fontWeight = FontWeight.Bold),
        )
    }
}

@Composable
private fun FacilityManagementEmpty(
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(AppSpacing.screenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        FacilityEmptyMessage(onAdd = onAdd)
    }
}

@Composable
private fun FacilityEmptyMessage(
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        Text(
            text = stringResource(Res.string.instructor_facility_management_empty_title),
            style = appTypography().titleLarge.copy(fontWeight = FontWeight.Bold),
            color = InsColors.TextPrimary,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(Res.string.instructor_facility_management_empty_description),
            style = appTypography().bodyMedium,
            color = InsColors.TextSecondary,
            textAlign = TextAlign.Center,
        )
        FacilityAddButton(onClick = onAdd)
    }
}

@Composable
private fun FacilityManagementLoading(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().semantics { liveRegion = LiveRegionMode.Polite },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(color = InsColors.Purple)
        Text(
            text = stringResource(Res.string.instructor_facility_management_loading),
            modifier = Modifier.padding(top = AppSpacing.lg),
            style = appTypography().bodyMedium,
            color = InsColors.TextSecondary,
        )
    }
}

@Composable
private fun FacilityManagementError(
    onRetry: () -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(AppSpacing.screenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(Res.string.instructor_facility_management_error_title),
            style = appTypography().titleLarge.copy(fontWeight = FontWeight.Bold),
            color = InsColors.TextPrimary,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(Res.string.instructor_facility_management_error_description),
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
                Text(stringResource(Res.string.instructor_facility_management_retry))
            }
            TextButton(onClick = onAdd) {
                Text(stringResource(Res.string.instructor_facility_management_add))
            }
        }
    }
}

private val facilityManagementFixture =
    FacilityList(
        totalCount = 2,
        facilities =
            listOf(
                ManagedFacility(
                    id = InstructorFacilityId("facility-1"),
                    name = "더 에이치 휘트니스 강남점",
                    address = "서울 강남구 테헤란로 123",
                ),
                ManagedFacility(
                    id = InstructorFacilityId("facility-2"),
                    name = "린 필라테스 스튜디오",
                    address = "서울 강남구 압구정로 45",
                ),
            ),
    )

@Preview(
    name = "Content · Success visible · Instructor",
    group = "Screen/FacilityManagement",
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun FacilityManagementScreenPreview_Content() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        FacilityManagementScreen(
            uiState =
                FacilityManagementUiState.Content(
                    page = facilityManagementFixture,
                    successNotice = FacilitySuccessNotice.Visible,
                ),
            onAction = {},
        )
    }
}

@Preview(
    name = "Content · Hidden · Long values · Instructor",
    group = "Screen/FacilityManagement",
    widthDp = 320,
    heightDp = 720,
    fontScale = 1.3f,
)
@Composable
private fun FacilityManagementScreenPreview_LongValues() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        FacilityManagementScreen(
            uiState =
                FacilityManagementUiState.Content(
                    page =
                        FacilityList(
                            totalCount = 1,
                            facilities =
                                listOf(
                                    ManagedFacility(
                                        id = InstructorFacilityId("facility-long"),
                                        name = "정말 긴 시설 이름이 들어와도 카드 너비를 벗어나지 않는 테스트 시설",
                                        address = "서울특별시 강남구 테헤란로를 따라 이어지는 아주 긴 시설 주소 테스트",
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
    group = "Screen/FacilityManagement",
)
@Composable
private fun FacilityManagementScreenPreview_Loading() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        FacilityManagementScreen(uiState = FacilityManagementUiState.Loading, onAction = {})
    }
}

@Preview(
    name = "Empty · Instructor",
    group = "Screen/FacilityManagement",
)
@Composable
private fun FacilityManagementScreenPreview_Empty() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        FacilityManagementScreen(uiState = FacilityManagementUiState.Empty, onAction = {})
    }
}

@Preview(
    name = "Error · Instructor",
    group = "Screen/FacilityManagement",
)
@Composable
private fun FacilityManagementScreenPreview_Error() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        FacilityManagementScreen(
            uiState =
                FacilityManagementUiState.Error(
                    reason = com.classitda.feature.instructor.mypage.contract.FacilityManagementUiError.NETWORK,
                ),
            onAction = {},
        )
    }
}
