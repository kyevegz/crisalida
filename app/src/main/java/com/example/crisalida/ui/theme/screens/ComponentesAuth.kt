package com.example.crisalida.ui.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crisalida.ui.theme.JakartaSansFont

/** Mensaje de error compartido por Login y Registro. Si error es null, no muestra nada. */
@Composable
fun MensajeError(error: String?, modifier: Modifier = Modifier) {
    if (error != null) {
        Text(
            text = error,
            fontFamily = JakartaSansFont,
            fontSize = 13.sp,
            color = Color(0xFFB3261E),
            textAlign = TextAlign.Center,
            modifier = modifier
                .fillMaxWidth(0.9f)
                .padding(top = 10.dp)
        )
    }
}