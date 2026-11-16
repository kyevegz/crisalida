package com.example.crisalida.data

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import java.io.File
import javax.crypto.spec.GCMParameterSpec

/**
 * Almacenamiento local CIFRADO (issue 5.3): borradores, cola de envíos pendientes y último contenido.
 *
 * - Todo se cifra con AES-256-GCM. La clave se crea dentro del Keystore de Android: la app nunca la ve
 *   ni sale del teléfono, así que copiar los archivos de la app no sirve para leer los datos.
 * - Cada dato queda ligado a su nombre y a la usuaria (datos asociados de GCM): no se puede cambiar un
 *   valor por otro, y otra usuaria que use este teléfono no ve lo de la anterior.
 * - [borrarTodo] lo elimina por completo (datos y clave). Se usa al cerrar sesión y en la salida de emergencia.
 * - El PIN y el token NUNCA se guardan en el teléfono: el token vive solo en memoria ([SesionActual]) y el
 *   PIN solo se envía al servidor al iniciar sesión.
 *
 * Es trabajo de disco y de Keystore: llámalo fuera del hilo principal cuando sean datos grandes
 * (los borradores de texto son pequeños y no hay problema).
 */
object AlmacenamientoCifrado {
    private const val ARCHIVO = "crisalida_local_cifrado"
    private const val PROVEEDOR = "AndroidKeyStore"
    private const val ALIAS = "crisalida_clave_local_v1"
    private const val TRANSFORMACION = "AES/GCM/NoPadding"
    private const val LARGO_IV = 12
    private const val BITS_ETIQUETA = 128

    // Cada dato se guarda como «<usuaria>:<nombre>»; sin sesión iniciada no se guarda ni se lee nada
    private fun nombreCompleto(nombre: String): String? =
        SesionActual.usuariaId.takeIf { it.isNotBlank() }?.let { "$it:$nombre" }

    @Synchronized
    private fun clave(): SecretKey {
        val almacen = KeyStore.getInstance(PROVEEDOR).apply { load(null) }
        (almacen.getKey(ALIAS, null) as? SecretKey)?.let { return it }
        val generador = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, PROVEEDOR)
        generador.init(
            KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return generador.generateKey()
    }

    private fun prefs(contexto: Context) =
        contexto.applicationContext.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE)

    /** Guarda [texto] cifrado con el nombre [nombre]. Devuelve false si no se pudo (sin sesión o falla del Keystore). */
    fun guardar(contexto: Context, nombre: String, texto: String): Boolean {
        val completo = nombreCompleto(nombre) ?: return false
        return try {
            val cifrador = Cipher.getInstance(TRANSFORMACION)
            cifrador.init(Cipher.ENCRYPT_MODE, clave())   // el Keystore genera un IV nuevo cada vez
            cifrador.updateAAD(completo.toByteArray(Charsets.UTF_8))
            val cifrado = cifrador.doFinal(texto.toByteArray(Charsets.UTF_8))
            val paquete = cifrador.iv + cifrado
            prefs(contexto).edit().putString(completo, Base64.encodeToString(paquete, Base64.NO_WRAP)).commit()
        } catch (e: Exception) {
            false
        }
    }

    /** Lee y descifra lo guardado con [nombre]. Devuelve null si no hay nada o si no se puede leer (en ese caso lo descarta). */
    fun leer(contexto: Context, nombre: String): String? {
        val completo = nombreCompleto(nombre) ?: return null
        val guardado = prefs(contexto).getString(completo, null) ?: return null
        return try {
            val paquete = Base64.decode(guardado, Base64.NO_WRAP)
            val cifrador = Cipher.getInstance(TRANSFORMACION)
            cifrador.init(Cipher.DECRYPT_MODE, clave(), GCMParameterSpec(BITS_ETIQUETA, paquete, 0, LARGO_IV))
            cifrador.updateAAD(completo.toByteArray(Charsets.UTF_8))
            String(cifrador.doFinal(paquete, LARGO_IV, paquete.size - LARGO_IV), Charsets.UTF_8)
        } catch (e: Exception) {
            // clave perdida (p. ej. copia de seguridad restaurada) o dato dañado: no se puede recuperar
            prefs(contexto).edit().remove(completo).apply()
            null
        }
    }

    // ---------- archivos cifrados (las fotos de los envíos pendientes). Mismo cifrado y misma clave; un archivo por foto.

    private fun carpetaArchivos(contexto: Context) = File(contexto.applicationContext.filesDir, "cola_envios")

    /** Guarda [bytes] cifrados en un archivo llamado [nombre] (sin diagonales). Devuelve false si no se pudo. */
    fun guardarArchivo(contexto: Context, nombre: String, bytes: ByteArray): Boolean {
        val completo = nombreCompleto(nombre) ?: return false
        return try {
            val cifrador = Cipher.getInstance(TRANSFORMACION)
            cifrador.init(Cipher.ENCRYPT_MODE, clave())
            cifrador.updateAAD(completo.toByteArray(Charsets.UTF_8))
            val paquete = cifrador.iv + cifrador.doFinal(bytes)
            val carpeta = carpetaArchivos(contexto).also { it.mkdirs() }
            File(carpeta, nombre).writeBytes(paquete)
            true
        } catch (e: Exception) {
            false
        }
    }

    /** Lee y descifra el archivo [nombre]. Devuelve null si no existe o no se puede leer. */
    fun leerArchivo(contexto: Context, nombre: String): ByteArray? {
        val completo = nombreCompleto(nombre) ?: return null
        val archivo = File(carpetaArchivos(contexto), nombre)
        if (!archivo.exists()) return null
        return try {
            val paquete = archivo.readBytes()
            val cifrador = Cipher.getInstance(TRANSFORMACION)
            cifrador.init(Cipher.DECRYPT_MODE, clave(), GCMParameterSpec(BITS_ETIQUETA, paquete, 0, LARGO_IV))
            cifrador.updateAAD(completo.toByteArray(Charsets.UTF_8))
            cifrador.doFinal(paquete, LARGO_IV, paquete.size - LARGO_IV)
        } catch (e: Exception) {
            null
        }
    }

    fun borrarArchivo(contexto: Context, nombre: String) {
        try { File(carpetaArchivos(contexto), nombre).delete() } catch (e: Exception) { }
    }

    /** Borra un dato de la usuaria actual. */
    fun borrar(contexto: Context, nombre: String) {
        val completo = nombreCompleto(nombre) ?: return
        prefs(contexto).edit().remove(completo).apply()
    }

    /**
     * Borra TODO lo local cifrado (de todas las usuarias de este teléfono) y también la clave del Keystore.
     * Se escribe de forma inmediata (commit): en la salida de emergencia la app se cierra enseguida.
     * También elimina el borrador antiguo que se guardaba sin cifrar.
     */
    fun borrarTodo(contexto: Context) {
        try {
            prefs(contexto).edit().clear().commit()
            carpetaArchivos(contexto).deleteRecursively()   // fotos de los envíos pendientes
            contexto.applicationContext.getSharedPreferences(ARCHIVO_ANTIGUO, Context.MODE_PRIVATE)
                .edit().clear().commit()
            KeyStore.getInstance(PROVEEDOR).apply { load(null) }.let {
                if (it.containsAlias(ALIAS)) it.deleteEntry(ALIAS)
            }
        } catch (e: Exception) {
            // si el Keystore falla, al menos los datos ya se borraron arriba (sin la clave no se pueden leer)
        }
    }

    private const val ARCHIVO_ANTIGUO = "crisalida_borradores"

    /**
     * Pasa un dato que se guardaba SIN cifrar (versión anterior de la app) al almacenamiento cifrado y
     * borra la copia en texto plano. Si ya no hay nada que pasar, no hace nada.
     */
    fun migrarTextoPlano(contexto: Context, archivoAntiguo: String, claveAntigua: String, nombreNuevo: String) {
        val viejo = contexto.applicationContext.getSharedPreferences(archivoAntiguo, Context.MODE_PRIVATE)
        val texto = viejo.getString(claveAntigua, null) ?: return
        // solo se borra el original si el nuevo quedó bien guardado
        if (guardar(contexto, nombreNuevo, texto)) viejo.edit().remove(claveAntigua).commit()
    }
}