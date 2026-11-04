package com.example.crisalida.ui.theme.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crisalida.data.BienestarRepositorio
import com.example.crisalida.data.EntradaBienestar
import com.example.crisalida.data.ResultadoApi
import com.example.crisalida.ui.theme.*
import kotlinx.coroutines.launch
import com.example.crisalida.ui.screens.CrisalidaScaffold
private val Pestanas = listOf(
    "emocion" to "Emociones",
    "monitoreo" to "Monitoreo",
    "reflexion" to "Reflexiones",
    "bitacora" to "Bitácora"
)

// historial de los 4 bloques, con pestañas, se puede borrar cada registro
@Composable
fun BienestarRegistrosScreen(
    onNavigate: (String) -> Unit,
    onVolver: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var pestana by remember { mutableIntStateOf(0) }
    var version by remember { mutableIntStateOf(0) }
    var porBorrar by remember { mutableStateOf<String?>(null) }
    var aviso by remember { mutableStateOf<String?>(null) }
    val subtipo = Pestanas[pestana].first
    val carga = rememberCarga(subtipo, version) { BienestarRepositorio.entradas(subtipo) }

    CrisalidaScaffold(currentTitle = "Mis registros", selectedTab = 3, onNavigate = onNavigate) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ColorFondoApp)
                .padding(innerPadding)
        ) {
            BarraVolver("Volver a Bienestar", onVolver)
            ScrollableTabRow(
                selectedTabIndex = pestana,
                containerColor = ColorFondoApp,
                edgePadding = 12.dp
            ) {
                Pestanas.forEachIndexed { i, (_, titulo) ->
                    Tab(
                        selected = pestana == i,
                        onClick = { pestana = i },
                        text = {
                            Text(
                                titulo, fontFamily = OutfitFont, fontSize = 14.sp,
                                fontWeight = if (pestana == i) FontWeight.Bold else FontWeight.Normal,
                                color = if (pestana == i) ColorBotonEntrar else ColorPlaceholder
                            )
                        }
                    )
                }
            }
            when (val e = carga.estado) {
                is Carga.Cargando -> IndicadorCargando()
                is Carga.Falla -> MensajeError(e.mensaje, carga.recargar)
                is Carga.Lista -> {
                    if (e.datos.entradas.isEmpty()) {
                        Text(
                            "Aún no tienes registros en este apartado. Cuando guardes uno en Bienestar, aparecerá aquí.",
                            fontFamily = JakartaSansFont, fontSize = 14.sp, color = ColorPlaceholder,
                            modifier = Modifier.padding(20.dp)
                        )
                    }
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(e.datos.entradas, key = { it.id }) { entrada ->
                            TarjetaRegistro(entrada, onBorrar = { porBorrar = entrada.id })
                        }
                        item {
                            Text(
                                "Tus registros se guardan cifrados y solo se muestran cuando entras con tu PIN.",
                                fontFamily = JakartaSansFont, fontSize = 12.sp, color = ColorPlaceholder,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    porBorrar?.let { id ->
        AlertDialog(
            onDismissRequest = { porBorrar = null },
            title = { Text("¿Borrar este registro?", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, color = ColorTextoPrincipal) },
            text = { Text("Se elimina para siempre. Las flores que ya ganaste se conservan.", fontFamily = JakartaSansFont, color = ColorTextoPrincipal) },
            confirmButton = {
                Button(
                    onClick = {
                        porBorrar = null
                        scope.launch {
                            when (val r = BienestarRepositorio.borrarEntrada(id)) {
                                is ResultadoApi.Exito -> version++
                                is ResultadoApi.Error -> aviso = r.mensaje
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ColorBotonEntrar)
                ) { Text("Borrar", color = Color.White) }
            },
            dismissButton = { TextButton(onClick = { porBorrar = null }) { Text("Cancelar", color = ColorBotonEntrar) } },
            containerColor = Color.White
        )
    }
    aviso?.let { t ->
        AlertDialog(
            onDismissRequest = { aviso = null },
            text = { Text(t, fontFamily = JakartaSansFont) },
            confirmButton = { TextButton(onClick = { aviso = null }) { Text("Entendido", color = ColorBotonEntrar) } },
            containerColor = Color.White
        )
    }
}

@Composable
private fun TarjetaRegistro(e: EntradaBienestar, onBorrar: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${fechaLegible(e.dia)} · ${e.hora}",
                    fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ColorPlaceholder
                )
                TextButton(onClick = onBorrar) {
                    Text("Borrar", fontFamily = JakartaSansFont, fontSize = 13.sp, color = ColorBotonEntrar)
                }
            }
            when (e.subtipo) {
                "emocion" -> Text(
                    "Me sentía: ${e.emocion.orEmpty()}",
                    fontFamily = OutfitFont, fontWeight = FontWeight.Medium, fontSize = 17.sp, color = ColorTextoPrincipal
                )
                "monitoreo" -> {
                    Escala("Tensión en el cuerpo", e.tension)
                    Escala("Claridad mental", e.claridad)
                    Escala("Energía disponible", e.energia)
                }
                "reflexion" -> {
                    val respuestas = e.respuestas.orEmpty()
                    PreguntasReflexion.forEachIndexed { i, pregunta ->
                        val r = respuestas.getOrNull(i).orEmpty()
                        if (r.isNotBlank()) {
                            Text(pregunta, fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ColorPlaceholder)
                            Text(r, fontFamily = JakartaSansFont, fontSize = 14.sp, lineHeight = 19.sp, color = ColorTextoPrincipal)
                            Spacer(Modifier.height(6.dp))
                        }
                    }
                }
                else -> Text(
                    e.texto.orEmpty(),
                    fontFamily = JakartaSansFont, fontSize = 14.sp, lineHeight = 19.sp, color = ColorTextoPrincipal
                )
            }
            Spacer(Modifier.height(6.dp))
        }
    }
}

@Composable
private fun Escala(titulo: String, valor: Int?) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
        Text(titulo, fontFamily = JakartaSansFont, fontSize = 13.sp, color = ColorTextoPrincipal, modifier = Modifier.weight(1f))
        Text("●".repeat(valor ?: 0) + "○".repeat(5 - (valor ?: 0)), fontSize = 14.sp, color = ColorBotonEntrar)
    }
}
