package com.example.crisalida.data

import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
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

/** Regla de «¿debe hablar?» (guía por voz): solo si la usuaria la activó y NO está en camuflaje. */
fun debeHablar(vozActiva: Boolean, enCamuflaje: Boolean): Boolean = vozActiva && !enCamuflaje

/**
 * Vibraciones de la app (issue 10.1). Cuatro patrones distinguibles al tacto:
 *  - exito(): dos toques cortos     (publicación, entrega o validación hecha)
 *  - error():  un golpe seco         (PIN incorrecto, publicación rechazada)
 *  - aviso():  un toque largo        (cuenta bloqueada 15 min)
 *  - tic():    un pulso muy ligero   (elegir una opción)
 * Y dos avisos pequeños para la respiración guiada (issue 10.2): finDeInhalar() (un toque) y finDeExhalar() (dos toques).
 * Nunca es el único aviso: siempre hay también texto en pantalla. Si no hay motor o falla, no hace nada.
 */
object Haptica {
    private const val ARCHIVO = "crisalida_ajustes"
    private const val CLAVE = "vibracion"
    private var cargado = false
    private var contextoApp: Context? = null

    var activa by mutableStateOf(false)
        private set

    val enCamuflaje: Boolean get() = Paleta.enCamuflaje || SesionActual.esCamuflaje

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
        if (activada) vibrar(longArrayOf(0, 40, 80, 40), 255)   // al encender, una muestra para que sepa cómo se siente
    }

    // exito() y error() además hacen sonar el sonido de envío / de error (issue 10.5), que tiene su propio ajuste
    fun exito(sonido: Sonido = Sonido.ENVIO) { vibrar(longArrayOf(0, 40, 80, 40), 255); Sonidos.reproducir(sonido) }
    fun error() { vibrar(longArrayOf(0, 90), 255); Sonidos.reproducir(Sonido.ERROR) }
    fun aviso() = vibrar(longArrayOf(0, 400), 255)
    fun tic() = vibrar(longArrayOf(0, 12), 80)

    /** Respiración guiada: se acabó el turno de inhalar → un solo toque pequeño. */
    fun finDeInhalar() { vibrar(longArrayOf(0, 60), 150); Sonidos.reproducir(Sonido.CALMA_INHALA) }

    /** Respiración guiada: se acabó el turno de exhalar → dos toques pequeños. */
    fun finDeExhalar() { vibrar(longArrayOf(0, 40, 70, 40), 150); Sonidos.reproducir(Sonido.CALMA_EXHALA) }

    /** Corta cualquier vibración en curso (Pausar, salir de la pantalla, segundo plano). */
    fun cancelar() {
        try {
            val contexto = contextoApp ?: return
            motor(contexto)?.cancel()
        } catch (e: Exception) {
            Log.d("Haptica", "falló cancelar: $e")
        }
    }

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
            val fuerzas = IntArray(patron.size) { i -> if (i % 2 == 0) 0 else fuerza }
            emitir(v, patron, fuerzas)
        } catch (e: Exception) {
            Log.d("Haptica", "falló la vibración: $e")   // la vibración nunca debe romper la app
        }
    }

    // Aquí se aplica la regla: ajuste activado, fuera de camuflaje y con motor
    private fun emitir(v: Vibrator?, patron: LongArray, fuerzas: IntArray?) {
        val tieneMotor = v?.hasVibrator() == true
        if (!debeVibrar(activa, enCamuflaje, tieneMotor)) {
            Log.d("Haptica", "no vibra: ajuste=$activa camuflaje=$enCamuflaje motor=$tieneMotor")
            return
        }
        @Suppress("DEPRECATION")
        val atributos = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // sin control de fuerza (motores sencillos de gama baja y media) basta la lista de tiempos
            val efecto = if (fuerzas != null && v!!.hasAmplitudeControl()) {
                VibrationEffect.createWaveform(patron, fuerzas, -1)
            } else {
                VibrationEffect.createWaveform(patron, -1)
            }
            if (Build.VERSION.SDK_INT >= 33) {
                // Android 13: se declara explícitamente como respuesta al tacto (sigue «Vibración al tocar» del sistema)
                v!!.vibrate(efecto, VibrationAttributes.createForUsage(VibrationAttributes.USAGE_TOUCH))
            } else {
                @Suppress("DEPRECATION")
                v!!.vibrate(efecto, atributos)                            // Android 8 a 12
            }
        } else {
            @Suppress("DEPRECATION")
            v!!.vibrate(patron, -1, atributos)                            // Android 7: método antiguo con lista de tiempos
        }
        Log.d("Haptica", "vibrar enviado: ${patron.toList()}")
    }
}
