package com.example.crisalida

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.crisalida.data.AuthRepositorio
import com.example.crisalida.data.ResultadoApi
import com.example.crisalida.ui.screens.LoginScreen
import com.example.crisalida.ui.screens.RegisterScreen
import com.example.crisalida.ui.theme.ColorFondoApp
import com.example.crisalida.ui.theme.ColorTextoPrincipal
import com.example.crisalida.ui.theme.screens.pantallasPrincipales
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val navController = rememberNavController()
                val scope = rememberCoroutineScope()   // permite hacer llamadas al servidor sin trabar la pantalla

                NavHost(navController = navController, startDestination = "login") {

                    // ---------- LOGIN ----------
                    composable("login") {
                        var cargando by remember { mutableStateOf(false) }
                        var error by remember { mutableStateOf<String?>(null) }

                        LoginScreen(
                            cargando = cargando,
                            error = error,
                            onLoginClick = { seudonimo, pin ->
                                scope.launch {
                                    cargando = true
                                    error = null
                                    when (val resultado = AuthRepositorio.login(seudonimo, pin)) {
                                        is ResultadoApi.Exito -> {
                                            // PIN de camuflaje → galería falsa; PIN normal → la app real
                                            val destino = if (resultado.datos.modo == "camuflaje") "camuflaje" else "home"
                                            navController.navigate(destino) {
                                                popUpTo("login") { inclusive = true }   // "atrás" ya no regresa al login
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

                    // ---------- REGISTRO ----------
                    composable("register") {
                        var cargando by remember { mutableStateOf(false) }
                        var error by remember { mutableStateOf<String?>(null) }

                        RegisterScreen(
                            cargando = cargando,
                            error = error,
                            onNextClick = { seudonimo, pinPrincipal, pinCamuflaje ->
                                scope.launch {
                                    cargando = true
                                    error = null
                                    when (val resultado = AuthRepositorio.registrar(seudonimo, pinPrincipal, pinCamuflaje)) {
                                        is ResultadoApi.Exito -> {
                                            // Cuando exista el cuestionario, aquí irá navigate("cuestionario")
                                            navController.navigate("home") {
                                                popUpTo("login") { inclusive = true }
                                            }
                                        }
                                        is ResultadoApi.Error -> error = resultado.mensaje
                                    }
                                    cargando = false
                                }
                            },
                            onNavigateToLogin = { navController.popBackStack() }
                        )
                    }

                    // ---------- GALERÍA DE CAMUFLAJE (provisional) ----------
                    composable("camuflaje") {
                        Box(
                            modifier = Modifier.fillMaxSize().background(ColorFondoApp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🦋 Galería de mariposas (pendiente)", color = ColorTextoPrincipal)
                        }
                    }

                    // ---------- home, retos, marketplace, bienestar, mascota, perfil, etc. ----------
                    pantallasPrincipales(navController)
                }
            }
        }
    }
}