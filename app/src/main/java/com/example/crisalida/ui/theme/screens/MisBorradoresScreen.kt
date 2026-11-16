package com.example.crisalida.ui.theme.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crisalida.data.Borradores
import com.example.crisalida.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

private val MkTitulo = Color(0xFF7F284F)
private val MkSubtitulo = Color(0xFF545454)
private val MkFondoBotonVolver = Color(0xFFE2D4DA)
private val MkFondoTarjeta = Color(0xFFF7F0F3)

/**
 * Perfil → Mis borradores (issue 5.9): lista los borradores cifrados de este teléfono, permite abrirlos
 * para seguir editándolos en «Subir producto» y eliminarlos. Sin borradores muestra un mensaje amable.
 */
@Composable
fun MisBorradoresScreen(onNavigate: (String) -> Unit) {
    val contexto = LocalContext.current
    var borradores by remember { mutableStateOf<List<Borradores.Borrador>?>(null) }   // null = leyendo
    var porEliminar by remember { mutableStateOf<Borradores.Borrador?>(null) }

    LaunchedEffect(Unit) {
        borradores = withContext(Dispatchers.IO) {
            Borradores.migrar(contexto)   // por si quedó un borrador de una versión anterior
            Borradores.listar(contexto)
        }
    }

    CrisalidaScaffold(currentTitle = "Mis borradores", selectedTab = 5, onNavigate = onNavigate) { innerPadding ->
        ContenidoCentrado(innerPadding) {
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(onClick = { onNavigate("perfil") }, shape = RoundedCornerShape(18.dp), color = MkFondoBotonVolver, shadowElevation = 1.dp) {
                    Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = MkTitulo, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Perfil", fontFamily = OutfitFont, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MkTitulo)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Mis borradores", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, fontSize = 28.sp, color = MkTitulo)
                Text(
                    "Se guardan cifrados solo en este teléfono, sin fotos ni tu dirección privada.",
                    fontFamily = JakartaSansFont, fontSize = 13.sp, color = MkSubtitulo
                )
                Spacer(modifier = Modifier.height(16.dp))

                val lista = borradores
                when {
                    lista == null -> CircularProgressIndicator(color = MkTitulo)
                    lista.isEmpty() -> Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Outlined.BookmarkBorder, contentDescription = null, tint = MkTitulo, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Aún no tienes borradores", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MkTitulo, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Cuando dejes una publicación a medias y toques «Guardar Borrador», la encontrarás aquí para seguir cuando quieras.",
                            fontFamily = JakartaSansFont, fontSize = 14.sp, lineHeight = 20.sp, color = MkSubtitulo, textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { onNavigate("publicar_producto") },
                            modifier = Modifier.heightIn(min = 48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MkTitulo)
                        ) { Text("Crear una publicación", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, color = Color.White) }
                    }
                    else -> lista.forEach { b ->
                        TarjetaBorrador(
                            b,
                            onAbrir = { Borradores.porAbrir = b.id; onNavigate("publicar_producto") },
                            onEliminar = { porEliminar = b }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    porEliminar?.let { b ->
        AlertDialog(
            onDismissRequest = { porEliminar = null },
            title = { Text("¿Eliminar este borrador?", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, color = MkTitulo) },
            text = { Text("«${b.titulo}» se borrará de este teléfono. No se puede deshacer.", fontFamily = JakartaSansFont, color = MkSubtitulo) },
            confirmButton = {
                TextButton(onClick = {
                    Borradores.eliminar(contexto, b.id)
                    borradores = Borradores.listar(contexto)
                    porEliminar = null
                }) { Text("Eliminar", fontWeight = FontWeight.Bold, color = MkTitulo) }
            },
            dismissButton = { TextButton(onClick = { porEliminar = null }) { Text("Cancelar", color = MkTitulo) } },
            containerColor = Color.White
        )
    }
}

@Composable
private fun TarjetaBorrador(b: Borradores.Borrador, onAbrir: () -> Unit, onEliminar: () -> Unit) {
    val j = remember(b.datos) { try { JSONObject(b.datos) } catch (e: Exception) { JSONObject() } }
    val tipo = if (j.optInt("tipo", 0) == 1) "Servicio" else "Producto"
    val precio = j.optString("precio").takeIf { it.isNotBlank() }?.let { " · \$$it" } ?: ""
    val fecha = java.text.SimpleDateFormat("d MMM yyyy, HH:mm", java.util.Locale("es", "MX")).format(java.util.Date(b.actualizadoEn))
    Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MkFondoTarjeta), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(b.titulo, fontFamily = OutfitFont, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = MkTitulo, maxLines = 2)
            Text("$tipo$precio", fontFamily = JakartaSansFont, fontSize = 13.sp, color = MkSubtitulo)
            Text("Guardado: $fecha", fontFamily = JakartaSansFont, fontSize = 12.sp, color = MkSubtitulo)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onAbrir, modifier = Modifier.heightIn(min = 44.dp), shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MkTitulo)
                ) { Text("Abrir y editar", fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White) }
                OutlinedButton(onClick = onEliminar, modifier = Modifier.heightIn(min = 44.dp), shape = RoundedCornerShape(10.dp)) {
                    Text("Eliminar", fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MkTitulo)
                }
            }
        }
    }
}

/** Los dos accesos del Perfil de la vendedora: Portafolio y Mis borradores (con cuántos hay). */
@Composable
fun AccesosPortafolioYBorradores(onNavigate: (String) -> Unit) {
    val contexto = LocalContext.current
    var cuantos by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) { cuantos = withContext(Dispatchers.IO) { Borradores.listar(contexto).size } }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedButton(
            onClick = { onNavigate("portafolio_completo") },
            modifier = Modifier.weight(1f).heightIn(min = 48.dp), shape = RoundedCornerShape(12.dp)
        ) { Text("Portafolio", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MkTitulo) }
        OutlinedButton(
            onClick = { onNavigate("mis_borradores") },
            modifier = Modifier.weight(1f).heightIn(min = 48.dp), shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                if (cuantos > 0) "Mis borradores ($cuantos)" else "Mis borradores",
                fontFamily = OutfitFont, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MkTitulo, maxLines = 1
            )
        }
    }
}