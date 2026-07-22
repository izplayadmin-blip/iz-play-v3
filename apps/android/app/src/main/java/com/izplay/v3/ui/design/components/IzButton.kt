package com.izplay.v3.ui.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.izplay.v3.ui.design.IzPreviewSurface
import com.izplay.v3.ui.design.focus.IzFocusRing
import com.izplay.v3.ui.design.focus.izFocusVisuals
import com.izplay.v3.ui.design.focus.rememberIzInteractionSource
import com.izplay.v3.ui.design.tokens.IzColor
import com.izplay.v3.ui.design.tokens.IzFocusScale
import com.izplay.v3.ui.design.tokens.IzOpacity
import com.izplay.v3.ui.design.tokens.IzRadius
import com.izplay.v3.ui.design.tokens.IzSize
import com.izplay.v3.ui.design.tokens.IzSpacing
import com.izplay.v3.ui.design.tokens.IzType

/** Variantes visuais do [IzButton]. */
enum class IzButtonStyle { Primary, Secondary }

/**
 * Botão da marca. Altura 48dp, raio 12dp, texto 16sp Medium (spec `buttons.md`).
 * `Primary` é vermelho; `Secondary` é superfície com contorno. Foco de TV escala
 * 1.03 e ganha borda vermelha via [izFocusVisuals].
 */
@Composable
fun IzButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: IzButtonStyle = IzButtonStyle.Primary,
    leadingIcon: ImageVector? = null,
    enabled: Boolean = true,
) {
    val interaction: MutableInteractionSource = rememberIzInteractionSource()
    val shape = RoundedCornerShape(IzRadius.md)

    val container = when (style) {
        IzButtonStyle.Primary -> IzColor.Primary
        IzButtonStyle.Secondary -> IzColor.Surface2
    }
    val content = when (style) {
        IzButtonStyle.Primary -> IzColor.OnPrimary
        IzButtonStyle.Secondary -> IzColor.TextPrimary
    }

    val focusRing = when (style) {
        IzButtonStyle.Primary -> IzFocusRing.OnPrimary
        IzButtonStyle.Secondary -> IzFocusRing.OnDark
    }

    Box(
        modifier = modifier
            .height(IzSize.buttonHeight)
            .defaultMinSize(minWidth = 96.dp)
            .izFocusVisuals(interaction, shape = shape, focusedScale = IzFocusScale.Button, ring = focusRing)
            .clip(shape)
            .background(if (enabled) container else container.copy(alpha = IzOpacity.Disabled))
            .then(
                if (style == IzButtonStyle.Secondary) {
                    Modifier.border(1.dp, IzColor.Border, shape)
                } else {
                    Modifier
                },
            )
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                onClick = onClick,
            )
            .padding(horizontal = IzSize.buttonPadding),
        contentAlignment = Alignment.Center,
    ) {
        val effectiveContent = if (enabled) content else content.copy(alpha = IzOpacity.Disabled)
        Row(
            horizontalArrangement = Arrangement.spacedBy(IzSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (leadingIcon != null) {
                Icon(leadingIcon, contentDescription = null, tint = effectiveContent)
            }
            Text(text, style = IzType.Button, color = effectiveContent)
        }
    }
}

@Preview(backgroundColor = 0xFF000000, showBackground = true)
@Composable
private fun IzButtonPreview() {
    IzPreviewSurface {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            IzButton(text = "Assistir", onClick = {})
            IzButton(text = "Detalhes", onClick = {}, style = IzButtonStyle.Secondary)
            IzButton(text = "Indisponível", onClick = {}, enabled = false)
        }
    }
}
