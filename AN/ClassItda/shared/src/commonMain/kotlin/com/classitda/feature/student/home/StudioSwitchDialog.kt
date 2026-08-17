package com.classitda.feature.student.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.ic_instructor
import classitda.shared.generated.resources.ic_person
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.StuColors
import com.classitda.feature.student.home.component.PrimaryTextButton
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioSwitchDialog(
    instructorModeOptions: List<InstructorStudioOptionUiModel>,
    memberModeOptions: List<MemberStudioOptionUiModel>,
    onOptionClick: (String) -> Unit,
    onDismissRequest: () -> Unit,
    onConfirmClick: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    isConfirmEnabled: Boolean = false,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = StuColors.Surface,
        modifier = modifier,
    ) {
        StudioSwitchDialogContent(
            instructorModeOptions = instructorModeOptions,
            memberModeOptions = memberModeOptions,
            onOptionClick = onOptionClick,
            onConfirmClick = onConfirmClick,
            isConfirmEnabled = isConfirmEnabled,
        )
    }
}

@Composable
private fun StudioSwitchDialogContent(
    instructorModeOptions: List<InstructorStudioOptionUiModel>,
    memberModeOptions: List<MemberStudioOptionUiModel>,
    onOptionClick: (String) -> Unit,
    onConfirmClick: () -> Unit,
    modifier: Modifier = Modifier,
    isConfirmEnabled: Boolean = false,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.screenPadding)
                .navigationBarsPadding()
                .padding(bottom = AppSpacing.xl),
    ) {
        Column(
            modifier =
                Modifier
                    .weight(1f, fill = false)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = "시설 전환",
                style =
                    MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                color = StuColors.TextPrimary,
            )

            Spacer(Modifier.height(AppSpacing.sm))

            Text(
                text = "전환할 시설을 선택해주세요.",
                style = MaterialTheme.typography.bodyMedium,
                color = StuColors.TextSecondary,
            )

            Spacer(Modifier.height(AppSpacing.sectionGap))

            if (instructorModeOptions.isNotEmpty()) {
                Text(
                    text = "강사모드",
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                    color = StuColors.TextTertiary,
                )
                Spacer(Modifier.height(AppSpacing.sm))

                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                    instructorModeOptions.forEach { option ->
                        InstructorStudioCard(
                            studioName = option.studioName,
                            isSelected = option.isSelected,
                            onClick = { onOptionClick(option.id) },
                            isLead = option.isLead,
                        )
                    }
                }
            }

            if (instructorModeOptions.isNotEmpty() && memberModeOptions.isNotEmpty()) {
                Spacer(Modifier.height(AppSpacing.xl))
                HorizontalDivider(color = StuColors.Divider, thickness = 1.dp)
                Spacer(Modifier.height(AppSpacing.sectionGap))
            }

            if (memberModeOptions.isNotEmpty()) {
                Text(
                    text = "회원모드",
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                    color = StuColors.TextTertiary,
                )
                Spacer(Modifier.height(AppSpacing.sm))

                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                    memberModeOptions.forEach { option ->
                        MemberStudioCard(
                            studioName = option.studioName,
                            isSelected = option.isSelected,
                            onClick = { onOptionClick(option.id) },
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(AppSpacing.sectionGap))

        PrimaryTextButton(
            content = "확인",
            onClick = onConfirmClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = isConfirmEnabled,
            contentPadding = PaddingValues(vertical = 16.dp),
        )
    }
}

@Composable
private fun MemberStudioCard(
    studioName: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(AppShape.Card)
                .background(Color.Transparent)
                .border(
                    width = 1.dp,
                    color = if (isSelected) StuColors.Primary else StuColors.Divider,
                    shape = AppShape.Card,
                ).clickable(onClick = onClick)
                .padding(AppSpacing.cardPadding),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_person),
            contentDescription = "회원 아이콘",
            tint = if (isSelected) StuColors.Primary else StuColors.TextSecondary,
            modifier = Modifier.size(24.dp),
        )

        Text(
            text = studioName,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = if (isSelected) StuColors.Primary else StuColors.TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun InstructorStudioCard(
    studioName: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    isLead: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(AppShape.Card)
                .background(Color.Transparent)
                .border(
                    width = 1.dp,
                    color = if (isSelected) StuColors.Primary else StuColors.Divider,
                    shape = AppShape.Card,
                ).clickable(onClick = onClick)
                .padding(AppSpacing.cardPadding),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_instructor),
            contentDescription = "강사 아이콘",
            tint = if (isSelected) StuColors.Primary else StuColors.TextSecondary,
            modifier = Modifier.size(24.dp),
        )

        Text(
            text = studioName,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = if (isSelected) StuColors.Primary else StuColors.TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Box(
            modifier =
                Modifier
                    .clip(AppShape.Pill)
                    .background(if (isLead) StuColors.Primary else StuColors.SurfaceVariant)
                    .padding(vertical = AppSpacing.xs, horizontal = AppSpacing.sm),
        ) {
            Text(
                text = if (isLead) "대표 강사" else "일반 강사",
                color = if (isLead) StuColors.White else StuColors.TextSecondary,
                style = MaterialTheme.typography.labelSmall.copy(),
            )
        }
    }
}

@Composable
@Preview(backgroundColor = 0xFFFFFFFF, showBackground = true)
private fun StudioSwitchDialogContentPreview() {
    AppTheme {
        StudioSwitchDialogContent(
            instructorModeOptions =
                listOf(
                    InstructorStudioOptionUiModel(
                        id = "instructor-1",
                        studioName = "코코 필라테스 & 피트니스코 영등점",
                        isSelected = true,
                        isLead = true,
                    ),
                    InstructorStudioOptionUiModel(
                        id = "instructor-2",
                        studioName = "필라테스 무브먼트 왕십리",
                        isLead = false,
                    ),
                ),
            memberModeOptions =

                listOf(
                    MemberStudioOptionUiModel(
                        id = "member-1",
                        studioName = "코어앤브리드 성수",
                        isSelected = false,
                    ),
                    MemberStudioOptionUiModel(
                        id = "member-2",
                        studioName = "필라테스 무브먼트 왕십리",
                    ),
                ),
            onConfirmClick = {},
            onOptionClick = {},
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
private fun DialogPreview() {
    StudioSwitchDialog(
        instructorModeOptions =
            listOf(
                InstructorStudioOptionUiModel(
                    id = "instructor-1",
                    studioName = "코코 필라테스 & 피트니스코 영등점",
                    isSelected = true,
                    isLead = true,
                ),
                InstructorStudioOptionUiModel(
                    id = "instructor-2",
                    studioName = "필라테스 무브먼트 왕십리",
                    isLead = false,
                ),
                InstructorStudioOptionUiModel(
                    id = "instructor-1",
                    studioName = "코코 필라테스 & 피트니스코 영등점",
                    isSelected = true,
                    isLead = true,
                ),
                InstructorStudioOptionUiModel(
                    id = "instructor-2",
                    studioName = "필라테스 무브먼트 왕십리",
                    isLead = false,
                ),
            ),
        memberModeOptions =
            listOf(
                MemberStudioOptionUiModel(
                    id = "member-1",
                    studioName = "코어앤브리드 성수",
                    isSelected = false,
                ),
                MemberStudioOptionUiModel(
                    id = "member-2",
                    studioName = "필라테스 무브먼트 왕십리",
                ),
            ),
        onOptionClick = {},
        onDismissRequest = {},
        onConfirmClick = {},
    )
}
