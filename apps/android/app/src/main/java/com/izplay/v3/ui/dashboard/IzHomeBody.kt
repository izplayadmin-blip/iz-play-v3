package com.izplay.v3.ui.dashboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import coil.compose.rememberAsyncImagePainter
import com.izplay.v3.IZPlayApp
import com.izplay.v3.data.local.VodStreamWithCategory
import com.izplay.v3.ui.design.components.IzBadge
import com.izplay.v3.ui.design.components.IzBadgeKind
import com.izplay.v3.ui.design.components.IzContentRow
import com.izplay.v3.ui.design.components.IzHero
import com.izplay.v3.ui.design.components.IzPosterCard
import com.izplay.v3.ui.design.tokens.IzSpacing

/**
 * Aba "Início" da Home IZ Play (layout aprovado, fiel ao V2).
 *
 * Consome APENAS dados reais da arquitetura herdada do Another:
 *  - Hero: o filme adicionado mais recentemente pelo provedor
 *    (`vodStreamDao().recentlyAdded`, regra determinística documentada em
 *    `DESIGN_SYSTEM.md`; sem imagem → gradiente neutro; sem itens → sem Hero);
 *  - "Continuar assistindo": o shelf funcional existente (watchHistory);
 *  - "Filmes · Adicionados recentemente": os mesmos dados do Hero em fileira.
 *
 * Nenhum dado falso; fileiras vazias se ocultam.
 */
@Composable
fun IzHomeBody(
    playlistId: String,
    onOpenMovie: (Int) -> Unit,
    onResumeEpisode: (episodeId: String) -> Unit,
    onPlayLive: (streamId: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val app = context.applicationContext as IZPlayApp
    var recent by remember(playlistId) { mutableStateOf<List<VodStreamWithCategory>>(emptyList()) }
    LaunchedEffect(playlistId) {
        recent = runCatching {
            app.appDatabaseForDownloads.vodStreamDao().recentlyAdded(playlistId, 15)
        }.getOrDefault(emptyList())
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = IzSpacing.md, vertical = IzSpacing.md),
    ) {
        val heroItem = recent.firstOrNull()
        if (heroItem != null) {
            val backdrop = heroItem.stream.streamIcon
                ?.takeIf { it.isNotBlank() }
                ?.let { rememberAsyncImagePainter(it) }
            IzHero(
                title = heroItem.stream.name,
                description = heroItem.categoryName,
                backdrop = backdrop,
                onPlay = { onOpenMovie(heroItem.stream.streamId) },
                onDetails = { onOpenMovie(heroItem.stream.streamId) },
                badge = { IzBadge("NOVO", kind = IzBadgeKind.Live) },
            )
            Spacer(Modifier.height(IzSpacing.lg))
        }

        // Continuar assistindo — lógica existente do Another, intocada.
        ContinueWatchingShelf(
            playlistId = playlistId,
            onResumeMovie = onOpenMovie,
            onResumeSeries = onResumeEpisode,
            onPlayLive = onPlayLive,
        )

        if (recent.isNotEmpty()) {
            Spacer(Modifier.height(IzSpacing.lg))
            IzContentRow(
                title = "Filmes · Adicionados recentemente",
                items = recent,
            ) { row ->
                IzPosterCard(
                    title = row.stream.name,
                    image = row.stream.streamIcon
                        ?.takeIf { it.isNotBlank() }
                        ?.let { rememberAsyncImagePainter(it) },
                    onClick = { onOpenMovie(row.stream.streamId) },
                )
            }
        }
        Spacer(Modifier.height(IzSpacing.section))
    }
}
