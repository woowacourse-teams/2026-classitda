package com.classitda.core.designsystem

import androidx.compose.ui.unit.dp

object AppSpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
    val xxxl = 32.dp

    val screenPadding = xl // mainContent padding
    val cardPadding = lg // card 내부 패딩

    val chipVerticalPadding = sm // statusChip 세로 padding
    val chipHorizontalPadding = md // statusChip 가로 padding
    val chipIconGap = sm // statusChip 내 component 간격

    val pillChipVerticalPadding = xs // pillChip(필터용) 세로 padding
    val pillChipHorizontalPadding = sm // pillChip(필터용) 가로 padding

    val cardGap = md // card 리스트에서의 gap
    val cardItemVerticalGap = sm // card 내 세로 component 간격
    val cardItemHorizontalGap = xs // card 내 가로 component 간격

    val sectionGap = xxl // 섹션 나뉘는 곳의 간격
}
