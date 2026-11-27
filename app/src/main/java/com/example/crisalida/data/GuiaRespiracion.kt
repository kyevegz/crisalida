package com.example.crisalida.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Ajuste «Guía por voz» de la respiración con Lilu (issue 10.2): si la voz dice «Inhala» y «Exhala».
 * Se guarda en el teléfono (archivo «crisalida_ajustes»). Empieza apagada.
 */
object GuiaRespiracion {
    private const val ARCHIVO = "crisalida_ajustes"
    private const val CLAVE = "guia_voz"
    private var cargado = false

    var voz by mutableStateOf(false)
        private set

    fun cargar(context: Context) {
        if (cargado) return
        cargado = true
        voz = context.applicationContext.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE).getBoolean(CLAVE, false)
    }

    fun cambiarVoz(context: Context, activada: Boolean) {
        cargado = true
        voz = activada
        context.applicationContext.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE).edit().putBoolean(CLAVE, activada).apply()
    }
}
