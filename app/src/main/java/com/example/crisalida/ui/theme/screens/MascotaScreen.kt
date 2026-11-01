package com.example.crisalida.ui.theme.screens

import com.example.crisalida.ui.screens.*
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

/* ──────────────────────────────────────────────────────────────
 * Colores medidos del mockup de la Mascota (mascotaF.png)
 * ────────────────────────────────────────────────────────────── */
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

    CrisalidaScaffold(
        currentTitle = "Mascota",
        selectedTab = 4, // 4 = Pestaña de Mascota
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

            // Título principal
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

            // ── Tarjeta Principal de Lilu ──────────────────────────────
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
                    // Imagen circular de mascotadormida.png
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEFE6EB)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.mascotadormida),
                            contentDescription = "Lilu durmiendo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "En florecimiento",
                        fontFamily = OutfitFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MkTitulo
                    )
                    Text(
                        text = "Lilu está aleteando junto a otras mariposas",
                        fontFamily = JakartaSansFont,
                        fontSize = 12.5.sp,
                        color = MkSubtitulo
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Barra de progreso personalizada
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
                                    .fillMaxWidth(0.85f) // 85% de avance
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
                                text = "85 flores",
                                fontFamily = JakartaSansFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = MkTitulo
                            )
                            Text(
                                text = "Meta: 100 flores",
                                fontFamily = JakartaSansFont,
                                fontSize = 12.sp,
                                color = MkSubtitulo
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Botón Respirar con Lilu
                    Button(
                        onClick = { onNavigate("respirar_lilu") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
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

            // ── Banner Cero pérdida ──────────────────────────────────
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
                        fontSize = 11.5.sp,
                        lineHeight = 14.sp,
                        color = MkVerdeCeroPerdidaTexto
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Sección Cómo obtener flores
            Text(
                text = "Cómo obtener flores",
                fontFamily = OutfitFont,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MkTitulo
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 1. Publicar un nuevo producto
            TarjetaObtenerFlores(
                titulo = "Publicar un nuevo producto",
                descripcion = "Sumar un nuevo producto o servicio a tu catálogo personal te ayuda a crecer a ti y a tu mascota.",
                flores = 20,
                iconVector = Icons.Outlined.ViewInAr,
                onCanjearClick = { onNavigate("publicar_producto") }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Registra cómo te has sentido hoy
            TarjetaObtenerFlores(
                titulo = "Registra cómo te has sentido hoy",
                descripcion = "Reconocer y expresar tus emociones puede ser un buen método de desahogo. Añade tu estado de ánimo y obtén néctar para tu mascota.",
                flores = 10,
                iconVector = Icons.Outlined.Face,
                onCanjearClick = { onNavigate("bienestar") }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Completa un microrreto
            TarjetaObtenerFlores(
                titulo = "Completa un microrreto",
                descripcion = "Usa tus habilidades y talentos para ayudar a los demás, con ello obtienes una remuneración económica y alimentas a Lilu.",
                flores = 30,
                iconVector = Icons.Outlined.Speed,
                onCanjearClick = { onNavigate("retos") }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Subir de nivel (Informativo sin botón)
            TarjetaInformativaFlores(
                titulo = "Subir de nivel",
                descripcion = "Recuerda que conforme pasa el tiempo, subes de nivel, entre cada eslabón se te recompensa con flores para Lilu.",
                iconVector = Icons.Outlined.Verified
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 5. Hacer una venta (Informativo sin botón)
            TarjetaInformativaFlores(
                titulo = "Hacer una venta",
                descripcion = "Cada que concretas una venta, se te recompensa con 40 flores.\n¡Anímate a compartir tu talento!",
                iconVector = Icons.Outlined.ShoppingBag
            )

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

/** Tarjeta interactiva con botón Canjear X flores */
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

            // Caja con Icono Morado
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

            // Botón Canjear X flores
            Button(
                onClick = onCanjearClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MkBotonCanjear)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Canjear $flores flores",
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

/** Tarjeta meramente informativa (Subir de nivel y Hacer una venta) */
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

            // Caja con Icono Morado
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