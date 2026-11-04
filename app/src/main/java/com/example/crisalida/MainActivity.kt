package com.example.crisalida

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.crisalida.data.AuthRepositorio
import com.example.crisalida.data.ResultadoApi
import com.example.crisalida.data.SesionActual
import com.example.crisalida.ui.theme.screens.LoginScreen
import com.example.crisalida.ui.theme.screens.RadarScreen
import com.example.crisalida.ui.theme.ConEscalaDeTexto
import com.example.crisalida.ui.theme.screens.RegisterScreen
import com.example.crisalida.ui.theme.screens.GaleriaMariposasScreen
import com.example.crisalida.ui.theme.screens.pantallasPrincipales
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val navController = rememberNavController()
                val scope = rememberCoroutineScope()   // para llamar al servidor sin trabar la pantalla

                NavHost(navController = navController, startDestination = "login") {

                    // login
                    composable("login") {
                        var cargando by remember { mutableStateOf(false) }
                        var error by remember { mutableStateOf<String?>(null) }

                        ConEscalaDeTexto {
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
                                onNavigateToRegister = { navController.navigate("register") }
                            )
                        }
                    }

                    // registro (solo valida, la cuenta se crea al terminar el radar)
                    composable("register") {
                        var cargando by remember { mutableStateOf(false) }
                        var error by remember { mutableStateOf<String?>(null) }

                        ConEscalaDeTexto {
                            RegisterScreen(
                                cargando = cargando,
                                error = error,
                                onNextClick = { seudonimo, pinPrincipal, pinCamuflaje ->
                                    scope.launch {
                                        cargando = true
                                        error = null
                                        when (val resultado = AuthRepositorio.validarRegistro(seudonimo, pinPrincipal, pinCamuflaje)) {
                                            is ResultadoApi.Exito -> navController.navigate("radar")   // todavía sin cuenta
                                            is ResultadoApi.Error -> error = resultado.mensaje
                                        }
                                        cargando = false
                                    }
                                },
                                onNavigateToLogin = { navController.popBackStack() }
                            )
                        }
                    }

                    // radar de talentos
                    composable("radar") {
                        ConEscalaDeTexto {
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
