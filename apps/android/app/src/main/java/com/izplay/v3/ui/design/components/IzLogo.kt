package com.izplay.v3.ui.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import com.izplay.v3.ui.design.IzPreviewSurface
import com.izplay.v3.ui.design.tokens.IzColor
import com.izplay.v3.ui.design.tokens.IzRadius
import com.izplay.v3.ui.design.tokens.IzType

/**
 * Marca IZ Play em Compose (código próprio, sem asset de terceiros).
 *
 * Tratamento provisório: tile vermelho arredondado com "IZ" branco + "PLAY"
 * letra-espaçado. Serve como logo oficial até que o arquivo de marca do IZ Play
 * seja fornecido e sua autoria/licença confirmada (ver `LICENSES.md`). Quando
 * isso ocorrer, trocar apenas o interior deste componente — o resto do app já o
 * consome por aqui.
 *
 * @param height altura total da marca; o restante escala proporcionalmente.
 */
@Composable
fun IzLogo(
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 64.dp,
) {
    val tileText: TextUnit = (height.value * 0.5f).sp
    val wordText: TextUnit = (height.value * 0.34f).sp

    Row(
        modifier = modifier.height(height),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(height * 0.22f),
    ) {
        Box(
            modifier = Modifier
                .height(height)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(IzRadius.md))
                .background(IzColor.Primary),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "IZ",
                color = IzColor.OnPrimary,
                fontFamily = IzType.Family,
                fontWeight = FontWeight.Black,
                fontSize = tileText,
                textAlign = TextAlign.Center,
            )
        }
        Text(
            text = "PLAY",
            color = IzColor.TextPrimary,
            fontFamily = IzType.Family,
            fontWeight = FontWeight.Black,
            fontSize = wordText,
            letterSpacing = 0.18.em,
            modifier = Modifier.padding(end = 4.dp),
        )
    }
}

@Preview(backgroundColor = 0xFF000000, showBackground = true, widthDp = 320, heightDp = 160)
@Composable
private fun IzLogoPreview() {
    IzPreviewSurface {
        Box(contentAlignment = Alignment.Center) {
            IzLogo(height = 72.dp)
        }
    }
}
