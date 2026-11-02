package com.example.crisalida.data

import com.google.gson.annotations.SerializedName

/* ================================================================
 * Radar de talentos: datos y repositorio.
 * (Las 3 líneas de endpoints van en ApiService, dentro de ApiCrisalida.kt)
 * ================================================================ */

// ---------- Preguntas (las fijas y las dinámicas usan EL MISMO formato)

data class OpcionDto(
    val id: String,
    val texto: String,
    val icono: String?
)

data class PreguntaDto(
    val id: String,
    val tipo: String,                 // "casillas", "opcion_unica", "chips" o "texto"
    val texto: String,
    val opciones: List<OpcionDto>,
    val placeholder: String?
)

data class Bloque1Response(val preguntas: List<PreguntaDto>)

// ---------- Lo que se ENVÍA

data class Bloque1Request(
    val actividades: List<String>,
    val tiempo: String
)

data class RespuestaDinamicaDto(
    val pregunta: String,
    val tipo: String,
    val seleccion: List<String>,
    val texto: String
)

data class FinalizarRequest(
    val actividades: List<String>,
    val tiempo: String,
    val respuestas: List<RespuestaDinamicaDto>
)

// Lo que viaja junto con la creación de la cuenta (radar completo + el perfil que se le mostró)
data class RadarFinalRequest(
    val actividades: List<String>,
    val tiempo: String,
    val respuestas: List<RespuestaDinamicaDto>,
    val perfil: PerfilResponse
)

// ---------- Lo que RESPONDE el servidor

data class Bloque2Response(
    val mensaje: String,
    val preguntas: List<PreguntaDto>,
    val fuente: String                // "ia" o "respaldo"
)

data class HabilidadDto(
    val titulo: String,
    val descripcion: String,
    val categoria: String
)

data class PerfilResponse(
    @SerializedName("mensaje_final") val mensajeFinal: String,
    @SerializedName("areas_fuertes") val areasFuertes: List<String>,
    @SerializedName("areas_oportunidad") val areasOportunidad: List<String>,
    val habilidades: List<HabilidadDto>,
    val fuente: String
)

// ---------- Lo que usa la pantalla

object RadarRepositorio {

    suspend fun bloque1(): ResultadoApi<Bloque1Response> =
        llamarApi { RetrofitCliente.api.radarBloque1() }

    suspend fun analizar(actividades: List<String>, tiempo: String): ResultadoApi<Bloque2Response> =
        llamarApi { RetrofitCliente.api.radarAnalizar(Bloque1Request(actividades, tiempo)) }

    suspend fun finalizar(
        actividades: List<String>,
        tiempo: String,
        respuestas: List<RespuestaDinamicaDto>
    ): ResultadoApi<PerfilResponse> =
        llamarApi { RetrofitCliente.api.radarFinalizar(FinalizarRequest(actividades, tiempo, respuestas)) }
}
