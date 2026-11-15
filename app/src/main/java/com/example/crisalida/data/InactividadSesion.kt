package com.example.crisalida.data

import android.content.Context
import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue

// Cierre de sesión por inactividad. La usuaria elige cuánto tiempo sin tocar la pantalla; la opción se
// guarda en el teléfono (no en la base de datos) y se conserva al cerrar y abrir la app.
// Usa SystemClock.elapsedRealtime(): sigue contando con la pantalla apagada y no se puede adelantar
// ni atrasar cambiando la hora del teléfono.
object `InactividadSesion` {
    const val NUNCA = 0

    // minutos → texto que se le muestra
    val opciones = listOf(
        5 to "5 minutos",
        10 to "10 minutos",
        15 to "15 minutos",
        30 to "30 minutos",
        NUNCA to "Nunca"
    )
    private const val PREDETERMINADO = 10

    var minutos by mutableIntStateOf(PREDETERMINADO)
        private set

    private var ultimaActividad = SystemClock.elapsedRealtime()
    private var cargado = false
    private const val ARCHIVO = "crisalida_ajustes"
    private const val CLAVE = "inactividad_minutos"

    fun cargar(context: Context) {
        if (cargado) return
        cargado = true
        val guardado = context.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE).getInt(CLAVE, PREDETERMINADO)
        minutos = if (opciones.any { it.first == guardado }) guardado else PREDETERMINADO
    }

    fun cambiar(context: Context, nuevos: Int) {
        minutos = nuevos
        context.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE).edit().putInt(CLAVE, nuevos).apply()
        registrarActividad()
    }

    // se llama con cada toque en la pantalla: reinicia el temporizador
    fun registrarActividad() {
        ultimaActividad = SystemClock.elapsedRealtime()
    }

    // milisegundos que faltan para cerrar la sesión; null si no hay nada que vigilar
    // (sin sesión, en modo camuflaje —la galería falsa no debe comportarse distinto a una galería normal— o en "Nunca")
    fun milisRestantes(): Long? {
        if (SesionActual.token == null || SesionActual.esCamuflaje || minutos == NUNCA) return null
        return minutos * 60_000L - (SystemClock.elapsedRealtime() - ultimaActividad)
    }

    fun debeCerrar(): Boolean = (milisRestantes() ?: Long.MAX_VALUE) <= 0L
}