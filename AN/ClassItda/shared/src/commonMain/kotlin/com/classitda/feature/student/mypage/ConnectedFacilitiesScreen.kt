package com.classitda.feature.student.mypage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.connected_facilities_back
import classitda.shared.generated.resources.connected_facilities_connected_on
import classitda.shared.generated.resources.connected_facilities_count
import classitda.shared.generated.resources.connected_facilities_error_description
import classitda.shared.generated.resources.connected_facilities_error_title
import classitda.shared.generated.resources.connected_facilities_loading
import classitda.shared.generated.resources.connected_facilities_retry
import classitda.shared.generated.resources.ic_arrow_back
import classitda.shared.generated.resources.my_page_connected_facilities
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.ThemeType
import com.classitda.core.designsystem.appTypography
import com.classitda.domain.model.student.mypage.ConnectedFacility
import com.classitda.domain.repository.student.mypage.MyPageFailureReason
import com.classitda.feature.student.mypage.contract.ConnectedFacilitiesAction
import com.classitda.feature.student.mypage.contract.ConnectedFacilitiesUiState
import com.classitda.feature.student.mypage.preview.MyPageSettingsBoundaryFixture
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun ConnectedFacilitiesScreen(
    uiState: ConnectedFacilitiesUiState,
    onAction: (ConnectedFacilitiesAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ConnectedFacilitiesTopBar(
                onBack = { onAction(ConnectedFacilitiesAction.Back) },
            )
        },
    ) { innerPadding ->
        when (uiState) {
            ConnectedFacilitiesUiState.Loading -> {
                ConnectedFacilitiesLoadingContent(
                    modifier = Modifier.padding(innerPadding),
                )
            }

            ConnectedFacilitiesUiState.Empty -> {
                ConnectedFacilitiesList(
                    facilities = emptyList(),
                    modifier = Modifier.padding(innerPadding),
                )
            }

            is ConnectedFacilitiesUiState.Content -> {
                ConnectedFacilitiesList(
                    facilities = uiState.facilities,
                    modifier = Modifier.padding(innerPadding),
                )
            }

            is ConnectedFacilitiesUiState.Error -> {
                ConnectedFacilitiesErrorContent(
                    onRetry = { onAction(ConnectedFacilitiesAction.Retry) },
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }
    }
}

@Composable
private fun ConnectedFacilitiesTopBar(onBack: () -> Unit) {
    val typography = appTypography()

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.xs, vertical = AppSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                painter = painterResource(Res.drawable.ic_arrow_back),
                contentDescription = stringResource(Res.string.connected_facilities_back),
                modifier = Modifier.size(AppSpacing.xxl),
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
        Text(
            text = stringResource(Res.string.my_page_connected_facilities),
            modifier =
                Modifier
                    .weight(1f)
                    .semantics { heading() },
            style = typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun ConnectedFacilitiesList(
    facilities: List<ConnectedFacility>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding =
            PaddingValues(
                start = AppSpacing.screenPadding,
                end = AppSpacing.screenPadding,
                bottom = AppSpacing.sectionGap,
            ),
    ) {
        item {
            ConnectedFacilitiesCount(
                count = facilities.size,
                modifier =
                    Modifier.padding(
                        top = AppSpacing.xxl,
                        bottom = AppSpacing.xxl,
                    ),
            )
        }
        itemsIndexed(
            items = facilities,
            key = { _, facility -> facility.id.value },
        ) { index, facility ->
            ConnectedFacilityRow(facility = facility)
            if (index < facilities.lastIndex) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}

@Composable
private fun ConnectedFacilitiesCount(
    count: Int,
    modifier: Modifier = Modifier,
) {
    val typography = appTypography()
    val countText = count.toString()
    val sentence = stringResource(Res.string.connected_facilities_count, count)
    val countStart = sentence.indexOf(countText)
    val annotatedSentence =
        buildAnnotatedString {
            if (countStart < 0) {
                append(sentence)
                return@buildAnnotatedString
            }

            append(sentence.substring(0, countStart))
            withStyle(
                SpanStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                ),
            ) {
                append(countText)
            }
            append(sentence.substring(countStart + countText.length))
        }

    Text(
        text = annotatedSentence,
        modifier = modifier.fillMaxWidth(),
        style = typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun ConnectedFacilityRow(facility: ConnectedFacility) {
    val typography = appTypography()

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = AppSpacing.xxl),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        Text(
            text = facility.name,
            modifier = Modifier.fillMaxWidth(),
            style = typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text =
                stringResource(
                    Res.string.connected_facilities_connected_on,
                    facility.connectedOn.toDisplayDate(),
                ),
            modifier = Modifier.fillMaxWidth(),
            style = typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ConnectedFacilitiesLoadingContent(modifier: Modifier = Modifier) {
    val typography = appTypography()

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(AppSpacing.lg))
        Text(
            text = stringResource(Res.string.connected_facilities_loading),
            style = typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ConnectedFacilitiesErrorContent(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val typography = appTypography()
    val errorTitle = stringResource(Res.string.connected_facilities_error_title)

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = AppSpacing.screenPadding)
                .semantics { error(errorTitle) },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = errorTitle,
            modifier = Modifier.fillMaxWidth(),
            style = typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(AppSpacing.sm))
        Text(
            text = stringResource(Res.string.connected_facilities_error_description),
            modifier = Modifier.fillMaxWidth(),
            style = typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(AppSpacing.xxl))
        OutlinedButton(onClick = onRetry) {
            Text(
                text = stringResource(Res.string.connected_facilities_retry),
                style = typography.labelLarge,
            )
        }
    }
}

private fun LocalDate.toDisplayDate(): String {
    val yearPart = year.toString().padStart(4, '0')
    val monthPart = month.number.toString().padStart(2, '0')
    val dayPart = day.toString().padStart(2, '0')

    return "$yearPart.$monthPart.$dayPart"
}

private fun ConnectedFacilitiesAction.previewLabel(): String =
    when (this) {
        ConnectedFacilitiesAction.Back -> "Back"
        ConnectedFacilitiesAction.Retry -> "Retry"
    }

private object ConnectedFacilitiesPreviewFixture {
    val content = MyPageSettingsBoundaryFixture.facilitiesMany
    val empty = MyPageSettingsBoundaryFixture.facilitiesEmpty
    val one = MyPageSettingsBoundaryFixture.facilitiesOne
    val loading = ConnectedFacilitiesUiState.Loading
    val error = ConnectedFacilitiesUiState.Error(MyPageFailureReason.NETWORK)
    val longName = MyPageSettingsBoundaryFixture.facilitiesLongName
}

@Preview(
    name = "Content · Student · Default",
    group = "Screen/ConnectedFacilities",
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun ConnectedFacilitiesScreenPreview_Content_Student_Default() {
    AppTheme(theme = ThemeType.STUDENT) {
        ConnectedFacilitiesScreen(
            uiState = ConnectedFacilitiesPreviewFixture.content,
            onAction = {},
        )
    }
}

@Preview(
    name = "Empty · Student · Default",
    group = "Screen/ConnectedFacilities",
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun ConnectedFacilitiesScreenPreview_Empty_Student_Default() {
    AppTheme(theme = ThemeType.STUDENT) {
        ConnectedFacilitiesScreen(
            uiState = ConnectedFacilitiesPreviewFixture.empty,
            onAction = {},
        )
    }
}

@Preview(
    name = "Loading · Student · Default",
    group = "Screen/ConnectedFacilities",
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun ConnectedFacilitiesScreenPreview_Loading_Student_Default() {
    AppTheme(theme = ThemeType.STUDENT) {
        ConnectedFacilitiesScreen(
            uiState = ConnectedFacilitiesPreviewFixture.loading,
            onAction = {},
        )
    }
}

@Preview(
    name = "Error · Student · Default",
    group = "Screen/ConnectedFacilities",
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun ConnectedFacilitiesScreenPreview_Error_Student_Default() {
    AppTheme(theme = ThemeType.STUDENT) {
        ConnectedFacilitiesScreen(
            uiState = ConnectedFacilitiesPreviewFixture.error,
            onAction = {},
        )
    }
}

@Preview(
    name = "Long name · Large font · Small screen",
    group = "Boundary/ConnectedFacilities",
    widthDp = 320,
    heightDp = 568,
    fontScale = 1.5f,
)
@Composable
private fun ConnectedFacilitiesScreenPreview_LongName_LargeFont_SmallScreen() {
    AppTheme(theme = ThemeType.STUDENT) {
        ConnectedFacilitiesScreen(
            uiState = ConnectedFacilitiesPreviewFixture.longName,
            onAction = {},
        )
    }
}

@Preview(
    name = "Boundary · One facility",
    group = "Boundary/ConnectedFacilities",
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun ConnectedFacilitiesScreenPreview_Boundary_OneFacility() {
    AppTheme(theme = ThemeType.STUDENT) {
        ConnectedFacilitiesScreen(
            uiState = ConnectedFacilitiesPreviewFixture.one,
            onAction = {},
        )
    }
}

@Preview(
    name = "Back and Retry · Student · Interactive",
    group = "Harness/ConnectedFacilities",
    widthDp = 390,
    heightDp = 840,
)
@Composable
private fun ConnectedFacilitiesScreenPreview_BackAndRetry_Student_Interactive() {
    var lastAction by remember { mutableStateOf("None") }

    AppTheme(theme = ThemeType.STUDENT) {
        val typography = appTypography()

        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "마지막 행동: $lastAction",
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = AppSpacing.screenPadding,
                            vertical = AppSpacing.sm,
                        ),
                style = typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Box(modifier = Modifier.weight(1f)) {
                ConnectedFacilitiesScreen(
                    uiState = ConnectedFacilitiesPreviewFixture.error,
                    onAction = { lastAction = it.previewLabel() },
                )
            }
        }
    }
}
