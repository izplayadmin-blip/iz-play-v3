package com.izplay.v3.ui.design.tokens

/**
 * Durações de animação e escalas de foco do IZ Play V3.
 * Fonte: `design-system/tokens/animation.json` e `08-Focus-System.md` do V2.
 */
object IzMotion {
    // Durações (ms)
    const val Fast = 150
    const val Normal = 200

    /** Recolher/expandir da sidebar — 180ms, valor do V2 Android (Sidebar.kt). */
    const val Sidebar = 180

    /** Foco de item da sidebar — 130ms (V2 Android). */
    const val SidebarFocus = 130
    const val Max = 300
}

/**
 * Escalas de foco para Android TV. A spec do V2 define valores distintos por
 * tipo de elemento; divergir deles exige justificativa registrada.
 */
object IzFocusScale {
    const val Rest = 1.0f

    /** Card focado: 1.05 a 1.08. Usamos o teto para leitura clara à distância. */
    const val Card = 1.08f

    /** Botão focado. */
    const val Button = 1.03f

    /** Item de sidebar focado — 1.08 no código Android do V2 (NavIcon). */
    const val Sidebar = 1.08f
}

/** Opacidades de estado. */
object IzOpacity {
    const val Disabled = 0.38f
    const val Pressed = 0.12f
    const val ScrimHero = 0.85f
    const val SkeletonHigh = 1.0f
    const val SkeletonLow = 0.3f
}
