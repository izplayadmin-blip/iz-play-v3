package com.izplay.v3.ui.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.izplay.v3.ui.design.tokens.IzColor
import com.izplay.v3.ui.design.tokens.IzTypography

/**
 * Tema do design system IZ Play V3.
 *
 * ISOLADO de propósito: NÃO substitui o `IZPlayTheme` de `ui/theme/` que as
 * telas funcionais herdadas usam hoje. Serve os componentes `Iz*` e seus
 * previews com o [darkColorScheme] derivado dos tokens oficiais. A troca do
 * tema das telas reais acontece na migração visual (Etapa 3), tela a tela.
 */
private val IzDarkColors = darkColorScheme(
    primary = IzColor.Primary,
    onPrimary = IzColor.OnPrimary,
    secondary = IzColor.PrimarySoft,
    background = IzColor.Background,
    onBackground = IzColor.TextPrimary,
    surface = IzColor.Surface,
    onSurface = IzColor.TextPrimary,
    surfaceVariant = IzColor.Surface2,
    onSurfaceVariant = IzColor.TextSecondary,
    outline = IzColor.Border,
    error = IzColor.Error,
)

@Composable
fun IzTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = IzDarkColors,
        typography = IzTypography,
        content = content,
    )
}

/**
 * Moldura de preview: aplica [IzTheme] sobre o fundo preto da marca com uma
 * borda de respiro. Usada por todos os `@Preview` do design system.
 */
@Composable
internal fun IzPreviewSurface(
    padding: Boolean = true,
    content: @Composable () -> Unit,
) {
    IzTheme {
        Box(
            Modifier
                .background(IzColor.Background)
                .then(if (padding) Modifier.padding(24.dp) else Modifier),
        ) { content() }
    }
}
