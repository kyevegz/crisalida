package com.example.crisalida.ui.screens

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
import com.example.crisalida.R
import com.example.crisalida.ui.theme.*
// (si el archivo está en otro paquete, estos nombres son los mismos)
import com.example.crisalida.ui.theme.screens.rememberProgreso
import com.example.crisalida.ui.theme.screens.Carga
/* ──────────────────────────────────────────────────────────────
 * Colores medidos del mockup Respira con Lilu (respiraConLilu.png)
 * ────────────────────────────────────────────────────────────── */
private val MkTitulo = Color(0xFF7F284F)
private val MkSubtitulo = Color(0xFF545454)
private val MkFondoBotonVolver = Color(0xFFE2D4DA)
private val MkFondoTarjetaAyuda = Color(0xFFD8CCD3)
private val MkTextoAyudaGris = Color(0xFF4A3B43)

@Composable
fun RespirarLiluScreen(
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Animación sutil de respiración/pulsación para la imagen de Lilu
    val infiniteTransition = rememberInfiniteTransition(label = "lilu_anim")
    val scaleAnim by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "escala_lilu"
    )

    // Animación flotante suave para las frases de apoyo
    val floatAnim by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flotar_frases"
    )
    val progreso = rememberProgreso()
    val etapa = (progreso.estado as? Carga.Lista)?.datos?.lilu?.etapa
    CrisalidaScaffold(
        currentTitle = "Respirar con Lilu",
        selectedTab = 4,
        onNavigate = onNavigate
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ColorFondoApp)
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Botón superior izquierdo "Mascota" (Volver)
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

            // Imagen animada de Lilu (mascotagrande.png)
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .graphicsLayer(
                        scaleX = scaleAnim,
                        scaleY = scaleAnim
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = when (etapa) { 2 -> R.drawable.lilu_larva; 3 -> R.drawable.lilu_mariposa; else -> R.drawable.lilu_huevo}),
                    contentDescription = "Lilu emocionada",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Mensaje principal de Lilu
            Text(
                text = "Lilu está muy feliz de que estés aquí. Quiere recordarte lo valiosa que eres para el mundo.",
                fontFamily = OutfitFont,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                color = MkTitulo,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Frases de apoyo flotantes con animación dinámica
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer(translationY = floatAnim),
                verticalArrangement = Arrangement.spacedBy(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                FraseFlotante(
                    texto = "No eres lo que te ha tocado vivir, eres lo que eres capaz de superar",
                    align = TextAlign.Start
                )

                FraseFlotante(
                    texto = "Tu fuerza no se mide por lo que aguantas en silencio, sino por tu valentía para buscar paz y libertad",
                    align = TextAlign.End
                )

                FraseFlotante(
                    texto = "Tienes derecho a que todos los minutos de tu vida sean de libertad, de felicidad y de paz",
                    align = TextAlign.Start
                )

                FraseFlotante(
                    texto = "Permítete no tener todas las respuestas resueltas ahora. La independencia se construye también en los días donde simplemente eliges cuidarte y respirar",
                    align = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Sección de Números de Emergencia y Ayuda (Dialer sin marcar)
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
                        fontSize = 12.5.sp,
                        lineHeight = 17.sp,
                        color = MkTextoAyudaGris
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Lista de números interactivos (ACTION_DIAL)
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

/** Componente auxiliar para cada frase de apoyo con estilo tipográfico elegante */
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

/** Botón de número telefónico que abre el marcador (Dialer) SIN marcar automáticamente */
@Composable
fun BotonTelefonoAyuda(
    numero: String,
    descripcion: String,
    context: android.content.Context
) {
    Surface(
        onClick = {
            // Limpiamos formato para el intent tel: conservando espacios o dígitos necesarios
            val limpiarNumero = numero.replace(Regex("[^0-9+]"), "")
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$limpiarNumero")
            }
            context.startActivity(intent)
        },
        shape = RoundedCornerShape(10.dp),
        color = Color.White.copy(alpha = 0.85f),
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
                text = "$numero ($descripcion)",
                fontFamily = JakartaSansFont,
                fontWeight = FontWeight.Bold,
                fontSize = 13.5.sp,
                color = MkTitulo
            )
        }
    }
}