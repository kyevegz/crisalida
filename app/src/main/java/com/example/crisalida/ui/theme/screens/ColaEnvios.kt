package com.example.crisalida.data

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.google.gson.Gson
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.IOException
import java.util.UUID
import java.util.concurrent.TimeUnit

/**
 * Cola de envíos pendientes (issue 5.4).
 *
 * Lo que la usuaria CREA estando sin conexión (registros de bienestar, la entrega de un reto, una publicación)
 * no se pierde: queda en esta cola, guardado CIFRADO en el teléfono (AlmacenamientoCifrado, issue 5.3), y se envía
 * solo cuando vuelve el internet.
 *
 * - Cada envío lleva su identificador único (UUID) como Idempotency-Key (issue 5.1). Así, si el servidor ya lo había
 *   recibido pero la respuesta se perdió, el reenvío devuelve lo mismo y NO se crea doble ni se dan flores dos veces.
 * - Las flores y el progreso solo se muestran como ganados cuando el servidor lo confirma: mientras el envío está en
 *   la cola, la pantalla dice «guardado, se enviará» y no suma nada. Al confirmarse se actualiza el progreso real.
 * - Se envía con WorkManager (cuando hay internet) y también al volver la conexión con la app abierta.
 * - El token NUNCA se guarda en el teléfono: si la app se cerró del todo, los envíos esperan (cifrados) y salen
 *   cuando la usuaria vuelve a iniciar sesión. La salida de emergencia y cerrar sesión desde Perfil los borran.
 * - Un envío que el servidor rechaza (p. ej. el reto ya no existe) no se reintenta: se queda a la vista con el motivo
 *   para que la usuaria lo cancele.
 */
enum class TipoEnvio(val etiqueta: String) {
    EMOCION("Registro de emoción"),
    MONITOREO("Monitoreo corporal y emocional"),
    REFLEXION("Preguntas de acompañamiento"),
    BITACORA("Bitácora"),
    ENTREGA_RETO("Entrega de reto"),
    PUBLICACION("Publicación")
}

/** Todo con valor por defecto: así Gson puede leerlo aunque se agreguen campos en versiones futuras. */
data class EnvioPendiente(
    val id: String = "",                 // = Idempotency-Key
    val tipo: String = "",               // nombre de un TipoEnvio
    val datos: String = "",              // cuerpo en JSON (o el id de la aceptación en las entregas)
    val creadoEn: Long = 0L,
    val fotos: List<String> = emptyList(),   // nombres de los archivos cifrados de las fotos (publicaciones)
    val publicacionId: String? = null,   // la publicación ya se creó en el servidor; faltan fotos
    val fotosSubidas: Int = 0,
    val error: String? = null            // el servidor lo rechazó: ya no se reintenta
)

private sealed interface Salida {
    object Hecho : Salida
    object Reintentar : Salida
    object Detener : Salida
    data class Rechazado(val mensaje: String) : Salida
}

private sealed interface Respuesta<out T> {
    data class Ok<T>(val datos: T) : Respuesta<T>
    data class Otra(val salida: Salida) : Respuesta<Nothing>
}

object ColaEnvios {
    /** Se pone como estado de la respuesta cuando el envío quedó en la cola en lugar de llegar al servidor. */
    const val ESTADO_EN_COLA = "en_cola"

    private const val NOMBRE_COLA = "cola_envios"
    private const val TRABAJO = "enviar_cola"
    private val gson = Gson()
    private val candado = Mutex()
    private var contexto: Context? = null

    /** Lo que hay pendiente ahora. Es estado de Compose: la tarjeta de Perfil se actualiza sola. */
    var pendientes by mutableStateOf<List<EnvioPendiente>>(emptyList())
        private set

    val cantidad: Int get() = pendientes.size

    fun aJson(objeto: Any): String = gson.toJson(objeto)

    fun iniciar(c: Context) {
        contexto = c.applicationContext
    }

    /** Al iniciar sesión: carga lo que había pendiente (cifrado, de esta usuaria) y lo manda a enviar. */
    fun alIniciarSesion() {
        val c = contexto ?: return
        if (SesionActual.esCamuflaje) return
        pendientes = leerDeDisco(c)
        if (pendientes.isNotEmpty()) programar()
    }

    /** Al cerrar la sesión se limpia la memoria; lo cifrado en disco queda ligado a la usuaria (o lo borra borrarTodo). */
    fun alCerrarSesion() {
        pendientes = emptyList()
    }

    /**
     * Intenta enviar de inmediato. Si no hay conexión, lo deja en la cola y devuelve Exito(alEncolar()) para que la
     * pantalla diga «guardado, se enviará». Cualquier otro error (el servidor lo rechazó) se devuelve tal cual.
     */
    suspend fun <T> enviar(
        tipo: TipoEnvio,
        datos: String,
        fotos: List<ByteArray> = emptyList(),
        alEncolar: () -> T,
        llamada: suspend (clave: String) -> Response<T>
    ): ResultadoApi<T> {
        val clave = UUID.randomUUID().toString()
        val resultado = llamarApi { llamada(clave) }
        if (resultado is ResultadoApi.Error && resultado.sinConexion) {
            return if (encolar(tipo, clave, datos, fotos)) ResultadoApi.Exito(alEncolar())
            else ResultadoApi.Error("Sin conexión, y no pudimos guardar tu envío en el teléfono. Inténtalo de nuevo cuando tengas internet.")
        }
        return resultado
    }

    private suspend fun encolar(tipo: TipoEnvio, clave: String, datos: String, fotos: List<ByteArray>): Boolean =
        withContext(Dispatchers.IO) {
            candado.withLock {
                val c = contexto ?: return@withLock false
                if (SesionActual.usuariaId.isBlank()) return@withLock false
                // Una entrega que ya está esperando no se vuelve a guardar (evita enviarla dos veces)
                if (tipo == TipoEnvio.ENTREGA_RETO && pendientes.any { it.tipo == tipo.name && it.datos == datos }) {
                    return@withLock true
                }
                val nombres = mutableListOf<String>()
                for ((i, bytes) in fotos.withIndex()) {
                    val nombre = "${clave}_$i.foto"
                    if (!AlmacenamientoCifrado.guardarArchivo(c, nombre, bytes)) {
                        nombres.forEach { AlmacenamientoCifrado.borrarArchivo(c, it) }
                        return@withLock false
                    }
                    nombres += nombre
                }
                val lista = pendientes + EnvioPendiente(clave, tipo.name, datos, System.currentTimeMillis(), nombres)
                if (!guardarEnDisco(c, lista)) {
                    nombres.forEach { AlmacenamientoCifrado.borrarArchivo(c, it) }
                    return@withLock false
                }
                pendientes = lista
                true
            }
        }.also { if (it) programar() }

    /** La usuaria cancela un envío: no se enviará y se borra del teléfono (incluidas sus fotos). */
    suspend fun cancelar(id: String) {
        withContext(Dispatchers.IO) {
            candado.withLock { quitar(id) }
        }
    }

    /** Pide a WorkManager que envíe lo pendiente en cuanto haya internet. */
    fun programar() {
        val c = contexto ?: return
        val pedido = OneTimeWorkRequestBuilder<EnviarColaWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(c).enqueueUniqueWork(TRABAJO, ExistingWorkPolicy.KEEP, pedido)
    }

    /**
     * Envía lo pendiente, en orden. Devuelve true si no queda nada por reintentar (todo enviado, o lo que queda lo
     * rechazó el servidor, o no hay sesión) y false si conviene reintentar más tarde (sin conexión, error del servidor).
     */
    suspend fun vaciar(): Boolean = withContext(Dispatchers.IO) {
        candado.withLock {
            val c = contexto ?: return@withLock true
            // Sin sesión en memoria (el token no se guarda en disco) no se puede enviar: espera al próximo inicio de sesión
            if (SesionActual.token == null || SesionActual.esCamuflaje) return@withLock true
            val vistos = mutableSetOf<String>()
            var huboAvance = false
            while (true) {
                val envio = pendientes.firstOrNull { it.error == null && it.id !in vistos } ?: break
                vistos += envio.id
                when (val salida = enviarUno(c, envio)) {
                    Salida.Hecho -> { quitar(envio.id); huboAvance = true }
                    is Salida.Rechazado -> {
                        val actual = pendientes.firstOrNull { it.id == envio.id } ?: envio
                        actualizar(c, actual.copy(error = salida.mensaje))
                    }
                    Salida.Reintentar -> { if (huboAvance) ProgresoRepositorio.avisarCambio(); return@withLock false }
                    Salida.Detener -> return@withLock true
                }
            }
            // Ya se confirmó en el servidor: ahora sí se refresca el progreso (flores, nivel) con los datos reales
            if (huboAvance) ProgresoRepositorio.avisarCambio()
            true
        }
    }

    // ---------- envío de cada tipo

    private suspend fun enviarUno(c: Context, e: EnvioPendiente): Salida {
        val api = RetrofitCliente.api
        return try {
            when (TipoEnvio.valueOf(e.tipo)) {
                TipoEnvio.EMOCION -> pedir { api.guardarEmocion(gson.fromJson(e.datos, EmocionRequest::class.java), e.id) }.aSalida()
                TipoEnvio.MONITOREO -> pedir { api.guardarMonitoreo(gson.fromJson(e.datos, MonitoreoRequest::class.java), e.id) }.aSalida()
                TipoEnvio.REFLEXION -> pedir { api.guardarReflexion(gson.fromJson(e.datos, ReflexionRequest::class.java), e.id) }.aSalida()
                TipoEnvio.BITACORA -> pedir { api.guardarBitacora(gson.fromJson(e.datos, BitacoraRequest::class.java), e.id) }.aSalida()
                TipoEnvio.ENTREGA_RETO -> pedir { api.entregarReto(e.datos, e.id) }.aSalida()
                TipoEnvio.PUBLICACION -> enviarPublicacion(c, e)
            }
        } catch (ex: IllegalArgumentException) {
            Salida.Rechazado("Este envío ya no es válido.")
        } catch (ex: com.google.gson.JsonParseException) {
            Salida.Rechazado("Este envío está dañado y no se puede enviar.")
        }
    }

    private suspend fun enviarPublicacion(c: Context, e0: EnvioPendiente): Salida {
        var e = e0
        if (e.publicacionId == null) {
            val peticion = gson.fromJson(e.datos, PublicacionRequest::class.java)
            when (val r = pedir { RetrofitCliente.api.publicar(peticion, e.id) }) {
                is Respuesta.Ok -> { e = e.copy(publicacionId = r.datos.id); actualizar(c, e) }
                is Respuesta.Otra -> return r.salida
            }
        }
        val publicacionId = e.publicacionId ?: return Salida.Rechazado("Este envío está dañado y no se puede enviar.")
        // Las fotos se suben de una en una; si se corta, se continúa por la que faltaba
        while (e.fotosSubidas < e.fotos.size) {
            val nombre = e.fotos[e.fotosSubidas]
            val bytes = AlmacenamientoCifrado.leerArchivo(c, nombre)
            if (bytes != null) {
                val r = pedir { RetrofitCliente.api.subirImagen(publicacionId, bytes.toRequestBody("image/jpeg".toMediaType())) }
                // Una foto que el servidor rechaza se omite (la publicación ya existe); lo demás se reintenta
                if (r is Respuesta.Otra && r.salida !is Salida.Rechazado) return r.salida
            }
            AlmacenamientoCifrado.borrarArchivo(c, nombre)
            e = e.copy(fotosSubidas = e.fotosSubidas + 1)
            actualizar(c, e)
        }
        return Salida.Hecho
    }

    private fun Respuesta<*>.aSalida(): Salida = when (this) {
        is Respuesta.Ok -> Salida.Hecho
        is Respuesta.Otra -> salida
    }

    /** Hace una petición y la clasifica: listo, reintentar más tarde, detener o rechazado por el servidor. */
    private suspend fun <T> pedir(llamada: suspend () -> Response<T>): Respuesta<T> = try {
        val r = llamada()
        val cuerpo = r.body()
        val codigo = r.code()
        when {
            r.isSuccessful && cuerpo != null -> Respuesta.Ok(cuerpo)
            codigo == 401 -> Respuesta.Otra(Salida.Detener)   // la sesión venció: se envía al volver a iniciar sesión
            codigo == 408 || codigo == 429 || codigo >= 500 -> Respuesta.Otra(Salida.Reintentar)
            r.isSuccessful -> Respuesta.Otra(Salida.Rechazado("El servidor respondió algo inesperado."))
            else -> Respuesta.Otra(Salida.Rechazado(extraerMensaje(r.errorBody()?.string())))
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: IOException) {
        Respuesta.Otra(Salida.Reintentar)
    } catch (e: Exception) {
        Respuesta.Otra(Salida.Rechazado("Ocurrió un error inesperado."))
    }

    // ---------- guardado en disco (cifrado)

    private fun leerDeDisco(c: Context): List<EnvioPendiente> =
        AlmacenamientoCifrado.leer(c, NOMBRE_COLA)
            ?.let { runCatching { gson.fromJson(it, Array<EnvioPendiente>::class.java)?.toList() }.getOrNull() }
            ?: emptyList()

    private fun guardarEnDisco(c: Context, lista: List<EnvioPendiente>): Boolean {
        if (lista.isEmpty()) { AlmacenamientoCifrado.borrar(c, NOMBRE_COLA); return true }
        return AlmacenamientoCifrado.guardar(c, NOMBRE_COLA, gson.toJson(lista))
    }

    private fun actualizar(c: Context, nuevo: EnvioPendiente) {
        val lista = pendientes.map { if (it.id == nuevo.id) nuevo else it }
        guardarEnDisco(c, lista)
        pendientes = lista
    }

    // se llama siempre con el candado tomado
    private fun quitar(id: String) {
        val c = contexto ?: return
        val envio = pendientes.firstOrNull { it.id == id } ?: return
        envio.fotos.drop(envio.fotosSubidas).forEach { AlmacenamientoCifrado.borrarArchivo(c, it) }
        val lista = pendientes.filter { it.id != id }
        guardarEnDisco(c, lista)
        pendientes = lista
    }

    /** Texto corto para mostrar en la tarjeta. No se muestra lo escrito en bienestar (es privado); sí el título de una publicación. */
    fun descripcion(e: EnvioPendiente): String {
        val tipo = runCatching { TipoEnvio.valueOf(e.tipo) }.getOrNull() ?: return "Envío pendiente"
        if (tipo == TipoEnvio.PUBLICACION) {
            val titulo = runCatching { gson.fromJson(e.datos, PublicacionRequest::class.java)?.titulo }.getOrNull()
            if (!titulo.isNullOrBlank()) return "Publicación: «$titulo»"
        }
        return tipo.etiqueta
    }
}

/** Lo ejecuta WorkManager cuando hay internet. */
class EnviarColaWorker(contexto: Context, parametros: WorkerParameters) : CoroutineWorker(contexto, parametros) {
    override suspend fun doWork(): Result {
        ColaEnvios.iniciar(applicationContext)
        return if (ColaEnvios.vaciar()) Result.success() else Result.retry()
    }
}