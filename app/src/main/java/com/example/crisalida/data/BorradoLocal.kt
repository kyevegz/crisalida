package com.example.crisalida.data

import android.content.Context

/**
 * UN solo lugar para borrar todo lo que la app guarda en el teléfono. La salida de emergencia y cerrar sesión
 * lo llaman igual, para que no se les olvide nada a uno de los dos.
 */
object BorradoLocal {
    /**
     * Borrador(es), cola de envíos (disco, memoria y envío programado), fotos pendientes, último contenido y clave.
     * Llamar ANTES de SesionActual.cerrar(). Devuelve true si se comprobó que no quedó nada.
     */
    fun todo(contexto: Context): Boolean {
        ColaEnvios.alBorrarTodo()
        Borradores.porAbrir = null
        return AlmacenamientoCifrado.borrarTodo(contexto)
    }
}