package com.example.crisalida.ui.theme.screens

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.material.icons.outlined.Settings
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
import com.example.crisalida.ui.screens.CrisalidaScaffold
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
private val MkFondoItem = Color(0xFFEFE8EC)
//lista con todos los iconos para ponerse de foto de perfil
private val Avatares = listOf("🐱", "🦋", "🌸", "🌿", "⭐", "🌙", "🪻", "🫐", "🌀","🍓","☕","🍊","🍋","🍒","🌽","🍄","🍄‍🟫","🌷","🌺","🌱","🍁","🍂","🫧","🌝","🌈","❄️","💧","💙","🖤","🩶","🤍","💚","💛","🧡","🤎","💜","🩷","❤️","🩵","💫","🔮","🪩","🪄","🪼","🦑","🪸","🦚","🐦‍🔥","🐣","🐧","🐌","🐝","🐲","🐸","🐶","🐺","🦝","🐮","🐷","🙉","👾","👽","👻","🐼","🐬","🦖","🐠","🐡","🐋","🦆","🐤","🐛","🐞")

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
    onNavigate: (String) -> Unit,
    onEditar: (String) -> Unit = {},
    onCerrarSesion: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    val scope = rememberCoroutineScope()
    var mostrarSelectorAvatar by remember { mutableStateOf(false) }
    var mostrarConfiguracion by remember { mutableStateOf(false) }
    val contexto = LocalContext.current
    var avatarSeleccionado by remember { mutableStateOf(SesionActual.avatar) }
    var mensajeInfo by remember { mutableStateOf<String?>(null) }

    // Todo viene del servidor
    val progreso = rememberProgreso()
    val datosProgreso = (progreso.estado as? Carga.Lista)?.datos
    val portafolio = rememberCarga(ProgresoRepositorio.version) { PublicacionesRepositorio.mias() }
    val habilidadesCarga = rememberCarga { ProgresoRepositorio.habilidades() }
    val nivelesCarga = rememberCarga { ProgresoRepositorio.niveles() }
    val reglasCarga = rememberCarga { ProgresoRepositorio.reglas() }
    var verTodosLosNiveles by remember { mutableStateOf(false) }

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

                Column(modifier = Modifier.weight(1f)) {
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

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MkBarraFondo)
                        .clickable { mostrarConfiguracion = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "Configuración",
                        tint = MkTitulo,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (progreso.estado is Carga.Falla) {
                MensajeError((progreso.estado as Carga.Falla).mensaje, progreso.recargar)
            }

            //tarjeta del nivel
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
                            fontSize = 12.sp,
                            color = MkSubtitulo
                        )
                        Text(
                            text = "${datosProgreso?.nivel?.porcentaje ?: 0}%",
                            fontFamily = JakartaSansFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MkSubtitulo
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // apartado ¿Cómo subo de nivel?
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MkFondoCard),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "¿Cómo subo de nivel?",
                        fontFamily = OutfitFont, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MkTitulo
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Tu nivel sube con tus actividades completadas. Estas son las que cuentan:",
                        fontFamily = JakartaSansFont, fontSize = 13.sp, lineHeight = 18.sp, color = MkSubtitulo
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val reglas = (reglasCarga.estado as? Carga.Lista)?.datos?.reglas ?: emptyMap()
                    Text("-  Cada venta completada (cuando la compradora confirma que recibió su pedido)", fontFamily = JakartaSansFont, fontSize = 13.sp, lineHeight = 18.sp, color = ColorTextoPrincipal)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("-  Cada microrreto que el contratista valida como bien hecho", fontFamily = JakartaSansFont, fontSize = 13.sp, lineHeight = 18.sp, color = ColorTextoPrincipal)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Lo demás (publicar, registrar tus emociones, cuidar a Lilu) no sube de nivel, pero sí te da flores 🌸 para Lilu." +
                            (reglas["nivel"]?.let { " Y cada vez que subes de nivel, ¡ganas $it flores de regalo!" } ?: ""),
                        fontFamily = JakartaSansFont, fontSize = 13.sp, lineHeight = 18.sp, color = MkSubtitulo
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = { verTodosLosNiveles = !verTodosLosNiveles }, contentPadding = PaddingValues(0.dp)) {
                        Text(
                            text = if (verTodosLosNiveles) "Ocultar los niveles ▲" else "Ver los 10 niveles ▼",
                            fontFamily = OutfitFont, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MkTitulo
                        )
                    }
                    if (verTodosLosNiveles) {
                        val niveles = (nivelesCarga.estado as? Carga.Lista)?.datos?.niveles ?: emptyList()
                        if (niveles.isEmpty()) Text("Cargando…", fontFamily = JakartaSansFont, fontSize = 13.sp, color = MkSubtitulo)
                        niveles.forEach { n ->
                            val esActual = n.nivel == datosProgreso?.nivel?.nivel
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (esActual) MkBarraFondo else Color.Transparent)
                                    .padding(horizontal = 8.dp, vertical = 5.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Nivel ${n.nivel} · ${n.nombre}" + if (esActual) "  (tú)" else "",
                                    fontFamily = JakartaSansFont, fontSize = 13.sp,
                                    fontWeight = if (esActual) FontWeight.Bold else FontWeight.Normal, color = ColorTextoPrincipal
                                )
                                Text(
                                    text = if (n.actividadesRequeridas == 0) "al empezar" else "${n.actividadesRequeridas} actividades",
                                    fontFamily = JakartaSansFont, fontSize = 13.sp, color = MkSubtitulo
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            //apartado de estadísticas
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

            //portafolio personal
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
                        fontSize = 13.sp,
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

            //solo muestras 3 primeros publis del catálogo/portafolio
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
                    onEditarClick = { onEditar(pub.id) }
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            Button(
                onClick = { onNavigate("publicar_producto") },
                modifier = Modifier.fillMaxWidth().heightIn(min = 46.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF005524))
            ) {
                Text(
                    text = "+  Añadir nuevo producto",
                    fontFamily = OutfitFont, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            //habilidades
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
                fontSize = 13.sp,
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
                                Text(text = "🎖️", fontSize = 18.sp)
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
                                    fontSize = 13.sp,
                                    color = MkSubtitulo,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            OutlinedButton(
                onClick = onCerrarSesion,
                modifier = Modifier.fillMaxWidth().heightIn(min = 46.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Cerrar sesión", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MkTitulo)
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }

    //modal de confirmación para pausar / reactivar publicación
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

    //avisos
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

    //config
    if (mostrarConfiguracion) {
        AlertDialog(
            onDismissRequest = { mostrarConfiguracion = false },
            title = {
                Text(text = "Configuración", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, color = MkTitulo)
            },
            text = {
                Column {
                    Text(text = "Tamaño del texto", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color.Black)
                    Text(
                        text = "Elige el que se te haga más cómodo de leer.",
                        fontFamily = JakartaSansFont, fontSize = 14.sp, color = MkSubtitulo
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    AjustesTexto.opciones.forEach { (nombre, valor) ->
                        val elegida = AjustesTexto.escala == valor
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { AjustesTexto.cambiar(contexto, valor) }
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = elegida,
                                onClick = { AjustesTexto.cambiar(contexto, valor) },
                                colors = RadioButtonDefaults.colors(selectedColor = MkTitulo)
                            )
                            Text(
                                text = nombre,
                                fontFamily = JakartaSansFont,
                                fontWeight = if (elegida) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 16.sp,
                                color = Color.Black
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MkFondoItem)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Así se va a ver el texto de la app. Lilu te acompaña en cada paso.",
                            fontFamily = JakartaSansFont, fontSize = 15.sp, color = Color.Black
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { mostrarConfiguracion = false }) {
                    Text(text = "Listo", fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, color = MkTitulo)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(12.dp)
        )
    }

    //selector de Avatar
    if (mostrarSelectorAvatar) {
        AlertDialog(
            onDismissRequest = { mostrarSelectorAvatar = false },
            title = {
                Text(text = "Elige tu avatar predeterminado", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, color = MkTitulo)
            },
            text = {
                //solo los emojis que el teléfono sabe dibujar
                val disponibles = remember { val pincel = android.graphics.Paint(); Avatares.filter { pincel.hasGlyph(it) } }
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 52.dp),
                    modifier = Modifier.fillMaxWidth().heightIn(max = 320.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(disponibles) { emoji ->
                        Box(
                            modifier = Modifier
                                .size(48.dp)
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
                            Text(text = emoji, fontSize = 26.sp)
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

//tarjeta de estadística
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
        modifier = modifier.heightIn(min = 105.dp)
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
                fontSize = 12.sp,
                color = MkSubtitulo,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

//tarjeta de publicación
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
                // Foto miniatura cuadrada
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
                                fontSize = 12.sp,
                                color = textoBadge,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = pub.detalleVentas,
                            fontFamily = JakartaSansFont,
                            fontSize = 12.sp,
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
                    fontSize = 12.sp,
                    color = MkSubtitulo,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            //botones inferiores
            if (estaPausado) {
                //si está pausado muestra un botón ancho de Reactivar y Editar Detalles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onPausarClick,
                        modifier = Modifier
                            .weight(1.2f)
                            .heightIn(min = 38.dp),
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
                            .heightIn(min = 38.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
                    ) {
                        Text(text = "Editar detalles", fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MkTitulo)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(imageVector = Icons.Outlined.Edit, contentDescription = null, tint = MkTitulo, modifier = Modifier.size(14.dp))
                    }
                }
            } else {
                //si está activo o por encargo muestra: Pedidos(n), Editar detalles y el botón de Pausa
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    //pedidos en curso
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFDED2D9),
                        modifier = Modifier
                            .heightIn(min = 38.dp)
                            .weight(0.9f)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text(
                                text = "Pedidos (${pub.pedidosCount})",
                                fontFamily = JakartaSansFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MkTitulo
                            )
                        }
                    }

                    //btn editar detalles
                    OutlinedButton(
                        onClick = onEditarClick,
                        modifier = Modifier
                            .heightIn(min = 38.dp)
                            .weight(1.3f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White)
                    ) {
                        Text(text = "Editar detalles", fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MkTitulo)
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(imageVector = Icons.Outlined.Edit, contentDescription = null, tint = MkTitulo, modifier = Modifier.size(13.dp))
                    }

                    //btn de pause
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