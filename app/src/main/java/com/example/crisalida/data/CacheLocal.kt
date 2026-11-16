package com.example.crisalida.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser

/**
 * Último contenido guardado en el teléfono para verlo SIN conexión (issue 5.5).
 *
 * Solo se guarda lo que la usuaria necesita para seguir viendo lo suyo: la mascota y su progreso, el portafolio y la
 * bitácora reciente. NO se guardan pagos, compras, pedidos ni retos: sin conexión no se muestran como si fueran actuales.
 *
 * - Va cifrado con el mismo Keystore de AlmacenamientoCifrado (issue 5.3) y ligado a la usuaria; cerrar sesión desde
 *   Perfil y la salida de emergencia lo borran junto con todo lo demás.
 * - Cada copia guarda cuándo se bajó del servidor, para mostrar «Última actualización».
 * - Si la usuaria borra su bitácora, la copia local también se borra (ver BienestarRepositorio).
 */
class CacheDe<T>(
    val nombre: String,
    val clase: Class<T>,
    /** Recorta lo que se guarda (p. ej. solo los registros más recientes). */
    val reducir: (T) -> T = { it }
)

class GuardadoLocal<T>(val datos: T, val actualizadoEn: Long)

object Caches {
    val progreso = CacheDe("progreso", ProgresoResponse::class.java)
    val portafolio = CacheDe("portafolio", CatalogoResponse::class.java)

    /** Solo los 20 registros más recientes de la bitácora (es lo más privado: se guarda lo mínimo). */
    val bitacora = CacheDe("bitacora", EntradasBienestarResponse::class.java) { r ->
        EntradasBienestarResponse(r.entradas.sortedByDescending { it.dia + " " + it.hora }.take(20))
    }
}

object CacheLocal {
    private val gson = Gson()
    private var contexto: Context? = null

    fun iniciar(c: Context) {
        contexto = c.applicationContext
    }

    private fun clave(nombre: String) = "cache_$nombre"

    fun <T> guardar(c: Context, cache: CacheDe<T>, datos: T) {
        try {
            val sobre = JsonObject()
            sobre.addProperty("t", System.currentTimeMillis())
            sobre.add("d", gson.toJsonTree(cache.reducir(datos)))
            AlmacenamientoCifrado.guardar(c, clave(cache.nombre), sobre.toString())
        } catch (e: Exception) {
            // si no se pudo guardar, simplemente no habrá copia: nunca debe romper la pantalla
        }
    }

    fun <T> leer(c: Context, cache: CacheDe<T>): GuardadoLocal<T>? {
        return try {
            val texto = AlmacenamientoCifrado.leer(c, clave(cache.nombre)) ?: return null
            val sobre = JsonParser.parseString(texto).asJsonObject
            val datos = gson.fromJson(sobre.get("d"), cache.clase) ?: return null
            GuardadoLocal(datos, sobre.get("t").asLong)
        } catch (e: Exception) {
            null
        }
    }

    fun borrar(cache: CacheDe<*>) {
        val c = contexto ?: return
        AlmacenamientoCifrado.borrar(c, clave(cache.nombre))
    }
}