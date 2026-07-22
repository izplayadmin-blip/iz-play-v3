package com.izplay.v3.ui.design.tokens

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Escala tipográfica do IZ Play V3.
 * Fonte: `design-system/tokens/typography.json` do V2.
 *
 * A família alvo é **Inter**. Enquanto ela não for empacotada (obrigação de
 * licença SIL OFL — ver `THIRD_PARTY_NOTICES.md`), usamos a fonte do sistema
 * via [FontFamily.Default]. Trocar por [FontFamily] do Inter aqui, num único
 * ponto, quando o arquivo da fonte entrar.
 */
object IzType {
    val Family: FontFamily = FontFamily.Default

    private val Regular = FontWeight.Normal // 400
    private val Medium = FontWeight.Medium // 500
    private val Bold = FontWeight.Bold // 700
    private val Black = FontWeight.Black // 900

    val Hero = TextStyle(fontFamily = Family, fontWeight = Black, fontSize = 48.sp)
    val Title = TextStyle(fontFamily = Family, fontWeight = Bold, fontSize = 40.sp)
    val Subtitle = TextStyle(fontFamily = Family, fontWeight = Bold, fontSize = 24.sp)
    val Category = TextStyle(fontFamily = Family, fontWeight = Bold, fontSize = 20.sp)
    val Card = TextStyle(fontFamily = Family, fontWeight = Bold, fontSize = 18.sp)
    val Body = TextStyle(fontFamily = Family, fontWeight = Regular, fontSize = 16.sp)
    val Button = TextStyle(fontFamily = Family, fontWeight = Medium, fontSize = 16.sp)
    val Description = TextStyle(fontFamily = Family, fontWeight = Regular, fontSize = 14.sp)
    val Caption = TextStyle(fontFamily = Family, fontWeight = Medium, fontSize = 12.sp)
}

/** Mapeia a escala IZ Play para o [Typography] do Material 3 (usado no preview). */
internal val IzTypography = Typography(
    displayLarge = IzType.Hero,
    headlineLarge = IzType.Title,
    headlineMedium = IzType.Subtitle,
    titleLarge = IzType.Category,
    titleMedium = IzType.Card,
    bodyLarge = IzType.Body,
    labelLarge = IzType.Button,
    bodyMedium = IzType.Description,
    labelSmall = IzType.Caption,
)
