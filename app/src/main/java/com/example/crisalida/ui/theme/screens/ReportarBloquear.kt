package com.example.crisalida.ui.theme.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crisalida.data.BloqueoDto
import com.example.crisalida.data.MotivoReporteDto
import com.example.crisalida.data.MotivosRespaldo
import com.example.crisalida.data.ReporteRequest
import com.example.crisalida.data.ResultadoApi
import com.example.crisalida.data.SeguridadRepositorio
import com.example.crisalida.ui.theme.*
import kotlinx.coroutines.launch

private val RbVino = Color(0xFF6B3045)
private val RbGris = Color(0xFF545454)
private val RbRojo = Color(0xFFB3261E)

/** Qué se está reportando o bloqueando: la app identifica a la persona por su publicación o su reto. */
enum class ObjetivoSeguridad(val tipoReporte: String) { PUBLICACION("publicacion"), RETO("reto") }

private data class AvisoSeguridad(val titulo: String, val texto: String, val salir: Boolean)

/**
 * Botones «Reportar» y «Bloquear» con texto, para los detalles de producto y de reto (issue 4.9).
 * Toda acción se confirma con un mensaje claro. Si al final la persona quedó bloqueada se llama [onBloqueada]
 * (la pantalla regresa o se recarga, porque ya no se verá lo de esa persona).
 * [puedeBloquear] es falso, por ejemplo, en los retos de demo, que no tienen una cuenta detrás.
 */
@Composable
fun BotonesReportarBloquear(
    objetivo: ObjetivoSeguridad,
    objetivoId: String,
    nombre: String?,
    puedeBloquear: Boolean,
    onBloqueada: () -> Unit
) {
    ConDialogosSeguridad(objetivo, objetivoId, nombre, puedeBloquear, onBloqueada) { reportar, bloquear ->
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                onClick = reportar,
                modifier = Modifier.weight(1f).heightIn(min = 44.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Outlined.Flag, contentDescription = null, tint = RbVino, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Reportar", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, color = RbVino)
            }
            if (bloquear != null) {
                OutlinedButton(
                    onClick = bloquear,
                    modifier = Modifier.weight(1f).heightIn(min = 44.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Outlined.Block, contentDescription = null, tint = RbVino, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Bloquear", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, color = RbVino)
                }
            }
        }
    }
}

/**
 * Los mismos dos botones, solo con icono (círculos rosados: ⊘ bloquear y 🏳 reportar), para las tarjetas del
 * tablero de retos y del marketplace. Abren los mismos diálogos de confirmación.
 */
@Composable
fun IconosReportarBloquear(
    objetivo: ObjetivoSeguridad,
    objetivoId: String,
    nombre: String?,
    puedeBloquear: Boolean,
    onBloqueada: () -> Unit,
    modifier: Modifier = Modifier
) {
    val quien = nombre?.takeIf { it.isNotBlank() } ?: "esta persona"
    ConDialogosSeguridad(objetivo, objetivoId, nombre, puedeBloquear, onBloqueada) { reportar, bloquear ->
        Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (bloquear != null) BotonCircular(Icons.Outlined.Block, "Bloquear a $quien", bloquear)
            BotonCircular(Icons.Outlined.Flag, "Reportar", reportar)
        }
    }
}

@Composable
private fun BotonCircular(icono: androidx.compose.ui.graphics.vector.ImageVector, descripcion: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = ColorCamposFondo,
        modifier = Modifier.size(44.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Icon(icono, contentDescription = descripcion, tint = RbVino, modifier = Modifier.size(26.dp))
        }
    }
}

/** Guarda el estado de los diálogos y le pasa al contenido las dos acciones ([bloquear] es null si no se puede bloquear). */
@Composable
private fun ConDialogosSeguridad(
    objetivo: ObjetivoSeguridad,
    objetivoId: String,
    nombre: String?,
    puedeBloquear: Boolean,
    onBloqueada: () -> Unit,
    contenido: @Composable (reportar: () -> Unit, bloquear: (() -> Unit)?) -> Unit
) {
    var reportando by remember { mutableStateOf(false) }
    var bloqueando by remember { mutableStateOf(false) }
    var aviso by remember { mutableStateOf<AvisoSeguridad?>(null) }
    val quien = nombre?.takeIf { it.isNotBlank() } ?: "esta persona"

    contenido({ reportando = true }, if (puedeBloquear) ({ bloqueando = true }) else null)

    if (reportando) {
        DialogoReporte(
            objetivo = objetivo, objetivoId = objetivoId, puedeBloquear = puedeBloquear, quien = quien,
            onCancelar = { reportando = false },
            onEnviado = { bloqueada, mensaje ->
                reportando = false
                aviso = AvisoSeguridad(
                    "Reporte enviado",
                    mensaje.ifBlank { "Gracias. Recibimos tu reporte y lo revisaremos con cuidado." } +
                            if (bloqueada) "\n\nTambién bloqueaste a $quien. Puedes desbloquearla cuando quieras en Configuración → Privacidad." else "",
                    salir = bloqueada
                )
            }
        )
    }
    if (bloqueando) {
        DialogoBloquear(
            objetivo = objetivo, objetivoId = objetivoId, quien = quien,
            onCancelar = { bloqueando = false },
            onBloqueada = { seudonimo ->
                bloqueando = false
                aviso = AvisoSeguridad(
                    "Persona bloqueada",
                    "Bloqueaste a ${seudonimo.ifBlank { quien }}. Ya no verás sus publicaciones ni sus retos, y ella tampoco verá los tuyos. " +
                            "Puedes desbloquearla cuando quieras en Configuración → Privacidad → Usuarias bloqueadas.",
                    salir = true
                )
            }
        )
    }
    aviso?.let { a ->
        AlertDialog(
            onDismissRequest = {},   // se confirma con el botón
            title = { Text(a.titulo, fontFamily = OutfitFont, fontWeight = FontWeight.Bold, color = RbVino) },
            text = { Text(a.texto, fontFamily = JakartaSansFont, color = RbGris, modifier = Modifier.verticalScroll(rememberScrollState())) },
            confirmButton = {
                TextButton(onClick = { aviso = null; if (a.salir) onBloqueada() }) {
                    Text("Entendido", fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, color = RbVino)
                }
            },
            containerColor = Color.White
        )
    }
}

@Composable
private fun DialogoReporte(
    objetivo: ObjetivoSeguridad,
    objetivoId: String,
    puedeBloquear: Boolean,
    quien: String,
    onCancelar: () -> Unit,
    onEnviado: (bloqueada: Boolean, mensaje: String) -> Unit
) {
    val scope = rememberCoroutineScope()
    // los motivos vienen del servidor; si no hay conexión se usan los mismos guardados en la app
    val motivos by produceState(MotivosRespaldo) {
        (SeguridadRepositorio.motivos() as? ResultadoApi.Exito)?.datos?.motivos?.takeIf { it.isNotEmpty() }?.let { value = it }
    }
    var motivo by remember { mutableStateOf<String?>(null) }
    var detalle by remember { mutableStateOf("") }
    var prioritaria by remember { mutableStateOf(false) }
    var bloquear by remember { mutableStateOf(false) }
    var enviando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (!enviando) onCancelar() },
        title = { Text("Reportar", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, color = RbVino) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    "Cuéntanos qué pasó. Tu reporte es privado: la persona no sabrá que fuiste tú.",
                    fontFamily = JakartaSansFont, fontSize = 14.sp, color = RbGris
                )
                Spacer(Modifier.height(10.dp))
                Column(modifier = Modifier.selectableGroup()) {
                    motivos.forEach { m -> FilaMotivo(m, seleccionado = motivo == m.clave) { motivo = m.clave } }
                }
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = detalle,
                    onValueChange = { if (it.length <= 500) detalle = it },
                    label = { Text("Detalles (opcional)", fontFamily = JakartaSansFont) },
                    supportingText = { Text("${detalle.length}/500", fontFamily = JakartaSansFont) },
                    minLines = 2, maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
                FilaCasilla("Pedir revisión prioritaria", prioritaria) { prioritaria = it }
                if (puedeBloquear) FilaCasilla("Bloquear también a $quien", bloquear) { bloquear = it }
                error?.let {
                    Text(it, fontFamily = JakartaSansFont, fontSize = 13.sp, color = RbRojo, modifier = Modifier.padding(top = 6.dp))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val elegido = motivo ?: return@Button
                    error = null
                    scope.launch {
                        enviando = true
                        val pedido = ReporteRequest(
                            tipo = objetivo.tipoReporte, objetivoId = objetivoId, motivo = elegido,
                            detalle = detalle.trim().ifBlank { null },
                            bloquear = bloquear && puedeBloquear, revisionPrioritaria = prioritaria
                        )
                        when (val r = SeguridadRepositorio.reportar(pedido)) {
                            is ResultadoApi.Exito -> onEnviado(r.datos.bloqueada, r.datos.mensaje)
                            is ResultadoApi.Error -> error = r.mensaje
                        }
                        enviando = false
                    }
                },
                enabled = motivo != null && !enviando,
                colors = ButtonDefaults.buttonColors(containerColor = RbVino)
            ) {
                if (enviando) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                else Text("Enviar reporte", color = Color.White)
            }
        },
        dismissButton = { TextButton(onClick = onCancelar, enabled = !enviando) { Text("Cancelar", color = RbVino) } },
        containerColor = Color.White
    )
}

@Composable
private fun FilaMotivo(m: MotivoReporteDto, seleccionado: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().selectable(selected = seleccionado, onClick = onClick, role = Role.RadioButton).padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        RadioButton(selected = seleccionado, onClick = null, colors = RadioButtonDefaults.colors(selectedColor = RbVino))
        Spacer(Modifier.width(8.dp))
        Column {
            Text(m.titulo, fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
            if (m.descripcion.isNotBlank()) Text(m.descripcion, fontFamily = JakartaSansFont, fontSize = 12.sp, lineHeight = 16.sp, color = RbGris)
        }
    }
}

@Composable
private fun FilaCasilla(texto: String, marcada: Boolean, onCambio: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().selectable(selected = marcada, onClick = { onCambio(!marcada) }, role = Role.Checkbox),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = marcada, onCheckedChange = null, colors = CheckboxDefaults.colors(checkedColor = RbVino))
        Spacer(Modifier.width(8.dp))
        Text(texto, fontFamily = JakartaSansFont, fontSize = 14.sp, color = Color.Black)
    }
}

@Composable
private fun DialogoBloquear(
    objetivo: ObjetivoSeguridad,
    objetivoId: String,
    quien: String,
    onCancelar: () -> Unit,
    onBloqueada: (seudonimo: String) -> Unit
) {
    val scope = rememberCoroutineScope()
    var enviando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = { if (!enviando) onCancelar() },
        title = { Text("¿Bloquear a $quien?", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, color = RbVino) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    "Dejarás de ver sus publicaciones y sus retos, y ella dejará de ver los tuyos. No se entera de que la bloqueaste. " +
                            "Puedes desbloquearla en Configuración → Privacidad.",
                    fontFamily = JakartaSansFont, color = RbGris
                )
                error?.let { Text(it, fontFamily = JakartaSansFont, fontSize = 13.sp, color = RbRojo, modifier = Modifier.padding(top = 8.dp)) }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    error = null
                    scope.launch {
                        enviando = true
                        val r = if (objetivo == ObjetivoSeguridad.PUBLICACION) SeguridadRepositorio.bloquearPorPublicacion(objetivoId)
                        else SeguridadRepositorio.bloquearPorReto(objetivoId)
                        when (r) {
                            is ResultadoApi.Exito -> onBloqueada(r.datos.seudonimo)
                            is ResultadoApi.Error -> error = r.mensaje
                        }
                        enviando = false
                    }
                },
                enabled = !enviando,
                colors = ButtonDefaults.buttonColors(containerColor = RbVino)
            ) {
                if (enviando) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                else Text("Bloquear", color = Color.White)
            }
        },
        dismissButton = { TextButton(onClick = onCancelar, enabled = !enviando) { Text("Cancelar", color = RbVino) } },
        containerColor = Color.White
    )
}

// ---------------------------------------------------------------- Configuración → Privacidad → Usuarias bloqueadas

@Composable
fun ListaBloqueadas() {
    val scope = rememberCoroutineScope()
    var version by remember { mutableIntStateOf(0) }
    val carga = rememberCarga(version) { SeguridadRepositorio.bloqueos() }
    var porDesbloquear by remember { mutableStateOf<BloqueoDto?>(null) }
    var desbloqueando by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf<Pair<String, Boolean>?>(null) }   // texto, ¿fue éxito?

    Text(
        "Las personas que bloqueas no ven lo que publicas y tú no ves lo suyo. No se enteran de que las bloqueaste.",
        fontFamily = JakartaSansFont, fontSize = 14.sp, lineHeight = 19.sp, color = RbGris
    )
    Spacer(Modifier.height(8.dp))
    when (val e = carga.estado) {
        is Carga.Cargando -> IndicadorCargando()
        is Carga.Falla -> ErrorDeCarga(e, carga.recargar, "No podemos mostrar tus bloqueos sin conexión.")
        is Carga.Lista -> if (e.datos.bloqueos.isEmpty()) {
            Text("No has bloqueado a nadie.", fontFamily = JakartaSansFont, fontSize = 14.sp, color = RbGris)
        } else e.datos.bloqueos.forEach { b ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(b.avatar ?: "🦋", fontSize = 26.sp)
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(b.seudonimo, fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.Black)
                    if (b.creadoEn.isNotBlank()) Text("Bloqueada el ${fechaLegible(b.creadoEn)}", fontFamily = JakartaSansFont, fontSize = 12.sp, color = RbGris)
                }
                OutlinedButton(onClick = { mensaje = null; porDesbloquear = b }, shape = RoundedCornerShape(8.dp), modifier = Modifier.heightIn(min = 44.dp)) {
                    Text("Desbloquear", fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = RbVino)
                }
            }
        }
    }
    mensaje?.let { (texto, exito) ->
        Text(texto, fontFamily = JakartaSansFont, fontSize = 13.sp, color = if (exito) Color(0xFF1E681E) else RbRojo, modifier = Modifier.padding(top = 6.dp))
    }

    porDesbloquear?.let { b ->
        AlertDialog(
            onDismissRequest = { if (!desbloqueando) porDesbloquear = null },
            title = { Text("¿Desbloquear a ${b.seudonimo}?", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, color = RbVino) },
            text = { Text("Volverás a ver sus publicaciones y sus retos, y ella volverá a ver los tuyos.", fontFamily = JakartaSansFont, color = RbGris) },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            desbloqueando = true
                            when (val r = SeguridadRepositorio.desbloquear(b.id)) {
                                is ResultadoApi.Exito -> { mensaje = "Desbloqueaste a ${b.seudonimo}." to true; version++ }
                                is ResultadoApi.Error -> mensaje = r.mensaje to false
                            }
                            desbloqueando = false
                            porDesbloquear = null
                        }
                    },
                    enabled = !desbloqueando,
                    colors = ButtonDefaults.buttonColors(containerColor = RbVino)
                ) { Text("Desbloquear", color = Color.White) }
            },
            dismissButton = { TextButton(onClick = { porDesbloquear = null }, enabled = !desbloqueando) { Text("Cancelar", color = RbVino) } },
            containerColor = Color.White
        )
    }
}