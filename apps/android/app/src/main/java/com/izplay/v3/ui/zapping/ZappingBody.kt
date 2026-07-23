package com.izplay.v3.ui.zapping

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.izplay.v3.R
import com.izplay.v3.data.PlaybackUrlBuilder
import com.izplay.v3.data.local.LiveStreamWithCategory
import com.izplay.v3.model.Playlist
import com.izplay.v3.player.MPVPlayer
import com.izplay.v3.player.MPVSurfaceView
import com.izplay.v3.ui.LocalFavoriteRepository
import com.izplay.v3.ui.LocalPlaylistRepository
import com.izplay.v3.ui.design.focus.izFocusVisuals
import com.izplay.v3.ui.design.focus.rememberIzInteractionSource
import com.izplay.v3.ui.design.tokens.IzColor
import com.izplay.v3.ui.design.tokens.IzRadius
import com.izplay.v3.ui.design.tokens.IzSpacing
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

/**
 * Tela de zapping do IZ Play (layout do V2): lista de canais numerada à
 * esquerda (cabeçalho de categoria + busca + contagem) e, à direita, a
 * **prévia ao vivo** do canal selecionado num [MPVPlayer] próprio, com as
 * ações FAVORITO · RECONECTAR · TELA CHEIA · INFO e a seção PROGRAMAÇÃO.
 *
 * Reutilizável: Favoritos passa a lista de favoritos; Canais passa a lista da
 * categoria. O player da prévia é descartado no dispose — não interfere no
 * player de tela cheia (rota própria).
 */
@Composable
fun ZappingBody(
    playlistId: String,
    categoryLabel: String,
    channels: List<LiveStreamWithCategory>,
    onOpenFullscreen: (streamId: Int) -> Unit,
    modifier: Modifier = Modifier,
    onPickCategory: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val playlistRepository = LocalPlaylistRepository.current
    val favoriteRepository = LocalFavoriteRepository.current
    val scope = rememberCoroutineScope()

    var playlist by remember(playlistId) { mutableStateOf<Playlist?>(null) }
    LaunchedEffect(playlistId) { playlist = playlistRepository.find(playlistId) }

    var query by remember { mutableStateOf("") }
    val filtered = remember(channels, query) {
        if (query.isBlank()) channels
        else channels.filter { it.stream.name.contains(query, ignoreCase = true) }
    }

    var selected by remember(playlistId) { mutableStateOf<LiveStreamWithCategory?>(null) }
    LaunchedEffect(filtered) {
        if (selected == null || filtered.none { it.id == selected?.id }) {
            selected = filtered.firstOrNull()
        }
    }

    // Player exclusivo da prévia — criado uma vez, descartado no dispose.
    val previewPlayer = remember {
        runCatching { MPVPlayer(context.applicationContext as Application) }.getOrNull()
    }
    DisposableEffect(Unit) {
        onDispose { previewPlayer?.dispose() }
    }
    // Troca de canal => loadfile de baixa latência na prévia.
    LaunchedEffect(selected?.id, playlist?.id) {
        val pl = playlist ?: return@LaunchedEffect
        val ch = selected ?: return@LaunchedEffect
        previewPlayer?.load(
            PlaybackUrlBuilder(pl).liveUrl(ch.stream.streamId),
            play = true,
            liveLowLatency = true,
        )
    }

    var isFavorite by remember { mutableStateOf(false) }
    LaunchedEffect(selected?.id) {
        val ch = selected ?: return@LaunchedEffect
        favoriteRepository.observeIsFavorite(ch.stream.streamId, playlistId, "live")
            .collect { isFavorite = it }
    }

    Row(modifier = modifier.fillMaxSize().background(IzColor.Background)) {
        // ===== Painel esquerdo: categoria + busca + lista =====
        Column(
            Modifier
                .width(340.dp)
                .fillMaxHeight()
                .background(IzColor.BackgroundDeep)
                .padding(horizontal = IzSpacing.md, vertical = IzSpacing.md),
        ) {
            Text(
                stringResource(R.string.zap_category).uppercase(),
                color = IzColor.TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
            )
            Text(
                categoryLabel.uppercase(),
                color = IzColor.Primary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = if (onPickCategory != null) {
                    Modifier.clickable(onClick = onPickCategory)
                } else {
                    Modifier
                },
            )
            Spacer(Modifier.height(IzSpacing.sm))

            // Busca
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(IzRadius.md))
                    .background(IzColor.Surface2)
                    .padding(horizontal = IzSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.Search, null, tint = IzColor.Muted, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(IzSpacing.xs))
                BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    textStyle = TextStyle(color = IzColor.TextPrimary, fontSize = 14.sp),
                    cursorBrush = SolidColor(IzColor.Primary),
                    decorationBox = { inner ->
                        if (query.isEmpty()) {
                            Text(
                                stringResource(R.string.zap_search_channel),
                                color = IzColor.Muted,
                                fontSize = 14.sp,
                            )
                        }
                        inner()
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Spacer(Modifier.height(IzSpacing.sm))

            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(filtered, key = { it.id }) { row ->
                    ChannelRow(
                        number = filtered.indexOf(row) + 1,
                        row = row,
                        selected = row.id == selected?.id,
                        onClick = { selected = row },
                    )
                }
            }

            Text(
                stringResource(R.string.zap_channels_count, filtered.size),
                color = IzColor.Muted,
                fontSize = 12.sp,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = IzSpacing.xs),
            )
        }

        // ===== Painel direito: prévia + ações + programação =====
        Column(
            Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(IzSpacing.md),
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(IzRadius.md))
                    .background(Color.Black),
            ) {
                if (previewPlayer != null) {
                    MPVSurfaceView(player = previewPlayer)
                }
            }
            Spacer(Modifier.height(IzSpacing.sm))
            Text(
                selected?.stream?.name ?: "—",
                color = IzColor.TextPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(IzSpacing.sm))
            Row(horizontalArrangement = Arrangement.spacedBy(IzSpacing.sm)) {
                ZapAction(
                    if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    stringResource(R.string.zap_favorite),
                    highlighted = isFavorite,
                ) {
                    val ch = selected ?: return@ZapAction
                    scope.launch {
                        runCatching { favoriteRepository.toggle(ch.stream.streamId, playlistId, "live") }
                    }
                }
                ZapAction(Icons.Filled.Refresh, stringResource(R.string.zap_reconnect)) {
                    val pl = playlist ?: return@ZapAction
                    val ch = selected ?: return@ZapAction
                    previewPlayer?.load(
                        PlaybackUrlBuilder(pl).liveUrl(ch.stream.streamId),
                        play = true,
                        liveLowLatency = true,
                    )
                }
                ZapAction(Icons.Filled.Fullscreen, stringResource(R.string.zap_fullscreen)) {
                    selected?.let { onOpenFullscreen(it.stream.streamId) }
                }
                ZapAction(Icons.Filled.Info, stringResource(R.string.zap_info)) { /* categoria abaixo */ }
            }
            Spacer(Modifier.height(IzSpacing.md))
            Text(
                stringResource(R.string.zap_epg).uppercase(),
                color = IzColor.TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                selected?.categoryName ?: stringResource(R.string.zap_epg_none),
                color = IzColor.TextSecondary,
                fontSize = 14.sp,
            )
            Text(
                stringResource(R.string.zap_epg_none),
                color = IzColor.Muted,
                fontSize = 13.sp,
            )
        }
    }
}

@Composable
private fun ChannelRow(
    number: Int,
    row: LiveStreamWithCategory,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val interaction = rememberIzInteractionSource()
    val shape = RoundedCornerShape(IzRadius.sm)
    Row(
        Modifier
            .fillMaxWidth()
            .height(52.dp)
            .izFocusVisuals(interaction, shape = shape, focusedScale = 1.02f)
            .clip(shape)
            .background(if (selected) IzColor.RowSelected else Color.Transparent)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = IzSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            number.toString(),
            color = IzColor.TextSecondary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(34.dp),
        )
        Box(
            Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(IzColor.Surface2),
            contentAlignment = Alignment.Center,
        ) {
            row.stream.streamIcon?.takeIf { it.isNotBlank() }?.let {
                AsyncImage(model = it, contentDescription = null, modifier = Modifier.fillMaxSize())
            }
        }
        Spacer(Modifier.width(IzSpacing.sm))
        Text(
            row.stream.name,
            color = if (selected) IzColor.TextPrimary else IzColor.TextSecondary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ZapAction(
    icon: ImageVector,
    label: String,
    highlighted: Boolean = false,
    onClick: () -> Unit,
) {
    val interaction = rememberIzInteractionSource()
    val shape = RoundedCornerShape(IzRadius.md)
    Column(
        Modifier
            .width(170.dp)
            .height(64.dp)
            .izFocusVisuals(interaction, shape = shape, focusedScale = 1.03f)
            .clip(shape)
            .background(IzColor.Surface2)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = if (highlighted) IzColor.Live else IzColor.TextPrimary,
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.height(4.dp))
        Text(
            label.uppercase(),
            color = IzColor.TextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp,
        )
    }
}
