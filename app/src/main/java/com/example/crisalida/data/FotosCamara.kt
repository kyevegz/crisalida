package com.example.crisalida.data

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

// Fotos tomadas con la cámara al publicar. El archivo temporal vive en la carpeta PRIVADA de la app
// (caché), nunca en la galería del teléfono, y se comparte con la app de cámara mediante un FileProvider.
// Se borra al cancelar, al quitar la miniatura y al salir de la pantalla de publicar.
object FotosCamara {
    private const val CARPETA = "fotos_camara"   // debe coincidir con res/xml/file_paths.xml

    private fun autoridad(contexto: Context) = "${contexto.packageName}.fileprovider"

    fun hayCamara(contexto: Context): Boolean =
        contexto.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)

    /** Crea un archivo vacío donde la cámara dejará la foto y devuelve su dirección segura (content://). */
    fun crearArchivoTemporal(contexto: Context): Uri {
        val carpeta = File(contexto.cacheDir, CARPETA).apply { mkdirs() }
        val archivo = File.createTempFile("foto_", ".jpg", carpeta)
        return FileProvider.getUriForFile(contexto, autoridad(contexto), archivo)
    }

    // solo las fotos de nuestra cámara se pueden borrar desde aquí; nunca una foto de la galería
    fun esDeCamara(contexto: Context, uri: Uri): Boolean = uri.authority == autoridad(contexto)

    fun borrar(contexto: Context, uri: Uri?) {
        if (uri == null || !esDeCamara(contexto, uri)) return
        try {
            contexto.contentResolver.delete(uri, null, null)
        } catch (e: Exception) {
            // si ya no existe no pasa nada
        }
    }

    fun limpiarTodo(contexto: Context) {
        File(contexto.cacheDir, CARPETA).listFiles()?.forEach { it.delete() }
    }
}
