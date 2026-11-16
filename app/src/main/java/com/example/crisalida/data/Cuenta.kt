package com.example.crisalida.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.gson.annotations.SerializedName

/* ================================================================
 * Estado de la cuenta (issue 6.3): suspender, reactivar y borrar. Las rutas están en el backend (cuenta.py).
 * - Suspendida («en reposo»): sus productos, servicios y retos dejan de verse; sigue pudiendo entrar y usar
 *   su mascota y su bienestar.
 * - Borrar pide el PIN. Con pedidos o retos en curso el servidor responde 409 y la app ofrece suspender.
 * ================================================================ */

data class EstadoCuentaResponse(
    val estado: String = "activa",                       // "activa" | "suspendida"
    @SerializedName("suspendida_en") val suspendidaEn: String? = null,
    val mensaje: String? = null
)

data class BorrarCuentaRequest(val pin: String)
data class BorrarCuentaResponse(val borrada: Boolean = false, val mensaje: String = "")

object CuentaRepositorio {
    suspend fun estado(): ResultadoApi<EstadoCuentaResponse> = llamarApi { RetrofitCliente.api.estadoCuenta() }
    suspend fun suspender(): ResultadoApi<EstadoCuentaResponse> = llamarApi { RetrofitCliente.api.suspenderCuenta() }
    suspend fun reactivar(): ResultadoApi<EstadoCuentaResponse> = llamarApi { RetrofitCliente.api.reactivarCuenta() }
    suspend fun borrar(pin: String): ResultadoApi<BorrarCuentaResponse> = llamarApi { RetrofitCliente.api.borrarCuenta(BorrarCuentaRequest(pin)) }
}

/** Si la cuenta está suspendida (lo leen el aviso de Inicio y de Perfil y la sección Cuenta de Configuración). */
object EstadoCuenta {
    var suspendida by mutableStateOf(false)
        private set
    var desde by mutableStateOf<String?>(null)
        private set

    fun aplicar(r: EstadoCuentaResponse) {
        suspendida = r.estado == "suspendida"
        desde = r.suspendidaEn
    }

    /** Pregunta al servidor. Sin sesión (o en camuflaje) no hace nada. */
    suspend fun refrescar() {
        if (SesionActual.token == null || SesionActual.esCamuflaje) return
        (CuentaRepositorio.estado() as? ResultadoApi.Exito)?.let { aplicar(it.datos) }
    }

    fun limpiar() { suspendida = false; desde = null }
}