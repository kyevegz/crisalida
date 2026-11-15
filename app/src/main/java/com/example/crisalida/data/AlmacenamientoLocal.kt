package com.example.crisalida.data

import android.content.Context
import java.io.File
import java.util.Locale

/**
 * Limpieza de los datos temporales que la app guarda en el teléfono ("Limpiar caché").
 * Solo toca la carpeta de caché y las fotos en memoria: NO cierra la sesión, NO borra los ajustes
 * (tamaño de texto, inactividad, capturas) ni nada de la cuenta o del servidor.
 */
object AlmacenamientoLocal {

    /** Borra la caché y devuelve cuántos bytes se liberaron. Es trabajo de disco: llámala fuera del hilo principal. */
    fun limpiarCache(contexto: Context): Long {
        var liberados = 0L
        contexto.cacheDir?.listFiles()?.forEach { liberados += borrarTodo(it) }
        vaciarCacheFotos()
        return liberados
    }

    // borra un archivo o una carpeta completa y suma lo que pesaba
    private fun borrarTodo(archivo: File): Long {
        var peso = 0L
        if (archivo.isDirectory) archivo.listFiles()?.forEach { peso += borrarTodo(it) } else peso = archivo.length()
        archivo.delete()
        return peso
    }

    /** "1.5 MB", "230 KB" o "menos de 1 KB", para decirle a la usuaria cuánto espacio se liberó. */
    fun formatoTamano(bytes: Long): String = when {
        bytes < 1024 -> "menos de 1 KB"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> String.format(Locale("es", "MX"), "%.1f MB", bytes / (1024.0 * 1024.0))
    }
}
