package com.example.crisalida.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.text.Normalizer
import java.util.Locale

/*
 * Control por voz (issue 10.4): moverse por la app diciendo una palabra clave.
 * La interpretación es una función local (sin IA ni servicios externos) y sin nada de Android, para probarla.
 */

/** Órdenes que entiende la app. Ninguna borra, publica, valida ni cambia datos. */
enum class OrdenVoz { RETOS, MARKETPLACE, INICIO, BIENESTAR, MASCOTA, PERFIL, CONFIGURACION, VOLVER, LEER_PANTALLA, OMITIR, SIGUIENTE, ATRAS, RESPIRAR, BORRADORES, PUBLICAR_PRODUCTO }

const val CONTROL_VOZ_NO_ENTENDI = "No te entendí. Prueba con: retos, inicio o volver."

// Si la frase trae alguna de estas palabras no se hace NADA, aunque también diga una orden válida
private val PALABRAS_BLOQUEADAS = setOf(
    "borrar", "borra", "eliminar", "elimina", "emergencia", "salida", "salir",
    "validar", "valida", "pin", "cuenta", "cerrar", "sesion"
)
// «publicar» tampoco hace nada, salvo en «publicar (o subir) un producto», que solo ABRE el formulario
private val PALABRAS_PUBLICAR = setOf("publicar", "publica", "subir", "sube")
private val PALABRAS_RESPIRAR = setOf("respirar", "respira", "respiracion", "respirando")

private val PALABRAS_CLAVE: List<Pair<Set<String>, OrdenVoz>> = listOf(
    setOf("retos", "reto") to OrdenVoz.RETOS,
    setOf("marketplace", "market", "mercado") to OrdenVoz.MARKETPLACE,
    setOf("inicio", "home", "principal") to OrdenVoz.INICIO,
    setOf("bienestar") to OrdenVoz.BIENESTAR,
    setOf("mascota", "lilu") to OrdenVoz.MASCOTA,
    setOf("perfil") to OrdenVoz.PERFIL,
    setOf("configuracion", "ajustes") to OrdenVoz.CONFIGURACION,
    setOf("volver", "vuelve", "regresar", "regresa") to OrdenVoz.VOLVER,
    setOf("leer", "lee", "lectura") to OrdenVoz.LEER_PANTALLA,
    setOf("omitir", "omite", "saltar", "salta") to OrdenVoz.OMITIR,
    setOf("siguiente", "sigue", "adelante") to OrdenVoz.SIGUIENTE,
    setOf("atras") to OrdenVoz.ATRAS,
    setOf("borradores", "borrador") to OrdenVoz.BORRADORES
)

/** Minúsculas y sin acentos: «Configuración» → «configuracion». */
fun normalizarFrase(texto: String): String =
    Normalizer.normalize(texto.lowercase(Locale.ROOT), Normalizer.Form.NFD).replace(Regex("\\p{Mn}+"), "")

/**
 * Busca la primera palabra clave de la frase. Devuelve null si no hay ninguna o si la frase pide algo destructivo
 * (borrar, salida de emergencia, publicar, validar, PIN...).
 */
fun interpretarOrden(frase: String): OrdenVoz? {
    val palabras = normalizarFrase(frase).split(Regex("[^a-z0-9]+")).filter { it.isNotEmpty() }
    if (palabras.any { it in PALABRAS_BLOQUEADAS }) return null
    if ("producto" in palabras && palabras.any { it in PALABRAS_PUBLICAR }) return OrdenVoz.PUBLICAR_PRODUCTO
    if (palabras.any { it == "publicar" || it == "publica" }) return null   // publicar cualquier otra cosa: nada
    // «respirar con Lilu» gana sobre «Lilu» (que sola va a la mascota)
    if (palabras.any { it in PALABRAS_RESPIRAR }) return OrdenVoz.RESPIRAR
    for (palabra in palabras) {
        PALABRAS_CLAVE.firstOrNull { palabra in it.first }?.let { return it.second }
    }
    return null
}

/** Ajuste «Control por voz» de Configuración. Empieza apagado y se guarda en el teléfono. */
object ControlPorVoz {
    private const val ARCHIVO = "crisalida_ajustes"
    private const val CLAVE = "control_voz"
    private var cargado = false

    var activo by mutableStateOf(false)
        private set

    fun cargar(context: Context) {
        if (cargado) return
        cargado = true
        activo = context.applicationContext.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE).getBoolean(CLAVE, false)
    }

    fun cambiar(context: Context, activado: Boolean) {
        cargado = true
        activo = activado
        context.applicationContext.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE).edit().putBoolean(CLAVE, activado).apply()
    }

    /** ¿Se puede usar ahora? Solo con el ajuste activado, con sesión iniciada y fuera de camuflaje. */
    val disponible: Boolean get() = activo && SesionActual.token != null && !SesionActual.esCamuflaje
}

/** Lo que dice «leer pantalla»: las pantallas con botón «Escuchar» dejan aquí su texto mientras están abiertas. */
object LecturaPantalla {
    var lineas: List<String> = emptyList()
}
