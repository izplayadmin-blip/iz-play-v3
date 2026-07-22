package com.izplay.v3.ui.design.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.izplay.v3.R
import com.izplay.v3.ui.design.tokens.IzColor

/**
 * Tela de espera da marca — reimplementação fiel do `LoadingScreen` do IZ Play
 * V2 Android: glow radial vermelho-escuro sobre preto, logo oficial 300dp,
 * barra de varredura 260×3dp em gradiente vermelho (1150ms linear) e mensagem
 * 13sp. Usada no carregamento inicial do catálogo.
 */
@Composable
fun IzLoadingScreen(
    modifier: Modifier = Modifier,
    message: String? = null,
) {
    val transition = rememberInfiniteTransition(label = "izLoading")
    val offset by transition.animateFloat(
        initialValue = -0.45f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1150, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "izLoadingBar",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    // Pré-mesclado (RedDeep@28% sobre preto) para evitar
                    // banding de alpha em GPUs fracas de TV box.
                    listOf(Color(0xFF190000), Color.Black),
                    radius = 920f,
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(R.drawable.izplay_logo),
                contentDescription = "IZ Play",
                contentScale = ContentScale.Fit,
                modifier = Modifier.width(300.dp),
            )
            Spacer(Modifier.height(34.dp))
            Box(
                Modifier
                    .width(260.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color.White.copy(alpha = 0.13f)),
            ) {
                Box(
                    Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.38f)
                        .offset(x = (offset * 260).dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            Brush.horizontalGradient(listOf(IzColor.Primary, Color(0xFFFF5A62))),
                        ),
                )
            }
            if (!message.isNullOrEmpty()) {
                Spacer(Modifier.height(18.dp))
                Text(
                    message,
                    color = IzColor.TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Preview(backgroundColor = 0xFF000000, showBackground = true, widthDp = 640, heightDp = 360)
@Composable
private fun IzLoadingScreenPreview() {
    IzLoadingScreen(message = "Carregando canais, filmes e séries…")
}
