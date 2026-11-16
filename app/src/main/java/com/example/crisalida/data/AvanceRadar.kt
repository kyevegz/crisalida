package com.example.crisalida.data

import android.content.Context
import com.google.gson.Gson

/**
 * Avance LOCAL del radar de talentos (issue 8.5). Mientras la usuaria contesta, lo que lleva se guarda en el teléfono
 * (cifrado con [AlmacenamientoCifrado], AES-256-GCM, issue 5.3) y NUNCA en la base de datos: si cierra la app a medias,
 * puede continuar donde se quedó. Al terminar (aceptar su resultado) o al abandonar el radar, se borra.
 *
 * - Durante el registro todavía no hay sesión: el avance queda ligado al seudónimo que se está registrando
 *   (nadie más que esa persona lo ve; el PIN y el token no se guardan nunca).
 * - Con sesión iniciada (una compradora que vuelve a vender) queda ligado a su usuaria.
 * - Cerrar sesión y la salida de emergencia lo borran junto con todo lo local ([BorradoLocal.todo]).
 * - Si pasan más de 7 días, se descarta solo.
 */
object AvanceRadar {
    private const val NOMBRE = "radar_avance"
    private const val VERSION = 1
    private const val VIGENCIA_MS = 7L * 24 * 60 * 60 * 1000
    private val gson = Gson()

    data class Avance(
        val version: Int = VERSION,
        val paso: Int = 0,
        val respuestas: Map<String, List<String>>? = null,
        val textos: Map<String, String>? = null,
        val analisis: Bloque2Response? = null,
        val huellaAnalizada: String? = null,
        val perfil: PerfilResponse? = null,
        val huellaPerfil: String? = null,
        val habilidades: List<HabilidadDto>? = null,
        val guardadoEn: Long = 0L
    )

    /** De quién es el avance: la usuaria con sesión o, si se está registrando, su seudónimo. null: no hay a quién ligarlo. */
    fun dueno(): String? = when {
        SesionActual.usuariaId.isNotBlank() -> SesionActual.usuariaId
        else -> AuthRepositorio.seudonimoPendiente()?.lowercase()?.let { "registro:$it" }
    }

    fun guardar(contexto: Context, dueno: String?, avance: Avance): Boolean {
        if (dueno == null) return false
        return AlmacenamientoCifrado.guardar(
            contexto, NOMBRE, gson.toJson(avance.copy(version = VERSION, guardadoEn = System.currentTimeMillis())), dueno
        )
    }

    /** El avance guardado, o null si no hay, está dañado, es de otra versión o ya caducó (en esos casos se borra). */
    fun leer(contexto: Context, dueno: String?): Avance? {
        if (dueno == null) return null
        val texto = AlmacenamientoCifrado.leer(contexto, NOMBRE, dueno) ?: return null
        val avance = try { gson.fromJson(texto, Avance::class.java) } catch (e: Exception) { null }
        val vigente = avance != null && avance.version == VERSION &&
                System.currentTimeMillis() - avance.guardadoEn <= VIGENCIA_MS
        if (!vigente) {
            AlmacenamientoCifrado.borrar(contexto, NOMBRE, dueno)
            return null
        }
        return avance
    }

    fun borrar(contexto: Context, dueno: String?) {
        if (dueno == null) return
        AlmacenamientoCifrado.borrar(contexto, NOMBRE, dueno)
    }
}
