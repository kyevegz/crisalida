package com.example.crisalida.data

import com.google.gson.JsonParser
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.CancellationException
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
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

data class DisponibleResponse(
    val disponible: Boolean,
    val motivo: String?
)

// ---------- 3. Lista de endpoints. Para agregar uno nuevo, añade UNA línea aquí.

interface ApiService {
    @POST("auth/registro")
    suspend fun registro(@Body datos: RegistroRequest): Response<SesionResponse>

    @POST("auth/login")
    suspend fun login(@Body datos: LoginRequest): Response<SesionResponse>

    @GET("auth/seudonimo-disponible")
    suspend fun seudonimoDisponible(@Query("seudonimo") seudonimo: String): Response<DisponibleResponse>
}

// ---------- 4. Quién soy ahora mismo (se borra al cerrar la app o con la salida de emergencia)

object SesionActual {
    var token: String? = null
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
        usuariaId = ""
        seudonimo = ""
        avatar = "🐱"
        modo = "normal"
    }
}

// ---------- 5. La conexión: UNA sola vez para toda la app

object RetrofitCliente {
    // ⚠️ CAMBIA ESTO:
    //  - Celular real en tu mismo Wi-Fi: la IP de tu compu (en la consola: ipconfig → "Dirección IPv4")
    //  - Emulador de Android Studio:      http://10.0.2.2:8000/
    //private const val BASE_URL = "http://192.168.1.68:8000/"
    private const val BASE_URL = "http://localhost:8000/"
    private val cliente = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)   // Neon puede tardar unos segundos si estaba "dormida"
        .readTimeout(20, TimeUnit.SECONDS)
        // Este interceptor pega el token en CADA petición, así no hay que repetirlo en ningún lado
        .addInterceptor { cadena ->
            val peticion = cadena.request().newBuilder()
            SesionActual.token?.let { peticion.header("Authorization", "Bearer $it") }
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

    suspend fun registrar(seudonimo: String, pin: String, pinCamuflaje: String): ResultadoApi<SesionResponse> =
        llamarApi { RetrofitCliente.api.registro(RegistroRequest(seudonimo.trim(), pin, pinCamuflaje)) }
            .also { if (it is ResultadoApi.Exito) SesionActual.iniciar(it.datos) }

    suspend fun login(seudonimo: String, pin: String): ResultadoApi<SesionResponse> =
        llamarApi { RetrofitCliente.api.login(LoginRequest(seudonimo.trim(), pin)) }
            .also { if (it is ResultadoApi.Exito) SesionActual.iniciar(it.datos) }

    suspend fun seudonimoDisponible(seudonimo: String): ResultadoApi<DisponibleResponse> =
        llamarApi { RetrofitCliente.api.seudonimoDisponible(seudonimo.trim()) }
}