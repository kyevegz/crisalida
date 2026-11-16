package com.example.crisalida.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/**
 * Borradores de publicación: puede haber varios a la vez.
 * Todo vive en UNA sola entrada de AlmacenamientoCifrado (AES-256-GCM con llave del Keystore),
 * así que se borra junto con el resto al cerrar sesión o salir de emergencia.
 * Nunca se guardan fotos ni la dirección privada.
 */
object Borradores {
    const val MAXIMO = 10
    private const val LISTA = "borradores_publicacion"
    private const val UNICO_ANTIGUO = "borrador_publicacion"
    private const val INTERRUMPIDO = "borrador_interrumpido"
    private val candado = Any()

    /** Borrador que Inicio debe abrir cuando la usuaria elige «Continuar» (solo en memoria). */
    var porAbrir: String? = null

    data class Borrador(val id: String, val actualizadoEn: Long, val datos: String) {
        private fun json() = try { JSONObject(datos) } catch (e: Exception) { JSONObject() }
        val titulo: String
            get() {
                val j = json()
                return j.optString("titulo").ifBlank { j.optString("descripcion") }.ifBlank { "Borrador sin título" }
            }
    }

    private fun leerLista(contexto: Context): MutableList<Borrador> {
        val texto = AlmacenamientoCifrado.leer(contexto, LISTA) ?: return mutableListOf()
        return try {
            val arr = JSONArray(texto)
            val lista = mutableListOf<Borrador>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                lista.add(Borrador(o.getString("id"), o.optLong("actualizadoEn"), o.getString("datos")))
            }
            lista
        } catch (e: Exception) { mutableListOf() }
    }

    private fun escribirLista(contexto: Context, lista: List<Borrador>): Boolean {
        val arr = JSONArray()
        lista.forEach { arr.put(JSONObject().put("id", it.id).put("actualizadoEn", it.actualizadoEn).put("datos", it.datos)) }
        return AlmacenamientoCifrado.guardar(contexto, LISTA, arr.toString())
    }

    /** Los más recientes primero. */
    fun listar(contexto: Context): List<Borrador> = synchronized(candado) {
        leerLista(contexto).sortedByDescending { it.actualizadoEn }
    }

    fun lleno(contexto: Context): Boolean = synchronized(candado) { leerLista(contexto).size >= MAXIMO }

    /** Crea (id = null) o actualiza un borrador. Devuelve su id, o null si no se pudo guardar. */
    fun guardar(contexto: Context, id: String?, datos: String): String? = synchronized(candado) {
        val lista = leerLista(contexto)
        val existente = id?.let { buscado -> lista.indexOfFirst { it.id == buscado } } ?: -1
        val idFinal = if (existente >= 0) id!! else UUID.randomUUID().toString()
        val nuevo = Borrador(idFinal, System.currentTimeMillis(), datos)
        if (existente >= 0) lista[existente] = nuevo else lista.add(nuevo)
        if (escribirLista(contexto, lista)) idFinal else null
    }

    fun eliminar(contexto: Context, id: String) {
        synchronized(candado) {
            val lista = leerLista(contexto)
            if (lista.removeAll { it.id == id }) escribirLista(contexto, lista)
        }
    }

    /**
     * Borra TODOS los borradores de la usuaria actual (la lista, la marca de «interrumpido» y el borrador único
     * de versiones anteriores, cifrado o sin cifrar). Lo usa «Borrar mi historial». La salida de emergencia y
     * cerrar sesión borran todo el almacenamiento cifrado con [BorradoLocal.todo].
     */
    fun borrarTodos(contexto: Context) {
        synchronized(candado) {
            AlmacenamientoCifrado.borrar(contexto, LISTA)
            AlmacenamientoCifrado.borrar(contexto, INTERRUMPIDO)
            AlmacenamientoCifrado.borrar(contexto, UNICO_ANTIGUO)
            contexto.applicationContext.getSharedPreferences("crisalida_borradores", Context.MODE_PRIVATE)
                .edit().remove("publicacion").commit()
            porAbrir = null
        }
    }

    /** Marca (cifrado) cuál borrador se guardó porque la sesión se cerró por inactividad. */
    fun marcarInterrumpido(contexto: Context, id: String) {
        AlmacenamientoCifrado.guardar(contexto, INTERRUMPIDO, id)
    }

    /** El borrador guardado por inactividad, si todavía existe; null si no hay ninguno que ofrecer. */
    fun interrumpido(contexto: Context): Borrador? {
        synchronized(candado) {
            val id = AlmacenamientoCifrado.leer(contexto, INTERRUMPIDO) ?: return null
            return leerLista(contexto).firstOrNull { it.id == id }
        }
    }

    fun olvidarInterrumpido(contexto: Context) {
        AlmacenamientoCifrado.borrar(contexto, INTERRUMPIDO)
    }

    /**
     * Pasa a la lista el borrador único de versiones anteriores (sin cifrar o cifrado de una sola ranura).
     * No pierde nada: el viejo se borra solo DESPUÉS de que la lista nueva se guardó bien,
     * y si algo falla se reintenta la próxima vez. Se puede llamar muchas veces.
     */
    fun migrar(contexto: Context) {
        synchronized(candado) {
            AlmacenamientoCifrado.migrarTextoPlano(contexto, "crisalida_borradores", "publicacion", UNICO_ANTIGUO)
            val viejo = AlmacenamientoCifrado.leer(contexto, UNICO_ANTIGUO) ?: return
            val valido = try { JSONObject(viejo); true } catch (e: Exception) { false }
            if (!valido) { AlmacenamientoCifrado.borrar(contexto, UNICO_ANTIGUO); return }
            val lista = leerLista(contexto)
            lista.add(Borrador(UUID.randomUUID().toString(), System.currentTimeMillis(), viejo))
            if (escribirLista(contexto, lista)) AlmacenamientoCifrado.borrar(contexto, UNICO_ANTIGUO)
        }
    }
}

/**
 * Formularios con algo escrito que se guardan solos antes de que la sesión se cierre por inactividad.
 * Cada pantalla se registra mientras está abierta y se quita al salir.
 */
object FormulariosAbiertos {
    private val guardadores = mutableListOf<() -> Unit>()

    fun registrar(guardar: () -> Unit) { guardadores.add(guardar) }
    fun quitar(guardar: () -> Unit) { guardadores.remove(guardar) }

    /** Se llama justo ANTES de SesionActual.cerrar(): después ya no se podría descifrar ni cifrar nada. */
    fun guardarTodo() {
        guardadores.toList().forEach { try { it() } catch (e: Exception) { /* nunca debe impedir el cierre */ } }
    }
}