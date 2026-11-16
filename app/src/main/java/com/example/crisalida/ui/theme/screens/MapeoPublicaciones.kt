package com.example.crisalida.ui.theme.screens

import com.example.crisalida.data.PublicacionDto
import java.util.Locale

//pasa lo del servidor a los textos que usan las tarjetas

fun formatoPrecio(precio: Double): String = "$" + String.format(Locale.US, "%,.2f", precio)


fun PublicacionDto.precioTexto(): String =
    formatoPrecio(precio) + (notaPrecio?.takeIf { it.isNotBlank() }?.let { " $it" } ?: "")

//estado como lo ven las tarjetas
fun PublicacionDto.estadoTexto(): String = when (estado) {
    "pausado" -> "Pausado"
    "en_revision" -> "En revisión"
    "rechazado" -> "Rechazado"
    else -> if (porEncargo) "Por encargo" else "Activo"
}

fun PublicacionDto.ventasTexto(): String =
    if (tipo == "servicio") "$vendidos completados" else "$vendidos vendidos"

//línea de entrega debajo del precio
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

//la zona, nunca la dirección
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

/**
 * [sinActualizar] = true cuando se muestran datos guardados (sin conexión): las ventas y los pedidos son cosas que
 * cambian solas, así que NO se muestran como actuales (sin número de ventas y «Pedidos» sin cantidad).
 */
fun PublicacionDto.aPortafolioUi(sinActualizar: Boolean = false) = PublicacionPortafolio(
    id = id,
    titulo = titulo,
    precio = precioTexto(),
    estado = estadoTexto(),
    detalleVentas = if (sinActualizar) "" else ventasTexto(),
    ubicacionDetalle = detalleEntregaTexto(),
    pedidosCount = if (sinActualizar) -1 else (pedidosPendientes ?: 0),   // -1 = no mostrar la cantidad
    imagenRuta = imagenes.firstOrNull()
)