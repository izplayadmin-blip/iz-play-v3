package com.izplay.v3.ui.design.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.izplay.v3.ui.design.IzPreviewSurface
import com.izplay.v3.ui.design.tokens.IzColor
import com.izplay.v3.ui.design.tokens.IzSpacing
import com.izplay.v3.ui.design.tokens.IzType

/**
 * Estado vazio: ícone neutro, título e texto de apoio, ação opcional. Nenhuma
 * lista deve "sumir em silêncio" — sempre mostrar este estado.
 */
@Composable
fun IzEmptyState(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    icon: ImageVector = Icons.Outlined.Inbox,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
) {
    IzMessageColumn(
        modifier = modifier,
        icon = icon,
        iconTint = IzColor.Muted,
        title = title,
        description = description,
        actionText = actionText,
        onAction = onAction,
    )
}

/**
 * Estado de erro: ícone de alerta, mensagem e ação de repetir. Spec: nunca
 * deixar erro sem saída — sempre oferecer nova tentativa quando fizer sentido.
 */
@Composable
fun IzErrorState(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    retryText: String? = "Tentar novamente",
    onRetry: (() -> Unit)? = null,
) {
    IzMessageColumn(
        modifier = modifier,
        icon = Icons.Outlined.ErrorOutline,
        iconTint = IzColor.Error,
        title = title,
        description = description,
        actionText = if (onRetry != null) retryText else null,
        onAction = onRetry,
    )
}

@Composable
private fun IzMessageColumn(
    icon: ImageVector,
    iconTint: androidx.compose.ui.graphics.Color,
    title: String,
    description: String?,
    actionText: String?,
    onAction: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(IzSpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(IzSpacing.sm),
    ) {
        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.padding(bottom = IzSpacing.xs))
        Text(title, style = IzType.Subtitle, color = IzColor.TextPrimary, textAlign = TextAlign.Center)
        if (description != null) {
            Text(
                description,
                style = IzType.Description,
                color = IzColor.TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 360.dp),
            )
        }
        if (actionText != null && onAction != null) {
            IzButton(text = actionText, onClick = onAction, modifier = Modifier.padding(top = IzSpacing.xs))
        }
    }
}

@Preview(backgroundColor = 0xFF000000, showBackground = true, widthDp = 420, heightDp = 320)
@Composable
private fun IzStatesPreview() {
    IzPreviewSurface {
        Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
            IzEmptyState(
                title = "Nada por aqui",
                description = "Você ainda não adicionou favoritos.",
                actionText = "Explorar catálogo",
                onAction = {},
            )
            IzErrorState(
                title = "Não foi possível carregar",
                description = "Verifique a conexão e tente de novo.",
                onRetry = {},
            )
        }
    }
}
