package com.example.crisalida.ui.theme.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crisalida.data.ColaEnvios
import com.example.crisalida.data.EstadoResponse
import com.example.crisalida.data.FotoRemota
import com.example.crisalida.data.ProgresoRepositorio
import com.example.crisalida.data.ResultadoApi
import com.example.crisalida.data.SesionActual
import com.example.crisalida.data.RetoDto
import com.example.crisalida.data.RetosRepositorio
import com.example.crisalida.ui.theme.*
import kotlinx.coroutines.launch
import com.example.crisalida.ui.theme.screens.CrisalidaScaffold
import com.example.crisalida.ui.theme.Paleta
private val RetoVerde: Color get() = Paleta.verdeBoton
private val RetoGris: Color get() = Paleta.textoGris
private val RetoCaja: Color get() = Paleta.tarjetaGris

//detalle del microrreto
@Composable
fun RetoDetalleScreen(
    retoId: String,
    onNavigate: (String) -> Unit,
    onVolver: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var version by remember { mutableIntStateOf(0) }
    val carga = rememberCarga(retoId, version) { RetosRepositorio.detalle(retoId) }
    var aviso by remember { mutableStateOf<String?>(null) }
    var ocupado by remember { mutableStateOf(false) }

    fun ejecutar(accion: suspend () -> ResultadoApi<*>, exito: String) {
        if (ocupado) return
        ocupado = true
        scope.launch {
            aviso = when (val r = accion()) {
                is ResultadoApi.Exito ->
                    if ((r.datos as? EstadoResponse)?.estado == ColaEnvios.ESTADO_EN_COLA) "Sin conexión: tu entrega quedó guardada en tu teléfono y se enviará sola cuando vuelva el internet. Se marcará como entregada cuando el servidor lo confirme." else exito
                is ResultadoApi.Error -> r.mensaje
            }
            ocupado = false
            version++
            ProgresoRepositorio.avisarCambio()
        }
    }

    CrisalidaScaffold(currentTitle = "Detalle del reto", selectedTab = 0, onNavigate = onNavigate) { innerPadding ->
        ContenidoCentrado(innerPadding) {
            Column(modifier = Modifier.fillMaxSize()) {
                BarraVolver("Volver al tablero", onVolver)
                when (val e = carga.estado) {
                    is Carga.Cargando -> IndicadorCargando()
                    is Carga.Falla -> ErrorDeCarga(e, carga.recargar, "No podemos abrir este reto ni aceptarlo sin conexión.")
                    is Carga.Lista -> {
                        val reto = e.datos
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 20.dp)
                        ) {
                            Contenido(reto, ocupado, onVolver = onVolver, onAceptar = {
                                ejecutar({ RetosRepositorio.aceptar(reto.retoId) },
                                    "¡Reto aceptado! Lo encuentras en «Mis retos». Cuando termines, márcalo como entregado.")
                            }, onEntregar = {
                                ejecutar({ RetosRepositorio.entregar(reto.aceptacionId.orEmpty()) },
                                    "¡Entregado! Cuando quien publicó el reto lo valide, recibirás tus flores.")
                            }, onSoltar = {
                                ejecutar({ RetosRepositorio.abandonar(reto.aceptacionId.orEmpty()) },
                                    "Soltaste el reto. Volvió al tablero para otra persona.")
                            })
                        }
                    }
                }
            }
        }
    }

    aviso?.let { texto ->
        AlertDialog(
            onDismissRequest = { aviso = null },
            text = { Text(text = texto, fontFamily = OutfitFont, color = Paleta.textoNegro, modifier = Modifier.verticalScroll(rememberScrollState())) },
            confirmButton = {
                TextButton(onClick = { aviso = null }) {
                    Text("Entendido", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, color = RetoVerde)
                }
            },
            containerColor = Paleta.superficie
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ColumnScope.Contenido(
    reto: RetoDto,
    ocupado: Boolean,
    onVolver: () -> Unit,
    onAceptar: () -> Unit,
    onEntregar: () -> Unit,
    onSoltar: () -> Unit
) {
    FotoRemota(
        ruta = reto.imagenRuta,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(12.dp)),
        alternativa = {
            Box(
                modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(12.dp)).background(ColorCamposFondo),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Outlined.Image, contentDescription = null, tint = RetoGris, modifier = Modifier.size(48.dp)) }
        }
    )
    Spacer(Modifier.height(14.dp))
    Text(reto.titulo, fontFamily = OutfitFont, fontWeight = FontWeight.Bold, fontSize = 26.sp, lineHeight = 31.sp, color = Paleta.textoNegro)
    Spacer(Modifier.height(6.dp))
    Text("${formatoPrecio(reto.compensacion)} MXN", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, fontSize = 24.sp, color = RetoVerde)
    Text("Pago al completar el reto y que sea validado", fontFamily = JakartaSansFont, fontSize = 13.sp, lineHeight = 18.sp, color = RetoGris)
    Spacer(Modifier.height(12.dp))

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Etiqueta(reto.categoria)
        Etiqueta(dificultadTexto(reto.dificultad))
        reto.etiqueta?.takeIf { it != reto.categoria }?.let { Etiqueta(it) }
    }

    Spacer(Modifier.height(18.dp))
    Text("Lo que se pide", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Paleta.textoNegro)
    Spacer(Modifier.height(6.dp))
    Text(reto.descripcion, fontFamily = JakartaSansFont, fontSize = 15.sp, lineHeight = 22.sp, color = Paleta.textoNeutro)

    Spacer(Modifier.height(18.dp))
    Column(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(RetoCaja).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Dato(Icons.Outlined.Schedule, "Duración aproximada", "${reto.duracionMin} minutos")
        Dato(Icons.Outlined.AccountTree, "Habilidad que se usa", reto.habilidadRequerida ?: "Ninguna en especial")
        Dato(Icons.Outlined.Event, "Fecha límite", fechaLegible(reto.fechaLimite))
        Dato(Icons.Outlined.Storefront, "Lo pide", reto.contratista)
    }

    // la compradora solo consulta el reto: sin texto de entrega ni botones para aceptarlo
    if (!SesionActual.esCompradora) {
        Spacer(Modifier.height(10.dp))
        Text(
            "Al entregarlo, el contratista lo revisa. Cuando lo valide, el reto cuenta para tu nivel y ganas flores para Lilu.",
            fontFamily = JakartaSansFont, fontSize = 13.sp, lineHeight = 18.sp, color = RetoGris
        )

        Spacer(Modifier.height(18.dp))
        when (reto.miEstado) {
            "aceptado" -> {
                Boton("Marcar como entregado", !ocupado, onEntregar)
                TextButton(onClick = onSoltar, enabled = !ocupado, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text("Soltar este reto", fontFamily = OutfitFont, fontSize = 14.sp, color = RetoGris)
                }
            }
            "entregado" -> Boton("Esperando validación", false) {}
            "validado" -> Boton("Completado ✓", false) {}
            else -> Boton("Aceptar el reto", !ocupado, onAceptar)
        }
    }
    // reportar o bloquear a quien publicó el reto (privado). No aparece en el reto que publicaste tú misma.
    if (!reto.esMio) {
        Spacer(Modifier.height(12.dp))
        BotonesReportarBloquear(
            objetivo = ObjetivoSeguridad.RETO, objetivoId = reto.retoId, nombre = reto.contratista,
            puedeBloquear = reto.bloqueable, onBloqueada = onVolver
        )
    }
    Spacer(Modifier.height(28.dp))
}

@Composable
private fun Boton(texto: String, habilitado: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = habilitado,
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = RetoVerde)
    ) { Text(texto, fontFamily = OutfitFont, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Paleta.blancoSuave) }
}

@Composable
private fun Etiqueta(texto: String) {
    Box(modifier = Modifier.clip(CircleShape).background(Paleta.barraFondo).padding(horizontal = 12.dp, vertical = 4.dp)) {
        Text(texto, fontFamily = OutfitFont, fontSize = 13.sp, lineHeight = 18.sp, color = Paleta.textoVinoOscuro)
    }
}

@Composable
private fun Dato(icono: ImageVector, titulo: String, valor: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(icono, contentDescription = null, tint = RetoGris, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Column {
            Text(titulo, fontFamily = JakartaSansFont, fontSize = 12.sp, color = RetoGris)
            Text(valor, fontFamily = OutfitFont, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = Paleta.textoNegro)
        }
    }
}


@PreviewResponsivo
@Composable
private fun RetoDetallePreview() {
    Column(
        modifier = Modifier
            .background(ColorFondoApp)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Contenido(
            reto = RetoDto(
                retoId = "1",
                titulo = "Etiquetado de frascos para mermelada artesanal",
                descripcion = "Se necesitan 60 etiquetas pegadas y alineadas en frascos de 250 ml, con el nombre del producto hacia el frente. El material se entrega en la tienda.",
                compensacion = 350.0, duracionMin = 90, dificultad = "media",
                habilidadRequerida = "Trabajo manual y atención al detalle", etiqueta = "Manualidades",
                fechaLimite = "2026-12-05", categoriaId = 1, categoria = "Manualidades",
                contratista = "Mermeladas La Abuela", aceptacionId = "a1", estado = "aceptado",
                imagenRuta = null, miEstado = "aceptado"
            ),
            ocupado = false, onVolver = {}, onAceptar = {}, onEntregar = {}, onSoltar = {}
        )
    }
}