package com.example.crisalida.ui.theme.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Face
import androidx.compose.material.icons.outlined.LocalFlorist
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material.icons.outlined.ViewInAr
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crisalida.R
import com.example.crisalida.ui.theme.*
import com.example.crisalida.data.ProgresoRepositorio
import com.example.crisalida.ui.screens.CrisalidaScaffold
private val MkTitulo = Color(0xFF9D4067)
private val MkSubtitulo = Color(0xFF545454)
private val MkVerdeBoton = Color(0xFF1E681E)
private val MkBarraProgreso = Color(0xFFB82C67)
private val MkBarraFondo = Color(0xFFE8D7DD)
private val MkVerdeCeroPerdida = Color(0xFFC7EBC3)
private val MkVerdeCeroPerdidaTexto = Color(0xFF16521A)
private val MkBotonCanjear = Color(0xFF7A2E50)
private val MkIconoCajaFondo = Color(0xFFE3D3DC)
private val MkIconoCajaColor = Color(0xFF6B3045)
private val MkFondoCard = Color(0xFFFFFFFF)

@Composable
fun MascotaScreen(
    onNavigate: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    //datos del servidor: etapa de Lilu, flores y cuánto vale cada acción
    val progreso = rememberProgreso()
    val lilu = (progreso.estado as? Carga.Lista)?.datos?.lilu
    val nivel = (progreso.estado as? Carga.Lista)?.datos?.nivel
    val reglasCarga = rememberCarga { ProgresoRepositorio.reglas() }
    val reglas = (reglasCarga.estado as? Carga.Lista)?.datos?.reglas ?: emptyMap()
    fun flores(clave: String, porDefecto: Int) = reglas[clave] ?: porDefecto
    val maxBienestar = flores("bienestar_emocion", 5) + flores("bienestar_monitoreo", 3) +
        flores("bienestar_reflexion", 8) + flores("bienestar_bitacora", 12)

    CrisalidaScaffold(
        currentTitle = "Mascota",
        selectedTab = 4, //esto es lo que resalta la pestaña donde se encuentra en la barrita de navegación
        onNavigate = onNavigate
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ColorFondoApp)
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 18.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(18.dp))

            if (progreso.estado is Carga.Falla) {
                MensajeError((progreso.estado as Carga.Falla).mensaje, progreso.recargar)
            }

            Text(
                text = "Tu camino a florecer",
                fontFamily = OutfitFont,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                color = MkTitulo
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Cada paso tuyo cuenta. Tu esfuerzo no caduca, se acumula, no hay penalizaciones ni retrocesos.",
                fontFamily = JakartaSansFont,
                fontSize = 13.5.sp,
                lineHeight = 18.sp,
                color = MkSubtitulo
            )

            Spacer(modifier = Modifier.height(20.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MkFondoCard),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEFE6EB)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = when (lilu?.etapa) { 2 -> R.drawable.lilu_larva; 3 -> R.drawable.lilu_mariposa; else -> R.drawable.lilu_huevo }),
                            contentDescription = "Lilu",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = lilu?.let { "Lilu • ${it.etapaNombre}" } ?: "Lilu",
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MkTitulo
                    )
                    Text(
                        text = when (lilu?.etapa) {
                            1 -> "Lilu descansa en su huevo, esperando crecer contigo"
                            2 -> "Lilu ya salió y crece con cada paso tuyo"
                            3 -> "¡Lilu ya tiene alas! Sigue floreciendo con ella"
                            else -> "Cargando a Lilu…"
                        },
                        fontFamily = JakartaSansFont,
                        fontSize = 13.sp,
                        color = MkSubtitulo
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(CircleShape)
                                .background(MkBarraFondo)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(((lilu?.porcentaje ?: 0) / 100f).coerceIn(0f, 1f))
                                    .fillMaxHeight()
                                    .clip(CircleShape)
                                    .background(MkBarraProgreso)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${lilu?.flores ?: 0} flores",
                                fontFamily = JakartaSansFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MkTitulo
                            )
                            Text(
                                text = lilu?.let { if (it.floresSiguiente != null) "Meta: ${it.floresSiguiente} flores (${it.siguienteNombre})" else "¡Etapa máxima!" } ?: "",
                                fontFamily = JakartaSansFont,
                                fontSize = 13.sp,
                                color = MkSubtitulo
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))


                    Button(
                        onClick = { onNavigate("respirar_lilu") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 44.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MkVerdeBoton)
                    ) {
                        Text(
                            text = "Respirar con Lilu",
                            fontFamily = OutfitFont,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 17.sp,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MkVerdeCeroPerdida)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MkVerdeCeroPerdidaTexto.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Shield,
                        contentDescription = "Escudo",
                        tint = MkVerdeCeroPerdidaTexto,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Cero pérdida",
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MkVerdeCeroPerdidaTexto
                    )
                    Text(
                        text = "En caso de inactividad, el progreso seguirá igual, tus datos 100% intactos.",
                        fontFamily = JakartaSansFont,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        color = MkVerdeCeroPerdidaTexto
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))


            Text(
                text = "Cómo obtener flores",
                fontFamily = OutfitFont,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MkTitulo
            )

            Spacer(modifier = Modifier.height(12.dp))


            TarjetaObtenerFlores(
                titulo = "Publicar un nuevo producto",
                descripcion = "Sumar un nuevo producto o servicio a tu catálogo personal te ayuda a crecer a ti y a tu mascota. Ganas flores cuando tu publicación es aprobada (una vez al día).",
                flores = flores("publicacion", 20),
                iconVector = Icons.Outlined.ViewInAr,
                onCanjearClick = { onNavigate("publicar_producto") }
            )

            Spacer(modifier = Modifier.height(14.dp))


            TarjetaObtenerFlores(
                titulo = "Registra cómo te has sentido hoy",
                descripcion = "Reconocer y expresar tus emociones puede ser un buen método de desahogo. Añade tu estado de ánimo y completa los demás bloques de bienestar: puedes ganar hasta $maxBienestar flores al día (te puedes registrar las veces que quieras, pero las flores se dan solo la primera vez del día en cada bloque).",
                flores = flores("bienestar_emocion", 5),
                iconVector = Icons.Outlined.Face,
                onCanjearClick = { onNavigate("bienestar") }
            )

            Spacer(modifier = Modifier.height(14.dp))


            TarjetaObtenerFlores(
                titulo = "Completa un microrreto",
                descripcion = "Usa tus habilidades y talentos para ayudar a los demás, con ello obtienes una remuneración económica y alimentas a Lilu.",
                flores = flores("reto", 30),
                iconVector = Icons.Outlined.Speed,
                onCanjearClick = { onNavigate("retos") }
            )

            Spacer(modifier = Modifier.height(14.dp))

            TarjetaInformativaFlores(
                titulo = "Subir de nivel",
                descripcion = "Recuerda que conforme pasa el tiempo, subes de nivel, entre cada eslabón se te recompensa con ${flores("nivel", 25)} flores para Lilu.",
                iconVector = Icons.Outlined.Verified
            )

            Spacer(modifier = Modifier.height(14.dp))

            TarjetaInformativaFlores(
                titulo = "Hacer una venta",
                descripcion = "Cada que concretas una venta, se te recompensa con ${flores("venta", 30)} flores.\n¡Anímate a compartir tu talento!",
                iconVector = Icons.Outlined.ShoppingBag
            )

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

//tarjeta con botón de canjear
@Composable
private fun TarjetaObtenerFlores(
    titulo: String,
    descripcion: String,
    flores: Int,
    iconVector: ImageVector,
    onCanjearClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MkFondoCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = titulo,
                fontFamily = OutfitFont,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = descripcion,
                fontFamily = JakartaSansFont,
                fontSize = 13.sp,
                lineHeight = 17.sp,
                color = MkSubtitulo
            )
            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MkIconoCajaFondo),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = MkIconoCajaColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onCanjearClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 40.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MkBotonCanjear)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Ganar $flores flores",
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Outlined.LocalFlorist,
                        contentDescription = "Flor",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

//tarjeta solo informativa
@Composable
private fun TarjetaInformativaFlores(
    titulo: String,
    descripcion: String,
    iconVector: ImageVector
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MkFondoCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = titulo,
                fontFamily = OutfitFont,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = descripcion,
                fontFamily = JakartaSansFont,
                fontSize = 13.sp,
                lineHeight = 17.sp,
                color = MkSubtitulo
            )
            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MkIconoCajaFondo),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = MkIconoCajaColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}