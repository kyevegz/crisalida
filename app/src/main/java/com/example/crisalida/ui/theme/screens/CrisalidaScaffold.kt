package com.example.crisalida.ui.theme.screens

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crisalida.R
import com.example.crisalida.ui.theme.*
import com.example.crisalida.data.AlmacenamientoCifrado
import com.example.crisalida.data.BorradoLocal
import com.example.crisalida.data.SesionActual
import com.example.crisalida.ui.theme.screens.PreviewResponsivo
import com.example.crisalida.ui.theme.Paleta

//Colores
private val MkCrisalida: Color get() = Paleta.tituloRosa
private val MkGrisInicio: Color get() = Paleta.grisInicio
private val MkMorado: Color get() = Paleta.moradoAcento
private val MkTextoEmergencia: Color get() = Paleta.textoEmergencia
private val MkRosaBarra: Color get() = Paleta.rosaBarra
private val MkRosaIconos: Color get() = Paleta.iconosBarra
private val MkSeleccionada: Color get() = Paleta.vino

@Composable
fun CrisalidaScaffold(




    currentTitle: String,
    selectedTab: Int, // 0: Retos, 1: Marketplace, 2: Inicio, 3: Bienestar, 4: Mascota, 5: Perfil, son las opciones de la barrita inferior
    onNavigate: (String) -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    val context = LocalContext.current
    // la compradora no tiene bienestar emocional ni mascota: su barra tiene solo 4 pestañas
    val esCompradora = SesionActual.esCompradora
    // en horizontal casi no hay alto: las barras se hacen más bajitas para dejar espacio al contenido
    val compacto = LocalConfiguration.current.screenHeightDp < 480
    val altoBarraSuperior = if (compacto) 56.dp else 64.dp
    val altoBarraInferior = if (compacto) 62.dp else 84.dp

    Scaffold(
        containerColor = ColorFondoApp,
        topBar = {
            // las barras casi no crecen con el texto grande (tienen poco espacio): se limita su escala
            ConEscalaLimitada(tope = 1.15f) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = ColorFondoApp,
                    shadowElevation = 6.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .heightIn(min = altoBarraSuperior)
                            .padding(start = 18.dp, end = 12.dp, top = 4.dp, bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // logo + título: cede espacio si la pantalla es angosta
                        Row(
                            modifier = Modifier.weight(1f, fill = false),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.crisalida_logo),
                                contentDescription = "Logo",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.height(44.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f, fill = false)) {
                                Text(
                                    text = "Crisálida",
                                    fontFamily = OutfitFont,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 22.sp,
                                    color = MkCrisalida
                                )
                                Text(
                                    text = currentTitle,
                                    fontFamily = OutfitFont,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 13.sp,
                                    color = MkGrisInicio
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Botón de Salida de Emergencia (RF-10): mide hasta 200 dp; si no cabe, el texto baja a dos líneas
                        Row(
                            modifier = Modifier
                                .weight(1.2f, fill = false)
                                .widthIn(max = 200.dp)
                                .heightIn(min = 33.dp)
                                .clip(RoundedCornerShape(7.dp))
                                .background(MkMorado)
                                .clickable { (context as? Activity)?.let { activity ->
                                    BorradoLocal.todo(context)   // borradores, cola y todo lo local, sin dejar nada en el teléfono
                                    SesionActual.cerrar()
                                    activity.finishAndRemoveTask()
                                }}
                                .padding(start = 6.dp, end = 4.dp, top = 3.dp, bottom = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Salida de emergencia",
                                fontFamily = OutfitFont,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                lineHeight = 18.sp,
                                color = MkTextoEmergencia,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Outlined.Logout,
                                contentDescription = "Salida de emergencia",
                                tint = MkTextoEmergencia,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            // los textos de las pestañas no crecen con el texto grande (el alto es fijo)
            ConEscalaLimitada(tope = 1f) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MkRosaBarra,
                    shadowElevation = 12.dp
                ) {
                    BoxWithConstraints(modifier = Modifier.fillMaxWidth().navigationBarsPadding()) {
                        // 6 pestañas (4 si es compradora) comparten el ancho: la letra se ajusta para que "Marketplace" no se parta (8 a 11 sp)
                        val pestanas = if (esCompradora) 4 else 6
                        val tamanoLetra = ((maxWidth / pestanas).value / 6.2f).coerceIn(8f, 11f).sp
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(altoBarraInferior),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // La pestaña activa se resalta en un color vino oscuro, las demás quedan en rosa claro.
                            BottomVectorTabItem("Retos", Icons.Outlined.TrackChanges, selectedTab == 0, Modifier.weight(1f), tamanoLetra, compacto) { onNavigate("retos") }
                            BottomVectorTabItem("Marketplace", Icons.Outlined.Storefront, selectedTab == 1, Modifier.weight(1f), tamanoLetra, compacto) { onNavigate("marketplace") }
                            BottomVectorTabItem("Inicio", Icons.Outlined.Home, selectedTab == 2, Modifier.weight(1f), tamanoLetra, compacto) { onNavigate("home") }
                            if (!esCompradora) {
                                BottomVectorTabItem("Bienestar diario", Icons.Outlined.EventAvailable, selectedTab == 3, Modifier.weight(1f), tamanoLetra, compacto) { onNavigate("bienestar") }
                                BottomVectorTabItem("Mascota", Icons.Outlined.Spa, selectedTab == 4, Modifier.weight(1f), tamanoLetra, compacto) { onNavigate("mascota") }
                            }
                            BottomVectorTabItem("Perfil", Icons.Filled.Person, selectedTab == 5, Modifier.weight(1f), tamanoLetra, compacto) { onNavigate("perfil") }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        content(innerPadding)
    }
}

@Composable
fun BottomVectorTabItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    tamanoLetra: TextUnit = 11.sp,
    compacto: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clickable { onClick() }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) MkSeleccionada else MkRosaIconos,
            modifier = Modifier.size(if (compacto) 26.dp else 33.dp)
        )
        Text(
            text = label,
            fontFamily = OutfitFont,
            fontWeight = FontWeight.Bold,
            fontSize = tamanoLetra,
            lineHeight = tamanoLetra * 1.2f,
            textAlign = TextAlign.Center,
            color = if (isSelected) MkSeleccionada else MkRosaIconos
        )
    }
}

@PreviewResponsivo
@Composable
private fun BarrasPreview() {
    CrisalidaScaffold(currentTitle = "Bienestar diario", selectedTab = 1, onNavigate = {}) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding))
    }
}