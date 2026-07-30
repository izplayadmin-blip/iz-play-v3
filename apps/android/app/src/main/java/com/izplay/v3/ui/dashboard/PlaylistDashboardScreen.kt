package com.izplay.v3.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.material.icons.filled.StarBorder
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
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalConfiguration
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
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
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
    onOpenProfiles: () -> Unit = {},
) {
    val repository = LocalPlaylistRepository.current
    val store = LocalPlaylistContentStore.current
    val scope = rememberCoroutineScope()
    val isMobile = LocalConfiguration.current.screenWidthDp < 600

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
    if (!isMobile) {
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
    }
    Scaffold(
        bottomBar = {
            if (isMobile) {
                NavigationBar(containerColor = Color(0xFF090909)) {
                    val selectedAccent = Color(0xFFE52235)
                    NavigationBarItem(
                        selected = pagerState.currentPage == PAGE_HOME,
                        onClick = { scope.launch { pagerState.animateScrollToPage(PAGE_HOME) } },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Início") },
                        label = { Text("Início", maxLines = 1, fontSize = 10.sp) },
                        colors = mobileNavigationColors(selectedAccent),
                    )
                    NavigationBarItem(
                        selected = pagerState.currentPage == PAGE_FAVORITES,
                        onClick = { scope.launch { pagerState.animateScrollToPage(PAGE_FAVORITES) } },
                        icon = { Icon(Icons.Default.StarBorder, contentDescription = "Favoritos") },
                        label = { Text("Favoritos", maxLines = 1, fontSize = 10.sp) },
                        colors = mobileNavigationColors(selectedAccent),
                    )
                    val catalogItems = listOf(
                        Triple(0, Icons.Default.LiveTv, "Canais de TV"),
                        Triple(1, Icons.Default.Movie, "Filmes"),
                        Triple(2, Icons.Default.Tv, "Séries de TV"),
                    )
                    catalogItems.forEach { (page, icon, label) ->
                        NavigationBarItem(
                            selected = pagerState.currentPage == page,
                            onClick = { scope.launch { pagerState.animateScrollToPage(page) } },
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label, maxLines = 1, fontSize = 10.sp) },
                            colors = mobileNavigationColors(selectedAccent),
                        )
                    }
                }
            }
        },
        topBar = {
            if (isMobile) {
                Surface(color = Color.Black) {
                    Box(
                        Modifier
                            .statusBarsPadding()
                            .padding(horizontal = 18.dp, vertical = 6.dp),
                    ) {
                        MobileIzTopBar(
                            onSearch = onOpenSearch,
                            onProfile = onOpenProfiles,
                        )
                    }
                }
            }
            // A Home (Início) não tem top bar: ela é o destino principal —
            // sidebar indica a seção e o Hero é o elemento dominante. As
            // outras abas mantêm a barra (voltar/busca/categorias/refresh).
            if (!isMobile && pagerState.currentPage != PAGE_HOME && pagerState.currentPage != 0) {
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
                        0 -> if (isMobile) {
                            MobileLiveTab(
                                playlistId = playlistId,
                                categories = liveCats,
                                byCategoryId = liveByCategory,
                                selectedCategoryId = liveZapCategoryId,
                                onSearch = onOpenSearch,
                                onPickCategory = { pickerType = "live" },
                                onPlayChannel = onPlayLive,
                            )
                        } else {
                            LiveTabBody(
                                playlistId = playlistId,
                                categories = liveCats,
                                byCategoryId = liveByCategory,
                                zapCategoryId = liveZapCategoryId,
                                onPickCategory = { pickerType = "live" },
                                onSelectZapCategory = { liveZapCategoryId = it },
                                onPlayChannel = onPlayLive,
                            )
                        }
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
                            onOpenSearch = onOpenSearch,
                            onOpenCategories = { pickerType = "vod" },
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
                            onOpenSearch = onOpenSearch,
                            onOpenCategories = { pickerType = "series" },
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
                            onOpenProfiles = onOpenProfiles,
                        )
                        PAGE_FAVORITES -> com.izplay.v3.ui.favorites.UnifiedFavoritesBody(
                            playlistId = playlistId,
                            onOpenMovie = onOpenMovie,
                            onOpenSeries = onOpenSeries,
                            onPlayLive = onPlayLive,
                            onSearch = onOpenSearch,
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
private const val PAGE_FAVORITES = 6
private const val TAB_COUNT = 7

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

@Composable
private fun mobileNavigationColors(selectedAccent: Color) =
    androidx.compose.material3.NavigationBarItemDefaults.colors(
        selectedIconColor = selectedAccent,
        selectedTextColor = selectedAccent,
        indicatorColor = Color.Transparent,
        unselectedIconColor = Color(0xFF8C8C8C),
        unselectedTextColor = Color(0xFF8C8C8C),
    )

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
    onSelectZapCategory: (String) -> Unit,
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
    // FAVORITOS entra como pseudocategoria no topo do painel (padrão V2).
    val liveFavs by com.izplay.v3.ui.LocalFavoriteRepository.current
        .observeLive(playlistId)
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val favLabel = stringResource(com.izplay.v3.R.string.screen_favorites)
    val isFav = zapCategoryId == FAV_CATEGORY_ID
    com.izplay.v3.ui.zapping.ZappingBody(
        playlistId = playlistId,
        categoryLabel = if (isFav) favLabel else current.name,
        channels = if (isFav) liveFavs else byCategoryId[current.id].orEmpty(),
        onOpenFullscreen = onPlayChannel,
        categories = listOf(FAV_CATEGORY_ID to favLabel) +
            visibleCategories.map { it.id to it.name },
        categoryCounts = byCategoryId.mapValues { it.value.size } +
            mapOf(FAV_CATEGORY_ID to liveFavs.size),
        selectedCategoryId = if (isFav) FAV_CATEGORY_ID else current.id,
        onSelectCategory = onSelectZapCategory,
    )
}

/** Pseudocategoria "Favoritos" do painel de categorias do zapping. */
private const val FAV_CATEGORY_ID = "__izplay_favorites__"

@Composable
private fun MobileLiveTab(
    playlistId: String,
    categories: List<CategoryEntity>,
    byCategoryId: Map<String, List<LiveStreamWithCategory>>,
    selectedCategoryId: String?,
    onSearch: () -> Unit,
    onPickCategory: () -> Unit,
    onPlayChannel: (Int) -> Unit,
) {
    val favoriteRepository = com.izplay.v3.ui.LocalFavoriteRepository.current
    val favoriteChannels by favoriteRepository.observeLive(playlistId)
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val favoriteIds = remember(favoriteChannels) {
        favoriteChannels.mapTo(hashSetOf()) { it.stream.streamId }
    }
    val scope = rememberCoroutineScope()
    val selected = categories.firstOrNull { it.id == selectedCategoryId }
    val streams = remember(byCategoryId, selectedCategoryId) {
        if (selectedCategoryId == null) {
            byCategoryId.values.flatten()
        } else {
            byCategoryId[selectedCategoryId].orEmpty()
        }
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentPadding = PaddingValues(bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "live-mobile-header") {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 14.dp),
            ) {
                Spacer(Modifier.height(26.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Canais de TV",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                    )
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF181818))
                            .clickable(onClick = onPickCategory)
                            .padding(horizontal = 18.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            selected?.name ?: "Todos",
                            color = Color.White,
                            fontSize = 16.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Categorias", tint = Color.White)
                    }
                }
            }
        }
        items(streams, key = { "mobile_live_${it.stream.streamId}" }) { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF171717))
                    .clickable { onPlayChannel(row.stream.streamId) }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(74.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF1F1F1)),
                    contentAlignment = Alignment.Center,
                ) {
                    if (!row.stream.streamIcon.isNullOrBlank()) {
                        AsyncImage(
                            model = row.stream.streamIcon,
                            contentDescription = row.stream.name,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize().padding(8.dp),
                        )
                    } else {
                        Text(
                            row.stream.name.take(3).uppercase(),
                            color = Color(0xFF222222),
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        row.stream.name,
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Sem informações sobre o programa atual",
                        color = Color(0xFF999999),
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                IconButton(
                    onClick = {
                        scope.launch {
                            favoriteRepository.toggle(
                                streamId = row.stream.streamId,
                                playlistId = playlistId,
                                type = com.izplay.v3.data.FavoriteRepository.Type.LIVE,
                            )
                        }
                    },
                ) {
                    val favorite = row.stream.streamId in favoriteIds
                    Icon(
                        imageVector = if (favorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = if (favorite) "Remover dos favoritos" else "Adicionar aos favoritos",
                        tint = if (favorite) Color(0xFFFF172B) else Color(0xFFAAAAAA),
                        modifier = Modifier.size(28.dp),
                    )
                }
            }
        }
    }
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
    onOpenSearch: () -> Unit = {},
    onOpenCategories: () -> Unit = {},
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
        contentPadding = PaddingValues(bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "mobile-header") {
            if (LocalConfiguration.current.screenWidthDp < 600) {
                MobileCatalogHeader(
                    title = "Filmes",
                    onSearch = onOpenSearch,
                    onOpenCategories = onOpenCategories,
                )
            }
        }
        item(key = "continue-watching") {
            ContinueWatchingShelf(
                playlistId = playlistId,
                onResumeMovie = onResumeMovie,
                onResumeSeries = onResumeEpisode,
                onPlayLive = onPlayLive,
                contentType = "vod",
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
    onOpenSearch: () -> Unit = {},
    onOpenCategories: () -> Unit = {},
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
        contentPadding = PaddingValues(bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "mobile-header") {
            if (LocalConfiguration.current.screenWidthDp < 600) {
                MobileCatalogHeader(
                    title = "Séries",
                    onSearch = onOpenSearch,
                    onOpenCategories = onOpenCategories,
                )
            }
        }
        item(key = "continue-watching") {
            ContinueWatchingShelf(
                playlistId = playlistId,
                onResumeMovie = onResumeMovie,
                onResumeSeries = onResumeEpisode,
                onPlayLive = onPlayLive,
                contentType = "series",
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

@Composable
private fun MobileCatalogHeader(
    title: String,
    onSearch: () -> Unit,
    onOpenCategories: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 20.dp),
    ) {
        Spacer(Modifier.height(28.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                title,
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF171717))
                    .clickable(onClick = onOpenCategories)
                    .padding(horizontal = 18.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("Categorias", color = Color.White, fontSize = 16.sp)
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = "Abrir categorias",
                    tint = Color.White,
                )
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
