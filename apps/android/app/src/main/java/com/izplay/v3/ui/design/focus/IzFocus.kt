package com.izplay.v3.ui.design.focus

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import com.izplay.v3.ui.design.tokens.IzBorder
import com.izplay.v3.ui.design.tokens.IzColor
import com.izplay.v3.ui.design.tokens.IzFocusScale
import com.izplay.v3.ui.design.tokens.IzMotion
import com.izplay.v3.ui.design.tokens.IzOpacity

/**
 * Cor do anel de foco por tipo de fundo (correção de contraste da Etapa 2).
 * Regra: componente vermelho recebe anel claro; componente escuro recebe anel
 * vermelho. Amarelo nunca é anel de foco.
 */
enum class IzFocusRing {
    /** Item sobre superfície escura → anel vermelho da marca. */
    OnDark,

    /** Item vermelho (primário) → anel claro (branco) para alto contraste. */
    OnPrimary,
}

/**
 * Núcleo do sistema de foco de TV do IZ Play V3.
 *
 * Regra do V2 (`08-Focus-System.md`): **todo item interativo tem foco visível;
 * foco invisível nunca é aceitável.** Em resposta ao foco por D-pad, aplica:
 *  - escala animada (150ms, valor por tipo de elemento — ver [IzFocusScale]);
 *  - anel de foco de alto contraste, com um separador fino por baixo para
 *    permanecer visível sobre qualquer fundo em 720p, 1080p e 4K;
 *  - leve escurecimento no estado pressionado.
 *
 * O contraste NÃO depende só da escala: o anel tem cor contextual ([ring]) e é
 * reforçado por um separador. Deve ser combinado com um `focusable`/`clickable`
 * que use a MESMA [interactionSource].
 */
@Composable
fun Modifier.izFocusVisuals(
    interactionSource: MutableInteractionSource,
    shape: RoundedCornerShape,
    focusedScale: Float = IzFocusScale.Card,
    ring: IzFocusRing = IzFocusRing.OnDark,
    showRing: Boolean = true,
): Modifier {
    val focused by interactionSource.collectIsFocusedAsState()
    val pressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (focused) focusedScale else IzFocusScale.Rest,
        animationSpec = tween(durationMillis = IzMotion.Fast),
        label = "izFocusScale",
    )
    val contentAlpha = if (pressed) 1f - IzOpacity.Pressed else 1f

    val ringColor: Color = when (ring) {
        IzFocusRing.OnDark -> IzColor.Primary
        IzFocusRing.OnPrimary -> IzColor.TextPrimary
    }
    // Cor do separador: o oposto do anel, para o contorno "saltar" em qualquer
    // fundo (anel vermelho ganha separador escuro; anel branco, vermelho).
    val separatorColor: Color = when (ring) {
        IzFocusRing.OnDark -> IzColor.Background
        IzFocusRing.OnPrimary -> IzColor.Primary
    }

    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
            alpha = contentAlpha
        }
        .then(
            if (showRing && focused) {
                // Separador por fora + anel de foco por dentro dele.
                Modifier
                    .border(IzBorder.focusSeparator, separatorColor, shape)
                    .padding(IzBorder.focusSeparator)
                    .border(IzBorder.focus, ringColor, shape)
            } else {
                Modifier
            },
        )
}

/** Cria e memoiza uma [MutableInteractionSource] para um item focável. */
@Composable
fun rememberIzInteractionSource(): MutableInteractionSource =
    remember { MutableInteractionSource() }
