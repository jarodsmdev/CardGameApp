package com.jarod.card.core.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Diálogo de confirmación reutilizable (p. ej. confirmar la salida de una
 * pantalla). Al tocar fuera o cancelar se invoca [onDismiss]; al confirmar,
 * [onConfirm].
 */
@Composable
fun ConfirmDialog(
    title: String,
    text: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(title)
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        text = {
            Text(text)
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Aceptar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Preview(
    name = "ConfirmDialog",
    showBackground = true,
    //showSystemUi = true
)
@Composable
fun ConfirmDialogPreview(){
    ConfirmDialog(
        title = "Salir",
        text = "¿Estás seguro de que deseas salir de esta pantalla?",
        onConfirm = {},
        onDismiss = {}
    )
}
