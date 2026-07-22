package com.izplay.v3.ui.design.focus

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import com.izplay.v3.ui.design.tokens.IzBorder
import com.izplay.v3.ui.design.tokens.IzColor
import com.izplay.v3.ui.design.tokens.IzFocusScale
import com.izplay.v3.ui.design.tokens.IzMotion
import com.izplay.v3.ui.design.tokens.IzOpacity

/**
 * Núcleo do sistema de foco de TV do IZ Play V3.
 *
 * Regra do V2 (`08-Focus-System.md`): **todo item interativo tem foco visível;
 * foco invisível nunca é aceitável.** Em resposta ao foco por D-pad, aplica:
 *  - escala animada (valor conforme o tipo de elemento, ver [IzFocusScale]);
 *  - borda vermelha da marca ([IzColor.Primary]) na largura [IzBorder.focus];
 *  - leve escurecimento no estado pressionado.
 *
 * Apenas visualiza o foco — não altera a lógica de clique nem consome foco.
 * Deve ser combinado com um `focusable`/`clickable` que use a MESMA
 * [interactionSource].
 *
 * @param focusedScale escala no estado focado (default [IzFocusScale.Card]).
 * @param shape forma da borda de foco; deve casar com a do conteúdo.
 * @param showBorder desliga a borda quando o componente já a desenha sozinho.
 */
@Composable
fun Modifier.izFocusVisuals(
    interactionSource: MutableInteractionSource,
    shape: RoundedCornerShape,
    focusedScale: Float = IzFocusScale.Card,
    borderWidth: Dp = IzBorder.focus,
    showBorder: Boolean = true,
): Modifier {
    val focused by interactionSource.collectIsFocusedAsState()
    val pressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (focused) focusedScale else IzFocusScale.Rest,
        animationSpec = tween(durationMillis = IzMotion.Fast),
        label = "izFocusScale",
    )
    val contentAlpha = if (pressed) 1f - IzOpacity.Pressed else 1f

    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
            alpha = contentAlpha
        }
        .then(
            if (showBorder && focused) {
                Modifier.border(borderWidth, IzColor.Primary, shape)
            } else {
                Modifier
            },
        )
}

/** Cria e memoiza uma [MutableInteractionSource] para um item focável. */
@Composable
fun rememberIzInteractionSource(): MutableInteractionSource =
    remember { MutableInteractionSource() }
