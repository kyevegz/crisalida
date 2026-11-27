package com.example.crisalida.ui.theme.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.crisalida.data.CONTROL_VOZ_NO_ENTENDI
import com.example.crisalida.data.DICTADO_SIN_PERMISO
import com.example.crisalida.data.Haptica
import com.example.crisalida.data.OrdenVoz
import com.example.crisalida.data.interpretarOrden
import com.example.crisalida.ui.theme.ColorTextoPrincipal
import com.example.crisalida.ui.theme.JakartaSansFont
import com.example.crisalida.ui.theme.Paleta
import kotlinx.coroutines.delay

private const val SEGUNDOS_MAX_ORDEN = 5

/** Estado del control por voz: escucha UNA orden (máximo 5 segundos) y se la entrega a quien la ejecuta. */
class ControlVoz internal constructor(
    private val dictador: Dictador,
    private val respuestaDeLaOrden: () -> String?,
    private val empezar: () -> Unit
) {
    val escuchando: Boolean get() = dictador.escuchando
    val mensaje: String? get() = respuestaDeLaOrden() ?: dictador.mensaje
    fun alternar() { if (dictador.escuchando) dictador.parar() else empezar() }
}

/**
 * Crea el control por voz. [alOrden] ejecuta la orden y devuelve un texto para mostrar (por ejemplo, por qué no se
 * pudo) o null si todo salió bien. Pide el permiso del micrófono solo al tocarlo, con una explicación antes.
 */
@Composable
fun rememberControlVoz(alOrden: (OrdenVoz) -> String?): ControlVoz {
    val contexto = LocalContext.current
    val dictador = remember { Dictador(contexto, mensajeNoEntendido = CONTROL_VOZ_NO_ENTENDI) }
    val ordenAhora by rememberUpdatedState(alOrden)
    var respuesta by remember { mutableStateOf<String?>(null) }
    var explicando by remember { mutableStateOf(false) }

    val recibir: (String) -> Unit = { frase ->
        val orden = interpretarOrden(frase)
        if (orden == null) Haptica.error()   // no entendió: golpe seco
        respuesta = if (orden == null) CONTROL_VOZ_NO_ENTENDI else ordenAhora(orden)
    }
    val pedirPermiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (concedido) dictador.iniciar(recibir) else dictador.avisar(DICTADO_SIN_PERMISO)
    }

    // máximo 5 segundos escuchando una orden
    LaunchedEffect(dictador.escuchando) {
        if (dictador.escuchando) {
            delay(SEGUNDOS_MAX_ORDEN * 1000L)
            dictador.parar()
        }
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

    if (explicando) {
        AlertDialog(
            onDismissRequest = { explicando = false },
            title = { Text("Control por voz", fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Para escuchar tu orden, Crisálida necesita usar el micrófono de tu teléfono, solo unos segundos cada vez. " +
                            "No guardamos tu voz. Según tu teléfono, el reconocimiento puede usar el servicio de voz de Google.",
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

    return remember(dictador) {
        ControlVoz(
            dictador = dictador,
            respuestaDeLaOrden = { respuesta },
            empezar = {
                respuesta = null
                if (ContextCompat.checkSelfPermission(contexto, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                    dictador.iniciar(recibir)
                } else {
                    explicando = true
                }
            }
        )
    }
}

/** Micrófono pequeño para la barra superior. Mientras escucha se pone de color y cambia a «detener». */
@Composable
fun BotonControlVozIcono(control: ControlVoz, modifier: Modifier = Modifier) {
    IconButton(
        onClick = { control.alternar() },
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(if (control.escuchando) Paleta.vinoFijo else Paleta.fondoBotonVolver)
            .semantics {
                contentDescription = if (control.escuchando) "Escuchando una orden. Toca para detener" else "Control por voz"
            }
    ) {
        Icon(
            imageVector = if (control.escuchando) Icons.Filled.Stop else Icons.Filled.Mic,
            contentDescription = null,
            tint = if (control.escuchando) Paleta.blanco else ColorTextoPrincipal,
            modifier = Modifier.size(24.dp)
        )
    }
}

/** Botón con texto para pantallas sin barra superior (el radar). */
@Composable
fun BotonControlVozTexto(control: ControlVoz, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = { control.alternar() },
        modifier = modifier
            .heightIn(min = 48.dp)
            .semantics {
                contentDescription = if (control.escuchando) "Escuchando una orden. Toca para detener" else "Control por voz"
            },
        shape = RoundedCornerShape(24.dp)
    ) {
        Icon(
            imageVector = if (control.escuchando) Icons.Filled.Stop else Icons.Filled.Mic,
            contentDescription = null,
            tint = ColorTextoPrincipal,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = if (control.escuchando) "Detener" else "Hablar",
            fontFamily = JakartaSansFont,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = ColorTextoPrincipal
        )
    }
}

/** Franja visible: «Escuchando…» mientras escucha, y después lo que pasó (por ejemplo, «No te entendí…»). */
@Composable
fun FranjaControlVoz(control: ControlVoz, modifier: Modifier = Modifier) {
    val texto = when {
        control.escuchando -> "Escuchando… di una orden (por ejemplo: retos, inicio, respirar o volver)"
        else -> control.mensaje
    } ?: return
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = if (control.escuchando) Paleta.vinoFijo else Paleta.fondoAyuda,
        shadowElevation = 4.dp
    ) {
        Text(
            text = texto,
            fontFamily = JakartaSansFont,
            fontWeight = if (control.escuchando) FontWeight.Bold else FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 19.sp,
            color = if (control.escuchando) Paleta.blanco else ColorTextoPrincipal,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .semantics { liveRegion = LiveRegionMode.Polite }
        )
    }
}
