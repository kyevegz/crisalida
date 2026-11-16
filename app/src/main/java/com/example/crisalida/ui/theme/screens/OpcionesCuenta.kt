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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crisalida.data.CuentaRepositorio
import com.example.crisalida.data.DatosPersonalesRepositorio
import com.example.crisalida.data.EstadoCuenta
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
 *  - Suspender mi cuenta (6.3): oculta lo que publica; se reactiva con un botón. No borra nada.
 *  - Borrar mi cuenta (6.3): doble confirmación y PIN. Con pedidos o retos en curso el servidor responde 409 y se ofrece suspender.
 *  - Cambiar de rol: todavía sin conectar.
 */
@Composable
fun OpcionesCuenta(onCuentaBorrada: () -> Unit = {}) {
    val scope = rememberCoroutineScope()
    var mensajeSuspension by remember { mutableStateOf<Pair<String, Boolean>?>(null) }
    var suspendiendo by remember { mutableStateOf(false) }
    // suspender: lo usan el botón de «Suspender» y la oferta que sale cuando borrar da 409
    fun suspender() {
        if (suspendiendo) return
        scope.launch {
            suspendiendo = true
            mensajeSuspension = when (val r = CuentaRepositorio.suspender()) {
                is ResultadoApi.Exito -> { EstadoCuenta.aplicar(r.datos); (r.datos.mensaje ?: "Tu cuenta está en reposo.") to true }
                is ResultadoApi.Error -> r.mensaje to false
            }
            suspendiendo = false
        }
    }

    val carga = rememberCarga { DatosPersonalesRepositorio.mios() }

    TituloCuenta("Mis datos")
    when (val e = carga.estado) {
        is Carga.Cargando -> IndicadorCargando()
        is Carga.Falla -> ErrorDeCarga(e, carga.recargar, "No podemos mostrar ni cambiar tus datos de contacto sin conexión.")
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
    SeccionSuspender(mensajeSuspension, suspendiendo, ::suspender)

    Spacer(modifier = Modifier.height(24.dp))
    SeccionBorrar(onSuspender = ::suspender, onCuentaBorrada = onCuentaBorrada)
}

// ---------------------------------------------------------------- Suspender mi cuenta

@Composable
private fun SeccionSuspender(mensaje: Pair<String, Boolean>?, ocupado: Boolean, onSuspender: () -> Unit) {
    val scope = rememberCoroutineScope()
    var confirmando by remember { mutableStateOf(false) }
    var reactivando by remember { mutableStateOf(false) }
    var mensajeReactivar by remember { mutableStateOf<Pair<String, Boolean>?>(null) }
    LaunchedEffect(Unit) { EstadoCuenta.refrescar() }

    if (EstadoCuenta.suspendida) {
        TituloCuenta("Tu cuenta está suspendida")
        TextoCuenta(
            "Está en reposo" + (EstadoCuenta.desde?.let { " desde el ${fechaLegible(it)}" } ?: "") + ". " +
                    "Tus productos, servicios y retos no se muestran, pero puedes seguir usando tu mascota y tu bienestar. " +
                    "Al reactivarla todo vuelve a verse como estaba."
        )
        Spacer(modifier = Modifier.height(8.dp))
        BotonCuenta(if (reactivando) "Reactivando…" else "Reactivar mi cuenta", habilitado = !reactivando) {
            scope.launch {
                reactivando = true
                mensajeReactivar = when (val r = CuentaRepositorio.reactivar()) {
                    is ResultadoApi.Exito -> { EstadoCuenta.aplicar(r.datos); (r.datos.mensaje ?: "¡Bienvenida de vuelta!") to true }
                    is ResultadoApi.Error -> r.mensaje to false
                }
                reactivando = false
            }
        }
        mensajeReactivar?.let { MensajeCuenta(it) }
    } else {
        TituloCuenta("Suspender mi cuenta")
        TextoCuenta(
            "¿Quieres tomar un descanso? Al suspenderla, tus productos, servicios y retos dejan de mostrarse en el marketplace y en el tablero. " +
                    "No se borra nada y puedes seguir entrando: tu mascota, tu bienestar y tus datos siguen contigo."
        )
        Spacer(modifier = Modifier.height(4.dp))
        TextoCuenta(
            "Para reactivarla, entra a Configuración → Cuenta y toca «Reactivar mi cuenta». " +
                    "También verás un aviso con el botón «Reactivar» en Inicio y en Perfil."
        )
        Spacer(modifier = Modifier.height(8.dp))
        BotonCuenta(if (ocupado) "Suspendiendo…" else "Suspender mi cuenta", habilitado = !ocupado) { mensajeReactivar = null; confirmando = true }
        mensaje?.let { MensajeCuenta(it) }
    }

    if (confirmando) {
        AlertDialog(
            onDismissRequest = { confirmando = false },
            title = { Text("¿Suspender tu cuenta?", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, color = CuentaTitulo) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    TextoCuenta("Qué pasa: tus productos, servicios y retos dejan de verse hasta que la reactives. No se borra nada.")
                    Spacer(modifier = Modifier.height(6.dp))
                    TextoCuenta("Qué sigue igual: puedes entrar, usar tu mascota y tu bienestar, y ver tus datos.")
                    Spacer(modifier = Modifier.height(6.dp))
                    TextoCuenta("Cómo reactivarla: con el botón «Reactivar» en Inicio y en Perfil, o en Configuración → Cuenta.")
                }
            },
            confirmButton = {
                TextButton(onClick = { confirmando = false; onSuspender() }) {
                    Text("Sí, suspender", fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, color = CuentaTitulo)
                }
            },
            dismissButton = { TextButton(onClick = { confirmando = false }) { Text("Cancelar", color = CuentaTitulo) } },
            containerColor = Color.White
        )
    }
}

// ---------------------------------------------------------------- Borrar mi cuenta (doble confirmación + PIN)

@Composable
private fun SeccionBorrar(onSuspender: () -> Unit, onCuentaBorrada: () -> Unit) {
    val scope = rememberCoroutineScope()
    var paso by remember { mutableStateOf(0) }            // 0 nada · 1 primera confirmación · 2 PIN
    var pin by remember { mutableStateOf("") }            // solo en memoria
    var borrando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var enCurso by remember { mutableStateOf<String?>(null) }   // mensaje del servidor cuando responde 409

    TituloCuenta("Borrar mi cuenta")
    TextoCuenta("¿Borrar tu cuenta para siempre? Se eliminarán tu perfil, tu bitácora, tus publicaciones y tu progreso. No podremos recuperarlos.")
    Spacer(modifier = Modifier.height(8.dp))
    BotonCuenta("Borrar mi cuenta") { error = null; pin = ""; paso = 1 }

    if (paso == 1) {
        AlertDialog(
            onDismissRequest = { paso = 0 },
            title = { Text("¿Borrar tu cuenta para siempre?", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, color = CuentaTitulo) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    TextoCuenta("Se eliminarán tu perfil, tu bitácora, tus publicaciones con sus fotos, tu mascota y tu progreso. No podremos recuperarlos.")
                    Spacer(modifier = Modifier.height(6.dp))
                    TextoCuenta("Si solo quieres descansar, mejor suspende tu cuenta: no pierdes nada y la reactivas cuando quieras.")
                }
            },
            confirmButton = {
                TextButton(onClick = { paso = 2 }) { Text("Continuar", fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, color = CuentaTitulo) }
            },
            dismissButton = { TextButton(onClick = { paso = 0 }) { Text("Cancelar", color = CuentaTitulo) } },
            containerColor = Color.White
        )
    }

    if (paso == 2) {
        AlertDialog(
            onDismissRequest = { if (!borrando) { paso = 0; pin = "" } },
            title = { Text("Confirma con tu PIN", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, color = CuentaTitulo) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    TextoCuenta("Última confirmación. Escribe tu PIN para borrar tu cuenta para siempre.")
                    Spacer(modifier = Modifier.height(10.dp))
                    CasillasPin(pin = pin, visible = false, onCambio = { pin = it })
                    error?.let {
                        Text(it, fontFamily = JakartaSansFont, fontSize = 13.sp, color = Color(0xFFB3261E), modifier = Modifier.padding(top = 8.dp))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            borrando = true
                            error = null
                            when (val r = CuentaRepositorio.borrar(pin)) {
                                is ResultadoApi.Exito -> { pin = ""; paso = 0; onCuentaBorrada() }
                                is ResultadoApi.Error -> {
                                    pin = ""
                                    if (r.codigoHttp == 409) { paso = 0; enCurso = r.mensaje }   // pedidos o retos en curso: se ofrece suspender
                                    else error = r.mensaje                                      // PIN incorrecto, bloqueo, sin conexión...
                                }
                            }
                            borrando = false
                        }
                    },
                    enabled = pin.length == 6 && !borrando,
                    colors = ButtonDefaults.buttonColors(containerColor = CuentaTitulo)
                ) { Text(if (borrando) "Borrando…" else "Borrar para siempre", color = Color.White) }
            },
            dismissButton = { TextButton(onClick = { paso = 0; pin = "" }, enabled = !borrando) { Text("Cancelar", color = CuentaTitulo) } },
            containerColor = Color.White
        )
    }

    enCurso?.let { mensaje ->
        AlertDialog(
            onDismissRequest = { enCurso = null },
            title = { Text("Todavía no podemos borrar tu cuenta", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, color = CuentaTitulo) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    TextoCuenta(mensaje)
                    Spacer(modifier = Modifier.height(6.dp))
                    TextoCuenta("Suspender tu cuenta no borra nada: tus publicaciones dejan de verse y la reactivas cuando quieras.")
                }
            },
            confirmButton = {
                TextButton(onClick = { enCurso = null; onSuspender() }) {
                    Text("Suspender mi cuenta", fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, color = CuentaTitulo)
                }
            },
            dismissButton = { TextButton(onClick = { enCurso = null }) { Text("Entendido", color = CuentaTitulo) } },
            containerColor = Color.White
        )
    }
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