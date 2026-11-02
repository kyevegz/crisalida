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
    val radar: RadarFinalRequest   // definido en ApiRadar.kt
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
    val token: String
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
    suspend fun publicar(@Body datos: PublicacionRequest): Response<PublicacionCreadaResponse>

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
    suspend fun entregarReto(@Path("id") aceptacionId: String): Response<EstadoResponse>

    @POST("retos/aceptaciones/{id}/abandonar")
    suspend fun abandonarReto(@Path("id") aceptacionId: String): Response<EstadoResponse>

    // ----- Bienestar
    @GET("bienestar/hoy")
    suspend fun bienestarHoy(): Response<BienestarHoyResponse>

    @POST("bienestar/emocion")
    suspend fun guardarEmocion(@Body datos: EmocionRequest): Response<FloresResponse>

    @POST("bienestar/monitoreo")
    suspend fun guardarMonitoreo(@Body datos: MonitoreoRequest): Response<FloresResponse>

    @POST("bienestar/reflexion")
    suspend fun guardarReflexion(@Body datos: ReflexionRequest): Response<FloresResponse>

    @POST("bienestar/bitacora")
    suspend fun guardarBitacora(@Body datos: BitacoraRequest): Response<FloresResponse>

    @GET("bienestar/entradas")
    suspend fun entradasBienestar(@Query("subtipo") subtipo: String): Response<EntradasBienestarResponse>

    @DELETE("bienestar/entradas/{id}")
    suspend fun borrarEntradaBienestar(@Path("id") id: String): Response<BorradoResponse>
}

// ---------- 4. Quién soy ahora mismo (se borra al cerrar la app o con la salida de emergencia)

object SesionActual {
    var token: String? = null
    var tokenRegistro: String? = null   // pase temporal: solo existe entre "Siguiente" y el final del radar
    var usuariaId: String = ""
    var seudonimo: String = ""
    var avatar: String = "🐱"
    var modo: String = "normal"

    val esCamuflaje: Boolean get() = modo == "camuflaje"

    fun iniciar(s: SesionResponse) {
        token = s.token
        usuariaId = s.usuariaId
        seudonimo = s.seudonimo
        avatar = s.avatar
        modo = s.modo
    }

    fun cerrar() {
        token = null
        tokenRegistro = null
        AuthRepositorio.cancelarRegistro()   // borra de la memoria el seudónimo y PIN pendientes
        usuariaId = ""
        seudonimo = ""
        avatar = "🐱"
        modo = "normal"
        ProgresoRepositorio.avisarCambio()   // que ninguna pantalla conserve datos de la sesión anterior
    }
}

// ---------- 5. La conexión: UNA sola vez para toda la app

object RetrofitCliente {
    // ⚠️ CAMBIA ESTO:
    //  - Celular real en tu mismo Wi-Fi: la IP de tu compu (en la consola: ipconfig → "Dirección IPv4")
    //  - Emulador de Android Studio:      http://10.0.2.2:8000/
    //private const val BASE_URL = "http://192.168.1.68:8000/"
    const val BASE_URL = "http://localhost:8000/"   // público: FotoRemota lo usa para armar la dirección de las fotos
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
    data class Error(val mensaje: String) : ResultadoApi<Nothing>()
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
        ResultadoApi.Error("No hay conexión con el servidor. Revisa tu internet.")
    } catch (e: Exception) {
        ResultadoApi.Error("Ocurrió un error inesperado. Intenta de nuevo.")
    }
}

/** El servidor manda {"detail": "texto"} o, en validaciones, {"detail": [{"msg": "..."}]} */
private fun extraerMensaje(cuerpo: String?): String {
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

    /** Paso 1 ("Siguiente"): el servidor valida y la app recibe un pase para usar el radar. NO crea la cuenta. */
    suspend fun validarRegistro(seudonimo: String, pin: String, pinCamuflaje: String): ResultadoApi<TokenRegistroResponse> {
        val datos = RegistroRequest(seudonimo.trim(), pin, pinCamuflaje)
        return llamarApi { RetrofitCliente.api.validarRegistro(datos) }
            .also {
                if (it is ResultadoApi.Exito) {
                    SesionActual.tokenRegistro = it.datos.tokenRegistro
                    pendiente = datos
                }
            }
    }

    /** Paso final (segundo "Terminar"): crea la cuenta con el radar completo e inicia la sesión. */
    suspend fun completarRegistro(
        actividades: List<String>,
        tiempo: String,
        respuestas: List<RespuestaDinamicaDto>,
        perfil: PerfilResponse
    ): ResultadoApi<SesionResponse> {
        val credenciales = pendiente
            ?: return ResultadoApi.Error("Tu registro expiró. Regresa y vuelve a empezar.")
        val peticion = RegistroCompletoRequest(
            seudonimo = credenciales.seudonimo,
            pin = credenciales.pin,
            pinCamuflaje = credenciales.pinCamuflaje,
            radar = RadarFinalRequest(actividades, tiempo, respuestas, perfil)
        )
        return llamarApi { RetrofitCliente.api.registroCompleto(peticion) }
            .also {
                if (it is ResultadoApi.Exito) {
                    SesionActual.iniciar(it.datos)
                    SesionActual.tokenRegistro = null
                    pendiente = null
                }
            }
    }

    /** Descarta el registro a medias (salida de emergencia o si la usuaria regresa al inicio). */
    fun cancelarRegistro() {
        pendiente = null
        SesionActual.tokenRegistro = null
    }

    suspend fun login(seudonimo: String, pin: String): ResultadoApi<SesionResponse> =
        llamarApi { RetrofitCliente.api.login(LoginRequest(seudonimo.trim(), pin)) }
            .also { if (it is ResultadoApi.Exito) SesionActual.iniciar(it.datos) }

    suspend fun seudonimoDisponible(seudonimo: String): ResultadoApi<DisponibleResponse> =
        llamarApi { RetrofitCliente.api.seudonimoDisponible(seudonimo.trim()) }
}