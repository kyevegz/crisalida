package com.example.crisalida.ui.theme.screens

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.crisalida.data.CacheLocal
import com.example.crisalida.data.Caches
import com.example.crisalida.data.GuardadoLocal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crisalida.data.CacheDe
import com.example.crisalida.data.EstadoConexion
import com.example.crisalida.data.ProgresoRepositorio
import com.example.crisalida.data.ProgresoResponse
import com.example.crisalida.data.ResultadoApi
import com.example.crisalida.data.SesionActual
import com.example.crisalida.ui.theme.ColorBotonEntrar
import com.example.crisalida.ui.theme.ColorTextoPrincipal
import com.example.crisalida.ui.theme.JakartaSansFont

// cosas comunes para pantallas que piden datos al servidor

// estado de una carga: cargando, con datos o con error
sealed interface Carga<out T> {
    object Cargando : Carga<Nothing>
    /** [actualizadoEn] solo viene lleno cuando son datos guardados en el teléfono (sin conexión): se muestra «Última actualización». */
    data class Lista<T>(val datos: T, val actualizadoEn: Long? = null) : Carga<T>
    data class Falla(val mensaje: String, val sinConexion: Boolean = false) : Carga<Nothing>
}

class CargaRemota<T>(val estado: Carga<T>, val recargar: () -> Unit)

/** Cuándo se bajaron del servidor los datos que se están mostrando, SI son los guardados en el teléfono; si no, null. */
val CargaRemota<*>.actualizadoEn: Long? get() = (estado as? Carga.Lista<*>)?.actualizadoEn

// pide los datos al entrar y cada que cambien las claves; la función recargar() lo repite.
// Con [cache], lo último que bajó bien se guarda (cifrado) y, sin conexión, se muestra eso con su fecha de actualización.
@Composable
fun <T> rememberCarga(vararg claves: Any?, cache: CacheDe<T>? = null, llamada: suspend () -> ResultadoApi<T>): CargaRemota<T> {
    var intento by remember { mutableIntStateOf(0) }
    val contexto = LocalContext.current.applicationContext
    val estado by produceState<Carga<T>>(Carga.Cargando, intento, *claves) {
        val copia = cache?.takeIf { !SesionActual.esCamuflaje }
        suspend fun guardado(): GuardadoLocal<T>? =
            if (copia == null) null else withContext(Dispatchers.IO) { CacheLocal.leer(contexto, copia) }
        value = Carga.Cargando
        // si ya se sabe que no hay internet, se enseña de inmediato lo guardado mientras se intenta
        var local: GuardadoLocal<T>? = null
        if (copia != null && !EstadoConexion.enLinea) {
            local = guardado()
            local?.let { value = Carga.Lista(it.datos, it.actualizadoEn) }
        }
        value = when (val r = llamada()) {
            is ResultadoApi.Exito -> {
                if (copia != null) withContext(Dispatchers.IO) { CacheLocal.guardar(contexto, copia, r.datos) }
                Carga.Lista(r.datos)
            }
            is ResultadoApi.Error -> {
                if (r.sinConexion && local == null) local = guardado()
                val viejo = local
                if (r.sinConexion && viejo != null) Carga.Lista(viejo.datos, viejo.actualizadoEn)
                else Carga.Falla(r.mensaje, r.sinConexion)
            }
        }
    }
    // si se está mostrando una falla o datos guardados por falta de internet, se vuelve a pedir solo cuando regrese la conexión
    val enLinea = EstadoConexion.enLinea
    LaunchedEffect(enLinea) {
        val e = estado
        if (enLinea && ((e as? Carga.Falla)?.sinConexion == true || (e as? Carga.Lista<*>)?.actualizadoEn != null)) intento++
    }
    return CargaRemota(estado) { intento++ }
}

// progreso (nivel, flores, Lilu), se refresca cuando gana flores
@Composable
fun rememberProgreso(conCopiaLocal: Boolean = false): CargaRemota<ProgresoResponse> =
    rememberCarga(ProgresoRepositorio.version, cache = if (conCopiaLocal) Caches.progreso else null) { ProgresoRepositorio.yo() }

/**
 * El servidor solo abre algunas rutas (progreso, mis retos, portafolio...) a la vendedora.
 * Si la compradora llegara a pedirlas, la app ni siquiera hace la llamada y no muestra un error técnico.
 */
suspend fun <T> soloVendedora(llamada: suspend () -> ResultadoApi<T>): ResultadoApi<T> =
    if (SesionActual.esCompradora) ResultadoApi.Error("Esta sección no está disponible para tu cuenta.") else llamada()

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

/**
 * Error de una carga. Si fue por falta de internet NO se muestra nada técnico: se explica qué no se puede hacer
 * ahora (queNoSePuede) y que se actualizará solo al volver la conexión. Si fue otro error, se muestra su mensaje.
 */
@Composable
fun ErrorDeCarga(
    falla: Carga.Falla,
    onReintentar: (() -> Unit)? = null,
    queNoSePuede: String = "Necesitas internet para ver esto.",
    modifier: Modifier = Modifier
) {
    if (!falla.sinConexion) {
        MensajeError(falla.mensaje, onReintentar, modifier)
        return
    }
    Column(
        modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(Icons.Outlined.CloudOff, contentDescription = null, tint = ColorTextoPrincipal)
        Text("Sin conexión", fontFamily = JakartaSansFont, fontWeight = FontWeight.SemiBold, fontSize = 15.sp,
            color = ColorTextoPrincipal, textAlign = TextAlign.Center)
        Text("$queNoSePuede Se actualizará solo cuando vuelvas a tener internet.", fontFamily = JakartaSansFont,
            fontSize = 13.sp, color = ColorTextoPrincipal, textAlign = TextAlign.Center)
        if (onReintentar != null) {
            Spacer(Modifier.height(4.dp))
            Button(onClick = onReintentar, colors = ButtonDefaults.buttonColors(containerColor = ColorBotonEntrar)) {
                Text("Reintentar", fontFamily = JakartaSansFont, fontSize = 13.sp)
            }
        }
    }
}

// muestra cargando / error / contenido según la carga
@Composable
fun <T> CargaRemota<T>.Mostrar(modifier: Modifier = Modifier, contenido: @Composable (T) -> Unit) {
    when (val e = estado) {
        is Carga.Cargando -> IndicadorCargando(modifier)
        is Carga.Falla -> ErrorDeCarga(e, recargar, modifier = modifier)
        is Carga.Lista -> contenido(e.datos)
    }
}