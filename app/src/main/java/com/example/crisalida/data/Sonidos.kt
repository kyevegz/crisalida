package com.example.crisalida.data

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.crisalida.R

/**
 * Regla de «¿debe sonar?» sin nada de Android, para probarla con una prueba unitaria.
 * Suena solo si la usuaria lo activó, NO está en camuflaje y el teléfono está en modo sonido
 * (ni en silencio ni en vibración).
 */
fun debeSonar(ajusteActivo: Boolean, enCamuflaje: Boolean, timbreNormal: Boolean): Boolean =
    ajusteActivo && !enCamuflaje && timbreNormal

/** Los sonidos de la app (res/raw). Son propios, generados por síntesis: ver tools/gen_sounds.py y el README. */
enum class Sonido(val recurso: Int) {
    FLOR(R.raw.sfx_flor),     // ganó una flor
    NIVEL(R.raw.sfx_nivel),   // subió de nivel
    ENVIO(R.raw.sfx_envio),   // envío exitoso (publicar, entregar, validar)
    ERROR(R.raw.sfx_error),   // algo salió mal
    CALMA_INHALA(R.raw.sfx_calma_inhala),   // respiración con Lilu: terminó el turno de inhalar
    CALMA_EXHALA(R.raw.sfx_calma_exhala)    // respiración con Lilu: terminó el turno de exhalar
}

/**
 * Sonidos breves (issue 10.5) con SoundPool. Empiezan APAGADOS. Nunca son el único aviso: siempre hay texto en pantalla.
 * No suenan en camuflaje ni si el teléfono está en silencio o en vibración.
 */
object Sonidos {
    private const val ARCHIVO = "crisalida_ajustes"
    private const val CLAVE = "sonidos"
    private var cargado = false
    private var contextoApp: Context? = null
    private var pool: SoundPool? = null
    private val ids = HashMap<Sonido, Int>()

    var activos by mutableStateOf(false)
        private set

    fun cargar(context: Context) {
        contextoApp = context.applicationContext
        if (cargado) return
        cargado = true
        activos = context.applicationContext.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE).getBoolean(CLAVE, false)
        if (activos) prepararPool()
    }

    fun cambiar(context: Context, activados: Boolean) {
        contextoApp = context.applicationContext
        cargado = true
        activos = activados
        context.applicationContext.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE).edit().putBoolean(CLAVE, activados).apply()
        if (activados) {
            prepararPool()
            reproducir(Sonido.ENVIO)   // al encender, una muestra para que sepa cómo suena
        } else {
            soltarPool()
        }
    }

    /** Reproduce el sonido si se cumple la regla. Si algo falla, no hace nada. */
    fun reproducir(sonido: Sonido) {
        try {
            val contexto = contextoApp ?: return
            val audio = contexto.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            val timbreNormal = audio?.ringerMode == AudioManager.RINGER_MODE_NORMAL
            val enCamuflaje = Haptica.enCamuflaje
            if (!debeSonar(activos, enCamuflaje, timbreNormal)) {
                Log.d("Sonidos", "no suena: ajuste=$activos camuflaje=$enCamuflaje timbreNormal=$timbreNormal")
                return
            }
            if (pool == null) prepararPool()
            val id = ids[sonido] ?: return
            pool?.play(id, 1f, 1f, 1, 0, 1f)
        } catch (e: Exception) {
            Log.d("Sonidos", "falló el sonido: $e")
        }
    }

    private fun prepararPool() {
        if (pool != null) return
        val contexto = contextoApp ?: return
        try {
            val atributos = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)   // sonido de interacción, no es música ni alarma
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            val nuevo = SoundPool.Builder().setMaxStreams(2).setAudioAttributes(atributos).build()
            Sonido.values().forEach { ids[it] = nuevo.load(contexto, it.recurso, 1) }
            pool = nuevo
        } catch (e: Exception) {
            Log.d("Sonidos", "no se pudieron cargar los sonidos: $e")
        }
    }

    private fun soltarPool() {
        try { pool?.release() } catch (e: Exception) { }
        pool = null
        ids.clear()
    }
}
