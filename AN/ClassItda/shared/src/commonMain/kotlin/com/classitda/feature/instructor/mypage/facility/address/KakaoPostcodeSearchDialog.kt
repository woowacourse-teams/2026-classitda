package com.classitda.feature.instructor.mypage.facility.address

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.ic_close
import classitda.shared.generated.resources.ic_error
import classitda.shared.generated.resources.ic_location_on
import classitda.shared.generated.resources.instructor_facility_address_search_close
import classitda.shared.generated.resources.instructor_facility_address_search_description
import classitda.shared.generated.resources.instructor_facility_address_search_error_blocked
import classitda.shared.generated.resources.instructor_facility_address_search_error_network
import classitda.shared.generated.resources.instructor_facility_address_search_error_parse
import classitda.shared.generated.resources.instructor_facility_address_search_error_unknown
import classitda.shared.generated.resources.instructor_facility_address_search_retry
import classitda.shared.generated.resources.instructor_facility_address_search_title
import com.classitda.core.designsystem.AppShape
import com.classitda.core.designsystem.AppSpacing
import com.classitda.core.designsystem.InsColors
import com.classitda.core.designsystem.appTypography
import com.classitda.core.platform.KakaoPostcodeError
import com.classitda.core.platform.KakaoPostcodeResult
import com.classitda.core.platform.KakaoPostcodeSearchState
import com.classitda.core.platform.KakaoPostcodeWebContent
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun KakaoPostcodeSearchDialog(
    state: KakaoPostcodeSearchState,
    onLoadingChanged: (Boolean) -> Unit,
    onResult: (KakaoPostcodeResult) -> Unit,
    onCancelled: () -> Unit,
    onError: (KakaoPostcodeError) -> Unit,
    onRetry: () -> Unit,
) {
    Dialog(
        onDismissRequest = onCancelled,
        properties =
            DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false,
            ),
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(horizontal = AppSpacing.lg, vertical = AppSpacing.xxl),
            contentAlignment = Alignment.Center,
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth().fillMaxHeight(0.92f).heightIn(min = AppSpacing.xxxl * 8),
                shape = AppShape.Card,
                color = InsColors.Surface,
                shadowElevation = AppSpacing.sm,
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(start = AppSpacing.xl, end = AppSpacing.sm),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_location_on),
                                contentDescription = null,
                                modifier = Modifier.size(AppSpacing.xxl),
                                tint = InsColors.Primary,
                            )
                            Text(
                                text = stringResource(Res.string.instructor_facility_address_search_title),
                                modifier = Modifier.weight(1f).padding(start = AppSpacing.sm).semantics { heading() },
                                style = appTypography().titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = InsColors.TextPrimary,
                            )
                            IconButton(onClick = onCancelled) {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_close),
                                    contentDescription =
                                        stringResource(Res.string.instructor_facility_address_search_close),
                                    tint = InsColors.TextSecondary,
                                )
                            }
                        }
                        Text(
                            text = stringResource(Res.string.instructor_facility_address_search_description),
                            modifier = Modifier.padding(bottom = AppSpacing.lg),
                            style = appTypography().bodySmall,
                            color = InsColors.TextSecondary,
                        )
                    }
                    HorizontalDivider(color = InsColors.Divider)
                    Surface(
                        modifier = Modifier.fillMaxSize().padding(AppSpacing.md),
                        shape = AppShape.Card,
                        color = InsColors.SurfaceVariant,
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            KakaoPostcodeWebContent(
                                onLoadingChanged = onLoadingChanged,
                                onResult = onResult,
                                onCancelled = onCancelled,
                                onError = onError,
                                modifier = Modifier.fillMaxSize(),
                            )
                            if (state is KakaoPostcodeSearchState.Error) {
                                KakaoPostcodeErrorContent(
                                    reason = state.reason,
                                    onRetry = onRetry,
                                    onClose = onCancelled,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            } else if (state == KakaoPostcodeSearchState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.align(Alignment.Center),
                                    color = InsColors.Purple,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KakaoPostcodeErrorContent(
    reason: KakaoPostcodeError,
    onRetry: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val message =
        when (reason) {
            KakaoPostcodeError.NETWORK -> Res.string.instructor_facility_address_search_error_network
            KakaoPostcodeError.INVALID_PAYLOAD -> Res.string.instructor_facility_address_search_error_parse
            KakaoPostcodeError.NAVIGATION_BLOCKED -> Res.string.instructor_facility_address_search_error_blocked
            KakaoPostcodeError.UNKNOWN -> Res.string.instructor_facility_address_search_error_unknown
        }
    Surface(modifier = modifier, color = InsColors.Surface) {
        Column(
            modifier = Modifier.fillMaxSize().padding(AppSpacing.xxl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_error),
                contentDescription = null,
                modifier = Modifier.size(AppSpacing.xxxl),
                tint = InsColors.Purple,
            )
            Text(
                text = stringResource(message),
                modifier = Modifier.padding(top = AppSpacing.lg),
                style = appTypography().bodyLarge,
                color = InsColors.TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
            Row(
                modifier = Modifier.padding(top = AppSpacing.xl),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
            ) {
                TextButton(onClick = onClose) {
                    Text(stringResource(Res.string.instructor_facility_address_search_close))
                }
                Button(onClick = onRetry, shape = AppShape.Card) {
                    Text(stringResource(Res.string.instructor_facility_address_search_retry))
                }
            }
        }
    }
}
