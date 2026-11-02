package com.example.crisalida.ui.screens

import androidx.compose.foundation.background
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

/* ──────────────────────────────────────────────────────────────
 * Colores medidos del mockup Perfil/Portafolio (portafolio.png)
 * ────────────────────────────────────────────────────────────── */
private val MkTitulo = Color(0xFF7F284F)
private val MkSubtitulo = Color(0xFF545454)
private val MkVerdeBoton = Color(0xFF005524)
private val MkFondoBotonVolver = Color(0xFFE2D4DA)

@Composable
fun PortafolioScreen(
    onNavigate: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    var publicacionSeleccionadaParaAccion by remember { mutableStateOf<PublicacionPortafolio?>(null) }
    var tipoAccionPendiente by remember { mutableStateOf<String?>(null) } // "pausar" o "reactivar"

    // Lista completa de publicaciones del portafolio (mostrando todas las registradas)
    val publicacionesPortafolio = remember {
        mutableStateListOf(
            PublicacionPortafolio("1", "Nieve de limón", "$45.00 c/litro", "Activo", "8 vendidos", "Entrega: parque Ireka", 3),
            PublicacionPortafolio("2", "Pastel de elote", "$120.00 c/u", "Por encargo", "8 vendidos", "Elaboración: 24 hrs anticipación • Recoger en calle Juárez...", 0),
            PublicacionPortafolio("3", "Paseo de perros", "$50.00 c/perro x hora", "Pausado", "3 completados", "Recorrido: parque Ireka-parque lomas, 1 hora", 0),
            PublicacionPortafolio("4", "Paseo de perros", "$50.00 c/perro x hora", "Pausado", "3 completados", "Recorrido: parque Ireka-parque lomas, 1 hora", 0)
        )
    }

    CrisalidaScaffold(
        currentTitle = "Perfil/portafolio",
        selectedTab = 5, // Pestaña de Perfil
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

            // Botón superior izquierdo "Perfil" (Volver)
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

            // Cabecera: Avatar, Nombre y Botón "+ Añadir nuevo producto"
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Avatar con nivel 3
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF8A6D9E)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🐱", fontSize = 40.sp)
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
                                text = "3",
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
                        text = SesionActual.seudonimo,
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = MkTitulo
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        onClick = { onNavigate("publicar_producto") },
                        modifier = Modifier.height(38.dp),
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

            // Título de la sección Portafolio Personal
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

            // Listado completo de publicaciones reutilizando la misma Card del perfil
            publicacionesPortafolio.forEach { pub ->
                ItemPortafolioCardFiel(
                    pub = pub,
                    onPausarClick = {
                        publicacionSeleccionadaParaAccion = pub
                        tipoAccionPendiente = if (pub.estado == "Pausado") "reactivar" else "pausar"
                    },
                    onEditarClick = { /* Acción editar detalles */ }
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Botón inferior "+ Añadir nuevo producto" (Fiel al mockup)
            Button(
                onClick = { onNavigate("publicar_producto") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
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
                        if (esPausa) {
                            pub.estado = "Pausado"
                        } else {
                            pub.estado = "Activo"
                        }
                        publicacionSeleccionadaParaAccion = null
                        tipoAccionPendiente = null
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
}