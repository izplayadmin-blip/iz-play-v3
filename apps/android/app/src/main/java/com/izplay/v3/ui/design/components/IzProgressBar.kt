package com.izplay.v3.ui.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.layout
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.izplay.v3.ui.design.IzPreviewSurface
import com.izplay.v3.ui.design.tokens.IzColor
import com.izplay.v3.ui.design.tokens.IzSize
import com.izplay.v3.ui.design.tokens.IzRadius
import androidx.compose.foundation.layout.width

/**
 * Barra de progresso determinística (posição de reprodução, progresso de
 * download). Trilho em superfície, preenchimento vermelho. [progress] em 0f..1f.
 */
@Composable
fun IzProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
) {
    val clamped = progress.coerceIn(0f, 1f)
    val shape = RoundedCornerShape(IzRadius.pill)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(IzSize.progressThickness)
            .clip(shape)
            .background(IzColor.Border),
    ) {
        Box(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(clamped)
                .clip(shape)
                .background(IzColor.Primary),
        )
    }
}

@Preview(backgroundColor = 0xFF000000, showBackground = true, widthDp = 260)
@Composable
private fun IzProgressBarPreview() {
    IzPreviewSurface {
        Box(Modifier.width(220.dp)) {
            IzProgressBar(progress = 0.42f)
        }
    }
}
