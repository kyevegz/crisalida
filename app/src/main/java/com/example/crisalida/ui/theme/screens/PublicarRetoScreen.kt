package com.example.crisalida.ui.theme.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crisalida.data.MarketplaceRepositorio
import com.example.crisalida.data.MiniaturaLocal
import com.example.crisalida.data.prepararFoto
import com.example.crisalida.data.ResultadoApi
import com.example.crisalida.data.RetoPublicadoResponse
import com.example.crisalida.data.RetoPublicarRequest
import com.example.crisalida.data.RetosPublicadosRepositorio
import com.example.crisalida.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

private val PrVino = Color(0xFF7F284F)
private val PrGris = Color(0xFF545454)
private val PrTexto = Color(0xFF2B2B2B)
private val PrFondoVolver = Color(0xFFE2D4DA)
private val PrFondoIntro = Color(0xFFE6D6DE)
private val PrCampo = Color(0xFFFAF8F8)
private val PrError = Color(0xFFB3261E)
private val PrFondoError = Color(0xFFFBE9E7)
private val PrFondoExito = Color(0xFFE3F1E6)
private val PrVerde = Color(0xFF2E7D32)

private const val MAX_TITULO = 120
private const val MAX_DESCRIPCION = 1500
private const val UNA_HORA = 3_600_000L
private const val SESENTA_DIAS = 60L * 86_400_000L

/**
 * Publicar un microrreto (issue 7.5). Formulario sencillo: título, categoría, descripción, recompensa y fecha/hora límite.
 * Antes de llegar aquí se pasa por la puerta del correo (PuertaCorreo): el servidor exige correo verificado.
 * Tras enviarlo, el reto queda "en revisión" y se explica claramente.
 * Foto de referencia: opcional y UNA sola (el servidor guarda una por reto). Se sube después de crear el reto; si falla,
 * el reto ya quedó enviado y se ofrece reintentar solo la foto.
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PublicarRetoScreen(
    onNavigate: (String) -> Unit,
    onVerificarCorreo: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val contexto = LocalContext.current
    val categoriasCarga = rememberCarga { MarketplaceRepositorio.categorias("reto") }

    var titulo by rememberSaveable { mutableStateOf("") }
    var categoriaId by rememberSaveable { mutableStateOf<Int?>(null) }
    var descripcion by rememberSaveable { mutableStateOf("") }
    var recompensa by rememberSaveable { mutableStateOf("") }
    var fechaMillis by rememberSaveable { mutableStateOf<Long?>(null) }   // día elegido (medianoche UTC, como lo da el selector)
    var hora by rememberSaveable { mutableStateOf<Int?>(null) }
    var minuto by rememberSaveable { mutableStateOf<Int?>(null) }

    var errorEnvio by remember { mutableStateOf<String?>(null) }
    var fotoUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var fotoBytes by remember { mutableStateOf<ByteArray?>(null) }      // la foto ya comprimida, lista para subir
    var avisoFoto by remember { mutableStateOf<String?>(null) }         // la foto no se pudo subir (el reto sí se envió)
    var subiendoFoto by remember { mutableStateOf(false) }
    var retoIdEnviado by remember { mutableStateOf<String?>(null) }
    val launcherGaleria = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) { fotoUri = uri; fotoBytes = null; errorEnvio = null }
    }

    var errores by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var enviando by remember { mutableStateOf(false) }
    var resultado by remember { mutableStateOf<RetoPublicadoResponse?>(null) }
    var pideCorreo by remember { mutableStateOf<String?>(null) }
    // una clave por reto: si se toca dos veces o se corta la conexión, el servidor no crea el reto repetido
    var claveEnvio by remember { mutableStateOf(UUID.randomUUID().toString()) }
    var verFecha by remember { mutableStateOf(false) }
    var verHora by remember { mutableStateOf(false) }

    fun quitarError(campo: String) { errores = errores - campo }

    fun validar(): Map<String, String> {
        val e = mutableMapOf<String, String>()
        if (titulo.trim().length < 5) e["titulo"] = "Escribe un título de al menos 5 letras."
        if (categoriaId == null) e["categoria"] = "Elige una categoría."
        if (descripcion.trim().length < 20) e["descripcion"] = "Cuéntanos un poco más: escribe al menos 20 caracteres."
        val monto = recompensa.toDoubleOrNull()
        if (monto == null || monto < 10.0 || monto > 5000.0) e["recompensa"] = "La recompensa debe estar entre \$10 y \$5,000 MXN."
        val f = fechaMillis
        val h = hora
        val m = minuto
        if (f == null) e["fecha"] = "Elige el día límite."
        if (h == null || m == null) e["hora"] = "Elige la hora límite."
        if (f != null && h != null && m != null) {
            val limite = combinarFechaYHora(f, h, m).timeInMillis
            val ahora = System.currentTimeMillis()
            if (limite < ahora + UNA_HORA) e["fecha"] = "El plazo debe ser al menos una hora después de ahora."
            else if (limite > ahora + SESENTA_DIAS) e["fecha"] = "El plazo no puede ser a más de 60 días."
        }
        return e
    }

    suspend fun subirFoto(retoId: String, bytes: ByteArray) {
        subiendoFoto = true
        avisoFoto = null
        when (val f = RetosPublicadosRepositorio.subirFoto(retoId, bytes)) {
            is ResultadoApi.Exito -> Unit
            is ResultadoApi.Error -> avisoFoto = f.mensaje
        }
        subiendoFoto = false
    }

    fun publicar() {
        if (enviando) return
        errorEnvio = null
        val e = validar()
        errores = e
        if (e.isNotEmpty()) return
        val limite = combinarFechaYHora(fechaMillis!!, hora!!, minuto!!)
        val datos = RetoPublicarRequest(
            titulo = titulo.trim().replace(Regex("\\s+"), " "),
            categoriaId = categoriaId!!,
            descripcion = descripcion.trim(),
            compensacion = recompensa.toDouble(),
            fechaLimite = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).format(limite.time)
        )
        scope.launch {
            enviando = true
            // la foto se prepara ANTES de publicar: si no se puede leer, no se crea nada y se avisa
            val uri = fotoUri
            if (uri != null && fotoBytes == null) {
                fotoBytes = withContext(Dispatchers.IO) { prepararFoto(contexto, uri) }
                if (fotoBytes == null) {
                    errorEnvio = "No pudimos leer esa foto. Elige otra o quítala para publicar sin foto."
                    enviando = false
                    return@launch
                }
            }
            when (val r = RetosPublicadosRepositorio.publicar(datos, claveEnvio)) {
                is ResultadoApi.Exito -> {
                    resultado = r.datos
                    retoIdEnviado = r.datos.retoId
                    val bytes = fotoBytes
                    if (bytes != null) subirFoto(r.datos.retoId, bytes)
                }
                is ResultadoApi.Error ->
                    if (r.codigoHttp == 403 && r.mensaje.contains("correo", ignoreCase = true)) pideCorreo = r.mensaje
                    else errorEnvio = r.mensaje   // filtros de seguridad (422), límites, sin conexión...
            }
            enviando = false
        }
    }

    fun empezarOtro() {
        titulo = ""; categoriaId = null; descripcion = ""; recompensa = ""
        fechaMillis = null; hora = null; minuto = null
        fotoUri = null; fotoBytes = null; avisoFoto = null; retoIdEnviado = null
        errores = emptyMap(); errorEnvio = null; resultado = null
        claveEnvio = UUID.randomUUID().toString()
    }

    CrisalidaScaffold(currentTitle = "Publicar reto", selectedTab = 5, onNavigate = onNavigate) { innerPadding ->
        ContenidoCentrado(innerPadding) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    onClick = { onNavigate("perfil") },
                    shape = RoundedCornerShape(18.dp),
                    color = PrFondoVolver,
                    shadowElevation = 1.dp
                ) {
                    Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = PrVino, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Mi perfil", fontFamily = OutfitFont, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = PrVino)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                ConEscalaLimitada(tope = 1.3f) {
                    Text("Microrretos", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, fontSize = 32.sp, color = PrVino,
                        modifier = Modifier.semantics { heading() })
                }
                Text("Publicar un reto", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PrVino)

                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = PrFondoIntro),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                        Box(
                            modifier = Modifier.size(40.dp).clip(CircleShape).background(PrVino.copy(alpha = 0.55f)),
                            contentAlignment = Alignment.Center
                        ) { Icon(Icons.Outlined.VolunteerActivism, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp)) }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Recibe ayuda mientras ayudas", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = PrVino)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Al publicar una tarea o «reto» en el que necesites ayuda, también estás ayudando a otra mujer que quizás necesita un ingreso extra. " +
                                        "Gracias por colaborar con la comunidad. Ten presentes las normas para hacer de Crisálida un espacio seguro para todas.",
                                fontFamily = JakartaSansFont, fontSize = 14.sp, lineHeight = 20.sp, color = PrGris
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                val enviado = resultado
                if (enviado != null) {
                    RetoEnRevision(
                        mensaje = enviado.mensaje,
                        avisoFoto = avisoFoto,
                        subiendoFoto = subiendoFoto,
                        onReintentarFoto = {
                            val id = retoIdEnviado
                            val bytes = fotoBytes
                            if (id != null && bytes != null) scope.launch { subirFoto(id, bytes) }
                        },
                        onPerfil = { onNavigate("perfil") },
                        onOtro = { empezarOtro() }
                    )
                } else {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Información del microrreto", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = PrVino,
                                modifier = Modifier.semantics { heading() })

                            // ---- título
                            Etiqueta("Título descriptivo")
                            CampoTexto(
                                valor = titulo,
                                onCambio = { titulo = it.take(MAX_TITULO); quitarError("titulo") },
                                placeholder = "Ej. Pasar a Excel unas cuentas",
                                error = errores["titulo"],
                                contador = "${titulo.length}/$MAX_TITULO",
                                capitalizacion = KeyboardCapitalization.Sentences
                            )

                            // ---- categoría (las da el servidor)
                            Etiqueta("Selecciona una categoría")
                            when (val e = categoriasCarga.estado) {
                                is Carga.Cargando -> IndicadorCargando()
                                is Carga.Falla -> ErrorDeCarga(e, categoriasCarga.recargar, "Necesitas internet para elegir la categoría.")
                                is Carga.Lista -> FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    e.datos.categorias.forEach { c ->
                                        ChipCategoria(c.nombre, categoriaId == c.id) { categoriaId = c.id; quitarError("categoria") }
                                    }
                                }
                            }
                            errores["categoria"]?.let { TextoError(it) }

                            // ---- foto de referencia (opcional, una sola)
                            Etiqueta("Imagen de referencia (opcional)")
                            Text("Puedes agregar una foto que ayude a entender lo que necesitas.", fontFamily = JakartaSansFont, fontSize = 13.sp, color = PrGris)
                            Spacer(modifier = Modifier.height(8.dp))
                            val foto = fotoUri
                            if (foto != null) {
                                Box(modifier = Modifier.size(120.dp).clip(RoundedCornerShape(8.dp)).background(Color.LightGray)) {
                                    MiniaturaLocal(uri = foto, modifier = Modifier.fillMaxSize())
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(4.dp)
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xAA000000))
                                            .clickable(onClickLabel = "Quitar la foto") { fotoUri = null; fotoBytes = null },
                                        contentAlignment = Alignment.Center
                                    ) { Text("✕", fontSize = 14.sp, color = Color.White) }
                                }
                            } else {
                                Column(
                                    modifier = Modifier
                                        .size(120.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(1.dp, PrVino.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                        .background(PrCampo)
                                        .clickable(onClickLabel = "Agregar una foto", role = Role.Button) { launcherGaleria.launch("image/*") },
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Outlined.AddPhotoAlternate, contentDescription = null, tint = PrVino, modifier = Modifier.size(36.dp))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("+ Agregar foto", fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PrVino)
                                }
                            }

                            // ---- descripción
                            Etiqueta("Descripción")
                            CampoTexto(
                                valor = descripcion,
                                onCambio = { descripcion = it.take(MAX_DESCRIPCION); quitarError("descripcion") },
                                placeholder = "Cuenta qué necesitas, para cuándo y cómo quieres recibirlo.",
                                error = errores["descripcion"],
                                contador = "${descripcion.length}/$MAX_DESCRIPCION",
                                capitalizacion = KeyboardCapitalization.Sentences,
                                lineasMin = 4
                            )

                            // ---- recompensa
                            Etiqueta("Recompensa")
                            OutlinedTextField(
                                value = recompensa,
                                onValueChange = { recompensa = soloMonto(it); quitarError("recompensa") },
                                prefix = { Text("\$", fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, color = PrVino) },
                                suffix = { Text("MXN", fontFamily = JakartaSansFont, color = PrGris) },
                                placeholder = { Text("150.00", fontFamily = JakartaSansFont, fontSize = 14.sp) },
                                isError = errores["recompensa"] != null,
                                supportingText = { errores["recompensa"]?.let { Text(it, color = PrError) } },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.widthIn(min = 180.dp, max = 260.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = coloresCampo(),
                                textStyle = TextStyle(fontFamily = JakartaSansFont, fontSize = 15.sp, color = PrTexto)
                            )

                            // ---- fecha y hora límite
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Etiqueta("Fecha límite", arriba = 0.dp)
                                    CampoSeleccion(
                                        icono = Icons.Outlined.CalendarMonth,
                                        texto = fechaMillis?.let { formatoFecha(it) } ?: "Seleccionar día",
                                        descripcion = "Elegir el día límite",
                                        error = errores["fecha"] != null,
                                        modifier = Modifier.fillMaxWidth()
                                    ) { verFecha = true }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Etiqueta("Hora límite", arriba = 0.dp)
                                    CampoSeleccion(
                                        icono = Icons.Outlined.Schedule,
                                        texto = if (hora != null && minuto != null) formatoHora(hora!!, minuto!!) else "Seleccionar hora",
                                        descripcion = "Elegir la hora límite",
                                        error = errores["hora"] != null,
                                        modifier = Modifier.fillMaxWidth()
                                    ) { verHora = true }
                                }
                            }
                            errores["fecha"]?.let { TextoError(it) }
                            errores["hora"]?.let { TextoError(it) }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        "*Recuerda que todos los retos pasan por una revisión previa para asegurarnos de que no se publique nada inapropiado. " +
                                "Nuestro equipo se encargará de aprobarlo o rechazarlo a la brevedad.",
                        fontFamily = JakartaSansFont, fontSize = 13.sp, lineHeight = 19.sp, color = PrGris
                    )

                    if (errores.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Revisa los campos marcados antes de publicar.", fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold,
                            fontSize = 14.sp, color = PrError, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
                    }
                    errorEnvio?.let { mensaje ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = PrFondoError),
                            modifier = Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite }
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Revisa esto antes de publicar", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = PrError)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(mensaje, fontFamily = JakartaSansFont, fontSize = 14.sp, lineHeight = 20.sp, color = PrTexto)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { publicar() },
                        enabled = !enviando,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrVino)
                    ) {
                        if (enviando) CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                        else Text("Publicar microrreto", fontFamily = OutfitFont, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, color = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }

    if (verFecha) {
        val estado = rememberDatePickerState(initialSelectedDateMillis = fechaMillis)
        DatePickerDialog(
            onDismissRequest = { verFecha = false },
            confirmButton = {
                TextButton(onClick = {
                    estado.selectedDateMillis?.let { fechaMillis = it; quitarError("fecha") }
                    verFecha = false
                }) { Text("Aceptar", fontWeight = FontWeight.Bold, color = PrVino) }
            },
            dismissButton = { TextButton(onClick = { verFecha = false }) { Text("Cancelar", color = PrVino) } }
        ) { DatePicker(state = estado) }
    }

    if (verHora) {
        val estado = rememberTimePickerState(initialHour = hora ?: 18, initialMinute = minuto ?: 0, is24Hour = false)
        AlertDialog(
            onDismissRequest = { verHora = false },
            title = { Text("Hora límite", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, color = PrVino) },
            text = { Column(modifier = Modifier.verticalScroll(rememberScrollState())) { TimePicker(state = estado) } },
            confirmButton = {
                TextButton(onClick = {
                    hora = estado.hour; minuto = estado.minute; quitarError("hora"); quitarError("fecha")
                    verHora = false
                }) { Text("Aceptar", fontWeight = FontWeight.Bold, color = PrVino) }
            },
            dismissButton = { TextButton(onClick = { verHora = false }) { Text("Cancelar", color = PrVino) } },
            containerColor = Color.White
        )
    }

    pideCorreo?.let { mensaje ->
        AlertDialog(
            onDismissRequest = { pideCorreo = null },
            title = { Text("Verifica tu correo", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, color = PrVino) },
            text = {
                Text("$mensaje Verifícalo y vuelve a escribir tu reto.", fontFamily = JakartaSansFont, color = PrGris)
            },
            confirmButton = {
                TextButton(onClick = { pideCorreo = null; onVerificarCorreo() }) { Text("Verificar correo", fontWeight = FontWeight.Bold, color = PrVino) }
            },
            dismissButton = { TextButton(onClick = { pideCorreo = null }) { Text("Ahora no", color = PrVino) } },
            containerColor = Color.White
        )
    }
}

// ---------------------------------------------------------------- Después de enviar

@Composable
private fun RetoEnRevision(
    mensaje: String,
    avisoFoto: String?,
    subiendoFoto: Boolean,
    onReintentarFoto: () -> Unit,
    onPerfil: () -> Unit,
    onOtro: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = PrFondoExito),
        modifier = Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite }
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = PrVerde, modifier = Modifier.size(30.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text("¡Tu reto está en revisión!", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = PrVerde,
                    modifier = Modifier.semantics { heading() })
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(mensaje, fontFamily = JakartaSansFont, fontSize = 15.sp, lineHeight = 21.sp, color = PrTexto)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Mientras lo revisamos no aparece en el tablero de retos. Cuando lo aprueben, otras mujeres podrán verlo y aceptarlo.",
                fontFamily = JakartaSansFont, fontSize = 14.sp, lineHeight = 20.sp, color = PrGris
            )
        }
    }
    if (subiendoFoto) {
        Spacer(modifier = Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = PrVino, strokeWidth = 2.dp)
            Spacer(modifier = Modifier.width(10.dp))
            Text("Subiendo tu foto…", fontFamily = JakartaSansFont, fontSize = 14.sp, color = PrGris)
        }
    }
    if (avisoFoto != null && !subiendoFoto) {
        Spacer(modifier = Modifier.height(12.dp))
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = PrFondoError),
            modifier = Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite }
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Tu reto se envió, pero la foto no se pudo subir", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = PrError)
                Spacer(modifier = Modifier.height(4.dp))
                Text(avisoFoto, fontFamily = JakartaSansFont, fontSize = 14.sp, color = PrTexto)
                TextButton(onClick = onReintentarFoto) { Text("Reintentar subir la foto", fontWeight = FontWeight.Bold, color = PrVino) }
            }
        }
    }
    Spacer(modifier = Modifier.height(16.dp))
    Button(
        onClick = onPerfil,
        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
        shape = RoundedCornerShape(6.dp),
        colors = ButtonDefaults.buttonColors(containerColor = PrVino)
    ) { Text("Ir a mi perfil", fontFamily = OutfitFont, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = Color.White) }
    Spacer(modifier = Modifier.height(8.dp))
    TextButton(onClick = onOtro, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
        Text("Publicar otro reto", fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, color = PrVino)
    }
}

// ---------------------------------------------------------------- Piezas del formulario

@Composable
private fun Etiqueta(texto: String, arriba: androidx.compose.ui.unit.Dp = 16.dp) {
    Spacer(modifier = Modifier.height(arriba))
    Text(texto, fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = PrTexto)
    Spacer(modifier = Modifier.height(4.dp))
}

@Composable
private fun TextoError(texto: String) {
    Text(texto, fontFamily = JakartaSansFont, fontSize = 12.sp, color = PrError, modifier = Modifier.padding(top = 4.dp))
}

@Composable
private fun coloresCampo() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = PrCampo, unfocusedContainerColor = PrCampo,
    focusedBorderColor = PrVino, unfocusedBorderColor = ColorCamposFondo,
    errorContainerColor = PrCampo, errorBorderColor = PrError
)

@Composable
private fun CampoTexto(
    valor: String,
    onCambio: (String) -> Unit,
    placeholder: String,
    error: String?,
    contador: String,
    capitalizacion: KeyboardCapitalization,
    lineasMin: Int = 1
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        placeholder = { Text(placeholder, fontFamily = JakartaSansFont, fontSize = 14.sp) },
        isError = error != null,
        supportingText = {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(error.orEmpty(), color = PrError, modifier = Modifier.weight(1f))
                Text(contador, color = PrGris)
            }
        },
        singleLine = lineasMin == 1,
        minLines = lineasMin,
        keyboardOptions = KeyboardOptions(capitalization = capitalizacion),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = coloresCampo(),
        textStyle = TextStyle(fontFamily = JakartaSansFont, fontSize = 15.sp, color = PrTexto)
    )
}

@Composable
private fun ChipCategoria(texto: String, seleccionada: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (seleccionada) PrVino else Color(0xFFF1E9ED),
        modifier = Modifier.heightIn(min = 44.dp).semantics { selected = seleccionada }
    ) {
        Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp), contentAlignment = Alignment.Center) {
            Text(
                texto, fontFamily = JakartaSansFont, fontSize = 14.sp,
                fontWeight = if (seleccionada) FontWeight.Bold else FontWeight.Medium,
                color = if (seleccionada) Color.White else PrVino
            )
        }
    }
}

@Composable
private fun CampoSeleccion(
    icono: ImageVector,
    texto: String,
    descripcion: String,
    error: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, if (error) PrError else ColorCamposFondo, RoundedCornerShape(8.dp))
            .background(PrCampo)
            .clickable(onClickLabel = descripcion, role = Role.Button, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icono, contentDescription = null, tint = PrVino, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(texto, fontFamily = JakartaSansFont, fontSize = 14.sp, color = PrTexto)
    }
}

// ---------------------------------------------------------------- Ayudas

/** Deja solo dígitos y un punto decimal, con máximo 2 decimales y 5 enteros (el servidor valida el rango). */
private fun soloMonto(texto: String): String {
    val limpio = texto.filter { it.isDigit() || it == '.' }
    val punto = limpio.indexOf('.')
    if (punto < 0) return limpio.take(5)
    val enteros = limpio.substring(0, punto).take(5)
    val decimales = limpio.substring(punto + 1).replace(".", "").take(2)
    return "$enteros.$decimales"
}

/** El selector de día da la medianoche UTC del día elegido; con la hora se arma el momento en la zona del teléfono. */
private fun combinarFechaYHora(diaUtcMillis: Long, hora: Int, minuto: Int): Calendar {
    val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = diaUtcMillis }
    return Calendar.getInstance().apply {
        clear()
        set(utc.get(Calendar.YEAR), utc.get(Calendar.MONTH), utc.get(Calendar.DAY_OF_MONTH), hora, minuto, 0)
    }
}

private fun formatoFecha(diaUtcMillis: Long): String =
    SimpleDateFormat("d 'de' MMMM 'de' yyyy", Locale("es", "MX")).apply { timeZone = TimeZone.getTimeZone("UTC") }.format(diaUtcMillis)

private fun formatoHora(hora: Int, minuto: Int): String {
    val c = Calendar.getInstance().apply { clear(); set(2000, 0, 1, hora, minuto, 0) }
    return SimpleDateFormat("h:mm a", Locale("es", "MX")).format(c.time)
}