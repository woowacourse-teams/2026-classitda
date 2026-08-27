package com.classitda.feature.instructor.classsession.detail.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.ic_arrow_back
import classitda.shared.generated.resources.ic_edit
import classitda.shared.generated.resources.ic_more
import classitda.shared.generated.resources.ic_person
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.ThemeType
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun ClassSessionDetailTopBar(
    isMenuExpanded: Boolean,
    onBackClick: () -> Unit,
    onMoreClick: () -> Unit,
    onDismissMenu: () -> Unit,
    onEditClick: () -> Unit,
    onMemberEditClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(52.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_arrow_back),
                contentDescription = "뒤로가기",
                tint = InsColors.TextPrimary,
                modifier =
                    Modifier
                        .size(48.dp)
                        .clickable(onClick = onBackClick)
                        .padding(12.dp),
            )
            Text(
                text = "수업 상세",
                color = InsColors.TextPrimary,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
            )
            Box {
                Icon(
                    painter = painterResource(Res.drawable.ic_more),
                    contentDescription = "더보기",
                    tint = InsColors.TextPrimary,
                    modifier =
                        Modifier
                            .size(48.dp)
                            .clickable(onClick = onMoreClick)
                            .padding(12.dp),
                )
                DropdownMenu(
                    expanded = isMenuExpanded,
                    onDismissRequest = onDismissMenu,
                ) {
                    DropdownMenuItem(
                        text = { Text("수업 수정") },
                        onClick = onEditClick,
                        leadingIcon = {
                            Icon(
                                painter = painterResource(Res.drawable.ic_edit),
                                contentDescription = null,
                            )
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("회원 수정") },
                        onClick = onMemberEditClick,
                        leadingIcon = {
                            Icon(
                                painter = painterResource(Res.drawable.ic_person),
                                contentDescription = null,
                            )
                        },
                    )
                }
            }
        }
    }
}

@Preview(name = "수업 상세 상단바", showBackground = true, widthDp = 390)
@Composable
private fun ClassSessionDetailTopBarPreview() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        ClassSessionDetailTopBar(
            isMenuExpanded = false,
            onBackClick = {},
            onMoreClick = {},
            onDismissMenu = {},
            onEditClick = {},
            onMemberEditClick = {},
        )
    }
}

@Preview(name = "수업 상세 상단바 - 메뉴", showBackground = true, widthDp = 390)
@Composable
private fun ClassSessionDetailTopBarMenuPreview() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        ClassSessionDetailTopBar(
            isMenuExpanded = true,
            onBackClick = {},
            onMoreClick = {},
            onDismissMenu = {},
            onEditClick = {},
            onMemberEditClick = {},
        )
    }
}
