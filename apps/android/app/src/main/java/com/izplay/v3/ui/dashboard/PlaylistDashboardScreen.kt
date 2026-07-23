package com.izplay.v3.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.izplay.v3.data.local.CategoryEntity
import com.izplay.v3.data.local.LiveStreamWithCategory
import com.izplay.v3.data.local.SeriesWithCategory
import com.izplay.v3.data.local.VodStreamWithCategory
import com.izplay.v3.model.Playlist
import com.izplay.v3.ui.LocalPlaylistContentStore
import com.izplay.v3.ui.LocalPlaylistRepository
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import com.izplay.v3.ui.design.IzTheme
import com.izplay.v3.ui.design.components.IzNavDestination
import com.izplay.v3.ui.design.components.IzSidebar
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Top-level catalog screen for a playlist — three tabs (Live TV, Movies,
 * Series), each a vertical scroll of categories, each category a
 * horizontal shelf of cards.
 *
 * iOS counterpart: `DashboardView` + `LiveStreamsView` / `VODView` /
 * `SeriesView`. The Android layout collapses those into one screen because
 * the inner views are almost entirely shelf-of-shelves and don't earn
 * their own files until the category-detail / search views land.
 *
 * Bottom NavigationBar swaps the [HorizontalPager] page so swiping
 * sideways and tapping the bar agree on the active tab.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDashboardScreen(
    playlistId: String,
    onBack: () -> Unit,
    onOpenMovie: (Int) -> Unit,
    onOpenSeries: (Int) -> Unit,
    onOpenLiveCategory: (String) -> Unit,
    onOpenVodCategory: (String) -> Unit,
    onOpenSeriesCategory: (String) -> Unit,
    onOpenFavorites: (String) -> Unit,
    onPlayLive: (streamId: Int) -> Unit,
    appVersion: String,
    // Optional callbacks that surface Downloads / Watch History from the
    // settings tab. Default to no-op so the existing call sites in tests /
    // previews keep working.
    onOpenDownloads: () -> Unit = {},
    onOpenHistory: () -> Unit = {},
    onOpenSearch: () -> Unit = {},
    onResumeEpisode: (episodeId: String) -> Unit = {},
) {
    val repository = LocalPlaylistRepository.current
    val store = LocalPlaylistContentStore.current
    val scope = rememberCoroutineScope()

    var playlist by remember(playlistId) { mutableStateOf<Playlist?>(null) }
    LaunchedEffect(playlistId) {
        playlist = repository.find(playlistId)
        playlist?.let { store.loadPlaylistSuspending(it) }
    }

    val isLoading by store.isLoading.collectAsStateWithLifecycle()
    val loadingMessage by store.loadingMessage.collectAsStateWithLifecycle()
    val loadError by store.loadError.collectAsStateWithLifecycle()
    val streamsLoaded by store.streamsLoaded.collectAsStateWithLifecycle()
    val liveCats by store.liveCategories.collectAsStateWithLifecycle()
    val vodCats by store.vodCategories.collectAsStateWithLifecycle()
    val seriesCats by store.seriesCategories.collectAsStateWithLifecycle()
    val liveByCategory by store.liveStreamsByCategoryId.collectAsStateWithLifecycle()
    val vodByCategory by store.vodStreamsByCategoryId.collectAsStateWithLifecycle()
    val seriesByCategory by store.seriesItemsByCategoryId.collectAsStateWithLifecycle()

    // A Home (Início) é a página inicial — layout IZ Play aprovado.
    val pagerState = rememberPagerState(initialPage = PAGE_HOME) { TAB_COUNT }
    // Picker sheet — opened from the list icon on content tabs. Holds the
    // type ("live" / "vod" / "series") of the active tab so the sheet's
    // hide/unhide writes the right namespace.
    val pickerSheetState = rememberModalBottomSheetState()
    var pickerType by remember { mutableStateOf<String?>(null) }

    // Categoria ativa do zapping de canais (persistente por playlist).
    var liveZapCategoryId by rememberSaveable(playlistId) { mutableStateOf<String?>(null) }

    // Relógio real da sidebar (V2 mostra HH:mm na base da faixa vermelha).
    var clock by remember { mutableStateOf(clockNow()) }
    LaunchedEffect(Unit) {
        while (true) {
            clock = clockNow()
            delay(30_000)
        }
    }

    IzTheme {
    Row(Modifier.fillMaxSize()) {
    IzSidebar(
        destinations = listOf(
            IzNavDestination("home", stringResource(com.izplay.v3.R.string.screen_home), Icons.Default.Home),
            IzNavDestination("favorites", stringResource(com.izplay.v3.R.string.screen_favorites), Icons.Default.Favorite),
            IzNavDestination("live", stringResource(com.izplay.v3.R.string.screen_live_tv), Icons.Default.LiveTv),
            IzNavDestination("movies", stringResource(com.izplay.v3.R.string.screen_movies), Icons.Default.Movie),
            IzNavDestination("series", stringResource(com.izplay.v3.R.string.screen_series), Icons.Default.Tv),
        ),
        bottomDestinations = listOf(
            IzNavDestination("settings", stringResource(com.izplay.v3.R.string.screen_settings), Icons.Default.Settings),
            IzNavDestination("search", stringResource(com.izplay.v3.R.string.screen_search), Icons.Default.Search),
        ),
        selectedKey = when (pagerState.currentPage) {
            PAGE_HOME -> "home"
            0 -> "live"
            1 -> "movies"
            2 -> "series"
            3 -> "settings"
            else -> "search"
        },
        clock = clock,
        onSelect = { key ->
            when (key) {
                // Favoritos abre a tela existente; o tipo segue a aba de
                // conteúdo ativa (comportamento da estrela da top bar) e cai
                // em "live" quando se está na Home/Configuração/Busca.
                "favorites" -> onOpenFavorites(
                    if (pagerState.currentPage < CONTENT_TAB_LIMIT) {
                        tabTypeFor(pagerState.currentPage)
                    } else {
                        "live"
                    },
                )
                else -> {
                    val target = when (key) {
                        "home" -> PAGE_HOME
                        "live" -> 0
                        "movies" -> 1
                        "series" -> 2
                        "settings" -> 3
                        else -> 4
                    }
                    scope.launch { pagerState.animateScrollToPage(target) }
                }
            }
        },
    )
    Scaffold(
        topBar = {
            // A Home (Início) não tem top bar: ela é o destino principal —
            // sidebar indica a seção e o Hero é o elemento dominante. As
            // outras abas mantêm a barra (voltar/busca/categorias/refresh).
            if (pagerState.currentPage != PAGE_HOME) {
            TopAppBar(
                title = {
                    val tabTitleId = TAB_TITLE_IDS.getOrNull(pagerState.currentPage)
                    Text(
                        text = tabTitleId?.let { androidx.compose.ui.res.stringResource(it) }
                            ?: stringResource(com.izplay.v3.R.string.screen_playlists),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                    // Search is available on every tab.
                    IconButton(onClick = onOpenSearch) {
                        Icon(Icons.Default.Search, contentDescription = "Ara")
                    }
                    // Content-tab actions: jump to a category + favorites +
                    // refresh. The Settings tab has its own "İçeriği yeniden
                    // indir" row, so these are hidden there.
                    if (pagerState.currentPage < CONTENT_TAB_LIMIT) {
                        val typeForCurrentTab = tabTypeFor(pagerState.currentPage)
                        IconButton(onClick = { pickerType = typeForCurrentTab }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.FormatListBulleted,
                                contentDescription = "Kategoriler",
                            )
                        }
                        IconButton(onClick = { onOpenFavorites(typeForCurrentTab) }) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Favoriler",
                                tint = Color(0xFFFFC107),
                            )
                        }
                        IconButton(
                            enabled = playlist != null && !isLoading,
                            onClick = {
                                val current = playlist ?: return@IconButton
                                scope.launch {
                                    store.syncFromNetworkReplacingLocal(current) {}
                                    store.reloadFromDatabase(current.id)
                                }
                            },
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Yenile")
                        }
                    }
                },
            )
            }
        },
    ) { innerPadding ->
        when {
            // First-time bootstrap with no categories yet: show the
            // overall spinner + progress message instead of three empty
            // tabs.
            loadError != null && liveCats.isEmpty() && vodCats.isEmpty() && seriesCats.isEmpty() ->
                ErrorState(
                    modifier = Modifier.padding(innerPadding),
                    message = loadError ?: "Bilinmeyen hata",
                    onRetry = {
                        val current = playlist ?: return@ErrorState
                        scope.launch { store.loadPlaylistSuspending(current) }
                    },
                )
            isLoading && liveCats.isEmpty() && vodCats.isEmpty() && seriesCats.isEmpty() ->
                LoadingState(
                    modifier = Modifier.padding(innerPadding),
                    message = loadingMessage,
                )
            else ->
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    // Tab geçişi sadece alt bar'daki tıklamayla; swipe ile
                    // kazara yan tab'a kaymak istemiyoruz. animateScrollToPage
                    // bu bayrak false iken de çalışmaya devam eder.
                    userScrollEnabled = false,
                ) { page ->
                    when (page) {
                        0 -> LiveTabBody(
                            playlistId = playlistId,
                            categories = liveCats,
                            byCategoryId = liveByCategory,
                            zapCategoryId = liveZapCategoryId,
                            onPickCategory = { pickerType = "live" },
                            onPlayChannel = onPlayLive,
                        )
                        1 -> MoviesTabBody(
                            playlistId = playlistId,
                            categories = vodCats,
                            byCategoryId = vodByCategory,
                            streamsLoading = !streamsLoaded,
                            onOpenMovie = onOpenMovie,
                            onOpenCategory = onOpenVodCategory,
                            onResumeMovie = onOpenMovie,
                            onResumeEpisode = onResumeEpisode,
                            onPlayLive = onPlayLive,
                        )
                        2 -> SeriesTabBody(
                            playlistId = playlistId,
                            categories = seriesCats,
                            byCategoryId = seriesByCategory,
                            streamsLoading = !streamsLoaded,
                            onOpenSeries = onOpenSeries,
                            onOpenCategory = onOpenSeriesCategory,
                            onResumeMovie = onOpenMovie,
                            onResumeEpisode = onResumeEpisode,
                            onPlayLive = onPlayLive,
                        )
                        3 -> com.izplay.v3.ui.settings.PlaylistSettingsBody(
                            playlistId = playlistId,
                            appVersion = appVersion,
                            onOpenDownloads = onOpenDownloads,
                            onOpenHistory = onOpenHistory,
                            modifier = Modifier.fillMaxSize(),
                        )
                        4 -> com.izplay.v3.ui.search.SearchBody(
                            playlistId = playlistId,
                            onOpenMovie = onOpenMovie,
                            onOpenSeries = onOpenSeries,
                            onPlayLive = onPlayLive,
                            modifier = Modifier.fillMaxSize(),
                        )
                        PAGE_HOME -> IzHomeBody(
                            playlistId = playlistId,
                            onOpenMovie = onOpenMovie,
                            onOpenSeries = onOpenSeries,
                            onResumeEpisode = onResumeEpisode,
                            onPlayLive = onPlayLive,
                            onExploreCatalog = {
                                scope.launch { pagerState.animateScrollToPage(1) }
                            },
                            seriesByCategory = seriesByCategory,
                            liveByCategory = liveByCategory,
                        )
                    }
                }
        }
    }

    val activePickerType = pickerType
    if (activePickerType != null) {
        // Pick the categories + per-category counts for whichever type the
        // top-bar action exposed. The sheet reads its own hide state via
        // CompositionLocal, so no extra plumbing is needed here.
        val (categoriesForSheet, countsForSheet) = when (activePickerType) {
            "live" -> liveCats to liveByCategory.mapValues { it.value.size }
            "vod" -> vodCats to vodByCategory.mapValues { it.value.size }
            else -> seriesCats to seriesByCategory.mapValues { it.value.size }
        }
        CategoryPickerSheet(
            title = pickerTitleFor(activePickerType),
            playlistId = playlistId,
            contentType = activePickerType,
            categories = categoriesForSheet,
            itemCountsByCategoryId = countsForSheet,
            sheetState = pickerSheetState,
            onSelect = { categoryId ->
                // Na aba de canais, escolher categoria troca a lista do
                // zapping (layout V2). Nas demais, apenas fecha a sheet.
                if (activePickerType == "live") liveZapCategoryId = categoryId
                scope.launch { pickerSheetState.hide() }.invokeOnCompletion {
                    pickerType = null
                }
            },
            onDismiss = { pickerType = null },
        )
    }
    } // Row (sidebar + conteúdo)
    } // IzTheme
}

/** Página da Home (Início) no pager — depois das abas herdadas. */
private const val PAGE_HOME = 5
private const val TAB_COUNT = 6

/** Relógio HH:mm da sidebar (java.time coberto pelo desugaring no API 24/25). */
private fun clockNow(): String =
    LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))

// Tab titles resolved via stringResource at compose time — see TAB_TITLE_IDS.
private val TAB_TITLE_IDS = intArrayOf(
    com.izplay.v3.R.string.screen_live_tv,
    com.izplay.v3.R.string.screen_movies,
    com.izplay.v3.R.string.screen_series,
    com.izplay.v3.R.string.screen_settings,
    com.izplay.v3.R.string.screen_search,
    com.izplay.v3.R.string.screen_home,
)
// Settings (3) and Search (4) hide the content-tab top-bar actions.
private const val CONTENT_TAB_LIMIT = 3

/** Index → iOS-compatible type discriminator. */
private fun tabTypeFor(page: Int): String = when (page) {
    0 -> "live"
    1 -> "vod"
    else -> "series"
}

/** Per-type display string for the picker sheet header. */
@Composable
private fun pickerTitleFor(type: String): String = stringResource(
    when (type) {
        "live" -> com.izplay.v3.R.string.picker_live_categories
        "vod" -> com.izplay.v3.R.string.picker_vod_categories
        else -> com.izplay.v3.R.string.picker_series_categories
    },
)

// A bottom bar herdada do Another foi substituída pela IzSidebar vermelha
// (layout IZ Play aprovado). A navegação continua sendo o mesmo pager.

// ---- Tab bodies ----

@Composable
private fun LiveTabBody(
    playlistId: String,
    categories: List<CategoryEntity>,
    byCategoryId: Map<String, List<LiveStreamWithCategory>>,
    zapCategoryId: String?,
    onPickCategory: () -> Unit,
    onPlayChannel: (Int) -> Unit,
) {
    // Layout de zapping do V2: lista da categoria à esquerda + prévia ao vivo
    // à direita. A categoria vem do seletor (ícone de lista / cabeçalho).
    val hiddenStore = com.izplay.v3.ui.LocalHiddenCategoryStore.current
    val hiddenIds by hiddenStore.observeHidden(playlistId, "live")
        .collectAsStateWithLifecycle(initialValue = hiddenStore.hiddenIds(playlistId, "live"))
    val visibleCategories = remember(categories, hiddenIds) {
        categories.filter { it.id !in hiddenIds }
    }
    if (visibleCategories.isEmpty()) {
        EmptyTab(message = if (categories.isEmpty()) stringResource(com.izplay.v3.R.string.empty_no_categories) else stringResource(com.izplay.v3.R.string.empty_categories_hidden))
        return
    }
    val current = visibleCategories.firstOrNull { it.id == zapCategoryId }
        ?: visibleCategories.first()
    com.izplay.v3.ui.zapping.ZappingBody(
        playlistId = playlistId,
        categoryLabel = current.name,
        channels = byCategoryId[current.id].orEmpty(),
        onOpenFullscreen = onPlayChannel,
        onPickCategory = onPickCategory,
    )
}

@Composable
private fun MoviesTabBody(
    playlistId: String,
    categories: List<CategoryEntity>,
    byCategoryId: Map<String, List<VodStreamWithCategory>>,
    streamsLoading: Boolean,
    onOpenMovie: (Int) -> Unit,
    onOpenCategory: (String) -> Unit,
    onResumeMovie: (Int) -> Unit = {},
    onResumeEpisode: (String) -> Unit = {},
    onPlayLive: (Int) -> Unit = {},
) {
    val hiddenStore = com.izplay.v3.ui.LocalHiddenCategoryStore.current
    val hiddenIds by hiddenStore.observeHidden(playlistId, "vod")
        .collectAsStateWithLifecycle(initialValue = hiddenStore.hiddenIds(playlistId, "vod"))
    val visibleCategories = remember(categories, hiddenIds) {
        categories.filter { it.id !in hiddenIds }
    }
    if (visibleCategories.isEmpty()) {
        EmptyTab(message = if (categories.isEmpty()) stringResource(com.izplay.v3.R.string.empty_no_categories) else stringResource(com.izplay.v3.R.string.empty_categories_hidden))
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "continue-watching") {
            ContinueWatchingShelf(
                playlistId = playlistId,
                onResumeMovie = onResumeMovie,
                onResumeSeries = onResumeEpisode,
                onPlayLive = onPlayLive,
            )
        }
        item(key = "recently-added") {
            RecentlyAddedShelf(
                playlistId = playlistId,
                onOpenMovie = onOpenMovie,
            )
        }
        items(visibleCategories, key = { "vod_${it.id}" }) { category ->
            val items = byCategoryId[category.id].orEmpty()
            CategoryShelf(
                title = category.name,
                itemCount = items.size,
                streamsLoading = streamsLoading,
                onHeaderClick = { onOpenCategory(category.id) },
            ) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(items, key = { it.id }) { row ->
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
    }
}

@Composable
private fun SeriesTabBody(
    playlistId: String,
    categories: List<CategoryEntity>,
    byCategoryId: Map<String, List<SeriesWithCategory>>,
    streamsLoading: Boolean,
    onOpenSeries: (Int) -> Unit,
    onOpenCategory: (String) -> Unit,
    onResumeMovie: (Int) -> Unit = {},
    onResumeEpisode: (String) -> Unit = {},
    onPlayLive: (Int) -> Unit = {},
) {
    val hiddenStore = com.izplay.v3.ui.LocalHiddenCategoryStore.current
    val hiddenIds by hiddenStore.observeHidden(playlistId, "series")
        .collectAsStateWithLifecycle(initialValue = hiddenStore.hiddenIds(playlistId, "series"))
    val visibleCategories = remember(categories, hiddenIds) {
        categories.filter { it.id !in hiddenIds }
    }
    if (visibleCategories.isEmpty()) {
        EmptyTab(message = if (categories.isEmpty()) stringResource(com.izplay.v3.R.string.empty_no_categories) else stringResource(com.izplay.v3.R.string.empty_categories_hidden))
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "continue-watching") {
            ContinueWatchingShelf(
                playlistId = playlistId,
                onResumeMovie = onResumeMovie,
                onResumeSeries = onResumeEpisode,
                onPlayLive = onPlayLive,
            )
        }
        items(visibleCategories, key = { "series_${it.id}" }) { category ->
            val items = byCategoryId[category.id].orEmpty()
            CategoryShelf(
                title = category.name,
                itemCount = items.size,
                streamsLoading = streamsLoading,
                onHeaderClick = { onOpenCategory(category.id) },
            ) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(items, key = { it.id }) { row ->
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

// ---- State views ----

@Composable
private fun EmptyTab(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier, message: String?) {
    // Tela de espera da marca (fiel ao LoadingScreen do V2 Android).
    com.izplay.v3.ui.design.components.IzLoadingScreen(
        modifier = modifier,
        message = message,
    )
}

@Composable
private fun ErrorState(modifier: Modifier = Modifier, message: String, onRetry: () -> Unit) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp),
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp),
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("Tekrar dene")
            }
        }
    }
}
