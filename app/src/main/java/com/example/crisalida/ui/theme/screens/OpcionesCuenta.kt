package com.example.crisalida.ui.theme.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crisalida.data.DatosPersonalesRepositorio
import com.example.crisalida.data.DatosPersonalesResponse
import com.example.crisalida.data.ResultadoApi
import com.example.crisalida.data.SesionActual
import com.example.crisalida.ui.theme.ColorBotonEntrar
import com.example.crisalida.ui.theme.JakartaSansFont
import com.example.crisalida.ui.theme.OutfitFont
import kotlinx.coroutines.launch

private val CuentaTitulo = Color(0xFF7F284F)
private val CuentaTexto = Color(0xFF545454)
private val CuentaExito = Color(0xFF2E7D32)

/**
 * Configuración → Cuenta.
 *  - Mis datos: correo (se confirma con un código), municipio y localidad. Funciona.
 *  - Cambiar de rol, Suspender mi cuenta y Borrar mi cuenta: ya tienen su texto, pero el servidor todavía no tiene
 *    esas funciones; se conectan en sus propios issues (piden confirmar el PIN).
 */
@Composable
fun OpcionesCuenta() {
    val carga = rememberCarga { DatosPersonalesRepositorio.mios() }

    TituloCuenta("Mis datos")
    when (val e = carga.estado) {
        is Carga.Cargando -> IndicadorCargando()
        is Carga.Falla -> MensajeError(e.mensaje, carga.recargar)
        is Carga.Lista -> {
            FilaCorreo(e.datos, carga.recargar)
            Spacer(modifier = Modifier.height(16.dp))
            FilaUbicacion("Municipio", e.datos.municipio, carga.recargar) { DatosPersonalesRepositorio.actualizarMunicipio(it) }
            Spacer(modifier = Modifier.height(16.dp))
            FilaUbicacion("Localidad", e.datos.localidad, carga.recargar) { DatosPersonalesRepositorio.actualizarLocalidad(it) }
        }
    }

    Spacer(modifier = Modifier.height(24.dp))
    TituloCuenta("Cambiar de rol")
    val esCompradora = SesionActual.esCompradora
    TextoCuenta("Tu rol actual: ${if (esCompradora) "Compradora" else "Vendedora"}.")
    TextoCuenta(
        if (esCompradora) "Si cambias a vendedora podrás ofrecer productos y servicios, y tendrás bienestar emocional y mascota."
        else "Si cambias a compradora dejarás de ver tu portafolio y tus publicaciones. No se borran, y puedes volver a vender cuando quieras."
    )
    Proximamente()

    Spacer(modifier = Modifier.height(24.dp))
    TituloCuenta("Suspender mi cuenta")
    TextoCuenta("¿Quieres tomar un descanso? Tu cuenta quedará pausada y nadie podrá ver tu perfil. No perderás nada. Para volver, inicia sesión con tu PIN.")
    Proximamente()

    Spacer(modifier = Modifier.height(24.dp))
    TituloCuenta("Borrar mi cuenta")
    TextoCuenta("¿Borrar tu cuenta para siempre? Se eliminarán tu perfil, tu bitácora, tus publicaciones y tu progreso. No podremos recuperarlos.")
    Proximamente()
}

@Composable
private fun TituloCuenta(texto: String) {
    Text(
        text = texto,
        fontFamily = OutfitFont,
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        color = Color.Black,
        modifier = Modifier.semantics { heading() }
    )
}

@Composable
private fun TextoCuenta(texto: String) {
    Text(text = texto, fontFamily = JakartaSansFont, fontSize = 14.sp, lineHeight = 19.sp, color = CuentaTexto)
}

@Composable
private fun Proximamente() {
    Text(
        text = "Disponible pronto.",
        fontFamily = JakartaSansFont,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = CuentaTitulo,
        modifier = Modifier.padding(top = 4.dp)
    )
}

@Composable
private fun BotonCuenta(texto: String, descripcion: String = texto, habilitado: Boolean = true, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = habilitado,
        modifier = Modifier
            .heightIn(min = 48.dp)
            .semantics { contentDescription = descripcion },
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = ColorBotonEntrar)
    ) {
        Text(texto, fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
    }
}

@Composable
private fun CampoCuenta(
    etiqueta: String,
    valor: String,
    onCambio: (String) -> Unit,
    teclado: KeyboardType,
    habilitado: Boolean
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta, fontFamily = JakartaSansFont) },
        singleLine = true,
        enabled = habilitado,
        keyboardOptions = KeyboardOptions(keyboardType = teclado),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CuentaTitulo,
            focusedLabelColor = CuentaTitulo,
            cursorColor = CuentaTitulo
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

// mensaje de resultado (texto, ¿fue éxito?): TalkBack lo lee cuando aparece
@Composable
private fun MensajeCuenta(mensaje: Pair<String, Boolean>) {
    Text(
        text = mensaje.first,
        fontFamily = JakartaSansFont,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 19.sp,
        color = if (mensaje.second) CuentaExito else CuentaTitulo,
        modifier = Modifier
            .padding(top = 8.dp)
            .semantics { liveRegion = LiveRegionMode.Polite }
    )
}

// Municipio o localidad: se muestra el valor con «Editar»; al editar sale el campo con Guardar y Cancelar
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilaUbicacion(
    etiqueta: String,
    valor: String?,
    alGuardar: () -> Unit,
    guardar: suspend (String) -> ResultadoApi<*>
) {
    val scope = rememberCoroutineScope()
    var editando by remember { mutableStateOf(false) }
    var texto by remember(valor) { mutableStateOf(valor.orEmpty()) }
    var cargando by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf<Pair<String, Boolean>?>(null) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = etiqueta, fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
        if (!editando) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = valor ?: "Sin agregar",
                    fontFamily = JakartaSansFont,
                    fontSize = 15.sp,
                    color = if (valor == null) CuentaTexto else Color.Black,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )
                BotonCuenta("Editar", "Editar $etiqueta") {
                    mensaje = null
                    editando = true
                }
            }
        } else {
            Spacer(modifier = Modifier.height(4.dp))
            CampoCuenta(etiqueta, texto, { texto = it.take(80) }, KeyboardType.Text, !cargando)
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BotonCuenta(if (cargando) "Guardando…" else "Guardar", "Guardar $etiqueta", !cargando) {
                    scope.launch {
                        cargando = true
                        mensaje = null
                        when (val r = guardar(texto)) {
                            is ResultadoApi.Exito -> {
                                editando = false
                                mensaje = "Listo, tu ${etiqueta.lowercase()} se guardó." to true
                                alGuardar()
                            }
                            is ResultadoApi.Error -> mensaje = r.mensaje to false
                        }
                        cargando = false
                    }
                }
                OutlinedButton(
                    onClick = { editando = false; texto = valor.orEmpty() },
                    enabled = !cargando,
                    modifier = Modifier.heightIn(min = 48.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancelar", fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, color = CuentaTitulo)
                }
            }
        }
        mensaje?.let { MensajeCuenta(it) }
    }
}

// Correo: «Editar» manda un código al correo nuevo; mientras no se confirme se ve «Correo sin verificar»
// con «Escribir código» y «Reenviar código»
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilaCorreo(datos: DatosPersonalesResponse, alCambiar: () -> Unit) {
    val scope = rememberCoroutineScope()
    var editando by remember { mutableStateOf(false) }
    var escribiendoCodigo by remember { mutableStateOf(false) }
    var texto by remember(datos.correo) { mutableStateOf(datos.correo.orEmpty()) }
    var codigo by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf<Pair<String, Boolean>?>(null) }

    // guarda el correo (queda sin verificar) y el servidor manda el código; también sirve para reenviarlo
    fun enviarCodigo(correo: String) {
        if (cargando) return
        scope.launch {
            cargando = true
            mensaje = null
            when (val r = DatosPersonalesRepositorio.guardarCorreo(correo)) {
                is ResultadoApi.Exito -> {
                    editando = false
                    if (r.datos.correoVerificado) {
                        escribiendoCodigo = false
                        mensaje = "Tu correo ya está confirmado." to true
                    } else {
                        escribiendoCodigo = true
                        codigo = ""
                        mensaje = (r.datos.mensaje ?: "Te enviamos un código para confirmar que es tuyo.") to true
                    }
                    alCambiar()
                }
                is ResultadoApi.Error -> mensaje = r.mensaje to false
            }
            cargando = false
        }
    }

    fun confirmar() {
        if (cargando) return
        scope.launch {
            cargando = true
            mensaje = null
            when (val r = DatosPersonalesRepositorio.verificarCorreo(codigo)) {
                is ResultadoApi.Exito -> {
                    escribiendoCodigo = false
                    mensaje = "Listo, tu correo quedó confirmado." to true
                    alCambiar()
                }
                is ResultadoApi.Error -> mensaje = r.mensaje to false
            }
            cargando = false
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = "Correo", fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
        if (!editando) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = datos.correo ?: "Sin agregar",
                    fontFamily = JakartaSansFont,
                    fontSize = 15.sp,
                    color = if (datos.correo == null) CuentaTexto else Color.Black,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )
                BotonCuenta("Editar", "Editar correo") {
                    mensaje = null
                    editando = true
                }
            }
            if (datos.correo != null) {
                if (datos.correoVerificado) {
                    Text("Correo verificado", fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CuentaExito)
                } else {
                    Text("Correo sin verificar", fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = CuentaTitulo)
                    if (!escribiendoCodigo) {
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            BotonCuenta("Escribir código", habilitado = !cargando) { mensaje = null; codigo = ""; escribiendoCodigo = true }
                            BotonCuenta("Reenviar código", habilitado = !cargando) { enviarCodigo(datos.correo) }
                        }
                    }
                }
            }
        } else {
            Spacer(modifier = Modifier.height(4.dp))
            CampoCuenta("Correo", texto, { texto = it.take(254) }, KeyboardType.Email, !cargando)
            TextoCuenta("Te enviaremos un código para confirmar que es tuyo.")
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BotonCuenta(if (cargando) "Enviando…" else "Enviar código", "Guardar correo y enviar código", !cargando && texto.isNotBlank()) {
                    enviarCodigo(texto)
                }
                OutlinedButton(
                    onClick = { editando = false; texto = datos.correo.orEmpty() },
                    enabled = !cargando,
                    modifier = Modifier.heightIn(min = 48.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancelar", fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, color = CuentaTitulo)
                }
            }
        }

        if (escribiendoCodigo && !editando) {
            Spacer(modifier = Modifier.height(8.dp))
            CampoCuenta("Código de 6 dígitos", codigo, { codigo = it.filter(Char::isDigit).take(6) }, KeyboardType.NumberPassword, !cargando)
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BotonCuenta(if (cargando) "Confirmando…" else "Confirmar código", habilitado = !cargando && codigo.length == 6) { confirmar() }
                datos.correo?.let { correo ->
                    BotonCuenta("Reenviar código", habilitado = !cargando) { enviarCodigo(correo) }
                }
            }
        }
        mensaje?.let { MensajeCuenta(it) }
    }
}
