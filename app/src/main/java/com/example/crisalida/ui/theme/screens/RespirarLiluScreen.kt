package com.example.crisalida.ui.theme.screens

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.platform.LocalConfiguration
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.crisalida.data.GuiaRespiracion
import com.example.crisalida.data.Haptica
import com.example.crisalida.data.debeHablar
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crisalida.data.FrasesRepositorio
import com.example.crisalida.ui.theme.*
import com.example.crisalida.ui.theme.screens.rememberProgreso
import com.example.crisalida.ui.theme.screens.Carga
import com.example.crisalida.ui.theme.screens.CrisalidaScaffold
import com.example.crisalida.ui.theme.Paleta
private val MkTitulo: Color get() = Paleta.titulo
private val MkSubtitulo: Color get() = Paleta.textoGris
private val MkFondoBotonVolver: Color get() = Paleta.fondoBotonVolver
private val MkFondoTarjetaAyuda: Color get() = Paleta.fondoAyuda
private val MkTextoAyudaGris: Color get() = Paleta.textoAyuda

// Tiempos de la respiración (milisegundos): inhalar 4 s y exhalar 6 s (la exhalación más larga relaja).
// La animación de Lilu, la vibración y la voz usan ESTOS mismos valores, así van siempre a la par.
private const val INHALA_MS = 4000
private const val EXHALA_MS = 6000

@Composable
fun RespirarLiluScreen(
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    //animación sutil de respiración/pulsación para la imagen de Lilu
    val infiniteTransition = rememberInfiniteTransition(label = "lilu_anim")
    val scaleAnim by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 0.97f,
        animationSpec = infiniteRepeatable(
            // un ciclo completo: sube en INHALA_MS y baja en EXHALA_MS
            animation = keyframes {
                durationMillis = INHALA_MS + EXHALA_MS
                1.03f at INHALA_MS using FastOutSlowInEasing
                0.97f at INHALA_MS + EXHALA_MS using FastOutSlowInEasing
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "escala_lilu"
    )

    // ── Respiración guiada (10.2): vibración + voz al ritmo de la animación ──
    GuiaRespiracion.cargar(context)
    Haptica.cargar(context)
    var guiando by remember { mutableStateOf(false) }
    var fase by remember { mutableStateOf<String?>(null) }   // «Inhala» o «Exhala» mientras se guía
    val escalaGuia = remember { Animatable(0.97f) }
    val lector = rememberLectorVoz()   // se detiene solo al salir de la pantalla y al pasar a segundo plano
    val hablar by rememberUpdatedState(debeHablar(GuiaRespiracion.voz, Haptica.enCamuflaje))   // se lee al instante si cambia el interruptor

    // al pasar la app a segundo plano (botón de inicio, pantalla apagada) se pausa todo
    val ciclo = (LocalActivity.current as? ComponentActivity)?.lifecycle
    DisposableEffect(ciclo) {
        val observador = LifecycleEventObserver { _, evento -> if (evento == Lifecycle.Event.ON_STOP) guiando = false }
        ciclo?.addObserver(observador)
        onDispose { ciclo?.removeObserver(observador) }
    }

    LaunchedEffect(guiando) {
        if (!guiando) return@LaunchedEffect
        try {
            escalaGuia.snapTo(0.97f)
            while (true) {
                fase = "Inhala"
                if (hablar) lector.leer(listOf("Inhala"))
                escalaGuia.animateTo(1.03f, tween(INHALA_MS, easing = FastOutSlowInEasing))
                Haptica.finDeInhalar()   // se acabó el turno de inhalar: un toque
                fase = "Exhala"
                if (hablar) lector.leer(listOf("Exhala"))
                escalaGuia.animateTo(0.97f, tween(EXHALA_MS, easing = FastOutSlowInEasing))
                Haptica.finDeExhalar()   // se acabó el turno de exhalar: dos toques
            }
        } finally {
            // Pausar, salir de la pantalla o segundo plano: se corta la vibración y la voz de inmediato
            Haptica.cancelar()
            lector.detener()
            fase = null
        }
    }

    //animación flotante suave para las frases de apoyo
    val floatAnim by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flotar_frases"
    )
    // en horizontal casi no hay alto: Lilu se hace más chica para que no ocupe toda la pantalla
    val compacto = LocalConfiguration.current.screenHeightDp < 480
    val progreso = rememberProgreso()
    val lilu = (progreso.estado as? Carga.Lista)?.datos?.lilu
    val etapa = lilu?.etapa
    // Las frases se piden al servidor cada vez que se abre la pantalla (al azar, según el estado de Lilu).
    // Si no hay conexión o el servidor falla, se usan las frases guardadas en la app: nunca queda vacía.
    val frasesServidor = rememberCarga { soloVendedora { FrasesRepositorio.apoyo() } }
    val respuestaFrases = (frasesServidor.estado as? Carga.Lista)?.datos
    val reposo = lilu?.reposo == true || respuestaFrases?.estado == "reposo"
    val frases: List<String> = respuestaFrases?.frases?.map { it.texto.trim() }?.filter { it.isNotEmpty() }
        ?.takeIf { it.isNotEmpty() } ?: frasesDeRespaldo(reposo)
    CrisalidaScaffold(
        currentTitle = "Respirar con Lilu",
        selectedTab = 4,
        onNavigate = onNavigate
    ) { innerPadding ->
        ContenidoCentrado(innerPadding) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                //botón superior izquierdo para volver a screen de mascota
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Surface(
                        onClick = { onNavigate("mascota") },
                        shape = RoundedCornerShape(18.dp),
                        color = MkFondoBotonVolver,
                        shadowElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = MkTitulo,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Mascota",
                                fontFamily = OutfitFont,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = MkTitulo
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))


                Box(
                    modifier = Modifier
                        .size(if (compacto) 150.dp else 240.dp)
                        .graphicsLayer(
                            scaleX = if (guiando) escalaGuia.value else scaleAnim,
                            scaleY = if (guiando) escalaGuia.value else scaleAnim
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = imagenLilu(etapa, reposo)),
                        contentDescription = if (reposo) "Lilu durmiendo" else "Lilu emocionada",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = saludoRespirarLilu(reposo),
                    fontFamily = OutfitFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    color = MkTitulo,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                GuiaDeRespiracion(
                    guiando = guiando,
                    fase = fase,
                    vozActiva = GuiaRespiracion.voz,
                    vozNoDisponible = GuiaRespiracion.voz && lector.estado == EstadoVoz.NO_DISPONIBLE,
                    vibracionApagada = !Haptica.activa,
                    onAlternar = { guiando = !guiando },
                    onCambioVoz = { GuiaRespiracion.cambiarVoz(context, it) }
                )

                Spacer(modifier = Modifier.height(28.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer(translationY = floatAnim),
                    verticalArrangement = Arrangement.spacedBy(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    frases.forEachIndexed { i, frase ->
                        FraseFlotante(
                            texto = frase,
                            align = when (i % 4) { 0 -> TextAlign.Start; 1 -> TextAlign.End; 2 -> TextAlign.Start; else -> TextAlign.Center }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(36.dp))

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MkFondoTarjetaAyuda),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = "Si necesitas ayuda, recuerda que Guanajuato cuenta con líneas de atención gratuitas, confidenciales y especializadas que operan las 24 horas del día, además de los números nacionales de emergencia.",
                            fontFamily = JakartaSansFont,
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            color = MkTextoAyudaGris
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        //lista de números interactivos
                        BotonTelefonoAyuda(numero = "9-1-1", descripcion = "Emergencias en curso", context = context)
                        Spacer(modifier = Modifier.height(8.dp))
                        BotonTelefonoAyuda(numero = "0-7-5", descripcion = "Línea de Atención a las Mujeres", context = context)
                        Spacer(modifier = Modifier.height(8.dp))
                        BotonTelefonoAyuda(numero = "473 164 0369", descripcion = "WhatsApp de Apoyo a Mujeres", context = context)
                        Spacer(modifier = Modifier.height(8.dp))
                        BotonTelefonoAyuda(numero = "0-8-9", descripcion = "Denuncia Anónima", context = context)
                        Spacer(modifier = Modifier.height(8.dp))
                        BotonTelefonoAyuda(numero = "800 290 0024", descripcion = "Línea de salud mental", context = context)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

// Tarjeta de la respiración guiada: botón Empezar/Pausar, interruptor de voz y la fase actual
@Composable
private fun GuiaDeRespiracion(
    guiando: Boolean,
    fase: String?,
    vozActiva: Boolean,
    vozNoDisponible: Boolean,
    vibracionApagada: Boolean,
    onAlternar: () -> Unit,
    onCambioVoz: (Boolean) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MkFondoTarjetaAyuda),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Respiración guiada",
                fontFamily = OutfitFont,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = MkTitulo
            )
            Text(
                text = "Sigue a Lilu con los ojos cerrados: inhala 4 s y exhala 6 s. Un toque suave avisa cuando termina el turno de inhalar y dos cuando termina el de exhalar.",
                fontFamily = JakartaSansFont,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                textAlign = TextAlign.Center,
                color = MkTextoAyudaGris,
                modifier = Modifier.padding(top = 4.dp)
            )
            // Qué toca ahora (sin anuncio automático de TalkBack: ya habla la voz opcional)
            Text(
                text = fase ?: " ",
                fontFamily = OutfitFont,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                color = MkTitulo,
                modifier = Modifier.padding(vertical = 10.dp)
            )
            Button(
                onClick = onAlternar,
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MkTitulo)
            ) {
                Text(
                    text = if (guiando) "Pausar" else "Empezar",
                    fontFamily = JakartaSansFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Paleta.blanco
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .toggleable(value = vozActiva, onValueChange = onCambioVoz, role = Role.Switch),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Guía por voz",
                    fontFamily = OutfitFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Paleta.textoNegro,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = vozActiva,
                    onCheckedChange = null,   // el cambio lo maneja toda la fila
                    colors = SwitchDefaults.colors(checkedTrackColor = MkTitulo)
                )
            }
            if (vozNoDisponible) {
                Text(
                    text = "Tu teléfono no tiene una voz en español: seguirás con la animación y la vibración.",
                    fontFamily = JakartaSansFont, fontSize = 12.sp, lineHeight = 17.sp, color = MkTextoAyudaGris
                )
            }
            if (vibracionApagada) {
                Text(
                    text = "La vibración está apagada. Puedes activarla en Configuración → Sonidos y vibración.",
                    fontFamily = JakartaSansFont, fontSize = 12.sp, lineHeight = 17.sp, color = MkTextoAyudaGris,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

// Frases guardadas en la app: se usan solo si el servidor no responde (son las de siempre; en reposo, unas sin reproche)
private fun frasesDeRespaldo(reposo: Boolean): List<String> =
    if (reposo) listOf(
        "Qué bueno verte. Aquí estoy, sin prisas.",
        "Descansar también es cuidarte.",
        "Puedes ir a tu ritmo. Lilu va contigo.",
        "Mereces un momento de calma, hoy y cuando quieras."
    ) else listOf(
        "No eres lo que te ha tocado vivir, eres lo que eres capaz de superar",
        "Tu fuerza no se mide por lo que aguantas en silencio, sino por tu valentía para buscar paz y libertad",
        "Tienes derecho a que todos los minutos de tu vida sean de libertad, de felicidad y de paz",
        "Permítete no tener todas las respuestas resueltas ahora. La independencia se construye también en los días donde simplemente eliges cuidarte y respirar"
    )

//cada frase de apoyo
@Composable
fun FraseFlotante(texto: String, align: TextAlign) {
    Text(
        text = texto,
        fontFamily = OutfitFont,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
        textAlign = align,
        color = MkSubtitulo,
        modifier = Modifier
            .fillMaxWidth(0.9f)
            .padding(horizontal = 8.dp)
    )
}

@Composable
fun BotonTelefonoAyuda(
    numero: String,
    descripcion: String,
    context: android.content.Context
) {
    Surface(
        onClick = {
            val limpiarNumero = numero.replace(Regex("[^0-9+]"), "")
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$limpiarNumero")
            }
            context.startActivity(intent)
        },
        shape = RoundedCornerShape(10.dp),
        color = Paleta.blanco.copy(alpha = 0.85f),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Phone,
                contentDescription = "Llamar",
                tint = MkTitulo,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                modifier = Modifier.weight(1f),
                text = "$numero ($descripcion)",
                fontFamily = JakartaSansFont,
                fontWeight = FontWeight.Bold,
                fontSize = 13.5.sp,
                color = MkTitulo
            )
        }
    }
}