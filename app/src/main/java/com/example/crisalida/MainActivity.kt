package com.example.crisalida

import android.os.Bundle
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
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
import com.example.crisalida.data.ROL_COMPRADORA
import com.example.crisalida.data.ROL_VENDEDORA
import com.example.crisalida.data.ResultadoApi
import com.example.crisalida.data.SesionActual
import com.example.crisalida.ui.theme.screens.ElegirRolScreen
import com.example.crisalida.ui.theme.screens.LoginScreen
import com.example.crisalida.ui.theme.screens.RadarScreen
import com.example.crisalida.ui.theme.screens.VigilanteInactividad
import com.example.crisalida.ui.theme.AjustesTexto
import com.example.crisalida.ui.theme.ConEscalaDeTexto
import com.example.crisalida.ui.theme.screens.RegisterScreen
import com.example.crisalida.ui.theme.screens.GaleriaMariposasScreen
import com.example.crisalida.ui.theme.screens.pantallasPrincipales
import kotlinx.coroutines.launch
import com.example.crisalida.data.InactividadSesion
class MainActivity : ComponentActivity() {

    // cada toque en cualquier parte de la pantalla reinicia el temporizador de inactividad
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        `InactividadSesion`.registrarActividad()
        return super.dispatchTouchEvent(ev)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AjustesTexto.cargar(applicationContext)   // tamaño de letra elegido, guardado en el teléfono
        `InactividadSesion`.cargar(applicationContext)   // minutos de inactividad elegidos, guardados en el teléfono
        setContent {
            MaterialTheme {
                val navController = rememberNavController()
                val scope = rememberCoroutineScope()   // para llamar al servidor sin trabar la pantalla

                VigilanteInactividad(navController)   // cierra la sesión si pasa el tiempo elegido sin tocar la pantalla

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
                                                // pin de camuflaje abre la galería, pin normal abre la app
                                                val destino = if (resultado.datos.modo == "camuflaje") "camuflaje" else "home"
                                                navController.navigate(destino) {
                                                    popUpTo("login") { inclusive = true }
                                                }
                                            }
                                            is ResultadoApi.Error -> error = resultado.mensaje
                                        }
                                        cargando = false
                                    }
                                },
                                onNavigateToRegister = { navController.navigate("elegir_rol") }
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
                                onNextClick = { seudonimo, pinPrincipal, pinCamuflaje ->
                                    scope.launch {
                                        cargando = true
                                        error = null
                                        when (val validacion = AuthRepositorio.validarRegistro(seudonimo, pinPrincipal, pinCamuflaje, rol)) {
                                            is ResultadoApi.Exito -> {
                                                if (rol == ROL_COMPRADORA) {
                                                    when (val cuenta = AuthRepositorio.completarRegistroCompradora()) {
                                                        is ResultadoApi.Exito -> navController.navigate("home") {
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

                    // radar de talentos
                    composable("radar") {
                        ConEscalaDeTexto(tope = 2f) {
                            RadarScreen(
                                onTerminar = {
                                    // ya se creó la cuenta y la sesión, con ello entra a la app
                                    navController.navigate("home") {
                                        popUpTo("login") { inclusive = true }   //ya no se regresa a login/registro/radar
                                    }
                                }
                            )
                        }
                    }

                    //galería de mariposas (app de camuflaje)
                    composable("camuflaje") {
                        GaleriaMariposasScreen(
                            onCerrar = {
                                SesionActual.cerrar()
                                navController.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        )
                    }

                    //home, retos, marketplace, bienestar, mascota, perfil, etc.
                    pantallasPrincipales(navController)
                }
            }
        }
    }
}