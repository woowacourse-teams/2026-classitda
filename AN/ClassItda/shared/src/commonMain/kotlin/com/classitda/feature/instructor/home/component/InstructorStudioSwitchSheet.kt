package com.classitda.feature.instructor.home.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.ic_instructor
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.ThemeType
import com.classitda.domain.model.studio.Studio
import com.classitda.domain.model.studio.StudioId
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun InstructorStudioSwitchSheet(
    studios: List<Studio>,
    selectedStudioId: String?,
    onStudioClick: (Studio) -> Unit,
    onConfirmClick: () -> Unit,
    onDismissRequest: () -> Unit,
    errorMessage: String? = null,
    onRetry: () -> Unit = {},
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = InsColors.White,
        modifier = modifier,
    ) {
        InstructorStudioSwitchSheetContent(
            studios = studios,
            selectedStudioId = selectedStudioId,
            onStudioClick = onStudioClick,
            onConfirmClick = onConfirmClick,
            errorMessage = errorMessage,
            onRetry = onRetry,
        )
    }
}

@Composable
private fun InstructorStudioSwitchSheetContent(
    studios: List<Studio>,
    selectedStudioId: String?,
    onStudioClick: (Studio) -> Unit,
    onConfirmClick: () -> Unit,
    errorMessage: String? = null,
    onRetry: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = AppSpacing.screenPadding)
                .padding(bottom = AppSpacing.xl),
    ) {
        Column(
            modifier = Modifier.weight(1f, fill = false).fillMaxWidth().verticalScroll(rememberScrollState()),
        ) {
            Text(
                text = "시설 전환",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = InsColors.TextPrimary,
            )
            Spacer(Modifier.height(AppSpacing.sm))
            Text(
                text = "전환할 시설을 선택해주세요.",
                style = MaterialTheme.typography.bodyMedium,
                color = InsColors.TextSecondary,
            )
            Spacer(Modifier.height(AppSpacing.sectionGap))
            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = InsColors.TextSecondary,
                )
                Spacer(Modifier.height(AppSpacing.sm))
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(containerColor = InsColors.Primary),
                ) {
                    Text("다시 시도")
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xl)) {
                    studios.forEach { studio ->
                        StudioOption(
                            studio = studio,
                            isSelected = studio.id.value == selectedStudioId,
                            onClick = { onStudioClick(studio) },
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(AppSpacing.sectionGap))
        Button(
            onClick = onConfirmClick,
            enabled = errorMessage == null && studios.any { it.id.value == selectedStudioId },
            modifier = Modifier.fillMaxWidth(),
            shape = AppShape.Card,
            colors = ButtonDefaults.buttonColors(containerColor = InsColors.Primary),
            contentPadding = PaddingValues(vertical = 16.dp),
        ) {
            Text("확인")
        }
    }
}

@Composable
private fun StudioOption(
    studio: Studio,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(AppShape.Card)
                .background(InsColors.White)
                .border(
                    width = 1.dp,
                    color = if (isSelected) InsColors.TextPrimary else InsColors.Divider,
                    shape = AppShape.Card,
                ).clickable(onClick = onClick)
                .padding(AppSpacing.cardPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Icon(
            painter = painterResource(Res.drawable.ic_instructor),
            contentDescription = null,
            tint = if (isSelected) InsColors.TextPrimary else InsColors.TextSecondary,
            modifier = Modifier.size(24.dp),
        )
        Text(
            text = studio.name,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = if (isSelected) InsColors.TextPrimary else InsColors.TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview(name = "시설 전환", showBackground = true, widthDp = 390)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InstructorStudioSwitchSheetPreview() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        InstructorStudioSwitchSheetContent(
            studios =
                listOf(
                    Studio(
                        id = StudioId("studio-1"),
                        name = "클래스잇다 요가&필라테스",
                        address = "서울시",
                        phoneNumber = "010-0000-0000",
                        openTime = null,
                        closeTime = null,
                        imageUrl = null,
                        description = null,
                    ),
                    Studio(
                        id = StudioId("studio-2"),
                        name = "브리드 라운지 연남",
                        address = "서울시 마포구",
                        phoneNumber = "010-0000-0000",
                        openTime = null,
                        closeTime = null,
                        imageUrl = null,
                        description = null,
                    ),
                ),
            selectedStudioId = "studio-1",
            onStudioClick = {},
            onConfirmClick = {},
        )
    }
}
