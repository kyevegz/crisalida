package com.example.crisalida.ui.theme.screens

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.PersistableBundle
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crisalida.data.ProteccionPantalla
import com.example.crisalida.ui.theme.*
import com.example.crisalida.ui.theme.Paleta

private val ColorTituloCodigo: Color get() = Paleta.tituloRosa

private const val CODIGO_PARRAFO_1 = "Este código te servirá para recuperar tu PIN si más adelante lo pierdes. " +
    "En caso de que no quieras añadir un correo electrónico, este código es de gran importancia."
private const val CODIGO_PARRAFO_2 = "No hay forma de consultarlo de nuevo. Guárdalo muy bien porque no se volverá a mostrar " +
    "ni puedes tomar captura de pantalla."

/**
 * Pantalla del código de recuperación (issue 4.5). Se muestra UNA vez, al terminar el registro: el servidor
 * solo lo entrega en esa respuesta y después guarda únicamente su hash.
 *  - El código se ve grande y separado en grupos de 3 (K7M 3QX), con botón para copiarlo.
 *  - No se puede continuar sin marcar que lo guardó, y el botón «atrás» del teléfono no hace nada.
 *  - La pantalla no se puede capturar (FLAG_SECURE), sin importar el ajuste de Configuración.
 */
@Composable
fun CodigoRecuperacionScreen(
    codigo: String?,
    onFinalizar: () -> Unit
) {
    val contexto = LocalContext.current
    val actividad = LocalActivity.current

    // Bloqueo de capturas desde el primer cuadro; al salir se restaura lo que corresponda a la siguiente pantalla
    DisposableEffect(actividad) {
        actividad?.window?.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        onDispose { actividad?.let { ProteccionPantalla.aplicar(it) } }
    }

    // sin confirmar que lo guardó no se puede salir de aquí
    BackHandler(enabled = true) { }

    // Si por alguna razón no hay código (por ejemplo, la app se cerró a medias), no se deja a la usuaria atorada
    LaunchedEffect(codigo) { if (codigo == null) onFinalizar() }
    if (codigo == null) return

    var guardado by remember { mutableStateOf(false) }
    var copiado by remember { mutableStateOf(false) }
    val separado = codigo.chunked(3).joinToString(" ")
    val leido = codigo.toCharArray().joinToString(", ")   // TalkBack lo dice letra por letra

    ContenedorAcceso {
        // Escuchar (8.7): lee las instrucciones, pero NO el código: por seguridad no se dice en voz alta
        // (algunos teléfonos mandan el audio a un servicio en internet, y quien esté cerca lo oiría).
        BotonEscuchar(
            listOf(
                "Código de recuperación.",
                "Tu código está en la pantalla. Por tu seguridad, no se lee en voz alta.",
                CODIGO_PARRAFO_1,
                CODIGO_PARRAFO_2,
                "Casilla: Confirmo que he guardado mi código de recuperación.",
                "Botón: Finalizar."
            )
        )
        Text(
            text = "Código de recuperación",
            fontFamily = OutfitFont,
            fontSize = 36.sp,
            lineHeight = 42.sp,
            color = ColorTituloCodigo,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() }
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = separado,
                fontFamily = OutfitFont,
                fontWeight = FontWeight.Bold,
                fontSize = 44.sp,
                letterSpacing = 2.sp,
                color = ColorTituloCodigo,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f, fill = false)
                    .semantics { contentDescription = "Tu código de recuperación: $leido" }
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    copiarCodigo(contexto, codigo)
                    copiado = true
                },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.ContentCopy,
                    contentDescription = "Copiar código",
                    tint = ColorTextoPrincipal
                )
            }
        }
        if (copiado) {
            Text(
                text = "Código copiado. Pégalo en un lugar seguro.",
                fontFamily = JakartaSansFont,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Paleta.exito,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .semantics { liveRegion = LiveRegionMode.Polite }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = CODIGO_PARRAFO_1,
            fontFamily = JakartaSansFont,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = ColorTextoPrincipal,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = CODIGO_PARRAFO_2,
            fontFamily = JakartaSansFont,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = ColorTextoPrincipal,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // toda la fila es la casilla (TalkBack: «Confirmo que he guardado mi código, casilla, no marcada»)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .toggleable(value = guardado, onValueChange = { guardado = it }, role = Role.Checkbox),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = guardado,
                onCheckedChange = null,
                colors = CheckboxDefaults.colors(checkedColor = ColorBotonEntrar),
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(
                text = "Confirmo que he guardado mi código de recuperación",
                fontFamily = JakartaSansFont,
                fontSize = 14.sp,
                lineHeight = 19.sp,
                color = ColorTextoPrincipal
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onFinalizar,
            enabled = guardado,
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ColorBotonEntrar,
                disabledContainerColor = ColorBotonEntrar.copy(alpha = 0.5f)
            )
        ) {
            Text("Finalizar", fontFamily = OutfitFont, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Paleta.blanco)
        }
    }
}

// Copia el código marcándolo como sensible (en Android 13+ el teléfono no muestra su vista previa)
private fun copiarCodigo(contexto: Context, codigo: String) {
    val portapapeles = contexto.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Código de recuperación", codigo)
    if (Build.VERSION.SDK_INT >= 33) {
        clip.description.extras = PersistableBundle().apply { putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true) }
    }
    portapapeles.setPrimaryClip(clip)
}

@PreviewAcceso
@Composable
private fun CodigoRecuperacionPreview() {
    CodigoRecuperacionScreen(codigo = "K7M3QX", onFinalizar = {})
}
