package com.example.crisalida.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crisalida.data.ProgresoRepositorio
import com.example.crisalida.data.ProgresoResponse
import com.example.crisalida.data.ResultadoApi
import com.example.crisalida.ui.theme.ColorBotonEntrar
import com.example.crisalida.ui.theme.ColorTextoPrincipal
import com.example.crisalida.ui.theme.JakartaSansFont

/* ================================================================
 * Piezas comunes para pantallas que leen datos del servidor.
 * ================================================================ */

/** Estado de una carga: cargando, lista con datos, o falla con un mensaje listo para mostrar. */
sealed interface Carga<out T> {
    object Cargando : Carga<Nothing>
    data class Lista<T>(val datos: T) : Carga<T>
    data class Falla(val mensaje: String) : Carga<Nothing>
}

class CargaRemota<T>(val estado: Carga<T>, val recargar: () -> Unit)

/**
 * Llama al servidor al entrar a la pantalla y cada vez que cambie alguna de [claves]
 * (un filtro, un buscador...). `recargar()` la repite (botón "Reintentar").
 */
@Composable
fun <T> rememberCarga(vararg claves: Any?, llamada: suspend () -> ResultadoApi<T>): CargaRemota<T> {
    var intento by remember { mutableIntStateOf(0) }
    val estado by produceState<Carga<T>>(Carga.Cargando, intento, *claves) {
        value = Carga.Cargando
        value = when (val r = llamada()) {
            is ResultadoApi.Exito -> Carga.Lista(r.datos)
            is ResultadoApi.Error -> Carga.Falla(r.mensaje)
        }
    }
    return CargaRemota(estado) { intento++ }
}

/** El progreso (nivel, flores, Lilu...). Se refresca solo cuando la usuaria gana flores. */
@Composable
fun rememberProgreso(): CargaRemota<ProgresoResponse> =
    rememberCarga(ProgresoRepositorio.version) { ProgresoRepositorio.yo() }

@Composable
fun IndicadorCargando(modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator(color = ColorBotonEntrar)
    }
}

@Composable
fun MensajeError(mensaje: String, onReintentar: (() -> Unit)? = null, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(mensaje, fontFamily = JakartaSansFont, fontSize = 13.sp, color = ColorTextoPrincipal, textAlign = TextAlign.Center)
        if (onReintentar != null) {
            Spacer(Modifier.height(4.dp))
            Button(onClick = onReintentar, colors = ButtonDefaults.buttonColors(containerColor = ColorBotonEntrar)) {
                Text("Reintentar", fontFamily = JakartaSansFont, fontSize = 13.sp)
            }
        }
    }
}

/** Atajo: muestra cargando / error / contenido según el estado de una carga. */
@Composable
fun <T> CargaRemota<T>.Mostrar(modifier: Modifier = Modifier, contenido: @Composable (T) -> Unit) {
    when (val e = estado) {
        is Carga.Cargando -> IndicadorCargando(modifier)
        is Carga.Falla -> MensajeError(e.mensaje, recargar, modifier)
        is Carga.Lista -> contenido(e.datos)
    }
}
