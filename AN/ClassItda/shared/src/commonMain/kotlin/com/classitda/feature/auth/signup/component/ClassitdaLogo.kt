package com.classitda.feature.auth.signup.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import classitda.shared.generated.resources.Res
import classitda.shared.generated.resources.classitda_logo
import classitda.shared.generated.resources.classitda_wordmark
import com.classitda.core.designsystem.AppTheme
import com.classitda.core.designsystem.ThemeType
import org.jetbrains.compose.resources.painterResource

@Composable
internal fun ClassitdaLogo(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(Res.drawable.classitda_logo),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentScale = ContentScale.Crop,
            alignment = Alignment.TopCenter,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Image(
            painter = painterResource(Res.drawable.classitda_wordmark),
            contentDescription = "클래스잇다 로고",
            modifier = Modifier.fillMaxWidth().weight(0.4f),
            contentScale = ContentScale.Fit,
        )
    }
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
