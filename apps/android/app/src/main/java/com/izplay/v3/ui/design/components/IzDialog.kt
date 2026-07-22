package com.izplay.v3.ui.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.izplay.v3.ui.design.IzPreviewSurface
import com.izplay.v3.ui.design.tokens.IzColor
import com.izplay.v3.ui.design.tokens.IzRadius
import com.izplay.v3.ui.design.tokens.IzSize
import com.izplay.v3.ui.design.tokens.IzSpacing
import com.izplay.v3.ui.design.tokens.IzType

/**
 * Diálogo modal da marca. Raio 20dp, padding 32dp. Spec `dialog.md`: no máximo
 * dois botões principais; o modal prende o foco (garantido pelo [Dialog] do
 * Compose, que captura o D-pad enquanto aberto).
 */
@Composable
fun IzDialog(
    title: String,
    onDismiss: () -> Unit,
    confirmText: String,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    message: String? = null,
    cancelText: String? = "Cancelar",
) {
    Dialog(onDismissRequest = onDismiss) {
        IzDialogContent(
            title = title,
            message = message,
            confirmText = confirmText,
            onConfirm = onConfirm,
            cancelText = cancelText,
            onCancel = onDismiss,
            modifier = modifier,
        )
    }
}

@Composable
private fun IzDialogContent(
    title: String,
    message: String?,
    confirmText: String,
    onConfirm: () -> Unit,
    cancelText: String?,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .widthIn(min = 280.dp, max = 480.dp)
            .clip(RoundedCornerShape(IzRadius.dialog))
            .background(IzColor.Surface)
            .padding(IzSize.dialogPadding),
        verticalArrangement = Arrangement.spacedBy(IzSpacing.md),
    ) {
        Text(title, style = IzType.Subtitle, color = IzColor.TextPrimary)
        if (message != null) {
            Text(message, style = IzType.Body, color = IzColor.TextSecondary)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = IzSpacing.xs),
            horizontalArrangement = Arrangement.spacedBy(IzSpacing.sm, alignment = androidx.compose.ui.Alignment.End),
        ) {
            if (cancelText != null) {
                IzButton(text = cancelText, onClick = onCancel, style = IzButtonStyle.Secondary)
            }
            IzButton(text = confirmText, onClick = onConfirm, style = IzButtonStyle.Primary)
        }
    }
}

@Preview(backgroundColor = 0xFF000000, showBackground = true, widthDp = 520, heightDp = 260)
@Composable
private fun IzDialogPreview() {
    // Preview do conteúdo (sem a janela real do Dialog, que não renderiza no preview).
    IzPreviewSurface {
        IzDialogContent(
            title = "Remover playlist?",
            message = "Isto apaga a configuração local desta playlist. O conteúdo do provedor não é afetado.",
            confirmText = "Remover",
            onConfirm = {},
            cancelText = "Cancelar",
            onCancel = {},
        )
    }
}
