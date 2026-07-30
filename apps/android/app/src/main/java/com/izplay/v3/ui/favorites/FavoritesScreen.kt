package com.izplay.v3.ui.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.izplay.v3.ui.LocalFavoriteRepository
import com.izplay.v3.ui.dashboard.ImageKind
import androidx.compose.ui.res.stringResource
import com.izplay.v3.R
import com.izplay.v3.ui.dashboard.LiveStreamCard
import com.izplay.v3.ui.dashboard.PosterCard
import coil.compose.AsyncImage
import com.izplay.v3.data.FavoriteRepository
import kotlinx.coroutines.launch

/** Per-tab type for the segmented switcher. */
enum class FavoriteType(val key: String, val labelRes: Int, val icon: ImageVector) {
    LIVE("live", R.string.search_type_live, Icons.Default.LiveTv),
    VOD("vod", R.string.settings_vod_count, Icons.Default.Movie),
    SERIES("series", R.string.settings_series_count, Icons.Default.Tv);

    companion object {
        fun fromKey(key: String?): FavoriteType = entries.firstOrNull { it.key == key } ?: VOD
    }
}

/**
 * Kotlin port of iOS `FavoritesView`.
 *
 * Segmented switcher (Canlı / Film / Dizi) plus a grid for the active
 * segment. Each grid pulls a Flow from [com.izplay.v3.data.FavoriteRepository]
 * — Room auto-refreshes when the user toggles a favorite from any detail
 * screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    playlistId: String,
    initialType: String,
    onBack: () -> Unit,
    onOpenMovie: (Int) -> Unit,
    onOpenSeries: (Int) -> Unit,
    onPlayLive: (streamId: Int) -> Unit,
) {
    val favoriteRepository = LocalFavoriteRepository.current

    var selected by remember(initialType) { mutableStateOf(FavoriteType.fromKey(initialType)) }

    val liveFavs by favoriteRepository.observeLive(playlistId)
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val vodFavs by favoriteRepository.observeVod(playlistId)
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val seriesFavs by favoriteRepository.observeSeries(playlistId)
        .collectAsStateWithLifecycle(initialValue = emptyList())

    com.izplay.v3.ui.design.IzTheme {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.screen_favorites)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            FavoriteTypeSegmentedButtons(
                selected = selected,
                onSelect = { selected = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            )

            when (selected) {
                FavoriteType.LIVE -> {
                    // Layout de zapping do V2: lista de favoritos + prévia ao
                    // vivo. Mesmo vazio, a estrutura permanece (como no V2).
                    com.izplay.v3.ui.zapping.ZappingBody(
                        playlistId = playlistId,
                        categoryLabel = stringResource(R.string.screen_favorites),
                        channels = liveFavs,
                        onOpenFullscreen = onPlayLive,
                    )
                }
                FavoriteType.VOD -> {
                    if (vodFavs.isEmpty()) {
                        EmptyState(icon = Icons.Default.Movie, message = stringResource(R.string.empty_favorites_vod))
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(110.dp),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            items(vodFavs.size, key = { idx -> "vod_${vodFavs[idx].id}" }) { idx ->
                                val row = vodFavs[idx]
                                PosterCard(
                                    name = row.stream.name,
                                    coverUrl = row.stream.streamIcon,
                                    kind = ImageKind.Movie,
                                    onClick = { onOpenMovie(row.stream.streamId) },
                                )
                            }
                        }
                    }
                }
                FavoriteType.SERIES -> {
                    if (seriesFavs.isEmpty()) {
                        EmptyState(icon = Icons.Default.Tv, message = stringResource(R.string.empty_favorites_series))
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(110.dp),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            items(seriesFavs.size, key = { idx -> "series_${seriesFavs[idx].id}" }) { idx ->
                                val row = seriesFavs[idx]
                                PosterCard(
                                    name = row.series.name,
                                    coverUrl = row.series.cover,
                                    kind = ImageKind.Series,
                                    onClick = { onOpenSeries(row.series.seriesId) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FavoriteTypeSegmentedButtons(
    selected: FavoriteType,
    onSelect: (FavoriteType) -> Unit,
    modifier: Modifier = Modifier,
) {
    val all = FavoriteType.entries
    SingleChoiceSegmentedButtonRow(modifier = modifier) {
        all.forEachIndexed { index, type ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = index, count = all.size),
                selected = type == selected,
                onClick = { onSelect(type) },
                icon = { Icon(type.icon, contentDescription = null) },
                label = { Text(stringResource(type.labelRes)) },
            )
        }
    }
}

@Composable
private fun EmptyState(icon: ImageVector, message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(32.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(48.dp),
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private enum class UnifiedFilter(val label: String) {
    ALL("Todos"),
    VOD("Filmes"),
    LIVE("Canais"),
    SERIES("Séries"),
}

private data class UnifiedFavoriteItem(
    val id: Int,
    val title: String,
    val imageUrl: String?,
    val type: UnifiedFilter,
)

/**
 * Favoritos mobile em uma única tela. Faz parte do pager principal para que a
 * barra inferior continue visível e o usuário não precise procurar por tipo.
 */
@Composable
fun UnifiedFavoritesBody(
    playlistId: String,
    onOpenMovie: (Int) -> Unit,
    onOpenSeries: (Int) -> Unit,
    onPlayLive: (Int) -> Unit,
    onSearch: () -> Unit,
) {
    val repository = LocalFavoriteRepository.current
    val scope = rememberCoroutineScope()
    val live by repository.observeLive(playlistId).collectAsStateWithLifecycle(initialValue = emptyList())
    val vod by repository.observeVod(playlistId).collectAsStateWithLifecycle(initialValue = emptyList())
    val series by repository.observeSeries(playlistId).collectAsStateWithLifecycle(initialValue = emptyList())
    var filter by remember { mutableStateOf(UnifiedFilter.ALL) }

    val allItems = remember(live, vod, series) {
        buildList {
            addAll(vod.map { UnifiedFavoriteItem(it.stream.streamId, it.stream.name, it.stream.streamIcon, UnifiedFilter.VOD) })
            addAll(live.map { UnifiedFavoriteItem(it.stream.streamId, it.stream.name, it.stream.streamIcon, UnifiedFilter.LIVE) })
            addAll(series.map { UnifiedFavoriteItem(it.series.seriesId, it.series.name, it.series.cover, UnifiedFilter.SERIES) })
        }
    }
    val visible = remember(allItems, filter) {
        if (filter == UnifiedFilter.ALL) allItems else allItems.filter { it.type == filter }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        Text(
            "Favoritos",
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 18.dp, top = 18.dp),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            UnifiedFilter.entries.forEach { option ->
                val selected = option == filter
                Text(
                    option.label,
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (selected) Color(0xFF31070B) else Color(0xFF161616))
                        .border(
                            width = 1.dp,
                            color = if (selected) Color(0xFFFF172B) else Color(0xFF303030),
                            shape = RoundedCornerShape(9.dp),
                        )
                        .then(
                            if (selected) Modifier.background(
                                Color(0xFF31070B),
                                RoundedCornerShape(9.dp),
                            ) else Modifier,
                        )
                        .clickable { filter = option }
                        .padding(vertical = 12.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }
        Text(
            "${visible.size} ${if (visible.size == 1) "item salvo" else "itens salvos"}",
            color = Color(0xFFAAAAAA),
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp),
        )
        if (visible.isEmpty()) {
            EmptyState(Icons.Default.Star, "Nenhum favorito salvo neste filtro.")
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(visible.size, key = { index -> "${visible[index].type}_${visible[index].id}" }) { index ->
                    val item = visible[index]
                    UnifiedFavoriteCard(
                        item = item,
                        onClick = {
                            when (item.type) {
                                UnifiedFilter.VOD -> onOpenMovie(item.id)
                                UnifiedFilter.SERIES -> onOpenSeries(item.id)
                                UnifiedFilter.LIVE -> onPlayLive(item.id)
                                UnifiedFilter.ALL -> Unit
                            }
                        },
                        onRemove = {
                            scope.launch {
                                repository.setFavorite(
                                    streamId = item.id,
                                    playlistId = playlistId,
                                    type = when (item.type) {
                                        UnifiedFilter.VOD -> FavoriteRepository.Type.VOD
                                        UnifiedFilter.SERIES -> FavoriteRepository.Type.SERIES
                                        else -> FavoriteRepository.Type.LIVE
                                    },
                                    favorite = false,
                                )
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun UnifiedFavoriteCard(
    item: UnifiedFavoriteItem,
    onClick: () -> Unit,
    onRemove: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(142.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF151515))
            .clickable(onClick = onClick),
    ) {
        if (!item.imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.26f)))
        Text(
            when (item.type) {
                UnifiedFilter.VOD -> "Filme"
                UnifiedFilter.SERIES -> "Série"
                else -> "Canal"
            },
            color = Color.White,
            fontSize = 11.sp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(Color.Black.copy(alpha = 0.72f))
                .padding(horizontal = 8.dp, vertical = 4.dp),
        )
        IconButton(
            onClick = onRemove,
            modifier = Modifier.align(Alignment.TopEnd),
        ) {
            Icon(Icons.Default.Star, contentDescription = "Remover favorito", tint = Color(0xFFFF172B))
        }
        Text(
            item.title,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.58f))
                .padding(10.dp),
        )
    }
}
