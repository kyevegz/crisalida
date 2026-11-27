package com.example.crisalida

import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.crisalida.data.AuthRepositorio
import com.example.crisalida.data.CacheLocal
import com.example.crisalida.data.ColaEnvios
import com.example.crisalida.data.EstadoConexion
import com.example.crisalida.data.ROL_COMPRADORA
import com.example.crisalida.data.ROL_VENDEDORA
import com.example.crisalida.data.ResultadoApi
import com.example.crisalida.data.SesionActual
import com.example.crisalida.ui.theme.screens.AvisoPendienteScreen
import com.example.crisalida.ui.theme.screens.AvisoSinConexion
import com.example.crisalida.ui.theme.screens.VigilanteCola
import com.example.crisalida.ui.theme.screens.CodigoRecuperacionScreen
import com.example.crisalida.ui.theme.screens.CompletarDatosScreen
import com.example.crisalida.ui.theme.screens.ElegirRolScreen
import com.example.crisalida.ui.theme.screens.ModoDatos
import com.example.crisalida.ui.theme.screens.PuertaCorreo
import com.example.crisalida.ui.theme.screens.LoginScreen
import com.example.crisalida.ui.theme.screens.RecuperarPinCodigoScreen
import com.example.crisalida.ui.theme.screens.RecuperarPinCorreoScreen
import com.example.crisalida.ui.theme.screens.RecuperarPinMetodoScreen
import com.example.crisalida.ui.theme.screens.RadarScreen
import com.example.crisalida.ui.theme.screens.VigilanteInactividad
import com.example.crisalida.ui.theme.AjustesTexto
import com.example.crisalida.ui.theme.CrisalidaTemaApp
import com.example.crisalida.ui.theme.CrisalidaTemaClaroFijo
import com.example.crisalida.ui.theme.Paleta
import com.example.crisalida.ui.theme.ConEscalaDeTexto
import com.example.crisalida.ui.theme.screens.RegisterScreen
import com.example.crisalida.ui.theme.screens.GaleriaMariposasScreen
import com.example.crisalida.ui.theme.screens.pantallasPrincipales
import kotlinx.coroutines.launch
import com.example.crisalida.data.Haptica
import com.example.crisalida.data.Sonidos
import com.example.crisalida.data.InactividadSesion
import com.example.crisalida.data.ProteccionPantalla
import com.example.crisalida.ui.theme.screens.VigilantePantalla

class MainActivity : ComponentActivity() {

    // cada toque en cualquier parte de la pantalla reinicia el temporizador de inactividad
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        InactividadSesion.registrarActividad()
        return super.dispatchTouchEvent(ev)
    }

    // al salir de la app (otra app, pantalla bloqueada, vista de recientes) se marca la ventana como segura
    // para que la miniatura de «apps recientes» no muestre el contenido
    override fun onPause() {
        ProteccionPantalla.alPasarASegundoPlano()
        ProteccionPantalla.aplicar(this)
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        ProteccionPantalla.alVolverAPrimerPlano()
        ProteccionPantalla.aplicar(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AjustesTexto.cargar(applicationContext)   // tamaño de letra elegido, guardado en el teléfono
        Paleta.cargar(applicationContext)         // modo claro u oscuro elegido, guardado en el teléfono
        InactividadSesion.cargar(applicationContext)   // minutos de inactividad elegidos, guardados en el teléfono
        Haptica.cargar(applicationContext)              // si la vibración está activada, guardado en el teléfono
        Sonidos.cargar(applicationContext)              // si los sonidos están activados, guardado en el teléfono
        ProteccionPantalla.cargar(applicationContext)   // si se bloquean las capturas, guardado en el teléfono
        CacheLocal.iniciar(applicationContext)          // última versión de mascota, portafolio y bitácora (para verlos sin conexión)
        ColaEnvios.iniciar(applicationContext)          // cola de envíos pendientes (se envían al volver el internet)
        EstadoConexion.iniciar(applicationContext)      // si hay internet (también al abrir la app ya sin conexión)
        setContent {
            CrisalidaTemaApp {
                val navController = rememberNavController()
                val scope = rememberCoroutineScope()   // para llamar al servidor sin trabar la pantalla

                VigilanteInactividad(navController)   // cierra la sesión si pasa el tiempo elegido sin tocar la pantalla
                VigilanteCola()                       // envía lo pendiente en cuanto vuelve el internet
                VigilantePantalla(navController)      // oculta la app en recientes y bloquea capturas en pantallas sensibles

                Box(Modifier.fillMaxSize()) {
                    NavHost(navController = navController, startDestination = "login") {

                        // login
                        composable("login") {
                            var cargando by remember { mutableStateOf(false) }
                            var error by remember { mutableStateOf<String?>(null) }

                            ConEscalaDeTexto(tope = 2f) {
                                LoginScreen(
                                    cargando = cargando,
                                    error = error,
                                    onLoginClick = { seudonimo, pin ->
                                        scope.launch {
                                            cargando = true
                                            error = null
                                            when (val resultado = AuthRepositorio.login(seudonimo, pin)) {
                                                is ResultadoApi.Exito -> {
                                                    // pin de camuflaje abre la galería; si falta aceptar el aviso se pide primero;
                                                    // si no, pin normal abre la app
                                                    val destino = when {
                                                        resultado.datos.modo == "camuflaje" -> "camuflaje"
                                                        resultado.datos.avisoPendiente -> "aviso"
                                                        else -> "home"
                                                    }
                                                    navController.navigate(destino) {
                                                        popUpTo("login") { inclusive = true }
                                                    }
                                                }
                                                is ResultadoApi.Error -> {
                                                    error = resultado.mensaje
                                                    // 429 = cuenta bloqueada 15 min (aviso largo); otro fallo de credenciales = golpe seco
                                                    if (resultado.codigoHttp == 429) Haptica.aviso() else if (!resultado.sinConexion) Haptica.error()
                                                }
                                            }
                                            cargando = false
                                        }
                                    },
                                    onNavigateToRegister = { navController.navigate("elegir_rol") },
                                    onOlvidePin = { navController.navigate("recuperar_pin") }
                                )
                            }
                        }

                        // olvidé mi PIN: elegir método → código de recuperación o correo (todo antes de iniciar sesión)
                        composable("recuperar_pin") {
                            ConEscalaDeTexto(tope = 2f) {
                                RecuperarPinMetodoScreen(
                                    onCodigo = { navController.navigate("recuperar_pin_codigo") },
                                    onCorreo = { navController.navigate("recuperar_pin_correo") },
                                    onVolver = { navController.popBackStack() }
                                )
                            }
                        }
                        composable("recuperar_pin_codigo") {
                            ConEscalaDeTexto(tope = 2f) {
                                RecuperarPinCodigoScreen(
                                    onVolver = { navController.popBackStack() },
                                    onListo = { navController.popBackStack("login", false) }
                                )
                            }
                        }
                        composable("recuperar_pin_correo") {
                            ConEscalaDeTexto(tope = 2f) {
                                RecuperarPinCorreoScreen(
                                    onVolver = { navController.popBackStack() },
                                    onListo = { navController.popBackStack("login", false) }
                                )
                            }
                        }

                        // elección de rol: primer paso del registro
                        composable("elegir_rol") {
                            ConEscalaDeTexto(tope = 2f) {
                                ElegirRolScreen(
                                    onElegir = { rol -> navController.navigate("register/$rol") },
                                    onNavigateToLogin = { navController.popBackStack() }
                                )
                            }
                        }

                        // registro: la vendedora sigue al radar (ahí se crea su cuenta);
                        // la compradora crea su cuenta aquí mismo, sin radar (registro corto)
                        composable(
                            "register/{rol}",
                            arguments = listOf(navArgument("rol") { type = NavType.StringType })
                        ) { entrada ->
                            val rol = if (entrada.arguments?.getString("rol") == ROL_COMPRADORA) ROL_COMPRADORA else ROL_VENDEDORA
                            var cargando by remember { mutableStateOf(false) }
                            var error by remember { mutableStateOf<String?>(null) }

                            ConEscalaDeTexto(tope = 2f) {
                                RegisterScreen(
                                    cargando = cargando,
                                    error = error,
                                    esCompradora = rol == ROL_COMPRADORA,
                                    onAvisoCambio = { acepta, mayor -> AuthRepositorio.registrarAviso(acepta, mayor) },
                                    onNextClick = { seudonimo, pinPrincipal, pinCamuflaje ->
                                        scope.launch {
                                            cargando = true
                                            error = null
                                            when (val validacion = AuthRepositorio.validarRegistro(seudonimo, pinPrincipal, pinCamuflaje, rol)) {
                                                is ResultadoApi.Exito -> {
                                                    if (rol == ROL_COMPRADORA) {
                                                        when (val cuenta = AuthRepositorio.completarRegistroCompradora()) {
                                                            is ResultadoApi.Exito -> navController.navigate("codigo_recuperacion") {
                                                                popUpTo("login") { inclusive = true }   // ya no se regresa a login/registro
                                                            }
                                                            is ResultadoApi.Error -> error = cuenta.mensaje
                                                        }
                                                    } else {
                                                        navController.navigate("radar")   // todavía sin cuenta
                                                    }
                                                }
                                                is ResultadoApi.Error -> error = validacion.mensaje
                                            }
                                            cargando = false
                                        }
                                    },
                                    onNavigateToLogin = { navController.popBackStack("login", false) }
                                )
                            }
                        }

                        // código de recuperación: se muestra una sola vez; sin confirmar que se guardó no se avanza
                        composable("codigo_recuperacion") {
                            ConEscalaDeTexto(tope = 2f) {
                                CodigoRecuperacionScreen(
                                    codigo = AuthRepositorio.codigoRecuperacion,
                                    onFinalizar = {
                                        AuthRepositorio.limpiarCodigoRecuperacion()   // ya no queda en memoria
                                        navController.navigate("completar_datos") {
                                            popUpTo("codigo_recuperacion") { inclusive = true }
                                        }
                                    }
                                )
                            }
                        }

                        // datos opcionales tras el registro (correo, municipio y localidad); se pueden omitir
                        composable("completar_datos") {
                            val entrarALaApp = {
                                navController.navigate("home") { popUpTo("completar_datos") { inclusive = true } }
                            }
                            ConEscalaDeTexto(tope = 2f) {
                                CompletarDatosScreen(
                                    modo = ModoDatos.REGISTRO,
                                    onTerminar = { entrarALaApp() },
                                    onOmitir = { entrarALaApp() }
                                )
                            }
                        }

                        // puerta del correo: antes de publicar un reto se pide el correo si la usuaria no lo tiene.
                        // Uso: navController.navigate("requiere_correo/<ruta de publicar reto>")
                        composable(
                            "requiere_correo/{destino}",
                            arguments = listOf(navArgument("destino") { type = NavType.StringType })
                        ) { entrada ->
                            val destino = entrada.arguments?.getString("destino").orEmpty()
                            ConEscalaDeTexto(tope = 2f) {
                                PuertaCorreo(
                                    onListo = {
                                        navController.navigate(destino) {
                                            popUpTo("requiere_correo/{destino}") { inclusive = true }
                                        }
                                    },
                                    onCancelar = { navController.popBackStack() }
                                )
                            }
                        }

                        // aviso de privacidad y términos para cuentas que aún no lo han aceptado
                        composable("aviso") {
                            var cargando by remember { mutableStateOf(false) }
                            var error by remember { mutableStateOf<String?>(null) }

                            ConEscalaDeTexto(tope = 2f) {
                                AvisoPendienteScreen(
                                    cargando = cargando,
                                    error = error,
                                    onAceptar = {
                                        scope.launch {
                                            cargando = true
                                            error = null
                                            when (val r = AuthRepositorio.aceptarAviso()) {
                                                is ResultadoApi.Exito -> navController.navigate("home") {
                                                    popUpTo("login") { inclusive = true }
                                                }
                                                is ResultadoApi.Error -> error = r.mensaje
                                            }
                                            cargando = false
                                        }
                                    },
                                    onRechazar = {
                                        // sin aceptar no se puede usar la app: se cierra la sesión y vuelve al inicio
                                        SesionActual.cerrar()
                                        navController.navigate("login") { popUpTo(0) { inclusive = true } }
                                    }
                                )
                            }
                        }

                        // radar de talentos
                        composable("radar") {
                            ConEscalaDeTexto(tope = 2f) {
                                RadarScreen(
                                    onAbandonar = { navController.popBackStack() },   // salió del radar: ya se borró su avance local
                                    onTerminar = {
                                        // ya se creó la cuenta y la sesión: lo primero es mostrar el código de recuperación
                                        navController.navigate("codigo_recuperacion") {
                                            popUpTo("login") { inclusive = true }   //ya no se regresa a login/registro/radar
                                        }
                                    }
                                )
                            }
                        }

                        //galería de mariposas (app de camuflaje)
                        composable("camuflaje") {
                            CrisalidaTemaClaroFijo {
                                GaleriaMariposasScreen(
                                    onCerrar = {
                                        SesionActual.cerrar()
                                        navController.navigate("login") {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    }
                                )
                            }
                        }

                        //home, retos, marketplace, bienestar, mascota, perfil, etc.
                        pantallasPrincipales(navController)
                    }
                    // aviso discreto de «Sin conexión» encima de cualquier pantalla
                    AvisoSinConexion(navController)
                }
            }
        }
    }
}