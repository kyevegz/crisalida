package com.example.crisalida.ui.theme.screens

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.LocalFlorist
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crisalida.R
import com.example.crisalida.ui.theme.*
import com.example.crisalida.data.FotoRemota
import com.example.crisalida.data.ProgresoRepositorio
import com.example.crisalida.data.PublicacionesRepositorio
import com.example.crisalida.data.ResultadoApi
import com.example.crisalida.data.SesionActual
import kotlinx.coroutines.launch
import com.example.crisalida.ui.screens.*
/* ──────────────────────────────────────────────────────────────
 * Colores medidos del mockup de Perfil y Tarjetas
 * ────────────────────────────────────────────────────────────── */
private val MkTitulo = Color(0xFF7F284F)
private val MkSubtitulo = Color(0xFF545454)
private val MkBarraProgreso = Color(0xFFB82C67)
private val MkBarraFondo = Color(0xFFE8D7DD)
private val MkVerdeActivoFondo = Color(0xFFD4EDDA)
private val MkVerdeActivoTexto = Color(0xFF155724)
private val MkAmarilloEncargoFondo = Color(0xFFFFF3CD)
private val MkAmarilloEncargoTexto = Color(0xFF856404)
private val MkGrisPausadoFondo = Color(0xFFE2D8DE)
private val MkGrisPausadoTexto = Color(0xFF4A3B43)
private val MkFondoCard = Color(0xFFFFFFFF)
private val MkFondoItem = Color(0xFFEFE8EC) // Tono rosado claro de la card del portafolio

data class PublicacionPortafolio(
    val id: String,
    val titulo: String,
    val precio: String,
    var estado: String, // "Activo", "Por encargo", "Pausado"
    val detalleVentas: String,
    val ubicacionDetalle: String,
    val pedidosCount: Int,
    @DrawableRes val imagenRes: Int? = null,
    val imagenRuta: String? = null   // foto que viene del servidor (ver FotoRemota)
)

data class HabilidadInsignia(
    val titulo: String,
    val descripcion: String,
    val categoria: String
)

@Composable
fun PerfilScreen(
    onNavigate: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    val scope = rememberCoroutineScope()
    var mostrarSelectorAvatar by remember { mutableStateOf(false) }
    var avatarSeleccionado by remember { mutableStateOf(SesionActual.avatar) }
    var mensajeInfo by remember { mutableStateOf<String?>(null) }

    // ── Todo viene del servidor ──
    val progreso = rememberProgreso()
    val datosProgreso = (progreso.estado as? Carga.Lista)?.datos
    val portafolio = rememberCarga(ProgresoRepositorio.version) { PublicacionesRepositorio.mias() }
    val habilidadesCarga = rememberCarga { ProgresoRepositorio.habilidades() }

    // Diálogos de confirmación para pausar/reactivar
    var publicacionSeleccionadaParaAccion by remember { mutableStateOf<PublicacionPortafolio?>(null) }
    var tipoAccionPendiente by remember { mutableStateOf<String?>(null) } // "pausar" o "reactivar"

    val publicacionesPortafolio: List<PublicacionPortafolio> =
        (portafolio.estado as? Carga.Lista)?.datos?.publicaciones?.map { it.aPortafolioUi() } ?: emptyList()

    val habilidadesInsignias: List<HabilidadInsignia> =
        (habilidadesCarga.estado as? Carga.Lista)?.datos?.habilidades
            ?.map { HabilidadInsignia(it.titulo, it.descripcion, it.categoria) } ?: emptyList()

    CrisalidaScaffold(
        currentTitle = "Perfil",
        selectedTab = 5,
        onNavigate = onNavigate
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ColorFondoApp)
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(18.dp))

            // Cabecera Perfil
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF8A6D9E)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = avatarSeleccionado, fontSize = 50.sp)
                    }
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(Color(0xFF2E7D32)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = datosProgreso?.nivel?.nivel?.toString() ?: "–",
                                fontFamily = OutfitFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = SesionActual.seudonimo,
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        color = MkTitulo
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    TextButton(
                        onClick = { mostrarSelectorAvatar = true },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = null,
                            tint = MkTitulo,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Editar avatar",
                            fontFamily = JakartaSansFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MkTitulo
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (progreso.estado is Carga.Falla) {
                MensajeError((progreso.estado as Carga.Falla).mensaje, progreso.recargar)
            }

            // Tarjeta Nivel
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MkFondoCard),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LocalFlorist,
                            contentDescription = null,
                            tint = MkTitulo,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = datosProgreso?.let { "Nivel ${it.nivel.nivel}: ${it.nivel.nombre}" } ?: "Cargando tu nivel…",
                            fontFamily = OutfitFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MkTitulo
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(MkBarraFondo)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(((datosProgreso?.nivel?.porcentaje ?: 0) / 100f).coerceIn(0f, 1f))
                                .fillMaxHeight()
                                .clip(CircleShape)
                                .background(MkBarraProgreso)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = datosProgreso?.nivel?.let { n ->
                                if (n.siguienteNombre != null) "Próximo nivel: ${n.siguienteNombre} • ${n.actividades}/${n.actividadesSiguiente} actividades"
                                else "¡Llegaste al nivel máximo!"
                            } ?: "",
                            fontFamily = JakartaSansFont,
                            fontSize = 11.sp,
                            color = MkSubtitulo
                        )
                        Text(
                            text = "${datosProgreso?.nivel?.porcentaje ?: 0}%",
                            fontFamily = JakartaSansFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = MkSubtitulo
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Estadísticas en Dirección de Columna (Fiel al Mockup 15) ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CardEstadisticaColumna(numero = datosProgreso?.estadisticas?.retosCompletados?.toString() ?: "–", etiqueta = "Retos completados", icon = Icons.Outlined.WorkspacePremium, modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(8.dp))
                CardEstadisticaColumna(numero = datosProgreso?.estadisticas?.calificacionPromedio?.let { String.format(java.util.Locale.US, "%.1f", it) } ?: "–", etiqueta = "${datosProgreso?.estadisticas?.ventasCompletadas ?: 0} ventas", icon = Icons.Outlined.StarBorder, modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(8.dp))
                CardEstadisticaColumna(numero = datosProgreso?.lilu?.flores?.toString() ?: "–", etiqueta = "Flores", icon = Icons.Outlined.LocalFlorist, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Portafolio Personal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Portafolio personal",
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = MkTitulo
                    )
                    Text(
                        text = "${publicacionesPortafolio.size} publicaciones registradas",
                        fontFamily = JakartaSansFont,
                        fontSize = 12.sp,
                        color = MkSubtitulo
                    )
                }
                TextButton(onClick = { onNavigate("portafolio_completo") }) {
                    Text(
                        text = "Ver más >>",
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MkTitulo
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Primeros 3 elementos del catálogo con el diseño exacto de la Card
            when (val e = portafolio.estado) {
                is Carga.Cargando -> IndicadorCargando()
                is Carga.Falla -> MensajeError(e.mensaje, portafolio.recargar)
                is Carga.Lista -> if (publicacionesPortafolio.isEmpty()) {
                    Text(
                        text = "Aún no has publicado nada. ¡Anímate a subir tu primer producto o servicio!",
                        fontFamily = JakartaSansFont, fontSize = 13.sp, color = MkSubtitulo
                    )
                }
            }

            publicacionesPortafolio.take(3).forEach { pub ->
                ItemPortafolioCardFiel(
                    pub = pub,
                    onPausarClick = {
                        if (pub.estado == "En revisión" || pub.estado == "Rechazado") {
                            mensajeInfo = if (pub.estado == "En revisión")
                                "Tu publicación está en revisión. Cuando la aprobemos aparecerá en el marketplace."
                            else "Esta publicación no fue aprobada, así que no se puede pausar ni reactivar."
                        } else {
                            publicacionSeleccionadaParaAccion = pub
                            tipoAccionPendiente = if (pub.estado == "Pausado") "reactivar" else "pausar"
                        }
                    },
                    onEditarClick = { /* Acción editar detalles */ }
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Habilidades Radar de IA
            Text(
                text = "Habilidades y talentos",
                fontFamily = OutfitFont,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = MkTitulo
            )
            Text(
                text = "Insignias desbloqueadas a partir de tus respuestas en tu registro.",
                fontFamily = JakartaSansFont,
                fontSize = 12.sp,
                color = MkSubtitulo
            )

            Spacer(modifier = Modifier.height(12.dp))

            when (val e = habilidadesCarga.estado) {
                is Carga.Cargando -> IndicadorCargando()
                is Carga.Falla -> MensajeError(e.mensaje, habilidadesCarga.recargar)
                is Carga.Lista -> {}
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                habilidadesInsignias.forEach { hab ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MkFondoCard),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MkBarraFondo),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "🌸", fontSize = 18.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = hab.titulo,
                                    fontFamily = OutfitFont,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MkTitulo
                                )
                                Text(
                                    text = hab.descripcion,
                                    fontFamily = JakartaSansFont,
                                    fontSize = 12.sp,
                                    color = MkSubtitulo,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }

    // Diálogo de confirmación para Pausar / Reactivar publicación
    if (publicacionSeleccionadaParaAccion != null && tipoAccionPendiente != null) {
        val pub = publicacionSeleccionadaParaAccion!!
        val esPausa = tipoAccionPendiente == "pausar"
        AlertDialog(
            onDismissRequest = { publicacionSeleccionadaParaAccion = null },
            title = {
                Text(
                    text = if (esPausa) "¿Pausar publicación?" else "¿Reactivar publicación?",
                    fontFamily = OutfitFont,
                    fontWeight = FontWeight.Bold,
                    color = MkTitulo
                )
            },
            text = {
                Text(
                    text = if (esPausa)
                        "¿Deseas pausar \"${pub.titulo}\"? Dejará de aparecer temporalmente en el marketplace."
                    else
                        "¿Deseas reactivar \"${pub.titulo}\"? Volverá a estar visible para las usuarias.",
                    fontFamily = JakartaSansFont,
                    color = MkSubtitulo
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val nuevoEstado = if (esPausa) "pausado" else "activo"
                        publicacionSeleccionadaParaAccion = null
                        tipoAccionPendiente = null
                        scope.launch {
                            when (val r = PublicacionesRepositorio.cambiarEstado(pub.id, nuevoEstado)) {
                                is ResultadoApi.Exito -> portafolio.recargar()
                                is ResultadoApi.Error -> mensajeInfo = r.mensaje
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MkTitulo)
                ) {
                    Text(text = "Confirmar", fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { publicacionSeleccionadaParaAccion = null }) {
                    Text(text = "Cancelar", fontFamily = JakartaSansFont, color = MkTitulo)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(12.dp)
        )
    }

    // Avisos (errores del servidor, publicaciones en revisión...)
    mensajeInfo?.let { texto ->
        AlertDialog(
            onDismissRequest = { mensajeInfo = null },
            text = { Text(text = texto, fontFamily = JakartaSansFont, color = MkSubtitulo) },
            confirmButton = {
                TextButton(onClick = { mensajeInfo = null }) {
                    Text(text = "Entendido", fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, color = MkTitulo)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(12.dp)
        )
    }

    // Selector de Avatar
    if (mostrarSelectorAvatar) {
        AlertDialog(
            onDismissRequest = { mostrarSelectorAvatar = false },
            title = {
                Text(text = "Elige tu avatar predeterminado", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, color = MkTitulo)
            },
            text = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    val avatares = listOf("🐱", "🦋", "🌸", "🌿", "⭐", "🌙")
                    avatares.forEach { emoji ->
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(if (avatarSeleccionado == emoji) MkBarraFondo else Color.LightGray.copy(alpha = 0.3f))
                                .clickable {
                                    val anterior = avatarSeleccionado
                                    avatarSeleccionado = emoji
                                    mostrarSelectorAvatar = false
                                    scope.launch {
                                        val r = ProgresoRepositorio.cambiarAvatar(emoji)
                                        if (r is ResultadoApi.Error) {
                                            avatarSeleccionado = anterior
                                            mensajeInfo = r.mensaje
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 24.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { mostrarSelectorAvatar = false }) {
                    Text(text = "Cerrar", fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, color = MkTitulo)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(12.dp)
        )
    }
}

/** Tarjeta de estadística en dirección de columna (Fiel al mockup 15) */
@Composable
private fun CardEstadisticaColumna(
    numero: String,
    etiqueta: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MkFondoCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.height(105.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = MkTitulo, modifier = Modifier.size(26.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = numero,
                fontFamily = OutfitFont,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = MkTitulo
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = etiqueta,
                fontFamily = JakartaSansFont,
                fontSize = 11.sp,
                color = MkSubtitulo,
                maxLines = 1
            )
        }
    }
}

/** Tarjeta de publicación 100% fiel al mockup[cite: 14] con foto, botones y estado interactivo */
@Composable
fun ItemPortafolioCardFiel(
    pub: PublicacionPortafolio,
    onPausarClick: () -> Unit,
    onEditarClick: () -> Unit
) {
    val estaPausado = pub.estado == "Pausado"

    // Colores dinámicos del badge de estado
    val (fondoBadge, textoBadge) = when (pub.estado) {
        "Activo" -> Pair(MkVerdeActivoFondo, MkVerdeActivoTexto)
        "Por encargo" -> Pair(MkAmarilloEncargoFondo, MkAmarilloEncargoTexto)
        else -> Pair(MkGrisPausadoFondo, MkGrisPausadoTexto)
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MkFondoItem),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Foto miniatura cuadrada (Fiel al mockup 14)
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    if (pub.imagenRuta != null) {
                        FotoRemota(
                            ruta = pub.imagenRuta,
                            modifier = Modifier.fillMaxSize(),
                            alternativa = { Text(text = "📷", fontSize = 24.sp) }
                        )
                    } else if (pub.imagenRes != null) {
                        Image(
                            painter = painterResource(id = pub.imagenRes),
                            contentDescription = pub.titulo,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(text = "📷", fontSize = 24.sp)
                    }
                }

                // Información principal
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = fondoBadge
                        ) {
                            Text(
                                text = "• ${pub.estado}",
                                fontFamily = JakartaSansFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = textoBadge,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = pub.detalleVentas,
                            fontFamily = JakartaSansFont,
                            fontSize = 11.sp,
                            color = MkSubtitulo
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = pub.titulo,
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.Black
                    )
                    Text(
                        text = pub.precio,
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MkTitulo
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Badge de Ubicación / Detalles
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.White)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = MkSubtitulo,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = pub.ubicacionDetalle,
                    fontFamily = JakartaSansFont,
                    fontSize = 11.sp,
                    color = MkSubtitulo,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botones inferiores (Fieles al mockup 14: Pedidos / Editar detalles / Pausa / Reactivar)
            if (estaPausado) {
                // Si está pausado muestra un botón ancho de Reactivar y Editar Detalles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onPausarClick,
                        modifier = Modifier
                            .weight(1.2f)
                            .height(38.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC5B8C2))
                    ) {
                        Icon(imageVector = Icons.Outlined.PlayArrow, contentDescription = null, tint = MkTitulo, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Reactivar", fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MkTitulo)
                    }

                    OutlinedButton(
                        onClick = onEditarClick,
                        modifier = Modifier
                            .weight(1.3f)
                            .height(38.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
                    ) {
                        Text(text = "Editar detalles", fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MkTitulo)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(imageVector = Icons.Outlined.Edit, contentDescription = null, tint = MkTitulo, modifier = Modifier.size(14.dp))
                    }
                }
            } else {
                // Si está activo o por encargo muestra: Pedidos(n), Editar detalles y el botón de Pausa
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Pedidos (no es botón interactivo de acción, indicador visual)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFDED2D9),
                        modifier = Modifier
                            .height(38.dp)
                            .weight(0.9f)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = "Pedidos (${pub.pedidosCount})",
                                fontFamily = JakartaSansFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MkTitulo
                            )
                        }
                    }

                    // Botón Editar Detalles
                    OutlinedButton(
                        onClick = onEditarClick,
                        modifier = Modifier
                            .height(38.dp)
                            .weight(1.3f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
                    ) {
                        Text(text = "Editar detalles", fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MkTitulo)
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(imageVector = Icons.Outlined.Edit, contentDescription = null, tint = MkTitulo, modifier = Modifier.size(13.dp))
                    }

                    // Botón de Pausar (Icono de pausa)
                    Surface(
                        onClick = onPausarClick,
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFDED2D9),
                        modifier = Modifier
                            .size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(
                                imageVector = Icons.Outlined.Pause,
                                contentDescription = "Pausar",
                                tint = MkTitulo,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}