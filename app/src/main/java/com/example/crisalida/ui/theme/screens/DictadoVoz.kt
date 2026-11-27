package com.example.crisalida.ui.theme.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.crisalida.data.DICTADO_SIN_PERMISO
import com.example.crisalida.data.DICTADO_SIN_SERVICIO
import com.example.crisalida.data.SesionActual
import com.example.crisalida.data.mensajeDeErrorDictado
import com.example.crisalida.ui.theme.ColorBotonEntrar
import com.example.crisalida.ui.theme.ColorPlaceholder
import com.example.crisalida.ui.theme.ColorTextoPrincipal
import com.example.crisalida.ui.theme.JakartaSansFont
import com.example.crisalida.ui.theme.Paleta

/**
 * Dictado por voz (issue 10.3) con el SpeechRecognizer de Android.
 *
 * - Español de México y preferencia por reconocer SIN internet. En Android 12 o más usa el reconocedor del propio
 *   teléfono cuando existe.
 * - NO guarda el audio: la app solo recibe el texto. Según el teléfono, el servicio de voz puede usar internet.
 * - Solo se usa en textos largos (descripciones y bienestar). Nunca en PIN, correo, código de recuperación ni seudónimo.
 */
class Dictador(private val contexto: Context) {
    var escuchando by mutableStateOf(false)
        private set
    var mensaje by mutableStateOf<String?>(null)
        private set

    private var reconocedor: SpeechRecognizer? = null
    private var alTexto: ((String) -> Unit)? = null

    private val oyente = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}
        override fun onPartialResults(partialResults: Bundle?) {}
        override fun onEvent(eventType: Int, params: Bundle?) {}

        override fun onResults(results: Bundle?) {
            val frase = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull().orEmpty()
            val entregar = alTexto
            terminar()
            if (frase.isBlank()) mensaje = mensajeDeErrorDictado(SpeechRecognizer.ERROR_NO_MATCH)
            else entregar?.invoke(frase)   // el texto se agrega al final de lo escrito (lo decide quien llama)
        }

        override fun onError(error: Int) {
            terminar()
            mensaje = mensajeDeErrorDictado(error)
        }
    }

    fun iniciar(alTexto: (String) -> Unit) {
        if (escuchando) return
        mensaje = null
        val app = contexto.applicationContext
        val enElTelefono = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && SpeechRecognizer.isOnDeviceRecognitionAvailable(app)
        if (!enElTelefono && !SpeechRecognizer.isRecognitionAvailable(app)) {
            mensaje = DICTADO_SIN_SERVICIO
            return
        }
        try {
            val r = if (enElTelefono) SpeechRecognizer.createOnDeviceSpeechRecognizer(app) else SpeechRecognizer.createSpeechRecognizer(app)
            r.setRecognitionListener(oyente)
            reconocedor = r
            this.alTexto = alTexto
            escuchando = true
            r.startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-MX")
                putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            })
        } catch (e: Exception) {
            terminar()
            mensaje = DICTADO_SIN_SERVICIO
        }
    }

    /** Termina de escuchar y entrega lo que alcanzó a entender. */
    fun parar() {
        try { reconocedor?.stopListening() } catch (e: Exception) { terminar() }
    }

    /** Corta todo sin insertar nada: al salir de la pantalla o pasar la app a segundo plano. */
    fun cancelar() {
        alTexto = null
        try { reconocedor?.cancel() } catch (e: Exception) { }
        terminar()
    }

    fun avisar(texto: String) { mensaje = texto }

    private fun terminar() {
        escuchando = false
        alTexto = null
        try { reconocedor?.destroy() } catch (e: Exception) { }
        reconocedor = null
    }
}

/**
 * Botón «Dictar» para un campo de texto largo. [alDictar] recibe la frase y quien lo usa la agrega al final del campo
 * (con unirDictado). Pide el permiso del micrófono solo al tocarlo, con una explicación antes.
 */
@Composable
fun BotonDictado(alDictar: (String) -> Unit, modifier: Modifier = Modifier) {
    if (SesionActual.esCamuflaje) return
    val contexto = LocalContext.current
    val dictador = remember { Dictador(contexto) }
    val dictarAhora by rememberUpdatedState(alDictar)
    var explicando by remember { mutableStateOf(false) }

    val pedirPermiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (concedido) dictador.iniciar { dictarAhora(it) } else dictador.avisar(DICTADO_SIN_PERMISO)
    }

    // se corta al salir de la pantalla y al pasar la app a segundo plano
    val ciclo = (LocalActivity.current as? ComponentActivity)?.lifecycle
    DisposableEffect(dictador, ciclo) {
        val observador = LifecycleEventObserver { _, evento -> if (evento == Lifecycle.Event.ON_STOP) dictador.cancelar() }
        ciclo?.addObserver(observador)
        onDispose {
            ciclo?.removeObserver(observador)
            dictador.cancelar()
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = {
                when {
                    dictador.escuchando -> dictador.parar()
                    ContextCompat.checkSelfPermission(contexto, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED ->
                        dictador.iniciar { dictarAhora(it) }
                    else -> explicando = true
                }
            },
            modifier = Modifier
                .heightIn(min = 48.dp)
                .semantics {
                    contentDescription = if (dictador.escuchando) "Escuchando. Toca para dejar de dictar" else "Dictar con la voz"
                },
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, ColorBotonEntrar)
        ) {
            Icon(
                imageVector = if (dictador.escuchando) Icons.Filled.Stop else Icons.Filled.Mic,
                contentDescription = null,
                tint = ColorTextoPrincipal,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (dictador.escuchando) "Detener" else "Dictar",
                fontFamily = JakartaSansFont,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = ColorTextoPrincipal
            )
        }
        if (dictador.escuchando) {
            Text(
                text = "Escuchando… habla ahora",
                fontFamily = JakartaSansFont,
                fontSize = 13.sp,
                color = Paleta.textoNegro,
                modifier = Modifier.padding(top = 4.dp).semantics { liveRegion = LiveRegionMode.Polite }
            )
        }
        dictador.mensaje?.let {
            Text(
                text = it,
                fontFamily = JakartaSansFont,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                color = ColorPlaceholder,
                modifier = Modifier.padding(top = 4.dp).semantics { liveRegion = LiveRegionMode.Polite }
            )
        }
    }

    if (explicando) {
        AlertDialog(
            onDismissRequest = { explicando = false },
            title = { Text("Dictar con tu voz", fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Para dictar, Crisálida necesita usar el micrófono de tu teléfono, solo mientras estés escuchando. " +
                            "No guardamos tu voz: el texto aparece en el campo y puedes corregirlo. " +
                            "Según tu teléfono, el reconocimiento puede usar el servicio de voz de Google.",
                    fontFamily = JakartaSansFont, fontSize = 14.sp, lineHeight = 20.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { explicando = false; pedirPermiso.launch(Manifest.permission.RECORD_AUDIO) },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) { Text("Continuar") }
            },
            dismissButton = {
                TextButton(onClick = { explicando = false }, modifier = Modifier.heightIn(min = 48.dp)) { Text("Ahora no") }
            }
        )
    }
}
