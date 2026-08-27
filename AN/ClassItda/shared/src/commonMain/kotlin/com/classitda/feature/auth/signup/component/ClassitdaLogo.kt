package com.classitda.feature.auth.signup.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.classitda_character
import classitda.shared.generated.resources.classitda_logo
import classitda.shared.generated.resources.classitda_wordmark
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.ThemeType
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun ClassitdaLogo(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(Res.drawable.classitda_logo),
        contentDescription = "클래스잇다 로고",
        modifier = modifier,
        contentScale = ContentScale.Fit,
    )
}

@Composable
internal fun ClassitdaCharacter(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(Res.drawable.classitda_character),
        contentDescription = "클래스잇다 캐릭터",
        modifier = modifier,
        contentScale = ContentScale.Fit,
    )
}

@Composable
internal fun ClassitdaWordmark(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(Res.drawable.classitda_wordmark),
        contentDescription = "클래스잇다 워드마크",
        modifier = modifier,
        contentScale = ContentScale.Fit,
    )
}

@Preview(name = "Classitda logo", showBackground = true, widthDp = 280, heightDp = 220)
@Composable
private fun ClassitdaLogoPreview() {
    AppTheme(theme = ThemeType.STUDENT) {
        ClassitdaLogo(
            modifier = Modifier.width(240.dp).height(180.dp),
        )
    }
}

@Preview(name = "Classitda character", showBackground = true, widthDp = 280)
@Composable
private fun ClassitdaCharacterPreview() {
    AppTheme(theme = ThemeType.STUDENT) {
        ClassitdaCharacter(modifier = Modifier.width(240.dp))
    }
}

@Preview(name = "Classitda wordmark", showBackground = true, widthDp = 280, heightDp = 100)
@Composable
private fun ClassitdaWordmarkPreview() {
    AppTheme(theme = ThemeType.STUDENT) {
        ClassitdaWordmark(
            modifier = Modifier.width(240.dp).height(80.dp),
        )
    }
}
