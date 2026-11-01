package com.example.crisalida.ui.screens

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crisalida.R
import com.example.crisalida.ui.theme.*

// Colores medidos del mockup (home.png)
private val MkCrisalida = Color(0xFF9D4067)
private val MkGrisInicio = Color(0xFF878686)
private val MkMorado = Color(0xFFA47DAB)
private val MkTextoEmergencia = Color(0xFFEFD6F3)
private val MkRosaBarra = Color(0xFFD0A0B5)
private val MkRosaIconos = Color(0xFFFFDEF7)
private val MkSeleccionada = Color(0xFF62012A) // mismo vino del chip "Todos" del mockup

@Composable
fun CrisalidaScaffold(




    currentTitle: String,
    selectedTab: Int, // 0: Retos, 1: Marketplace, 2: Inicio, 3: Bienestar, 4: Mascota, 5: Perfil
    onNavigate: (String) -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        containerColor = ColorFondoApp,
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = ColorFondoApp,
                shadowElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(64.dp)
                        .padding(start = 18.dp, end = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.crisalida_logo),
                            contentDescription = "Logo",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.height(48.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
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

                    // Botón de Salida de Emergencia (RF-10)
                    Row(
                        modifier = Modifier
                            .size(width = 200.dp, height = 33.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(MkMorado)
                            .clickable { (context as? Activity)?.let { activity ->
                                activity.finishAffinity()
                                activity.finishAndRemoveTask()
                            }}
                            .padding(start = 4.dp, end = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Salida de emergencia",
                            fontFamily = OutfitFont,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            lineHeight = 14.sp,
                            color = MkTextoEmergencia,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Outlined.Logout,
                            contentDescription = "Salida de emergencia",
                            tint = MkTextoEmergencia,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MkRosaBarra,
                shadowElevation = 12.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .height(84.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // La pestaña activa se resalta en vino oscuro; las demás quedan en rosa claro.
                    BottomVectorTabItem("Retos", Icons.Outlined.TrackChanges, selectedTab == 0, Modifier.weight(1f)) { onNavigate("retos") }
                    BottomVectorTabItem("Marketplace", Icons.Outlined.Storefront, selectedTab == 1, Modifier.weight(1f)) { onNavigate("marketplace") }
                    BottomVectorTabItem("Inicio", Icons.Outlined.Home, selectedTab == 2, Modifier.weight(1f)) { onNavigate("home") }
                    BottomVectorTabItem("Bienestar diario", Icons.Outlined.EventAvailable, selectedTab == 3, Modifier.weight(1f)) { onNavigate("bienestar") }
                    // Material no tiene mariposa; Spa (flor de loto) es lo más cercano de la librería.
                    BottomVectorTabItem("Mascota", Icons.Outlined.Spa, selectedTab == 4, Modifier.weight(1f)) { onNavigate("mascota") }
                    BottomVectorTabItem("Perfil", Icons.Filled.Person, selectedTab == 5, Modifier.weight(1f)) { onNavigate("perfil") }
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
            modifier = Modifier.size(33.dp)
        )
        Text(
            text = label,
            fontFamily = OutfitFont,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            lineHeight = 13.sp,
            textAlign = TextAlign.Center,
            color = if (isSelected) MkSeleccionada else MkRosaIconos
        )
    }
}