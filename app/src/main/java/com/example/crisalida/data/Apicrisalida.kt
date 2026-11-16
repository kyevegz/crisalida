package com.example.crisalida.data

import com.google.gson.JsonParser
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.CancellationException
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import java.io.IOException
import java.util.concurrent.TimeUnit

/* ================================================================
 * TODO LO DE RED VIVE EN ESTE ARCHIVO (se escribe una sola vez).
 * Las pantallas nunca hablan con Retrofit: usan AuthRepositorio.
 * ================================================================ */

// ---------- 1. Datos que se ENVÍAN al servidor (los nombres deben coincidir con los de Python)

data class RegistroRequest(
    val seudonimo: String,
    val pin: String,
    @SerializedName("pin_camuflaje") val pinCamuflaje: String
)

// Todo lo que viaja al crear la cuenta (al terminar el radar): credenciales + radar completo
data class RegistroCompletoRequest(
    val seudonimo: String,
    val pin: String,
    @SerializedName("pin_camuflaje") val pinCamuflaje: String,
    val rol: String,                       // "vendedora" o "compradora"
    val radar: RadarFinalRequest?,         // solo la vendedora (definido en ApiRadar.kt); si es null no se envía
    @SerializedName("acepta_aviso") val aceptaAviso: Boolean,
    @SerializedName("es_mayor_de_edad") val esMayorDeEdad: Boolean,
    @SerializedName("version_aviso") val versionAviso: String?
)

// Aceptación del aviso de privacidad y términos (cuentas que aún no lo han aceptado)
data class AceptarAvisoRequest(
    val version: String,
    @SerializedName("es_mayor_de_edad") val esMayorDeEdad: Boolean
)

data class AvisoAceptadoResponse(
    @SerializedName("aviso_version") val avisoVersion: String,
    val aceptado: Boolean
)

data class LoginRequest(
    val seudonimo: String,
    val pin: String
)

// ---------- 2. Datos que RESPONDE el servidor

data class SesionResponse(
    @SerializedName("usuaria_id") val usuariaId: String,
    val seudonimo: String,
    val avatar: String,
    val modo: String,   // "normal" o "camuflaje"
    val token: String,
    val rol: String? = null,   // "vendedora" o "compradora"
    // true: la cuenta todavía no acepta la versión vigente del aviso y la app debe mostrarlo
    @SerializedName("aviso_pendiente") val avisoPendiente: Boolean = false,
    // solo viene al crear la cuenta; la pantalla del código (issue 4.5) lo mostrará
    @SerializedName("codigo_recuperacion") val codigoRecuperacion: String? = null
)

// Pase temporal que entrega el servidor al validar el paso 1 del registro (todavía no hay cuenta)
data class TokenRegistroResponse(
    @SerializedName("token_registro") val tokenRegistro: String
)

data class DisponibleResponse(
    val disponible: Boolean,
    val motivo: String?
)

// ---------- 3. Lista de endpoints. Para agregar uno nuevo, añade UNA línea aquí.

interface ApiService {
    @POST("auth/validar-registro")
    suspend fun validarRegistro(@Body datos: RegistroRequest): Response<TokenRegistroResponse>

    @POST("auth/registro-completo")
    suspend fun registroCompleto(@Body datos: RegistroCompletoRequest): Response<SesionResponse>

    @POST("auth/login")
    suspend fun login(@Body datos: LoginRequest): Response<SesionResponse>

    @GET("reportes/motivos")
    suspend fun motivosReporte(): Response<MotivosResponse>

    @POST("reportes")
    suspend fun reportar(@Body datos: ReporteRequest): Response<ReporteResponse>

    @GET("bloqueos")
    suspend fun misBloqueos(): Response<BloqueosResponse>

    @POST("bloqueos")
    suspend fun bloquear(@Body datos: BloqueoRequest): Response<BloqueoCreadoResponse>

    @DELETE("bloqueos/{id}")
    suspend fun desbloquear(@Path("id") id: Int): Response<DesbloqueoResponse>

    @GET("perfil/datos")
    suspend fun misDatos(): Response<DatosPersonalesResponse>

    @PUT("perfil/datos")
    suspend fun guardarUbicacion(@Body datos: UbicacionRequest): Response<DatosPersonalesResponse>

    @POST("perfil/correo")
    suspend fun guardarCorreo(@Body datos: CorreoRequest): Response<CorreoGuardadoResponse>

    @POST("perfil/correo/verificar")
    suspend fun verificarCorreo(@Body datos: CodigoCorreoRequest): Response<CorreoVerificadoResponse>

    @POST("auth/aceptar-aviso")
    suspend fun aceptarAviso(@Body datos: AceptarAvisoRequest): Response<AvisoAceptadoResponse>

    @GET("auth/seudonimo-disponible")
    suspend fun seudonimoDisponible(@Query("seudonimo") seudonimo: String): Response<DisponibleResponse>

    @GET("radar/bloque1")
    suspend fun radarBloque1(): Response<Bloque1Response>

    @POST("radar/analizar")
    suspend fun radarAnalizar(@Body datos: Bloque1Request): Response<Bloque2Response>

    @POST("radar/finalizar")
    suspend fun radarFinalizar(@Body datos: FinalizarRequest): Response<PerfilResponse>

    @GET("radar/resultado")
    suspend fun radarResultado(): Response<ResultadoRadarResponse>

    // ----- Progreso y perfil
    @GET("progreso/yo")
    suspend fun progresoYo(): Response<ProgresoResponse>

    @GET("progreso/niveles")
    suspend fun niveles(): Response<NivelesResponse>

    @GET("progreso/reglas")
    suspend fun reglasFlores(): Response<ReglasResponse>

    @GET("frases-apoyo")
    suspend fun frasesApoyo(): Response<FrasesApoyoResponse>

    @PUT("perfil/avatar")
    suspend fun cambiarAvatar(@Body datos: AvatarRequest): Response<AvatarResponse>

    // ----- Marketplace
    @GET("categorias")
    suspend fun categorias(@Query("ambito") ambito: String?): Response<CategoriasResponse>

    @GET("marketplace")
    suspend fun marketplace(
        @Query("tipo") tipo: String?,
        @Query("categoria_id") categoriaId: Int?,
        @Query("q") busqueda: String?
    ): Response<CatalogoResponse>

    @GET("marketplace/{id}")
    suspend fun marketplaceDetalle(@Path("id") id: String): Response<PublicacionDto>

    // ----- Publicaciones (portafolio)
    @POST("publicaciones")
    suspend fun publicar(@Body datos: PublicacionRequest, @Header("Idempotency-Key") clave: String?): Response<PublicacionCreadaResponse>

    @GET("publicaciones/mias")
    suspend fun misPublicaciones(): Response<CatalogoResponse>

    @PUT("publicaciones/{id}")
    suspend fun editarPublicacion(@Path("id") id: String, @Body datos: PublicacionRequest): Response<PublicacionEditadaResponse>

    @DELETE("publicaciones/{id}/imagenes/{imagenId}")
    suspend fun borrarImagen(@Path("id") id: String, @Path("imagenId") imagenId: String): Response<BorradoResponse>

    @PATCH("publicaciones/{id}/estado")
    suspend fun cambiarEstadoPublicacion(@Path("id") id: String, @Body datos: EstadoRequest): Response<EstadoResponse>

    @POST("publicaciones/{id}/imagenes")
    suspend fun subirImagen(@Path("id") id: String, @Body imagen: RequestBody): Response<ImagenSubidaResponse>

    // ----- Retos
    @GET("retos")
    suspend fun retos(
        @Query("categoria_id") categoriaId: Int?,
        @Query("dificultad") dificultad: String?,
        @Query("q") busqueda: String?
    ): Response<RetosResponse>

    @GET("retos/mis")
    suspend fun misRetos(): Response<RetosResponse>

    @GET("retos/{id}")
    suspend fun retoDetalle(@Path("id") retoId: String): Response<RetoDto>

    @POST("retos/{id}/aceptar")
    suspend fun aceptarReto(@Path("id") retoId: String): Response<AceptacionResponse>

    @POST("retos/aceptaciones/{id}/entregar")
    suspend fun entregarReto(@Path("id") aceptacionId: String, @Header("Idempotency-Key") clave: String?): Response<EstadoResponse>

    @POST("retos/aceptaciones/{id}/abandonar")
    suspend fun abandonarReto(@Path("id") aceptacionId: String): Response<EstadoResponse>

    // ----- Bienestar
    @GET("bienestar/hoy")
    suspend fun bienestarHoy(): Response<BienestarHoyResponse>

    @POST("bienestar/emocion")
    suspend fun guardarEmocion(@Body datos: EmocionRequest, @Header("Idempotency-Key") clave: String?): Response<FloresResponse>

    @POST("bienestar/monitoreo")
    suspend fun guardarMonitoreo(@Body datos: MonitoreoRequest, @Header("Idempotency-Key") clave: String?): Response<FloresResponse>

    @POST("bienestar/reflexion")
    suspend fun guardarReflexion(@Body datos: ReflexionRequest, @Header("Idempotency-Key") clave: String?): Response<FloresResponse>

    @POST("bienestar/bitacora")
    suspend fun guardarBitacora(@Body datos: BitacoraRequest, @Header("Idempotency-Key") clave: String?): Response<FloresResponse>

    @GET("bienestar/entradas")
    suspend fun entradasBienestar(@Query("subtipo") subtipo: String): Response<EntradasBienestarResponse>

    @DELETE("bienestar/entradas/{id}")
    suspend fun borrarEntradaBienestar(@Path("id") id: String): Response<BorradoResponse>

    @DELETE("bienestar/entradas")
    suspend fun borrarHistorialBienestar(): Response<HistorialBorradoResponse>
}

// ---------- 4. Quién soy ahora mismo (se borra al cerrar la app o con la salida de emergencia)

// Los dos roles de la app. El rol se elige al registrarse y viaja en la sesión.
// Versión del aviso de privacidad y términos que muestra la app. Debe ser igual a AVISO_VERSION del servidor
// (si el servidor la sube, el registro y el login piden aceptar otra vez; la app se actualiza con la nueva versión).
const val VERSION_AVISO = "1"

const val ROL_VENDEDORA = "vendedora"
const val ROL_COMPRADORA = "compradora"

object SesionActual {
    var token: String? = null
    var tokenRegistro: String? = null   // pase temporal: solo existe entre "Siguiente" y el final del radar
    var usuariaId: String = ""
    var seudonimo: String = ""
    var avatar: String = "🐱"
    var modo: String = "normal"
    var rol: String = ROL_VENDEDORA

    val esCamuflaje: Boolean get() = modo == "camuflaje"
    val esCompradora: Boolean get() = rol == ROL_COMPRADORA

    fun iniciar(s: SesionResponse) {
        token = s.token
        usuariaId = s.usuariaId
        seudonimo = s.seudonimo
        avatar = s.avatar
        modo = s.modo
        rol = s.rol ?: ROL_VENDEDORA
        ColaEnvios.alIniciarSesion()   // retoma los envíos que quedaron pendientes (cifrados) de esta usuaria
    }

    fun cerrar() {
        ColaEnvios.alCerrarSesion()
        token = null
        tokenRegistro = null
        AuthRepositorio.cancelarRegistro()   // borra de la memoria el seudónimo y PIN pendientes
        usuariaId = ""
        seudonimo = ""
        avatar = "🐱"
        modo = "normal"
        rol = ROL_VENDEDORA
        ProgresoRepositorio.avisarCambio()   // que ninguna pantalla conserve datos de la sesión anterior
    }
}

// ---------- 5. La conexión: UNA sola vez para toda la app

object RetrofitCliente {
    // ⚠️ CAMBIA ESTO:
    //  - Celular real en tu mismo Wi-Fi: la IP de tu compu (en la consola: ipconfig → "Dirección IPv4")
    //  - Emulador de Android Studio:      http://10.0.2.2:8000/
    //private const val BASE_URL = "http://192.168.1.68:8000/"
    const val BASE_URL = "https://crisalidabackend.onrender.com/"   // público: FotoRemota lo usa para armar la dirección de las fotos
    private val cliente = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)   // Neon puede tardar unos segundos si estaba "dormida"
        .readTimeout(45, TimeUnit.SECONDS)      // el servidor espera hasta 20 s a la IA del radar; este margen evita cortarlo antes
        // Este interceptor pega el token en CADA petición, así no hay que repetirlo en ningún lado
        .addInterceptor { cadena ->
            val peticion = cadena.request().newBuilder()
            // Sesión normal; y si todavía se está registrando, el pase temporal del radar
            (SesionActual.token ?: SesionActual.tokenRegistro)?.let { peticion.header("Authorization", "Bearer $it") }
            cadena.proceed(peticion.build())
        }
        .build()

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(cliente)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}

// ---------- 6. Manejo de errores: UNA sola vez para todas las llamadas

sealed class ResultadoApi<out T> {
    data class Exito<T>(val datos: T) : ResultadoApi<T>()
    data class Error(val mensaje: String, val sinConexion: Boolean = false) : ResultadoApi<Nothing>()
}

/** Envuelve cualquier llamada: devuelve Exito(datos) o Error("mensaje listo para mostrar"). */
suspend fun <T> llamarApi(llamada: suspend () -> Response<T>): ResultadoApi<T> {
    return try {
        val respuesta = llamada()
        val cuerpo = respuesta.body()
        if (respuesta.isSuccessful && cuerpo != null) {
            ResultadoApi.Exito(cuerpo)
        } else {
            ResultadoApi.Error(extraerMensaje(respuesta.errorBody()?.string()))
        }
    } catch (e: CancellationException) {
        throw e   // si Android cancela la tarea, no es un error que mostrar
    } catch (e: IOException) {
        ResultadoApi.Error("Sin conexión. Revisa tu internet e inténtalo de nuevo.", sinConexion = true)
    } catch (e: Exception) {
        ResultadoApi.Error("Ocurrió un error inesperado. Intenta de nuevo.")
    }
}

/** El servidor manda {"detail": "texto"} o, en validaciones, {"detail": [{"msg": "..."}]} */
internal fun extraerMensaje(cuerpo: String?): String {
    val generico = "Algo salió mal. Intenta de nuevo."
    if (cuerpo.isNullOrBlank()) return generico
    return try {
        val detalle = JsonParser.parseString(cuerpo).asJsonObject.get("detail") ?: return generico
        when {
            detalle.isJsonPrimitive -> detalle.asString
            detalle.isJsonArray -> detalle.asJsonArray.firstOrNull()
                ?.asJsonObject?.get("msg")?.asString
                ?.removePrefix("Value error, ")
                ?: generico
            else -> generico
        }
    } catch (e: Exception) {
        generico
    }
}

// ---------- 7. Lo que usan las pantallas

object AuthRepositorio {

    // Seudónimo y PIN del paso 1: viven SOLO en memoria hasta que se crea la cuenta (nunca en disco)
    private var pendiente: RegistroRequest? = null
    private var rolPendiente: String = ROL_VENDEDORA

    // Código de recuperación que entrega el servidor UNA sola vez, al crear la cuenta. Vive solo en memoria
    // (nunca en disco) y se borra al mostrarlo, al cerrar sesión o al cancelar el registro.
    var codigoRecuperacion: String? = null
        private set

    fun limpiarCodigoRecuperacion() { codigoRecuperacion = null }

    // Casillas del aviso de privacidad y de mayoría de edad. La pantalla del aviso (issue siguiente)
    // las llena antes de crear la cuenta; mientras no estén marcadas el servidor rechaza el registro.
    var aceptaAviso: Boolean = false
    var esMayorDeEdad: Boolean = false
    var versionAviso: String? = null

    /** Paso 1 ("Siguiente"): el servidor valida y la app recibe un pase. NO crea la cuenta. */
    suspend fun validarRegistro(
        seudonimo: String,
        pin: String,
        pinCamuflaje: String,
        rol: String = ROL_VENDEDORA
    ): ResultadoApi<TokenRegistroResponse> {
        val datos = RegistroRequest(seudonimo.trim(), pin, pinCamuflaje)
        return llamarApi { RetrofitCliente.api.validarRegistro(datos) }
            .also {
                if (it is ResultadoApi.Exito) {
                    SesionActual.tokenRegistro = it.datos.tokenRegistro
                    pendiente = datos
                    rolPendiente = rol
                }
            }
    }

    /** Vendedora, paso final (segundo "Terminar"): crea la cuenta con el radar completo e inicia la sesión. */
    suspend fun completarRegistro(
        actividades: List<String>,
        tiempo: String,
        respuestas: List<RespuestaDinamicaDto>,
        perfil: PerfilResponse
    ): ResultadoApi<SesionResponse> =
        enviarRegistro(ROL_VENDEDORA, RadarFinalRequest(actividades, tiempo, respuestas, perfil))

    /** Compradora: su registro es corto, sin radar. Crea la cuenta e inicia la sesión. */
    suspend fun completarRegistroCompradora(): ResultadoApi<SesionResponse> =
        enviarRegistro(ROL_COMPRADORA, null)

    private suspend fun enviarRegistro(rol: String, radar: RadarFinalRequest?): ResultadoApi<SesionResponse> {
        val credenciales = pendiente
            ?: return ResultadoApi.Error("Tu registro expiró. Regresa y vuelve a empezar.")
        val peticion = RegistroCompletoRequest(
            seudonimo = credenciales.seudonimo,
            pin = credenciales.pin,
            pinCamuflaje = credenciales.pinCamuflaje,
            rol = rol,
            radar = radar,
            aceptaAviso = aceptaAviso,
            esMayorDeEdad = esMayorDeEdad,
            versionAviso = versionAviso
        )
        return llamarApi { RetrofitCliente.api.registroCompleto(peticion) }
            .also {
                if (it is ResultadoApi.Exito) {
                    SesionActual.iniciar(it.datos)
                    SesionActual.tokenRegistro = null
                    cancelarRegistro()
                    codigoRecuperacion = it.datos.codigoRecuperacion   // después de cancelarRegistro, que lo borra
                }
            }
    }

    /** Lo llama la pantalla de registro cada vez que cambian las casillas de aviso y mayoría de edad. */
    fun registrarAviso(acepta: Boolean, mayorDeEdad: Boolean) {
        aceptaAviso = acepta
        esMayorDeEdad = mayorDeEdad
        versionAviso = if (acepta) VERSION_AVISO else null
    }

    /** Cuenta que ya existía (o aviso nuevo): acepta la versión vigente y confirma que es mayor de 18 años. */
    suspend fun aceptarAviso(): ResultadoApi<AvisoAceptadoResponse> =
        llamarApi { RetrofitCliente.api.aceptarAviso(AceptarAvisoRequest(VERSION_AVISO, true)) }

    /** Descarta el registro a medias (salida de emergencia o si la usuaria regresa al inicio). */
    fun cancelarRegistro() {
        pendiente = null
        rolPendiente = ROL_VENDEDORA
        aceptaAviso = false
        esMayorDeEdad = false
        versionAviso = null
        codigoRecuperacion = null
        SesionActual.tokenRegistro = null
    }

    suspend fun login(seudonimo: String, pin: String): ResultadoApi<SesionResponse> =
        llamarApi { RetrofitCliente.api.login(LoginRequest(seudonimo.trim(), pin)) }
            .also { if (it is ResultadoApi.Exito) SesionActual.iniciar(it.datos) }

    suspend fun seudonimoDisponible(seudonimo: String): ResultadoApi<DisponibleResponse> =
        llamarApi { RetrofitCliente.api.seudonimoDisponible(seudonimo.trim()) }
}