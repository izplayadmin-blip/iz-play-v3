package com.izplay.v3.ui.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import coil.compose.rememberAsyncImagePainter
import com.izplay.v3.IZPlayApp
import com.izplay.v3.R
import com.izplay.v3.data.local.LiveStreamWithCategory
import com.izplay.v3.data.local.SeriesWithCategory
import com.izplay.v3.data.local.VodStreamWithCategory
import com.izplay.v3.data.local.WatchHistoryEntity
import com.izplay.v3.ui.design.components.IzBadge
import com.izplay.v3.ui.design.components.IzBadgeKind
import com.izplay.v3.ui.design.components.IzChannelCard
import com.izplay.v3.ui.design.components.IzContentRow
import com.izplay.v3.ui.design.components.IzHero
import com.izplay.v3.ui.design.components.IzPosterCard
import com.izplay.v3.ui.design.tokens.IzSpacing
import com.izplay.v3.data.AdultContentFilter
import androidx.compose.ui.unit.dp

/**
 * Aba "Início" — composição oficial IZ Play: Hero sempre presente (cadeia de
 * fallback determinística abaixo, apenas dados reais) e fileiras que se
 * ocultam quando vazias.
 *
 * Cadeia do Hero (DESIGN_SYSTEM.md):
 *  1. filme recente com imagem → 2. série com capa → 3. continuar assistindo →
 *  4. filme recente sem imagem → 5. série sem capa → 6. canal do histórico →
 *  7. hero institucional da marca (sem conteúdo falso).
 */
@Composable
fun IzHomeBody(
    playlistId: String,
    onOpenMovie: (Int) -> Unit,
    onOpenSeries: (Int) -> Unit,
    onResumeEpisode: (episodeId: String) -> Unit,
    onPlayLive: (streamId: Int) -> Unit,
    onExploreCatalog: () -> Unit,
    seriesByCategory: Map<String, List<SeriesWithCategory>>,
    liveByCategory: Map<String, List<LiveStreamWithCategory>>,
    onOpenProfiles: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val app = context.applicationContext as IZPlayApp
    var recentMovies by remember(playlistId) { mutableStateOf<List<VodStreamWithCategory>>(emptyList()) }
    LaunchedEffect(playlistId) {
        recentMovies = runCatching {
            app.appDatabaseForDownloads.vodStreamDao().recentlyAdded(playlistId, 20)
        }.getOrDefault(emptyList())
    }
    val history by app.appDatabaseForDownloads.watchHistoryDao()
        .observeRecent(playlistId, limit = 20)
        .collectAsState(initial = emptyList())

    // Séries em ordem determinística: ordem das categorias do provedor.
    val seriesFlat = remember(seriesByCategory) { seriesByCategory.values.flatten() }
    val profileState by com.izplay.v3.ui.LocalProfileStore.current.state.collectAsState()
    val tastes = profileState.activeProfile?.genres.orEmpty()
    val personalizedMovies = remember(recentMovies, tastes) {
        recentMovies.sortedByDescending { row ->
            tastes.count { taste ->
                row.categoryName.contains(taste, ignoreCase = true) ||
                    row.stream.name.contains(taste, ignoreCase = true)
            }
        }
    }
    val personalizedSeries = remember(seriesFlat, tastes) {
        seriesFlat.sortedByDescending { row ->
            tastes.count { taste ->
                row.categoryName.contains(taste, ignoreCase = true) ||
                    row.series.name.contains(taste, ignoreCase = true)
            }
        }
    }
    val recentChannels = remember(history) { history.filter { it.type == "live" } }
    val mobile = LocalConfiguration.current.screenWidthDp < 600

    if (mobile) {
        MobileHomeContent(
            playlistId = playlistId,
            recentMovies = personalizedMovies.filterNot {
                AdultContentFilter.isAdultCategoryName(it.categoryName) ||
                    AdultContentFilter.isAdultCategoryName(it.stream.name)
            },
            seriesFlat = personalizedSeries.filterNot {
                AdultContentFilter.isAdultCategoryName(it.categoryName) ||
                    AdultContentFilter.isAdultCategoryName(it.series.name)
            },
            liveStreams = liveByCategory.values.flatten().filterNot {
                AdultContentFilter.isAdultCategoryName(it.categoryName) ||
                    AdultContentFilter.isAdultCategoryName(it.stream.name)
            },
            recentChannels = recentChannels,
            onOpenMovie = onOpenMovie,
            onOpenSeries = onOpenSeries,
            onResumeEpisode = onResumeEpisode,
            onPlayLive = onPlayLive,
            onSearch = onExploreCatalog,
            onOpenProfiles = onOpenProfiles,
            modifier = modifier,
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = IzSpacing.lg, vertical = IzSpacing.md),
    ) {
        HomeHero(
            recentMovies = personalizedMovies,
            seriesFlat = personalizedSeries,
            history = history,
            onOpenMovie = onOpenMovie,
            onOpenSeries = onOpenSeries,
            onResumeEpisode = onResumeEpisode,
            onPlayLive = onPlayLive,
            onExploreCatalog = onExploreCatalog,
        )
        Spacer(Modifier.height(IzSpacing.xl))

        // 1. Continuar assistindo — lógica existente do Another, intocada.
        ContinueWatchingShelf(
            playlistId = playlistId,
            onResumeMovie = onOpenMovie,
            onResumeSeries = onResumeEpisode,
            onPlayLive = onPlayLive,
        )

        // 2. Canais recentes (histórico ao vivo).
        if (recentChannels.isNotEmpty()) {
            Spacer(Modifier.height(IzSpacing.xl))
            IzContentRow(
                title = stringResource(R.string.row_recent_channels),
                kicker = stringResource(R.string.kicker_live),
                items = recentChannels,
            ) { row ->
                IzChannelCard(
                    name = row.title,
                    logo = row.imageUrl?.takeIf { it.isNotBlank() }
                        ?.let { rememberAsyncImagePainter(it) },
                    live = true,
                    onClick = { row.streamId.toIntOrNull()?.let(onPlayLive) },
                )
            }
        }

        // 3. Filmes adicionados recentemente.
        if (personalizedMovies.isNotEmpty()) {
            Spacer(Modifier.height(IzSpacing.xl))
            IzContentRow(
                title = stringResource(R.string.row_recent_movies),
                kicker = stringResource(R.string.kicker_foryou),
                items = personalizedMovies,
            ) { row ->
                IzPosterCard(
                    title = row.stream.name,
                    image = row.stream.streamIcon?.takeIf { it.isNotBlank() }
                        ?.let { rememberAsyncImagePainter(it) },
                    onClick = { onOpenMovie(row.stream.streamId) },
                )
            }
        }

        // 4. Séries em destaque (primeiras categorias do provedor).
        if (personalizedSeries.isNotEmpty()) {
            Spacer(Modifier.height(IzSpacing.xl))
            IzContentRow(
                title = stringResource(R.string.row_recent_series),
                items = personalizedSeries.take(20),
            ) { row ->
                IzPosterCard(
                    title = row.series.name,
                    image = row.series.cover?.takeIf { it.isNotBlank() }
                        ?.let { rememberAsyncImagePainter(it) },
                    onClick = { onOpenSeries(row.series.seriesId) },
                )
            }
        }

        // 5-6. Uma fileira por categoria de canais (primeiras 2 com conteúdo).
        liveByCategory.entries.take(2).forEach { (_, streams) ->
            if (streams.isNotEmpty()) {
                Spacer(Modifier.height(IzSpacing.xl))
                IzContentRow(
                    title = streams.first().categoryName,
                    items = streams.take(20),
                ) { row ->
                    IzChannelCard(
                        name = row.stream.name,
                        logo = row.stream.streamIcon?.takeIf { it.isNotBlank() }
                            ?.let { rememberAsyncImagePainter(it) },
                        onClick = { onPlayLive(row.stream.streamId) },
                    )
                }
            }
        }
        Spacer(Modifier.height(IzSpacing.section))
    }
}

@Composable
private fun MobileHomeContent(
    playlistId: String,
    recentMovies: List<VodStreamWithCategory>,
    seriesFlat: List<SeriesWithCategory>,
    liveStreams: List<LiveStreamWithCategory>,
    recentChannels: List<WatchHistoryEntity>,
    onOpenMovie: (Int) -> Unit,
    onOpenSeries: (Int) -> Unit,
    onResumeEpisode: (String) -> Unit,
    onPlayLive: (Int) -> Unit,
    onSearch: () -> Unit,
    onOpenProfiles: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 18.dp, bottom = 24.dp),
    ) {
        if (liveStreams.isNotEmpty()) {
            Spacer(Modifier.height(28.dp))
            IzContentRow(
                title = "Canais de TV",
                kicker = "VER TODOS  ›",
                items = liveStreams.take(12),
            ) { row ->
                IzChannelCard(
                    name = row.stream.name,
                    logo = row.stream.streamIcon?.takeIf { it.isNotBlank() }
                        ?.let { rememberAsyncImagePainter(it) },
                    live = true,
                    onClick = { onPlayLive(row.stream.streamId) },
                )
            }
        }

        if (recentChannels.isNotEmpty()) {
            Spacer(Modifier.height(28.dp))
            IzContentRow(
                title = "Histórico de canais",
                kicker = "VER TODOS  ›",
                items = recentChannels.take(10),
            ) { row ->
                IzChannelCard(
                    name = row.title,
                    logo = row.imageUrl?.takeIf { it.isNotBlank() }
                        ?.let { rememberAsyncImagePainter(it) },
                    onClick = { row.streamId.toIntOrNull()?.let(onPlayLive) },
                )
            }
        }

        Spacer(Modifier.height(28.dp))
        ContinueWatchingShelf(
            playlistId = playlistId,
            onResumeMovie = onOpenMovie,
            onResumeSeries = onResumeEpisode,
            onPlayLive = onPlayLive,
        )

        if (recentMovies.isNotEmpty()) {
            Spacer(Modifier.height(28.dp))
            IzContentRow(
                title = "Novos lançamentos",
                kicker = "VER TODOS  ›",
                items = recentMovies.take(20),
            ) { row ->
                IzPosterCard(
                    title = row.stream.name,
                    image = row.stream.streamIcon?.takeIf { it.isNotBlank() }
                        ?.let { rememberAsyncImagePainter(it) },
                    onClick = { onOpenMovie(row.stream.streamId) },
                )
            }
        }

        if (seriesFlat.isNotEmpty()) {
            Spacer(Modifier.height(28.dp))
            IzContentRow(
                title = "Séries em destaque",
                kicker = "VER TODOS  ›",
                items = seriesFlat.take(20),
            ) { row ->
                IzPosterCard(
                    title = row.series.name,
                    image = row.series.cover?.takeIf { it.isNotBlank() }
                        ?.let { rememberAsyncImagePainter(it) },
                    onClick = { onOpenSeries(row.series.seriesId) },
                )
            }
        }
    }
}

/** Hero com a cadeia de fallback determinística — só dados reais. */
@Composable
private fun HomeHero(
    recentMovies: List<VodStreamWithCategory>,
    seriesFlat: List<SeriesWithCategory>,
    history: List<WatchHistoryEntity>,
    onOpenMovie: (Int) -> Unit,
    onOpenSeries: (Int) -> Unit,
    onResumeEpisode: (String) -> Unit,
    onPlayLive: (Int) -> Unit,
    onExploreCatalog: () -> Unit,
) {
    val watch = stringResource(R.string.hero_watch)
    val details = stringResource(R.string.hero_details)
    val explore = stringResource(R.string.hero_explore)

    val movieWithArt = recentMovies.firstOrNull { !it.stream.streamIcon.isNullOrBlank() }
    val seriesWithArt = seriesFlat.firstOrNull { !it.series.cover.isNullOrBlank() }
    val resume = history.firstOrNull()
    val anyMovie = recentMovies.firstOrNull()
    val anySeries = seriesFlat.firstOrNull()
    val anyChannel = history.firstOrNull { it.type == "live" }

    when {
        movieWithArt != null -> IzHero(
            title = movieWithArt.stream.name,
            description = movieWithArt.categoryName,
            eyebrow = stringResource(R.string.home_kicker_new),
            poster = rememberAsyncImagePainter(movieWithArt.stream.streamIcon),
            onPlay = { onOpenMovie(movieWithArt.stream.streamId) },
            onDetails = { onOpenMovie(movieWithArt.stream.streamId) },
            playLabel = watch,
            detailsLabel = details,
            badge = { IzBadge("FILME", kind = IzBadgeKind.Info) },
        )
        seriesWithArt != null -> IzHero(
            title = seriesWithArt.series.name,
            description = seriesWithArt.series.plot ?: seriesWithArt.categoryName,
            eyebrow = stringResource(R.string.home_kicker_new),
            poster = rememberAsyncImagePainter(seriesWithArt.series.cover),
            onPlay = { onOpenSeries(seriesWithArt.series.seriesId) },
            onDetails = { onOpenSeries(seriesWithArt.series.seriesId) },
            playLabel = watch,
            detailsLabel = details,
            badge = { IzBadge("SÉRIE", kind = IzBadgeKind.Info) },
        )
        resume != null -> IzHero(
            title = resume.title,
            description = resume.secondaryTitle,
            eyebrow = stringResource(R.string.home_kicker_continue),
            poster = resume.imageUrl?.takeIf { it.isNotBlank() }
                ?.let { rememberAsyncImagePainter(it) },
            onPlay = {
                when (resume.type) {
                    "live" -> resume.streamId.toIntOrNull()?.let(onPlayLive)
                    "series" -> onResumeEpisode(resume.streamId)
                    else -> resume.streamId.toIntOrNull()?.let(onOpenMovie)
                }
            },
            onDetails = onExploreCatalog,
            playLabel = watch,
            detailsLabel = explore,
        )
        anyMovie != null -> IzHero(
            title = anyMovie.stream.name,
            description = anyMovie.categoryName,
            eyebrow = stringResource(R.string.home_kicker_new),
            onPlay = { onOpenMovie(anyMovie.stream.streamId) },
            onDetails = { onOpenMovie(anyMovie.stream.streamId) },
            playLabel = watch,
            detailsLabel = details,
        )
        anySeries != null -> IzHero(
            title = anySeries.series.name,
            description = anySeries.categoryName,
            eyebrow = stringResource(R.string.home_kicker_new),
            onPlay = { onOpenSeries(anySeries.series.seriesId) },
            onDetails = { onOpenSeries(anySeries.series.seriesId) },
            playLabel = watch,
            detailsLabel = details,
        )
        anyChannel != null -> IzHero(
            title = anyChannel.title,
            description = anyChannel.secondaryTitle,
            eyebrow = stringResource(R.string.kicker_live),
            poster = anyChannel.imageUrl?.takeIf { it.isNotBlank() }
                ?.let { rememberAsyncImagePainter(it) },
            onPlay = { anyChannel.streamId.toIntOrNull()?.let(onPlayLive) },
            onDetails = onExploreCatalog,
            playLabel = watch,
            detailsLabel = explore,
        )
        else -> IzHero(
            // 7. Hero institucional da marca — sem conteúdo falso.
            title = stringResource(R.string.hero_brand_title),
            description = stringResource(R.string.hero_brand_desc),
            eyebrow = stringResource(R.string.home_kicker_brand),
            onPlay = null,
            onDetails = onExploreCatalog,
            playLabel = watch,
            detailsLabel = explore,
        )
    }
}
