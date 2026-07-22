package com.izplay.v3.ui.design.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.izplay.v3.ui.design.IzPreviewSurface
import com.izplay.v3.ui.design.tokens.IzColor
import com.izplay.v3.ui.design.tokens.IzGradient
import com.izplay.v3.ui.design.tokens.IzRadius
import com.izplay.v3.ui.design.tokens.IzSize
import com.izplay.v3.ui.design.tokens.IzSpacing
import com.izplay.v3.ui.design.tokens.IzType

/**
 * Hero de destaque. Altura 320dp (spec `hero.md`), backdrop em cover, overlay
 * de gradiente preto para legibilidade, título 48sp, descrição até 3 linhas,
 * botões Assistir + Detalhes. Sem backdrop, cai no placeholder gradiente
 * (fallback exigido pela spec).
 *
 * A rotação automática, os indicadores e o limite de itens do carrossel ficam
 * a cargo de quem compõe a Home (Etapa 3) — este componente renderiza UM slide.
 */
@Composable
fun IzHero(
    title: String,
    onPlay: () -> Unit,
    onDetails: () -> Unit,
    modifier: Modifier = Modifier,
    backdrop: Painter? = null,
    description: String? = null,
    badge: (@Composable () -> Unit)? = null,
) {
    val shape = RoundedCornerShape(IzRadius.hero)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(IzSize.heroHeight)
            .clip(shape)
            .background(IzColor.BackgroundDeep),
    ) {
        if (backdrop != null) {
            Image(backdrop, contentDescription = title, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        } else {
            Box(Modifier.fillMaxSize().background(IzGradient.SurfacePlaceholder))
        }
        // Overlays: escurece base e lateral esquerda para o texto respirar.
        Box(Modifier.fillMaxSize().background(IzGradient.ScrimStart))
        Box(Modifier.fillMaxSize().background(IzGradient.ScrimBottom))

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(IzSpacing.xl)
                .widthIn(max = 640.dp),
            verticalArrangement = Arrangement.spacedBy(IzSpacing.sm),
        ) {
            if (badge != null) badge()
            Text(title, style = IzType.Hero, color = IzColor.TextPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (description != null) {
                Text(description, style = IzType.Body, color = IzColor.TextSecondary, maxLines = 3, overflow = TextOverflow.Ellipsis)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(IzSpacing.sm), modifier = Modifier.padding(top = IzSpacing.xs)) {
                IzButton(text = "Assistir", onClick = onPlay, leadingIcon = Icons.Filled.PlayArrow)
                IzButton(text = "Detalhes", onClick = onDetails, style = IzButtonStyle.Secondary, leadingIcon = Icons.Filled.Info)
            }
        }
    }
}

@Preview(backgroundColor = 0xFF000000, showBackground = true, widthDp = 800, heightDp = 360)
@Composable
private fun IzHeroPreview() {
    IzPreviewSurface(padding = false) {
        IzHero(
            title = "A Casa das Sete Mulheres",
            description = "A saga de uma família durante a Revolução Farroupilha, entre guerra, paixões e resistência.",
            onPlay = {},
            onDetails = {},
            badge = { IzBadge("EM DESTAQUE", kind = IzBadgeKind.Info) },
        )
    }
}
