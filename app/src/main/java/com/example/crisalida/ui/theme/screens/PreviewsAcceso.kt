package com.example.crisalida.ui.theme.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

// Previsualizaciones para revisar el issue 1.1 sin emulador: en Android Studio abre este archivo y pulsa
// "Split" o "Design". Cada pantalla se ve en 320 dp, 600 dp, horizontal y con texto al 200 %.
// (Aquí el texto al 200 % va sin el tope de la app, que es el peor caso.)
@Preview(name = "320 dp", widthDp = 320, heightDp = 640, showBackground = true)
@Preview(name = "320 dp · texto 200 %", widthDp = 320, heightDp = 640, fontScale = 2f, showBackground = true)
@Preview(name = "600 dp", widthDp = 600, heightDp = 960, showBackground = true)
@Preview(name = "Horizontal", widthDp = 640, heightDp = 360, showBackground = true)
@Preview(name = "Horizontal · texto 200 %", widthDp = 640, heightDp = 360, fontScale = 2f, showBackground = true)
annotation class PreviewAcceso

@PreviewAcceso
@Composable
private fun LoginPreview() {
    LoginScreen(onLoginClick = { _, _ -> }, onNavigateToRegister = {})
}

@PreviewAcceso
@Composable
private fun LoginConErrorPreview() {
    LoginScreen(
        onLoginClick = { _, _ -> },
        onNavigateToRegister = {},
        error = "Seudónimo o PIN incorrectos"
    )
}

@PreviewAcceso
@Composable
private fun RegistroPreview() {
    RegisterScreen(onNextClick = { _, _, _ -> }, onNavigateToLogin = {})
}

@PreviewAcceso
@Composable
private fun GaleriaPreview() {
    GaleriaMariposasScreen(onCerrar = {})
}