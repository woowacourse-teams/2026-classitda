package com.classitda.feature.instructor.management.`class`.create.component

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.ThemeType
import kotlinx.coroutines.flow.filter
import kotlin.math.abs

@Composable
internal fun <T> WheelPicker(
    items: List<T>,
    selectedIndex: Int,
    onSelectedIndexChanged: (Int) -> Unit,
    itemText: (T) -> String,
    modifier: Modifier = Modifier,
    visibleItemCount: Int = 5,
    itemHeight: Dp = 40.dp,
) {
    val spacerCount = visibleItemCount / 2
    val listState = rememberLazyListState()
    val currentSelectedIndex = rememberUpdatedState(selectedIndex)

    LaunchedEffect(listState) {
        listState.scrollToItem(selectedIndex)

        snapshotFlow { listState.isScrollInProgress }
            .filter { isScrolling -> !isScrolling }
            .collect {
                val layoutInfo = listState.layoutInfo
                val centerY = layoutInfo.viewportSize.height / 2
                val centerItem =
                    layoutInfo.visibleItemsInfo.minByOrNull { info ->
                        abs((info.offset + info.size / 2) - centerY)
                    }
                centerItem?.let { info ->
                    val centeredIndex = info.index - spacerCount
                    if (centeredIndex in items.indices && centeredIndex != currentSelectedIndex.value) {
                        onSelectedIndexChanged(centeredIndex)
                    }
                }
            }
    }

    Box(
        modifier = modifier.height(itemHeight * visibleItemCount),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(itemHeight)
                    .background(InsColors.Gray100, AppShape.Card),
        )

        LazyColumn(
            state = listState,
            flingBehavior = rememberSnapFlingBehavior(listState, snapPosition = SnapPosition.Center),
        ) {
            items(spacerCount) {
                Box(modifier = Modifier.height(itemHeight).fillMaxWidth())
            }

            itemsIndexed(items) { index, item ->
                val isSelected = index == selectedIndex
                Box(
                    modifier = Modifier.height(itemHeight).fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = itemText(item),
                        style =
                            if (isSelected) {
                                MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            } else {
                                MaterialTheme.typography.bodyMedium
                            },
                        color = if (isSelected) InsColors.Black else InsColors.TextTertiary,
                    )
                }
            }

            items(spacerCount) {
                Box(modifier = Modifier.height(itemHeight).fillMaxWidth())
            }
        }
    }
}

@Composable
@Preview
private fun WheelPickerPreview() {
    AppTheme(theme = ThemeType.INSTRUCTOR) {
        WheelPicker(
            items = (0..59).toList(),
            selectedIndex = 10,
            onSelectedIndexChanged = {},
            itemText = { it.toString().padStart(2, '0') },
        )
    }
}
