package com.izplay.v3.ui.design.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.izplay.v3.R
import com.izplay.v3.ui.design.IzPreviewSurface

/**
 * Logo oficial IZ Play — o wordmark "iZ PLAY" metálico do V2
 * (`drawable-nodpi/izplay_logo.png`, 420×141, origem registrada em
 * `LICENSES.md`). Proporção preservada; nunca deformar, recortar ou recriar.
 *
 * @param height altura da marca; a largura segue a proporção 420:141.
 */
@Composable
fun IzLogo(
    modifier: Modifier = Modifier,
    height: Dp = 64.dp,
) {
    Image(
        painter = painterResource(R.drawable.izplay_logo),
        contentDescription = "IZ Play",
        contentScale = ContentScale.Fit,
        modifier = modifier
            .height(height)
            .aspectRatio(420f / 141f),
    )
}

@Preview(backgroundColor = 0xFF0A0A0A, showBackground = true, widthDp = 360, heightDp = 160)
@Composable
private fun IzLogoPreview() {
    IzPreviewSurface {
        Box(contentAlignment = Alignment.Center) {
            IzLogo(height = 72.dp)
        }
    }
}
