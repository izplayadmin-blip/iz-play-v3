package com.izplay.v3.debug

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.focusable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.izplay.v3.ui.design.IzTheme
import com.izplay.v3.ui.design.components.IzBadge
import com.izplay.v3.ui.design.components.IzBadgeKind
import com.izplay.v3.ui.design.components.IzButton
import com.izplay.v3.ui.design.components.IzButtonStyle
import com.izplay.v3.ui.design.components.IzChannelCard
import com.izplay.v3.ui.design.components.IzContentRow
import com.izplay.v3.ui.design.components.IzEmptyState
import com.izplay.v3.ui.design.components.IzErrorState
import com.izplay.v3.ui.design.components.IzHero
import com.izplay.v3.ui.design.components.IzLoading
import com.izplay.v3.ui.design.components.IzNavDestination
import com.izplay.v3.ui.design.components.IzPosterCard
import com.izplay.v3.ui.design.components.IzProgressBar
import com.izplay.v3.ui.design.components.IzSidebar
import com.izplay.v3.ui.design.components.IzTvCard
import com.izplay.v3.ui.design.tokens.IzColor
import com.izplay.v3.ui.design.tokens.IzSpacing
import com.izplay.v3.ui.design.tokens.IzType

/**
 * Galeria de preview do design system IZ Play V3 (debug-only).
 *
 * Exercita todos os componentes `Iz*` juntos, em dispositivo real, para
 * inspeção visual e teste de foco por D-pad. Não faz parte do app funcional.
 */
class IzGalleryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Prova de foco: pede foco num botão ao abrir, para o realce (escala
            // + borda vermelha) aparecer no screenshot mesmo com o box em touch
            // mode (que suprimiria o foco vindo de eventos adb de D-pad).
            val focusProof = remember { FocusRequester() }
            LaunchedEffect(Unit) { runCatching { focusProof.requestFocus() } }
            IzTheme {
                Row(Modifier.fillMaxSize().background(IzColor.Background)) {
                    IzSidebar(
                        destinations = Destinations,
                        selectedKey = "live",
                        expanded = true,
                        onSelect = {},
                    )
                    Column(
                        Modifier
                            .padding(IzSpacing.lg)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(IzSpacing.lg),
                    ) {
                        Section("Hero") {
                            IzHero(
                                title = "A Casa das Sete Mulheres",
                                description = "A saga de uma família na Revolução Farroupilha.",
                                onPlay = {}, onDetails = {},
                                badge = { IzBadge("EM DESTAQUE", kind = IzBadgeKind.Info) },
                            )
                        }
                        Section("Botões (o primeiro recebe foco na abertura)") {
                            Row(horizontalArrangement = Arrangement.spacedBy(IzSpacing.md)) {
                                IzButton("Assistir", onClick = {})
                                IzButton("Detalhes", onClick = {}, style = IzButtonStyle.Secondary, modifier = Modifier.focusRequester(focusProof))
                                IzButton("Indisponível", onClick = {}, enabled = false)
                            }
                        }
                        Section("Badges") {
                            Row(horizontalArrangement = Arrangement.spacedBy(IzSpacing.sm)) {
                                IzBadge("AO VIVO", kind = IzBadgeKind.Live)
                                IzBadge("4K", kind = IzBadgeKind.Info)
                                IzBadge("HD", kind = IzBadgeKind.Info)
                                IzBadge("ALERTA", kind = IzBadgeKind.Warning)
                            }
                        }
                        Section("Fileira de cards") {
                            IzContentRow(
                                title = "Filmes / Lançamentos",
                                items = listOf("Um Dia", "Vermiglio", "Barba Ensopada", "Heartstopper", "Menudas"),
                                onSeeMore = {},
                            ) { name -> IzPosterCard(title = name, onClick = {}) }
                        }
                        Section("Cards") {
                            Row(horizontalArrangement = Arrangement.spacedBy(IzSpacing.md)) {
                                IzTvCard(
                                    title = "Globo Centro-Oeste",
                                    subtitle = "Agora: Jornal Nacional",
                                    onClick = {},
                                    progress = 0.6f,
                                    badge = { IzBadge("AO VIVO", kind = IzBadgeKind.Live) },
                                )
                                IzChannelCard(name = "Premiere Clube", onClick = {}, live = true)
                            }
                        }
                        Section("Progresso") {
                            Column(Modifier.width(240.dp)) { IzProgressBar(progress = 0.42f) }
                        }
                        Section("Estados") {
                            Row(horizontalArrangement = Arrangement.spacedBy(IzSpacing.xl)) {
                                IzLoading(message = "Carregando…")
                                IzEmptyState(title = "Sem favoritos", description = "Adicione conteúdos.", modifier = Modifier.width(260.dp))
                                IzErrorState(title = "Falha ao carregar", onRetry = {}, modifier = Modifier.width(260.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(IzSpacing.sm)) {
        Text(title, style = IzType.Category, color = IzColor.Live)
        content()
    }
}

private val Destinations = listOf(
    IzNavDestination("home", "Início", Icons.Filled.Home),
    IzNavDestination("live", "TV ao vivo", Icons.Filled.LiveTv),
    IzNavDestination("movies", "Filmes", Icons.Filled.Movie),
    IzNavDestination("favorites", "Favoritos", Icons.Filled.Favorite),
    IzNavDestination("search", "Busca", Icons.Filled.Search),
)
