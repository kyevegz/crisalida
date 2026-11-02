package com.example.crisalida.ui.theme.screens

import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import com.example.crisalida.data.MiniaturaLocal
import com.example.crisalida.data.PublicacionRequest
import com.example.crisalida.data.PublicacionesRepositorio
import com.example.crisalida.data.ResultadoApi
import com.example.crisalida.data.prepararFoto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.LocalMall
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crisalida.ui.theme.*
import com.example.crisalida.ui.screens.*
/* ──────────────────────────────────────────────────────────────
 * Colores medidos del mockup Subir Producto
 * ────────────────────────────────────────────────────────────── */
private val MkTitulo = Color(0xFF7F284F)
private val MkSubtitulo = Color(0xFF545454)
private val MkFondoBotonVolver = Color(0xFFE2D4DA)
private val MkFondoAlertaRosa = Color(0xFFF4E4EC)
private val MkVerdeSugeridoFondo = Color(0xFFD4EDDA)
private val MkVerdeSugeridoTexto = Color(0xFF155724)
private val MkBotonPublicar = Color(0xFF7A2E50)
private val MkFondoCard = Color(0xFFFFFFFF)

@Composable
fun PublicarProductoScreen(
    onNavigate: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    // 0: Producto físico, 1: Servicio local
    var tipoPublicacion by remember { mutableIntStateOf(0) }

    // Listas oficiales de categorías
    val categoriasProductos = listOf(
        "Manualidades y Regalos", "Bisutería y Accesorios", "Belleza y Cosmética Natural",
        "Jardinería y Plantas", "Papelería y Cuadernos Artesanales", "Juguetes y Muñecos Hechos a Mano",
        "Velas y Aromaterapia", "Panadería y Galletas", "Conservas y Salsas", "Bordados y Tejidos"
    )

    val categoriasServicios = listOf(
        "Cuidado de Mascotas", "Organización de Eventos", "Cuidado Infantil",
        "Cuidado de Adultos Mayores", "Fotografía y Edición", "Diseño Gráfico Básico",
        "Corte y Confección", "Limpieza y Mantenimiento", "Clases y Tutorías", "Traducción y Transcripción"
    )

    val categoriasActuales = if (tipoPublicacion == 0) categoriasProductos else categoriasServicios

    // NINGUNA categoría seleccionada por defecto (inicia en null)
    var categoriaSeleccionada by remember { mutableStateOf<String?>(null) }

    // Al cambiar de tipo, limpiamos la categoría seleccionada
    LaunchedEffect(tipoPublicacion) {
        categoriaSeleccionada = null
    }

    // Campos de formulario limpios
    var tituloDescriptivo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var costoInsumos by remember { mutableStateOf("") }
    var horasDedicacion by remember { mutableStateOf("") }
    var precioFinal by remember { mutableStateOf("") }

    // Cálculo automático en tiempo real basado en insumos + $75/hr por hora de dedicación
    LaunchedEffect(costoInsumos, horasDedicacion) {
        val insumos = costoInsumos.toDoubleOrNull() ?: 0.0
        val horas = horasDedicacion.toDoubleOrNull() ?: 0.0
        if (insumos > 0 || horas > 0) {
            val sugerido = insumos + (horas * 75.0)
            precioFinal = String.format("%.2f", sugerido)
        }
    }

    // Modalidad de Servicio (Si es Servicio Local)
    var modalidadServicio by remember { mutableIntStateOf(1) } // 1: En casa de vendedora, 2: A domicilio de compradora
    var tipoCobroServicio by remember { mutableIntStateOf(1) } // 1: Por hora, 2: Por servicio fijo, 3: Por diseño / trabajo

    // Opción de entrega (Si es Producto Físico)
    var opcionEntrega by remember { mutableIntStateOf(1) } // 1: Domicilio, 2: Guía ciega, 3: Encargo previo

    // Galería de imágenes
    var imagenesSeleccionadas = remember { mutableStateListOf<Uri>() }
    val launcherGaleria = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null && imagenesSeleccionadas.size < 3) {
            imagenesSeleccionadas.add(uri)
        }
    }

    var mostrarDialogoBorrador by remember { mutableStateOf(false) }
    var mostrarDialogoVistaPrevia by remember { mutableStateOf(false) }

    // Zona pública (lo único que ven las compradoras) y punto de entrega real (privado, se guarda cifrado)
    var zonaPublica by remember { mutableStateOf("") }
    var puntoEntrega by remember { mutableStateOf("") }

    // Publicación
    val contexto = LocalContext.current
    val scope = rememberCoroutineScope()
    var publicando by remember { mutableStateOf(false) }
    var mensajePublicar by remember { mutableStateOf<String?>(null) }
    var publicacionCreada by remember { mutableStateOf(false) }

    fun publicar() {
        if (publicando) return
        val categoria = categoriaSeleccionada
        val precio = precioFinal.replace(",", ".").toDoubleOrNull()
        if (categoria == null) { mensajePublicar = "Elige una categoría para tu publicación."; return }
        if (tituloDescriptivo.trim().length < 3) { mensajePublicar = "Escribe un título para tu publicación."; return }
        if (precio == null || precio <= 0.0) { mensajePublicar = "Escribe un precio mayor a cero."; return }
        val esProducto = tipoPublicacion == 0
        val peticion = PublicacionRequest(
            tipo = if (esProducto) "producto" else "servicio",
            categoria = categoria,
            titulo = tituloDescriptivo.trim(),
            descripcion = descripcion.trim(),
            precio = precio,
            notaPrecio = if (esProducto) null else when (tipoCobroServicio) { 1 -> "Por hora"; 2 -> "Por servicio"; else -> "Por diseño" },
            opcionEntrega = if (esProducto) opcionEntrega else null,
            modalidadServicio = if (esProducto) null else modalidadServicio,
            esquemaCobro = if (esProducto) null else tipoCobroServicio,
            costoInsumos = costoInsumos.replace(",", ".").toDoubleOrNull(),
            horasDedicacion = horasDedicacion.replace(",", ".").toDoubleOrNull()?.takeIf { it > 0.0 },
            tiempoElaboracionHoras = if (esProducto && opcionEntrega == 3) 48 else null,
            zonaPublica = zonaPublica.trim().ifBlank { null },
            puntoEntrega = puntoEntrega.trim().ifBlank { null }
        )
        publicando = true
        scope.launch {
            when (val r = PublicacionesRepositorio.publicar(peticion)) {
                is ResultadoApi.Error -> mensajePublicar = r.mensaje
                is ResultadoApi.Exito -> {
                    // Con la publicación creada, se suben las fotos (comprimidas, sin metadatos)
                    var fallidas = 0
                    for (uri in imagenesSeleccionadas.toList()) {
                        val bytes = withContext(Dispatchers.IO) { prepararFoto(contexto, uri) }
                        val subida = if (bytes == null) null else PublicacionesRepositorio.subirFoto(r.datos.id, bytes)
                        if (subida !is ResultadoApi.Exito) fallidas++
                    }
                    publicacionCreada = true
                    mensajePublicar = buildString {
                        append(if (r.datos.estado == "activo") "¡Tu publicación ya está visible en el marketplace!"
                               else "¡Listo! Tu publicación quedó en revisión; en cuanto la aprobemos se verá en el marketplace.")
                        if (r.datos.floresGanadas > 0) append("\n\n🌸 Ganaste ${r.datos.floresGanadas} flores para Lilu.")
                        if (fallidas > 0) append("\n\nNo se pudieron subir $fallidas foto(s). Inténtalo de nuevo más tarde.")
                    }
                }
            }
            publicando = false
        }
    }

    CrisalidaScaffold(
        currentTitle = "Subir producto",
        selectedTab = 1,
        onNavigate = onNavigate
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ColorFondoApp)
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            // Botón superior izquierdo "Mi catálogo"
            Surface(
                onClick = { onNavigate("portafolio_completo") },
                shape = RoundedCornerShape(18.dp),
                color = MkFondoBotonVolver,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = MkTitulo, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Mi catálogo", fontFamily = OutfitFont, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MkTitulo)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(text = "Marketplace", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, fontSize = 32.sp, color = MkTitulo)
            Text(text = "Publicar en Mercado Seguro", fontFamily = JakartaSansFont, fontSize = 13.sp, color = MkSubtitulo)

            Spacer(modifier = Modifier.height(16.dp))

            // Alerta de Identidad
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MkFondoAlertaRosa),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(36.dp).clip(CircleShape).background(MkTitulo.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🛡️", fontSize = 18.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Vendes con tu alias de protección", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MkTitulo)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = "Recuerda que si te encuentras en situación de riesgo, tu nombre legal, ubicación y teléfono jamás serán expuestos.", fontFamily = JakartaSansFont, fontSize = 11.5.sp, lineHeight = 15.sp, color = MkSubtitulo)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pestañas: Producto Físico vs Servicio Local
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TabTipoPublicacion(
                    titulo = "Producto físico",
                    icono = Icons.Outlined.Storefront,
                    seleccionado = tipoPublicacion == 0,
                    modifier = Modifier.weight(1f),
                    onClick = { tipoPublicacion = 0 }
                )
                TabTipoPublicacion(
                    titulo = "Servicio local",
                    icono = Icons.Outlined.LocalMall,
                    seleccionado = tipoPublicacion == 1,
                    modifier = Modifier.weight(1f),
                    onClick = { tipoPublicacion = 1 }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ========================================================
            // BLOQUE 1: INFORMACIÓN GENERAL Y CATEGORÍAS
            // ========================================================
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MkFondoCard),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1. Información del ${if (tipoPublicacion == 0) "Producto" else "Servicio"}",
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MkTitulo
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(text = "Título descriptivo", fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MkTitulo)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = tituloDescriptivo,
                        onValueChange = { tituloDescriptivo = it },
                        placeholder = { Text(if (tipoPublicacion == 0) "Ej. Mermelada artesanal" else "Ej. Maquillaje a domicilio o Uñas acrílicas", fontFamily = JakartaSansFont, fontSize = 13.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color(0xFFFAF8F8), unfocusedContainerColor = Color(0xFFFAF8F8), focusedBorderColor = MkTitulo, unfocusedBorderColor = ColorCamposFondo),
                        textStyle = TextStyle(fontFamily = JakartaSansFont, fontSize = 14.sp, color = ColorTextoPrincipal)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(text = "Categoría (Selecciona una)", fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MkTitulo)
                    Spacer(modifier = Modifier.height(6.dp))

                    // Categorías dinámicas SIN selección por defecto
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        categoriasActuales.chunked(2).forEach { fila ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                fila.forEach { cat ->
                                    val isSelected = categoriaSeleccionada == cat
                                    Surface(
                                        onClick = { categoriaSeleccionada = cat },
                                        shape = RoundedCornerShape(16.dp),
                                        color = if (isSelected) MkTitulo else ColorCamposFondo.copy(alpha = 0.5f),
                                        modifier = Modifier.weight(1f).height(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp)) {
                                            Text(
                                                text = cat,
                                                fontFamily = JakartaSansFont,
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) Color.White else ColorTextoPrincipal,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                                if (fila.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(text = "Fotografías (hasta 3)", fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MkTitulo)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        imagenesSeleccionadas.toList().forEach { uri ->
                            Box(modifier = Modifier.size(90.dp).clip(RoundedCornerShape(8.dp)).background(Color.LightGray)) {
                                MiniaturaLocal(uri = uri, modifier = Modifier.fillMaxSize())
                                // Toca la ✕ para quitar la foto
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(3.dp)
                                        .size(22.dp)
                                        .clip(RoundedCornerShape(11.dp))
                                        .background(Color(0xAA000000))
                                        .clickable { imagenesSeleccionadas.remove(uri) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "✕", fontSize = 11.sp, color = Color.White)
                                }
                            }
                        }
                        if (imagenesSeleccionadas.size < 3) {
                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, MkTitulo.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFAF8F8))
                                    .clickable { launcherGaleria.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = "Agregar", tint = MkTitulo, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(text = "+ Agregar", fontFamily = JakartaSansFont, fontSize = 11.sp, color = MkTitulo, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(text = "Descripción", fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MkTitulo)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it },
                        placeholder = { Text("Detalla los materiales, duración o lo que incluye tu servicio...", fontFamily = JakartaSansFont, fontSize = 13.sp) },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color(0xFFFAF8F8), unfocusedContainerColor = Color(0xFFFAF8F8), focusedBorderColor = MkTitulo, unfocusedBorderColor = ColorCamposFondo),
                        textStyle = TextStyle(fontFamily = JakartaSansFont, fontSize = 14.sp, color = ColorTextoPrincipal)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ========================================================
            // BLOQUE 2: COSTOS Y TARIFA DINÁMICA
            // ========================================================
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MkFondoCard),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "2. Costos y Tarifas",
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MkTitulo
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    // Si es servicio local, permitimos elegir cómo cobra
                    if (tipoPublicacion == 1) {
                        Text(text = "Esquema de cobro del servicio", fontFamily = JakartaSansFont, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MkTitulo)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            BotonEsquemaCobro("Por hora", tipoCobroServicio == 1, Modifier.weight(1f)) { tipoCobroServicio = 1 }
                            BotonEsquemaCobro("Servicio fijo", tipoCobroServicio == 2, Modifier.weight(1f)) { tipoCobroServicio = 2 }
                            BotonEsquemaCobro("Por diseño", tipoCobroServicio == 3, Modifier.weight(1f)) { tipoCobroServicio = 3 }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = if (tipoPublicacion == 0) "Costo insumos" else "Materiales / Insumos", fontFamily = JakartaSansFont, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MkTitulo)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = costoInsumos,
                                onValueChange = { costoInsumos = it },
                                placeholder = { Text("\$ 0.00", fontFamily = JakartaSansFont, fontSize = 13.sp) },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color(0xFFFAF8F8), unfocusedContainerColor = Color(0xFFFAF8F8)),
                                textStyle = TextStyle(fontFamily = JakartaSansFont, fontSize = 14.sp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Dedicación (Horas)", fontFamily = JakartaSansFont, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MkTitulo)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = horasDedicacion,
                                onValueChange = { horasDedicacion = it },
                                placeholder = { Text("Ej. 1.5", fontFamily = JakartaSansFont, fontSize = 13.sp) },
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color(0xFFFAF8F8), unfocusedContainerColor = Color(0xFFFAF8F8)),
                                textStyle = TextStyle(fontFamily = JakartaSansFont, fontSize = 14.sp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Caja verde con Rango Justo Sugerido de IA
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(MkVerdeSugeridoFondo).padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "🌱 Rango Justo Sugerido (IA)", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MkVerdeSugeridoTexto)
                            Text(text = "Cubre insumos + \$75/hr base digna", fontFamily = JakartaSansFont, fontSize = 11.sp, color = MkVerdeSugeridoTexto)
                        }
                        Text(text = "Estimado automático", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MkVerdeSugeridoTexto)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(text = "Precio final (Calculado o editable)", fontFamily = JakartaSansFont, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MkTitulo)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = precioFinal,
                        onValueChange = { precioFinal = it },
                        placeholder = { Text("\$ 0.00 MXN", fontFamily = JakartaSansFont, fontSize = 14.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color(0xFFFAF8F8), unfocusedContainerColor = Color(0xFFFAF8F8)),
                        textStyle = TextStyle(fontFamily = OutfitFont, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MkTitulo)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ========================================================
            // BLOQUE 3: MODALIDAD DE ATENCIÓN / ENTREGA SEGURA
            // ========================================================
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MkFondoCard),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (tipoPublicacion == 0) "3. Entrega Neutral & Segura" else "3. Modalidad de Atención Segura",
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MkTitulo
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (tipoPublicacion == 0)
                            "Protege tu hogar. Selecciona cómo harás llegar el producto a tu compradora sin exponerte:"
                        else
                            "Define el entorno seguro donde se llevará a cabo el servicio:",
                        fontFamily = JakartaSansFont,
                        fontSize = 12.sp,
                        color = MkSubtitulo
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    if (tipoPublicacion == 0) {
                        // Opciones para Producto Físico
                        OpcionEntregaItem("Recoger en domicilio", "Las compradoras pasarán al domicilio registrado.", "A domicilio", opcionEntrega == 1) { opcionEntrega = 1 }
                        Spacer(modifier = Modifier.height(10.dp))
                        OpcionEntregaItem("Guía Ciega Prepago (Paquetería)", "Etiqueta generada automáticamente con remitente anónimo a nombre de Crisálida.", "Recomendado", opcionEntrega == 2) { opcionEntrega = 2 }
                        Spacer(modifier = Modifier.height(10.dp))
                        OpcionEntregaItem("Bajo encargo previo (Tiempo artesanal)", "El cliente paga en custodia; dispones de hasta 48 hrs para preparar sin prisas.", "24h - 48h", opcionEntrega == 3) { opcionEntrega = 3 }
                    } else {
                        // Opciones adaptadas para Servicio Local (Uñas, maquillaje, costura, etc.)
                        OpcionEntregaItem("Atención en casa de la vendedora", "La clienta acude a tu espacio de trabajo seguro.", "Espacio propio", modalidadServicio == 1) { modalidadServicio = 1 }
                        Spacer(modifier = Modifier.height(10.dp))
                        OpcionEntregaItem("Servicio a domicilio de la compradora", "Tú acudes al domicilio de la clienta (ej. maquillaje o peinado para eventos).", "A domicilio", modalidadServicio == 2) { modalidadServicio = 2 }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Zona pública (la verán las compradoras)", fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MkTitulo)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = zonaPublica,
                        onValueChange = { if (it.length <= 80) zonaPublica = it },
                        placeholder = { Text("Ej. Centro de Irapuato (solo colonia o zona)", fontFamily = JakartaSansFont, fontSize = 13.sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color(0xFFFAF8F8), unfocusedContainerColor = Color(0xFFFAF8F8), focusedBorderColor = MkTitulo, unfocusedBorderColor = ColorCamposFondo),
                        textStyle = TextStyle(fontFamily = JakartaSansFont, fontSize = 14.sp, color = ColorTextoPrincipal)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = "Punto de entrega o dirección (solo tú la ves)", fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MkTitulo)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = puntoEntrega,
                        onValueChange = { if (it.length <= 200) puntoEntrega = it },
                        placeholder = { Text("Opcional. Se guarda cifrada y nunca se muestra al público", fontFamily = JakartaSansFont, fontSize = 13.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color(0xFFFAF8F8), unfocusedContainerColor = Color(0xFFFAF8F8), focusedBorderColor = MkTitulo, unfocusedBorderColor = ColorCamposFondo),
                        textStyle = TextStyle(fontFamily = JakartaSansFont, fontSize = 14.sp, color = ColorTextoPrincipal)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón Publicar
            Button(
                onClick = { publicar() },
                enabled = !publicando,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MkBotonPublicar)
            ) {
                Text(text = if (publicando) "Publicando…" else if (tipoPublicacion == 0) "Publicar Producto" else "Publicar Servicio", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = { mostrarDialogoBorrador = true },
                    modifier = Modifier.weight(1f).height(42.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Outlined.BookmarkBorder, contentDescription = null, tint = MkTitulo, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Guardar Borrador", fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MkTitulo)
                }

                OutlinedButton(
                    onClick = { mostrarDialogoVistaPrevia = true },
                    modifier = Modifier.weight(1f).height(42.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Outlined.Visibility, contentDescription = null, tint = MkTitulo, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Vista Previa", fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MkTitulo)
                }
            }

            Spacer(modifier = Modifier.height(36.dp))
        }
    }

    if (mostrarDialogoBorrador) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoBorrador = false },
            title = { Text("Borradores próximamente", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, color = MkTitulo) },
            text = { Text("Por ahora los borradores no se pueden guardar. Pronto podrás hacerlo; mientras tanto, publica cuando estés lista.", fontFamily = JakartaSansFont, color = MkSubtitulo) },
            confirmButton = { Button(onClick = { mostrarDialogoBorrador = false }, colors = ButtonDefaults.buttonColors(containerColor = MkTitulo)) { Text("Aceptar", color = Color.White) } },
            containerColor = Color.White, shape = RoundedCornerShape(12.dp)
        )
    }

    mensajePublicar?.let { texto ->
        AlertDialog(
            onDismissRequest = { mensajePublicar = null; if (publicacionCreada) onNavigate("portafolio_completo") },
            text = { Text(texto, fontFamily = JakartaSansFont, color = MkSubtitulo) },
            confirmButton = {
                Button(
                    onClick = { mensajePublicar = null; if (publicacionCreada) onNavigate("portafolio_completo") },
                    colors = ButtonDefaults.buttonColors(containerColor = MkTitulo)
                ) { Text(if (publicacionCreada) "Ver mi catálogo" else "Entendido", color = Color.White) }
            },
            containerColor = Color.White, shape = RoundedCornerShape(12.dp)
        )
    }

    if (mostrarDialogoVistaPrevia) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoVistaPrevia = false },
            title = { Text("Vista previa", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, color = MkTitulo) },
            text = { Text("Así verán tu publicación en el Marketplace con tu alias protegido.", fontFamily = JakartaSansFont, color = MkSubtitulo) },
            confirmButton = { Button(onClick = { mostrarDialogoVistaPrevia = false }, colors = ButtonDefaults.buttonColors(containerColor = MkTitulo)) { Text("Cerrar", color = Color.White) } },
            containerColor = Color.White, shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
private fun TabTipoPublicacion(
    titulo: String,
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    seleccionado: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (seleccionado) Color.White else Color.Transparent,
        border = if (!seleccionado) BorderStroke(1.dp, Color(0xFFD3C5CD)) else null,
        shadowElevation = if (seleccionado) 2.dp else 0.dp,
        modifier = modifier.height(44.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icono, contentDescription = null, tint = if (seleccionado) MkTitulo else MkSubtitulo, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = titulo, fontFamily = OutfitFont, fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Medium, fontSize = 13.sp, color = if (seleccionado) MkTitulo else MkSubtitulo)
        }
    }
}

@Composable
private fun BotonEsquemaCobro(titulo: String, seleccionado: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (seleccionado) MkTitulo else Color(0xFFFAF8F8),
        border = BorderStroke(1.dp, if (seleccionado) MkTitulo else Color(0xFFE2D4DA)),
        modifier = modifier.height(34.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(text = titulo, fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = if (seleccionado) Color.White else MkTitulo)
        }
    }
}

@Composable
private fun OpcionEntregaItem(titulo: String, subtitulo: String, badge: String, seleccionada: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (seleccionada) Color(0xFFFAF0F4) else Color(0xFFFAF8F8),
        border = BorderStroke(1.dp, if (seleccionada) MkTitulo else Color(0xFFE2D4DA)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
            RadioButton(selected = seleccionada, onClick = onClick, colors = RadioButtonDefaults.colors(selectedColor = MkTitulo))
            Spacer(modifier = Modifier.width(6.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = titulo, fontFamily = OutfitFont, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MkTitulo)
                    Surface(shape = CircleShape, color = MkTitulo.copy(alpha = 0.1f)) {
                        Text(text = badge, fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = MkTitulo, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = subtitulo, fontFamily = JakartaSansFont, fontSize = 11.5.sp, lineHeight = 15.sp, color = MkSubtitulo)
            }
        }
    }
}