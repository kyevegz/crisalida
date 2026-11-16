package com.example.crisalida.ui.theme.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crisalida.ui.theme.ColorCamposFondo
import com.example.crisalida.ui.theme.ColorTextoPrincipal
import com.example.crisalida.ui.theme.JakartaSansFont
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Nota que se muestra cuando la pantalla enseña lo ÚLTIMO que se guardó en el teléfono (sin conexión):
 * «Última actualización: 15 nov, 18:20». Si [actualizadoEn] es null (datos recién bajados del servidor), no se ve nada.
 * El botón «Actualizar» vuelve a pedir los datos al servidor.
 */
@Composable
fun NotaUltimaActualizacion(actualizadoEn: Long?, onActualizar: (() -> Unit)? = null) {
    if (actualizadoEn == null) return
    val fecha = SimpleDateFormat("d MMM, HH:mm", Locale("es", "MX")).format(Date(actualizadoEn))
    Row(
        Modifier
            .fillMaxWidth()
            .background(ColorCamposFondo, RoundedCornerShape(10.dp))
            .padding(start = 12.dp, top = 6.dp, bottom = 6.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Outlined.CloudOff, contentDescription = null, tint = ColorTextoPrincipal, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text("Sin conexión", fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ColorTextoPrincipal)
            Text("Última actualización: $fecha", fontFamily = JakartaSansFont, fontSize = 12.sp, color = ColorTextoPrincipal)
        }
        if (onActualizar != null) {
            TextButton(onClick = onActualizar) {
                Text("Actualizar", fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ColorTextoPrincipal)
            }
        }
    }
    Spacer(Modifier.height(12.dp))
}