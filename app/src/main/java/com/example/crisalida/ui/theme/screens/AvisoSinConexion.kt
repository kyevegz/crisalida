package com.example.crisalida.ui.theme.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.crisalida.data.EstadoConexion
import com.example.crisalida.ui.theme.ColorTextoPrincipal
import com.example.crisalida.ui.theme.JakartaSansFont
import kotlinx.coroutines.delay
import com.example.crisalida.ui.theme.Paleta

// pantallas con barra de pestañas abajo: el aviso se sube para no taparla
private val RUTAS_CON_BARRA_INFERIOR = setOf("retos", "marketplace", "home", "bienestar", "mascota", "perfil")

/**
 * Aviso discreto de «Sin conexión»: una pastillita abajo que NO tapa los botones de arriba ni empuja el contenido,
 * y que no intercepta toques. Aparece si no hay internet (con 1.5 s de espera, para no parpadear en cambios de red
 * muy cortos) y desaparece sola en cuanto vuelve. Se anuncia a los lectores de pantalla.
 * Va dentro de un Box que ocupa toda la pantalla, encima del NavHost.
 */
@Composable
fun BoxScope.AvisoSinConexion(navController: NavHostController) {
    // la ruta se lee AQUÍ (no en MainActivity) para que solo se redibuje la pastilla al cambiar de pantalla
    val ruta = navController.currentBackStackEntryAsState().value?.destination?.route
    val enLinea = EstadoConexion.enLinea
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(enLinea) {
        if (enLinea) visible = false else { delay(1500); visible = true }
    }
    val margenInferior = if (ruta in RUTAS_CON_BARRA_INFERIOR) 96.dp else 16.dp

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically { it / 2 },
        exit = fadeOut(),
        modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = margenInferior, start = 16.dp, end = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .background(ColorTextoPrincipal.copy(alpha = 0.93f), RoundedCornerShape(20.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp)
                .semantics { liveRegion = LiveRegionMode.Polite },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.CloudOff, contentDescription = null, tint = Paleta.blanco, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                "Sin conexión. Algunas funciones no están disponibles.",
                fontFamily = JakartaSansFont, fontSize = 12.sp, color = Paleta.blanco, textAlign = TextAlign.Start
            )
        }
    }
}