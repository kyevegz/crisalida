package com.example.crisalida.ui.theme

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

//con esto se determina el tamaño de letra que elige en la configuración, se guarda en el teléfono. no en la bd
object AjustesTexto {
    val opciones = listOf("Normal" to 1f, "Grande" to 1.15f, "Muy grande" to 1.3f)

    var escala by mutableFloatStateOf(1f)
        private set

    private var cargado = false
    private const val ARCHIVO = "crisalida_ajustes"
    private const val CLAVE = "escala_texto"

    fun cargar(context: Context) {
        if (cargado) return
        cargado = true
        val guardada = context.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE).getFloat(CLAVE, 1f)
        escala = guardada.coerceIn(1f, 1.3f)
    }

    fun cambiar(context: Context, nueva: Float) {
        escala = nueva
        context.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE).edit().putFloat(CLAVE, nueva).apply()
    }
}

// aplica el tamaño elegido a toda la pantalla (y sus modales), con tope para que no se rompa
@Composable
fun ConEscalaDeTexto(contenido: @Composable () -> Unit) {
    val context = LocalContext.current
    remember(context) { AjustesTexto.cargar(context) }
    val base = LocalDensity.current
    val escalaFinal = (base.fontScale * AjustesTexto.escala).coerceAtMost(1.6f)
    CompositionLocalProvider(LocalDensity provides Density(base.density, escalaFinal)) {
        contenido()
    }
}

// para las barras de arriba y abajo, que tienen alto fijo: casi no crece, se limita por cuestiones de limites
@Composable
fun ConEscalaLimitada(tope: Float, contenido: @Composable () -> Unit) {
    val base = LocalDensity.current
    CompositionLocalProvider(LocalDensity provides Density(base.density, base.fontScale.coerceAtMost(tope))) {
        contenido()
    }
}
