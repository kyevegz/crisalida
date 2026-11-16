package com.example.crisalida.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * ¿Hay internet ahora mismo? Lo usa el aviso de «Sin conexión» y las pantallas que se recargan solas al volver.
 *
 * - Al iniciar se revisa la red actual: así funciona aunque la app se abra YA sin conexión.
 * - Después se escuchan los cambios de red de Android (no gasta batería ni datos: no hace ninguna petición).
 * - Cuenta como «en línea» solo si la red tiene internet y Android la validó (un Wi-Fi sin salida a internet no sirve).
 * - Si algo falla al consultar la red (sin permiso, sin servicio), se asume que SÍ hay internet: lo peor que pasa es
 *   que no se muestre el aviso, nunca que se bloquee algo.
 */
object EstadoConexion {
    /** true = hay internet. Es un estado de Compose: lo que lo lee se redibuja solo cuando cambia. */
    var enLinea by mutableStateOf(true)
        private set

    private var iniciado = false

    fun iniciar(contexto: Context) {
        if (iniciado) return
        val cm = contexto.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return
        iniciado = true
        try {
            enLinea = cm.activeNetwork?.let { cm.getNetworkCapabilities(it) }?.conInternet() ?: false
            cm.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
                override fun onCapabilitiesChanged(network: Network, capacidades: NetworkCapabilities) {
                    enLinea = capacidades.conInternet()
                }

                override fun onLost(network: Network) {
                    enLinea = false   // si hay otra red de respaldo, Android avisa enseguida y vuelve a true
                }
            })
        } catch (e: Exception) {
            enLinea = true
        }
    }

    private fun NetworkCapabilities.conInternet() =
        hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
}