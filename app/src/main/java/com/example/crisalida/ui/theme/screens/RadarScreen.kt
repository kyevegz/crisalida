package com.example.crisalida.ui.theme.screens

import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.LocalFlorist
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.crisalida.data.*
import com.example.crisalida.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val MkTitulo = Color(0xFF9D4067)
private val MkGris = Color(0xFF545454)
private val MkTarjeta = Color(0xFFF2F0EF)
private val MkLila = Color(0xFFCBB6CC)
private val MkOscuro = Color(0xFF3B1F40)
private val MkBadge = Color(0xFFFFB5D5)
private val MkRosaTarjeta = Color(0xFFF1D3DF)
private val MkVino = Color(0xFF62012A)
private val MkBoton = Color(0xFFA47DAB)
private val MkTextoBoton = Color(0xFFFDF0FF)
private val MkPlaceholder = Color(0xFF836B86)
private val MkTextoPregunta = Color(0xFF262626)

private const val MIN_ABIERTA = 3     // las 2 preguntas abiertas fijas no pueden ir vacías
private const val MAX_DINAMICAS_ESTIMADAS = 6   // el Bloque II trae hasta 6 preguntas (8.2)
private const val MAX_ABIERTA = 300   // ni ser demasiado largas (igual que el servidor)
private val PreguntasAbiertasFijas = setOf("gusta", "sabe")

//el radar va dentro del registro. Se contesta una pregunta a la vez, con barra de progreso, «Omitir» y «Atrás» (8.4)
@Composable
fun RadarScreen(
    onTerminar: () -> Unit,
    // salió del radar a propósito: su avance local ya se borró; quien llama solo regresa a la pantalla anterior
    onAbandonar: () -> Unit = {},
    // true: ya tiene cuenta (una compradora que vuelve a vender, 6.5). Al aceptar su resultado se guarda en el servidor
    // y solo se cierra la pantalla en lugar de crear la cuenta.
    yaTieneCuenta: Boolean = false
) {
    val scope = rememberCoroutineScope()
    val scroll = rememberScrollState()
    val contexto = LocalContext.current
    // De quién es el avance local (8.5): la usuaria con sesión o, si se está registrando, su seudónimo
    val duenoAvance = remember { AvanceRadar.dueno() }

    //bloque I (viene del servidor)
    var bloque1 by remember { mutableStateOf<List<PreguntaDto>?>(null) }
    var errorCarga by remember { mutableStateOf<String?>(null) }
    var intentoCarga by remember { mutableIntStateOf(0) }

    //Lo que va contestando (clave = id de la pregunta)
    val respuestas = remember { mutableStateMapOf<String, List<String>>() }
    val textos = remember { mutableStateMapOf<String, String>() }

    //bloque II (lo genera la IA o el respaldo)
    var analisis by remember { mutableStateOf<Bloque2Response?>(null) }
    var analizando by remember { mutableStateOf(false) }
    var errorAnalisis by remember { mutableStateOf<String?>(null) }
    var huellaAnalizada by remember { mutableStateOf<String?>(null) }   // con qué respuestas del Bloque I se armó el Bloque II

    //resultado: propuesta editable (8.3)
    var perfil by remember { mutableStateOf<PerfilResponse?>(null) }
    var huellaPerfil by remember { mutableStateOf<String?>(null) }      // con qué respuestas se armó la propuesta
    val habilidades = remember { mutableStateListOf<HabilidadDto>() }
    var detectando by remember { mutableStateOf(false) }
    var creandoCuenta by remember { mutableStateOf(false) }
    var errorFinal by remember { mutableStateOf<String?>(null) }

    //en qué pregunta va (0 = la primera)
    var paso by remember { mutableIntStateOf(0) }

    // Avance local (8.5)
    var avanceHallado by remember { mutableStateOf<AvanceRadar.Avance?>(null) }   // lo que se encontró guardado y falta decidir
    var avanceRevisado by remember { mutableStateOf(false) }                      // hasta decidirlo no se guarda nada (no se pisa el anterior)
    var terminado by remember { mutableStateOf(false) }                           // ya terminó o abandonó: nada más se guarda
    var confirmarSalida by remember { mutableStateOf(false) }

    // Pide las preguntas fijas del Bloque I al abrir la pantalla
    LaunchedEffect(intentoCarga) {
        errorCarga = null
        when (val r = RadarRepositorio.bloque1()) {
            is ResultadoApi.Exito -> bloque1 = r.datos.preguntas.conAbiertasFijas()
            is ResultadoApi.Error -> errorCarga = r.mensaje
        }
    }

    // Busca un avance guardado (cifrado, solo en este teléfono) una vez que ya cargó el Bloque I
    LaunchedEffect(bloque1 != null) {
        if (bloque1 != null && !avanceRevisado && avanceHallado == null) {
            val guardado = withContext(Dispatchers.IO) { AvanceRadar.leer(contexto, duenoAvance) }
            if (guardado != null) avanceHallado = guardado else avanceRevisado = true
        }
    }

    // Cada pregunta nueva se ve desde arriba
    LaunchedEffect(paso) { scroll.scrollTo(0) }

    fun alternar(preguntaId: String, opcionId: String, unica: Boolean) {
        val actual = respuestas[preguntaId].orEmpty()
        respuestas[preguntaId] = when {
            unica -> listOf(opcionId)
            opcionId in actual -> actual - opcionId
            else -> actual + opcionId
        }
    }

    val actividadesElegidas = respuestas["actividades"].orEmpty()
    val tiempoElegido = respuestas["tiempo"]?.firstOrNull()
    // Las 2 preguntas abiertas fijas (8.1): obligatorias, de MIN_ABIERTA a MAX_ABIERTA caracteres (el servidor las vuelve a revisar)
    val gusta = textos["gusta"].orEmpty().trim()
    val sabe = textos["sabe"].orEmpty().trim()
    val abiertasValidas = gusta.length >= MIN_ABIERTA && sabe.length >= MIN_ABIERTA
    val puedeAnalizar = actividadesElegidas.isNotEmpty() && tiempoElegido != null && abiertasValidas

    val habilidadesValidas = habilidades.isNotEmpty() && habilidades.all { it.titulo.trim().length >= 3 }

    // ---- dónde estamos
    val preguntasB1 = bloque1.orEmpty()
    val dinamicas = analisis?.preguntas.orEmpty()
    val nB1 = preguntasB1.size
    val nDin = dinamicas.size
    val enResultado = perfil != null && analisis != null && paso == nB1 + nDin
    val esBloque1 = nB1 > 0 && paso < nB1
    val esDinamica = !esBloque1 && paso >= nB1 && paso < nB1 + nDin
    val preguntaActual: PreguntaDto? = when {
        esBloque1 -> preguntasB1.getOrNull(paso)
        esDinamica -> dinamicas.getOrNull(paso - nB1)
        else -> null
    }
    // Mientras no se arma el Bloque II se cuenta con el máximo (6); el total se ajusta cuando llegan
    val totalPreguntas = nB1 + (if (analisis != null) nDin else MAX_DINAMICAS_ESTIMADAS)
    val ocupado = analizando || detectando || creandoCuenta

    // Lo que contestó de las dinámicas; las que omitió (o dejó vacías) no se mandan
    fun construirRespuestas(): List<RespuestaDinamicaDto> =
        analisis?.preguntas.orEmpty().map { p ->
            RespuestaDinamicaDto(
                pregunta = p.texto,
                tipo = p.tipo,
                seleccion = respuestas[p.id].orEmpty(),
                texto = textos[p.id].orEmpty()
            )
        }.filter { it.seleccion.isNotEmpty() || it.texto.isNotBlank() }

    // Para saber si lo contestado cambió desde que se armó el Bloque II / la propuesta
    fun huellaBloque1(): String =
        actividadesElegidas.joinToString(",") + "|" + (tiempoElegido ?: "") + "|" + gusta + "|" + sabe

    fun huellaRespuestas(): String =
        construirRespuestas().joinToString("¦") { r -> r.pregunta + ":" + r.seleccion.joinToString(",") + ":" + r.texto.trim() }

    fun analizar() {
        val tiempo = tiempoElegido ?: return
        scope.launch {
            analizando = true
            errorAnalisis = null
            when (val r = RadarRepositorio.analizar(actividadesElegidas, tiempo, gusta, sabe)) {
                is ResultadoApi.Exito -> {
                    // lo contestado de un análisis anterior ya no aplica a las preguntas nuevas
                    dinamicas.forEach { d ->
                        respuestas.remove(d.id)
                        textos.remove(d.id)
                    }
                    analisis = r.datos
                    huellaAnalizada = huellaBloque1()
                    perfil = null
                    huellaPerfil = null
                    habilidades.clear()
                    paso = nB1
                }
                is ResultadoApi.Error -> errorAnalisis = r.mensaje
            }
            analizando = false
        }
    }

    // El servidor solo PROPONE el resultado: no se guarda nada todavía (8.3)
    fun proponer() {
        val tiempo = tiempoElegido ?: return
        scope.launch {
            errorFinal = null
            detectando = true
            when (val r = RadarRepositorio.finalizar(actividadesElegidas, tiempo, gusta, sabe, construirRespuestas())) {
                is ResultadoApi.Exito -> {
                    perfil = r.datos
                    huellaPerfil = huellaRespuestas()
                    habilidades.clear()
                    habilidades.addAll(r.datos.habilidades)
                    paso = nB1 + nDin
                }
                is ResultadoApi.Error -> errorFinal = r.mensaje
            }
            detectando = false
        }
    }

    // Aquí es donde por fin se guarda: lo que ve (con sus cambios) es lo que se guarda
    fun aceptar() {
        val tiempo = tiempoElegido ?: return
        val perfilActual = perfil ?: return
        scope.launch {
            errorFinal = null
            val aceptado = perfilActual.copy(
                habilidades = habilidades.map { it.copy(titulo = it.titulo.trim(), descripcion = it.descripcion.trim()) }
            )
            creandoCuenta = true
            if (yaTieneCuenta) {
                when (val r = RadarRepositorio.aceptar(actividadesElegidas, tiempo, gusta, sabe, construirRespuestas(), aceptado)) {
                    is ResultadoApi.Exito -> {
                        terminado = true
                        AvanceRadar.borrar(contexto, duenoAvance)   // ya está guardado en el servidor: el avance local sobra
                        onTerminar()
                    }
                    is ResultadoApi.Error -> errorFinal = r.mensaje
                }
            } else {
                when (val r = AuthRepositorio.completarRegistro(actividadesElegidas, tiempo, gusta, sabe, construirRespuestas(), aceptado)) {
                    is ResultadoApi.Exito -> {
                        terminado = true
                        AvanceRadar.borrar(contexto, duenoAvance)   // la cuenta ya se creó: el avance local sobra
                        onTerminar()
                    }
                    is ResultadoApi.Error -> errorFinal = r.mensaje
                }
            }
            creandoCuenta = false
        }
    }

    fun siguiente() {
        errorAnalisis = null
        errorFinal = null
        when {
            esBloque1 && paso < nB1 - 1 -> paso++
            esBloque1 ->
                // última del Bloque I: si no cambió nada desde que se armó el Bloque II, se reutiliza
                if (analisis != null && huellaAnalizada == huellaBloque1()) paso++ else analizar()
            esDinamica && paso < nB1 + nDin - 1 -> paso++
            esDinamica ->
                if (perfil != null && huellaPerfil == huellaRespuestas()) paso++ else proponer()
        }
    }

    // Omitir una dinámica: se descarta lo que hubiera marcado en ella y se sigue
    fun omitir() {
        val p = preguntaActual ?: return
        if (!esDinamica) return
        respuestas.remove(p.id)
        textos.remove(p.id)
        siguiente()
    }

    // ---- Avance local (8.5)
    fun hayAlgo(): Boolean =
        paso > 0 || respuestas.values.any { it.isNotEmpty() } || textos.values.any { it.isNotBlank() }

    fun construirAvance() = AvanceRadar.Avance(
        paso = paso,
        respuestas = respuestas.toMap(),
        textos = textos.toMap(),
        analisis = analisis,
        huellaAnalizada = huellaAnalizada,
        perfil = perfil,
        huellaPerfil = huellaPerfil,
        habilidades = habilidades.toList()
    )

    // Guarda ya (al pasar la app a segundo plano o cerrarse la sesión por inactividad)
    fun guardarAvanceAhora() {
        if (terminado || !avanceRevisado) return
        if (hayAlgo()) AvanceRadar.guardar(contexto, duenoAvance, construirAvance())
        else AvanceRadar.borrar(contexto, duenoAvance)
    }

    fun retomar(av: AvanceRadar.Avance) {
        respuestas.clear()
        av.respuestas.orEmpty().forEach { (id, opciones) -> respuestas[id] = opciones }
        textos.clear()
        av.textos.orEmpty().forEach { (id, texto) -> textos[id] = texto }
        analisis = av.analisis
        huellaAnalizada = av.huellaAnalizada
        perfil = if (av.analisis != null) av.perfil else null
        huellaPerfil = if (perfil != null) av.huellaPerfil else null
        habilidades.clear()
        if (perfil != null) habilidades.addAll(av.habilidades.orEmpty())
        // el paso se ajusta a lo que realmente hay (por si cambió el número de preguntas)
        val din = av.analisis?.preguntas?.size ?: 0
        val ultimo = when {
            av.analisis == null -> nB1 - 1
            perfil != null -> nB1 + din
            else -> nB1 + din - 1
        }
        paso = av.paso.coerceIn(0, maxOf(0, ultimo))
    }

    fun empezarDeNuevo() {
        AvanceRadar.borrar(contexto, duenoAvance)
    }

    fun abandonar() {
        terminado = true
        AvanceRadar.borrar(contexto, duenoAvance)
        onAbandonar()
    }

    fun atras() {
        if (paso > 0 && !ocupado) {
            errorAnalisis = null
            errorFinal = null
            paso--
        }
    }

    // El botón «atrás» del teléfono regresa a la pregunta anterior (en la primera hace lo de siempre)
    BackHandler(enabled = paso > 0 && !ocupado) { atras() }
    // En la primera pregunta, salir con lo ya escrito pregunta primero: salir abandona el radar y borra el avance
    BackHandler(enabled = paso == 0 && hayAlgo() && !ocupado && avanceHallado == null) { confirmarSalida = true }

    // Se guarda solo mientras contesta (un momento después del último cambio, para no escribir letra por letra)
    LaunchedEffect(paso, analisis, perfil, respuestas.toMap(), textos.toMap(), habilidades.toList(), avanceRevisado, terminado) {
        if (terminado || !avanceRevisado) return@LaunchedEffect
        delay(500)
        val hay = hayAlgo()
        val avance = construirAvance()
        withContext(Dispatchers.IO) {
            if (hay) AvanceRadar.guardar(contexto, duenoAvance, avance) else AvanceRadar.borrar(contexto, duenoAvance)
        }
    }

    // También al pasar la app a segundo plano y antes de que la sesión se cierre por inactividad
    DisposableEffect(Unit) {
        val guardar: () -> Unit = { guardarAvanceAhora() }
        FormulariosAbiertos.registrar(guardar)
        val ciclo = (contexto as? ComponentActivity)?.lifecycle
        val observador = LifecycleEventObserver { _, evento -> if (evento == Lifecycle.Event.ON_STOP) guardar() }
        ciclo?.addObserver(observador)
        onDispose {
            FormulariosAbiertos.quitar(guardar)
            ciclo?.removeObserver(observador)
        }
    }

    // ¿Retomar lo que dejó a medias?
    avanceHallado?.let { av ->
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Retomamos tu avance", fontFamily = OutfitFont, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Dejaste tu radar a medias. Lo guardamos cifrado solo en este teléfono. ¿Quieres continuar donde te quedaste o empezar de nuevo?",
                    fontFamily = OutfitFont
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    retomar(av)
                    avanceHallado = null
                    avanceRevisado = true
                }) { Text("Continuar", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, color = MkTitulo) }
            },
            dismissButton = {
                TextButton(onClick = {
                    empezarDeNuevo()
                    avanceHallado = null
                    avanceRevisado = true
                }) { Text("Empezar de nuevo", fontFamily = OutfitFont, color = MkTitulo) }
            }
        )
    }

    // ¿Salir del radar?
    if (confirmarSalida) {
        AlertDialog(
            onDismissRequest = { confirmarSalida = false },
            title = { Text("¿Salir del radar?", fontFamily = OutfitFont, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Si sales ahora, se borra lo que llevas contestado. Si prefieres seguir después, solo cierra la app: tu avance queda guardado.",
                    fontFamily = OutfitFont
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmarSalida = false
                    abandonar()
                }) { Text("Salir y borrar", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, color = MkVino) }
            },
            dismissButton = {
                TextButton(onClick = { confirmarSalida = false }) {
                    Text("Seguir aquí", fontFamily = OutfitFont, color = MkTitulo)
                }
            }
        )
    }

    //es parte del registro, sin barras
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorFondoApp)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 640.dp)   // en tablets u horizontal no se estira de lado a lado
                .fillMaxSize()
        ) {
            // ---- Cabecera fija: título y barra de progreso
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 17.dp, end = 16.dp, top = 20.dp)
            ) {
                // título muy grande: crece poco para que "talentos" no se parta en pantallas chicas
                ConEscalaLimitada(tope = 1.3f) {
                    Text(
                        text = "Radar de talentos",
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 28.sp,
                        color = MkTitulo
                    )
                }
                if (bloque1 != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    BarraProgreso(
                        paso = paso,
                        total = totalPreguntas,
                        enResultado = enResultado,
                        totalProvisional = analisis == null
                    )
                }
            }

            // ---- Contenido: una sola pregunta a la vez
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scroll)
                    .padding(top = 16.dp, bottom = 12.dp)
            ) {
                val preguntaVista = preguntaActual
                when {
                    bloque1 == null -> EstadoCarga(errorCarga) { intentoCarga++ }

                    enResultado -> {
                        TarjetaRosa(titulo = "Esto es lo que vemos en ti", colorTitulo = MkVino) {
                            Text(
                                text = "Son sugerencias para orientarte, no un diagnóstico ni una evaluación. Tú decides qué te representa: cámbialas, quita las que no y agrega otras.",
                                fontFamily = OutfitFont,
                                fontSize = 16.sp,
                                lineHeight = 22.sp,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "No se guarda nada hasta que pulses «Aceptar». Si sales ahora, no queda nada guardado.",
                                fontFamily = OutfitFont,
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                color = MkGris
                            )
                            habilidades.forEachIndexed { i, h ->
                                Spacer(modifier = Modifier.height(16.dp))
                                CampoEditable(
                                    valor = h.titulo,
                                    etiqueta = "Habilidad",
                                    maximo = MAX_TITULO_HABILIDAD,
                                    habilitado = !ocupado,
                                    negrita = true,
                                    error = h.titulo.trim().length < 3,
                                    onCambio = { habilidades[i] = h.copy(titulo = it) }
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                CampoEditable(
                                    valor = h.descripcion,
                                    etiqueta = "Descripción (opcional)",
                                    maximo = MAX_DESCRIPCION_HABILIDAD,
                                    habilitado = !ocupado,
                                    negrita = false,
                                    error = false,
                                    onCambio = { habilidades[i] = h.copy(descripcion = it) }
                                )
                                // Siempre se ve «Quitar», pero debe quedar al menos UNA habilidad escrita (nombre de 3 letras o más)
                                val quedaEscrita = habilidades.filterIndexed { j, otra -> j != i }.any { otra -> otra.titulo.trim().length >= 3 }
                                TextButton(
                                    onClick = { habilidades.removeAt(i) },
                                    enabled = !ocupado && quedaEscrita,
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text(
                                        "Quitar esta habilidad",
                                        fontFamily = OutfitFont,
                                        fontWeight = FontWeight.Bold,
                                        color = if (quedaEscrita) MkVino else MkPlaceholder
                                    )
                                }
                            }
                            if (habilidades.size < MAX_HABILIDADES_PERFIL) {
                                TextButton(
                                    onClick = { habilidades.add(HabilidadDto("", "", habilidades.firstOrNull()?.categoria.orEmpty())) },
                                    enabled = !ocupado
                                ) {
                                    Text("+ Agregar una habilidad", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, color = MkVino)
                                }
                            }
                            if (habilidades.count { it.titulo.trim().length >= 3 } <= 1) {
                                Text(
                                    text = "Debe quedar al menos una habilidad escrita.",
                                    fontFamily = OutfitFont,
                                    fontSize = 13.sp,
                                    color = MkGris
                                )
                            }
                            if (!habilidadesValidas) {
                                Text(
                                    text = "Cada habilidad necesita un nombre de al menos 3 letras. Escríbelo o quítala.",
                                    fontFamily = OutfitFont,
                                    fontSize = 13.sp,
                                    color = MkVino
                                )
                            }
                        }
                        MensajeError(errorFinal)
                    }

                    preguntaVista != null -> {
                        // Al entrar al Bloque II se muestra el mensaje personalizado
                        if (esDinamica && paso == nB1) {
                            analisis?.let {
                                TarjetaRosa(titulo = "Tus respuestas han sido analizadas…") {
                                    Text(
                                        text = mensajeConNegrita(it.mensaje),
                                        fontFamily = OutfitFont,
                                        fontSize = 16.sp,
                                        lineHeight = 22.sp,
                                        color = Color.Black
                                    )
                                }
                                Spacer(modifier = Modifier.height(20.dp))
                            }
                        }
                        BloqueCard(
                            numero = if (esBloque1) "1" else "2",
                            titulo = if (esBloque1) "Bloque I. Punto de partida" else "Bloque II. Fase de adaptación",
                            subtitulo = if (esBloque1) "Sobre tu vocación diaria" else "Se arman según lo que nos contaste que te gusta y sabes hacer",
                            paddingInferior = 20.dp
                        ) {
                            PreguntaView(
                                pregunta = preguntaVista,
                                seleccion = respuestas[preguntaVista.id].orEmpty(),
                                texto = textos[preguntaVista.id].orEmpty(),
                                habilitado = !ocupado,
                                onAlternar = { alternar(preguntaVista.id, it, unica = preguntaVista.tipo == "opcion_unica") },
                                onTexto = { textos[preguntaVista.id] = it }
                            )
                        }
                        if (analizando || detectando) {
                            Spacer(modifier = Modifier.height(20.dp))
                            TarjetaRosa(titulo = if (analizando) "Analizando tus respuestas…" else "Detectando tus habilidades…") {
                                Spacer(modifier = Modifier.height(6.dp))
                                CircularProgressIndicator(modifier = Modifier.size(22.dp), color = MkVino, strokeWidth = 2.5.dp)
                            }
                        }
                        MensajeError(errorAnalisis)
                        MensajeError(errorFinal)
                    }
                }
            }

            // ---- Pie fijo: avanzar, volver y omitir
            if (bloque1 != null) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 8.dp)) {
                    if (esBloque1) {
                        Text(
                            text = "Esta pregunta es obligatoria",
                            fontFamily = OutfitFont,
                            fontSize = 12.sp,
                            color = MkGris,
                            modifier = Modifier.padding(start = 26.dp, bottom = 4.dp)
                        )
                    }
                    val ultimaB1 = esBloque1 && paso == nB1 - 1
                    val ultimaDin = esDinamica && paso == nB1 + nDin - 1
                    val respondida = preguntaActual?.let { p ->
                        when (p.id) {
                            "actividades" -> actividadesElegidas.isNotEmpty()
                            "tiempo" -> tiempoElegido != null
                            "gusta", "sabe" -> textos[p.id].orEmpty().trim().length >= MIN_ABIERTA
                            else -> true
                        }
                    } ?: true
                    BotonPrincipal(
                        texto = when {
                            enResultado -> "Aceptar"
                            ultimaB1 -> "Analizar mis respuestas"
                            ultimaDin -> "Ver mi resultado"
                            else -> "Siguiente"
                        },
                        tamano = 20.sp,
                        habilitado = when {
                            enResultado -> habilidadesValidas
                            ultimaB1 -> respondida && puedeAnalizar
                            esBloque1 -> respondida
                            else -> true
                        },
                        cargando = ocupado,
                        onClick = { if (enResultado) aceptar() else siguiente() }
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 17.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { atras() }, enabled = paso > 0 && !ocupado) {
                            Text("‹ Atrás", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, color = MkTitulo)
                        }
                        if (esDinamica) {
                            TextButton(onClick = { omitir() }, enabled = !ocupado) {
                                Text("Omitir", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, color = MkTitulo)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Barra de progreso: en qué pregunta va y cuántas faltan (8.4)
@Composable
private fun BarraProgreso(paso: Int, total: Int, enResultado: Boolean, totalProvisional: Boolean) {
    val actual = paso + 1
    val texto = if (enResultado) "Último paso · revisa tu resultado" else "Pregunta $actual de $total · faltan ${total - actual}"
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = texto,
            fontFamily = OutfitFont,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = MkOscuro
        )
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { if (enResultado) 1f else (paso.toFloat() / total).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape),
            color = MkBoton,
            trackColor = MkLila
        )
        if (totalProvisional && !enResultado) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "El total puede bajar cuando armemos tus preguntas.",
                fontFamily = OutfitFont,
                fontSize = 11.sp,
                color = MkGris
            )
        }
    }
}


//dibuja las preguntas fijas y las dinámicas
@Composable
private fun PreguntaView(
    pregunta: PreguntaDto,
    seleccion: List<String>,
    texto: String,
    habilitado: Boolean,
    onAlternar: (String) -> Unit,
    onTexto: (String) -> Unit
) {
    Column {
        Text(
            text = pregunta.texto,
            fontFamily = OutfitFont,
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp,
            lineHeight = 20.sp,
            color = MkTextoPregunta
        )
        Spacer(modifier = Modifier.height(5.dp))

        when (pregunta.tipo) {
            "casillas", "opcion_unica" -> Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
                pregunta.opciones.forEach { opcion ->
                    OpcionFila(
                        texto = opcion.texto,
                        icono = iconoDe(opcion.icono),
                        esRadio = pregunta.tipo == "opcion_unica",
                        marcada = opcion.id in seleccion,
                        habilitado = habilitado,
                        onClick = { onAlternar(opcion.id) }
                    )
                }
            }

            "chips" -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                pregunta.opciones.forEach { opcion ->
                    val marcada = opcion.id in seleccion
                    Box(
                        modifier = Modifier
                            .heightIn(min = 26.dp)
                            .clip(CircleShape)
                            .background(if (marcada) MkOscuro else MkLila)
                            .clickable(enabled = habilitado) { onAlternar(opcion.id) }
                            .padding(horizontal = 15.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = opcion.texto,
                            fontFamily = OutfitFont,
                            fontWeight = FontWeight.Medium,
                            fontSize = 18.sp,
                            color = if (marcada) MkLila else MkOscuro
                        )
                    }
                }
            }

            "texto" -> Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 93.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MkLila)
                    .padding(12.dp)
            ) {
                BasicTextField(
                    value = texto,
                    onValueChange = { if (it.length <= MAX_ABIERTA) onTexto(it) },
                    enabled = habilitado,
                    textStyle = TextStyle(fontFamily = OutfitFont, fontSize = 15.sp, color = MkOscuro),
                    decorationBox = { campo ->
                        Box {
                            if (texto.isEmpty()) {
                                Text(
                                    text = pregunta.placeholder ?: "Describe lo que tienes...",
                                    fontFamily = OutfitFont,
                                    fontSize = 16.sp,
                                    color = MkPlaceholder
                                )
                            }
                            campo()
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            val obligatoria = pregunta.id in PreguntasAbiertasFijas
            val faltan = obligatoria && texto.trim().length < MIN_ABIERTA
            Text(
                text = (if (obligatoria) "Obligatoria · " else "") + "${texto.length}/$MAX_ABIERTA" +
                    (if (faltan && texto.isNotEmpty()) " · escribe al menos $MIN_ABIERTA letras" else ""),
                fontFamily = OutfitFont,
                fontSize = 12.sp,
                color = MkGris,
                modifier = Modifier.padding(top = 4.dp, end = 4.dp).align(Alignment.End)
            )
            }
        }
    }
}

@Composable
private fun OpcionFila(
    texto: String,
    icono: ImageVector?,
    esRadio: Boolean,
    marcada: Boolean,
    habilitado: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 53.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MkLila)
            .clickable(enabled = habilitado, onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (esRadio) {
            Spacer(modifier = Modifier.width(13.dp))
            Radio(marcada)
            Spacer(modifier = Modifier.width(18.dp))
        } else if (icono != null) {
            Spacer(modifier = Modifier.width(7.dp))
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MkOscuro),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icono, contentDescription = null, tint = MkLila, modifier = Modifier.size(26.dp))
            }
            Spacer(modifier = Modifier.width(4.dp))
        } else {
            Spacer(modifier = Modifier.width(10.dp))
        }

        Text(
            text = texto,
            fontFamily = OutfitFont,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            color = MkOscuro,
            modifier = Modifier.weight(1f)
        )

        if (!esRadio) {
            Casilla(marcada)
            Spacer(modifier = Modifier.width(10.dp))
        }
    }
}

@Composable
private fun Casilla(marcada: Boolean) {
    Box(
        modifier = Modifier
            .size(19.dp)
            .border(2.5.dp, MkOscuro, RoundedCornerShape(3.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (marcada) {
            Icon(imageVector = Icons.Filled.Check, contentDescription = null, tint = MkOscuro, modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
private fun Radio(marcada: Boolean) {
    Box(
        modifier = Modifier
            .size(20.dp)
            .border(2.5.dp, MkOscuro, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (marcada) {
            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(MkOscuro))
        }
    }
}

//tarjeta con numerito, título y subtítulo
@Composable
private fun BloqueCard(
    numero: String,
    titulo: String,
    subtitulo: String,
    paddingInferior: androidx.compose.ui.unit.Dp,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MkTarjeta),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 17.dp, end = 16.dp)
    ) {
        Column(modifier = Modifier.padding(start = 7.dp, end = 7.dp, top = 7.dp, bottom = paddingInferior)) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(33.dp)
                        .clip(CircleShape)
                        .background(MkBadge),
                    contentAlignment = Alignment.Center
                ) {
                    // el número vive dentro de un círculo de 33 dp: no debe crecer con el texto
                    ConEscalaLimitada(tope = 1f) {
                        Text(
                            text = numero,
                            fontFamily = OutfitFont,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 26.sp,
                            color = MkTitulo
                        )
                    }
                }
                Spacer(modifier = Modifier.width(7.dp))
                Column(modifier = Modifier.padding(top = 2.dp)) {
                    Text(
                        text = titulo,
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.Medium,
                        fontSize = 18.sp,
                        lineHeight = 24.sp,
                        color = Color.Black
                    )
                    Text(
                        text = subtitulo,
                        fontFamily = OutfitFont,
                        fontSize = 14.sp,
                        lineHeight = 19.sp,
                        color = MkGris
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            contenido()
        }
    }
}

//tarjeta rosa
@Composable
private fun TarjetaRosa(
    titulo: String,
    colorTitulo: Color = Color.Black,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MkRosaTarjeta),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 17.dp, end = 16.dp)
    ) {
        Column(modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 15.dp, bottom = 17.dp)) {
            Text(
                text = titulo,
                fontFamily = OutfitFont,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = colorTitulo
            )
            Spacer(modifier = Modifier.height(3.dp))
            contenido()
        }
    }
}

@Composable
private fun BotonPrincipal(
    texto: String,
    tamano: TextUnit,
    habilitado: Boolean,
    cargando: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = { if (!cargando) onClick() },
        enabled = habilitado || cargando,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 25.dp)
            .heightIn(min = 43.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MkBoton,
            disabledContainerColor = MkBoton.copy(alpha = 0.5f)
        )
    ) {
        if (cargando) {
            CircularProgressIndicator(modifier = Modifier.size(22.dp), color = MkTextoBoton, strokeWidth = 2.5.dp)
        } else {
            Text(
                text = texto,
                fontFamily = OutfitFont,
                fontWeight = FontWeight.Bold,
                fontSize = tamano,
                color = MkTextoBoton
            )
        }
    }
}

// Campo de texto sencillo para editar el resultado (8.3)
@Composable
private fun CampoEditable(
    valor: String,
    etiqueta: String,
    maximo: Int,
    habilitado: Boolean,
    negrita: Boolean,
    error: Boolean,
    onCambio: (String) -> Unit
) {
    Column {
        Text(text = etiqueta, fontFamily = OutfitFont, fontSize = 12.sp, color = MkGris)
        Spacer(modifier = Modifier.height(2.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White)
                .border(1.dp, if (error) MkVino else Color.Transparent, RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            BasicTextField(
                value = valor,
                onValueChange = { if (it.length <= maximo) onCambio(it) },
                enabled = habilitado,
                textStyle = TextStyle(
                    fontFamily = OutfitFont,
                    fontWeight = if (negrita) FontWeight.Medium else FontWeight.Normal,
                    fontSize = 15.sp,
                    color = MkOscuro
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
        Text(
            text = "${valor.length}/$maximo",
            fontFamily = OutfitFont,
            fontSize = 11.sp,
            color = MkGris,
            modifier = Modifier.padding(top = 2.dp, end = 4.dp).align(Alignment.End)
        )
    }
}

@Composable
private fun EstadoCarga(error: String?, onReintentar: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (error == null) {
            CircularProgressIndicator(color = MkBoton)
        } else {
            MensajeError(error)
            TextButton(onClick = onReintentar) {
                Text("Reintentar", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, color = MkTitulo)
            }
        }
    }
}

private fun mensajeConNegrita(mensaje: String): AnnotatedString {
    val fin = mensaje.indexOfFirst { it == '!' || it == '.' || it == '?' }
    return buildAnnotatedString {
        if (fin in 0 until mensaje.length - 1) {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MkVino)) {
                append(mensaje.substring(0, fin + 1))
            }
            append(mensaje.substring(fin + 1))
        } else {
            append(mensaje)
        }
    }
}

private fun iconoDe(nombre: String?): ImageVector? = when (nombre) {
    "cocina" -> Icons.Outlined.Restaurant
    "manualidades" -> Icons.Outlined.Palette
    "hogar" -> Icons.Outlined.Inventory2
    "plantas" -> Icons.Outlined.LocalFlorist
    else -> null
}