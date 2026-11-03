package com.example.crisalida.ui.theme.screens

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
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.crisalida.ui.theme.ColorFondoApp
import com.example.crisalida.ui.theme.ColorTextoPrincipal
import com.example.crisalida.ui.theme.OutfitFont
import com.example.crisalida.ui.screens.CrisalidaScaffold
import com.example.crisalida.ui.screens.RespirarLiluScreen
/**
 * Navegación entre pestañas de la barra inferior:
 * una sola copia de cada pantalla en la pila, sin acumular historial y restaurando estado.
 * Usa "home" como base de la pila (después del login debes llegar a "home").
 */
fun NavController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo("home")
        launchSingleTop = true
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
    // Pantallas de detalle: se apilan encima y «Volver» regresa a donde estabas (sin tocar la pila de pestañas)
    val abrirReto: (String) -> Unit = { navController.navigate("reto_detalle/$it") }
    val abrirProducto: (String) -> Unit = { navController.navigate("producto_detalle/$it") }
    val editar: (String) -> Unit = { navController.navigate("editar_publicacion/$it") }
    val volver: () -> Unit = { navController.popBackStack() }
    val argId = listOf(navArgument("id") { type = NavType.StringType })

    composable("retos") { RetosScreen(onNavigate = ir, onAbrirDetalle = abrirReto) }
    composable("marketplace") { MarketplaceScreen(onNavigate = ir, onAbrirDetalle = abrirProducto) }
    composable("home") { HomeScreen(onNavigate = ir, onAbrirReto = abrirReto) }
    composable("bienestar") {
        BienestarScreen(onNavigate = ir, onVerRegistros = { navController.navigate("bienestar_registros") })
    }
    composable("mascota") { MascotaScreen (onNavigate =  ir) }
    composable("perfil") { PerfilScreen (onNavigate =  ir, onEditar = editar) }

    // Rutas de canje y complementos (cada ruta se registra UNA sola vez)
    composable("respirar_lilu") { RespirarLiluScreen(onNavigate = ir) }
    composable("portafolio_completo") { PortafolioScreen(onNavigate = ir, onEditar = editar) }
    composable("publicar_producto") { PublicarProductoScreen(onNavigate = ir) }

    // Detalles y edición
    composable("reto_detalle/{id}", arguments = argId) { entrada ->
        RetoDetalleScreen(retoId = entrada.arguments?.getString("id").orEmpty(), onNavigate = ir, onVolver = volver)
    }
    composable("producto_detalle/{id}", arguments = argId) { entrada ->
        ProductoDetalleScreen(
            publicacionId = entrada.arguments?.getString("id").orEmpty(),
            onNavigate = ir, onVolver = volver, onEditar = editar
        )
    }
    composable("editar_publicacion/{id}", arguments = argId) { entrada ->
        PublicarProductoScreen(onNavigate = ir, publicacionId = entrada.arguments?.getString("id").orEmpty(), onEditado = volver)
    }
    composable("bienestar_registros") { BienestarRegistrosScreen(onNavigate = ir, onVolver = volver) }
}