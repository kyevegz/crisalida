package com.example.crisalida.ui.theme.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.crisalida.ui.theme.ConEscalaLimitada
import com.example.crisalida.ui.theme.PaletaGaleria

// la app "falsa" que se abre con el PIN de camuflaje
// todo es local, no pide nada al servidor. Para salir: menú ⋮ > Cerrar galería

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
//mete mariposas falsas para simular
private val Mariposas = listOf(
    Mariposa("Monarca", "Danaus plexippus",
        "Viaja hasta 4,000 km cada año desde Canadá hasta los bosques de oyamel de Michoacán y el Estado de México.",
        PaletaGaleria.cF08A24, PaletaGaleria.cF5A742, PaletaGaleria.c2B1B12, PaletaGaleria.cFFE9C7, PaletaGaleria.cFFD08A),
    Mariposa("Azul morfo", "Morpho menelaus",
        "Sus alas no tienen pigmento azul: el color nace de la forma microscópica de sus escamas.",
        PaletaGaleria.c2F7DE1, PaletaGaleria.c5AA0F2, PaletaGaleria.c0E2A55, PaletaGaleria.cD7EBFF, PaletaGaleria.cA9D0FF),
    Mariposa("Cola de golondrina", "Papilio polyxenes",
        "Sus alas traseras terminan en pequeñas «colas», como las de una golondrina.",
        PaletaGaleria.c2B2B33, PaletaGaleria.c3B3B47, PaletaGaleria.cF5D547, PaletaGaleria.cE6F2D6, PaletaGaleria.cBFDDA0),
    Mariposa("Mariposa blanca", "Pieris rapae",
        "Es una de las mariposas más comunes en huertos y jardines de todo el mundo.",
        PaletaGaleria.cFFFFFF, PaletaGaleria.cF1F1F1, PaletaGaleria.c4A4A4A, PaletaGaleria.cD9EEF2, PaletaGaleria.cB4DCE6),
    Mariposa("Cuatro espejos", "Siproeta stelenes",
        "Sus alas verdes con bordes oscuros la hacen parecer una hoja iluminada por el sol.",
        PaletaGaleria.c5BBF5B, PaletaGaleria.c7ED27E, PaletaGaleria.c1E3D1E, PaletaGaleria.cEAF7DF, PaletaGaleria.cC6E8AE),
    Mariposa("Pavo real", "Junonia coenia",
        "Cada ala tiene «ojos» que confunden a los pájaros que intentan atraparla.",
        PaletaGaleria.c9C5B2E, PaletaGaleria.cB7753E, PaletaGaleria.c3E6FB5, PaletaGaleria.cFFE3E0, PaletaGaleria.cFFC2BB),
    Mariposa("Mariposa rosa", "Eurema daira",
        "Le gusta posarse en flores pequeñas y pasa el día entre jardines soleados.",
        PaletaGaleria.cF28DB2, PaletaGaleria.cF7B0CA, PaletaGaleria.c7A2E50, PaletaGaleria.cFFEAF2, PaletaGaleria.cFFCFE1),
    Mariposa("Atardecer", "Chlosyne janais",
        "Sus alas negras con franjas anaranjadas brillan como el cielo al caer la tarde.",
        PaletaGaleria.c202020, PaletaGaleria.c2E2E2E, PaletaGaleria.cFF7A2F, PaletaGaleria.cFFE0CC, PaletaGaleria.cFFB98F)
)

@Composable
fun GaleriaMariposasScreen(onCerrar: () -> Unit) {
    var abierta by remember { mutableStateOf<Mariposa?>(null) }
    var menu by remember { mutableStateOf(false) }
    val favoritas = remember { mutableStateListOf<String>() }
    var soloFavoritas by remember { mutableStateOf(false) }
    val visibles = if (soloFavoritas) Mariposas.filter { it.nombre in favoritas } else Mariposas

    Column(modifier = Modifier.fillMaxSize().background(PaletaGaleria.cFBF8F3).statusBarsPadding()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp, top = 12.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // título grande: crece poco para que "mariposas" no se parta en pantallas chicas
                ConEscalaLimitada(tope = 1.3f) {
                    Text("Galería de mariposas", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = PaletaGaleria.c3D2C1E)
                }
                Text("${Mariposas.size} fotografías", fontSize = 13.sp, color = PaletaGaleria.c8A7B6B)
            }
            Box {
                TextButton(onClick = { menu = true }) { Text("⋮", fontSize = 24.sp, color = PaletaGaleria.c3D2C1E) }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    DropdownMenuItem(text = { Text(if (soloFavoritas) "Ver todas" else "Solo favoritas ♥") }, onClick = { soloFavoritas = !soloFavoritas; menu = false })
                    DropdownMenuItem(text = { Text("Cerrar galería") }, onClick = { menu = false; onCerrar() })
                }
            }
        }

        if (visibles.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "Aún no tienes favoritas. Toca el ♥ en alguna mariposa.",
                    color = PaletaGaleria.c8A7B6B, fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 130.dp),   // 2 columnas en 320 dp, más en pantallas anchas
                // abajo se suma la barra de navegación del sistema para que la última fila no quede tapada
                contentPadding = PaddingValues(
                    start = 16.dp, top = 16.dp, end = 16.dp,
                    bottom = 16.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                ),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(visibles, key = { it.nombre }) { m ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = PaletaGaleria.cFFFFFF),
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
                                    fontSize = 20.sp, color = PaletaGaleria.cD64572,
                                    modifier = Modifier.align(Alignment.TopEnd).padding(10.dp)
                                )
                            }
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(m.nombre, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = PaletaGaleria.c3D2C1E)
                                Text(m.cientifico, fontSize = 12.sp, color = PaletaGaleria.c8A7B6B)
                            }
                        }
                    }
                }
            }
        }
    }

    abierta?.let { m ->
        Dialog(onDismissRequest = { abierta = null }) {
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = PaletaGaleria.cFFFFFF)) {
                // con scroll: en horizontal o con texto grande el contenido no cabe y los botones quedaban fuera
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .background(Brush.verticalGradient(listOf(m.fondoArriba, m.fondoAbajo)))
                    ) { DibujoMariposa(m, Modifier.fillMaxSize().padding(24.dp)) }
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(m.nombre, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = PaletaGaleria.c3D2C1E)
                        Text(m.cientifico, fontSize = 13.sp, color = PaletaGaleria.c8A7B6B)
                        Spacer(Modifier.height(10.dp))
                        Text(m.dato, fontSize = 14.sp, lineHeight = 20.sp, color = PaletaGaleria.c4F4034)
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            // el texto largo cede espacio para que "Cerrar" siempre se vea
                            TextButton(
                                onClick = { if (m.nombre in favoritas) favoritas.remove(m.nombre) else favoritas.add(m.nombre) },
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                Text(if (m.nombre in favoritas) "♥ Quitar de favoritas" else "♡ Marcar favorita", color = PaletaGaleria.cD64572)
                            }
                            TextButton(onClick = { abierta = null }) { Text("Cerrar", color = PaletaGaleria.c3D2C1E) }
                        }
                    }
                }
            }
        }
    }
}

// mariposa con 4 alas, manchas, cuerpo y antenas, las dibuja mediante el canvas para no tener tema con los derechos de autor
@Composable
private fun DibujoMariposa(m: Mariposa, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val u = minOf(size.width, size.height * 1.25f) / 2f//unidad de tamaño

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
                //manchas
                drawCircle(m.manchas, radius = alto * 0.16f, center = Offset(centro.x + lado * ancho * 0.12f, centro.y))
                drawCircle(m.manchas.copy(alpha = 0.7f), radius = alto * 0.10f, center = Offset(centro.x - lado * ancho * 0.16f, centro.y + alto * 0.12f))
            }
        }
        for (lado in listOf(-1f, 1f)) { ala(lado, true); ala(lado, false) }

        //cuerpo y antenas
        drawRoundRect(
            color = PaletaGaleria.c3A2A20,
            topLeft = Offset(cx - u * 0.05f, cy - u * 0.42f),
            size = Size(u * 0.10f, u * 0.84f),
            cornerRadius = CornerRadius(u * 0.05f)
        )
        drawLine(PaletaGaleria.c3A2A20, Offset(cx, cy - u * 0.40f), Offset(cx - u * 0.18f, cy - u * 0.66f), strokeWidth = u * 0.025f)
        drawLine(PaletaGaleria.c3A2A20, Offset(cx, cy - u * 0.40f), Offset(cx + u * 0.18f, cy - u * 0.66f), strokeWidth = u * 0.025f)
    }
}