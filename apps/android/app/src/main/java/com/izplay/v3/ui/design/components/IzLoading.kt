package com.izplay.v3.ui.design.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.izplay.v3.ui.design.IzPreviewSurface
import com.izplay.v3.ui.design.tokens.IzColor
import com.izplay.v3.ui.design.tokens.IzMotion
import com.izplay.v3.ui.design.tokens.IzOpacity
import com.izplay.v3.ui.design.tokens.IzRadius
import com.izplay.v3.ui.design.tokens.IzSpacing
import com.izplay.v3.ui.design.tokens.IzType

/**
 * Indicador de carregamento da marca: spinner vermelho, com mensagem opcional.
 * Spec `loading.md`: "nunca permitir loading infinito sem mensagem".
 */
@Composable
fun IzLoading(
    modifier: Modifier = Modifier,
    message: String? = null,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(IzSpacing.md),
    ) {
        CircularProgressIndicator(color = IzColor.Primary, strokeWidth = 3.dp, modifier = Modifier.size(40.dp))
        if (message != null) {
            Text(message, style = IzType.Description, color = IzColor.TextSecondary)
        }
    }
}

/**
 * Placeholder "skeleton" com pulsação de opacidade — usado enquanto a imagem de
 * um card carrega. [IzShimmerBox] preenche o espaço que o conteúdo vai ocupar.
 */
@Composable
fun IzShimmerBox(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(IzRadius.card),
) {
    val transition = rememberInfiniteTransition(label = "izShimmer")
    val alpha by transition.animateFloat(
        initialValue = IzOpacity.SkeletonLow,
        targetValue = IzOpacity.SkeletonHigh,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = IzMotion.Max * 3),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "izShimmerAlpha",
    )
    Box(
        modifier
            .clip(shape)
            .alpha(alpha)
            .background(IzColor.Surface2),
    )
}

@Preview(backgroundColor = 0xFF000000, showBackground = true, widthDp = 300, heightDp = 200)
@Composable
private fun IzLoadingPreview() {
    IzPreviewSurface {
        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
            IzLoading(message = "Carregando catálogo…")
            IzShimmerBox(
                Modifier
                    .fillMaxWidth()
                    .height(60.dp),
            )
        }
    }
}
