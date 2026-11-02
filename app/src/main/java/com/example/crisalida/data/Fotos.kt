package com.example.crisalida.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.ByteArrayOutputStream

/* ================================================================
 * Fotos de productos/servicios, sin librerías extra.
 *  - prepararFoto(): reduce y comprime la foto elegida (máx. ~380 KB) antes de subirla.
 *    Al volver a codificarla se pierden los metadatos (ubicación GPS, modelo del celular...): privacidad.
 *  - FotoRemota(): descarga y muestra una foto del servidor (con caché en memoria).
 * ================================================================ */

private const val LADO_MAXIMO = 1280
private const val BYTES_MAXIMOS = 380 * 1024

/** Devuelve la foto como JPEG liviano, o null si no se pudo leer. Llámala en un hilo de fondo (Dispatchers.IO). */
fun prepararFoto(contexto: Context, uri: Uri): ByteArray? {
    return try {
        val resolver = contexto.contentResolver
        // 1) Leer solo el tamaño para saber cuánto reducir
        val medidas = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, medidas) }
        if (medidas.outWidth <= 0 || medidas.outHeight <= 0) return null
        var muestreo = 1
        while (maxOf(medidas.outWidth, medidas.outHeight) / muestreo > LADO_MAXIMO * 2) muestreo *= 2

        // 2) Decodificar reducida
        val opciones = BitmapFactory.Options().apply { inSampleSize = muestreo }
        val original = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opciones) } ?: return null

        // 3) Ajustar al lado máximo
        val escala = LADO_MAXIMO.toFloat() / maxOf(original.width, original.height)
        val bmp = if (escala < 1f) Bitmap.createScaledBitmap(original, (original.width * escala).toInt(), (original.height * escala).toInt(), true) else original

        // 4) Bajar la calidad hasta que quepa
        var calidad = 85
        var bytes: ByteArray
        do {
            val salida = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.JPEG, calidad, salida)
            bytes = salida.toByteArray()
            calidad -= 10
        } while (bytes.size > BYTES_MAXIMOS && calidad >= 25)
        if (bytes.size > BYTES_MAXIMOS) null else bytes
    } catch (e: Exception) {
        null
    }
}

private val cacheFotos = object : LruCache<String, ImageBitmap>(24) {}
private val clienteFotos by lazy { OkHttpClient() }   // las fotos son públicas: no necesitan token

/** Muestra la foto que está en [ruta] (por ejemplo "/imagenes/abc..."). Mientras carga, o si falla, muestra [alternativa]. */
@Composable
fun FotoRemota(
    ruta: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    alternativa: @Composable () -> Unit = {}
) {
    val imagen by produceState<ImageBitmap?>(initialValue = ruta?.let { cacheFotos.get(it) }, key1 = ruta) {
        if (ruta == null) { value = null; return@produceState }
        cacheFotos.get(ruta)?.let { value = it; return@produceState }
        value = withContext(Dispatchers.IO) {
            try {
                val url = RetrofitCliente.BASE_URL.trimEnd('/') + "/" + ruta.trimStart('/')
                clienteFotos.newCall(Request.Builder().url(url).build()).execute().use { r ->
                    if (!r.isSuccessful) null
                    else r.body?.bytes()?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }?.asImageBitmap()
                        ?.also { cacheFotos.put(ruta, it) }
                }
            } catch (e: Exception) {
                null
            }
        }
    }
    val cargada = imagen
    if (cargada != null) {
        Image(bitmap = cargada, contentDescription = null, modifier = modifier, contentScale = contentScale)
    } else {
        alternativa()
    }
}

/** Vista previa de una foto que la usuaria acaba de elegir (todavía en su teléfono). */
@Composable
fun MiniaturaLocal(uri: Uri, modifier: Modifier = Modifier) {
    val contexto = LocalContext.current
    val imagen by produceState<ImageBitmap?>(initialValue = null, key1 = uri) {
        value = withContext(Dispatchers.IO) {
            try {
                val opciones = BitmapFactory.Options().apply { inSampleSize = 4 }
                contexto.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opciones) }?.asImageBitmap()
            } catch (e: Exception) {
                null
            }
        }
    }
    imagen?.let { Image(bitmap = it, contentDescription = null, modifier = modifier, contentScale = ContentScale.Crop) }
}
