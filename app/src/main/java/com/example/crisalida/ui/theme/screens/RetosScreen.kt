package com.example.crisalida.ui.theme.screens

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Schedule
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
import com.example.crisalida.ui.screens.CrisalidaScaffold
import com.example.crisalida.ui.theme.*

/* Colores medidos del mockup (microrretos.png, 1080 px = 360 dp) */
private val MkTitulo = Color(0xFF9D4067)
private val MkGris = Color(0xFF545454)
private val MkChipSeleccionado = Color(0xFF62012A)
private val MkChipNormal = Color(0xFFE0DCDA)
private val MkVerde = Color(0xFF1E681E)
private val MkSkillFondo = Color(0xFFE8D7DD)
private val MkFondoCard = Color(0xFFF2F0EF)

// El mockup tiene 21 dp a la izquierda y 12 dp a la derecha en las tarjetas.
private val MargenIzq = 21.dp
private val MargenDer = 12.dp

private data class RetoUi(
    val titulo: String,
    val precio: String,
    val etiqueta: String,
    val etiquetaFondo: Color,
    val etiquetaTexto: Color,
    val categoria: String,          // chip del filtro al que pertenece
    val habilidad: String,
    val duracion: String,
    @DrawableRes val imagen: Int?   // null = marcador con icono
)

@Composable
fun RetosScreen(
    onNavigate: (String) -> Unit
) {
    val categorias = listOf("Todos", "Organización y datos", "Redacción y textos", "Cocina y recetas")
    var seleccion by remember { mutableIntStateOf(0) }

    // Pon aquí tus fotos: imagen = R.drawable.reto_excel, etc.
    val retos = remember {
        listOf(
            RetoUi(
                "Captura y orden de 35 contactos en Excel", "$120.00 MXN",
                "TICS", Color(0xFFAFD0DD), Color(0xFF11215A),
                "Organización y datos", "Organización básica", "45 min. aprox", null
            ),
            RetoUi(
                "Revisar ortografía de un menú de cafetería (3pags)", "$150.00 MXN",
                "Redacción", Color(0xFFC8AEDD), Color(0xFF2B115A),
                "Redacción y textos", "Atención al detalle", "30 min. aprox", null
            ),
            RetoUi(
                "Receta del mole más rico", "$90.00 MXN",
                "Cocina", Color(0xFFDEBDAE), Color(0xFF5A2511),
                "Cocina y recetas", "Cocinar muy bueno", "10 min. aprox", null
            )
        )
    }
    val visibles = if (seleccion == 0) retos else retos.filter { it.categoria == categorias[seleccion] }

    CrisalidaScaffold(
        currentTitle = "Tablero de retos",
        selectedTab = 0,
        onNavigate = onNavigate
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(ColorFondoApp)
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 22.dp)
        ) {
            // Título + contador
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 19.dp, end = 11.dp, top = 17.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "Retos disponibles hoy",
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 26.sp,
                        color = MkTitulo
                    )
                    Text(
                        text = "${retos.size} nuevos",
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = MkGris,
                        modifier = Modifier.padding(bottom = 5.dp)
                    )
                }
            }

            // Chips de categoría (se desplazan; el 4.º queda cortado como en el mockup)
            item {
                LazyRow(
                    modifier = Modifier.padding(top = 9.dp),
                    contentPadding = PaddingValues(start = MargenIzq, end = MargenDer),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(categorias.size) { i ->
                        val activo = i == seleccion
                        Box(
                            modifier = Modifier
                                .height(24.dp)
                                .clip(CircleShape)
                                .background(if (activo) MkChipSeleccionado else MkChipNormal)
                                .clickable { seleccion = i }
                                .padding(horizontal = 13.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = categorias[i],
                                fontFamily = OutfitFont,
                                fontWeight = FontWeight.Normal,
                                fontSize = 11.5.sp,
                                color = if (activo) Color.White else Color.Black
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(3.dp)) }

            items(visibles) { reto ->
                RetoCard(
                    reto = reto,
                    modifier = Modifier.padding(start = MargenIzq, end = MargenDer, top = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun RetoCard(
    reto: RetoUi,
    modifier: Modifier = Modifier,
    onAceptar: () -> Unit = {}
) {
    Card(
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(containerColor = MkFondoCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(start = 13.dp, end = 11.dp, top = 10.dp, bottom = 13.dp)) {
            // Título + precio
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = reto.titulo,
                    fontFamily = OutfitFont,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    lineHeight = 20.sp,
                    color = Color.Black,
                    modifier = Modifier.width(147.dp)
                )
                Text(
                    text = reto.precio,
                    fontFamily = OutfitFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 21.sp,
                    color = MkVerde
                )
            }

            Spacer(modifier = Modifier.height(1.dp))

            // Etiqueta de categoría
            Box(
                modifier = Modifier
                    .height(13.dp)
                    .clip(CircleShape)
                    .background(reto.etiquetaFondo)
                    .padding(horizontal = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = reto.etiqueta,
                    fontFamily = OutfitFont,
                    fontSize = 8.sp,
                    lineHeight = 10.sp,
                    color = reto.etiquetaTexto
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Foto
            if (reto.imagen != null) {
                Image(
                    painter = painterResource(id = reto.imagen),
                    contentDescription = reto.titulo,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(125.dp)
                        .clip(RoundedCornerShape(7.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(125.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(ColorCamposFondo),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Image,
                        contentDescription = null,
                        tint = MkGris,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(15.dp))

            // Caja de habilidad y duración
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(41.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(MkSkillFondo)
                    .padding(start = 6.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier.height(15.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AccountTree,
                        contentDescription = null,
                        tint = MkGris,
                        modifier = Modifier.size(9.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "Habilidad: ${reto.habilidad}",
                        fontFamily = OutfitFont,
                        fontSize = 10.sp,
                        color = MkGris
                    )
                }
                Row(
                    modifier = Modifier.height(15.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = MkGris,
                        modifier = Modifier.size(9.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = reto.duracion,
                        fontFamily = OutfitFont,
                        fontSize = 10.sp,
                        color = MkGris
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botón alineado a la derecha
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Button(
                    onClick = onAceptar,
                    modifier = Modifier
                        .width(191.dp)
                        .height(38.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MkVerde),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "Aceptar el reto",
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        color = Color(0xFFFFFAFA)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Icon(
                        imageVector = Icons.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color(0xFFFFFAFA),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}