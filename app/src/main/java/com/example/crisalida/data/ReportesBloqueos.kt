package com.example.crisalida.data

import com.google.gson.annotations.SerializedName

/* ================================================================
 * Reportar y bloquear (issue 4.9). Las rutas están en el backend (reportes.py).
 * La persona reportada o bloqueada nunca se entera; las usuarias no ven ids de otras: se identifican por una
 * publicación o un reto suyos.
 * ================================================================ */

data class MotivoReporteDto(
    val clave: String,
    val titulo: String,
    val descripcion: String = "",
    val riesgo: Boolean = false
)

data class MotivosResponse(val motivos: List<MotivoReporteDto> = emptyList())

data class ReporteRequest(
    val tipo: String,                                   // "publicacion" | "reto"
    @SerializedName("objetivo_id") val objetivoId: String,
    val motivo: String,
    val detalle: String? = null,
    val bloquear: Boolean = false,
    @SerializedName("revision_prioritaria") val revisionPrioritaria: Boolean = false
)

data class ReporteResponse(val id: Int = 0, val bloqueada: Boolean = false, val mensaje: String = "")

/** Se manda UNO de los dos: se bloquea a quien publicó esa publicación o ese reto. */
data class BloqueoRequest(
    @SerializedName("publicacion_id") val publicacionId: String? = null,
    @SerializedName("reto_id") val retoId: String? = null
)

data class BloqueoCreadoResponse(val id: Int = 0, val seudonimo: String = "", val avatar: String? = null, val bloqueada: Boolean = true)

data class BloqueoDto(
    val id: Int,
    val seudonimo: String,
    val avatar: String? = null,
    @SerializedName("creado_en") val creadoEn: String = ""
)

data class BloqueosResponse(val bloqueos: List<BloqueoDto> = emptyList())
data class DesbloqueoResponse(val bloqueada: Boolean = false)

// Los mismos textos que el servidor, por si no hay conexión al abrir la ventana de reporte
val MotivosRespaldo = listOf(
    MotivoReporteDto("acoso", "Mensaje intimidante, hostigamiento o acoso",
        "Tono demandante, presión psicológica, amenazas encubiertas o comentarios que vulneran tu paz.", true),
    MotivoReporteDto("entrega_insegura", "Insiste en entrega en domicilio o punto no seguro",
        "Rechaza acudir a Puntos Violeta, estaciones protegidas o casilleros aliados comunitarios."),
    MotivoReporteDto("cobro_indebido", "Intento de cobro indebido o regateo abusivo",
        "No respeta el precio pactado, exige pagos no registrados o intenta eludir la custodia segura."),
    MotivoReporteDto("contacto_externo", "Pide teléfono personal o contacto externo",
        "Solicita WhatsApp, llamada privada o redes sociales fuera de los canales de protección autorizados.", true),
    MotivoReporteDto("otro", "Otro motivo que me genera desconfianza",
        "Cualquier intuición o comportamiento extraño que merezca la revisión de una orientadora.")
)

object SeguridadRepositorio {
    suspend fun motivos(): ResultadoApi<MotivosResponse> = llamarApi { RetrofitCliente.api.motivosReporte() }

    suspend fun reportar(datos: ReporteRequest): ResultadoApi<ReporteResponse> = llamarApi { RetrofitCliente.api.reportar(datos) }

    suspend fun bloquearPorPublicacion(id: String): ResultadoApi<BloqueoCreadoResponse> =
        llamarApi { RetrofitCliente.api.bloquear(BloqueoRequest(publicacionId = id)) }

    suspend fun bloquearPorReto(id: String): ResultadoApi<BloqueoCreadoResponse> =
        llamarApi { RetrofitCliente.api.bloquear(BloqueoRequest(retoId = id)) }

    suspend fun bloqueos(): ResultadoApi<BloqueosResponse> = llamarApi { RetrofitCliente.api.misBloqueos() }

    suspend fun desbloquear(id: Int): ResultadoApi<DesbloqueoResponse> = llamarApi { RetrofitCliente.api.desbloquear(id) }
}