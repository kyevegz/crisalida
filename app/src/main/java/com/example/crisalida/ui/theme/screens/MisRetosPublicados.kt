package com.example.crisalida.ui.theme.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crisalida.data.AceptacionPublicadaDto
import com.example.crisalida.data.FotoRemota
import com.example.crisalida.data.Haptica
import com.example.crisalida.data.ResultadoApi
import com.example.crisalida.data.RetoPublicadoDto
import com.example.crisalida.data.RetosPublicadosRepositorio
import com.example.crisalida.ui.theme.JakartaSansFont
import com.example.crisalida.ui.theme.OutfitFont
import com.example.crisalida.ui.theme.Paleta
import kotlinx.coroutines.launch

/* ================================================================
 * Mis retos y entregas (issue 7.6). Pestaña «Mis retos» del Perfil de la compradora.
 * - Lista de los retos que publicó, con su estado: Pendiente (en revisión), Activo, Rechazado
 *   (y también Completado / Cancelado cuando les toca).
 * - Cada reto muestra a quienes lo tomaron y sus entregas. Las entregas ya enviadas tienen Validar y Rechazar.
 * - Validar pide confirmar, da las flores a quien lo hizo y muestra la confirmación.
 * - Rechazar pide un motivo breve (la vendedora lo ve y puede corregir y volver a entregar).
 * Rutas del servidor: GET /retos/publicados, POST /retos/aceptaciones/{id}/validar y /rechazar (issues 7.3 y 7.4).
 * ================================================================ */

private val TituloReto: Color get() = Paleta.titulo
private val Subtitulo: Color get() = Paleta.textoGris

// la entrega sobre la que se está decidiendo
private data class EntregaElegida(val aceptacionId: String, val seudonimo: String, val tituloReto: String)

// lo que se muestra después de validar
private data class ValidacionHecha(val seudonimo: String, val tituloReto: String, val flores: Int)

private data class EtiquetaEstado(val texto: String, val fondo: Color, val letra: Color)

private fun etiquetaDe(estado: String): EtiquetaEstado = when (estado) {
    "en_revision" -> EtiquetaEstado("Pendiente", Paleta.amarilloFondo, Paleta.amarilloTexto)
    "abierto", "asignado" -> EtiquetaEstado("Activo", Paleta.verdeSuaveFondo, Paleta.verdeSuaveTexto)
    "completado" -> EtiquetaEstado("Completado", Paleta.etiquetaAzulFondo, Paleta.etiquetaAzulTexto)
    "rechazado" -> EtiquetaEstado("Rechazado", Paleta.fondoError, Paleta.error)
    "cancelado" -> EtiquetaEstado("Cancelado", Paleta.fondoGrisPerfil, Paleta.textoGris)
    else -> EtiquetaEstado(estado.replace('_', ' ').replaceFirstChar { it.uppercase() }, Paleta.fondoGrisPerfil, Paleta.textoGris)
}

// lo que se muestra del reto según su estado
private fun explicacionDe(reto: RetoPublicadoDto): String? = when (reto.estado) {
    "en_revision" -> "Nuestro equipo lo está revisando. Aún no aparece en el tablero."
    "rechazado" -> "No pasó la revisión. Motivo: " + (reto.motivoRechazo?.takeIf { it.isNotBlank() } ?: "no cumple las reglas de la comunidad") + "."
    "abierto" -> "Esperando a que una vendedora lo tome."
    "cancelado" -> "Este reto fue cancelado."
    else -> null
}

// solo las que siguen vigentes: tomadas, entregadas o validadas (las abandonadas no se muestran)
private fun entregasDe(reto: RetoPublicadoDto): List<AceptacionPublicadaDto> =
    if (reto.estado in listOf("abierto", "asignado", "completado"))
        reto.aceptaciones.filter { it.estado in listOf("aceptado", "entregado", "validado") }
    else emptyList()

/** Encabezado «Mis retos» y la lista. El botón «Añadir nuevo reto» lo pone el Perfil debajo. */
@Composable
fun SeccionMisRetosPublicados() {
    val carga = rememberCarga { RetosPublicadosRepositorio.mios() }
    val scope = rememberCoroutineScope()

    var porValidar by remember { mutableStateOf<EntregaElegida?>(null) }
    var porRechazar by remember { mutableStateOf<EntregaElegida?>(null) }
    var validada by remember { mutableStateOf<ValidacionHecha?>(null) }
    var aviso by remember { mutableStateOf<String?>(null) }

    val retos = (carga.estado as? Carga.Lista)?.datos?.retos ?: emptyList()

    Text(
        text = "Mis retos",
        fontFamily = OutfitFont,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        color = TituloReto,
        modifier = Modifier.semantics { heading() }
    )
    if (carga.estado is Carga.Lista) {
        Text(
            text = if (retos.size == 1) "1 microrreto publicado" else "${retos.size} microrretos publicados",
            fontFamily = JakartaSansFont,
            fontSize = 13.sp,
            color = Subtitulo
        )
    }
    Spacer(modifier = Modifier.height(12.dp))

    aviso?.let {
        Text(
            text = it,
            fontFamily = JakartaSansFont,
            fontSize = 14.sp,
            lineHeight = 19.sp,
            color = Paleta.verdeSuaveTexto,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Paleta.verdeSuaveFondo)
                .padding(12.dp)
                .semantics { liveRegion = LiveRegionMode.Polite }
        )
        Spacer(modifier = Modifier.height(12.dp))
    }

    when (val e = carga.estado) {
        is Carga.Cargando -> IndicadorCargando()
        is Carga.Falla -> ErrorDeCarga(e, carga.recargar, "No podemos mostrar tus retos sin conexión.")
        is Carga.Lista -> {
            if (retos.isEmpty()) {
                Text(
                    text = "Aún no has publicado microrretos. Cuando publiques uno, lo verás aquí.",
                    fontFamily = JakartaSansFont,
                    fontSize = 14.sp,
                    lineHeight = 19.sp,
                    color = Subtitulo
                )
            }
            retos.forEach { reto ->
                TarjetaMiReto(
                    reto = reto,
                    onValidar = { a -> porValidar = EntregaElegida(a.aceptacionId, a.seudonimo, reto.titulo) },
                    onRechazar = { a -> porRechazar = EntregaElegida(a.aceptacionId, a.seudonimo, reto.titulo) }
                )
                Spacer(modifier = Modifier.height(14.dp))
            }
        }
    }

    porValidar?.let { entrega ->
        DialogoValidarEntrega(
            entrega = entrega,
            onCancelar = { porValidar = null },
            onValidada = { flores ->
                porValidar = null
                aviso = null
                Haptica.exito()
                validada = ValidacionHecha(entrega.seudonimo, entrega.tituloReto, flores)
                carga.recargar()
            }
        )
    }

    porRechazar?.let { entrega ->
        DialogoRechazarEntrega(
            entrega = entrega,
            onCancelar = { porRechazar = null },
            onRechazada = {
                porRechazar = null
                aviso = "Listo. Le avisamos a ${entrega.seudonimo} que su entrega necesita cambios y por qué. Puede corregirla y volver a entregar."
                carga.recargar()
            }
        )
    }

    validada?.let { v ->
        AlertDialog(
            onDismissRequest = { validada = null },
            title = { Text("Entrega validada 🌸", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, color = Paleta.titulo) },
            text = {
                Text(
                    text = "Validaste la entrega de ${v.seudonimo} en «${v.tituloReto}». " +
                        (if (v.flores > 0) "Ganó ${v.flores} flores para Lilu. " else "") +
                        "El reto quedó como completado. ¡Gracias por tu ayuda!",
                    fontFamily = JakartaSansFont,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = Paleta.textoNegro
                )
            },
            confirmButton = {
                Button(
                    onClick = { validada = null },
                    modifier = Modifier.heightIn(min = 48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Paleta.tituloFijo)
                ) { Text("Entendido", color = Paleta.blanco) }
            },
            containerColor = Paleta.superficie
        )
    }
}

@Composable
private fun TarjetaMiReto(
    reto: RetoPublicadoDto,
    onValidar: (AceptacionPublicadaDto) -> Unit,
    onRechazar: (AceptacionPublicadaDto) -> Unit
) {
    val etiqueta = etiquetaDe(reto.estado)
    val entregas = entregasDe(reto)
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Paleta.fondoTarjetaPerfil),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Paleta.neutroClaro)
                ) {
                    FotoRemota(ruta = "/retos/${reto.retoId}/imagen", modifier = Modifier.fillMaxSize())
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(etiqueta.fondo)
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            Text(etiqueta.texto, fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = etiqueta.letra)
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        if (reto.categoria.isNotBlank()) {
                            Text(reto.categoria, fontFamily = JakartaSansFont, fontSize = 11.sp, color = Subtitulo, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                    Text(
                        text = reto.titulo,
                        fontFamily = OutfitFont, fontWeight = FontWeight.Bold, fontSize = 16.sp,
                        color = Paleta.textoNegro, maxLines = 2, overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = formatoPrecio(reto.compensacion) + " MXN",
                        fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Paleta.textoNegro
                    )
                }
            }

            reto.fechaLimite?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Paleta.lilaGris)
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Event, contentDescription = "Fecha límite", tint = Paleta.textoNegro, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(fechaLegible(it), fontFamily = JakartaSansFont, fontSize = 13.sp, color = Paleta.textoNegro)
                }
            }

            explicacionDe(reto)?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(it, fontFamily = JakartaSansFont, fontSize = 13.sp, lineHeight = 18.sp, color = Subtitulo)
            }

            if (entregas.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Entregas",
                    fontFamily = OutfitFont, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TituloReto,
                    modifier = Modifier.semantics { heading() }
                )
                entregas.forEach { entrega ->
                    Spacer(modifier = Modifier.height(8.dp))
                    FilaEntrega(entrega, onValidar = { onValidar(entrega) }, onRechazar = { onRechazar(entrega) })
                }
            }
        }
    }
}

@Composable
private fun FilaEntrega(entrega: AceptacionPublicadaDto, onValidar: () -> Unit, onRechazar: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Paleta.superficie)
            .padding(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(entrega.avatar ?: "🦋", fontSize = 22.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(entrega.seudonimo, fontFamily = OutfitFont, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Paleta.textoNegro)
                val situacion = when (entrega.estado) {
                    "entregado" -> "Entregó su trabajo" + (entrega.entregadoEn?.let { " el " + fechaLegible(it) } ?: "") + ". Falta tu revisión."
                    "validado" -> "Entrega validada ✓"
                    else -> "Tomó el reto y todavía no entrega."
                }
                Text(situacion, fontFamily = JakartaSansFont, fontSize = 13.sp, lineHeight = 18.sp, color = Subtitulo)
            }
        }
        // si ya se le pidieron cambios, se recuerda el motivo
        if (entrega.estado == "aceptado" && entrega.rechazos > 0 && !entrega.motivoRechazo.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Le pediste cambios: ${entrega.motivoRechazo}",
                fontFamily = JakartaSansFont, fontSize = 12.sp, lineHeight = 17.sp, color = Subtitulo
            )
        }
        if (entrega.estado == "entregado") {
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = onRechazar,
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Paleta.error)
                ) { Text("Rechazar", fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Paleta.error) }
                Button(
                    onClick = onValidar,
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Paleta.verdeBoton)
                ) { Text("Validar", fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Paleta.blanco) }
            }
        }
    }
}

@Composable
private fun DialogoValidarEntrega(entrega: EntregaElegida, onCancelar: () -> Unit, onValidada: (Int) -> Unit) {
    val scope = rememberCoroutineScope()
    var enviando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = { if (!enviando) onCancelar() },
        title = { Text("¿Validar esta entrega?", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, color = Paleta.titulo) },
        text = {
            Column {
                Text(
                    text = "Vas a dar por buena la entrega de ${entrega.seudonimo} en «${entrega.tituloReto}». " +
                        "Ella ganará flores para Lilu y el reto quedará completado. Esto no se puede deshacer.",
                    fontFamily = JakartaSansFont, fontSize = 14.sp, lineHeight = 20.sp, color = Paleta.textoNegro
                )
                error?.let {
                    Text(
                        it, fontFamily = JakartaSansFont, fontSize = 13.sp, color = Paleta.error,
                        modifier = Modifier.padding(top = 8.dp).semantics { liveRegion = LiveRegionMode.Polite }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    error = null
                    scope.launch {
                        enviando = true
                        when (val r = RetosPublicadosRepositorio.validar(entrega.aceptacionId)) {
                            is ResultadoApi.Exito -> onValidada(r.datos.floresOtorgadas)
                            is ResultadoApi.Error -> error = r.mensaje
                        }
                        enviando = false
                    }
                },
                enabled = !enviando,
                modifier = Modifier.heightIn(min = 48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Paleta.verdeBoton)
            ) {
                if (enviando) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Paleta.blanco, strokeWidth = 2.dp)
                else Text("Sí, validar", color = Paleta.blanco)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar, enabled = !enviando, modifier = Modifier.heightIn(min = 48.dp)) {
                Text("Cancelar", color = Paleta.titulo)
            }
        },
        containerColor = Paleta.superficie
    )
}

@Composable
private fun DialogoRechazarEntrega(entrega: EntregaElegida, onCancelar: () -> Unit, onRechazada: () -> Unit) {
    val scope = rememberCoroutineScope()
    var motivo by remember { mutableStateOf("") }
    var enviando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val limpio = motivo.trim()
    AlertDialog(
        onDismissRequest = { if (!enviando) onCancelar() },
        title = { Text("Rechazar entrega", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, color = Paleta.titulo) },
        text = {
            Column {
                Text(
                    text = "Cuéntale a ${entrega.seudonimo} qué debe cambiar. Podrá corregir su trabajo y volver a entregar. " +
                        "No incluyas datos de contacto ni direcciones.",
                    fontFamily = JakartaSansFont, fontSize = 14.sp, lineHeight = 20.sp, color = Paleta.textoNegro
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = motivo,
                    onValueChange = { if (it.length <= 300) motivo = it },
                    label = { Text("Motivo (de 5 a 300 caracteres)") },
                    supportingText = { Text("${limpio.length}/300") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                error?.let {
                    Text(
                        it, fontFamily = JakartaSansFont, fontSize = 13.sp, color = Paleta.error,
                        modifier = Modifier.padding(top = 6.dp).semantics { liveRegion = LiveRegionMode.Polite }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    error = null
                    scope.launch {
                        enviando = true
                        when (val r = RetosPublicadosRepositorio.rechazar(entrega.aceptacionId, limpio)) {
                            is ResultadoApi.Exito -> onRechazada()
                            is ResultadoApi.Error -> error = r.mensaje
                        }
                        enviando = false
                    }
                },
                enabled = limpio.length >= 5 && !enviando,
                modifier = Modifier.heightIn(min = 48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Paleta.tituloFijo)
            ) {
                if (enviando) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Paleta.blanco, strokeWidth = 2.dp)
                else Text("Rechazar entrega", color = Paleta.blanco)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar, enabled = !enviando, modifier = Modifier.heightIn(min = 48.dp)) {
                Text("Cancelar", color = Paleta.titulo)
            }
        },
        containerColor = Paleta.superficie
    )
}
