package com.example.crisalida.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
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
import com.example.crisalida.data.BienestarRepositorio
import com.example.crisalida.data.FloresResponse
import com.example.crisalida.data.ProgresoRepositorio
import com.example.crisalida.data.ResultadoApi
import kotlinx.coroutines.launch
import com.example.crisalida.ui.screens.*
import java.text.SimpleDateFormat
import java.util.Locale

data class EmocionItem(val emoji: String, val nombre: String)

@Composable
fun BienestarScreen(
    onNavigate: (String) -> Unit
) {
    // 1. Amplio catálogo de emociones
    val emocionesBase = listOf(
        EmocionItem("😊", "Calmada"),
        EmocionItem("⚡", "Con energía"),
        EmocionItem("😔", "Abrumada"),
        EmocionItem("🤝", "Buscando apoyo"),
        EmocionItem("😢", "Triste"),
        EmocionItem("🥺", "Sola"),
        EmocionItem("😠", "Enojada"),
        EmocionItem("😤", "Frustrada"),
        EmocionItem("🥱", "Cansada"),
        EmocionItem("💖", "Cariñosa"),
        EmocionItem("🕊️", "Compasiva"),
        EmocionItem("🌿", "Contemplativa"),
        EmocionItem("🛡️", "Cautelosa"),
        EmocionItem("✨", "Inspirada"),
        EmocionItem("🌧️", "Melancólica"),
        EmocionItem("🔥", "Motivada"),
        EmocionItem("🧘‍♀️", "Centrada"),
        EmocionItem("🌪️", "Ansiosa"),
        EmocionItem("🔋", "Agotada"),
        EmocionItem("🌱", "Esperanzada"),
        EmocionItem("🧩", "Confundida"),
        EmocionItem("🎯", "Enfocada"),
        EmocionItem("🌊", "Sensible"),
        EmocionItem("☀️", "Optimista"),
        EmocionItem("🥺", "Vulnerable"),
        EmocionItem("🍂", "Nostálgica"),
        EmocionItem("💔", "Desanimada"),
        EmocionItem("🌸", "Agradecida"),
        EmocionItem("😌", "Tranquila"),
        EmocionItem("💫", "Satisfecha"),
        EmocionItem("🌊", "Desbordada"),
        EmocionItem("🥀", "Insegura")
    )

    var searchQuery by remember { mutableStateOf("") }
    val emocionesFiltradas = if (searchQuery.isBlank()) {
        emocionesBase.take(6)
    } else {
        emocionesBase.filter { it.nombre.contains(searchQuery, ignoreCase = true) }
    }
    var emocionSeleccionada by remember { mutableStateOf<String?>(null) }
    var floresEmocionGuardadas by remember { mutableStateOf(false) }

    // 2. Monitoreo Corporal y Emocional (Los 3 con sus caritas expresivas)
    var tensionFisica by remember { mutableStateOf<Int?>(null) }     // 1 al 5
    var claridadMental by remember { mutableStateOf<Int?>(null) }    // 1 al 5
    var nivelEnergia by remember { mutableStateOf<Int?>(null) }      // 1 al 5
    var floresMonitoreoGuardadas by remember { mutableStateOf(false) }

    // Validación: Habilitar botón de monitoreo solo si los 3 están respondidos
    val monitoreoCompleto = tensionFisica != null && claridadMental != null && nivelEnergia != null

    // 3. Preguntas de Acompañamiento
    val preguntasReflexion = listOf(
        "¿Qué pensamiento te pesó más hoy?",
        "¿Pudiste tomarte 10 minutos para ti?",
        "¿Necesitas silencio o desahogarte?",
        "¿Reconociste tus propios límites?"
    )
    val respuestasPreguntas = remember { mutableStateMapOf<Int, String>() }
    var floresPreguntasGuardadas by remember { mutableStateOf(false) }
    val hayPreguntaRespondida = respuestasPreguntas.values.any { it.isNotBlank() }

    // 4. Bitácora Confidencial
    var textoBitacora by remember { mutableStateOf("") }
    var floresBitacoraGuardadas by remember { mutableStateOf(false) }

    // ── Servidor: lo que ya registró hoy, el valor de las flores y el historial de la bitácora ──
    val scope = rememberCoroutineScope()
    var guardando by remember { mutableStateOf(false) }
    var aviso by remember { mutableStateOf<String?>(null) }
    var versionBitacora by remember { mutableIntStateOf(0) }
    var entradaPorBorrar by remember { mutableStateOf<String?>(null) }

    val reglasCarga = rememberCarga { ProgresoRepositorio.reglas() }
    val reglas = (reglasCarga.estado as? Carga.Lista)?.datos?.reglas ?: emptyMap()
    fun fl(clave: String, porDefecto: Int) = reglas[clave] ?: porDefecto

    val hoyCarga = rememberCarga { BienestarRepositorio.hoy() }
    val hoy = (hoyCarga.estado as? Carga.Lista)?.datos
    val historial = rememberCarga(versionBitacora) { BienestarRepositorio.historialBitacora() }

    // Al llegar, se rellena lo que ya guardó hoy (y se marcan los bloques ya hechos)
    LaunchedEffect(hoy) {
        if (hoy != null) {
            hoy.emocion?.let { emocionSeleccionada = it }
            if (hoy.tension != null && hoy.claridad != null && hoy.energia != null) {
                tensionFisica = hoy.tension; claridadMental = hoy.claridad; nivelEnergia = hoy.energia
            }
            hoy.respuestas?.forEachIndexed { i, t -> if (t.isNotBlank()) respuestasPreguntas[i] = t }
            textoBitacora = hoy.bitacora ?: ""
            floresEmocionGuardadas = hoy.emocion != null
            floresMonitoreoGuardadas = hoy.tension != null
            floresPreguntasGuardadas = hoy.respuestas != null
            floresBitacoraGuardadas = hoy.bitacora != null
        }
    }

    fun guardar(accion: suspend () -> ResultadoApi<FloresResponse>, alExito: () -> Unit) {
        if (guardando) return
        guardando = true
        scope.launch {
            when (val r = accion()) {
                is ResultadoApi.Exito -> {
                    alExito()
                    aviso = if (r.datos.floresGanadas > 0) "🌸 ¡+${r.datos.floresGanadas} flores sumadas para Lilu!"
                    else "Guardado. Hoy ya ganaste las flores de este bloque; mañana podrás ganarlas otra vez."
                }
                is ResultadoApi.Error -> aviso = r.mensaje
            }
            guardando = false
        }
    }

    CrisalidaScaffold(
        currentTitle = "Bienestar emocional",
        selectedTab = 3,
        onNavigate = onNavigate
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(ColorFondoApp)
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(vertical = 24.dp)
        ) {
            // TÍTULO Y DESCRIPCIÓN
            item {
                Text(
                    text = "Bienestar emocional",
                    fontFamily = OutfitFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    color = ColorTextoPrincipal
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Tu sentir es completamente válido. Este espacio es privado y no juzga. Lo que escribes se guarda cifrado y solo se muestra en tu cuenta.",
                    fontFamily = JakartaSansFont,
                    fontSize = 14.sp,
                    color = ColorTextoPrincipal,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // ==========================================
            // BLOQUE 1: CÓMO TE SIENTES (+5 FLORES)
            // ==========================================
            item {
                Text(
                    text = "¿Cómo te sientes en este momento?",
                    fontFamily = JakartaSansFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = ColorTextoPrincipal
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Busca tu emoción (ej. triste, cansada...)", fontFamily = JakartaSansFont, fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar emoción", tint = ColorPlaceholder) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = ColorBotonEntrar,
                        unfocusedBorderColor = ColorCamposFondo
                    ),
                    textStyle = TextStyle(fontFamily = JakartaSansFont, fontSize = 15.sp, color = ColorTextoPrincipal)
                )
                Spacer(modifier = Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    emocionesFiltradas.chunked(2).forEach { fila ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            fila.forEach { item ->
                                val isSelected = emocionSeleccionada == item.nombre
                                Surface(
                                    onClick = { emocionSeleccionada = item.nombre },
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (isSelected) ColorBotonEntrar else Color.White,
                                    shadowElevation = 1.dp,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = item.emoji, fontSize = 20.sp)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = item.nombre,
                                            fontFamily = JakartaSansFont,
                                            fontSize = 14.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color.White else ColorTextoPrincipal
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

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (floresEmocionGuardadas) "🌸 ¡+${fl("bienestar_emocion", 5)} flores sumadas para Lilu!" else "Gana ${fl("bienestar_emocion", 5)} flores para tu mascota",
                        fontFamily = JakartaSansFont,
                        fontSize = 12.sp,
                        color = if (floresEmocionGuardadas) Color(0xFF2E7D32) else ColorPlaceholder,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = {
                            val e = emocionSeleccionada
                            if (e != null) guardar({ BienestarRepositorio.guardarEmocion(e) }) { floresEmocionGuardadas = true }
                        },
                        enabled = emocionSeleccionada != null && !guardando,
                        colors = ButtonDefaults.buttonColors(containerColor = if (floresEmocionGuardadas) Color(0xFF2E7D32) else ColorBotonEntrar),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Text(text = if (floresEmocionGuardadas) "Guardado ✓" else "Guardar emoción", fontFamily = JakartaSansFont, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // ========================================================
            // BLOQUE 2: MONITOREO CORPORAL Y EMOCIONAL (+3 FLORES)
            // ========================================================
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Monitoreo corporal y emocional",
                            fontFamily = OutfitFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = ColorTextoPrincipal
                        )
                        Text(
                            text = "Evalúa tus sensaciones actuales en las tres dimensiones:",
                            fontFamily = JakartaSansFont,
                            fontSize = 12.sp,
                            color = ColorPlaceholder
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        // 1. Tensión corporal
                        Text(
                            text = "1. Tensión en hombros y cuerpo",
                            fontFamily = JakartaSansFont,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorTextoPrincipal
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        EscalaConCaritas(
                            seleccionActual = tensionFisica,
                            onSelect = { tensionFisica = it }
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // 2. Claridad mental
                        Text(
                            text = "2. Claridad mental",
                            fontFamily = JakartaSansFont,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorTextoPrincipal
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        EscalaConCaritas(
                            seleccionActual = claridadMental,
                            onSelect = { claridadMental = it }
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // 3. Energía física
                        Text(
                            text = "3. Nivel de energía disponible",
                            fontFamily = JakartaSansFont,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorTextoPrincipal
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        EscalaConCaritas(
                            seleccionActual = nivelEnergia,
                            onSelect = { nivelEnergia = it }
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (floresMonitoreoGuardadas) "🌸 ¡+${fl("bienestar_monitoreo", 3)} flores sumadas para Lilu!" else "Gana ${fl("bienestar_monitoreo", 3)} flores (responde las 3)",
                                fontFamily = JakartaSansFont,
                                fontSize = 11.sp,
                                color = if (floresMonitoreoGuardadas) Color(0xFF2E7D32) else ColorPlaceholder,
                                fontWeight = FontWeight.Bold
                            )
                            Button(
                                onClick = {
                                    val t = tensionFisica; val c = claridadMental; val n = nivelEnergia
                                    if (t != null && c != null && n != null) {
                                        guardar({ BienestarRepositorio.guardarMonitoreo(t, c, n) }) { floresMonitoreoGuardadas = true }
                                    }
                                },
                                enabled = monitoreoCompleto && !guardando, // Habilitado SOLO si respondió los 3
                                colors = ButtonDefaults.buttonColors(containerColor = if (floresMonitoreoGuardadas) Color(0xFF2E7D32) else ColorBotonEntrar),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(38.dp)
                            ) {
                                Text(text = if (floresMonitoreoGuardadas) "Guardado ✓" else "Guardar monitoreo", fontFamily = JakartaSansFont, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // ==========================================
            // BLOQUE 3: PREGUNTAS DE ACOMPAÑAMIENTO (+8 FLORES)
            // ==========================================
            item {
                Text(
                    text = "Preguntas de acompañamiento",
                    fontFamily = OutfitFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = ColorTextoPrincipal
                )
                Text(
                    text = "Responde los pensamientos que resuenen en este momento:",
                    fontFamily = JakartaSansFont,
                    fontSize = 12.sp,
                    color = ColorPlaceholder
                )
                Spacer(modifier = Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    preguntasReflexion.forEachIndexed { index, pregunta ->
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = pregunta,
                                    fontFamily = JakartaSansFont,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorTextoPrincipal
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = respuestasPreguntas[index] ?: "",
                                    onValueChange = { respuestasPreguntas[index] = it },
                                    placeholder = { Text("Escribe tu reflexión...", fontFamily = JakartaSansFont, fontSize = 13.sp) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color(0xFFFAF8F8),
                                        unfocusedContainerColor = Color(0xFFFAF8F8),
                                        focusedBorderColor = ColorBotonEntrar,
                                        unfocusedBorderColor = ColorCamposFondo
                                    ),
                                    textStyle = TextStyle(fontFamily = JakartaSansFont, fontSize = 14.sp, color = ColorTextoPrincipal)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (floresPreguntasGuardadas) "🌸 ¡+${fl("bienestar_reflexion", 8)} flores sumadas para Lilu!" else "Gana ${fl("bienestar_reflexion", 8)} flores (responde al menos 1)",
                        fontFamily = JakartaSansFont,
                        fontSize = 11.sp,
                        color = if (floresPreguntasGuardadas) Color(0xFF2E7D32) else ColorPlaceholder,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = {
                            if (hayPreguntaRespondida) {
                                val respuestas = preguntasReflexion.indices.map { respuestasPreguntas[it].orEmpty() }
                                guardar({ BienestarRepositorio.guardarReflexion(respuestas) }) { floresPreguntasGuardadas = true }
                            }
                        },
                        enabled = hayPreguntaRespondida && !guardando,
                        colors = ButtonDefaults.buttonColors(containerColor = if (floresPreguntasGuardadas) Color(0xFF2E7D32) else ColorBotonEntrar),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Text(text = if (floresPreguntasGuardadas) "Guardado ✓" else "Guardar reflexión", fontFamily = JakartaSansFont, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // ==========================================
            // BLOQUE 4: BITÁCORA CONFIDENCIAL (+12 FLORES)
            // ==========================================
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Bitácora confidencial",
                            fontFamily = OutfitFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = ColorTextoPrincipal
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = textoBitacora,
                            onValueChange = { textoBitacora = it },
                            placeholder = {
                                Text(
                                    "Si las palabras quieren salir, déjalas rodar sin corregirte ni juzgarte...",
                                    fontFamily = JakartaSansFont,
                                    fontSize = 14.sp,
                                    color = ColorPlaceholder
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFFAF8F8),
                                unfocusedContainerColor = Color(0xFFFAF8F8),
                                focusedBorderColor = ColorBotonEntrar,
                                unfocusedBorderColor = ColorCamposFondo
                            ),
                            textStyle = TextStyle(fontFamily = JakartaSansFont, fontSize = 15.sp, color = ColorTextoPrincipal)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (floresBitacoraGuardadas) "🌸 ¡+${fl("bienestar_bitacora", 12)} flores sumadas!" else "Gana ${fl("bienestar_bitacora", 12)} flores",
                                fontFamily = JakartaSansFont,
                                fontSize = 12.sp,
                                color = if (floresBitacoraGuardadas) Color(0xFF2E7D32) else ColorPlaceholder,
                                fontWeight = FontWeight.Bold
                            )
                            Button(
                                onClick = {
                                    if (textoBitacora.isNotBlank()) {
                                        guardar({ BienestarRepositorio.guardarBitacora(textoBitacora) }) {
                                            floresBitacoraGuardadas = true
                                            versionBitacora++
                                        }
                                    }
                                },
                                enabled = textoBitacora.isNotBlank() && !guardando,
                                colors = ButtonDefaults.buttonColors(containerColor = if (floresBitacoraGuardadas) Color(0xFF2E7D32) else ColorBotonEntrar),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.height(42.dp)
                            ) {
                                Text(
                                    text = if (floresBitacoraGuardadas) "Guardado ✓" else "Guardar bitácora",
                                    fontFamily = OutfitFont,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Se guarda cifrada en nuestro servidor y solo se muestra cuando entras a tu cuenta con tu PIN. Puedes borrar cualquier entrada cuando quieras.",
                            fontFamily = JakartaSansFont,
                            fontSize = 11.sp,
                            color = ColorPlaceholder,
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // ==========================================
            // MIS ENTRADAS ANTERIORES (la usuaria puede releer y borrar su bitácora)
            // ==========================================
            item {
                Text(
                    text = "Mis entradas anteriores",
                    fontFamily = OutfitFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = ColorTextoPrincipal
                )
                Spacer(modifier = Modifier.height(8.dp))
                when (val e = historial.estado) {
                    is Carga.Cargando -> IndicadorCargando()
                    is Carga.Falla -> MensajeError(e.mensaje, historial.recargar)
                    is Carga.Lista -> {
                        if (e.datos.entradas.isEmpty()) {
                            Text(
                                text = "Aún no tienes entradas guardadas.",
                                fontFamily = JakartaSansFont, fontSize = 13.sp, color = ColorPlaceholder
                            )
                        }
                        e.datos.entradas.forEach { entrada ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = fechaLegible(entrada.dia),
                                            fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp, color = ColorPlaceholder
                                        )
                                        TextButton(onClick = { entradaPorBorrar = entrada.dia }) {
                                            Text("Borrar", fontFamily = JakartaSansFont, fontSize = 12.sp, color = ColorBotonEntrar)
                                        }
                                    }
                                    Text(
                                        text = entrada.texto,
                                        fontFamily = JakartaSansFont, fontSize = 14.sp,
                                        color = ColorTextoPrincipal, lineHeight = 19.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    aviso?.let { texto ->
        AlertDialog(
            onDismissRequest = { aviso = null },
            text = { Text(text = texto, fontFamily = JakartaSansFont, color = ColorTextoPrincipal) },
            confirmButton = {
                TextButton(onClick = { aviso = null }) {
                    Text("Entendido", fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, color = ColorBotonEntrar)
                }
            },
            containerColor = Color.White
        )
    }

    entradaPorBorrar?.let { dia ->
        AlertDialog(
            onDismissRequest = { entradaPorBorrar = null },
            title = { Text("¿Borrar esta entrada?", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, color = ColorTextoPrincipal) },
            text = { Text("Se elimina para siempre. Las flores que ya ganaste se conservan.", fontFamily = JakartaSansFont, color = ColorTextoPrincipal) },
            confirmButton = {
                Button(
                    onClick = {
                        entradaPorBorrar = null
                        scope.launch {
                            when (val r = BienestarRepositorio.borrarEntrada(dia)) {
                                is ResultadoApi.Exito -> { versionBitacora++; hoyCarga.recargar() }
                                is ResultadoApi.Error -> aviso = r.mensaje
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ColorBotonEntrar)
                ) { Text("Borrar", color = Color.White) }
            },
            dismissButton = { TextButton(onClick = { entradaPorBorrar = null }) { Text("Cancelar", color = ColorBotonEntrar) } },
            containerColor = Color.White
        )
    }
}

/** "2026-11-02" → "2 de noviembre de 2026" */
private fun fechaLegible(dia: String): String = try {
    val fecha = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(dia)
    if (fecha == null) dia else SimpleDateFormat("d 'de' MMMM 'de' yyyy", Locale("es", "MX")).format(fecha)
} catch (e: Exception) {
    dia
}

/** Componente reutilizable para mostrar las escalas 1 al 5 con sus respectivas caritas en los 3 apartados de monitoreo */
@Composable
fun EscalaConCaritas(
    seleccionActual: Int?,
    onSelect: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val niveles = listOf(
            Triple(1, "1", "😁"),
            Triple(2, "2", "🙂"),
            Triple(3, "3", "😐"),
            Triple(4, "4", "🙁"),
            Triple(5, "5", "😵")
        )
        niveles.forEach { (num, label, emoji) ->
            val selected = seleccionActual == num
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (selected) ColorTextoPrincipal else ColorCamposFondo.copy(alpha = 0.4f))
                    .clickable { onSelect(num) }
                    .padding(vertical = 8.dp, horizontal = 4.dp)
                    .width(56.dp)
            ) {
                Text(text = emoji, fontSize = 20.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = label,
                    fontFamily = OutfitFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (selected) Color.White else ColorTextoPrincipal
                )
            }
        }
    }
}