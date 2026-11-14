package com.example.crisalida.ui.theme.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crisalida.ui.theme.ColorCamposFondo
import com.example.crisalida.ui.theme.ColorFondoApp
import com.example.crisalida.ui.theme.ColorTextoPrincipal
import com.example.crisalida.ui.theme.JakartaSansFont

// ancho máximo del contenido de Login y Registro: en tablets o en horizontal no se estira de lado a lado
private val AnchoMaximoAcceso = 440.dp

// base de Login y Registro: respeta las barras del sistema y el teclado, hace scroll cuando el contenido
// no cabe (pantalla chica, horizontal o texto grande) y centra el contenido cuando sobra espacio
@Composable
fun ContenedorAcceso(contenido: @Composable ColumnScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorFondoApp)
            .systemBarsPadding()
            .imePadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = AnchoMaximoAcceso)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            content = contenido
        )
    }
}

// los 6 cuadritos del PIN: se reparten el ancho disponible (caben en 320 dp) y crecen si el texto es grande
@Composable
fun CasillasPin(
    pin: String,
    visible: Boolean,
    onCambio: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxWidth()) {
        // campo invisible que recibe el teclado
        BasicTextField(
            value = pin,
            onValueChange = { if (it.length <= 6 && it.all(Char::isDigit)) onCambio(it) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier.matchParentSize(),
            textStyle = TextStyle(color = Color.Transparent)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for (i in 0 until 6) {
                val caracter = if (i < pin.length) {
                    if (visible) pin[i].toString() else "•"
                } else ""

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 54.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ColorCamposFondo),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = caracter,
                        fontFamily = JakartaSansFont,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorTextoPrincipal,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// "¿Aún no tienes cuenta? Crea una cuenta aquí": si no cabe en una línea, el enlace baja a la siguiente
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EnlaceInferior(texto: String, enlace: String, onClick: () -> Unit) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalArrangement = Arrangement.Center,
        itemVerticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = texto,
            fontFamily = JakartaSansFont,
            fontSize = 13.sp,
            color = ColorTextoPrincipal
        )
        TextButton(
            onClick = onClick,
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            Text(
                text = enlace,
                fontFamily = JakartaSansFont,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp,
                color = ColorTextoPrincipal
            )
        }
    }
}

// mensaje de error de Login y Registro
@Composable
fun MensajeError(error: String?, modifier: Modifier = Modifier) {
    if (error != null) {
        Text(
            text = error,
            fontFamily = JakartaSansFont,
            fontSize = 13.sp,
            color = Color(0xFFB3261E),
            textAlign = TextAlign.Center,
            modifier = modifier
                .fillMaxWidth(0.9f)
                .padding(top = 10.dp)
        )
    }
}