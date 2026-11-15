package com.example.crisalida.ui.theme.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crisalida.data.MarketplaceRepositorio
import com.example.crisalida.data.ProgresoRepositorio
import com.example.crisalida.data.ResultadoApi
import com.example.crisalida.data.SesionActual
import com.example.crisalida.data.RetoDto
import com.example.crisalida.data.RetosRepositorio
import kotlinx.coroutines.launch
import com.example.crisalida.ui.theme.*
import com.example.crisalida.ui.theme.screens.CrisalidaScaffold
// colores del mockup
private val MkTitulo = Color(0xFF9D4067)
private val MkGris = Color(0xFF545454)
private val MkChipSeleccionado = Color(0xFF62012A)
private val MkChipNormal = Color(0xFFE0DCDA)
private val MkVerde = Color(0xFF1E681E)
private val MkSkillFondo = Color(0xFFE8D7DD)
private val MkFondoCard = Color(0xFFF2F0EF)

private val MargenIzq = 21.dp
private val MargenDer = 12.dp

private data class RetoUi(
    val retoId: String,
    val aceptacionId: String?,      // solo en "Mis retos"
    val estado: String?,            // aceptado, entregado, validado
    val titulo: String,
    val precio: String,
    val etiqueta: String,
    val etiquetaFondo: Color,
    val etiquetaTexto: Color,
    val categoria: String,
    val habilidad: String,
    val duracion: String,
    val imagenRuta: String?         // ruta de la foto en el servidor
)

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

@OptIn(ExperimentalLayoutApi::class)
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

    // Categorías de la bd
    val categoriasCarga = rememberCarga { MarketplaceRepositorio.categorias("reto") }
    val categoriasDto = (categoriasCarga.estado as? Carga.Lista)?.datos?.categorias ?: emptyList()
    val categorias = listOf("Todos") + categoriasDto.map { it.nombre }
    val categoriaId: Int? = if (seleccion == 0) null else categoriasDto.getOrNull(seleccion - 1)?.id

    var version by remember { mutableIntStateOf(0) }
    val tablero = rememberCarga(categoriaId, version, ProgresoRepositorio.version) { RetosRepositorio.tablero(categoriaId = categoriaId) }
    val mios = rememberCarga(version, ProgresoRepositorio.version) { soloVendedora { RetosRepositorio.mis() } }

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
        // Las tarjetas se acomodan solas según el ancho: 1 columna en teléfono, 2 o más en pantallas anchas.
        // Los encabezados (título, pestañas, chips, mensajes) ocupan todo el ancho.
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 320.dp),
            modifier = Modifier
                .fillMaxSize()
                .background(ColorFondoApp)
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 22.dp)
        ) {
            // Título + contador
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 19.dp, end = 11.dp, top = 17.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    // el título cede espacio al contador: antes, en pantallas angostas el contador desaparecía
                    Text(
                        text = if (verMisRetos) "Mis retos" else "Retos disponibles hoy",
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 35.sp,
                        lineHeight = 40.sp,
                        color = MkTitulo,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = if (verMisRetos) "${visibles.size}" else "${visibles.size} nuevos",
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.Normal,
                        fontSize = 18.sp,
                        color = MkGris,
                        modifier = Modifier.padding(start = 8.dp, bottom = 5.dp)
                    )
                }
            }

            // Pestañas: Disponibles / Mis retos (si no caben en una línea, la segunda baja).
            // La compradora no completa retos: no tiene "Mis retos".
            if (!SesionActual.esCompradora) item(span = { GridItemSpan(maxLineSpan) }) {
                FlowRow(
                    modifier = Modifier.padding(start = MargenIzq, end = MargenDer, top = 9.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("Disponibles" to false, "Mis retos" to true).forEach { (texto, esMios) ->
                        val sel = verMisRetos == esMios
                        Box(
                            modifier = Modifier
                                .heightIn(min = 36.dp)
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
            if (!verMisRetos) item(span = { GridItemSpan(maxLineSpan) }) {
                LazyRow(
                    modifier = Modifier.padding(top = 9.dp),
                    contentPadding = PaddingValues(start = MargenIzq, end = MargenDer),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(categorias.size) { i ->
                        val activoChip = i == seleccion
                        Box(
                            modifier = Modifier
                                .heightIn(min = 34.dp)
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

            item(span = { GridItemSpan(maxLineSpan) }) { Spacer(modifier = Modifier.height(3.dp)) }

            // Cargando / error / vacío
            item(span = { GridItemSpan(maxLineSpan) }) {
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
                        mostrarBoton = !SesionActual.esCompradora,   // la compradora solo mira los retos
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
            text = { Text(text = texto, fontFamily = OutfitFont, color = Color.Black, modifier = Modifier.verticalScroll(rememberScrollState())) },
            confirmButton = {
                TextButton(onClick = { aviso = null }) {
                    Text(text = "Entendido", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, color = MkVerde)
                }
            },
            containerColor = Color.White
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RetoCard(
    reto: RetoUi,
    modifier: Modifier = Modifier,
    onAbrir: () -> Unit = {},
    textoBoton: String = "Aceptar el reto",
    habilitado: Boolean = true,
    mostrarBoton: Boolean = true,
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // antes el título medía 250 dp fijos y el precio se cortaba en pantallas angostas
                Text(
                    text = reto.titulo,
                    fontFamily = OutfitFont,
                    fontWeight = FontWeight.Medium,
                    fontSize = 25.sp,
                    lineHeight = 30.sp,
                    color = Color.Black,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = reto.precio,
                    fontFamily = OutfitFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 21.sp,
                    lineHeight = 26.sp,
                    textAlign = TextAlign.End,
                    color = MkVerde,
                    modifier = Modifier.padding(start = 8.dp).widthIn(max = 170.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .wrapContentHeight()
                    .clip(CircleShape)
                    .background(reto.etiquetaFondo)
                    .padding(horizontal = 10.dp, vertical = 3.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = reto.etiqueta,
                    fontFamily = OutfitFont,
                    fontSize = 15.sp,
                    lineHeight = 20.sp,
                    color = reto.etiquetaTexto
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

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

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(7.dp))
                    .background(MkSkillFondo)
                    .padding(start = 6.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
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

            if (mostrarBoton) {
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Button(
                    onClick = onAceptar,
                    enabled = habilitado,
                    modifier = Modifier
                        .widthIn(min = 191.dp)
                        .heightIn(min = 38.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MkVerde),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
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


@PreviewResponsivo
@Composable
private fun RetoCardPreview() {
    Column(modifier = Modifier.background(ColorFondoApp).verticalScroll(rememberScrollState()).padding(vertical = 8.dp)) {
        RetoCard(
            reto = RetoUi(
                retoId = "1", aceptacionId = "a1", estado = "aceptado",
                titulo = "Etiquetado de frascos para mermelada artesanal",
                precio = "1,250.00 MXN", etiqueta = "Manualidades",
                etiquetaFondo = Color(0xFFAFD0DD), etiquetaTexto = Color(0xFF11215A),
                categoria = "Manualidades", habilidad = "Trabajo manual y atención al detalle",
                duracion = "45 min. aprox", imagenRuta = null
            ),
            modifier = Modifier.padding(start = MargenIzq, end = MargenDer, top = 16.dp),
            textoBoton = "Marcar como entregado",
            textoSecundario = "Soltar este reto"
        )
    }
}
