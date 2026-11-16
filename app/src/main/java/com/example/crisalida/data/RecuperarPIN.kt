package com.example.crisalida.data

import com.google.gson.annotations.SerializedName

/* ================================================================
 * Olvidé mi PIN (issue 4.7): lo que viaja al servidor y lo que responde.
 * Las rutas están en el backend (recuperacion.py): /auth/recuperar/...
 * Nada de esto se guarda en el teléfono: el código y los PIN solo viven en memoria mientras se escriben.
 * ================================================================ */

data class RecuperarConCodigoRequest(
    val seudonimo: String,
    val codigo: String,
    @SerializedName("pin_nuevo") val pinNuevo: String
)

data class SolicitarCodigoCorreoRequest(
    val seudonimo: String,
    val correo: String
)

data class RecuperarConCorreoRequest(
    val seudonimo: String,
    val correo: String,
    val codigo: String,
    @SerializedName("pin_nuevo") val pinNuevo: String
)

/** Todas las respuestas traen un texto; «solicitar» además dice cuántos minutos dura el código. */
data class MensajeRecuperacionResponse(
    val mensaje: String = "",
    @SerializedName("expira_en_minutos") val expiraEnMinutos: Int? = null
)

object RecuperacionRepositorio {
    /** Con el código de recuperación que se mostró al registrarse. */
    suspend fun conCodigo(seudonimo: String, codigo: String, pinNuevo: String): ResultadoApi<MensajeRecuperacionResponse> =
        llamarApi { RetrofitCliente.api.recuperarConCodigo(RecuperarConCodigoRequest(seudonimo.trim(), codigo.trim(), pinNuevo)) }

    /** Pide que se mande un código al correo. El servidor responde igual exista o no la cuenta. */
    suspend fun solicitarCodigoPorCorreo(seudonimo: String, correo: String): ResultadoApi<MensajeRecuperacionResponse> =
        llamarApi { RetrofitCliente.api.solicitarCodigoCorreo(SolicitarCodigoCorreoRequest(seudonimo.trim(), correo.trim())) }

    suspend fun conCorreo(seudonimo: String, correo: String, codigo: String, pinNuevo: String): ResultadoApi<MensajeRecuperacionResponse> =
        llamarApi { RetrofitCliente.api.recuperarConCorreo(RecuperarConCorreoRequest(seudonimo.trim(), correo.trim(), codigo.trim(), pinNuevo)) }
}