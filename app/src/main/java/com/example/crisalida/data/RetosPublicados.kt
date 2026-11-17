package com.example.crisalida.data

import com.google.gson.annotations.SerializedName
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

/* ================================================================
 * Publicar un reto (issue 7.5). Ruta del servidor: POST /retos (retos_publicados.py, issue 7.1).
 * - El reto queda "en_revision": no aparece en el tablero hasta que el equipo lo apruebe.
 * - Exige correo verificado (403 «Verifica tu correo...») y pasa por los filtros de seguridad (422 con el motivo).
 * - Con la misma Idempotency-Key, reenviar no crea otro reto (por si se toca dos veces o se corta la conexión).
 * - La foto de referencia es opcional y es UNA sola: se sube después de crear el reto (POST /retos/{id}/imagen).
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

data class FotoRetoResponse(val subida: Boolean = true)

/* ================================================================
 * Mis retos y entregas (issue 7.6). Rutas del servidor (retos_publicados.py, issues 7.3 y 7.4):
 *   GET  /retos/publicados                       mis retos con quién los tomó y si ya entregó
 *   POST /retos/aceptaciones/{id}/validar        da las flores a quien lo hizo y completa el reto
 *   POST /retos/aceptaciones/{id}/rechazar       pide cambios con un motivo; la vendedora puede volver a entregar
 * ================================================================ */

data class AceptacionPublicadaDto(
    @SerializedName("aceptacion_id") val aceptacionId: String,
    val estado: String,                                     // aceptado, entregado, validado, abandonado...
    val seudonimo: String,
    val avatar: String? = null,
    @SerializedName("entregado_en") val entregadoEn: String? = null,
    @SerializedName("motivo_rechazo") val motivoRechazo: String? = null,
    val rechazos: Int = 0
)

data class RetoPublicadoDto(
    @SerializedName("reto_id") val retoId: String,
    val titulo: String,
    val compensacion: Double = 0.0,
    @SerializedName("fecha_limite") val fechaLimite: String? = null,
    val estado: String,                                     // en_revision, abierto, asignado, completado, rechazado, cancelado
    @SerializedName("motivo_rechazo") val motivoRechazo: String? = null,
    val categoria: String = "",
    val aceptaciones: List<AceptacionPublicadaDto> = emptyList()
)

data class MisRetosPublicadosResponse(val retos: List<RetoPublicadoDto> = emptyList())

data class ValidacionEntregaResponse(
    @SerializedName("aceptacion_id") val aceptacionId: String,
    val estado: String,
    @SerializedName("flores_otorgadas") val floresOtorgadas: Int = 0
)

data class RechazoEntregaRequest(val motivo: String)

data class RechazoEntregaResponse(
    @SerializedName("aceptacion_id") val aceptacionId: String,
    val estado: String,
    @SerializedName("motivo_rechazo") val motivoRechazo: String? = null
)

object RetosPublicadosRepositorio {
    suspend fun publicar(datos: RetoPublicarRequest, clave: String): ResultadoApi<RetoPublicadoResponse> =
        llamarApi { RetrofitCliente.api.publicarReto(datos, clave) }

    suspend fun mios(): ResultadoApi<MisRetosPublicadosResponse> = llamarApi { RetrofitCliente.api.misRetosPublicados() }

    /** Da por buena la entrega. Repetirla no da flores dos veces (el servidor lo cuida). */
    suspend fun validar(aceptacionId: String): ResultadoApi<ValidacionEntregaResponse> =
        llamarApi { RetrofitCliente.api.validarEntrega(aceptacionId) }

    /** Pide cambios: [motivo] de 5 a 300 caracteres, sin datos de contacto (el servidor responde con el porqué si no). */
    suspend fun rechazar(aceptacionId: String, motivo: String): ResultadoApi<RechazoEntregaResponse> =
        llamarApi { RetrofitCliente.api.rechazarEntrega(aceptacionId, RechazoEntregaRequest(motivo)) }

    /** Sube la foto de referencia (UNA por reto; JPEG ya comprimido, ver prepararFoto en Fotos.kt). Repetirlo la reemplaza. */
    suspend fun subirFoto(retoId: String, jpeg: ByteArray): ResultadoApi<FotoRetoResponse> =
        llamarApi { RetrofitCliente.api.subirImagenReto(retoId, jpeg.toRequestBody("image/jpeg".toMediaType())) }
}