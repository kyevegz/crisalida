package com.example.crisalida.data

import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.crisalida.ui.theme.Paleta

/**
 * Regla de «¿debe vibrar?» sin nada de Android, para probarla con una prueba unitaria.
 * Vibra solo si la usuaria lo activó, NO está en camuflaje y el teléfono tiene motor.
 */
fun debeVibrar(ajusteActivo: Boolean, enCamuflaje: Boolean, tieneMotor: Boolean): Boolean =
    ajusteActivo && !enCamuflaje && tieneMotor

/**
 * Vibraciones de la app (issue 10.1). Cuatro patrones distinguibles al tacto:
 *  - exito(): dos toques cortos     (publicación, entrega o validación hecha)
 *  - error():  un golpe seco         (PIN incorrecto)
 *  - aviso():  un toque largo        (cuenta bloqueada 15 min)
 *  - tic():    un pulso muy ligero   (elegir una opción)
 * Nunca es el único aviso: siempre hay también texto en pantalla. Si no hay motor o falla, no hace nada.
 */
object Haptica {
    private const val ARCHIVO = "crisalida_ajustes"
    private const val CLAVE = "vibracion"
    private var cargado = false
    private var contextoApp: Context? = null

    var activa by mutableStateOf(false)
        private set

    fun cargar(context: Context) {
        contextoApp = context.applicationContext
        if (cargado) return
        cargado = true
        activa = context.applicationContext.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE).getBoolean(CLAVE, false)
    }

    fun cambiar(context: Context, activada: Boolean) {
        contextoApp = context.applicationContext
        cargado = true
        activa = activada
        context.applicationContext.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE).edit().putBoolean(CLAVE, activada).apply()
        if (activada) exito()   // al encender, una muestra para que sepa cómo se siente
    }

    fun exito() = vibrar(longArrayOf(0, 40, 80, 40), 255)
    fun error() = vibrar(longArrayOf(0, 90), 255)
    fun aviso() = vibrar(longArrayOf(0, 400), 255)
    fun tic() = vibrar(longArrayOf(0, 12), 80)

    private fun motor(context: Context): Vibrator? = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    } catch (e: Exception) {
        null
    }

    // patron = [espera, vibra, espera, vibra...] en milisegundos; fuerza de 1 a 255
    private fun vibrar(patron: LongArray, fuerza: Int) {
        try {
            val contexto = contextoApp ?: return
            val v = motor(contexto)
            val enCamuflaje = Paleta.enCamuflaje || SesionActual.esCamuflaje
            if (!debeVibrar(activa, enCamuflaje, v?.hasVibrator() == true)) return
            // Se declara que es una respuesta al tacto: sin esto, Xiaomi/Android 13 pueden descartar la vibración
            val atributos = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val efecto = if (v!!.hasAmplitudeControl()) {
                    val fuerzas = IntArray(patron.size) { i -> if (i % 2 == 0) 0 else fuerza }
                    VibrationEffect.createWaveform(patron, fuerzas, -1)
                } else {
                    VibrationEffect.createWaveform(patron, -1)
                }
                @Suppress("DEPRECATION")
                v.vibrate(efecto, atributos)                                // Android 8 o más
            } else {
                @Suppress("DEPRECATION")
                v!!.vibrate(patron, -1, atributos)                          // Android 7: método antiguo con lista de tiempos
            }
        } catch (e: Exception) {
            // la vibración nunca debe romper la app
        }
    }
}
