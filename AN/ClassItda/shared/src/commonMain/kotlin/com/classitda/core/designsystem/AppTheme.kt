package com.classitda.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class ThemeType {
    DEFAULT,
    STUDENT,
    INSTRUCTOR,
}

private val DefaultScheme =
    lightColorScheme(
        background = Color(0xffEDEDED),
        onBackground = Color(0xff000000),
        primary = Color(0xff000000),
        onPrimary = Color(0xffFFFFFF),
    )

private val StudentScheme =
    lightColorScheme(
        // Primary
        primary = StuColors.Green,
        onPrimary = StuColors.White,
        primaryContainer = StuColors.GreenLight,
        onPrimaryContainer = StuColors.Green,
        // Background
        background = StuColors.Background,
        onBackground = StuColors.TextPrimary,
        // Surface
        surface = StuColors.Surface,
        onSurface = StuColors.TextPrimary,
        surfaceVariant = StuColors.SurfaceVariant,
        onSurfaceVariant = StuColors.TextSecondary,
        surfaceTint = StuColors.Green,
        // DropdownMenu, DatePickerDialog 등 팝업 배경에 쓰이는 톤
        surfaceContainerLowest = StuColors.White,
        surfaceContainerLow = StuColors.Background,
        surfaceContainer = StuColors.White,
        surfaceContainerHigh = StuColors.SurfaceVariant,
        surfaceContainerHighest = StuColors.Gray200,
        // Outline / Divider
        outline = StuColors.DividerStrong,
        outlineVariant = StuColors.Divider,
        // Error
        error = StuColors.Red,
        onError = StuColors.White,
        errorContainer = StuColors.RedLight,
        onErrorContainer = StuColors.Red,
        // 모달 뒷 배경
        scrim = StuColors.Dim,
    )

private val InstructorScheme =
    lightColorScheme(
        // Primary
        primary = InsColors.Purple,
        onPrimary = InsColors.White,
        primaryContainer = InsColors.PurpleLight,
        onPrimaryContainer = InsColors.Purple,
        // Background
        background = InsColors.Background,
        onBackground = InsColors.TextPrimary,
        // Surface
        surface = InsColors.Surface,
        onSurface = InsColors.TextPrimary,
        surfaceVariant = InsColors.SurfaceVariant,
        onSurfaceVariant = InsColors.TextSecondary,
        surfaceTint = InsColors.Purple,
        // DropdownMenu, DatePickerDialog 등 팝업 배경에 쓰이는 톤
        surfaceContainerLowest = InsColors.White,
        surfaceContainerLow = InsColors.Background,
        surfaceContainer = InsColors.White,
        surfaceContainerHigh = InsColors.SurfaceVariant,
        surfaceContainerHighest = InsColors.Gray200,
        // Outline / Divider
        outline = InsColors.DividerStrong,
        outlineVariant = InsColors.Divider,
        // Error
        error = InsColors.Red,
        onError = InsColors.White,
        errorContainer = InsColors.RedLight,
        onErrorContainer = InsColors.Red,
        // 모달 뒷 배경
        scrim = InsColors.Dim,
    )

@Composable
fun AppTheme(
    theme: ThemeType = ThemeType.STUDENT,
    content: @Composable () -> Unit,
) {
    val scheme =
        when (theme) {
            ThemeType.DEFAULT -> DefaultScheme
            ThemeType.STUDENT -> StudentScheme
            ThemeType.INSTRUCTOR -> InstructorScheme
        }

    MaterialTheme(
        colorScheme = scheme,
        typography = appTypography(),
        content = content,
    )
}
