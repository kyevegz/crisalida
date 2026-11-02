package com.example.crisalida.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import com.google.gson.annotations.SerializedName
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

/* ================================================================
 * Datos y repositorios de TODO lo que no es login/radar:
 * progreso, perfil, marketplace, retos, publicaciones y bienestar.
 * (Los endpoints correspondientes están en ApiService, en ApiCrisalida.kt)
 * ================================================================ */

// ======================================================== PROGRESO / PERFIL

data class NivelInfo(
    val nivel: Int,
    val nombre: String,
    val descripcion: String?,
    val actividades: Int,
    @SerializedName("actividades_siguiente") val actividadesSiguiente: Int?,
    @SerializedName("siguiente_nombre") val siguienteNombre: String?,
    val porcentaje: Int
)

data class LiluInfo(
    val nombre: String,
    val flores: Int,
    val etapa: Int,                                   // 1 Huevo · 2 Larva · 3 Mariposa
    @SerializedName("etapa_nombre") val etapaNombre: String,
    @SerializedName("flores_siguiente") val floresSiguiente: Int?,
    @SerializedName("siguiente_nombre") val siguienteNombre: String?,
    val porcentaje: Int
)

data class EstadisticasInfo(
    @SerializedName("retos_completados") val retosCompletados: Int,
    @SerializedName("ventas_completadas") val ventasCompletadas: Int,
    @SerializedName("calificacion_promedio") val calificacionPromedio: Double?
)

data class ProgresoResponse(
    val seudonimo: String,
    val avatar: String,
    val nivel: NivelInfo,
    val lilu: LiluInfo,
    val estadisticas: EstadisticasInfo
)

/** Lo que devuelve GET radar/resultado (las habilidades que se detectaron en el radar). */
data class ResultadoRadarResponse(
    @SerializedName("areas_fuertes") val areasFuertes: List<String>,
    @SerializedName("areas_oportunidad") val areasOportunidad: List<String>,
    val habilidades: List<HabilidadDto>
)

data class NivelMapaDto(
    val nivel: Int,
    val nombre: String,
    val descripcion: String?,
    @SerializedName("actividades_requeridas") val actividadesRequeridas: Int
)
data class EtapaLiluDto(
    val etapa: Int,
    val nombre: String,
    @SerializedName("flores_requeridas") val floresRequeridas: Int
)
data class NivelesResponse(
    val niveles: List<NivelMapaDto>,
    @SerializedName("etapas_lilu") val etapasLilu: List<EtapaLiluDto>
)

/** Flores que da cada acción: venta, reto, publicacion, nivel, bienestar_emocion, bienestar_monitoreo, ... */
data class ReglasResponse(val reglas: Map<String, Int>)

data class AvatarRequest(val avatar: String)
data class AvatarResponse(val avatar: String)

// ======================================================== MARKETPLACE / PORTAFOLIO

data class CategoriaDto(val id: Int, val ambito: String, val nombre: String)
data class CategoriasResponse(val categorias: List<CategoriaDto>)

data class PublicacionDto(
    val id: String,
    val tipo: String,                                  // "producto" o "servicio"
    val titulo: String,
    val descripcion: String?,
    val precio: Double,
    @SerializedName("nota_precio") val notaPrecio: String?,
    @SerializedName("opcion_entrega") val opcionEntrega: Int?,            // productos: 1 domicilio · 2 guía ciega · 3 encargo
    @SerializedName("modalidad_servicio") val modalidadServicio: Int?,    // servicios: 1 en mi casa · 2 a domicilio
    @SerializedName("esquema_cobro") val esquemaCobro: Int?,
    @SerializedName("tiempo_elaboracion_horas") val tiempoElaboracionHoras: Int?,
    @SerializedName("zona_publica") val zonaPublica: String?,
    @SerializedName("categoria_id") val categoriaId: Int,
    val categoria: String,
    val vendedora: String?,
    @SerializedName("vendedora_avatar") val vendedoraAvatar: String?,
    @SerializedName("es_mia") val esMia: Boolean,
    val vendidos: Int,
    val calificacion: Double?,
    @SerializedName("por_encargo") val porEncargo: Boolean,
    val imagenes: List<String>,                        // rutas tipo "/imagenes/<id>" (ver FotoRemota)
    // Solo en "mis publicaciones" (portafolio):
    val estado: String?,
    @SerializedName("pedidos_pendientes") val pedidosPendientes: Int?,
    @SerializedName("punto_entrega") val puntoEntrega: String?,
    @SerializedName("costo_insumos") val costoInsumos: Double? = null,
    @SerializedName("horas_dedicacion") val horasDedicacion: Double? = null,
    @SerializedName("creada_en") val creadaEn: String? = null
)

data class CatalogoResponse(val publicaciones: List<PublicacionDto>)

data class PublicacionRequest(
    val tipo: String,
    val categoria: String,
    val titulo: String,
    val descripcion: String,
    val precio: Double,
    @SerializedName("nota_precio") val notaPrecio: String? = null,
    @SerializedName("opcion_entrega") val opcionEntrega: Int? = null,
    @SerializedName("modalidad_servicio") val modalidadServicio: Int? = null,
    @SerializedName("esquema_cobro") val esquemaCobro: Int? = null,
    @SerializedName("costo_insumos") val costoInsumos: Double? = null,
    @SerializedName("horas_dedicacion") val horasDedicacion: Double? = null,
    @SerializedName("tiempo_elaboracion_horas") val tiempoElaboracionHoras: Int? = null,
    @SerializedName("zona_publica") val zonaPublica: String? = null,
    @SerializedName("punto_entrega") val puntoEntrega: String? = null
)

data class PublicacionCreadaResponse(
    val id: String,
    val estado: String,                                // "en_revision" o "activo"
    @SerializedName("flores_ganadas") val floresGanadas: Int
)

data class PublicacionEditadaResponse(
    val id: String,
    val estado: String,
    @SerializedName("reenviada_a_revision") val reenviadaARevision: Boolean
)

data class ImagenSubidaResponse(val id: String, val orden: Int, val url: String)
data class EstadoRequest(val estado: String)          // "activo" o "pausado"
data class EstadoResponse(val estado: String)

// ======================================================== RETOS

data class RetoDto(
    @SerializedName("reto_id") val retoId: String,
    val titulo: String,
    val descripcion: String,
    val compensacion: Double,
    @SerializedName("duracion_min") val duracionMin: Int,
    val dificultad: String,                            // "facil" | "media" | "dificil"
    @SerializedName("habilidad_requerida") val habilidadRequerida: String?,
    val etiqueta: String?,
    @SerializedName("fecha_limite") val fechaLimite: String,
    @SerializedName("categoria_id") val categoriaId: Int,
    val categoria: String,
    val contratista: String,
    // Solo en "mis retos":
    @SerializedName("aceptacion_id") val aceptacionId: String?,
    val estado: String?,                               // aceptado | entregado | validado | abandonado
    // Foto del reto (ruta tipo "/retos/<id>/imagen", ver FotoRemota) y, en el detalle, mi estado en el reto:
    @SerializedName("imagen_ruta") val imagenRuta: String? = null,
    @SerializedName("mi_estado") val miEstado: String? = null
)

data class RetosResponse(val retos: List<RetoDto>)
data class AceptacionResponse(@SerializedName("aceptacion_id") val aceptacionId: String, val estado: String)

// ======================================================== BIENESTAR

data class EmocionRequest(val emocion: String)
data class MonitoreoRequest(val tension: Int, val claridad: Int, val energia: Int)
data class ReflexionRequest(val respuestas: List<String>)
data class BitacoraRequest(val texto: String)

data class FloresResponse(
    val id: String? = null,
    @SerializedName("flores_ganadas") val floresGanadas: Int,
    @SerializedName("ya_registrado_hoy") val yaRegistradoHoy: Boolean
)

data class BienestarHoyResponse(
    @SerializedName("flores_cobradas") val floresCobradas: Map<String, Boolean>,   // emocion, monitoreo, reflexion, bitacora
    @SerializedName("registros_hoy") val registrosHoy: Map<String, Int>
)

/** Un registro del historial. Según el bloque vienen unos campos u otros. */
data class EntradaBienestar(
    val id: String,
    val subtipo: String,                               // emocion | monitoreo | reflexion | bitacora
    val dia: String,                                   // "2026-11-02"
    val hora: String,                                  // "18:45" (hora de México)
    val emocion: String?,
    val tension: Int?,
    val claridad: Int?,
    val energia: Int?,
    val texto: String?,                                // bitácora
    val respuestas: List<String>?                      // reflexión (una por pregunta)
)
data class EntradasBienestarResponse(val entradas: List<EntradaBienestar>)
data class BorradoResponse(val borrada: Boolean)

// ======================================================== REPOSITORIOS

/** Se sube de número cada vez que la usuaria gana flores o avanza; las pantallas que muestran
 *  progreso lo leen y se refrescan solas. */
object ProgresoRepositorio {
    var version by mutableIntStateOf(0)
        private set

    fun avisarCambio() { version++ }

    suspend fun yo(): ResultadoApi<ProgresoResponse> = llamarApi { RetrofitCliente.api.progresoYo() }

    suspend fun habilidades(): ResultadoApi<ResultadoRadarResponse> = llamarApi { RetrofitCliente.api.radarResultado() }

    suspend fun reglas(): ResultadoApi<ReglasResponse> = llamarApi { RetrofitCliente.api.reglasFlores() }

    suspend fun niveles(): ResultadoApi<NivelesResponse> = llamarApi { RetrofitCliente.api.niveles() }

    suspend fun cambiarAvatar(emoji: String): ResultadoApi<AvatarResponse> =
        llamarApi { RetrofitCliente.api.cambiarAvatar(AvatarRequest(emoji)) }
            .also { if (it is ResultadoApi.Exito) { SesionActual.avatar = it.datos.avatar; avisarCambio() } }
}

object MarketplaceRepositorio {
    suspend fun categorias(ambito: String? = null): ResultadoApi<CategoriasResponse> =
        llamarApi { RetrofitCliente.api.categorias(ambito) }

    suspend fun detalle(id: String): ResultadoApi<PublicacionDto> = llamarApi { RetrofitCliente.api.marketplaceDetalle(id) }

    suspend fun catalogo(tipo: String? = null, categoriaId: Int? = null, busqueda: String? = null): ResultadoApi<CatalogoResponse> =
        llamarApi { RetrofitCliente.api.marketplace(tipo, categoriaId, busqueda?.takeIf { it.isNotBlank() }) }
}

object RetosRepositorio {
    suspend fun tablero(categoriaId: Int? = null, dificultad: String? = null, busqueda: String? = null): ResultadoApi<RetosResponse> =
        llamarApi { RetrofitCliente.api.retos(categoriaId, dificultad, busqueda?.takeIf { it.isNotBlank() }) }

    suspend fun mis(): ResultadoApi<RetosResponse> = llamarApi { RetrofitCliente.api.misRetos() }

    suspend fun detalle(retoId: String): ResultadoApi<RetoDto> = llamarApi { RetrofitCliente.api.retoDetalle(retoId) }

    suspend fun aceptar(retoId: String): ResultadoApi<AceptacionResponse> = llamarApi { RetrofitCliente.api.aceptarReto(retoId) }

    suspend fun entregar(aceptacionId: String): ResultadoApi<EstadoResponse> = llamarApi { RetrofitCliente.api.entregarReto(aceptacionId) }

    suspend fun abandonar(aceptacionId: String): ResultadoApi<EstadoResponse> = llamarApi { RetrofitCliente.api.abandonarReto(aceptacionId) }
}

object PublicacionesRepositorio {
    suspend fun publicar(datos: PublicacionRequest): ResultadoApi<PublicacionCreadaResponse> =
        llamarApi { RetrofitCliente.api.publicar(datos) }
            .also { if (it is ResultadoApi.Exito) ProgresoRepositorio.avisarCambio() }

    suspend fun mias(): ResultadoApi<CatalogoResponse> = llamarApi { RetrofitCliente.api.misPublicaciones() }

    /** Guarda los cambios de una publicación propia (el tipo producto/servicio no cambia). */
    suspend fun editar(id: String, datos: PublicacionRequest): ResultadoApi<PublicacionEditadaResponse> =
        llamarApi { RetrofitCliente.api.editarPublicacion(id, datos) }
            .also { if (it is ResultadoApi.Exito) ProgresoRepositorio.avisarCambio() }

    /** [imagenRuta] es la ruta que manda el servidor, p. ej. "/imagenes/<id>". */
    suspend fun borrarFoto(publicacionId: String, imagenRuta: String): ResultadoApi<BorradoResponse> =
        llamarApi { RetrofitCliente.api.borrarImagen(publicacionId, imagenRuta.substringAfterLast('/')) }
            .also { if (it is ResultadoApi.Exito) ProgresoRepositorio.avisarCambio() }

    suspend fun cambiarEstado(id: String, estado: String): ResultadoApi<EstadoResponse> =
        llamarApi { RetrofitCliente.api.cambiarEstadoPublicacion(id, EstadoRequest(estado)) }

    /** Sube una foto ya comprimida (JPEG, máx. 400 KB). Ver prepararFoto() en Fotos.kt. */
    suspend fun subirFoto(publicacionId: String, jpeg: ByteArray): ResultadoApi<ImagenSubidaResponse> =
        llamarApi { RetrofitCliente.api.subirImagen(publicacionId, jpeg.toRequestBody("image/jpeg".toMediaType())) }
            .also { if (it is ResultadoApi.Exito) ProgresoRepositorio.avisarCambio() }
}

object BienestarRepositorio {
    private fun <T> ResultadoApi<T>.avisandoFlores(): ResultadoApi<T> {
        if (this is ResultadoApi.Exito && ((datos as? FloresResponse)?.floresGanadas ?: 0) > 0) ProgresoRepositorio.avisarCambio()
        return this
    }

    suspend fun hoy(): ResultadoApi<BienestarHoyResponse> = llamarApi { RetrofitCliente.api.bienestarHoy() }

    suspend fun guardarEmocion(emocion: String): ResultadoApi<FloresResponse> =
        llamarApi { RetrofitCliente.api.guardarEmocion(EmocionRequest(emocion)) }.avisandoFlores()

    suspend fun guardarMonitoreo(tension: Int, claridad: Int, energia: Int): ResultadoApi<FloresResponse> =
        llamarApi { RetrofitCliente.api.guardarMonitoreo(MonitoreoRequest(tension, claridad, energia)) }.avisandoFlores()

    suspend fun guardarReflexion(respuestas: List<String>): ResultadoApi<FloresResponse> =
        llamarApi { RetrofitCliente.api.guardarReflexion(ReflexionRequest(respuestas)) }.avisandoFlores()

    suspend fun guardarBitacora(texto: String): ResultadoApi<FloresResponse> =
        llamarApi { RetrofitCliente.api.guardarBitacora(BitacoraRequest(texto)) }.avisandoFlores()

    suspend fun entradas(subtipo: String): ResultadoApi<EntradasBienestarResponse> =
        llamarApi { RetrofitCliente.api.entradasBienestar(subtipo) }

    suspend fun borrarEntrada(id: String): ResultadoApi<BorradoResponse> = llamarApi { RetrofitCliente.api.borrarEntradaBienestar(id) }
}
