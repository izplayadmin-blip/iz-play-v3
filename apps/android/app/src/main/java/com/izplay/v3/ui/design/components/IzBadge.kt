package com.izplay.v3.ui.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.izplay.v3.ui.design.IzPreviewSurface
import com.izplay.v3.ui.design.tokens.IzColor
import com.izplay.v3.ui.design.tokens.IzRadius
import com.izplay.v3.ui.design.tokens.IzType

/** Papéis semânticos do [IzBadge] — fixam a regra dos dois amarelos. */
enum class IzBadgeKind {
    /** AO VIVO / HOT — laranja [IzColor.Live]. NUNCA para estado de sistema. */
    Live,

    /** Destaques neutros: HD, 4K, NOVO — superfície. */
    Info,

    /** Aviso de sistema — [IzColor.Warning]. NUNCA para conteúdo. */
    Warning,
}

/**
 * Selo compacto. O [kind] escolhe a cor conforme o papel semântico, impedindo
 * que os dois amarelos (`#F5A623` conteúdo vs `#F39C12` sistema) sejam trocados.
 */
@Composable
fun IzBadge(
    text: String,
    modifier: Modifier = Modifier,
    kind: IzBadgeKind = IzBadgeKind.Info,
) {
    val container: Color
    val content: Color
    when (kind) {
        IzBadgeKind.Live -> {
            container = IzColor.Live; content = IzColor.OnLive
        }
        IzBadgeKind.Info -> {
            container = IzColor.Surface2; content = IzColor.TextPrimary
        }
        IzBadgeKind.Warning -> {
            container = IzColor.Warning; content = IzColor.OnLive
        }
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(IzRadius.sm))
            .background(container)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = text,
            style = IzType.Caption.copy(fontWeight = FontWeight.Bold),
            color = content,
        )
    }
}

@Preview(backgroundColor = 0xFF000000, showBackground = true)
@Composable
private fun IzBadgePreview() {
    IzPreviewSurface {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IzBadge("AO VIVO", kind = IzBadgeKind.Live)
            IzBadge("4K", kind = IzBadgeKind.Info)
            IzBadge("HD", kind = IzBadgeKind.Info)
            IzBadge("EXPIRA EM 3D", kind = IzBadgeKind.Warning)
        }
    }
}
