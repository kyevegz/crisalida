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
 * Un patrón de vibración. [tiempos] = [espera, vibra, espera, vibra...] en milisegundos.
 * [fuerzas] (de 0 a 255, una por cada tiempo) solo existe si el teléfono controla la intensidad; si es null, el patrón
 * es de encendido y apagado.
 */
class PatronVibracion(val tiempos: LongArray, val fuerzas: IntArray?)

/**
 * Inhalar: pulsos suaves y separados que van CRECIENDO (uno cada ~0.7 s). Dura exactamente [duracionMs].
 * Con control de intensidad cada pulso es más fuerte que el anterior; sin él, cada pulso dura más que el anterior.
 */
fun patronInhalar(duracionMs: Int, conFuerza: Boolean): PatronVibracion {
    val pulsos = maxOf(3, duracionMs / 700)
    val tramo = duracionMs / pulsos
    val resto = duracionMs - tramo * pulsos
    val tiempos = ArrayList<Long>()
    val fuerzas = ArrayList<Int>()
    tiempos.add(0); fuerzas.add(0)
    for (i in 0 until pulsos) {
        val avance = i.toFloat() / (pulsos - 1)                      // 0 en el primero, 1 en el último
        val encendido = if (conFuerza) 120 else (20 + 200 * avance).toInt()   // 20 ms … 220 ms
        val apagado = tramo - encendido + (if (i == pulsos - 1) resto else 0)
        tiempos.add(encendido.toLong()); fuerzas.add((30 + 170 * avance).toInt())   // fuerza 30 … 200
        tiempos.add(apagado.toLong()); fuerzas.add(0)
    }
    return PatronVibracion(tiempos.toLongArray(), if (conFuerza) fuerzas.toIntArray() else null)
}

/**
 * Exhalar: UNA vibración larga y suave que se va apagando durante todo [duracionMs]. Dura exactamente [duracionMs].
 * Con control de intensidad la fuerza baja poco a poco. Sin él se simula con ráfagas muy rápidas (50 ms) que
 * se sienten como una vibración continua más suave, y cada vez con más pausa entre ráfagas (se va apagando).
 */
fun patronExhalar(duracionMs: Int, conFuerza: Boolean): PatronVibracion {
    if (conFuerza) {
        val pasos = 8
        val tramo = duracionMs / pasos
        val resto = duracionMs - tramo * pasos
        val tiempos = LongArray(pasos + 1) { i -> if (i == 0) 0L else (tramo + (if (i == pasos) resto else 0)).toLong() }
        val fuerzas = IntArray(pasos + 1) { i -> if (i == 0) 0 else 110 - (85 * (i - 1)) / (pasos - 1) }   // 110 … 25
        return PatronVibracion(tiempos, fuerzas)
    }
    val ciclo = 50
    val proporciones = floatArrayOf(0.8f, 0.55f, 0.35f, 0.2f)   // parte del ciclo con el motor encendido, por cuarto de tiempo
    val tiempos = ArrayList<Long>()
    tiempos.add(0)
    var usado = 0
    while (usado + ciclo <= duracionMs) {
        val cuarto = minOf(3, usado * 4 / duracionMs)
        val encendido = (ciclo * proporciones[cuarto]).toInt()
        tiempos.add(encendido.toLong()); tiempos.add((ciclo - encendido).toLong())
        usado += ciclo
    }
    if (tiempos.size == 1) return PatronVibracion(longArrayOf(0, duracionMs.toLong()), null)   // duración muy corta
    if (usado < duracionMs) tiempos[tiempos.lastIndex] = tiempos.last() + (duracionMs - usado)
    return PatronVibracion(tiempos.toLongArray(), null)
}

/**
 * Vibraciones de la app (issue 10.1). Cuatro patrones distinguibles al tacto:
 *  - exito(): dos toques cortos     (publicación, entrega o validación hecha)
 *  - error():  un golpe seco         (PIN incorrecto, publicación rechazada)
 *  - aviso():  un toque largo        (cuenta bloqueada 15 min)
 *  - tic():    un pulso muy ligero   (elegir una opción)
 * Y dos para la respiración guiada (issue 10.2): inhalar() y exhalar().
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
        if (activada) exito()   // al encender, una muestra para que sepa cómo se siente
    }

    fun exito() = vibrar(longArrayOf(0, 40, 80, 40), 255)
    fun error() = vibrar(longArrayOf(0, 90), 255)
    fun aviso() = vibrar(longArrayOf(0, 400), 255)
    fun tic() = vibrar(longArrayOf(0, 12), 80)

    /** Respiración guiada: pulsos que crecen durante [duracionMs]. */
    fun inhalar(duracionMs: Int) = respirar(duracionMs, inhalando = true)

    /** Respiración guiada: una vibración larga y suave durante [duracionMs]. */
    fun exhalar(duracionMs: Int) = respirar(duracionMs, inhalando = false)

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

    private fun respirar(duracionMs: Int, inhalando: Boolean) {
        try {
            val contexto = contextoApp ?: return
            val v = motor(contexto) ?: return
            val conFuerza = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && v.hasAmplitudeControl()
            val patron = if (inhalando) patronInhalar(duracionMs, conFuerza) else patronExhalar(duracionMs, conFuerza)
            emitir(v, patron.tiempos, patron.fuerzas)
        } catch (e: Exception) {
            Log.d("Haptica", "falló la vibración: $e")
        }
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
