package com.example.crisalida.ui.theme.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crisalida.data.CuentaRepositorio
import com.example.crisalida.data.EstadoCuenta
import com.example.crisalida.data.ResultadoApi
import com.example.crisalida.ui.theme.ColorBotonEntrar
import com.example.crisalida.ui.theme.JakartaSansFont
import com.example.crisalida.ui.theme.OutfitFont
import kotlinx.coroutines.launch

private val CsVino = Color(0xFF7F284F)
private val CsGris = Color(0xFF545454)

/**
 * Aviso de «cuenta en reposo» con el botón «Reactivar» (issue 6.3). No se ve si la cuenta está activa.
 * Va arriba de Inicio y de Perfil.
 */
@Composable
fun AvisoCuentaSuspendida(modifier: Modifier = Modifier) {
    if (!EstadoCuenta.suspendida) return
    val scope = rememberCoroutineScope()
    var ocupado by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf<String?>(null) }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E3EA)),
        modifier = modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text("Tu cuenta está en reposo", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = CsVino)
            Spacer(Modifier.height(4.dp))
            Text(
                "Tus productos, servicios y retos no se muestran. Tu mascota y tu bienestar siguen disponibles. " +
                        "Cuando quieras volver, reactívala y todo se verá como estaba.",
                fontFamily = JakartaSansFont, fontSize = 14.sp, lineHeight = 19.sp, color = CsGris
            )
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = {
                    scope.launch {
                        ocupado = true
                        mensaje = when (val r = CuentaRepositorio.reactivar()) {
                            is ResultadoApi.Exito -> { EstadoCuenta.aplicar(r.datos); r.datos.mensaje ?: "¡Bienvenida de vuelta! Tu cuenta está activa." }
                            is ResultadoApi.Error -> r.mensaje
                        }
                        ocupado = false
                    }
                },
                enabled = !ocupado,
                modifier = Modifier.heightIn(min = 48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ColorBotonEntrar)
            ) {
                if (ocupado) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                else Text("Reactivar", fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
            }
        }
    }

    mensaje?.let { texto ->
        AlertDialog(
            onDismissRequest = { mensaje = null },
            text = { Text(texto, fontFamily = JakartaSansFont, color = CsGris, modifier = Modifier.verticalScroll(rememberScrollState())) },
            confirmButton = { TextButton(onClick = { mensaje = null }) { Text("Entendido", fontWeight = FontWeight.Bold, color = CsVino) } },
            containerColor = Color.White
        )
    }
}