package com.example.crisalida.ui.theme.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crisalida.data.FotoRemota
import com.example.crisalida.data.MarketplaceRepositorio
import com.example.crisalida.data.PublicacionDto
import com.example.crisalida.ui.theme.*
import com.example.crisalida.ui.screens.CrisalidaScaffold
private val PdGris = Color(0xFF545454)
private val PdVerde = Color(0xFF1E681E)
private val PdVino = Color(0xFF6B3045)

/** Detalle de un producto o servicio del marketplace (estilo tienda en línea): fotos, precio, vendedora, entrega y descripción. */
@Composable
fun ProductoDetalleScreen(
    publicacionId: String,
    onNavigate: (String) -> Unit,
    onVolver: () -> Unit,
    onEditar: (String) -> Unit = {}
) {
    val carga = rememberCarga(publicacionId) { MarketplaceRepositorio.detalle(publicacionId) }
    var dialogoCompra by remember { mutableStateOf(false) }

    CrisalidaScaffold(currentTitle = "Marketplace", selectedTab = 1, onNavigate = onNavigate) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ColorFondoApp)
                .padding(innerPadding)
        ) {
            BarraVolver("Volver al marketplace", onVolver)
            when (val e = carga.estado) {
                is Carga.Cargando -> IndicadorCargando()
                is Carga.Falla -> MensajeError(e.mensaje, carga.recargar)
                is Carga.Lista -> Column(
                    modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())
                ) {
                    Contenido(e.datos, onComprar = { dialogoCompra = true }, onEditar = { onEditar(e.datos.id) })
                }
            }
        }
    }

    if (dialogoCompra) {
        AlertDialog(
            onDismissRequest = { dialogoCompra = false },
            title = { Text("Compra en construcción", fontFamily = OutfitFont, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "El proceso de compra (pago en custodia y entrega) todavía no está activo en esta versión. " +
                        "Cuando lo esté, todo el contacto será dentro de la app, sin compartir teléfonos ni direcciones.",
                    fontFamily = JakartaSansFont
                )
            },
            confirmButton = { TextButton(onClick = { dialogoCompra = false }) { Text("Entendido", color = ColorBotonEntrar) } },
            containerColor = Color.White
        )
    }
}

@Composable
private fun Contenido(p: PublicacionDto, onComprar: () -> Unit, onEditar: () -> Unit) {
    var seleccionada by remember(p.id) { mutableIntStateOf(0) }

    // Foto principal
    Box(
        modifier = Modifier.fillMaxWidth().height(260.dp).background(Color(0xFFD9D9D9)),
        contentAlignment = Alignment.Center
    ) {
        FotoRemota(
            ruta = p.imagenes.getOrNull(seleccionada),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alternativa = { Icon(Icons.Outlined.Image, contentDescription = null, tint = PdGris, modifier = Modifier.size(56.dp)) }
        )
    }
    // Miniaturas (si hay más de una foto)
    if (p.imagenes.size > 1) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(p.imagenes) { i, ruta ->
                FotoRemota(
                    ruta = ruta,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(2.dp, if (i == seleccionada) PdVino else Color.Transparent, RoundedCornerShape(8.dp))
                        .clickable { seleccionada = i }
                )
            }
        }
    }

    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
        Text(
            if (p.tipo == "servicio") "Servicio · ${p.categoria}" else "Producto · ${p.categoria}",
            fontFamily = JakartaSansFont, fontSize = 12.sp, color = PdGris
        )
        Spacer(Modifier.height(4.dp))
        Text(p.titulo, fontFamily = OutfitFont, fontWeight = FontWeight.Bold, fontSize = 26.sp, lineHeight = 31.sp, color = Color.Black)
        Spacer(Modifier.height(8.dp))
        Text(p.precioTexto() + " MXN", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, fontSize = 26.sp, color = PdVerde)

        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.StarBorder, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text(
                (p.calificacion?.let { String.format(java.util.Locale.US, "%.1f", it) } ?: "Nueva") + " · " + p.ventasTexto(),
                fontFamily = JakartaSansFont, fontSize = 13.sp, color = Color.Black
            )
        }

        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.clip(CircleShape).background(Color(0xFFD3C5CD)).padding(horizontal = 12.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(p.vendedoraAvatar ?: "🌸", fontSize = 14.sp)
            Spacer(Modifier.width(6.dp))
            Text("Vendedora: ${p.vendedora ?: "—"}", fontFamily = JakartaSansFont, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = PdVino)
        }

        Spacer(Modifier.height(18.dp))
        Text("Descripción", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
        Spacer(Modifier.height(6.dp))
        Text(
            p.descripcion?.takeIf { it.isNotBlank() } ?: "La vendedora no agregó descripción.",
            fontFamily = JakartaSansFont, fontSize = 15.sp, lineHeight = 22.sp, color = Color(0xFF333333)
        )

        Spacer(Modifier.height(18.dp))
        Column(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Color(0xFFF2F0EF)).padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Fila(Icons.Outlined.LocalShipping, if (p.tipo == "servicio") "Modalidad" else "Entrega", p.entregaPublicaTexto())
            p.zonaPublica?.takeIf { it.isNotBlank() }?.let { Fila(Icons.Outlined.Place, "Zona", it) }
            if (p.porEncargo) {
                Fila(Icons.Outlined.Schedule, "Tiempo de elaboración", "${p.tiempoElaboracionHoras ?: 24} horas de anticipación")
            }
            if (p.tipo == "servicio") {
                Fila(Icons.Outlined.Schedule, "Cobro", when (p.esquemaCobro) { 1 -> "Por hora"; 2 -> "Servicio fijo"; else -> "Por diseño / trabajo" })
            }
        }

        Spacer(Modifier.height(10.dp))
        Text(
            "Por tu seguridad, la dirección exacta y el contacto no se muestran: todo se coordina dentro de la app.",
            fontFamily = JakartaSansFont, fontSize = 12.sp, lineHeight = 17.sp, color = PdGris
        )

        Spacer(Modifier.height(18.dp))
        if (p.esMia) {
            OutlinedButton(onClick = onEditar, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().height(48.dp)) {
                Text("Esta es tu publicación · Editar detalles", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, color = ColorBotonEntrar)
            }
        } else {
            Button(
                onClick = onComprar,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PdVerde),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text(
                    if (p.tipo == "servicio") "Contratar" else if (p.porEncargo) "Hacer encargo" else "Comprar",
                    fontFamily = OutfitFont, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFFFFFAFA)
                )
            }
        }
        Spacer(Modifier.height(28.dp))
    }
}

@Composable
private fun Fila(icono: androidx.compose.ui.graphics.vector.ImageVector, titulo: String, valor: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(icono, contentDescription = null, tint = PdGris, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Column {
            Text(titulo, fontFamily = JakartaSansFont, fontSize = 11.sp, color = PdGris)
            Text(valor, fontFamily = OutfitFont, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = Color.Black)
        }
    }
}
