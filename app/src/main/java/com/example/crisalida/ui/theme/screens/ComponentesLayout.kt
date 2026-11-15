package com.example.crisalida.ui.theme.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.crisalida.ui.theme.ColorFondoApp

// ancho máximo de las pantallas de lectura (Home, detalles): en tablets u horizontal el contenido
// no se estira de lado a lado, se queda centrado
private val AnchoMaximoLectura = 640.dp

// fondo de la pantalla + contenido centrado con ancho máximo. Va dentro del Scaffold: recibe su innerPadding
@Composable
fun ContenidoCentrado(innerPadding: PaddingValues, contenido: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorFondoApp)
            .padding(innerPadding),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(modifier = Modifier.widthIn(max = AnchoMaximoLectura).fillMaxSize()) {
            contenido()
        }
    }
}

// Previsualizaciones para revisar sin emulador: 320 dp, 600 dp, horizontal y con texto al 200 %
// (aquí el 200 % va sin el tope de la app, que es el peor caso).
@Preview(name = "320 dp", widthDp = 320, heightDp = 640, showBackground = true)
@Preview(name = "320 dp · texto 200 %", widthDp = 320, heightDp = 640, fontScale = 2f, showBackground = true)
@Preview(name = "600 dp", widthDp = 600, heightDp = 960, showBackground = true)
@Preview(name = "Horizontal", widthDp = 640, heightDp = 360, showBackground = true)
@Preview(name = "Horizontal · texto 200 %", widthDp = 640, heightDp = 360, fontScale = 2f, showBackground = true)
annotation class PreviewResponsivo
