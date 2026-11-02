package com.example.crisalida.ui.theme.screens

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
import com.example.crisalida.data.FotoRemota
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crisalida.data.MarketplaceRepositorio
import com.example.crisalida.data.ProgresoRepositorio
import com.example.crisalida.data.ResultadoApi
import com.example.crisalida.data.RetoDto
import com.example.crisalida.data.RetosRepositorio
import kotlinx.coroutines.launch
import com.example.crisalida.ui.theme.*
import com.example.crisalida.ui.screens.CrisalidaScaffold
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
    val retoId: String,
    val aceptacionId: String?,      // solo en "Mis retos"
    val estado: String?,            // aceptado | entregado | validado (solo en "Mis retos")
    val titulo: String,
    val precio: String,
    val etiqueta: String,
    val etiquetaFondo: Color,
    val etiquetaTexto: Color,
    val categoria: String,
    val habilidad: String,
    val duracion: String,
    val imagenRuta: String?         // ruta de la foto en el servidor; null = marcador con icono
)

// Colores de las etiquetas: se reparten según el nombre para que cada etiqueta siempre tenga el mismo color
private val PaletaEtiquetas = listOf(
    Color(0xFFAFD0DD) to Color(0xFF11215A),
    Color(0xFFC8AEDD) to Color(0xFF2B115A),
    Color(0xFFDEBDAE) to Color(0xFF5A2511),
    Color(0xFFBFDDB5) to Color(0xFF1B3F11),
    Color(0xFFF0D9A6) to Color(0xFF5A4311)
)

private fun RetoDto.aUi(): RetoUi {
    val (fondo, texto) = PaletaEtiquetas[(etiqueta ?: categoria).hashCode().mod(PaletaEtiquetas.size)]
    return RetoUi(
        retoId = retoId,
        aceptacionId = aceptacionId,
        estado = estado,
        titulo = titulo,
        precio = formatoPrecio(compensacion) + " MXN",
        etiqueta = etiqueta ?: categoria,
        etiquetaFondo = fondo,
        etiquetaTexto = texto,
        categoria = categoria,
        habilidad = habilidadRequerida ?: "Sin requisito especial",
        duracion = "$duracionMin min. aprox",
        imagenRuta = imagenRuta
    )
}

@Composable
fun RetosScreen(
    onNavigate: (String) -> Unit,
    onAbrirDetalle: (String) -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    var verMisRetos by remember { mutableStateOf(false) }
    var seleccion by remember { mutableIntStateOf(0) }
    var aviso by remember { mutableStateOf<String?>(null) }
    var ocupado by remember { mutableStateOf(false) }

    // Categorías reales (las mismas que usa la base de datos)
    val categoriasCarga = rememberCarga { MarketplaceRepositorio.categorias("reto") }
    val categoriasDto = (categoriasCarga.estado as? Carga.Lista)?.datos?.categorias ?: emptyList()
    val categorias = listOf("Todos") + categoriasDto.map { it.nombre }
    val categoriaId: Int? = if (seleccion == 0) null else categoriasDto.getOrNull(seleccion - 1)?.id

    // Tablero y "mis retos"; `version` fuerza volver a pedirlos después de aceptar/entregar/soltar
    var version by remember { mutableIntStateOf(0) }
    val tablero = rememberCarga(categoriaId, version, ProgresoRepositorio.version) { RetosRepositorio.tablero(categoriaId = categoriaId) }
    val mios = rememberCarga(version, ProgresoRepositorio.version) { RetosRepositorio.mis() }

    val activo = if (verMisRetos) mios else tablero
    val visibles: List<RetoUi> = ((activo.estado as? Carga.Lista)?.datos?.retos ?: emptyList())
        .filter { it.estado != "abandonado" }
        .map { it.aUi() }

    fun ejecutar(accion: suspend () -> ResultadoApi<*>, exito: String) {
        if (ocupado) return
        ocupado = true
        scope.launch {
            aviso = when (val r = accion()) {
                is ResultadoApi.Exito -> exito
                is ResultadoApi.Error -> r.mensaje
            }
            ocupado = false
            version++
            ProgresoRepositorio.avisarCambio()
        }
    }

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
                        text = if (verMisRetos) "Mis retos" else "Retos disponibles hoy",
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 35.sp,
                        color = MkTitulo
                    )
                    Text(
                        text = if (verMisRetos) "${visibles.size}" else "${visibles.size} nuevos",
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.Normal,
                        fontSize = 18.sp,
                        color = MkGris,
                        modifier = Modifier.padding(bottom = 5.dp)
                    )
                }
            }

            // Pestañas: Disponibles / Mis retos
            item {
                Row(
                    modifier = Modifier.padding(start = MargenIzq, end = MargenDer, top = 9.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("Disponibles" to false, "Mis retos" to true).forEach { (texto, esMios) ->
                        val sel = verMisRetos == esMios
                        Box(
                            modifier = Modifier
                                .height(28.dp)
                                .clip(CircleShape)
                                .background(if (sel) MkVerde else MkChipNormal)
                                .clickable { verMisRetos = esMios }
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = texto,
                                fontFamily = OutfitFont,
                                fontSize = 15.sp,
                                color = if (sel) Color.White else Color.Black
                            )
                        }
                    }
                }
            }

            // Chips de categoría (solo en "Disponibles")
            if (!verMisRetos) item {
                LazyRow(
                    modifier = Modifier.padding(top = 9.dp),
                    contentPadding = PaddingValues(start = MargenIzq, end = MargenDer),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(categorias.size) { i ->
                        val activoChip = i == seleccion
                        Box(
                            modifier = Modifier
                                .height(24.dp)
                                .clip(CircleShape)
                                .background(if (activoChip) MkChipSeleccionado else MkChipNormal)
                                .clickable { seleccion = i }
                                .padding(horizontal = 13.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = categorias[i],
                                fontFamily = OutfitFont,
                                fontWeight = FontWeight.Normal,
                                fontSize = 15.sp,
                                color = if (activoChip) Color.White else Color.Black
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(3.dp)) }

            // Cargando / error / vacío
            item {
                when (val e = activo.estado) {
                    is Carga.Cargando -> IndicadorCargando()
                    is Carga.Falla -> MensajeError(e.mensaje, activo.recargar)
                    is Carga.Lista -> if (visibles.isEmpty()) {
                        Text(
                            text = if (verMisRetos) "Aún no has tomado ningún reto. ¡Ve a «Disponibles» y elige uno!"
                            else "No hay retos en esta categoría por ahora. ¡Vuelve pronto!",
                            fontFamily = OutfitFont, fontSize = 15.sp, color = MkGris,
                            modifier = Modifier.padding(start = MargenIzq, end = MargenDer, top = 16.dp)
                        )
                    }
                }
            }

            items(visibles) { reto ->
                val modificador = Modifier.padding(start = MargenIzq, end = MargenDer, top = 16.dp)
                if (!verMisRetos) {
                    RetoCard(
                        reto = reto,
                        modifier = modificador,
                        onAbrir = { onAbrirDetalle(reto.retoId) },
                        habilitado = !ocupado,
                        onAceptar = {
                            ejecutar({ RetosRepositorio.aceptar(reto.retoId) },
                                "¡Reto aceptado! Lo encuentras en «Mis retos». Cuando termines, márcalo como entregado.")
                        }
                    )
                } else when (reto.estado) {
                    "aceptado" -> RetoCard(
                        reto = reto, modifier = modificador, onAbrir = { onAbrirDetalle(reto.retoId) },
                        textoBoton = "Marcar como entregado", habilitado = !ocupado,
                        onAceptar = {
                            ejecutar({ RetosRepositorio.entregar(reto.aceptacionId.orEmpty()) },
                                "¡Entregado! Cuando el contratista lo valide, recibirás tus flores.")
                        },
                        textoSecundario = "Soltar este reto",
                        onSecundario = {
                            ejecutar({ RetosRepositorio.abandonar(reto.aceptacionId.orEmpty()) },
                                "Soltaste el reto. Volvió al tablero para otra persona.")
                        }
                    )
                    "entregado" -> RetoCard(reto = reto, modifier = modificador, onAbrir = { onAbrirDetalle(reto.retoId) }, textoBoton = "Esperando validación", habilitado = false)
                    else -> RetoCard(reto = reto, modifier = modificador, onAbrir = { onAbrirDetalle(reto.retoId) }, textoBoton = "Completado ✓", habilitado = false)
                }
            }
        }
    }

    aviso?.let { texto ->
        AlertDialog(
            onDismissRequest = { aviso = null },
            text = { Text(text = texto, fontFamily = OutfitFont, color = Color.Black) },
            confirmButton = {
                TextButton(onClick = { aviso = null }) {
                    Text(text = "Entendido", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, color = MkVerde)
                }
            },
            containerColor = Color.White
        )
    }
}

@Composable
private fun RetoCard(
    reto: RetoUi,
    modifier: Modifier = Modifier,
    onAbrir: () -> Unit = {},
    textoBoton: String = "Aceptar el reto",
    habilitado: Boolean = true,
    onAceptar: () -> Unit = {},
    textoSecundario: String? = null,
    onSecundario: () -> Unit = {}
) {
    Card(
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(containerColor = MkFondoCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = modifier.fillMaxWidth().clickable { onAbrir() }
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
                    fontSize = 25.sp,
                    lineHeight = 30.sp,
                    color = Color.Black,
                    modifier = Modifier.width(250.dp)
                )
                Text(
                    text = reto.precio,
                    fontFamily = OutfitFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 21.sp,
                    color = MkVerde
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Etiqueta de categoría
            Box(
                modifier = Modifier
                    .wrapContentHeight()
                    .clip(CircleShape)
                    .background(reto.etiquetaFondo)
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = reto.etiqueta,
                    fontFamily = OutfitFont,
                    fontSize = 15.sp,
                    lineHeight = 10.sp,
                    color = reto.etiquetaTexto
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Foto del reto (si no tiene, o mientras carga, se ve el marcador)
            FotoRemota(
                ruta = reto.imagenRuta,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(125.dp)
                    .clip(RoundedCornerShape(7.dp)),
                alternativa = {
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
            )

            Spacer(modifier = Modifier.height(15.dp))

            // Caja de habilidad y duración
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(7.dp))
                    .background(MkSkillFondo)
                    .padding(start = 6.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier.wrapContentHeight(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AccountTree,
                        contentDescription = null,
                        tint = MkGris,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "Habilidad: ${reto.habilidad}",
                        fontFamily = OutfitFont,
                        fontSize = 15.sp,
                        color = MkGris
                    )
                }
                Row(
                    modifier = Modifier.wrapContentHeight(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = MkGris,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = reto.duracion,
                        fontFamily = OutfitFont,
                        fontSize = 15.sp,
                        color = MkGris
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botón alineado a la derecha
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Button(
                    onClick = onAceptar,
                    enabled = habilitado,
                    modifier = Modifier
                        .width(191.dp)
                        .height(38.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MkVerde),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = textoBoton,
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.Normal,
                        fontSize = 16.sp,
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

            if (textoSecundario != null) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onSecundario, enabled = habilitado) {
                        Text(text = textoSecundario, fontFamily = OutfitFont, fontSize = 14.sp, color = MkGris)
                    }
                }
            }
        }
    }
}
