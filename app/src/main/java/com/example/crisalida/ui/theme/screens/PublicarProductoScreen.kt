package com.example.crisalida.ui.theme.screens

import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import org.json.JSONObject
import androidx.core.content.ContextCompat
import com.example.crisalida.data.AlmacenamientoCifrado
import com.example.crisalida.data.Borradores
import com.example.crisalida.data.FormulariosAbiertos
import com.example.crisalida.data.ColaEnvios
import com.example.crisalida.data.FotoRemota
import com.example.crisalida.data.FotosCamara
import com.example.crisalida.data.Haptica
import com.example.crisalida.data.MarketplaceRepositorio
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.LocalMall
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crisalida.ui.theme.*
import com.example.crisalida.ui.theme.screens.CrisalidaScaffold
import com.example.crisalida.ui.theme.Paleta
private val MkTitulo: Color get() = Paleta.titulo
private val MkSubtitulo: Color get() = Paleta.textoGris
private val MkFondoBotonVolver: Color get() = Paleta.fondoBotonVolver
private val MkFondoAlertaRosa: Color get() = Paleta.fondoAlertaRosa
private val MkVerdeSugeridoFondo: Color get() = Paleta.verdeSuaveFondo
private val MkVerdeSugeridoTexto: Color get() = Paleta.verdeSuaveTexto
private val MkBotonPublicar: Color get() = Paleta.botonVino
private val MkFondoCard: Color get() = Paleta.superficie

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PublicarProductoScreen(
    onNavigate: (String) -> Unit,
    publicacionId: String? = null,
    onEditado: (() -> Unit)? = null
) {
    val editando = publicacionId != null
    val scrollState = rememberScrollState()

    var tipoPublicacion by remember { mutableIntStateOf(0) }

    val categoriasCarga = rememberCarga(tipoPublicacion) {
        MarketplaceRepositorio.categorias(if (tipoPublicacion == 0) "producto" else "servicio")
    }
    val categoriasActuales = (categoriasCarga.estado as? Carga.Lista)?.datos?.categorias?.map { it.nombre } ?: emptyList()

    var categoriaSeleccionada by remember { mutableStateOf<String?>(null) }

    var tituloDescriptivo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var costoInsumos by remember { mutableStateOf("") }
    var horasDedicacion by remember { mutableStateOf("") }
    var precioFinal by remember { mutableStateOf("") }

    var saltarSugerencia by remember { mutableStateOf(false) }
    LaunchedEffect(costoInsumos, horasDedicacion) {
        if (saltarSugerencia) { saltarSugerencia = false; return@LaunchedEffect }
        val insumos = costoInsumos.toDoubleOrNull() ?: 0.0
        val horas = horasDedicacion.toDoubleOrNull() ?: 0.0
        if (insumos > 0 || horas > 0) {
            val sugerido = insumos + (horas * 75.0)
            precioFinal = String.format("%.2f", sugerido)
        }
    }

    var modalidadServicio by remember { mutableIntStateOf(1) }
    var tipoCobroServicio by remember { mutableIntStateOf(1) }

    var opcionEntrega by remember { mutableIntStateOf(1) }

    val imagenesSeleccionadas = remember { mutableStateListOf<Uri>() }
    val fotosExistentes = remember { mutableStateListOf<String>() }
    val launcherGaleria = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null && imagenesSeleccionadas.size + fotosExistentes.size < 3) {
            imagenesSeleccionadas.add(uri)
        }
    }

    var mostrarDialogoVistaPrevia by remember { mutableStateOf(false) }

    var zonaPublica by remember { mutableStateOf("") }
    var puntoEntrega by remember { mutableStateOf("") }

    val contexto = LocalContext.current
    val scope = rememberCoroutineScope()

    // ---- fotos con la cámara: ver FotosCamara (archivo temporal privado) ----
    var mostrarOpcionesFoto by remember { mutableStateOf(false) }
    var mostrarExplicacionCamara by remember { mutableStateOf(false) }
    var mensajeCamara by remember { mutableStateOf<String?>(null) }
    // se guarda aunque Android recree la pantalla mientras la app de cámara está abierta
    var fotoPendiente by rememberSaveable { mutableStateOf<Uri?>(null) }

    val launcherCamara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { exito ->
        val uri = fotoPendiente
        fotoPendiente = null
        if (uri != null) {
            if (exito && imagenesSeleccionadas.size + fotosExistentes.size < 3) {
                imagenesSeleccionadas.add(uri)      // se trata igual que una foto de la galería
            } else {
                FotosCamara.borrar(contexto, uri)   // canceló: no se agrega nada ni queda ningún archivo
            }
        }
    }

    fun abrirCamara() {
        try {
            val uri = FotosCamara.crearArchivoTemporal(contexto)
            fotoPendiente = uri
            launcherCamara.launch(uri)
        } catch (e: Exception) {
            FotosCamara.borrar(contexto, fotoPendiente)
            fotoPendiente = null
            mensajeCamara = "No pudimos abrir la cámara en este momento. Puedes elegir una foto de tu galería."
        }
    }

    val launcherPermisoCamara = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (concedido) abrirCamara()
        else mensajeCamara = "No hay problema, tu publicación puede seguir con fotos de tu galería. " +
                "Si más adelante quieres usar la cámara, puedes activar el permiso en los ajustes del teléfono."
    }

    fun alElegirTomarFoto() {
        mostrarOpcionesFoto = false
        val permiso = ContextCompat.checkSelfPermission(contexto, Manifest.permission.CAMERA)
        if (permiso == PackageManager.PERMISSION_GRANTED) abrirCamara() else mostrarExplicacionCamara = true
    }

    // las fotos temporales de la cámara se borran al salir de la pantalla (y se limpian restos de una vez anterior)
    DisposableEffect(Unit) {
        FotosCamara.limpiarTodo(contexto)
        onDispose { FotosCamara.limpiarTodo(contexto) }
    }

    var publicando by remember { mutableStateOf(false) }
    var mensajePublicar by remember { mutableStateOf<String?>(null) }
    var publicacionCreada by remember { mutableStateOf(false) }

    // el borrador se guarda solo en este teléfono, CIFRADO (Keystore), sin fotos y sin la dirección privada
    var borradorRecuperado by remember { mutableStateOf(false) }
    var borradorId by remember { mutableStateOf<String?>(null) }
    var borradoresGuardados by remember { mutableStateOf<List<Borradores.Borrador>>(emptyList()) }
    var mostrarBorradores by remember { mutableStateOf(false) }
    var mensajeBorrador by remember { mutableStateOf<String?>(null) }

    fun limpiarFormulario() {
        tituloDescriptivo = ""; descripcion = ""; costoInsumos = ""; horasDedicacion = ""; precioFinal = ""
        zonaPublica = ""; categoriaSeleccionada = null; modalidadServicio = 1; tipoCobroServicio = 1; opcionEntrega = 1
    }

    fun datosBorrador(): String = JSONObject()
        .put("tipo", tipoPublicacion).put("categoria", categoriaSeleccionada ?: "")
        .put("titulo", tituloDescriptivo).put("descripcion", descripcion)
        .put("insumos", costoInsumos).put("horas", horasDedicacion).put("precio", precioFinal)
        .put("modalidad", modalidadServicio).put("cobro", tipoCobroServicio).put("entrega", opcionEntrega)
        .put("zona", zonaPublica).toString()

    fun guardarBorrador() {
        if (tituloDescriptivo.isBlank() && descripcion.isBlank() && precioFinal.isBlank()) {
            mensajeBorrador = "Escribe al menos el título o la descripción para guardar un borrador."
            return
        }
        if (borradorId == null && Borradores.lleno(contexto)) {
            mensajeBorrador = "Ya tienes ${Borradores.MAXIMO} borradores. Elimina alguno para guardar uno nuevo."
            return
        }
        val id = Borradores.guardar(contexto, borradorId, datosBorrador())
        if (id == null) {
            mensajeBorrador = "No pudimos guardar el borrador en este teléfono. Intenta de nuevo."
            return
        }
        borradorId = id
        borradoresGuardados = Borradores.listar(contexto)
        mensajeBorrador = "Borrador guardado, cifrado, en este teléfono. Puedes tener varios y abrirlos desde «Mis borradores». " +
                "No guarda las fotos ni tu dirección privada: esas hay que volver a agregarlas."
    }

    // si la sesión se cierra por inactividad con el formulario a medias, se guarda como borrador antes de cerrar
    val guardarPorInactividad = remember<() -> Unit> {
        {
            val hayContenido = tituloDescriptivo.isNotBlank() || descripcion.isNotBlank() || precioFinal.isNotBlank()
            if (!editando && !publicacionCreada && hayContenido && (borradorId != null || !Borradores.lleno(contexto))) {
                Borradores.guardar(contexto, borradorId, datosBorrador())?.let { Borradores.marcarInterrumpido(contexto, it) }
            }
        }
    }
    DisposableEffect(Unit) {
        FormulariosAbiertos.registrar(guardarPorInactividad)
        onDispose { FormulariosAbiertos.quitar(guardarPorInactividad) }
    }

    fun abrirBorrador(b: Borradores.Borrador) {
        try {
            val j = JSONObject(b.datos)
            tipoPublicacion = j.optInt("tipo", 0)
            categoriaSeleccionada = j.optString("categoria").ifBlank { null }
            tituloDescriptivo = j.optString("titulo"); descripcion = j.optString("descripcion")
            costoInsumos = j.optString("insumos"); horasDedicacion = j.optString("horas"); precioFinal = j.optString("precio")
            modalidadServicio = j.optInt("modalidad", 1); tipoCobroServicio = j.optInt("cobro", 1); opcionEntrega = j.optInt("entrega", 1)
            zonaPublica = j.optString("zona")
            saltarSugerencia = costoInsumos.isNotBlank() || horasDedicacion.isNotBlank()
            borradorId = b.id
            borradorRecuperado = true
        } catch (e: Exception) {
            Borradores.eliminar(contexto, b.id)
            borradoresGuardados = Borradores.listar(contexto)
            mensajeBorrador = "Ese borrador estaba dañado y se eliminó."
        }
    }

    LaunchedEffect(Unit) {
        if (!editando) {
            // pasa a la lista el borrador único de versiones anteriores sin perderlo, y lee todos
            borradoresGuardados = withContext(Dispatchers.IO) {
                Borradores.migrar(contexto)
                Borradores.listar(contexto)
            }
            // la usuaria eligió «Continuar» en Inicio tras volver a iniciar sesión
            Borradores.porAbrir?.let { id ->
                Borradores.porAbrir = null
                borradoresGuardados.firstOrNull { it.id == id }?.let { abrirBorrador(it) }
            }
        }
    }

    var cargandoEdicion by remember { mutableStateOf(editando) }
    LaunchedEffect(publicacionId) {
        if (publicacionId == null) return@LaunchedEffect
        when (val r = PublicacionesRepositorio.mias()) {
            is ResultadoApi.Error -> mensajePublicar = r.mensaje
            is ResultadoApi.Exito -> {
                val d = r.datos.publicaciones.firstOrNull { it.id == publicacionId }
                if (d == null) mensajePublicar = "No encontramos esa publicación."
                else {
                    tipoPublicacion = if (d.tipo == "producto") 0 else 1
                    categoriaSeleccionada = d.categoria
                    tituloDescriptivo = d.titulo
                    descripcion = d.descripcion.orEmpty()
                    costoInsumos = d.costoInsumos?.let { String.format(java.util.Locale.US, "%.2f", it) } ?: ""
                    horasDedicacion = d.horasDedicacion?.let { String.format(java.util.Locale.US, "%.2f", it) } ?: ""
                    precioFinal = String.format(java.util.Locale.US, "%.2f", d.precio)
                    modalidadServicio = d.modalidadServicio ?: 1
                    tipoCobroServicio = d.esquemaCobro ?: 1
                    opcionEntrega = d.opcionEntrega ?: 1
                    zonaPublica = d.zonaPublica.orEmpty()
                    puntoEntrega = d.puntoEntrega.orEmpty()
                    saltarSugerencia = costoInsumos.isNotBlank() || horasDedicacion.isNotBlank()
                    fotosExistentes.clear(); fotosExistentes.addAll(d.imagenes)
                }
            }
        }
        cargandoEdicion = false
    }

    fun publicar() {
        if (publicando) return
        val categoria = categoriaSeleccionada
        val precio = precioFinal.replace(",", ".").toDoubleOrNull()
        if (categoria == null) { mensajePublicar = "Elige una categoría para tu publicación."; Haptica.error(); return }
        if (tituloDescriptivo.trim().length < 3) { mensajePublicar = "Escribe un título para tu publicación."; Haptica.error(); return }
        if (precio == null || precio <= 0.0) { mensajePublicar = "Escribe un precio mayor a cero."; Haptica.error(); return }
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
        if (publicacionId != null) {
            publicando = true
            scope.launch {
                when (val r = PublicacionesRepositorio.editar(publicacionId, peticion)) {
                    is ResultadoApi.Error -> { mensajePublicar = r.mensaje; Haptica.error() }
                    is ResultadoApi.Exito -> {
                        var fallidas = 0
                        for (uri in imagenesSeleccionadas.toList()) {
                            val bytes = withContext(Dispatchers.IO) { prepararFoto(contexto, uri) }
                            val subida = if (bytes == null) null else PublicacionesRepositorio.subirFoto(publicacionId, bytes)
                            if (subida !is ResultadoApi.Exito) fallidas++
                        }
                        publicacionCreada = true
                        Haptica.exito()
                        mensajePublicar = buildString {
                            append("¡Cambios guardados!")
                            if (r.datos.reenviadaARevision) append("\n\nComo cambiaste los detalles, tu publicación vuelve a revisión y reaparecerá en el marketplace cuando se apruebe.")
                            if (fallidas > 0) append("\n\nNo se pudieron subir $fallidas foto(s). Inténtalo de nuevo.")
                        }
                    }
                }
                publicando = false
            }
            return
        }
        publicando = true
        scope.launch {
            // las fotos se comprimen ANTES de publicar: si no hay internet, se guardan cifradas junto con la publicación pendiente
            val preparadas = withContext(Dispatchers.IO) { imagenesSeleccionadas.toList().map { prepararFoto(contexto, it) } }
            val fotos = preparadas.filterNotNull()
            when (val r = PublicacionesRepositorio.publicar(peticion, fotos)) {
                is ResultadoApi.Error -> { mensajePublicar = r.mensaje; Haptica.error() }
                is ResultadoApi.Exito -> if (r.datos.estado == ColaEnvios.ESTADO_EN_COLA) {
                    // sin conexión: NO está publicada todavía y no se muestran flores hasta que el servidor confirme
                    publicacionCreada = true
                    Haptica.exito()
                    borradorId?.let { Borradores.eliminar(contexto, it) }
                    mensajePublicar = "Sin conexión: tu publicación quedó guardada en tu teléfono y se enviará sola cuando vuelva el internet. " +
                            "Puedes cancelarla desde tu Perfil. Las flores se sumarán cuando el servidor la confirme."
                } else {
                    //con la publicación creada, se suben las fotos comprimidas, sin metadatos
                    var fallidas = preparadas.size - fotos.size
                    for (bytes in fotos) {
                        val subida = PublicacionesRepositorio.subirFoto(r.datos.id, bytes)
                        if (subida !is ResultadoApi.Exito) fallidas++
                    }
                    publicacionCreada = true
                    Haptica.exito()
                    borradorId?.let { Borradores.eliminar(contexto, it) }
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
        currentTitle = if (editando) "Editar publicación" else "Subir producto",
        selectedTab = 1,
        onNavigate = onNavigate
    ) { innerPadding ->
        ContenidoCentrado(innerPadding) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Spacer(modifier = Modifier.height(14.dp))

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

                ConEscalaLimitada(tope = 1.3f) {
                    Text(text = "Marketplace", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, fontSize = 32.sp, color = MkTitulo)
                }
                Text(text = if (editando) "Editar los detalles de tu publicación" else "Publicar en Mercado Seguro", fontFamily = JakartaSansFont, fontSize = 13.sp, color = MkSubtitulo)
                if (cargandoEdicion) IndicadorCargando()
                if (borradorRecuperado) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MkVerdeSugeridoFondo),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("📝 Estás editando un borrador.", fontFamily = JakartaSansFont, fontSize = 13.sp, color = MkVerdeSugeridoTexto, modifier = Modifier.weight(1f))
                            TextButton(onClick = {
                                borradorId?.let { Borradores.eliminar(contexto, it) }
                                borradoresGuardados = Borradores.listar(contexto)
                                borradorId = null; limpiarFormulario(); borradorRecuperado = false
                            }) { Text("Descartar", fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MkVerdeSugeridoTexto) }
                        }
                    }
                }

                if (!editando && borradoresGuardados.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MkVerdeSugeridoFondo),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("📝 Mis borradores (${borradoresGuardados.size})", fontFamily = JakartaSansFont, fontSize = 13.sp, color = MkVerdeSugeridoTexto, modifier = Modifier.weight(1f))
                            TextButton(onClick = { mostrarBorradores = true }) {
                                Text("Ver", fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MkVerdeSugeridoTexto)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

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
                            Text(text = "Recuerda que si te encuentras en situación de riesgo, tu nombre legal, ubicación y teléfono jamás serán expuestos.", fontFamily = JakartaSansFont, fontSize = 12.sp, lineHeight = 17.sp, color = MkSubtitulo)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TabTipoPublicacion(
                        titulo = "Producto físico",
                        icono = Icons.Outlined.Storefront,
                        seleccionado = tipoPublicacion == 0,
                        modifier = Modifier.weight(1f),
                        onClick = { if (!editando && tipoPublicacion != 0) { tipoPublicacion = 0; categoriaSeleccionada = null } }
                    )
                    TabTipoPublicacion(
                        titulo = "Servicio local",
                        icono = Icons.Outlined.LocalMall,
                        seleccionado = tipoPublicacion == 1,
                        modifier = Modifier.weight(1f),
                        onClick = { if (!editando && tipoPublicacion != 1) { tipoPublicacion = 1; categoriaSeleccionada = null } }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                //información general y categorías
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
                            colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Paleta.tarjeta, unfocusedContainerColor = Paleta.tarjeta, focusedBorderColor = MkTitulo, unfocusedBorderColor = ColorCamposFondo),
                            textStyle = TextStyle(fontFamily = JakartaSansFont, fontSize = 14.sp, color = ColorTextoPrincipal)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(text = "Categoría (Selecciona una)", fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MkTitulo)
                        Spacer(modifier = Modifier.height(6.dp))

                        //categorías del servidor
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            categoriasActuales.chunked(2).forEach { fila ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(IntrinsicSize.Min),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    fila.forEach { cat ->
                                        val isSelected = categoriaSeleccionada == cat
                                        Surface(
                                            onClick = { categoriaSeleccionada = cat },
                                            shape = RoundedCornerShape(16.dp),
                                            color = if (isSelected) Paleta.tituloFijo else ColorCamposFondo.copy(alpha = 0.5f),
                                            modifier = Modifier.weight(1f).fillMaxHeight().heightIn(min = 36.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp)) {
                                                Text(
                                                    text = cat,
                                                    fontFamily = JakartaSansFont,
                                                    fontSize = 11.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected) Paleta.blanco else ColorTextoPrincipal,
                                                    maxLines = 2,
                                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
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

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            fotosExistentes.toList().forEach { ruta ->
                                Box(modifier = Modifier.size(90.dp).clip(RoundedCornerShape(8.dp)).background(Paleta.fondoNeutro)) {
                                    FotoRemota(ruta = ruta, modifier = Modifier.fillMaxSize())
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(3.dp)
                                            .size(22.dp)
                                            .clip(RoundedCornerShape(11.dp))
                                            .background(Paleta.velo)
                                            .clickable {
                                                scope.launch {
                                                    when (val r = PublicacionesRepositorio.borrarFoto(publicacionId.orEmpty(), ruta)) {
                                                        is ResultadoApi.Exito -> fotosExistentes.remove(ruta)
                                                        is ResultadoApi.Error -> mensajePublicar = r.mensaje
                                                    }
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = "✕", fontSize = 12.sp, color = Paleta.blanco)
                                    }
                                }
                            }
                            imagenesSeleccionadas.toList().forEach { uri ->
                                Box(modifier = Modifier.size(90.dp).clip(RoundedCornerShape(8.dp)).background(Paleta.fondoNeutro)) {
                                    MiniaturaLocal(uri = uri, modifier = Modifier.fillMaxSize())
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(3.dp)
                                            .size(22.dp)
                                            .clip(RoundedCornerShape(11.dp))
                                            .background(Paleta.velo)
                                            .clickable { imagenesSeleccionadas.remove(uri); FotosCamara.borrar(contexto, uri) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = "✕", fontSize = 12.sp, color = Paleta.blanco)
                                    }
                                }
                            }
                            if (imagenesSeleccionadas.size + fotosExistentes.size < 3) {
                                Box(
                                    modifier = Modifier
                                        .size(90.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(1.dp, MkTitulo.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                        .background(Paleta.tarjeta)
                                        .clickable(onClickLabel = "Agregar foto") {
                                            // con cámara: elegir entre galería y cámara; sin cámara: directo a la galería
                                            if (FotosCamara.hayCamara(contexto)) mostrarOpcionesFoto = true
                                            else launcherGaleria.launch("image/*")
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = MkTitulo, modifier = Modifier.size(24.dp))
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(text = "Agregar foto", fontFamily = JakartaSansFont, fontSize = 12.sp, color = MkTitulo, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
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
                            modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Paleta.tarjeta, unfocusedContainerColor = Paleta.tarjeta, focusedBorderColor = MkTitulo, unfocusedBorderColor = ColorCamposFondo),
                            textStyle = TextStyle(fontFamily = JakartaSansFont, fontSize = 14.sp, color = ColorTextoPrincipal)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

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

                        //si es servicio local, se permite elegir cómo cobra
                        if (tipoPublicacion == 1) {
                            Text(text = "Esquema de cobro del servicio", fontFamily = JakartaSansFont, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MkTitulo)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(IntrinsicSize.Min),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                BotonEsquemaCobro("Por hora", tipoCobroServicio == 1, Modifier.weight(1f)) { tipoCobroServicio = 1 }
                                BotonEsquemaCobro("Servicio fijo", tipoCobroServicio == 2, Modifier.weight(1f)) { tipoCobroServicio = 2 }
                                BotonEsquemaCobro("Por diseño", tipoCobroServicio == 3, Modifier.weight(1f)) { tipoCobroServicio = 3 }
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = if (tipoPublicacion == 0) "Costo insumos" else "Materiales / Insumos", fontFamily = JakartaSansFont, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MkTitulo)
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = costoInsumos,
                                    onValueChange = { costoInsumos = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    placeholder = { Text("\$ 0.00", fontFamily = JakartaSansFont, fontSize = 13.sp) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Paleta.tarjeta, unfocusedContainerColor = Paleta.tarjeta),
                                    textStyle = TextStyle(fontFamily = JakartaSansFont, fontSize = 14.sp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Dedicación (Horas)", fontFamily = JakartaSansFont, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MkTitulo)
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = horasDedicacion,
                                    onValueChange = { horasDedicacion = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    placeholder = { Text("Ej. 1.5", fontFamily = JakartaSansFont, fontSize = 13.sp) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Paleta.tarjeta, unfocusedContainerColor = Paleta.tarjeta),
                                    textStyle = TextStyle(fontFamily = JakartaSansFont, fontSize = 14.sp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(MkVerdeSugeridoFondo).padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Rango Justo Sugerido", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MkVerdeSugeridoTexto)
                                Text(text = "Cubre insumos + \$75/hr base digna", fontFamily = JakartaSansFont, fontSize = 12.sp, color = MkVerdeSugeridoTexto)
                            }
                            Text(text = "Estimado automático", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, fontSize = 13.sp, textAlign = androidx.compose.ui.text.style.TextAlign.End, color = MkVerdeSugeridoTexto, modifier = Modifier.padding(start = 8.dp))
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(text = "Precio final (Calculado o editable)", fontFamily = JakartaSansFont, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MkTitulo)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = precioFinal,
                            onValueChange = { precioFinal = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            placeholder = { Text("\$ 0.00 MXN", fontFamily = JakartaSansFont, fontSize = 14.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Paleta.tarjeta, unfocusedContainerColor = Paleta.tarjeta),
                            textStyle = TextStyle(fontFamily = OutfitFont, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MkTitulo)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

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
                            fontSize = 13.sp,
                            color = MkSubtitulo
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        if (tipoPublicacion == 0) {
                            //oppciones para producto físico
                            OpcionEntregaItem("Recoger en domicilio", "Las compradoras pasarán al domicilio registrado.", "A domicilio", opcionEntrega == 1) { opcionEntrega = 1 }
                            Spacer(modifier = Modifier.height(10.dp))
                            OpcionEntregaItem("Guía Ciega Prepago (Paquetería)", "Etiqueta generada automáticamente con remitente anónimo a nombre de Crisálida.", "Recomendado", opcionEntrega == 2) { opcionEntrega = 2 }
                            Spacer(modifier = Modifier.height(10.dp))
                            OpcionEntregaItem("Bajo encargo previo (Tiempo artesanal)", "El cliente paga en custodia; dispones de hasta 48 hrs para preparar sin prisas.", "24h - 48h", opcionEntrega == 3) { opcionEntrega = 3 }
                        } else {
                            //opciones adaptadas para servicio local
                            OpcionEntregaItem("Atención en casa de la vendedora", "La clienta acude a tu espacio de trabajo seguro.", "Espacio propio", modalidadServicio == 1) { modalidadServicio = 1 }
                            Spacer(modifier = Modifier.height(10.dp))
                            OpcionEntregaItem("Servicio a domicilio de la compradora", "Tú acudes al domicilio de la clienta (ej. maquillaje o peinado para eventos).", "A domicilio", modalidadServicio == 2) { modalidadServicio = 2 }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "Zona pública (la verán las compradoras)", fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MkTitulo)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = zonaPublica,
                            onValueChange = { if (it.length <= 80) zonaPublica = it },
                            placeholder = { Text("Ej. Centro de Irapuato (solo colonia o zona)", fontFamily = JakartaSansFont, fontSize = 13.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Paleta.tarjeta, unfocusedContainerColor = Paleta.tarjeta, focusedBorderColor = MkTitulo, unfocusedBorderColor = ColorCamposFondo),
                            textStyle = TextStyle(fontFamily = JakartaSansFont, fontSize = 14.sp, color = ColorTextoPrincipal)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = "Punto de entrega o dirección (solo tú la ves)", fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MkTitulo)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = puntoEntrega,
                            onValueChange = { if (it.length <= 200) puntoEntrega = it },
                            placeholder = { Text("Opcional. Se guarda cifrada y nunca se muestra al público", fontFamily = JakartaSansFont, fontSize = 13.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Paleta.tarjeta, unfocusedContainerColor = Paleta.tarjeta, focusedBorderColor = MkTitulo, unfocusedBorderColor = ColorCamposFondo),
                            textStyle = TextStyle(fontFamily = JakartaSansFont, fontSize = 14.sp, color = ColorTextoPrincipal)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { publicar() },
                    enabled = !publicando,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MkBotonPublicar)
                ) {
                    Text(text = if (publicando) (if (editando) "Guardando…" else "Publicando…") else if (editando) "Guardar cambios" else if (tipoPublicacion == 0) "Publicar Producto" else "Publicar Servicio", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Paleta.blanco)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (!editando) OutlinedButton(
                        onClick = { guardarBorrador() },
                        modifier = Modifier.weight(1f).heightIn(min = 42.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Outlined.BookmarkBorder, contentDescription = null, tint = MkTitulo, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Guardar Borrador", fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MkTitulo)
                    }

                    OutlinedButton(
                        onClick = { mostrarDialogoVistaPrevia = true },
                        modifier = Modifier.weight(1f).heightIn(min = 42.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Outlined.Visibility, contentDescription = null, tint = MkTitulo, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Vista Previa", fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MkTitulo)
                    }
                }

                Spacer(modifier = Modifier.height(36.dp))
            }
        }
    }

    if (mostrarOpcionesFoto) {
        AlertDialog(
            onDismissRequest = { mostrarOpcionesFoto = false },
            title = { Text("Agregar foto", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, color = MkTitulo) },
            text = {
                OpcionesAgregarFoto(
                    onGaleria = { mostrarOpcionesFoto = false; launcherGaleria.launch("image/*") },
                    onCamara = { alElegirTomarFoto() }
                )
            },
            confirmButton = { TextButton(onClick = { mostrarOpcionesFoto = false }) { Text("Cancelar", fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, color = MkTitulo) } },
            containerColor = Paleta.superficie, shape = RoundedCornerShape(12.dp)
        )
    }

    // antes de pedir el permiso se explica para qué se usa la cámara
    if (mostrarExplicacionCamara) {
        AlertDialog(
            onDismissRequest = { mostrarExplicacionCamara = false },
            title = { Text("Permiso de cámara", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, color = MkTitulo) },
            text = {
                Text(
                    "Usamos la cámara solo cuando tocas «Tomar foto», para fotografiar tu producto o servicio. " +
                            "La foto no se guarda en tu galería: solo se agrega a tu publicación.",
                    fontFamily = JakartaSansFont, color = MkSubtitulo,
                    modifier = Modifier.verticalScroll(rememberScrollState())
                )
            },
            confirmButton = {
                Button(
                    onClick = { mostrarExplicacionCamara = false; launcherPermisoCamara.launch(Manifest.permission.CAMERA) },
                    colors = ButtonDefaults.buttonColors(containerColor = Paleta.tituloFijo)
                ) { Text("Continuar", color = Paleta.blanco) }
            },
            dismissButton = { TextButton(onClick = { mostrarExplicacionCamara = false }) { Text("Ahora no", fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, color = MkTitulo) } },
            containerColor = Paleta.superficie, shape = RoundedCornerShape(12.dp)
        )
    }

    mensajeCamara?.let { texto ->
        AlertDialog(
            onDismissRequest = { mensajeCamara = null },
            text = { Text(texto, fontFamily = JakartaSansFont, color = MkSubtitulo, modifier = Modifier.verticalScroll(rememberScrollState())) },
            confirmButton = { Button(onClick = { mensajeCamara = null }, colors = ButtonDefaults.buttonColors(containerColor = Paleta.tituloFijo)) { Text("Entendido", color = Paleta.superficie) } },
            containerColor = Paleta.superficie, shape = RoundedCornerShape(12.dp)
        )
    }

    if (mostrarBorradores) {
        AlertDialog(
            onDismissRequest = { mostrarBorradores = false },
            title = { Text("Mis borradores", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, color = MkTitulo) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    if (borradoresGuardados.isEmpty()) Text("No tienes borradores guardados.", fontFamily = JakartaSansFont, color = MkSubtitulo)
                    borradoresGuardados.forEach { b ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(b.titulo, fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MkTitulo, maxLines = 2)
                                Text(
                                    java.text.SimpleDateFormat("d MMM, HH:mm", java.util.Locale("es", "MX")).format(java.util.Date(b.actualizadoEn)),
                                    fontFamily = JakartaSansFont, fontSize = 12.sp, color = MkSubtitulo
                                )
                            }
                            TextButton(onClick = { abrirBorrador(b); mostrarBorradores = false }) { Text("Abrir", color = MkTitulo) }
                            TextButton(onClick = {
                                Borradores.eliminar(contexto, b.id)
                                if (borradorId == b.id) { borradorId = null; borradorRecuperado = false }
                                borradoresGuardados = Borradores.listar(contexto)
                            }) { Text("Eliminar", color = MkTitulo) }
                        }
                    }
                }
            },
            confirmButton = { Button(onClick = { mostrarBorradores = false }, colors = ButtonDefaults.buttonColors(containerColor = Paleta.tituloFijo)) { Text("Cerrar", color = Paleta.superficie) } },
            containerColor = Paleta.superficie, shape = RoundedCornerShape(12.dp)
        )
    }

    mensajeBorrador?.let { texto ->
        AlertDialog(
            onDismissRequest = { mensajeBorrador = null },
            title = { Text("Borrador", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, color = MkTitulo) },
            text = { Text(texto, fontFamily = JakartaSansFont, color = MkSubtitulo, modifier = Modifier.verticalScroll(rememberScrollState())) },
            confirmButton = { Button(onClick = { mensajeBorrador = null }, colors = ButtonDefaults.buttonColors(containerColor = Paleta.tituloFijo)) { Text("Aceptar", color = Paleta.superficie) } },
            containerColor = Paleta.superficie, shape = RoundedCornerShape(12.dp)
        )
    }

    mensajePublicar?.let { texto ->
        AlertDialog(
            onDismissRequest = { mensajePublicar = null; if (publicacionCreada) { if (editando && onEditado != null) onEditado() else onNavigate("portafolio_completo") } },
            text = { Text(texto, fontFamily = JakartaSansFont, color = MkSubtitulo, modifier = Modifier.verticalScroll(rememberScrollState())) },
            confirmButton = {
                Button(
                    onClick = { mensajePublicar = null; if (publicacionCreada) { if (editando && onEditado != null) onEditado() else onNavigate("portafolio_completo") } },
                    colors = ButtonDefaults.buttonColors(containerColor = Paleta.tituloFijo)
                ) { Text(if (publicacionCreada) "Ver mi catálogo" else "Entendido", color = Paleta.blanco) }
            },
            containerColor = Paleta.superficie, shape = RoundedCornerShape(12.dp)
        )
    }

    if (mostrarDialogoVistaPrevia) {
        val precioNum = precioFinal.replace(",", ".").toDoubleOrNull()
        val esProd = tipoPublicacion == 0
        val entregaTxt = if (esProd) when (opcionEntrega) {
            1 -> "Recoge en " + zonaPublica.trim().ifBlank { "su domicilio" }
            3 -> "Por encargo" + (zonaPublica.trim().takeIf { it.isNotBlank() }?.let { " • $it" } ?: "")
            else -> "Envío por paquetería"
        } else {
            if (modalidadServicio == 2) "Va a tu domicilio" + (zonaPublica.trim().takeIf { it.isNotBlank() }?.let { " • $it" } ?: "")
            else "Atiende en " + zonaPublica.trim().ifBlank { "su espacio de trabajo" }
        }
        AlertDialog(
            onDismissRequest = { mostrarDialogoVistaPrevia = false },
            title = { Text("Vista previa", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, color = MkTitulo) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("Así verán tu publicación en el Marketplace con tu alias protegido.", fontFamily = JakartaSansFont, fontSize = 13.sp, color = MkSubtitulo)
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Paleta.tarjetaGris),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Box(
                                modifier = Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(8.dp)).background(Paleta.neutroClaro),
                                contentAlignment = Alignment.Center
                            ) {
                                val nueva = imagenesSeleccionadas.firstOrNull()
                                val existente = fotosExistentes.firstOrNull()
                                when {
                                    nueva != null -> MiniaturaLocal(uri = nueva, modifier = Modifier.fillMaxSize())
                                    existente != null -> FotoRemota(ruta = existente, modifier = Modifier.fillMaxSize())
                                    else -> Text("Sin foto", fontFamily = JakartaSansFont, fontSize = 13.sp, color = MkSubtitulo)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Vendedora: " + com.example.crisalida.data.SesionActual.seudonimo,
                                fontFamily = JakartaSansFont, fontSize = 12.sp, color = MkTitulo
                            )
                            Text(
                                categoriaSeleccionada ?: "(elige una categoría)",
                                fontFamily = JakartaSansFont, fontSize = 12.sp, color = MkSubtitulo
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                tituloDescriptivo.ifBlank { "(escribe un título)" },
                                fontFamily = OutfitFont, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Paleta.textoNegro
                            )
                            Text(
                                if (precioNum != null && precioNum > 0) formatoPrecio(precioNum) + " MXN" else "(escribe un precio)",
                                fontFamily = OutfitFont, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Paleta.verdeBoton
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(entregaTxt, fontFamily = JakartaSansFont, fontSize = 13.sp, color = MkSubtitulo)
                            if (descripcion.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(descripcion, fontFamily = JakartaSansFont, fontSize = 13.sp, lineHeight = 18.sp, color = Paleta.textoNeutro)
                            }
                        }
                    }
                }
            },
            confirmButton = { Button(onClick = { mostrarDialogoVistaPrevia = false }, colors = ButtonDefaults.buttonColors(containerColor = Paleta.tituloFijo)) { Text("Cerrar", color = Paleta.superficie) } },
            containerColor = Paleta.superficie, shape = RoundedCornerShape(12.dp)
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
        color = if (seleccionado) Paleta.superficie else Color.Transparent,
        border = if (!seleccionado) BorderStroke(1.dp, Paleta.fondoEtiquetaVendedoraLinea) else null,
        shadowElevation = if (seleccionado) 2.dp else 0.dp,
        modifier = modifier.heightIn(min = 44.dp)
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
        color = if (seleccionado) Paleta.tituloFijo else Paleta.tarjeta,
        border = BorderStroke(1.dp, if (seleccionado) MkTitulo else Paleta.fondoBotonVolverLinea),
        modifier = modifier.fillMaxHeight().heightIn(min = 38.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(text = titulo, fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.padding(horizontal = 4.dp), color = if (seleccionado) Paleta.blanco else MkTitulo)
        }
    }
}

@Composable
private fun OpcionEntregaItem(titulo: String, subtitulo: String, badge: String, seleccionada: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (seleccionada) Paleta.rosaMuyClaro else Paleta.tarjeta,
        border = BorderStroke(1.dp, if (seleccionada) MkTitulo else Paleta.fondoBotonVolverLinea),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
            RadioButton(selected = seleccionada, onClick = onClick, colors = RadioButtonDefaults.colors(selectedColor = MkTitulo))
            Spacer(modifier = Modifier.width(6.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = titulo, fontFamily = OutfitFont, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MkTitulo, modifier = Modifier.weight(1f).padding(end = 8.dp))
                    Surface(shape = CircleShape, color = MkTitulo.copy(alpha = 0.1f)) {
                        Text(text = badge, fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MkTitulo, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = subtitulo, fontFamily = JakartaSansFont, fontSize = 12.sp, lineHeight = 17.sp, color = MkSubtitulo)
            }
        }
    }
}
// Para revisar sin emulador (Preview: 320 dp, texto al 200 %, horizontal)
@PreviewResponsivo
@Composable
private fun PreviewControlesPublicar() {
    Column(
        modifier = Modifier
            .background(ColorFondoApp)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            BotonEsquemaCobro("Por hora", true, Modifier.weight(1f)) {}
            BotonEsquemaCobro("Servicio fijo", false, Modifier.weight(1f)) {}
            BotonEsquemaCobro("Por diseño", false, Modifier.weight(1f)) {}
        }
        OpcionEntregaItem(
            "Guía Ciega Prepago (Paquetería)",
            "Etiqueta generada automáticamente con remitente anónimo a nombre de Crisálida.",
            "Recomendado", true
        ) {}
        OpcionEntregaItem(
            "Bajo encargo previo (Tiempo artesanal)",
            "El cliente paga en custodia; dispones de hasta 48 hrs para preparar sin prisas.",
            "24h — 48h", false
        ) {}
    }
}
// las dos formas de agregar una foto; los botones miden al menos 48 dp y el texto puede bajar de línea
@Composable
private fun OpcionesAgregarFoto(onGaleria: () -> Unit, onCamara: () -> Unit) {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedButton(
            onClick = onGaleria,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, MkTitulo.copy(alpha = 0.5f))
        ) {
            Icon(Icons.Outlined.PhotoLibrary, contentDescription = null, tint = MkTitulo, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Elegir de la galería", fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, color = MkTitulo, modifier = Modifier.weight(1f, fill = false))
        }
        OutlinedButton(
            onClick = onCamara,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, MkTitulo.copy(alpha = 0.5f))
        ) {
            Icon(Icons.Outlined.PhotoCamera, contentDescription = null, tint = MkTitulo, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Tomar foto", fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, color = MkTitulo, modifier = Modifier.weight(1f, fill = false))
        }
    }
}

// Para revisar sin emulador (Preview: 320 dp, texto al 200 %, horizontal)
@PreviewResponsivo
@Composable
private fun PreviewOpcionesAgregarFoto() {
    Column(modifier = Modifier.background(Paleta.superficie).padding(16.dp)) {
        OpcionesAgregarFoto(onGaleria = {}, onCamara = {})
    }
}