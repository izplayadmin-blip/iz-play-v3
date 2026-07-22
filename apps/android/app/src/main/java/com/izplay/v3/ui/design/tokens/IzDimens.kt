package com.izplay.v3.ui.design.tokens

import androidx.compose.ui.unit.dp

/**
 * Espaçamentos, raios, elevação, bordas e dimensões de layout do IZ Play V3.
 * Fonte: tokens do IZ Play V2 (`spacing.json`, `radius.json`, `elevation.json`)
 * e specs de componentes (`sidebar.md`, `hero.md`, `buttons.md`, `dialog.md`).
 */
object IzSpacing {
    val xxs = 4.dp
    val xs = 8.dp
    val sm = 12.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
    val xxl = 40.dp
    val section = 48.dp
    val hero = 64.dp
    val screen = 96.dp
}

object IzRadius {
    val sm = 8.dp
    val md = 12.dp
    val card = 18.dp
    val dialog = 20.dp
    val hero = 24.dp

    /** "Pill": totalmente arredondado. Usar com `RoundedCornerShape(IzRadius.pill)`. */
    val pill = 999.dp
}

object IzElevation {
    val none = 0.dp
    val soft = 4.dp
    val medium = 8.dp
    val modal = 16.dp
}

object IzBorder {
    /**
     * Espessura do anel de foco. 3dp para permanecer visível de 720p a 4K —
     * dp é independente de densidade, mas um traço mais grosso garante leitura
     * à distância na TV.
     */
    val focus = 3.dp

    /** Traço separador do anel de foco, para contraste em qualquer fundo. */
    val focusSeparator = 1.dp

    /** Espessura de divisores e contornos neutros. */
    val hairline = 1.dp
}

/** Dimensões fixas de layout — valores do código Android do V2 (Sidebar.kt). */
object IzSize {
    // Sidebar: parâmetros reais do IZ Play V2 Android
    val sidebarExpanded = 214.dp
    val sidebarCollapsed = 78.dp
    val sidebarLogoArea = 82.dp
    val sidebarItemHeight = 54.dp
    val sidebarItemExpanded = 188.dp
    val sidebarItemCollapsed = 62.dp
    val sidebarIcon = 28.dp

    // Hero (design-system/components/hero.md)
    val heroHeight = 320.dp

    // Botão (design-system/components/buttons.md)
    val buttonHeight = 48.dp
    val buttonPadding = 24.dp

    // Dialog (design-system/components/dialog.md)
    val dialogPadding = 32.dp

    // Cards
    val cardWidth = 200.dp
    val posterWidth = 132.dp
    val channelSize = 132.dp

    /** Traço da barra de progresso. */
    val progressThickness = 4.dp
}
