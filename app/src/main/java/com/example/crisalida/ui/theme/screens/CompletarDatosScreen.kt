package com.example.crisalida.ui.theme.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crisalida.data.DatosPersonalesRepositorio
import com.example.crisalida.data.ResultadoApi
import com.example.crisalida.ui.theme.*
import kotlinx.coroutines.launch
import com.example.crisalida.ui.theme.Paleta

/** Cómo se abre la pantalla de datos opcionales. */
enum class ModoDatos {
    /** Justo después del registro: correo, municipio y localidad, todo opcional (se puede omitir). */
    REGISTRO,

    /** Antes de publicar un reto: se pide solo el correo. Quien lo abra decide qué pasa en onTerminar/onOmitir. */
    CORREO_PARA_RETOS
}

private enum class Paso { DATOS, CODIGO }

private const val DATOS_EXP_CORREO = "Sirve para recuperar tu PIN de forma más cómoda si lo olvidas. Es privado: nadie más lo ve."
private const val DATOS_EXP_MUNICIPIO = "Sirve para que otras usuarias sepan de dónde eres y para buscar productos cerca de ti. Escribe solo el municipio, nunca tu dirección."
private const val DATOS_EXP_LOCALIDAD = "Es el pueblo, colonia o comunidad. Se usa igual que el municipio, para encontrar lo que está cerca de ti."

/**
 * Invita a la usuaria a completar sus datos opcionales (issue 4.3). Cada dato explica, en lenguaje sencillo,
 * para qué se usa. El correo se confirma con un código que llega a su bandeja.
 *
 * [onTerminar]: guardó lo que quiso (o ya no hay nada más que hacer). [onOmitir]: prefirió dejarlo para después.
 */
@Composable
fun CompletarDatosScreen(
    modo: ModoDatos,
    onTerminar: () -> Unit,
    onOmitir: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val soloCorreo = modo == ModoDatos.CORREO_PARA_RETOS

    var paso by remember { mutableStateOf(Paso.DATOS) }
    var correo by remember { mutableStateOf("") }
    var municipio by remember { mutableStateOf("") }
    var localidad by remember { mutableStateOf("") }
    var codigo by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var info by remember { mutableStateOf<String?>(null) }

    // Guarda ubicación (si escribió algo) y manda el código al correo (si lo escribió)
    fun guardar() {
        if (cargando) return
        val hayUbicacion = !soloCorreo && (municipio.isNotBlank() || localidad.isNotBlank())
        val hayCorreo = correo.isNotBlank()
        if (!hayUbicacion && !hayCorreo) {
            if (soloCorreo) error = "Escribe tu correo para continuar." else onTerminar()
            return
        }
        scope.launch {
            cargando = true
            error = null
            info = null
            if (hayUbicacion) {
                val r = DatosPersonalesRepositorio.guardarUbicacion(municipio, localidad)
                if (r is ResultadoApi.Error) {
                    error = r.mensaje
                    cargando = false
                    return@launch
                }
            }
            if (hayCorreo) {
                when (val r = DatosPersonalesRepositorio.guardarCorreo(correo)) {
                    is ResultadoApi.Exito -> {
                        if (r.datos.correoVerificado) onTerminar()
                        else {
                            paso = Paso.CODIGO
                            info = r.datos.mensaje
                        }
                    }
                    is ResultadoApi.Error -> error = r.mensaje
                }
            } else {
                onTerminar()
            }
            cargando = false
        }
    }

    fun confirmarCodigo() {
        if (cargando) return
        scope.launch {
            cargando = true
            error = null
            when (val r = DatosPersonalesRepositorio.verificarCorreo(codigo)) {
                is ResultadoApi.Exito -> onTerminar()
                is ResultadoApi.Error -> error = r.mensaje
            }
            cargando = false
        }
    }

    fun reenviarCodigo() {
        if (cargando) return
        scope.launch {
            cargando = true
            error = null
            info = null
            when (val r = DatosPersonalesRepositorio.guardarCorreo(correo)) {
                is ResultadoApi.Exito -> info = if (r.datos.correoVerificado) "Tu correo ya estaba confirmado." else "Te enviamos un código nuevo."
                is ResultadoApi.Error -> error = r.mensaje
            }
            cargando = false
        }
    }

    val tituloPantalla = if (soloCorreo) "Agrega tu correo" else "Completa tus datos"
    val subtituloPantalla = when {
        paso == Paso.CODIGO -> "Revisa tu correo y escribe el código de 6 dígitos que te enviamos."
        soloCorreo -> "Para publicar retos necesitamos tu correo. Es privado: solo tú lo ves."
        else -> "Son datos opcionales. Puedes omitirlos ahora y agregarlos después en Configuración."
    }

    ContenedorAcceso {
        // Escuchar (8.7): lee el texto visible. No lee lo que escribes (ni tu correo).
        BotonEscuchar(
            listOfNotNull(
                tituloPantalla,
                subtituloPantalla,
                if (paso == Paso.DATOS) "Correo. $DATOS_EXP_CORREO" else "Código de 6 dígitos. Lo enviamos a tu correo. Vence en unos minutos.",
                if (paso == Paso.DATOS && !soloCorreo) "Municipio. $DATOS_EXP_MUNICIPIO" else null,
                if (paso == Paso.DATOS && !soloCorreo) "Localidad. $DATOS_EXP_LOCALIDAD" else null,
                info,
                error,
                if (paso == Paso.CODIGO) "Botón: Confirmar código." else if (soloCorreo) "Botón: Guardar correo." else "Botón: Guardar y continuar.",
                if (paso == Paso.CODIGO) "También puedes reenviar el código o confirmar después." else if (soloCorreo) "O elegir: Ahora no." else "O elegir: Omitir por ahora."
            )
        )
        Text(
            text = tituloPantalla,
            fontFamily = OutfitFont,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            color = ColorTextoPrincipal,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtituloPantalla,
            fontFamily = JakartaSansFont,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = ColorTextoPrincipal,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))

        if (paso == Paso.DATOS) {
            CampoDato(
                etiqueta = "Correo",
                explicacion = DATOS_EXP_CORREO,
                valor = correo,
                onCambio = { correo = it.take(254) },
                placeholder = "tucorreo@ejemplo.com",
                teclado = KeyboardType.Email,
                habilitado = !cargando
            )
            if (!soloCorreo) {
                Spacer(modifier = Modifier.height(16.dp))
                CampoDato(
                    etiqueta = "Municipio",
                    explicacion = DATOS_EXP_MUNICIPIO,
                    valor = municipio,
                    onCambio = { municipio = it.take(80) },
                    placeholder = "Ej. Pénjamo",
                    habilitado = !cargando
                )
                Spacer(modifier = Modifier.height(16.dp))
                CampoDato(
                    etiqueta = "Localidad",
                    explicacion = DATOS_EXP_LOCALIDAD,
                    valor = localidad,
                    onCambio = { localidad = it.take(80) },
                    placeholder = "Ej. La Cruz",
                    habilitado = !cargando
                )
            }
        } else {
            CampoDato(
                etiqueta = "Código de 6 dígitos",
                explicacion = "Lo enviamos a $correo. Vence en unos minutos.",
                valor = codigo,
                onCambio = { codigo = it.filter(Char::isDigit).take(6) },
                placeholder = "000000",
                teclado = KeyboardType.NumberPassword,
                habilitado = !cargando
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        // TalkBack lee los mensajes cuando aparecen
        info?.let {
            Text(
                text = it,
                fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, fontSize = 13.sp,
                color = Paleta.exito, textAlign = TextAlign.Center,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        error?.let {
            Box(modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }) { MensajeError(it) }
        }

        Button(
            onClick = { if (paso == Paso.DATOS) guardar() else confirmarCodigo() },
            enabled = !cargando && (paso == Paso.DATOS || codigo.length == 6),
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ColorBotonEntrar,
                disabledContainerColor = ColorBotonEntrar.copy(alpha = 0.5f)
            )
        ) {
            if (cargando) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Paleta.blanco, strokeWidth = 2.5.dp)
            } else {
                Text(
                    text = if (paso == Paso.CODIGO) "Confirmar código" else if (soloCorreo) "Guardar correo" else "Guardar y continuar",
                    fontFamily = OutfitFont, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Paleta.blanco
                )
            }
        }

        if (paso == Paso.CODIGO) {
            TextButton(onClick = { reenviarCodigo() }, enabled = !cargando, modifier = Modifier.heightIn(min = 48.dp)) {
                Text("Reenviar código", fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, color = ColorTextoPrincipal)
            }
            // sin confirmar el correo se puede seguir: queda como «Correo sin verificar» en Configuración
            TextButton(onClick = onTerminar, enabled = !cargando, modifier = Modifier.heightIn(min = 48.dp)) {
                Text("Confirmar después", fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, color = ColorTextoPrincipal)
            }
        } else {
            TextButton(onClick = onOmitir, enabled = !cargando, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(
                    text = if (soloCorreo) "Ahora no" else "Omitir por ahora",
                    fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, color = ColorTextoPrincipal
                )
            }
        }
    }
}

// un campo con su título y, debajo, para qué sirve ese dato
@Composable
private fun CampoDato(
    etiqueta: String,
    explicacion: String,
    valor: String,
    onCambio: (String) -> Unit,
    placeholder: String,
    teclado: KeyboardType = KeyboardType.Text,
    habilitado: Boolean = true
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = etiqueta,
            fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, fontSize = 14.sp,
            color = ColorTextoPrincipal
        )
        Text(
            text = explicacion,
            fontFamily = JakartaSansFont, fontSize = 13.sp, lineHeight = 18.sp,
            color = ColorTextoPrincipal
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(ColorCamposFondo)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            BasicTextField(
                value = valor,
                onValueChange = onCambio,
                enabled = habilitado,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = teclado, imeAction = ImeAction.Done),
                textStyle = TextStyle(fontFamily = JakartaSansFont, fontSize = 15.sp, color = ColorTextoPrincipal),
                decorationBox = { campo ->
                    if (valor.isEmpty()) {
                        Text(text = placeholder, fontFamily = JakartaSansFont, fontSize = 15.sp, color = ColorPlaceholder)
                    }
                    campo()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = etiqueta }
            )
        }
    }
}

@PreviewAcceso
@Composable
private fun CompletarDatosPreview() {
    CompletarDatosScreen(modo = ModoDatos.REGISTRO, onTerminar = {}, onOmitir = {})
}

@PreviewAcceso
@Composable
private fun CompletarCorreoPreview() {
    CompletarDatosScreen(modo = ModoDatos.CORREO_PARA_RETOS, onTerminar = {}, onOmitir = {})
}

/**
 * Puerta para publicar retos: si la usuaria ya tiene el correo VERIFICADO, pasa de largo ([onListo] al instante);
 * si no (no tiene o no lo ha verificado), le pide el correo (con la explicación de para qué se usa) y pasa cuando lo guarda.
 * Se abre con la ruta "requiere_correo/{destino}": la pantalla de publicar retos solo tiene que navegar
 * a "requiere_correo/publicar_reto" (o a su ruta) y la puerta la deja pasar cuando haya correo.
 */
@Composable
fun PuertaCorreo(onListo: () -> Unit, onCancelar: () -> Unit) {
    var tieneCorreo by remember { mutableStateOf<Boolean?>(null) }   // null: todavía se está revisando
    LaunchedEffect(Unit) { tieneCorreo = DatosPersonalesRepositorio.correoVerificado() }
    LaunchedEffect(tieneCorreo) { if (tieneCorreo == true) onListo() }

    when (tieneCorreo) {
        null, true -> ContenedorAcceso { CircularProgressIndicator(color = ColorBotonEntrar) }
        false -> CompletarDatosScreen(modo = ModoDatos.CORREO_PARA_RETOS, onTerminar = onListo, onOmitir = onCancelar)
    }
}