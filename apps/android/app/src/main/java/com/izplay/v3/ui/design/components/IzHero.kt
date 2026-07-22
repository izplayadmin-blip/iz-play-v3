package com.izplay.v3.ui.design.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.izplay.v3.ui.design.IzPreviewSurface
import com.izplay.v3.ui.design.tokens.IzColor
import com.izplay.v3.ui.design.tokens.IzGradient
import com.izplay.v3.ui.design.tokens.IzRadius
import com.izplay.v3.ui.design.tokens.IzSize
import com.izplay.v3.ui.design.tokens.IzSpacing
import com.izplay.v3.ui.design.tokens.IzType

/**
 * Hero do IZ Play (estrutura oficial do V2): texto à esquerda com kicker
 * amarelo, título grande branco, descrição cinza, badges e ações; imagem à
 * direita — [backdrop] preenche o fundo com scrim; sem backdrop, o [poster]
 * (2:3) fica à direita sobre fundo preto com gradiente vermelho discreto.
 * Sem imagem nenhuma, a estrutura permanece (nunca um retângulo vazio).
 *
 * Rotação/indicadores ficam com quem compõe a Home; este componente renderiza
 * UM destaque.
 */
@Composable
fun IzHero(
    title: String,
    onPlay: (() -> Unit)?,
    onDetails: () -> Unit,
    playLabel: String,
    detailsLabel: String,
    modifier: Modifier = Modifier,
    eyebrow: String? = null,
    backdrop: Painter? = null,
    poster: Painter? = null,
    description: String? = null,
    badge: (@Composable () -> Unit)? = null,
) {
    val shape = RoundedCornerShape(IzRadius.hero)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(IzSize.heroHeight)
            .clip(shape)
            .background(
                // Fundo preto com glow vermelho MUITO discreto (pré-mesclado,
                // sem alpha em gradiente — TV boxes fracas fazem banding).
                Brush.linearGradient(
                    listOf(Color(0xFF120202), IzColor.Background, Color(0xFF1A0303)),
                ),
            ),
    ) {
        if (backdrop != null) {
            Image(
                backdrop,
                contentDescription = title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Box(Modifier.fillMaxSize().background(IzGradient.ScrimStart))
            Box(Modifier.fillMaxSize().background(IzGradient.ScrimBottom))
        }

        if (backdrop == null && poster != null) {
            Box(
                Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = IzSpacing.xl)
                    .fillMaxHeight(0.82f)
                    .aspectRatio(2f / 3f)
                    .clip(RoundedCornerShape(IzRadius.md))
                    .background(IzColor.Surface2),
            ) {
                Image(
                    poster,
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = IzSpacing.xl, top = IzSpacing.lg, bottom = IzSpacing.lg)
                .widthIn(max = 680.dp),
            verticalArrangement = Arrangement.spacedBy(IzSpacing.sm),
        ) {
            if (eyebrow != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(IzSpacing.sm),
                ) {
                    Box(
                        Modifier
                            .width(30.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(IzRadius.pill))
                            .background(IzColor.Live),
                    )
                    Text(
                        eyebrow.uppercase(),
                        color = IzColor.Live,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        letterSpacing = 3.sp,
                    )
                }
            }
            Text(
                title,
                style = IzType.Hero.copy(fontSize = 44.sp),
                color = IzColor.TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (description != null) {
                Text(
                    description,
                    style = IzType.Body.copy(fontSize = 18.sp),
                    color = IzColor.TextSecondary,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 560.dp),
                )
            }
            if (badge != null) badge()
            Row(
                horizontalArrangement = Arrangement.spacedBy(IzSpacing.sm),
                modifier = Modifier.padding(top = IzSpacing.xs),
            ) {
                if (onPlay != null) {
                    IzButton(text = playLabel, onClick = onPlay, leadingIcon = Icons.Filled.PlayArrow)
                }
                IzButton(
                    text = detailsLabel,
                    onClick = onDetails,
                    style = IzButtonStyle.Secondary,
                    leadingIcon = Icons.Filled.Info,
                )
            }
        }
    }
}

@Preview(backgroundColor = 0xFF0A0A0A, showBackground = true, widthDp = 900, heightDp = 400)
@Composable
private fun IzHeroPreview() {
    IzPreviewSurface(padding = false) {
        IzHero(
            title = "A Casa das Sete Mulheres",
            description = "A saga de uma família durante a Revolução Farroupilha.",
            eyebrow = "Novo no IZ Play",
            onPlay = {},
            onDetails = {},
            playLabel = "Assistir",
            detailsLabel = "Mais detalhes",
            badge = { IzBadge("FILMES", kind = IzBadgeKind.Info) },
        )
    }
}
