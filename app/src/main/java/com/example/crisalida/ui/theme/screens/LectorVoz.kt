package com.example.crisalida.ui.theme.screens

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.crisalida.ui.theme.ColorBotonEntrar
import com.example.crisalida.ui.theme.ColorPlaceholder
import com.example.crisalida.ui.theme.ColorTextoPrincipal
import com.example.crisalida.ui.theme.JakartaSansFont
import java.util.Locale

/**
 * Lectura en voz alta (issue 8.7) con el TextToSpeech de Android.
 *
 * - Lee en español (México, Estados Unidos, España o genérico: el primero que el teléfono tenga). Si el teléfono no
 *   tiene voz en español, el botón explica que no está disponible.
 * - Se detiene al pulsar «Detener», al salir de la pantalla, al cambiar lo que se ve y al pasar la app a segundo plano.
 * - Lee solo textos de la pantalla (títulos, preguntas, opciones, explicaciones). NUNCA lo que la usuaria escribe
 *   ni su PIN, y tampoco el código de recuperación.
 */
enum class EstadoVoz { CARGANDO, LISTA, NO_DISPONIBLE }

class LectorVoz(contexto: Context) {
    var estado by mutableStateOf(EstadoVoz.CARGANDO)
        private set
    var hablando by mutableStateOf(false)
        private set

    private var motor: TextToSpeech? = null
    private var ultimoId = ""
    private var liberado = false

    init {
        motor = TextToSpeech(contexto.applicationContext) { codigo -> alIniciar(codigo) }
    }

    private fun alIniciar(codigo: Int) {
        val m = motor ?: return
        if (liberado) { m.shutdown(); return }
        if (codigo != TextToSpeech.SUCCESS) { estado = EstadoVoz.NO_DISPONIBLE; return }
        val idioma = listOf(Locale("es", "MX"), Locale("es", "US"), Locale("es", "ES"), Locale("es")).firstOrNull { l ->
            val r = try { m.isLanguageAvailable(l) } catch (e: Exception) { TextToSpeech.LANG_NOT_SUPPORTED }
            r >= TextToSpeech.LANG_AVAILABLE
        }
        if (idioma == null) { estado = EstadoVoz.NO_DISPONIBLE; return }
        val r = try { m.setLanguage(idioma) } catch (e: Exception) { TextToSpeech.LANG_NOT_SUPPORTED }
        if (r == TextToSpeech.LANG_MISSING_DATA || r == TextToSpeech.LANG_NOT_SUPPORTED) {
            estado = EstadoVoz.NO_DISPONIBLE
            return
        }
        m.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) { if (utteranceId == ultimoId) hablando = false }
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) { hablando = false }
            override fun onError(utteranceId: String?, errorCode: Int) { hablando = false }
            override fun onStop(utteranceId: String?, interrupted: Boolean) { if (utteranceId == ultimoId) hablando = false }
        })
        estado = EstadoVoz.LISTA
    }

    /** Lee las [lineas] una tras otra. Si ya estaba leyendo, empieza de nuevo con estas. */
    fun leer(lineas: List<String>) {
        val m = motor ?: return
        if (estado != EstadoVoz.LISTA) return
        val textos = lineas.map { it.trim() }.filter { it.isNotEmpty() }.flatMap { it.chunked(3500) }
        if (textos.isEmpty()) return
        val base = System.nanoTime().toString()
        ultimoId = "$base-${textos.lastIndex}"
        hablando = true
        textos.forEachIndexed { i, texto ->
            val modo = if (i == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
            val r = try { m.speak(texto, modo, null, "$base-$i") } catch (e: Exception) { TextToSpeech.ERROR }
            if (r == TextToSpeech.ERROR) { hablando = false; return }
        }
    }

    fun detener() {
        try { motor?.stop() } catch (e: Exception) { }
        hablando = false
    }

    /** Se llama al salir de la pantalla: calla la voz y suelta el motor. */
    fun liberar() {
        liberado = true
        try {
            motor?.stop()
            motor?.shutdown()
        } catch (e: Exception) { }
        motor = null
        hablando = false
    }
}

@Composable
private fun rememberLectorVoz(): LectorVoz {
    val contexto = LocalContext.current
    val lector = remember { LectorVoz(contexto) }
    val ciclo = (LocalActivity.current as? ComponentActivity)?.lifecycle
    DisposableEffect(lector) {
        val observador = LifecycleEventObserver { _, evento -> if (evento == Lifecycle.Event.ON_STOP) lector.detener() }
        ciclo?.addObserver(observador)
        onDispose {
            ciclo?.removeObserver(observador)
            lector.liberar()   // la voz se detiene al salir de la pantalla
        }
    }
    return lector
}

/**
 * Botón «Escuchar»: va al inicio de la pantalla. [lineas] es lo que se lee, en orden; si cambian (otra pregunta,
 * otro paso) la voz se calla. Con la lista vacía no se muestra nada.
 */
@Composable
fun BotonEscuchar(lineas: List<String>, modifier: Modifier = Modifier) {
    if (lineas.isEmpty()) return
    val lector = rememberLectorVoz()
    LaunchedEffect(lineas) { lector.detener() }

    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        when (lector.estado) {
            EstadoVoz.CARGANDO -> Spacer(modifier = Modifier.height(48.dp))
            EstadoVoz.NO_DISPONIBLE -> Text(
                text = "La lectura en voz alta no está disponible en tu teléfono (falta una voz en español).",
                fontFamily = JakartaSansFont,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                color = ColorPlaceholder,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            EstadoVoz.LISTA -> OutlinedButton(
                onClick = { if (lector.hablando) lector.detener() else lector.leer(lineas) },
                modifier = Modifier.heightIn(min = 48.dp),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, ColorBotonEntrar)
            ) {
                Icon(
                    imageVector = if (lector.hablando) Icons.Filled.Stop else Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = null,
                    tint = ColorTextoPrincipal,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (lector.hablando) "Detener" else "Escuchar",
                    fontFamily = JakartaSansFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = ColorTextoPrincipal
                )
            }
        }
    }
}
