package com.example.crisalida.ui.theme.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.LockClock
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crisalida.R
import com.example.crisalida.ui.theme.*
import com.example.crisalida.data.FotoRemota
import com.example.crisalida.data.ProgresoRepositorio
import com.example.crisalida.data.ResultadoApi
import com.example.crisalida.data.RetosRepositorio
import com.example.crisalida.data.SesionActual
import com.example.crisalida.data.RetoDto
import kotlinx.coroutines.launch
import com.example.crisalida.ui.screens.CrisalidaScaffold


private val MkFondo = Color(0xFFF2F0EF)
private val MkTitulo = Color(0xFF7F284F)
private val MkSubtitulo = Color(0xFF545454)
private val MkMorado = Color(0xFFA47DAB)
private val MkVerde = Color(0xFF005524)
private val MkVerdeChip = Color(0xFF007032)
private val MkTextoOscuro = Color(0xFF1E1B1D)
private val MkTextoSuave = Color(0xFF544247)
private val MkSuperficieSuave = Color(0xFFF9F2F4)
private val MkChipFondo = Color(0xFFFBD7FF)
private val MkChipTexto = Color(0xFF5C3A64)
private val MkTrack = Color(0xFFE8E1E3)
private val MargenIzqArriba = 19.dp
private val MargenDerArriba = 12.dp
private val MargenTarjetas = 15.dp

private val TamanoMascotaGrande = 410.dp

@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit,
    onAbrirReto: (String) -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    //datos del servidor: progreso (nivel y flores) y un microrreto abierto
    val progreso = rememberProgreso()
    val datos = (progreso.estado as? Carga.Lista)?.datos
    val retoCarga = rememberCarga(ProgresoRepositorio.version) { RetosRepositorio.tablero() }
    val reglas = (rememberCarga { ProgresoRepositorio.reglas() }.estado as? Carga.Lista)?.datos?.reglas
    val floresBitacora = reglas?.get("bienestar_bitacora") ?: 12
    val reto: RetoDto? = (retoCarga.estado as? Carga.Lista)?.datos?.retos?.firstOrNull()
    var aceptando by remember { mutableStateOf(false) }
    var avisoReto by remember { mutableStateOf<String?>(null) }

    CrisalidaScaffold(
        currentTitle = "Inicio",
        selectedTab = 2,
        onNavigate = onNavigate
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ColorFondoApp)
                .padding(innerPadding)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            //saludo
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = MargenIzqArriba, end = MargenDerArriba)
            ) {
                Text(
                    text = "Hola, ${SesionActual.seudonimo}",
                    fontFamily = JakartaSansFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = MkTitulo
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Bienvenida a tu espacio seguro, privado e impenetrable",
                    fontFamily = JakartaSansFont,
                    fontWeight = FontWeight.Normal,
                    fontSize = 18.sp,
                    lineHeight = 19.sp,
                    color = MkSubtitulo
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            //bitácora, lleva a Bienestar
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MkFondo),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = MargenIzqArriba, end = MargenDerArriba)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onNavigate("bienestar") }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MkChipFondo),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.EditNote,
                            contentDescription = null,
                            tint = MkChipTexto,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Tu bitácora de hoy",
                            fontFamily = JakartaSansFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MkTitulo
                        )
                        Text(
                            text = "Escribe lo que traes en la cabeza o en el corazón, sin presiones. " +
                                "Es privada y te da $floresBitacora flores la primera vez del día.",
                            fontFamily = JakartaSansFont,
                            fontSize = 13.sp,
                            lineHeight = 17.sp,
                            color = MkTextoSuave
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MkTitulo,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = { onNavigate("bienestar") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = MargenIzqArriba, end = MargenDerArriba)
                    .heightIn(min = 41.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MkMorado),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Ir a bienestar emocional",
                    fontFamily = JakartaSansFont,
                    fontWeight = FontWeight.Normal,
                    fontSize = 15.sp,
                    color = Color.White
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(412.dp)
                    .clipToBounds(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.mascotagrande),
                    contentDescription = "Mascota Lilu",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.requiredSize(TamanoMascotaGrande)
                )
            }
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MargenTarjetas)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 13.dp, top = 8.dp, end = 8.dp)
                    ) {
                        // Mascota pequeña + chip "Sereno"
                        Box(modifier = Modifier.width(58.dp).height(81.dp)) {
                            Image(
                                painter = painterResource(id = when (datos?.lilu?.etapa) { 2 -> R.drawable.lilu_larva; 3 -> R.drawable.lilu_mariposa; else -> R.drawable.lilu_huevo }),
                                contentDescription = "Lilu sereno",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .size(width = 58.dp, height = 60.dp)
                            )
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .offset(x = 29.dp)
                                    .clip(CircleShape)
                                    .background(MkVerdeChip)
                                    .padding(horizontal = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.WbSunny,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(10.dp)
                                )
                                Spacer(modifier = Modifier.width(1.dp))
                                Text(
                                    text = "Sereno",
                                    fontFamily = JakartaSansFont,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(15.dp))

                        Column(modifier = Modifier.width(400.dp)) {
                            Text(
                                text = "REFUGIO AMABLE",
                                fontFamily = OutfitFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                lineHeight = 14.sp,
                                color = MkTitulo
                            )
                            Text(
                                text = "«Tu esfuerzo nunca caduca. Hoy tu único trabajo es dar un respiro a tu ritmo».",
                                fontFamily = JakartaSansFont,
                                fontSize = 13.sp,
                                lineHeight = 21.sp,
                                color = MkTextoOscuro
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(15.dp))
                    //caja de nivel
                    Column(
                        modifier = Modifier
                            .padding(start = 8.dp, end = 8.dp, bottom = 10.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MkSuperficieSuave)
                            .padding(start = 5.dp, end = 5.dp, top = 5.dp, bottom = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = datos?.let { "Nivel ${it.nivel.nivel}:\n${it.nivel.nombre}" } ?: "Nivel…",
                                fontFamily = JakartaSansFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                lineHeight = 13.sp,
                                color = MkTextoOscuro
                            )
                            Row(
                                modifier = Modifier.padding(end = 17.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Eco,
                                    contentDescription = null,
                                    tint = MkVerde,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = datos?.lilu?.let { l ->
                                        if (l.floresSiguiente != null) "${l.flores} / ${l.floresSiguiente}\nFlores" else "${l.flores}\nFlores"
                                    } ?: "…\nFlores",
                                    fontFamily = JakartaSansFont,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    lineHeight = 13.sp,
                                    color = MkVerde
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .clip(CircleShape)
                                .background(MkTrack)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(((datos?.lilu?.porcentaje ?: 0) / 100f).coerceIn(0f, 1f))
                                    .fillMaxHeight()
                                    .background(MkVerde)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            modifier = Modifier.heightIn(min = 40.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.LockClock,
                                contentDescription = null,
                                tint = MkVerde,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "Cero pérdida de progreso",
                                fontFamily = JakartaSansFont,
                                fontSize = 13.sp,
                                color = MkTextoSuave
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(9.dp))

            Button(
                onClick = { onNavigate("mascota") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MargenTarjetas)
                    .heightIn(min = 41.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MkMorado),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Ir con Lilu",
                    fontFamily = JakartaSansFont,
                    fontWeight = FontWeight.Normal,
                    fontSize = 15.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Tarjeta Micro-reto
            if (reto == null) {
                when (val e = retoCarga.estado) {
                    is Carga.Cargando -> IndicadorCargando()
                    is Carga.Falla -> MensajeError(e.mensaje, retoCarga.recargar)
                    is Carga.Lista -> Text(
                        text = "Por ahora no hay microrretos abiertos. ¡Vuelve pronto!",
                        modifier = Modifier.padding(horizontal = MargenTarjetas),
                        fontFamily = JakartaSansFont, fontSize = 14.sp, color = MkTextoSuave
                    )
                }
            } else Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MargenTarjetas)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MkSuperficieSuave)
                    .clickable { onAbrirReto(reto.retoId) }
                    .padding(start = 5.dp, end = 5.dp, top = 5.dp)
            ) {
                //etiqueta de complejidad y duración aprox
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MkChipFondo)
                        .padding(horizontal = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Timer,
                        contentDescription = null,
                        tint = MkChipTexto,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(1.dp))
                    Text(
                        text = "${when (reto?.dificultad) { "facil" -> "Fácil"; "media" -> "Media"; else -> "Difícil" }} • ${reto?.duracionMin ?: 0} min",
                        fontFamily = JakartaSansFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = MkChipTexto
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    //foto del reto, si no tiene, o mientras carga, se ve el marcador
                    FotoRemota(
                        ruta = reto.imagenRuta,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(width = 80.dp, height = 78.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        alternativa = {
                            Box(
                                modifier = Modifier
                                    .size(width = 80.dp, height = 78.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(ColorCamposFondo),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Image,
                                    contentDescription = null,
                                    tint = MkTextoSuave,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = reto?.titulo ?: "",
                            fontFamily = OutfitFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MkTextoOscuro,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = reto?.descripcion ?: "",
                            fontFamily = JakartaSansFont,
                            fontSize = 15.sp,
                            color = MkTextoSuave,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "+" + formatoPrecio(reto?.compensacion ?: 0.0) + " MXN",
                            fontFamily = JakartaSansFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MkVerde
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = {
                        val r = reto ?: return@Button
                        if (!aceptando) {
                            aceptando = true
                            scope.launch {
                                avisoReto = when (val res = RetosRepositorio.aceptar(r.retoId)) {
                                    is ResultadoApi.Exito -> "¡Reto aceptado! Encuéntralo en la pestaña Retos."
                                    is ResultadoApi.Error -> res.mensaje
                                }
                                aceptando = false
                                retoCarga.recargar()
                            }
                        }
                    },
                    enabled = !aceptando,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 40.dp),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MkVerde),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Bolt,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(19.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "Aceptar Micro-Reto",
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(38.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    avisoReto?.let { texto ->
        AlertDialog(
            onDismissRequest = { avisoReto = null },
            text = { Text(text = texto, fontFamily = JakartaSansFont, color = MkTextoOscuro) },
            confirmButton = {
                TextButton(onClick = { avisoReto = null }) {
                    Text(text = "Entendido", fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, color = MkMorado)
                }
            },
            containerColor = Color.White
        )
    }
}
