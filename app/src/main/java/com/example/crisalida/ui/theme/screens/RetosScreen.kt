package com.example.crisalida.ui.theme.screens
import androidx.compose.foundation.Image
import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crisalida.R
import com.example.crisalida.ui.theme.*
import androidx.compose.foundation.clickable

@Composable
fun RetosScreen(
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            // Barra superior reutilizada con el título exacto de la pantalla
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = ColorFondoApp,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.crisalida_logo),
                            contentDescription = "Logo",
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Crisálida",
                                fontFamily = OutfitFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = ColorTextoPrincipal
                            )
                            Text(
                                text = "Tablero de Retos",
                                fontFamily = JakartaSansFont,
                                fontSize = 12.sp,
                                color = ColorPlaceholder
                            )
                        }
                    }

                    // Botón de Salida de Emergencia (RF-10)
                    Button(
                        onClick = { (context as? Activity)?.finishAffinity() },
                        colors = ButtonDefaults.buttonColors(containerColor = ColorBotonEntrar),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(
                            text = "Salida de emergencia",
                            fontFamily = JakartaSansFont,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        },
        bottomBar = {
            // Barra inferior reutilizada (aquí marcamos "Retos" como seleccionado)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = ColorBotonEntrar,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BottomNavVectorIcon(label = "Retos", icon = Icons.Default.List, isSelected = true, onClick = { onNavigate("retos") })
                    BottomNavVectorIcon(label = "Marketplace", icon = Icons.Default.ShoppingCart, onClick = { onNavigate("marketplace") })
                    BottomNavVectorIcon(label = "Inicio", icon = Icons.Default.Home, onClick = { onNavigate("home") })
                    BottomNavVectorIcon(label = "Bienestar", icon = Icons.Default.DateRange, onClick = { onNavigate("bienestar") })
                    BottomNavVectorIcon(label = "Mascota", icon = Icons.Default.Favorite, onClick = { onNavigate("mascota") })
                    BottomNavVectorIcon(label = "Perfil", icon = Icons.Default.Person, onClick = { onNavigate("perfil") })
                }
            }
        }
    ) { innerPadding ->
        // Contenido de la pantalla de Retos (Lista de micro-retos publicados por contratistas - RF-06)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ColorFondoApp)
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Microrretos Disponibles",
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = ColorTextoPrincipal
                    )
                    Text(
                        text = "Completa tareas sencillas asignadas por usuarios externos y genera ingresos seguros.",
                        fontFamily = JakartaSansFont,
                        fontSize = 12.sp,
                        color = ColorPlaceholder
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Elementos de ejemplo del tablero de retos
                item { RetoCard(titulo = "Transcribir notas de catálogo", tiempo = "25 min", recompensa = "+$140 MXN", dificultad = "Fácil") }
                item { RetoCard(titulo = "Clasificar etiquetas de inventario", tiempo = "40 min", recompensa = "+$220 MXN", dificultad = "Media") }
                item { RetoCard(titulo = "Verificación de textos descriptivos", tiempo = "15 min", recompensa = "+$90 MXN", dificultad = "Fácil") }
            }
        }
    }
}

@Composable
fun RetoCard(titulo: String, tiempo: String, recompensa: String, dificultad: String) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "⏱ $dificultad • $tiempo",
                fontFamily = JakartaSansFont,
                fontSize = 10.sp,
                color = ColorBotonEntrar,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = titulo,
                fontFamily = OutfitFont,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = ColorTextoPrincipal
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = recompensa,
                fontFamily = JakartaSansFont,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color(0xFF2E7D32)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = { /* Aceptar reto */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Aceptar Micro-Reto",
                    fontFamily = JakartaSansFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun BottomNavVectorIcon(label: String, icon: ImageVector, isSelected: Boolean = false, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = label,
            fontFamily = JakartaSansFont,
            fontSize = 10.sp,
            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}