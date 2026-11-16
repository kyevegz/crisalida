package com.example.crisalida.ui.theme.screens

import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crisalida.data.RecuperacionRepositorio
import com.example.crisalida.data.ResultadoApi
import com.example.crisalida.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.crisalida.ui.theme.Paleta

/*
 * Olvidé mi PIN (issue 4.7). Tres pantallas, todas ANTES de iniciar sesión:
 *   1. RecuperarPinMetodoScreen  → elegir cómo: código de recuperación o correo
 *   2. RecuperarPinCodigoScreen  → seudónimo + código de recuperación + PIN nuevo (dos veces)
 *   3. RecuperarPinCorreoScreen  → seudónimo + correo → «Recibir correo» → código recibido + PIN nuevo (dos veces)
 * Los mensajes de error son los del servidor y son siempre iguales: no dicen si el seudónimo existe.
 * Seudónimo, código y PIN viven solo en memoria (remember, nunca rememberSaveable ni disco).
 */

private const val LARGO_CODIGO = 6
private const val ESPERA_REENVIO_SEGUNDOS = 60

// ---------------------------------------------------------------- 1. elegir el método

@Composable
fun RecuperarPinMetodoScreen(onCodigo: () -> Unit, onCorreo: () -> Unit, onVolver: () -> Unit) {
    ContenedorAcceso {
        TituloCambiarPin()
        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = buildAnnotatedString {
                append("Para cambiar tu PIN debes ingresar el ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("código de recuperación") }
                append(" que se te dio en tu registro.")
            },
            fontFamily = JakartaSansFont, fontSize = 17.sp, lineHeight = 25.sp,
            color = Paleta.textoGris, textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        BotonMetodo("Código recuperación", onCodigo)

        Spacer(modifier = Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            HorizontalDivider(modifier = Modifier.weight(1f), thickness = 1.dp, color = ColorTextoPrincipal)
            Text("  o  ", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = ColorTextoPrincipal)
            HorizontalDivider(modifier = Modifier.weight(1f), thickness = 1.dp, color = ColorTextoPrincipal)
        }
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = buildAnnotatedString {
                append("Si tienes un ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("correo") }
                append(" vinculado puedes usarlo para cambiar tu PIN.")
            },
            fontFamily = JakartaSansFont, fontSize = 17.sp, lineHeight = 25.sp,
            color = Paleta.textoGris, textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        BotonMetodo("Correo electrónico", onCorreo)

        Spacer(modifier = Modifier.height(16.dp))
        TextButton(onClick = onVolver) {
            Text("Volver a iniciar sesión", fontFamily = JakartaSansFont, fontSize = 13.sp, color = ColorTextoPrincipal)
        }
    }
}

@Composable
private fun BotonMetodo(texto: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = ColorBotonEntrar)
    ) {
        Text(texto, fontFamily = OutfitFont, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, color = Paleta.blanco)
        Spacer(modifier = Modifier.width(10.dp))
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Paleta.blanco)
    }
}

// ---------------------------------------------------------------- 2. con el código de recuperación

@Composable
fun RecuperarPinCodigoScreen(onVolver: () -> Unit, onListo: () -> Unit) {
    val scope = rememberCoroutineScope()
    var seudonimo by remember { mutableStateOf("") }
    var codigo by remember { mutableStateOf("") }
    var nuevo by remember { mutableStateOf("") }
    var confirmar by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var listo by remember { mutableStateOf<String?>(null) }

    val puede = !cargando && seudonimo.isNotBlank() && codigo.length == LARGO_CODIGO &&
            nuevo.length == 6 && confirmar.length == 6

    ContenedorAcceso {
        TituloCambiarPin()
        Spacer(modifier = Modifier.height(24.dp))

        CampoTextoAcceso("Seudónimo", seudonimo, { seudonimo = it.take(30) }, "Ej. Mariposa1")
        Spacer(modifier = Modifier.height(12.dp))
        SeccionCodigo("Código de recuperación", codigo, { codigo = it }, soloDigitos = false)
        Spacer(modifier = Modifier.height(20.dp))
        SeccionPin("Nuevo PIN", nuevo) { nuevo = it }
        Spacer(modifier = Modifier.height(20.dp))
        SeccionPin("Confirmar PIN", confirmar) { confirmar = it }

        MensajeError(error)
        Spacer(modifier = Modifier.height(24.dp))
        BotonCambiar(puede, cargando) {
            error = null
            if (nuevo != confirmar) { error = "Los PIN no coinciden"; return@BotonCambiar }
            scope.launch {
                cargando = true
                when (val r = RecuperacionRepositorio.conCodigo(seudonimo, codigo, nuevo)) {
                    is ResultadoApi.Exito -> { codigo = ""; nuevo = ""; confirmar = ""; listo = r.datos.mensaje }
                    is ResultadoApi.Error -> error = r.mensaje
                }
                cargando = false
            }
        }
        TextButton(onClick = onVolver) {
            Text("Volver", fontFamily = JakartaSansFont, fontSize = 13.sp, color = ColorTextoPrincipal)
        }
    }
    listo?.let { DialogoListo(it, onListo) }
}

// ---------------------------------------------------------------- 3. con un código enviado al correo

@Composable
fun RecuperarPinCorreoScreen(onVolver: () -> Unit, onListo: () -> Unit) {
    val scope = rememberCoroutineScope()
    var seudonimo by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var codigo by remember { mutableStateOf("") }
    var nuevo by remember { mutableStateOf("") }
    var confirmar by remember { mutableStateOf("") }
    var pidiendo by remember { mutableStateOf(false) }
    var cargando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var aviso by remember { mutableStateOf<String?>(null) }
    var espera by remember { mutableIntStateOf(0) }   // segundos que faltan para poder pedir otro correo
    var listo by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(espera) { if (espera > 0) { delay(1000); espera-- } }

    val correoValido = Patterns.EMAIL_ADDRESS.matcher(correo.trim()).matches()
    val puedePedir = !pidiendo && !cargando && espera == 0 && seudonimo.isNotBlank() && correoValido
    val puede = !cargando && seudonimo.isNotBlank() && correoValido && codigo.length == LARGO_CODIGO &&
            nuevo.length == 6 && confirmar.length == 6

    ContenedorAcceso {
        TituloCambiarPin()
        Spacer(modifier = Modifier.height(24.dp))

        CampoTextoAcceso("Seudónimo", seudonimo, { seudonimo = it.take(30) }, "Ej. Mariposa1")
        Spacer(modifier = Modifier.height(12.dp))
        CampoTextoAcceso("Correo electrónico", correo, { correo = it.take(254) }, "Ej. correo@dominio.com", KeyboardType.Email)
        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Button(
                onClick = {
                    error = null
                    scope.launch {
                        pidiendo = true
                        when (val r = RecuperacionRepositorio.solicitarCodigoPorCorreo(seudonimo, correo)) {
                            // el servidor responde igual exista o no la cuenta; la app también
                            is ResultadoApi.Exito -> {
                                aviso = "Si los datos son correctos, te enviamos un código. Revisa tu bandeja de entrada y el correo no deseado."
                                espera = ESPERA_REENVIO_SEGUNDOS
                            }
                            is ResultadoApi.Error -> { aviso = null; error = r.mensaje }
                        }
                        pidiendo = false
                    }
                },
                enabled = puedePedir,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorTextoPrincipal.copy(alpha = 0.85f),
                    disabledContainerColor = ColorTextoPrincipal.copy(alpha = 0.4f)
                )
            ) {
                if (pidiendo) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Paleta.blanco, strokeWidth = 2.dp)
                else Text(
                    if (espera > 0) "Reenviar en ${espera}s" else "Recibir correo",
                    fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Paleta.blanco
                )
            }
        }
        aviso?.let {
            Text(
                it, fontFamily = JakartaSansFont, fontSize = 13.sp, color = ColorTextoPrincipal,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        SeccionCodigo("Código recibido", codigo, { codigo = it }, soloDigitos = false)
        Spacer(modifier = Modifier.height(20.dp))
        SeccionPin("Nuevo PIN", nuevo) { nuevo = it }
        Spacer(modifier = Modifier.height(20.dp))
        SeccionPin("Confirmar PIN", confirmar) { confirmar = it }

        MensajeError(error)
        Spacer(modifier = Modifier.height(24.dp))
        BotonCambiar(puede, cargando) {
            error = null
            if (nuevo != confirmar) { error = "Los PIN no coinciden"; return@BotonCambiar }
            scope.launch {
                cargando = true
                when (val r = RecuperacionRepositorio.conCorreo(seudonimo, correo, codigo, nuevo)) {
                    is ResultadoApi.Exito -> { codigo = ""; nuevo = ""; confirmar = ""; listo = r.datos.mensaje }
                    is ResultadoApi.Error -> error = r.mensaje
                }
                cargando = false
            }
        }
        TextButton(onClick = onVolver) {
            Text("Volver", fontFamily = JakartaSansFont, fontSize = 13.sp, color = ColorTextoPrincipal)
        }
    }
    listo?.let { DialogoListo(it, onListo) }
}

// ---------------------------------------------------------------- piezas comunes

@Composable
private fun TituloCambiarPin() {
    ConEscalaLimitada(tope = 1.2f) {
        Text(
            text = "Cambiar mi PIN",
            fontFamily = OutfitFont, fontWeight = FontWeight.Medium, fontSize = 40.sp, lineHeight = 46.sp,
            color = ColorTextoPrincipal, textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun BotonCambiar(habilitado: Boolean, cargando: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = habilitado,
        modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = ColorBotonEntrar,
            disabledContainerColor = ColorBotonEntrar.copy(alpha = 0.5f)
        )
    ) {
        if (cargando) CircularProgressIndicator(modifier = Modifier.size(26.dp), color = Paleta.blanco, strokeWidth = 2.5.dp)
        else Text("Cambiar", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, fontSize = 25.sp, color = Paleta.blanco)
    }
}

@Composable
private fun DialogoListo(mensaje: String, onListo: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},   // hay que tocar el botón: así se regresa al inicio de sesión
        title = { Text("PIN actualizado", fontFamily = OutfitFont, fontWeight = FontWeight.Bold, color = ColorTextoPrincipal) },
        text = { Text(mensaje, fontFamily = JakartaSansFont, color = Paleta.textoGris) },
        confirmButton = {
            TextButton(onClick = onListo) {
                Text("Ir a iniciar sesión", fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, color = ColorTextoPrincipal)
            }
        },
        containerColor = Paleta.superficie
    )
}

@Composable
private fun CampoTextoAcceso(
    etiqueta: String,
    valor: String,
    onCambio: (String) -> Unit,
    placeholder: String,
    teclado: KeyboardType = KeyboardType.Text
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(etiqueta, fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = ColorTextoPrincipal)
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth().heightIn(min = 52.dp)
                .clip(RoundedCornerShape(10.dp)).background(ColorCamposFondo)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            BasicTextField(
                value = valor,
                onValueChange = onCambio,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = teclado),
                textStyle = TextStyle(fontFamily = JakartaSansFont, fontSize = 16.sp, color = ColorTextoPrincipal),
                decorationBox = { campo ->
                    if (valor.isEmpty()) Text(placeholder, fontFamily = JakartaSansFont, fontSize = 16.sp, color = ColorPlaceholder)
                    campo()
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// título + ojito + 6 casillas del PIN (oculto con puntos hasta que se toca el ojito)
@Composable
private fun SeccionPin(titulo: String, pin: String, onCambio: (String) -> Unit) {
    var visible by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxWidth()) {
        EncabezadoConOjo(titulo, visible) { visible = !visible }
        Spacer(modifier = Modifier.height(8.dp))
        CasillasPin(pin = pin, visible = visible, onCambio = onCambio)
    }
}

@Composable
private fun SeccionCodigo(titulo: String, codigo: String, onCambio: (String) -> Unit, soloDigitos: Boolean) {
    var visible by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxWidth()) {
        EncabezadoConOjo(titulo, visible) { visible = !visible }
        Spacer(modifier = Modifier.height(8.dp))
        CasillasCodigo(codigo = codigo, visible = visible, onCambio = onCambio, soloDigitos = soloDigitos)
    }
}

@Composable
private fun EncabezadoConOjo(titulo: String, visible: Boolean, onAlternar: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            titulo, fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, fontSize = 15.sp,
            color = ColorTextoPrincipal, modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onAlternar, modifier = Modifier.size(32.dp)) {
            Icon(
                imageVector = if (visible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                contentDescription = if (visible) "Ocultar" else "Ver",
                tint = Paleta.iconoGris, modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Las 6 casillas del código (el de recuperación y el que llega al correo): letras y números, teclado de texto y
 * mayúsculas automáticas. Con [soloDigitos] solo se aceptan números (ya no se usa en estas pantallas).
 */
@Composable
fun CasillasCodigo(
    codigo: String,
    visible: Boolean,
    onCambio: (String) -> Unit,
    soloDigitos: Boolean,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxWidth()) {
        BasicTextField(
            value = codigo,
            onValueChange = { nuevo ->
                val limpio = nuevo.uppercase().filter { c ->
                    if (soloDigitos) c in '0'..'9' else (c in '0'..'9' || c in 'A'..'Z')
                }.take(LARGO_CODIGO)
                onCambio(limpio)
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = if (soloDigitos) KeyboardCapitalization.None else KeyboardCapitalization.Characters,
                // «Password» da un teclado de texto sin sugerencias (el código no debe quedar en el diccionario del teclado)
                keyboardType = if (soloDigitos) KeyboardType.NumberPassword else KeyboardType.Password
            ),
            cursorBrush = SolidColor(Color.Transparent),
            modifier = Modifier.matchParentSize(),
            textStyle = TextStyle(color = Color.Transparent)
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            for (i in 0 until LARGO_CODIGO) {
                val caracter = if (i < codigo.length) (if (visible) codigo[i].toString() else "•") else ""
                Box(
                    modifier = Modifier
                        .weight(1f).heightIn(min = 54.dp)
                        .clip(RoundedCornerShape(8.dp)).background(ColorCamposFondo),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        caracter, fontFamily = JakartaSansFont, fontSize = 22.sp, fontWeight = FontWeight.Bold,
                        color = ColorTextoPrincipal, textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}