package com.izplay.v3.ui.design.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.izplay.v3.ui.design.IzPreviewSurface
import com.izplay.v3.ui.design.focus.izFocusVisuals
import com.izplay.v3.ui.design.focus.rememberIzInteractionSource
import com.izplay.v3.ui.design.tokens.IzColor
import com.izplay.v3.ui.design.tokens.IzFocusScale
import com.izplay.v3.ui.design.tokens.IzMotion
import com.izplay.v3.ui.design.tokens.IzRadius
import com.izplay.v3.ui.design.tokens.IzSize
import com.izplay.v3.ui.design.tokens.IzSpacing
import com.izplay.v3.ui.design.tokens.IzType

/** Um destino de navegação da [IzSidebar]. */
data class IzNavDestination(val key: String, val label: String, val icon: ImageVector)

/**
 * Item de navegação da sidebar. Ícone sempre visível; rótulo aparece só quando
 * [expanded]. Estado selecionado usa a primária; foco escala 1.02 + borda
 * vermelha. Regra do V2: todo item interativo tem foco visível.
 */
@Composable
fun IzNavigationItem(
    destination: IzNavDestination,
    selected: Boolean,
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaction = rememberIzInteractionSource()
    val shape = RoundedCornerShape(IzRadius.md)
    val bg by animateColorAsState(
        targetValue = if (selected) IzColor.Primary else IzColor.Surface,
        animationSpec = tween(IzMotion.Fast),
        label = "izNavBg",
    )
    val fg = if (selected) IzColor.OnPrimary else IzColor.TextSecondary

    Row(
        modifier = modifier
            .height(48.dp)
            .izFocusVisuals(interaction, shape = shape, focusedScale = IzFocusScale.Sidebar)
            .clip(shape)
            .background(bg)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(IzSpacing.sm),
    ) {
        Icon(destination.icon, contentDescription = destination.label, tint = fg, modifier = Modifier.size(24.dp))
        if (expanded) {
            Text(destination.label, style = IzType.Body, color = fg, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

/**
 * Sidebar retrátil, **sempre à esquerda** (spec `sidebar.md`). Largura anima
 * entre 240dp (expandida) e 72dp (recolhida) em 250ms; padding 24dp.
 */
@Composable
fun IzSidebar(
    destinations: List<IzNavDestination>,
    selectedKey: String,
    expanded: Boolean,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val width by animateDpAsState(
        targetValue = if (expanded) IzSize.sidebarExpanded else IzSize.sidebarCollapsed,
        animationSpec = tween(IzMotion.Sidebar),
        label = "izSidebarWidth",
    )
    Column(
        modifier = modifier
            .width(width)
            .fillMaxHeight()
            .background(IzColor.Surface)
            .padding(vertical = IzSize.sidebarPadding, horizontal = IzSpacing.md),
        verticalArrangement = Arrangement.spacedBy(IzSpacing.xs),
    ) {
        destinations.forEach { dest ->
            IzNavigationItem(
                destination = dest,
                selected = dest.key == selectedKey,
                expanded = expanded,
                onClick = { onSelect(dest.key) },
                modifier = Modifier.width(if (expanded) IzSize.sidebarExpanded - IzSpacing.md * 2 else 48.dp),
            )
        }
        Spacer(Modifier.height(IzSpacing.md))
    }
}

private val previewDestinations = listOf(
    IzNavDestination("home", "Início", Icons.Filled.Home),
    IzNavDestination("live", "TV ao vivo", Icons.Filled.LiveTv),
    IzNavDestination("movies", "Filmes", Icons.Filled.Movie),
    IzNavDestination("series", "Séries", Icons.Filled.Tv),
    IzNavDestination("favorites", "Favoritos", Icons.Filled.Favorite),
    IzNavDestination("search", "Busca", Icons.Filled.Search),
)

@Preview(name = "Sidebar expandida", backgroundColor = 0xFF000000, showBackground = true, widthDp = 260, heightDp = 360)
@Composable
private fun IzSidebarExpandedPreview() {
    IzPreviewSurface(padding = false) {
        IzSidebar(previewDestinations, selectedKey = "live", expanded = true, onSelect = {})
    }
}

@Preview(name = "Sidebar recolhida", backgroundColor = 0xFF000000, showBackground = true, widthDp = 90, heightDp = 360)
@Composable
private fun IzSidebarCollapsedPreview() {
    IzPreviewSurface(padding = false) {
        IzSidebar(previewDestinations, selectedKey = "home", expanded = false, onSelect = {})
    }
}
