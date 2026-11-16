package com.example.crisalida.data

import com.google.gson.annotations.SerializedName

/* ================================================================
 * Radar de talentos: datos y repositorio.
 * (Las 3 líneas de endpoints van en ApiService, dentro de ApiCrisalida.kt)
 * ================================================================ */

// ---------- Preguntas (las fijas y las dinámicas usan EL MISMO formato).
// Las fijas del Bloque I son 4: actividades, tiempo y las 2 abiertas «¿Qué te gusta hacer?» (id "gusta") y «¿Qué sabes hacer?» (id "sabe").

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

/**
 * Las 2 preguntas abiertas fijas (8.1) SIEMPRE están en el Bloque I. Normalmente las manda el servidor; si no las manda
 * (un servidor que aún no se actualiza) la app las agrega con el mismo texto, así la usuaria nunca se queda sin ellas.
 */
fun List<PreguntaDto>.conAbiertasFijas(): List<PreguntaDto> {
    val faltantes = mutableListOf<PreguntaDto>()
    if (this.none { p -> p.id == "gusta" }) {
        faltantes.add(PreguntaDto("gusta", "texto", "¿Qué te gusta hacer?", emptyList(), "Cuéntanos con tus palabras..."))
    }
    if (this.none { p -> p.id == "sabe" }) {
        faltantes.add(PreguntaDto("sabe", "texto", "¿Qué sabes hacer?", emptyList(), "Lo que sabes hacer, aunque nunca lo hayas cobrado..."))
    }
    return this + faltantes
}

// ---------- Lo que se ENVÍA

data class Bloque1Request(
    val actividades: List<String>,
    val tiempo: String,
    val gusta: String,                // ¿Qué te gusta hacer? (obligatoria, 3 a 300 caracteres)
    val sabe: String                  // ¿Qué sabes hacer? (obligatoria, 3 a 300 caracteres)
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
    val gusta: String,
    val sabe: String,
    val respuestas: List<RespuestaDinamicaDto>
)

// Lo que viaja junto con la creación de la cuenta (radar completo + el perfil que se le mostró)
data class RadarFinalRequest(
    val actividades: List<String>,
    val tiempo: String,
    val gusta: String,
    val sabe: String,
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

    suspend fun analizar(actividades: List<String>, tiempo: String, gusta: String, sabe: String): ResultadoApi<Bloque2Response> =
        llamarApi { RetrofitCliente.api.radarAnalizar(Bloque1Request(actividades, tiempo, gusta, sabe)) }

    suspend fun finalizar(
        actividades: List<String>,
        tiempo: String,
        gusta: String,
        sabe: String,
        respuestas: List<RespuestaDinamicaDto>
    ): ResultadoApi<PerfilResponse> =
        llamarApi { RetrofitCliente.api.radarFinalizar(FinalizarRequest(actividades, tiempo, gusta, sabe, respuestas)) }
}
