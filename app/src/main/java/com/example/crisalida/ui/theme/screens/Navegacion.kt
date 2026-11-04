package com.example.crisalida.ui.theme.screens

import com.example.crisalida.data.SesionActual
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
import com.example.crisalida.ui.theme.ConEscalaDeTexto
import com.example.crisalida.ui.theme.OutfitFont
import com.example.crisalida.ui.screens.CrisalidaScaffold
// navegación de la barra de abajo, sin acumular historial
fun NavController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo("home")
        launchSingleTop = true
    }
}

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

fun NavGraphBuilder.pantallasPrincipales(navController: NavHostController) {
    val ir: (String) -> Unit = { navController.navigateToTab(it) }
    //pantallas de detalle: se apilan encima y «Volver» regresa a donde se estaba antes
    val abrirReto: (String) -> Unit = { navController.navigate("reto_detalle/$it") }
    val abrirProducto: (String) -> Unit = { navController.navigate("producto_detalle/$it") }
    val editar: (String) -> Unit = { navController.navigate("editar_publicacion/$it") }
    val volver: () -> Unit = { navController.popBackStack() }
    val argId = listOf(navArgument("id") { type = NavType.StringType })

    composable("retos") { ConEscalaDeTexto { RetosScreen(onNavigate = ir, onAbrirDetalle = abrirReto) } }
    composable("marketplace") { ConEscalaDeTexto { MarketplaceScreen(onNavigate = ir, onAbrirDetalle = abrirProducto) } }
    composable("home") { ConEscalaDeTexto { HomeScreen(onNavigate = ir, onAbrirReto = abrirReto) } }
    composable("bienestar") {
        ConEscalaDeTexto { BienestarScreen(onNavigate = ir, onVerRegistros = { navController.navigate("bienestar_registros") }) }
    }
    composable("mascota") { ConEscalaDeTexto { MascotaScreen (onNavigate =  ir) } }
    composable("perfil") {
        ConEscalaDeTexto {
            PerfilScreen(
                onNavigate = ir, onEditar = editar,
                onCerrarSesion = {
                    SesionActual.cerrar()
                    navController.navigate("login") { popUpTo(0) { inclusive = true } }
                }
            )
        }
    }

    composable("respirar_lilu") { ConEscalaDeTexto { RespirarLiluScreen(onNavigate = ir) } }
    composable("portafolio_completo") { ConEscalaDeTexto { PortafolioScreen(onNavigate = ir, onEditar = editar) } }
    composable("publicar_producto") { ConEscalaDeTexto { PublicarProductoScreen(onNavigate = ir) } }

    composable("reto_detalle/{id}", arguments = argId) { entrada ->
        ConEscalaDeTexto {
            RetoDetalleScreen(retoId = entrada.arguments?.getString("id").orEmpty(), onNavigate = ir, onVolver = volver)
        }
    }
    composable("producto_detalle/{id}", arguments = argId) { entrada ->
        ConEscalaDeTexto {
            ProductoDetalleScreen(
                publicacionId = entrada.arguments?.getString("id").orEmpty(),
                onNavigate = ir, onVolver = volver, onEditar = editar
            )
        }
    }
    composable("editar_publicacion/{id}", arguments = argId) { entrada ->
        ConEscalaDeTexto {
            PublicarProductoScreen(onNavigate = ir, publicacionId = entrada.arguments?.getString("id").orEmpty(), onEditado = volver)
        }
    }
    composable("bienestar_registros") { ConEscalaDeTexto { BienestarRegistrosScreen(onNavigate = ir, onVolver = volver) } }
}