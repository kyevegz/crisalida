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

// ---- Cambiar de rol (6.5): vendedora <-> compradora. El servidor no borra nada: oculta y devuelve un token nuevo.
data class EnCursoDto(val pedidos: Int = 0, val retos: Int = 0)

data class RolActualResponse(
    val rol: String = ROL_VENDEDORA,
    @SerializedName("en_curso") val enCurso: EnCursoDto = EnCursoDto(),
    @SerializedName("puede_cambiar") val puedeCambiar: Boolean = true
)

data class CambioRolRequest(val rol: String)

data class CambioRolResponse(
    val token: String,
    val rol: String,
    val cambio: Boolean = true,
    val mensaje: String = ""
)

object CuentaRepositorio {
    suspend fun estado(): ResultadoApi<EstadoCuentaResponse> = llamarApi { RetrofitCliente.api.estadoCuenta() }
    suspend fun suspender(): ResultadoApi<EstadoCuentaResponse> = llamarApi { RetrofitCliente.api.suspenderCuenta() }
    suspend fun reactivar(): ResultadoApi<EstadoCuentaResponse> = llamarApi { RetrofitCliente.api.reactivarCuenta() }
    suspend fun borrar(pin: String): ResultadoApi<BorrarCuentaResponse> = llamarApi { RetrofitCliente.api.borrarCuenta(BorrarCuentaRequest(pin)) }

    suspend fun rolActual(): ResultadoApi<RolActualResponse> = llamarApi { RetrofitCliente.api.rolActual() }
    suspend fun cambiarRol(rol: String): ResultadoApi<CambioRolResponse> = llamarApi { RetrofitCliente.api.cambiarRol(CambioRolRequest(rol)) }

    /** ¿Ya hizo el radar de talentos? true: sí · false: nunca (el servidor responde 404) · null: no se pudo saber (sin conexión). */
    suspend fun radarCompletado(): Boolean? =
        when (val r = llamarApi { RetrofitCliente.api.radarResultado() }) {
            is ResultadoApi.Exito -> true
            is ResultadoApi.Error -> if (r.codigoHttp == 404) false else null
        }
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