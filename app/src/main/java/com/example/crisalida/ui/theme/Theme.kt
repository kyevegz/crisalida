package com.example.crisalida.ui.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private fun Context.actividad(): Activity? {
    var c: Context? = this
    while (c is ContextWrapper) {
        if (c is Activity) return c
        c = c.baseContext
    }
    return null
}

// Tema de toda la app. En modo claro es el mismo MaterialTheme de siempre (esquema claro por defecto);
// en modo oscuro usa los colores de Paleta. El modo se cambia en Configuración > Apariencia
@Composable
fun CrisalidaTemaApp(content: @Composable () -> Unit) {
    // si el teléfono cambia de modo con la app abierta y se eligió «Automático», se actualiza solo
    val sistemaOscuro = isSystemInDarkTheme()
    SideEffect { Paleta.actualizarSistema(sistemaOscuro) }

    val oscuro = Paleta.modoOscuro
    val esquema = if (oscuro) {
        darkColorScheme(
            primary = Paleta.moradoAcento,
            onPrimary = Paleta.blanco,
            secondary = Paleta.botonEntrar,
            onSecondary = Paleta.blanco,
            background = Paleta.fondoApp,
            onBackground = Paleta.textoNegro,
            surface = Paleta.superficie,
            onSurface = Paleta.textoNegro,
            surfaceVariant = Paleta.tarjetaGris,
            onSurfaceVariant = Paleta.textoGris,
            surfaceContainerLowest = Paleta.fondoApp,
            surfaceContainerLow = Paleta.superficie,
            surfaceContainer = Paleta.tarjeta,
            surfaceContainerHigh = Paleta.tarjetaGris,
            surfaceContainerHighest = Paleta.campo,
            outline = Paleta.textoGris,
            outlineVariant = Paleta.fondoBotonVolver,
            error = Paleta.error,
            onError = Paleta.blanco
        )
    } else {
        lightColorScheme()
    }

    // barras del sistema y fondo de la ventana (se ve al abrir el teclado o al girar); en modo claro se dejan como estaban
    val vista = LocalView.current
    val ventana = remember(vista) { vista.context.actividad()?.window }
    val fondoOriginal = remember(ventana) { ventana?.decorView?.background }
    val iconosClarosOriginal = remember(ventana) {
        ventana?.let { WindowCompat.getInsetsController(it, vista).isAppearanceLightStatusBars }
    }
    val barrasOscuras = oscuro && !Paleta.enCamuflaje
    SideEffect {
        if (ventana == null) return@SideEffect
        val controlador = WindowCompat.getInsetsController(ventana, vista)
        if (barrasOscuras) {
            ventana.setBackgroundDrawable(ColorDrawable(Paleta.fondoApp.toArgb()))
            controlador.isAppearanceLightStatusBars = false
            controlador.isAppearanceLightNavigationBars = false
        } else {
            val d: Drawable? = fondoOriginal
            if (d != null) ventana.setBackgroundDrawable(d)
            if (iconosClarosOriginal != null) controlador.isAppearanceLightStatusBars = iconosClarosOriginal
        }
    }

    MaterialTheme(colorScheme = esquema) {
        // el texto sin color propio toma el color del tema (Material no lo hace solo fuera de un Surface)
        CompositionLocalProvider(LocalContentColor provides esquema.onBackground) {
            content()
        }
    }
}

// la galería de camuflaje se ve igual en los dos modos (así no delata nada): siempre con el esquema claro
@Composable
fun CrisalidaTemaClaroFijo(content: @Composable () -> Unit) {
    DisposableEffect(Unit) {
        Paleta.enCamuflaje = true
        onDispose { Paleta.enCamuflaje = false }
    }
    MaterialTheme(colorScheme = lightColorScheme()) {
        CompositionLocalProvider(LocalContentColor provides PaletaGaleria.cTextoNegro) {
            content()
        }
    }
}
