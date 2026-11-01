package com.example.crisalida.ui.theme.screens
import com.example.crisalida.ui.screens.HomeScreen
import com.example.crisalida.ui.screens.CrisalidaScaffold
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.crisalida.ui.theme.ColorFondoApp
import com.example.crisalida.ui.theme.ColorTextoPrincipal
import com.example.crisalida.ui.theme.OutfitFont

/**
 * Navegación entre pestañas de la barra inferior:
 * una sola copia de cada pantalla en la pila, sin acumular historial y restaurando estado.
 * Usa "home" como base de la pila (después del login debes llegar a "home").
 */
fun NavController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo("home") { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

/** Pantalla provisional para pestañas que aún no existen (evita el crash por ruta inexistente). */
@Composable
fun PantallaPendiente(titulo: String, selectedTab: Int, onNavigate: (String) -> Unit) {
    CrisalidaScaffold(currentTitle = titulo, selectedTab = selectedTab, onNavigate = onNavigate) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ColorFondoApp)
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$titulo — próximamente",
                fontFamily = OutfitFont,
                fontSize = 16.sp,
                color = ColorTextoPrincipal
            )
        }
    }
}

/**
 * Rutas de las 6 pestañas. Llámala dentro de tu NavHost:
 *
 *   NavHost(navController, startDestination = "login") {
 *       composable("login") { ... }       // lo que ya tengas
 *       composable("registro") { ... }
 *       pantallasPrincipales(navController)
 *   }
 *
 * Si ya tienes composable("bienestar") o composable("mascota"), borra esas líneas de aquí.
 */
fun NavGraphBuilder.pantallasPrincipales(navController: NavHostController) {
    val ir: (String) -> Unit = { navController.navigateToTab(it) }

    composable("retos") { RetosScreen(onNavigate = ir) }
    composable("marketplace") { PantallaPendiente("Marketplace", 1, ir) }
    composable("home") { HomeScreen(onNavigate = ir) }
    composable("bienestar") { PantallaPendiente("Bienestar diario", 3, ir) }
    composable("mascota") { PantallaPendiente("Mascota", 4, ir) }
    composable("perfil") { PantallaPendiente("Perfil", 5, ir) }
}