package com.izplay.v3.ui.design.tokens

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Gradientes da marca. A spec do Hero pede "overlay de gradiente preto" sobre o
 * backdrop, para legibilidade do título e das ações.
 */
object IzGradient {
    /** Scrim vertical: transparente no topo → preto na base. Para Hero e cards. */
    val ScrimBottom: Brush = Brush.verticalGradient(
        0.0f to Color.Transparent,
        0.55f to Color.Black.copy(alpha = 0.35f),
        1.0f to Color.Black.copy(alpha = IzOpacity.ScrimHero),
    )

    /** Scrim horizontal: preto à esquerda → transparente. Para heroes largos. */
    val ScrimStart: Brush = Brush.horizontalGradient(
        0.0f to Color.Black.copy(alpha = IzOpacity.ScrimHero),
        0.6f to Color.Black.copy(alpha = 0.2f),
        1.0f to Color.Transparent,
    )

    /** Placeholder de imagem ausente: leve variação sobre a superfície. */
    val SurfacePlaceholder: Brush = Brush.linearGradient(
        listOf(IzColor.Surface2, IzColor.Surface),
    )
}
