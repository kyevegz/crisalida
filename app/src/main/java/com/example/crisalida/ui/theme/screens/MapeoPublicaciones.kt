package com.example.crisalida.ui.theme.screens

import com.example.crisalida.data.PublicacionDto
import java.util.Locale

/* ================================================================
 * Convierte lo que manda el servidor (PublicacionDto) en los textos que
 * ya usan tus tarjetas. Un solo lugar para que Perfil, Portafolio y
 * Marketplace muestren lo mismo.
 * ================================================================ */

fun formatoPrecio(precio: Double): String = "$" + String.format(Locale.US, "%,.2f", precio)

/** "$45.00 c/litro" */
fun PublicacionDto.precioTexto(): String =
    formatoPrecio(precio) + (notaPrecio?.takeIf { it.isNotBlank() }?.let { " $it" } ?: "")

/** Estado como lo muestran las tarjetas: Activo · Por encargo · Pausado · En revisión · Rechazado */
fun PublicacionDto.estadoTexto(): String = when (estado) {
    "pausado" -> "Pausado"
    "en_revision" -> "En revisión"
    "rechazado" -> "Rechazado"
    else -> if (porEncargo) "Por encargo" else "Activo"
}

/** "8 vendidos" (productos) o "3 completados" (servicios) */
fun PublicacionDto.ventasTexto(): String =
    if (tipo == "servicio") "$vendidos completados" else "$vendidos vendidos"

/** Línea de entrega/modalidad que va debajo del precio en el portafolio (la usuaria ve su dirección real). */
fun PublicacionDto.detalleEntregaTexto(): String {
    val lugar = puntoEntrega?.takeIf { it.isNotBlank() } ?: zonaPublica?.takeIf { it.isNotBlank() }
    return if (tipo == "servicio") {
        val base = if (modalidadServicio == 2) "A domicilio de la compradora" else "Atención en tu espacio de trabajo"
        if (lugar != null && modalidadServicio != 2) "$base • $lugar" else base
    } else when (opcionEntrega) {
        1 -> "Recoger en domicilio" + (lugar?.let { ": $it" } ?: "")
        3 -> "Elaboración: ${tiempoElaboracionHoras ?: 24} hrs anticipación" + (lugar?.let { " • $it" } ?: "")
        else -> "Envío por guía ciega (paquetería)"
    }
}

/** Línea pública de la tarjeta del marketplace: NUNCA la dirección real, solo la zona. */
fun PublicacionDto.entregaPublicaTexto(): String {
    val zona = zonaPublica?.takeIf { it.isNotBlank() }
    return if (tipo == "servicio") {
        if (modalidadServicio == 2) "Va a tu domicilio" + (zona?.let { " • $it" } ?: "")
        else "Atiende en " + (zona ?: "su espacio de trabajo")
    } else when (opcionEntrega) {
        1 -> "Recoge en " + (zona ?: "su domicilio")
        3 -> "Por encargo" + (zona?.let { " • $it" } ?: "")
        else -> "Envío por paquetería"
    }
}

fun PublicacionDto.aPortafolioUi() = PublicacionPortafolio(
    id = id,
    titulo = titulo,
    precio = precioTexto(),
    estado = estadoTexto(),
    detalleVentas = ventasTexto(),
    ubicacionDetalle = detalleEntregaTexto(),
    pedidosCount = pedidosPendientes ?: 0,
    imagenRuta = imagenes.firstOrNull()
)
