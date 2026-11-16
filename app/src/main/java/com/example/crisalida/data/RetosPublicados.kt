package com.example.crisalida.data

import com.google.gson.annotations.SerializedName

/* ================================================================
 * Publicar un reto (issue 7.5). Ruta del servidor: POST /retos (retos_publicados.py, issue 7.1).
 * - El reto queda "en_revision": no aparece en el tablero hasta que el equipo lo apruebe.
 * - Exige correo verificado (403 «Verifica tu correo...») y pasa por los filtros de seguridad (422 con el motivo).
 * - Con la misma Idempotency-Key, reenviar no crea otro reto (por si se toca dos veces o se corta la conexión).
 * ================================================================ */

data class RetoPublicarRequest(
    val titulo: String,
    @SerializedName("categoria_id") val categoriaId: Int,
    val descripcion: String,
    val compensacion: Double,                              // recompensa en pesos (MXN)
    @SerializedName("fecha_limite") val fechaLimite: String // ISO con zona horaria, p. ej. 2026-12-01T18:00:00-06:00
)

data class RetoPublicadoResponse(
    @SerializedName("reto_id") val retoId: String,
    val estado: String,                                     // "en_revision"
    val mensaje: String
)

object RetosPublicadosRepositorio {
    suspend fun publicar(datos: RetoPublicarRequest, clave: String): ResultadoApi<RetoPublicadoResponse> =
        llamarApi { RetrofitCliente.api.publicarReto(datos, clave) }
}