package com.example.crisalida.ui.theme.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crisalida.data.ColaEnvios
import com.example.crisalida.data.EnvioPendiente
import com.example.crisalida.data.EstadoConexion
import com.example.crisalida.ui.theme.ColorPlaceholder
import com.example.crisalida.ui.theme.ColorTextoPrincipal
import com.example.crisalida.ui.theme.JakartaSansFont
import com.example.crisalida.ui.theme.OutfitFont
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Envía lo pendiente en cuanto vuelve el internet mientras la app está abierta (WorkManager cubre el resto).
 * Se pone una sola vez, en MainActivity.
 */
@Composable
fun VigilanteCola() {
    val enLinea = EstadoConexion.enLinea
    LaunchedEffect(enLinea) {
        if (enLinea && ColaEnvios.cantidad > 0) ColaEnvios.vaciar()
    }
}

/**
 * Tarjeta de «Envíos pendientes»: cuántos hay, en qué estado están y un botón para cancelar cada uno.
 * Si no hay ninguno, no ocupa espacio. Va en el Perfil (de las dos cuentas).
 */
@Composable
fun TarjetaEnviosPendientes() {
    val lista = ColaEnvios.pendientes
    if (lista.isEmpty()) return
    val scope = rememberCoroutineScope()
    val enLinea = EstadoConexion.enLinea
    var porCancelar by remember { mutableStateOf<EnvioPendiente?>(null) }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "Envíos pendientes (${lista.size})",
                fontFamily = OutfitFont, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = ColorTextoPrincipal
            )
            Text(
                "Se guardaron en tu teléfono y se enviarán solos cuando haya internet. " +
                        "Tus flores y tu avance se sumarán cuando el servidor lo confirme.",
                fontFamily = JakartaSansFont, fontSize = 12.sp, color = ColorPlaceholder
            )
            lista.forEach { envio ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            ColaEnvios.descripcion(envio),
                            fontFamily = JakartaSansFont, fontWeight = FontWeight.SemiBold, fontSize = 14.sp,
                            color = ColorTextoPrincipal
                        )
                        val hora = SimpleDateFormat("d MMM, HH:mm", Locale("es", "MX")).format(Date(envio.creadoEn))
                        val estado = when {
                            envio.error != null -> "No se pudo enviar: ${envio.error}"
                            !enLinea -> "Esperando conexión · guardado $hora"
                            else -> "Enviando… · guardado $hora"
                        }
                        Text(
                            estado, fontFamily = JakartaSansFont, fontSize = 12.sp,
                            color = if (envio.error != null) Color(0xFFB3261E) else ColorPlaceholder
                        )
                    }
                    TextButton(onClick = { porCancelar = envio }) {
                        Text("Cancelar", fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ColorTextoPrincipal)
                    }
                }
            }
        }
    }
    Spacer(Modifier.height(16.dp))

    porCancelar?.let { envio ->
        AlertDialog(
            onDismissRequest = { porCancelar = null },
            title = { Text("¿Cancelar este envío?", fontFamily = OutfitFont, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "${ColaEnvios.descripcion(envio)} no se enviará y se borrará de tu teléfono. " +
                            "Si ya se había alcanzado a enviar, no se puede deshacer.",
                    fontFamily = JakartaSansFont, fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { scope.launch { ColaEnvios.cancelar(envio.id) }; porCancelar = null }) {
                    Text("Sí, cancelar", fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { porCancelar = null }) { Text("No, mantener", fontFamily = JakartaSansFont) }
            }
        )
    }
}

/** Antes de cerrar sesión: si hay envíos pendientes, avisa que se van a perder. */
@Composable
fun AvisoCerrarConEnviosPendientes(onCerrarIgual: () -> Unit, onVolver: () -> Unit) {
    AlertDialog(
        onDismissRequest = onVolver,
        title = { Text("Tienes envíos pendientes", fontFamily = OutfitFont, fontWeight = FontWeight.Bold) },
        text = {
            Text(
                "Si cierras sesión ahora, los ${ColaEnvios.cantidad} envíos que esperan conexión se cancelarán y no se enviarán. " +
                        "Si prefieres que se envíen, vuelve y espera a tener internet.",
                fontFamily = JakartaSansFont, fontSize = 14.sp
            )
        },
        confirmButton = {
            TextButton(onClick = onCerrarIgual) { Text("Cerrar sesión igual", fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold) }
        },
        dismissButton = { TextButton(onClick = onVolver) { Text("Volver", fontFamily = JakartaSansFont) } }
    )
}