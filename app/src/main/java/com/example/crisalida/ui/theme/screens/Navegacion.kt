package com.example.crisalida.ui.theme.screens

import androidx.compose.ui.platform.LocalContext
import com.example.crisalida.data.AlmacenamientoCifrado
import com.example.crisalida.data.BorradoLocal
import com.example.crisalida.data.ProgresoRepositorio
import com.example.crisalida.data.SesionActual
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.crisalida.ui.theme.ColorBotonEntrar
import com.example.crisalida.ui.theme.JakartaSansFont
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
import com.example.crisalida.ui.theme.screens.CrisalidaScaffold
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

// Las pantallas de la vendedora (bienestar, mascota, portafolio, publicar) no existen para la compradora:
// si llegara a una por error, ve un aviso amable con un botón para volver a Inicio (nunca un error).
@Composable
private fun SoloVendedora(navController: NavHostController, contenido: @Composable () -> Unit) {
    if (SesionActual.esCompradora) {
        PantallaNoDisponible(onNavigate = { navController.navigateToTab(it) })
    } else {
        contenido()
    }
}

@Composable
fun PantallaNoDisponible(onNavigate: (String) -> Unit) {
    CrisalidaScaffold(currentTitle = "Sección no disponible", selectedTab = -1, onNavigate = onNavigate) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ColorFondoApp)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Spa,
                contentDescription = null,
                tint = ColorTextoPrincipal,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Esta sección no está disponible para tu cuenta",
                fontFamily = OutfitFont,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = ColorTextoPrincipal,
                textAlign = TextAlign.Center,
                modifier = Modifier.semantics { heading() }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tu cuenta es de compradora, así que no incluye esta parte de la app. Puedes seguir en Inicio, en el marketplace o en tu perfil.",
                fontFamily = JakartaSansFont,
                fontSize = 15.sp,
                lineHeight = 21.sp,
                color = ColorTextoPrincipal,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = { onNavigate("home") },
                modifier = Modifier.widthIn(min = 200.dp).heightIn(min = 48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ColorBotonEntrar)
            ) {
                Text(text = "Ir a Inicio", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
            }
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
    // publicar un reto: antes se pide el correo si la usuaria no lo tiene (ver PuertaCorreo en MainActivity)
    val publicarReto: () -> Unit = { navController.navigate("requiere_correo/publicar_reto") }
    val argId = listOf(navArgument("id") { type = NavType.StringType })

    composable("retos") { ConEscalaDeTexto(tope = 2f) { RetosScreen(onNavigate = ir, onAbrirDetalle = abrirReto) } }
    composable("marketplace") { ConEscalaDeTexto(tope = 2f) { MarketplaceScreen(onNavigate = ir, onAbrirDetalle = abrirProducto) } }
    composable("home") { ConEscalaDeTexto(tope = 2f) { HomeScreen(onNavigate = ir, onAbrirReto = abrirReto, onAbrirProducto = abrirProducto, onPublicarReto = publicarReto) } }
    composable("bienestar") {
        SoloVendedora(navController) {
            ConEscalaDeTexto { BienestarScreen(onNavigate = ir, onVerRegistros = { navController.navigate("bienestar_registros") }) }
        }
    }
    composable("mascota") { SoloVendedora(navController) { ConEscalaDeTexto { MascotaScreen (onNavigate =  ir) } } }
    composable("perfil") {
        val contexto = LocalContext.current
        ConEscalaDeTexto {
            PerfilScreen(
                onNavigate = ir, onEditar = editar,
                onAbrirConfiguracion = { navController.navigate("configuracion") },
                onPublicarReto = publicarReto,
                onCerrarSesion = {
                    BorradoLocal.todo(contexto)   // borradores, cola y último contenido guardados en el teléfono
                    SesionActual.cerrar()
                    navController.navigate("login") { popUpTo(0) { inclusive = true } }
                }
            )
        }
    }

    // pantalla para publicar retos: se hará en su propio issue
    composable("publicar_reto") {
        ConEscalaDeTexto {
            PublicarRetoScreen(
                onNavigate = ir,
                // si el servidor dice que falta verificar el correo: se vuelve a la puerta del correo y de ahí a esta pantalla
                onVerificarCorreo = {
                    navController.navigate("requiere_correo/publicar_reto") { popUpTo("publicar_reto") { inclusive = true } }
                }
            )
        }
    }
    composable("configuracion") {
        val contexto = LocalContext.current
        ConEscalaDeTexto {
            ConfiguracionScreen(
                onNavigate = ir, onVolver = volver,
                onAbrirRadar = { navController.navigate("radar_vendedora") },
                onCuentaBorrada = {
                    BorradoLocal.todo(contexto)   // la cuenta ya no existe: no queda nada suyo en el teléfono
                    SesionActual.cerrar()
                    navController.navigate("login") { popUpTo(0) { inclusive = true } }
                }
            )
        }
    }
    // Radar de talentos para una cuenta que ya existe (una compradora que vuelve a vender, 6.5). Al terminar regresa a Configuración.
    composable("radar_vendedora") {
        ConEscalaDeTexto(tope = 2f) {
            RadarScreen(
                yaTieneCuenta = true,
                onTerminar = {
                    ProgresoRepositorio.avisarCambio()
                    navController.popBackStack()
                }
            )
        }
    }
    composable("respirar_lilu") { SoloVendedora(navController) { ConEscalaDeTexto { RespirarLiluScreen(onNavigate = ir) } } }
    composable("portafolio_completo") { SoloVendedora(navController) { ConEscalaDeTexto { PortafolioScreen(onNavigate = ir, onEditar = editar) } } }
    composable("mis_borradores") { SoloVendedora(navController) { ConEscalaDeTexto { MisBorradoresScreen(onNavigate = ir) } } }
    composable("publicar_producto") { SoloVendedora(navController) { ConEscalaDeTexto { PublicarProductoScreen(onNavigate = ir) } } }

    composable("reto_detalle/{id}", arguments = argId) { entrada ->
        ConEscalaDeTexto(tope = 2f) {
            RetoDetalleScreen(retoId = entrada.arguments?.getString("id").orEmpty(), onNavigate = ir, onVolver = volver)
        }
    }
    composable("producto_detalle/{id}", arguments = argId) { entrada ->
        ConEscalaDeTexto(tope = 2f) {
            ProductoDetalleScreen(
                publicacionId = entrada.arguments?.getString("id").orEmpty(),
                onNavigate = ir, onVolver = volver, onEditar = editar
            )
        }
    }
    composable("editar_publicacion/{id}", arguments = argId) { entrada ->
        SoloVendedora(navController) {
            ConEscalaDeTexto {
                PublicarProductoScreen(onNavigate = ir, publicacionId = entrada.arguments?.getString("id").orEmpty(), onEditado = volver)
            }
        }
    }
    composable("bienestar_registros") { SoloVendedora(navController) { ConEscalaDeTexto { BienestarRegistrosScreen(onNavigate = ir, onVolver = volver) } } }
}