package com.example.crisalida.data.model

data class HabilidadDto(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val categoria: String
)

data class PerfilCompletoResponse(
    val seudonimo: String,
    val avatar: String,
    val nivel: Int,
    val nombre_nivel: String,
    val descripcion_nivel: String,
    val flores: Int,
    val actividades_completadas: Int,
    val siguiente_nivel: String?,
    val actividades_faltantes: Int,
    val nivel_maximo: Boolean,
    val areas_fuertes: List<String>,
    val areas_oportunidad: List<String>,
    val habilidades: List<HabilidadDto>
)