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
        primary = StuColors.PrimaryGreen,
        onPrimary = StuColors.White,
        primaryContainer = StuColors.SecondaryGreen,
        onPrimaryContainer = StuColors.PrimaryGreen,
        // Background
        background = StuColors.Background,
        onBackground = StuColors.TextPrimary,
        // Surface
        surface = StuColors.Surface,
        onSurface = StuColors.TextPrimary,
        surfaceVariant = StuColors.SurfaceVariant,
        onSurfaceVariant = StuColors.TextSecondary,
        // Outline / Divider
        outline = StuColors.Divider,
        outlineVariant = StuColors.Divider,
        // Error
        error = StuColors.AccentRed,
        onError = StuColors.White,
        errorContainer = StuColors.SecondaryRed,
        onErrorContainer = StuColors.AccentRed,
        // 모달 뒷 배경
        scrim = StuColors.dim,
    )

private val InstructorScheme =
    lightColorScheme(
        // Primary
        primary = InsColors.PrimaryPurple,
        onPrimary = InsColors.White,
        primaryContainer = InsColors.SecondaryPurple,
        onPrimaryContainer = InsColors.PrimaryPurple,
        // Background
        background = InsColors.Background,
        onBackground = InsColors.TextPrimary,
        // Surface
        surface = InsColors.Surface,
        onSurface = InsColors.TextPrimary,
        surfaceVariant = InsColors.SurfaceVariant,
        onSurfaceVariant = InsColors.TextSecondary,
        // Outline / Divider
        outline = InsColors.Divider,
        outlineVariant = InsColors.Divider,
        // Error
        error = InsColors.AccentRed,
        onError = InsColors.White,
        errorContainer = InsColors.SecondaryRed,
        onErrorContainer = InsColors.AccentRed,
        // 모달 뒷 배경
        scrim = InsColors.dim,
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
