package com.izplay.v3.ui.design.tokens

import androidx.compose.ui.graphics.Color

/**
 * Cores oficiais do IZ Play V3.
 *
 * Fonte: design system do IZ Play V2 (`design-system/tokens/colors.json`,
 * commit `6f3b4b1`), reimplementado como código próprio. Valores confirmados
 * pelo responsável na autorização da Etapa 2.
 *
 * Regra: **amarelo nunca é cor primária**. A primária é [Primary] (`#CC0000`).
 * [Live] (`#F5A623`) é exclusivo de indicadores AO VIVO/HOT; [Warning]
 * (`#F39C12`) é exclusivo de alertas do sistema. Ver [[design-system]].
 */
object IzColor {
    // Marca
    val Primary = Color(0xFFCC0000)
    val PrimaryHover = Color(0xFFE60000)
    val PrimarySoft = Color(0xFFFF3333)

    // Superfícies
    val Background = Color(0xFF000000)
    val BackgroundDeep = Color(0xFF05070B)
    val Surface = Color(0xFF111111)
    val Surface2 = Color(0xFF181818)
    val SurfaceHover = Color(0xFF1B1B1B)
    val Border = Color(0xFF2A2A2A)

    // Texto
    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFFA8A8A8)
    val Muted = Color(0xFF888888)

    // Destaque de conteúdo — SOMENTE AO VIVO/HOT
    val Live = Color(0xFFF5A623)

    // Semânticos de sistema
    val Warning = Color(0xFFF39C12)
    val Success = Color(0xFF2ECC71)
    val Error = Color(0xFFE74C3C)

    // Sobre a cor primária / sobre superfícies escuras
    val OnPrimary = Color(0xFFFFFFFF)
    val OnLive = Color(0xFF000000)
}
