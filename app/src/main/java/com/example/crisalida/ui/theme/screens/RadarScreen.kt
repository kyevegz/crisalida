package com.example.crisalida.ui.theme.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.example.crisalida.data.*
import com.example.crisalida.ui.theme.*
import kotlinx.coroutines.launch

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

//el radar va dentro del registro, la cuenta se crea hasta el segundo dar clic en terminar
@Composable
fun RadarScreen(onTerminar: () -> Unit) {
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    //bloque I (viene del servidor)
    var bloque1 by remember { mutableStateOf<List<PreguntaDto>?>(null) }
    var errorCarga by remember { mutableStateOf<String?>(null) }
    var intentoCarga by remember { mutableIntStateOf(0) }

    //Lo que va contestando (clave = id de la pregunta)
    val respuestas = remember { mutableStateMapOf<String, List<String>>() }
    val textos = remember { mutableStateMapOf<String, String>() }

    //bloque II (lo genera la IA)
    var analisis by remember { mutableStateOf<Bloque2Response?>(null) }
    var analizando by remember { mutableStateOf(false) }
    var errorAnalisis by remember { mutableStateOf<String?>(null) }

    //besultado final
    var perfil by remember { mutableStateOf<PerfilResponse?>(null) }
    var detectando by remember { mutableStateOf(false) }
    var creandoCuenta by remember { mutableStateOf(false) }
    var errorFinal by remember { mutableStateOf<String?>(null) }

    // Pide las 2 preguntas fijas al abrir la pantalla
    LaunchedEffect(intentoCarga) {
        errorCarga = null
        when (val r = RadarRepositorio.bloque1()) {
            is ResultadoApi.Exito -> bloque1 = r.datos.preguntas
            is ResultadoApi.Error -> errorCarga = r.mensaje
        }
    }

    //desplazamiento automático para que la usuaria vea lo nuevo que va apareciendo
    LaunchedEffect(analizando) { if (analizando) listState.animateScrollToItem(2) }
    LaunchedEffect(analisis) { if (analisis != null) listState.animateScrollToItem(2) }
    LaunchedEffect(perfil) { if (perfil != null) listState.animateScrollToItem(4) }

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
    val puedeAnalizar = actividadesElegidas.isNotEmpty() && tiempoElegido != null

    val primeraRespondida = analisis?.preguntas?.firstOrNull()
        ?.let { respuestas[it.id].orEmpty().isNotEmpty() } == true
    val puedeTerminar = perfil != null || (analisis != null && primeraRespondida)

    fun analizar() {
        val tiempo = tiempoElegido ?: return
        scope.launch {
            analizando = true
            errorAnalisis = null
            when (val r = RadarRepositorio.analizar(actividadesElegidas, tiempo)) {
                is ResultadoApi.Exito -> analisis = r.datos
                is ResultadoApi.Error -> errorAnalisis = r.mensaje
            }
            analizando = false
        }
    }

    fun construirRespuestas(): List<RespuestaDinamicaDto> =
        analisis?.preguntas.orEmpty().map { p ->
            RespuestaDinamicaDto(
                pregunta = p.texto,
                tipo = p.tipo,
                seleccion = respuestas[p.id].orEmpty(),
                texto = textos[p.id].orEmpty()
            )
        }

    fun terminar() {
        if (analisis == null) return
        val tiempo = tiempoElegido ?: return
        val perfilActual = perfil
        scope.launch {
            errorFinal = null
            if (perfilActual == null) {
                detectando = true
                when (val r = RadarRepositorio.finalizar(actividadesElegidas, tiempo, construirRespuestas())) {
                    is ResultadoApi.Exito -> perfil = r.datos
                    is ResultadoApi.Error -> errorFinal = r.mensaje
                }
                detectando = false
            } else {
                creandoCuenta = true
                when (val r = AuthRepositorio.completarRegistro(actividadesElegidas, tiempo, construirRespuestas(), perfilActual)) {
                    is ResultadoApi.Exito -> onTerminar()
                    is ResultadoApi.Error -> errorFinal = r.mensaje
                }
                creandoCuenta = false
            }
        }
    }

    //es parte del registro, sin barras
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorFondoApp)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 17.dp, end = 16.dp, top = 28.dp)
                ) {
                    Text(
                        text = "Radar de talentos",
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 32.sp,
                        color = MkTitulo
                    )
                    Text(
                        text = "Descubre tus superpoderes cotidianos a tu propio ritmo, sin tecnicismos ni formularios rígidos.",
                        fontFamily = OutfitFont,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = MkGris
                    )
                }
            }

            item {
                Column {
                    Spacer(modifier = Modifier.height(20.dp))
                    val preguntas = bloque1
                    if (preguntas == null) {
                        EstadoCarga(errorCarga) { intentoCarga++ }
                    } else {
                        BloqueCard(
                            numero = "1",
                            titulo = "Bloque I. Punto de partida",
                            subtitulo = "Sobre tu vocación diaria",
                            paddingInferior = 27.dp
                        ) {
                            preguntas.forEachIndexed { i, p ->
                                if (i > 0) Spacer(modifier = Modifier.height(40.dp))
                                PreguntaView(
                                    pregunta = p,
                                    seleccion = respuestas[p.id].orEmpty(),
                                    texto = "",
                                    habilitado = analisis == null && !analizando,
                                    onAlternar = { alternar(p.id, it, unica = p.tipo == "opcion_unica") },
                                    onTexto = {}
                                )
                            }
                        }
                    }
                }
            }

            item {
                Column {
                    if (bloque1 != null) {
                        if (analisis == null && !analizando) {
                            Spacer(modifier = Modifier.height(20.dp))
                            BotonPrincipal(
                                texto = "Analizar mis respuestas",
                                tamano = 18.sp,
                                habilitado = puedeAnalizar,
                                cargando = false,
                                onClick = { analizar() }
                            )
                            MensajeError(errorAnalisis)
                        } else {
                            Spacer(modifier = Modifier.height(28.dp))
                            TarjetaRosa(
                                titulo = if (analizando) "Analizando tus respuestas…" else "Tus respuestas han sido analizadas…"
                            ) {
                                if (analizando) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(22.dp),
                                        color = MkVino,
                                        strokeWidth = 2.5.dp
                                    )
                                } else {
                                    analisis?.let {
                                        Text(
                                            text = mensajeConNegrita(it.mensaje),
                                            fontFamily = OutfitFont,
                                            fontSize = 16.sp,
                                            lineHeight = 18.sp,
                                            color = Color.Black
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Column {
                    analisis?.let { b2 ->
                        Spacer(modifier = Modifier.height(36.dp))
                        BloqueCard(
                            numero = "2",
                            titulo = "Bloque II. Fase de adaptación",
                            subtitulo = "Las siguientes preguntas se generan dinámicamente según tus intereses",
                            paddingInferior = 20.dp,

                        ) {
                            b2.preguntas.forEachIndexed { i, p ->
                                if (i > 0) Spacer(modifier = Modifier.height(40.dp))
                                PreguntaView(
                                    pregunta = p,
                                    seleccion = respuestas[p.id].orEmpty(),
                                    texto = textos[p.id].orEmpty(),
                                    habilitado = perfil == null && !detectando && !creandoCuenta,
                                    onAlternar = { alternar(p.id, it, unica = false) },
                                    onTexto = { textos[p.id] = it }
                                )
                            }
                        }
                    }
                }
            }

            item {
                Column {
                    perfil?.let { p ->
                        Spacer(modifier = Modifier.height(38.dp))
                        TarjetaRosa(titulo = "¡Tus habilidades han sido detectadas!", colorTitulo = MkVino) {
                            Text(
                                text = "Después de este cuestionario, se harán saber los resultados detectados por el radar, esperando que sirvan como guía en este nuevo camino que haz emprendido <3.",
                                fontFamily = OutfitFont,
                                fontSize = 16.sp,
                                lineHeight = 15.sp,
                                color = Color.Black
                            )
                            //resumen de lo detectado
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = p.habilidades.joinToString(" · ") { it.titulo },
                                fontFamily = OutfitFont,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                lineHeight = 16.sp,
                                color = MkVino
                            )
                        }
                    }
                }
            }

            item {
                Column {
                    if (analisis != null) {
                        Spacer(modifier = Modifier.height(34.dp))
                        MensajeError(errorFinal)
                        Spacer(modifier = Modifier.height(10.dp))
                        BotonPrincipal(
                            texto = "Terminar",
                            tamano = 22.sp,
                            habilitado = puedeTerminar,
                            cargando = detectando || creandoCuenta,
                            onClick = { terminar() }
                        )
                    }
                }
            }
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
            lineHeight = 13.sp,
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

            "texto" -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 93.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MkLila)
                    .padding(12.dp)
            ) {
                BasicTextField(
                    value = texto,
                    onValueChange = { if (it.length <= 300) onTexto(it) },
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
                    Text(
                        text = numero,
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 26.sp,
                        color = MkTitulo
                    )
                }
                Spacer(modifier = Modifier.width(7.dp))
                Column(modifier = Modifier.padding(top = 2.dp)) {
                    Text(
                        text = titulo,
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.Medium,
                        fontSize = 18.sp,
                        lineHeight = 18.sp,
                        color = Color.Black
                    )
                    Text(
                        text = subtitulo,
                        fontFamily = OutfitFont,
                        fontSize = 14.sp,
                        lineHeight = 14.sp,
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