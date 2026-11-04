package com.example.crisalida.ui.theme.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crisalida.ui.theme.ColorBotonEntrar
import com.example.crisalida.ui.theme.OutfitFont
import java.text.SimpleDateFormat
import java.util.Locale

// cosas compartidas por las pantallas de detalle e historial
fun fechaLegible(iso: String): String = try {
    val fecha = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(iso.take(10))
    if (fecha == null) iso else SimpleDateFormat("d 'de' MMMM 'de' yyyy", Locale("es", "MX")).format(fecha)
} catch (e: Exception) {
    iso
}

fun dificultadTexto(d: String): String = when (d) {
    "facil" -> "Fácil"
    "media" -> "Intermedio"
    "dificil" -> "Difícil"
    else -> d
}

// fila de Volver para las pantallas de detalle / bienestar
@Composable
fun BarraVolver(texto: String = "Volver", onVolver: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onVolver() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = ColorBotonEntrar)
        Text(
            text = "  $texto",
            fontFamily = OutfitFont, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = ColorBotonEntrar
        )
    }
}

// las 4 preguntas de acompañamiento
val PreguntasReflexion = listOf(
    "¿Qué pensamiento te pesó más hoy?",
    "¿Pudiste tomarte 10 minutos para ti?",
    "¿Necesitas silencio o desahogarte?",
    "¿Reconociste tus propios límites?"
)
