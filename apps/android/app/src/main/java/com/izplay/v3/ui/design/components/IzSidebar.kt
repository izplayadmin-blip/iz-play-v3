package com.izplay.v3.ui.design.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.izplay.v3.ui.design.IzPreviewSurface
import com.izplay.v3.ui.design.focus.rememberIzInteractionSource
import com.izplay.v3.ui.design.tokens.IzColor
import com.izplay.v3.ui.design.tokens.IzFocusScale
import com.izplay.v3.ui.design.tokens.IzMotion
import com.izplay.v3.ui.design.tokens.IzSize

/** Um destino de navegação da [IzSidebar]. */
data class IzNavDestination(val key: String, val label: String, val icon: ImageVector)

/**
 * Sidebar vermelha do IZ Play — reimplementação fiel dos parâmetros do
 * `Sidebar.kt` do IZ Play V2 Android:
 *
 *  - recolhida **78dp** / expandida **214dp**, anima em 180ms;
 *  - **expande quando o foco do D-pad entra na sidebar** e recolhe quando sai
 *    ou quando um item é selecionado (mesmo comportamento retrátil do V2);
 *  - gradiente vertical `#E00000 → #990000`, "IZ" 22sp Black no topo,
 *    divisor 44×1dp branco 22%;
 *  - item 188/62×54dp, raio 10dp, ícone 28dp, rótulo MAIÚSCULO 12sp Black;
 *  - selecionado = overlay preto 34% · focado = overlay 12% + borda branca
 *    2dp @78% + escala 1.08 (130ms);
 *  - área inferior para destinos secundários + relógio 11sp.
 *
 * Consome apenas os destinos passados pelo chamador — nenhuma navegação
 * paralela; quem roteia é a navegação existente do app.
 */
@Composable
fun IzSidebar(
    destinations: List<IzNavDestination>,
    selectedKey: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    bottomDestinations: List<IzNavDestination> = emptyList(),
    clock: String? = null,
    expandedOverride: Boolean? = null,
) {
    var hasFocus by remember { mutableStateOf(false) }
    var forceCollapsed by remember { mutableStateOf(false) }
    val expanded = expandedOverride ?: (hasFocus && !forceCollapsed)
    val width by animateDpAsState(
        targetValue = if (expanded) IzSize.sidebarExpanded else IzSize.sidebarCollapsed,
        animationSpec = tween(IzMotion.Sidebar),
        label = "izSidebarWidth",
    )

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(width)
            .background(Brush.verticalGradient(listOf(IzColor.RedBright, IzColor.RedDark)))
            .focusGroup()
            .onFocusChanged {
                hasFocus = it.hasFocus
                if (!it.hasFocus) forceCollapsed = false
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(IzSize.sidebarLogoArea),
            contentAlignment = Alignment.Center,
        ) {
            Text("IZ", fontSize = 22.sp, fontWeight = FontWeight.Black, color = IzColor.TextPrimary)
        }
        Box(
            Modifier
                .width(44.dp)
                .height(1.dp)
                .background(Color.White.copy(alpha = 0.22f)),
        )
        Spacer(Modifier.height(12.dp))

        destinations.forEach { dest ->
            IzNavigationItem(
                destination = dest,
                selected = dest.key == selectedKey,
                expanded = expanded,
                onFocus = { forceCollapsed = false },
                onClick = {
                    forceCollapsed = true
                    onSelect(dest.key)
                },
            )
            Spacer(Modifier.height(6.dp))
        }

        Spacer(Modifier.weight(1f))

        bottomDestinations.forEach { dest ->
            IzNavigationItem(
                destination = dest,
                selected = dest.key == selectedKey,
                expanded = expanded,
                onFocus = { forceCollapsed = false },
                onClick = {
                    forceCollapsed = true
                    onSelect(dest.key)
                },
            )
            Spacer(Modifier.height(6.dp))
        }

        if (clock != null) {
            Text(
                clock,
                color = IzColor.TextPrimary.copy(alpha = 0.82f),
                fontWeight = FontWeight.Black,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp),
            )
        }
    }
}

/**
 * Item da sidebar, com os estados do V2: selecionado (overlay preto 34%),
 * focado (overlay 12% + borda branca 78% + escala 1.08) e repouso.
 */
@Composable
fun IzNavigationItem(
    destination: IzNavDestination,
    selected: Boolean,
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onFocus: () -> Unit = {},
) {
    val interaction = rememberIzInteractionSource()
    val focused by interaction.collectIsFocusedAsState()
    val scale by animateFloatAsState(
        targetValue = if (focused) IzFocusScale.Sidebar else 1f,
        animationSpec = tween(IzMotion.SidebarFocus),
        label = "izNavScale",
    )
    val shape = RoundedCornerShape(10.dp)
    val bg = when {
        selected -> IzColor.SidebarSelectedOverlay
        focused -> Color.Black.copy(alpha = 0.12f)
        else -> Color.Transparent
    }
    val contentAlpha = if (focused || selected) 1f else 0.72f

    Row(
        modifier = modifier
            .width(if (expanded) IzSize.sidebarItemExpanded else IzSize.sidebarItemCollapsed)
            .height(IzSize.sidebarItemHeight)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(shape)
            .background(bg)
            .then(
                if (focused) {
                    Modifier.border(2.dp, Color.White.copy(alpha = 0.78f), shape)
                } else {
                    Modifier
                },
            )
            .onFocusChanged { if (it.isFocused) onFocus() }
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = if (expanded) 17.dp else 0.dp),
        horizontalArrangement = if (expanded) Arrangement.Start else Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            destination.icon,
            contentDescription = destination.label,
            tint = IzColor.TextPrimary.copy(alpha = contentAlpha),
            modifier = Modifier.size(IzSize.sidebarIcon),
        )
        if (expanded) {
            Spacer(Modifier.width(14.dp))
            Text(
                destination.label.uppercase(),
                color = IzColor.TextPrimary.copy(alpha = if (focused || selected) 1f else 0.82f),
                fontWeight = FontWeight.Black,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private val previewDestinations = listOf(
    IzNavDestination("home", "Início", Icons.Filled.Home),
    IzNavDestination("favorites", "Favoritos", Icons.Filled.Favorite),
    IzNavDestination("live", "Canais de TV", Icons.Filled.LiveTv),
    IzNavDestination("movies", "Filmes", Icons.Filled.Movie),
    IzNavDestination("series", "Séries", Icons.Filled.Tv),
)

private val previewBottom = listOf(
    IzNavDestination("settings", "Configuração", Icons.Filled.Settings),
    IzNavDestination("search", "Busca", Icons.Filled.Search),
)

@Preview(name = "Sidebar expandida", backgroundColor = 0xFF0A0A0A, showBackground = true, widthDp = 240, heightDp = 480)
@Composable
private fun IzSidebarExpandedPreview() {
    IzPreviewSurface(padding = false) {
        IzSidebar(
            destinations = previewDestinations,
            bottomDestinations = previewBottom,
            selectedKey = "home",
            clock = "10:00",
            expandedOverride = true,
            onSelect = {},
        )
    }
}

@Preview(name = "Sidebar recolhida", backgroundColor = 0xFF0A0A0A, showBackground = true, widthDp = 100, heightDp = 480)
@Composable
private fun IzSidebarCollapsedPreview() {
    IzPreviewSurface(padding = false) {
        IzSidebar(
            destinations = previewDestinations,
            bottomDestinations = previewBottom,
            selectedKey = "home",
            clock = "10:00",
            expandedOverride = false,
            onSelect = {},
        )
    }
}
