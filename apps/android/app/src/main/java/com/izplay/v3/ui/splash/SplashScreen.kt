package com.izplay.v3.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.izplay.v3.ui.design.IzTheme
import com.izplay.v3.ui.design.components.IzLogo
import com.izplay.v3.ui.design.tokens.IzColor
import com.izplay.v3.ui.design.tokens.IzMotion

/**
 * Splash da marca IZ Play — apenas apresentação.
 *
 * Fundo preto, logo centralizado, animação curta de fade + leve escala.
 * [onAnimationEnd] dispara quando a entrada termina (uma janela de marca curta,
 * sem atraso artificial longo). QUEM decide o destino (retomar dashboard ou ir
 * à lista de playlists) é a navegação — este componente não roteia nem lê dados.
 */
@Composable
fun SplashScreen(
    onAnimationEnd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val alpha = remember { Animatable(0f) }
    val scale = remember { Animatable(0.92f) }

    LaunchedEffect(Unit) {
        alpha.animateTo(1f, tween(durationMillis = IzMotion.Max))
        scale.animateTo(1f, tween(durationMillis = IzMotion.Max))
        onAnimationEnd()
    }

    IzTheme {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(IzColor.Background),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(alpha.value)
                    .scale(scale.value),
            ) {
                IzLogo(height = 104.dp, modifier = Modifier.fillMaxWidth(0.72f))
                Spacer(Modifier.height(34.dp))
                Text(
                    text = "C A R R E G A N D O",
                    color = IzColor.TextPrimary,
                    fontSize = 13.sp,
                )
            }
        }
    }
}

@Preview(backgroundColor = 0xFF000000, showBackground = true, widthDp = 640, heightDp = 360)
@Composable
private fun SplashScreenPreview() {
    SplashScreen(onAnimationEnd = {})
}
