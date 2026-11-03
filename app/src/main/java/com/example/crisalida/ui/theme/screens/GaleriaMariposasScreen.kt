package com.example.crisalida.ui.theme.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

/* ================================================================
 * Galería de mariposas: la app «alternativa» que se abre con el PIN de camuflaje.
 * Es 100 % local: no pide nada al servidor ni muestra ningún dato de la usuaria.
 * Las mariposas se dibujan con código (no hay imágenes), así la app no necesita archivos extra.
 * Salir: menú ⋮ → «Cerrar galería» (cierra la sesión y vuelve al inicio).
 * ================================================================ */

private class Mariposa(
    val nombre: String,
    val cientifico: String,
    val dato: String,
    val alaSuperior: Color,
    val alaInferior: Color,
    val manchas: Color,
    val fondoArriba: Color,
    val fondoAbajo: Color
)

private val Mariposas = listOf(
    Mariposa("Monarca", "Danaus plexippus",
        "Viaja hasta 4,000 km cada año desde Canadá hasta los bosques de oyamel de Michoacán y el Estado de México.",
        Color(0xFFF08A24), Color(0xFFF5A742), Color(0xFF2B1B12), Color(0xFFFFE9C7), Color(0xFFFFD08A)),
    Mariposa("Azul morfo", "Morpho menelaus",
        "Sus alas no tienen pigmento azul: el color nace de la forma microscópica de sus escamas.",
        Color(0xFF2F7DE1), Color(0xFF5AA0F2), Color(0xFF0E2A55), Color(0xFFD7EBFF), Color(0xFFA9D0FF)),
    Mariposa("Cola de golondrina", "Papilio polyxenes",
        "Sus alas traseras terminan en pequeñas «colas», como las de una golondrina.",
        Color(0xFF2B2B33), Color(0xFF3B3B47), Color(0xFFF5D547), Color(0xFFE6F2D6), Color(0xFFBFDDA0)),
    Mariposa("Mariposa blanca", "Pieris rapae",
        "Es una de las mariposas más comunes en huertos y jardines de todo el mundo.",
        Color(0xFFFFFFFF), Color(0xFFF1F1F1), Color(0xFF4A4A4A), Color(0xFFD9EEF2), Color(0xFFB4DCE6)),
    Mariposa("Cuatro espejos", "Siproeta stelenes",
        "Sus alas verdes con bordes oscuros la hacen parecer una hoja iluminada por el sol.",
        Color(0xFF5BBF5B), Color(0xFF7ED27E), Color(0xFF1E3D1E), Color(0xFFEAF7DF), Color(0xFFC6E8AE)),
    Mariposa("Pavo real", "Junonia coenia",
        "Cada ala tiene «ojos» que confunden a los pájaros que intentan atraparla.",
        Color(0xFF9C5B2E), Color(0xFFB7753E), Color(0xFF3E6FB5), Color(0xFFFFE3E0), Color(0xFFFFC2BB)),
    Mariposa("Mariposa rosa", "Eurema daira",
        "Le gusta posarse en flores pequeñas y pasa el día entre jardines soleados.",
        Color(0xFFF28DB2), Color(0xFFF7B0CA), Color(0xFF7A2E50), Color(0xFFFFEAF2), Color(0xFFFFCFE1)),
    Mariposa("Atardecer", "Chlosyne janais",
        "Sus alas negras con franjas anaranjadas brillan como el cielo al caer la tarde.",
        Color(0xFF202020), Color(0xFF2E2E2E), Color(0xFFFF7A2F), Color(0xFFFFE0CC), Color(0xFFFFB98F))
)

@Composable
fun GaleriaMariposasScreen(onCerrar: () -> Unit) {
    var abierta by remember { mutableStateOf<Mariposa?>(null) }
    var menu by remember { mutableStateOf(false) }
    val favoritas = remember { mutableStateListOf<String>() }
    var soloFavoritas by remember { mutableStateOf(false) }
    val visibles = if (soloFavoritas) Mariposas.filter { it.nombre in favoritas } else Mariposas

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFFBF8F3))) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp, top = 36.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Galería de mariposas", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3D2C1E))
                Text("${Mariposas.size} fotografías", fontSize = 13.sp, color = Color(0xFF8A7B6B))
            }
            Box {
                TextButton(onClick = { menu = true }) { Text("⋮", fontSize = 24.sp, color = Color(0xFF3D2C1E)) }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    DropdownMenuItem(text = { Text(if (soloFavoritas) "Ver todas" else "Solo favoritas ♥") }, onClick = { soloFavoritas = !soloFavoritas; menu = false })
                    DropdownMenuItem(text = { Text("Cerrar galería") }, onClick = { menu = false; onCerrar() })
                }
            }
        }

        if (visibles.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Aún no tienes favoritas. Toca el ♥ en alguna mariposa.", color = Color(0xFF8A7B6B), fontSize = 14.sp)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(visibles, key = { it.nombre }) { m ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.clickable { abierta = m }
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .background(Brush.verticalGradient(listOf(m.fondoArriba, m.fondoAbajo)))
                            ) {
                                DibujoMariposa(m, Modifier.fillMaxSize().padding(14.dp))
                                Text(
                                    if (m.nombre in favoritas) "♥" else "♡",
                                    fontSize = 20.sp, color = Color(0xFFD64572),
                                    modifier = Modifier.align(Alignment.TopEnd).padding(10.dp)
                                )
                            }
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(m.nombre, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF3D2C1E))
                                Text(m.cientifico, fontSize = 11.sp, color = Color(0xFF8A7B6B))
                            }
                        }
                    }
                }
            }
        }
    }

    abierta?.let { m ->
        Dialog(onDismissRequest = { abierta = null }) {
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .background(Brush.verticalGradient(listOf(m.fondoArriba, m.fondoAbajo)))
                    ) { DibujoMariposa(m, Modifier.fillMaxSize().padding(24.dp)) }
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(m.nombre, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color(0xFF3D2C1E))
                        Text(m.cientifico, fontSize = 12.sp, color = Color(0xFF8A7B6B))
                        Spacer(Modifier.height(10.dp))
                        Text(m.dato, fontSize = 14.sp, lineHeight = 20.sp, color = Color(0xFF4F4034))
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            TextButton(onClick = { if (m.nombre in favoritas) favoritas.remove(m.nombre) else favoritas.add(m.nombre) }) {
                                Text(if (m.nombre in favoritas) "♥ Quitar de favoritas" else "♡ Marcar favorita", color = Color(0xFFD64572))
                            }
                            TextButton(onClick = { abierta = null }) { Text("Cerrar", color = Color(0xFF3D2C1E)) }
                        }
                    }
                }
            }
        }
    }
}

/** Dibuja una mariposa con 4 alas ovaladas, manchas, cuerpo y antenas. */
@Composable
private fun DibujoMariposa(m: Mariposa, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val u = minOf(size.width, size.height * 1.25f) / 2f      // unidad de tamaño

        fun ala(lado: Float, arriba: Boolean) {
            val ancho = u * (if (arriba) 0.78f else 0.55f)
            val alto = u * (if (arriba) 0.56f else 0.42f)
            val centro = Offset(cx + lado * u * (if (arriba) 0.42f else 0.30f), cy + (if (arriba) -u * 0.18f else u * 0.28f))
            val giro = lado * (if (arriba) -25f else 30f)
            rotate(degrees = giro, pivot = centro) {
                drawOval(
                    color = if (arriba) m.alaSuperior else m.alaInferior,
                    topLeft = Offset(centro.x - ancho / 2f, centro.y - alto / 2f),
                    size = Size(ancho, alto)
                )
                // Manchas
                drawCircle(m.manchas, radius = alto * 0.16f, center = Offset(centro.x + lado * ancho * 0.12f, centro.y))
                drawCircle(m.manchas.copy(alpha = 0.7f), radius = alto * 0.10f, center = Offset(centro.x - lado * ancho * 0.16f, centro.y + alto * 0.12f))
            }
        }
        for (lado in listOf(-1f, 1f)) { ala(lado, true); ala(lado, false) }

        // Cuerpo y antenas
        drawRoundRect(
            color = Color(0xFF3A2A20),
            topLeft = Offset(cx - u * 0.05f, cy - u * 0.42f),
            size = Size(u * 0.10f, u * 0.84f),
            cornerRadius = CornerRadius(u * 0.05f)
        )
        drawLine(Color(0xFF3A2A20), Offset(cx, cy - u * 0.40f), Offset(cx - u * 0.18f, cy - u * 0.66f), strokeWidth = u * 0.025f)
        drawLine(Color(0xFF3A2A20), Offset(cx, cy - u * 0.40f), Offset(cx + u * 0.18f, cy - u * 0.66f), strokeWidth = u * 0.025f)
    }
}
