package com.example.crisalida.ui.theme.screens

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavHostController
import com.example.crisalida.data.SesionActual
import kotlinx.coroutines.delay
import androidx.compose.runtime.getValue
import androidx.activity.compose.LocalActivity
import com.example.crisalida.data.InactividadSesion
import com.example.crisalida.data.FormulariosAbiertos
// Vigila el tiempo sin tocar la pantalla (los toques los registra MainActivity.dispatchTouchEvent).
// Cuando se cumple el tiempo la sesión se cierra SIN avisar:
//  - con la app abierta (o en segundo plano): se cierra la sesión y se saca a la usuaria de la app,
//    igual que el botón de Salida de emergencia (no queda en «recientes»);
//  - si el teléfono estaba bloqueado o en otra app y no hubo tiempo de cerrarla: al abrir la app ya no hay
//    sesión y aparece el login.
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VigilanteInactividad(navController: NavHostController) {
    val actividad = LocalActivity.current as? ComponentActivity


    // Mientras el teclado está abierto se cuenta como actividad: así no se pierde una bitácora larga
    // que se está escribiendo (escribir con el teclado en pantalla no genera toques sobre la app)
    val tecladoVisible by rememberUpdatedState(WindowInsets.isImeVisible)

    DisposableEffect(actividad) {
        val observador = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_START && `InactividadSesion`.debeCerrar()) {
                FormulariosAbiertos.guardarTodo()   // guarda el formulario abierto ANTES de cerrar la sesión
                SesionActual.cerrar()
                navController.navigate("login") { popUpTo(0) { inclusive = true } }
            }
        }
        actividad?.lifecycle?.addObserver(observador)
        onDispose { actividad?.lifecycle?.removeObserver(observador) }
    }

    LaunchedEffect(Unit) {
        while (true) {
            if (tecladoVisible) `InactividadSesion`.registrarActividad()
            if (`InactividadSesion`.debeCerrar()) {
                FormulariosAbiertos.guardarTodo()   // guarda el formulario abierto ANTES de cerrar la sesión
                SesionActual.cerrar()
                actividad?.finishAndRemoveTask()
            }
            // revisa con más calma si falta mucho; el tiempo se reinicia con los toques, así que no se
            // puede dormir hasta el final
            delay((`InactividadSesion`.milisRestantes() ?: 5_000L).coerceIn(500L, 5_000L))
        }
    }
}