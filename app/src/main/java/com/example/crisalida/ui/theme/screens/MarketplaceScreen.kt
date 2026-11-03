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
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.StarBorder
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
import com.example.crisalida.ui.theme.*
import com.example.crisalida.data.FotoRemota
import com.example.crisalida.data.MarketplaceRepositorio
import com.example.crisalida.data.ProgresoRepositorio
import com.example.crisalida.data.PublicacionDto
import com.example.crisalida.ui.screens.CrisalidaScaffold
/* ──────────────────────────────────────────────────────────────
 * Colores y dimensiones MEDIDOS del mockup del Marketplace
 * ────────────────────────────────────────────────────────────── */
private val MkTitulo = Color(0xFF9D4067)
private val MkGrisContador = Color(0xFF545454)
private val MkChipSeleccionado = Color(0xFF62012A)
private val MkChipNormal = Color(0xFFE0DCDA)
private val MkFondoCard = Color(0xFFF2F0EF)
private val MkTagVendedoraFondo = Color(0xFFD3C5CD)
private val MkTagVendedoraPunto = Color(0xFF6B3045)
private val MkVerdePuntoMedio = Color(0xFFA5E3A1)
private val MkVerdeTextoPunto = Color(0xFF1B5E20)
private val MkVerdeBotonComprar = Color(0xFF1E681E)

private val MargenIzq = 19.dp
private val MargenDer = 11.dp

data class ProductoMarketplaceUi(
    val id: String,
    val titulo: String,
    val vendedora: String,
    val calificacion: String,
    val ventas: Int,
    val puntoEntrega: String,
    val precio: String,
    val esServicio: Boolean = false,
    val notaPrecio: String? = null, // p. ej. "*Por sesión"
    val categoria: String,
    val esMia: Boolean = false,
    @DrawableRes val imagenRes: Int? = null,
    val imagenRuta: String? = null   // foto del servidor (ver FotoRemota)
)

@Composable
fun MarketplaceScreen(
    onNavigate: (String) -> Unit,
    onAbrirDetalle: (String) -> Unit = {}
) {
    // Filtros: "Todos", los dos tipos y las categorías reales de la base de datos
    val categoriasCarga = rememberCarga { MarketplaceRepositorio.categorias() }
    val categoriasDto = ((categoriasCarga.estado as? Carga.Lista)?.datos?.categorias ?: emptyList())
        .filter { it.ambito != "reto" }
    val categorias = listOf("Todos", "Productos", "Servicios") + categoriasDto.map { it.nombre }

    var seleccionCategoria by remember { mutableIntStateOf(0) }
    val tipo: String? = when (seleccionCategoria) { 1 -> "producto"; 2 -> "servicio"; else -> null }
    val categoriaId: Int? = if (seleccionCategoria >= 3) categoriasDto.getOrNull(seleccionCategoria - 3)?.id else null

    // Catálogo real: se vuelve a pedir cada vez que cambia el filtro
    val catalogo = rememberCarga(tipo, categoriaId, ProgresoRepositorio.version) { MarketplaceRepositorio.catalogo(tipo = tipo, categoriaId = categoriaId) }
    val productosFiltrados: List<ProductoMarketplaceUi> =
        (catalogo.estado as? Carga.Lista)?.datos?.publicaciones?.map { it.aMarketplaceUi() } ?: emptyList()

    CrisalidaScaffold(
        currentTitle = "Marketplace",
        selectedTab = 1, // Pestaña del Marketplace seleccionada (índice 1)
        onNavigate = onNavigate
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(ColorFondoApp)
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Header: Título "Explorar por rubro" + Contador de productos
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = MargenIzq, end = MargenDer, top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "Explorar por rubro",
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp,
                        color = MkTitulo
                    )
                    Text(
                        text = "${productosFiltrados.size} publicaciones",
                        fontFamily = JakartaSansFont,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        color = MkGrisContador,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }

            // Chips de filtrado horizontal
            item {
                LazyRow(
                    modifier = Modifier.padding(top = 10.dp),
                    contentPadding = PaddingValues(start = MargenIzq, end = MargenDer),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(categorias.size) { i ->
                        val estaActivo = i == seleccionCategoria
                        Box(
                            modifier = Modifier
                                .height(30.dp)
                                .clip(CircleShape)
                                .background(if (estaActivo) MkChipSeleccionado else MkChipNormal)
                                .clickable { seleccionCategoria = i }
                                .padding(horizontal = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = categorias[i],
                                fontFamily = OutfitFont,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                color = if (estaActivo) Color.White else Color.Black
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                when (val e = catalogo.estado) {
                    is Carga.Cargando -> IndicadorCargando()
                    is Carga.Falla -> MensajeError(e.mensaje, catalogo.recargar)
                    is Carga.Lista -> if (productosFiltrados.isEmpty()) {
                        Text(
                            text = "Todavía no hay publicaciones en este rubro.",
                            fontFamily = JakartaSansFont, fontSize = 14.sp, color = MkGrisContador,
                            modifier = Modifier.padding(start = MargenIzq, end = MargenDer, top = 8.dp)
                        )
                    }
                }
            }

            // Lista de Tarjetas de Productos / Servicios
            items(productosFiltrados) { producto ->
                ProductoCard(
                    producto = producto,
                    modifier = Modifier.padding(start = MargenIzq, end = MargenDer, top = 14.dp),
                    onAbrir = { onAbrirDetalle(producto.id) },
                    onComprarClick = { onAbrirDetalle(producto.id) }
                )
            }
        }
    }
}

@Composable
private fun ProductoCard(
    producto: ProductoMarketplaceUi,
    modifier: Modifier = Modifier,
    onAbrir: () -> Unit = {},
    onComprarClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MkFondoCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = modifier.fillMaxWidth().clickable { onAbrir() }
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            // Imagen del Producto / Servicio
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFD9D9D9)),
                contentAlignment = Alignment.Center
            ) {
                if (producto.imagenRuta != null) {
                    FotoRemota(
                        ruta = producto.imagenRuta,
                        modifier = Modifier.fillMaxSize(),
                        alternativa = {
                            Icon(
                                imageVector = Icons.Outlined.Image,
                                contentDescription = null,
                                tint = MkGrisContador,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    )
                } else if (producto.imagenRes != null) {
                    Image(
                        painter = painterResource(id = producto.imagenRes),
                        contentDescription = producto.titulo,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Marcador visual por defecto si aún no hay imagen en res/drawable
                    Icon(
                        imageVector = Icons.Outlined.Image,
                        contentDescription = null,
                        tint = MkGrisContador,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Fila de vendedora (Chip) + Rating con ventas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Insignia / Tag del seudónimo
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MkTagVendedoraFondo)
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(MkTagVendedoraPunto)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = producto.vendedora,
                        fontFamily = JakartaSansFont,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MkTagVendedoraPunto
                    )
                }

                // Calificación y ventas (ej. ☆ 4.9 (18 ventas))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.StarBorder,
                        contentDescription = "Rating",
                        tint = Color.Black,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${producto.calificacion} (${producto.ventas} ventas)",
                        fontFamily = JakartaSansFont,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Título del Producto
            Text(
                text = producto.titulo,
                fontFamily = OutfitFont,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                lineHeight = 22.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Banner verde: Entrega en punto medio
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MkVerdePuntoMedio)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = MkVerdeTextoPunto,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = producto.puntoEntrega,
                    fontFamily = JakartaSansFont,
                    fontSize = 12.sp,
                    color = MkVerdeTextoPunto,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Fila de Precio + Botón "Comprar"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = producto.precio,
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp,
                        color = Color.Black
                    )
                    if (producto.notaPrecio != null) {
                        Text(
                            text = producto.notaPrecio,
                            fontFamily = JakartaSansFont,
                            fontSize = 11.sp,
                            color = MkGrisContador
                        )
                    }
                }

                if (producto.esMia) {
                    // es de ella, no se compra
                    Box(
                        modifier = Modifier
                            .widthIn(min = 135.dp)
                            .heightIn(min = 40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MkTagVendedoraFondo)
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (producto.esServicio) "Este es tu servicio" else "Este es tu producto",
                            fontFamily = OutfitFont,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = MkTagVendedoraPunto
                        )
                    }
                } else {
                    Button(
                        onClick = onComprarClick,
                        modifier = Modifier
                            .width(135.dp)
                            .height(40.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MkVerdeBotonComprar),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "Comprar",
                            fontFamily = OutfitFont,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 19.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

/** Del servidor a la tarjeta. La dirección real NUNCA llega aquí: solo la zona pública. */
private fun PublicacionDto.aMarketplaceUi() = ProductoMarketplaceUi(
    id = id,
    titulo = titulo,
    vendedora = vendedora ?: "Vendedora",
    calificacion = calificacion?.let { String.format(java.util.Locale.US, "%.1f", it) } ?: "Nueva",
    ventas = vendidos,
    puntoEntrega = entregaPublicaTexto(),
    precio = formatoPrecio(precio) + " MXN" + (if (notaPrecio.isNullOrBlank()) "" else "*"),
    esServicio = tipo == "servicio",
    notaPrecio = notaPrecio?.takeIf { it.isNotBlank() }?.let { "*$it" },
    categoria = categoria,
    esMia = esMia,
    imagenRuta = imagenes.firstOrNull()
)
