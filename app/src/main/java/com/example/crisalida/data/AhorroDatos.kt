package com.example.crisalida.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Ahorro de datos (Configuración → Datos y almacenamiento), para usuarias con datos móviles limitados.
 * Con él activo, las fotos NO se descargan solas: se ven hasta que la usuaria toca la foto (ver FotoRemota).
 * La opción se guarda en el teléfono y se conserva al cerrar la app.
 *
 * Ojo: el modo sin conexión NO es un ajuste, es automático al perder la red; no tiene nada que ver con esto.
 */
object AhorroDatos {
    private const val ARCHIVO = "crisalida_ajustes"
    private const val CLAVE = "ahorro_datos"
    private var cargado = false

    var activo by mutableStateOf(false)
        private set

    /** Lee lo guardado. Es seguro llamarlo muchas veces: solo lee la primera vez. */
    fun cargar(context: Context) {
        if (cargado) return
        cargado = true
        activo = context.applicationContext.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE).getBoolean(CLAVE, false)
    }

    fun cambiar(context: Context, activado: Boolean) {
        cargado = true
        activo = activado
        context.applicationContext.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE).edit().putBoolean(CLAVE, activado).apply()
    }
}
