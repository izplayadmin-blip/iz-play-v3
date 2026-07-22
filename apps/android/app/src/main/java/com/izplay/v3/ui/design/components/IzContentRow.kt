package com.izplay.v3.ui.design.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.izplay.v3.ui.design.IzPreviewSurface
import com.izplay.v3.ui.design.tokens.IzColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.izplay.v3.ui.design.tokens.IzSpacing
import com.izplay.v3.ui.design.tokens.IzType

/**
 * Cabeçalho de seção + fileira horizontal rolável. Spec `section-header.md`:
 * título 24sp Bold, "Ver mais" 16sp, espaçamento inferior 16dp, **nunca
 * centralizar**. Rola por D-pad no Android TV (o foco dos itens arrasta a lista).
 *
 * Genérico em [T]: recebe os itens e um slot que desenha cada card, então serve
 * qualquer conteúdo (canais, filmes, séries) sem duplicar layout.
 */
@Composable
fun <T> IzContentRow(
    title: String,
    items: List<T>,
    modifier: Modifier = Modifier,
    kicker: String? = null,
    onSeeMore: (() -> Unit)? = null,
    itemContent: @Composable (T) -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = IzSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(title, style = IzType.Subtitle.copy(fontSize = 26.sp), color = IzColor.TextPrimary)
            if (kicker != null) {
                Text(
                    kicker.uppercase(),
                    style = IzType.Caption.copy(fontSize = 13.sp, letterSpacing = 2.sp),
                    color = IzColor.Live,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(start = IzSpacing.sm),
                )
            }
            if (onSeeMore != null) {
                Text(
                    stringResource(com.izplay.v3.R.string.row_see_all),
                    style = IzType.Body,
                    color = IzColor.TextSecondary,
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .padding(start = IzSpacing.md)
                        .clickable(onClick = onSeeMore),
                )
            }
        }
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(IzSpacing.md),
            contentPadding = PaddingValues(end = IzSpacing.md),
        ) {
            items(items.size) { index -> itemContent(items[index]) }
        }
    }
}

@Preview(backgroundColor = 0xFF000000, showBackground = true, widthDp = 700, heightDp = 300)
@Composable
private fun IzContentRowPreview() {
    IzPreviewSurface {
        IzContentRow(
            title = "Filmes / Lançamentos",
            items = listOf("Um Dia", "Vermiglio", "Barba Ensopada", "Heartstopper"),
            onSeeMore = {},
        ) { name ->
            IzPosterCard(title = name, onClick = {})
        }
    }
}
