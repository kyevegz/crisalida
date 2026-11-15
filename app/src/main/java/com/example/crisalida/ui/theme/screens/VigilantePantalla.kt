package com.example.crisalida.ui.theme.screens

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.crisalida.data.ProteccionPantalla

// Mantienela protección de la ventana según la pantalla en la que está la usuaria
// y si el interruptor de Configuración está encendido
@Composable
fun VigilantePantalla(navController: NavHostController) {
    val actividad = LocalContext.current as? ComponentActivity
    val ruta = navController.currentBackStackEntryAsState().value?.destination?.route
    @Suppress("UNUSED_VARIABLE")
    val bloquear = ProteccionPantalla.bloquearCapturas   // se lee para volver a aplicar cuando cambie el interruptor
    SideEffect {
        ProteccionPantalla.alCambiarRuta(ruta)
        actividad?.let { ProteccionPantalla.aplicar(it) }
    }
}
