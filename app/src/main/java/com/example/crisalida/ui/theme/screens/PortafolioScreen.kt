package com.example.crisalida.ui.theme.screens

import androidx.compose.foundation.background
import com.example.crisalida.data.ProgresoRepositorio
import com.example.crisalida.data.PublicacionesRepositorio
import com.example.crisalida.data.ResultadoApi
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crisalida.ui.theme.*
import com.example.crisalida.data.SesionActual
import com.example.crisalida.ui.screens.CrisalidaScaffold
private val MkTitulo = Color(0xFF7F284F)
private val MkSubtitulo = Color(0xFF545454)
private val MkVerdeBoton = Color(0xFF005524)
private val MkFondoBotonVolver = Color(0xFFE2D4DA)

@Composable
fun PortafolioScreen(
    onNavigate: (String) -> Unit,
    onEditar: (String) -> Unit = {}
) {
    val scrollState = rememberScrollState()

    var publicacionSeleccionadaParaAccion by remember { mutableStateOf<PublicacionPortafolio?>(null) }
    var tipoAccionPendiente by remember { mutableStateOf<String?>(null) } // "pausar" o "reactivar"

    val scope = rememberCoroutineScope()
    var mensajeInfo by remember { mutableStateOf<String?>(null) }

    // Nivel y avatar reales (se actualizan solos cuando cambian)
    val progreso = (rememberProgreso().estado as? Carga.Lista)?.datos
    val nivelActual = progreso?.nivel?.nivel
    val avatarActual = progreso?.avatar ?: SesionActual.avatar

    // Lista completa de publicaciones de la usuaria, desde el servidor
    val portafolio = rememberCarga(ProgresoRepositorio.version) { PublicacionesRepositorio.mias() }
    val publicacionesPortafolio: List<PublicacionPortafolio> =
        (portafolio.estado as? Carga.Lista)?.datos?.publicaciones?.map { it.aPortafolioUi() } ?: emptyList()

    CrisalidaScaffold(
        currentTitle = "Perfil/portafolio",
        selectedTab = 5, //Pestaña de perfil
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
            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                onClick = { onNavigate("perfil") },
                shape = RoundedCornerShape(18.dp),
                color = MkFondoBotonVolver,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = MkTitulo,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Perfil",
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = MkTitulo
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Avatar con su nivel actual
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF8A6D9E)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = avatarActual, fontSize = 40.sp)
                    }
                    Box(
                        modifier = Modifier
                            .size(28.dp)
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
                                text = nivelActual?.toString() ?: "–",
                                fontFamily = OutfitFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = progreso?.seudonimo ?: SesionActual.seudonimo,
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = MkTitulo
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        onClick = { onNavigate("publicar_producto") },
                        modifier = Modifier.heightIn(min = 38.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MkVerdeBoton)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Añadir nuevo producto",
                            fontFamily = OutfitFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Portafolio personal",
                fontFamily = OutfitFont,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = MkTitulo
            )
            Text(
                text = "${publicacionesPortafolio.size} publicaciones registradas",
                fontFamily = JakartaSansFont,
                fontSize = 13.sp,
                color = MkSubtitulo
            )

            Spacer(modifier = Modifier.height(14.dp))

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

            publicacionesPortafolio.forEach { pub ->
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

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = { onNavigate("publicar_producto") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MkVerdeBoton)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Añadir nuevo producto",
                    fontFamily = OutfitFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }

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
}
