package com.izplay.v3.ui.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import com.izplay.v3.ui.design.IzPreviewSurface
import com.izplay.v3.ui.design.focus.izFocusVisuals
import com.izplay.v3.ui.design.focus.rememberIzInteractionSource
import com.izplay.v3.ui.design.tokens.IzColor
import com.izplay.v3.ui.design.tokens.IzGradient
import com.izplay.v3.ui.design.tokens.IzRadius
import com.izplay.v3.ui.design.tokens.IzSize
import com.izplay.v3.ui.design.tokens.IzSpacing
import com.izplay.v3.ui.design.tokens.IzType

/**
 * Card base focável do IZ Play. Proporção 16:9 (spec `movie-card.md`), raio
 * 18dp, foco escala 1.08 com borda vermelha. Usado para thumbnails paisagem
 * (canais em destaque, capítulos). [image] nulo cai no placeholder.
 */
@Composable
fun IzTvCard(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    image: Painter? = null,
    subtitle: String? = null,
    aspectRatio: Float = 16f / 9f,
    badge: (@Composable () -> Unit)? = null,
    progress: Float? = null,
) {
    val mobile = LocalConfiguration.current.screenWidthDp < 600
    val interaction = rememberIzInteractionSource()
    val shape = RoundedCornerShape(IzRadius.card)

    Column(modifier = modifier.width(if (mobile) 156.dp else IzSize.cardWidth)) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(aspectRatio)
                .izFocusVisuals(interaction, shape = shape)
                .clip(shape)
                .background(IzColor.Surface2)
                .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        ) {
            IzCardImage(image, title)
            if (badge != null) {
                Box(Modifier.align(Alignment.TopStart).padding(IzSpacing.xs)) { badge() }
            }
            if (progress != null) {
                Box(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = IzSpacing.xs, vertical = IzSpacing.xs),
                ) { IzProgressBar(progress = progress) }
            }
        }
        Text(
            title,
            style = IzType.Card.copy(fontSize = if (mobile) 13.sp else IzType.Card.fontSize),
            color = IzColor.TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = IzSpacing.xs),
        )
        if (subtitle != null) {
            Text(
                subtitle,
                style = IzType.Description,
                color = IzColor.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * Card de pôster retrato (2:3), para filmes e séries. Mesmo comportamento de
 * foco do [IzTvCard], só muda a proporção e a largura base.
 */
@Composable
fun IzPosterCard(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    image: Painter? = null,
    badge: (@Composable () -> Unit)? = null,
) {
    val mobile = LocalConfiguration.current.screenWidthDp < 600
    val interaction = rememberIzInteractionSource()
    val shape = RoundedCornerShape(IzRadius.card)

    Column(modifier = modifier.width(if (mobile) 112.dp else IzSize.posterWidth)) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(2f / 3f)
                .izFocusVisuals(interaction, shape = shape)
                .clip(shape)
                .background(IzColor.Surface2)
                .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        ) {
            IzCardImage(image, title)
            if (badge != null) {
                Box(Modifier.align(Alignment.TopStart).padding(IzSpacing.xs)) { badge() }
            }
        }
        Text(
            title,
            style = IzType.Card.copy(fontSize = if (mobile) 13.sp else IzType.Card.fontSize),
            color = IzColor.TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = IzSpacing.xs),
        )
    }
}

/**
 * Card de canal ao vivo (quadrado), logo centralizado sobre superfície, com
 * badge AO VIVO opcional no canto.
 */
@Composable
fun IzChannelCard(
    name: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    logo: Painter? = null,
    live: Boolean = false,
) {
    val mobile = LocalConfiguration.current.screenWidthDp < 600
    val interaction = rememberIzInteractionSource()
    val shape = RoundedCornerShape(IzRadius.card)

    val channelSize = if (mobile) 108.dp else IzSize.channelSize
    Column(modifier = modifier.width(channelSize)) {
        Box(
            Modifier
                .size(channelSize)
                .izFocusVisuals(interaction, shape = shape)
                .clip(shape)
                .background(IzColor.Surface2)
                .clickable(interactionSource = interaction, indication = null, onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            if (logo != null) {
                Image(
                    logo,
                    contentDescription = name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.padding(if (mobile) 12.dp else IzSpacing.md).fillMaxSize(),
                )
            } else {
                Text(name.take(3).uppercase(), style = IzType.Subtitle, color = IzColor.TextSecondary)
            }
            if (live) {
                Box(Modifier.align(Alignment.TopStart).padding(IzSpacing.xs)) {
                    IzBadge("AO VIVO", kind = IzBadgeKind.Live)
                }
            }
        }
        Text(
            name,
            style = IzType.Description.copy(fontSize = if (mobile) 12.sp else IzType.Description.fontSize),
            color = IzColor.TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = IzSpacing.xs),
        )
    }
}

/** Imagem do card ou placeholder gradiente quando ausente. */
@Composable
private fun IzCardImage(image: Painter?, contentDescription: String) {
    if (image != null) {
        Image(
            painter = image,
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
    } else {
        Box(Modifier.fillMaxSize().background(IzGradient.SurfacePlaceholder))
    }
}

@Preview(backgroundColor = 0xFF000000, showBackground = true, widthDp = 640, heightDp = 320)
@Composable
private fun IzCardsPreview() {
    IzPreviewSurface {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            IzTvCard(
                title = "Globo Centro-Oeste",
                subtitle = "Agora: Jornal Nacional",
                onClick = {},
                progress = 0.6f,
                badge = { IzBadge("AO VIVO", kind = IzBadgeKind.Live) },
            )
            IzPosterCard(title = "A Casa das Sete Mulheres", onClick = {}, badge = { IzBadge("4K", kind = IzBadgeKind.Info) })
            IzChannelCard(name = "Premiere Clube", onClick = {}, live = true)
        }
    }
}
