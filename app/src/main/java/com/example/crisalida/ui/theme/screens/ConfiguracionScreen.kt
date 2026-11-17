package com.example.crisalida.ui.theme.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crisalida.data.AhorroDatos
import com.example.crisalida.data.AlmacenamientoLocal
import com.example.crisalida.data.BienestarRepositorio
import com.example.crisalida.data.Borradores
import com.example.crisalida.data.ResultadoApi
import com.example.crisalida.data.SesionActual
import com.example.crisalida.ui.theme.AjustesTexto
import com.example.crisalida.ui.theme.ColorBotonEntrar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.crisalida.ui.theme.ColorFondoApp
import com.example.crisalida.ui.theme.ConEscalaLimitada
import com.example.crisalida.ui.theme.JakartaSansFont
import com.example.crisalida.ui.theme.OutfitFont
import com.example.crisalida.data.InactividadSesion
import com.example.crisalida.data.ProteccionPantalla
import com.example.crisalida.ui.theme.ModoApariencia
import com.example.crisalida.ui.theme.Paleta
private val MkTitulo: Color get() = Paleta.titulo
private val MkSubtitulo: Color get() = Paleta.textoGris
private val MkFondoItem: Color get() = Paleta.fondoItem
private val MkExito: Color get() = Paleta.exito

// ⚠️ Correo de contacto y ayuda: cámbialo por el correo real del equipo antes de publicar la app.
private const val CORREO_AYUDA = "ayuda@crisalida.example"

// Configuración: se abre desde el engrane del Perfil. Las secciones Seguridad, Privacidad, Datos y
// almacenamiento y Cuenta se irán llenando en sus propios issues; Apariencia ya tiene el tamaño de
// texto (antes era un diálogo en Perfil) y Ayuda lleva a las líneas de apoyo.
@Composable
fun ConfiguracionScreen(
    onNavigate: (String) -> Unit,
    onVolver: () -> Unit,
    onCuentaBorrada: () -> Unit = {},
    onAbrirRadar: () -> Unit = {}
) {
    val contexto = LocalContext.current
    CrisalidaScaffold(
        currentTitle = "Configuración",
        selectedTab = 5, // sigue resaltando Perfil
        onNavigate = onNavigate
    ) { innerPadding ->
        ContenidoCentrado(innerPadding) {
            ContenidoConfiguracion(
                contexto = contexto,
                onVolver = onVolver,
                onAbrirAyuda = { onNavigate("respirar_lilu") },
                onCuentaBorrada = onCuentaBorrada,
                onAbrirRadar = onAbrirRadar
            )
        }
    }
}

@Composable
private fun ContenidoConfiguracion(
    contexto: Context,
    onVolver: () -> Unit,
    onAbrirAyuda: () -> Unit,
    onCuentaBorrada: () -> Unit,
    onAbrirRadar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        BarraVolver("Volver al perfil", onVolver)

        Column(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            ConEscalaLimitada(tope = 1.3f) {
                Text(
                    text = "Configuración",
                    fontFamily = OutfitFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = MkTitulo,
                    modifier = Modifier.semantics { heading() }
                )
            }

            SeccionConfiguracion("Seguridad") {
                OpcionesInactividad(contexto)
                Spacer(modifier = Modifier.height(16.dp))
                FilaInterruptor(
                    titulo = "Bloquear capturas de pantalla",
                    descripcion = "Impide capturar o grabar la pantalla en Bienestar, Perfil y Configuración. " +
                            "En «apps recientes» el contenido de la app siempre se oculta.",
                    activado = ProteccionPantalla.bloquearCapturas,
                    onCambio = { ProteccionPantalla.cambiar(contexto, it) }
                )
                Spacer(modifier = Modifier.height(16.dp))
                TextoProximamente("Aquí podrás cambiar tu PIN y administrar tu código de recuperación.")
            }

            SeccionConfiguracion("Privacidad") {
                OpcionesPrivacidad(contexto)
            }

            SeccionConfiguracion("Datos y almacenamiento") {
                OpcionesDatosYAlmacenamiento(contexto)
            }

            SeccionConfiguracion("Cuenta") {
                OpcionesCuenta(onCuentaBorrada, onAbrirRadar)
            }

            SeccionConfiguracion("Apariencia") {
                OpcionesTamanoTexto(contexto)
                Spacer(modifier = Modifier.height(20.dp))
                OpcionesModoPantalla(contexto)
            }

            SeccionConfiguracion("Ayuda") {
                FilaNavegable(
                    texto = "Líneas de ayuda y respirar con Lilu",
                    accion = "Abrir líneas de ayuda",
                    onClick = onAbrirAyuda
                )
                Spacer(modifier = Modifier.height(16.dp))
                OpcionContacto(contexto)
            }
        }
    }
}

// Una sección: título (TalkBack lo anuncia como encabezado y permite saltar entre secciones)
// y una tarjeta con su contenido
@Composable
private fun SeccionConfiguracion(
    titulo: String,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = titulo,
            fontFamily = OutfitFont,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MkTitulo,
            modifier = Modifier
                .padding(start = 4.dp)
                .semantics { heading() }
        )
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Paleta.superficie),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                content = contenido
            )
        }
    }
}

@Composable
private fun TextoProximamente(texto: String) {
    Text(
        text = texto,
        fontFamily = JakartaSansFont,
        fontSize = 14.sp,
        lineHeight = 19.sp,
        color = MkSubtitulo
    )
}

@Composable
private fun FilaNavegable(texto: String, accion: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClickLabel = accion, role = Role.Button, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = texto,
            fontFamily = JakartaSansFont,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = MkTitulo,
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MkTitulo
        )
    }
}

// el tamaño de letra de antes (Normal / Grande / Muy grande), con la misma lógica: AjustesTexto lo guarda
// en el teléfono y se aplica al instante
@Composable
private fun OpcionesTamanoTexto(contexto: Context) {
    Text(
        text = "Tamaño del texto",
        fontFamily = OutfitFont,
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        color = Paleta.textoNegro
    )
    Text(
        text = "Elige el que se te haga más cómodo de leer.",
        fontFamily = JakartaSansFont,
        fontSize = 14.sp,
        color = MkSubtitulo
    )
    Spacer(modifier = Modifier.height(8.dp))
    Column(modifier = Modifier.selectableGroup()) {
        AjustesTexto.opciones.forEach { (nombre, valor) ->
            OpcionRadio(
                texto = nombre,
                elegida = AjustesTexto.escala == valor,
                onClick = { AjustesTexto.cambiar(contexto, valor) }
            )
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MkFondoItem)
            .padding(12.dp)
    ) {
        Text(
            text = "Así se va a ver el texto de la app. Lilu te acompaña en cada paso.",
            fontFamily = JakartaSansFont,
            fontSize = 15.sp,
            color = Paleta.textoNegro
        )
    }
}

// modo de pantalla: claro, oscuro o automático (sigue al teléfono). Se guarda en el teléfono y se aplica al instante
@Composable
private fun OpcionesModoPantalla(contexto: Context) {
    Text(
        text = "Modo de pantalla",
        fontFamily = OutfitFont,
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        color = Paleta.textoNegro
    )
    Text(
        text = "Elige cómo se ve la app. «Automático» sigue el modo de tu teléfono.",
        fontFamily = JakartaSansFont,
        fontSize = 14.sp,
        color = MkSubtitulo
    )
    Spacer(modifier = Modifier.height(8.dp))
    Column(modifier = Modifier.selectableGroup()) {
        listOf(
            ModoApariencia.CLARO to "Claro",
            ModoApariencia.OSCURO to "Oscuro",
            ModoApariencia.SISTEMA to "Automático (como el teléfono)"
        ).forEach { (modo, nombre) ->
            OpcionRadio(
                texto = nombre,
                elegida = Paleta.modo == modo,
                onClick = { Paleta.cambiar(contexto, modo) }
            )
        }
    }
}

// cierre de sesión por inactividad: 5, 10, 15, 30 minutos o nunca
@Composable
private fun OpcionesInactividad(contexto: Context) {
    Text(
        text = "Cerrar sesión por inactividad",
        fontFamily = OutfitFont,
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        color = Paleta.textoNegro
    )
    Text(
        text = "Si pasa este tiempo sin que toques la pantalla, tu sesión se cierra y la app se cierra sola. " +
                "Para volver a entrar necesitarás tu PIN.",
        fontFamily = JakartaSansFont,
        fontSize = 14.sp,
        lineHeight = 19.sp,
        color = MkSubtitulo
    )
    Spacer(modifier = Modifier.height(8.dp))
    Column(modifier = Modifier.selectableGroup()) {
        `InactividadSesion`.opciones.forEach { (minutos, nombre) ->
            OpcionRadio(
                texto = nombre,
                elegida = `InactividadSesion`.minutos == minutos,
                onClick = { `InactividadSesion`.cambiar(contexto, minutos) }
            )
        }
    }
}

// interruptor con título y explicación: toda la fila es el interruptor (TalkBack lo anuncia como un solo
// elemento: «Bloquear capturas de pantalla, interruptor, activado»)
@Composable
private fun FilaInterruptor(
    titulo: String,
    descripcion: String,
    activado: Boolean,
    onCambio: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clip(RoundedCornerShape(8.dp))
            .toggleable(value = activado, onValueChange = onCambio, role = Role.Switch),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = titulo,
                fontFamily = OutfitFont,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = Paleta.textoNegro
            )
            Text(
                text = descripcion,
                fontFamily = JakartaSansFont,
                fontSize = 14.sp,
                lineHeight = 19.sp,
                color = MkSubtitulo
            )
        }
        Switch(
            checked = activado,
            onCheckedChange = null, // el cambio lo maneja toda la fila
            colors = SwitchDefaults.colors(checkedTrackColor = MkTitulo)
        )
    }
}

// subtítulo de una opción (TalkBack lo anuncia como encabezado)
@Composable
private fun TituloOpcion(texto: String) {
    Text(
        text = texto,
        fontFamily = OutfitFont,
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        color = Paleta.textoNegro,
        modifier = Modifier.semantics { heading() }
    )
}

// Privacidad: usuarias bloqueadas, permisos del sistema, qué datos guardamos y el aviso con los términos
@Composable
private fun OpcionesPrivacidad(contexto: Context) {
    var avisoAbierto by remember { mutableStateOf<String?>(null) }
    var mensajePermisos by remember { mutableStateOf<String?>(null) }

    TituloOpcion("Usuarias bloqueadas")
    ListaBloqueadas()   // las personas que bloqueaste, con «Desbloquear» (issue 4.9)

    Spacer(modifier = Modifier.height(20.dp))
    TituloOpcion("Permisos de la app")
    TextoProximamente("Aquí puedes ver y cambiar a qué puede acceder Crisálida: ubicación, cámara y fotos.")
    Spacer(modifier = Modifier.height(8.dp))
    BotonAccion(texto = "Abrir ajustes del teléfono") {
        mensajePermisos = if (abrirAjustesDeLaApp(contexto)) null
        else "No pudimos abrir los ajustes. Búscalos en Ajustes del teléfono → Apps → Crisálida → Permisos."
    }
    mensajePermisos?.let { MensajeResultado(it, false) }

    Spacer(modifier = Modifier.height(20.dp))
    TituloOpcion("Qué datos guardamos y para qué")
    Spacer(modifier = Modifier.height(4.dp))
    DatoConTitulo(
        "Lo que guardamos: ",
        if (SesionActual.esCompradora) "tu correo, tu municipio y tu localidad, si decides agregarlos."
        else "tu correo, tu municipio y tu localidad, y lo que registras en tu bitácora."
    )
    DatoConTitulo(
        "Para qué: ",
        "para que puedas entrar a tu cuenta, ver recursos cerca de ti y recuperar tu información si cambias de teléfono."
    )
    DatoConTitulo("Lo que no hacemos: ", "no vendemos tus datos ni los compartimos con terceros.")

    Spacer(modifier = Modifier.height(20.dp))
    TituloOpcion("Aviso de privacidad y términos")
    Spacer(modifier = Modifier.height(4.dp))
    FilaNavegable("Aviso de privacidad", "Leer el aviso de privacidad") { avisoAbierto = AVISO_PRIVACIDAD }
    FilaNavegable("Términos de uso", "Leer los términos de uso") { avisoAbierto = TERMINOS_DE_USO }

    avisoAbierto?.let { DialogoAviso(it) { avisoAbierto = null } }
}

// "Lo que guardamos: texto" con el inicio en negritas
@Composable
private fun DatoConTitulo(titulo: String, texto: String) {
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(titulo) }
            append(texto)
        },
        fontFamily = JakartaSansFont,
        fontSize = 14.sp,
        lineHeight = 19.sp,
        color = MkSubtitulo,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

// abre la pantalla de ajustes de ESTA app en el teléfono (ahí se cambian los permisos)
private fun abrirAjustesDeLaApp(contexto: Context): Boolean = try {
    contexto.startActivity(
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", contexto.packageName, null))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
    true
} catch (e: Exception) {
    false
}

// Ayuda → Contacto: abre la app de correo con un mensaje en blanco para el equipo
@Composable
private fun OpcionContacto(contexto: Context) {
    var mensaje by remember { mutableStateOf<String?>(null) }
    TituloOpcion("Contacto")
    TextoProximamente("¿Tienes dudas o algún problema con la app? Escríbenos. No incluyas tu PIN ni datos que no quieras compartir.")
    Spacer(modifier = Modifier.height(8.dp))
    BotonAccion(texto = "Escribir correo") {
        mensaje = try {
            contexto.startActivity(
                Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", CORREO_AYUDA, null))
                    .putExtra(Intent.EXTRA_SUBJECT, "Ayuda con Crisálida")
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
            null
        } catch (e: Exception) {
            "No encontramos una app de correo. Puedes escribirnos a $CORREO_AYUDA."
        }
    }
    mensaje?.let { MensajeResultado(it, false) }
}

// Datos y almacenamiento: borrar el historial de bienestar (con confirmación) y limpiar la caché del teléfono
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OpcionesDatosYAlmacenamiento(contexto: Context) {
    val scope = rememberCoroutineScope()
    var confirmando by remember { mutableStateOf(false) }
    var borrando by remember { mutableStateOf(false) }
    var mensajeHistorial by remember { mutableStateOf<Pair<String, Boolean>?>(null) }   // texto, ¿fue éxito?
    var limpiando by remember { mutableStateOf(false) }
    var mensajeCache by remember { mutableStateOf<String?>(null) }

    // Ahorro de datos: con él activo las fotos no se descargan solas, se cargan al tocarlas.
    // (El modo sin conexión no es un ajuste: se activa solo cuando se pierde la red.)
    AhorroDatos.cargar(contexto)
    FilaInterruptor(
        titulo = "Ahorro de datos",
        descripcion = "Las fotos no se descargan solas: toca una foto para verla. Útil si tienes pocos datos móviles. " +
                "Se conserva aunque cierres la app.",
        activado = AhorroDatos.activo,
        onCambio = { AhorroDatos.cambiar(contexto, it) }
    )

    // La compradora no tiene bienestar emocional: no hay historial que borrar
    if (!SesionActual.esCompradora) {
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Borrar mi historial",
            fontFamily = OutfitFont,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            color = Paleta.textoNegro,
            modifier = Modifier.semantics { heading() }
        )
        TextoProximamente(
            "Elimina todos los registros de tu bitácora y de tu bienestar, y también los borradores guardados en este teléfono. " +
                    "Tus flores, tu nivel y tu mascota no cambian."
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (!confirmando) {
            BotonAccion(texto = "Borrar mi historial", habilitado = !borrando) {
                mensajeHistorial = null
                confirmando = true
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MkFondoItem)
                    .padding(12.dp)
            ) {
                Text(
                    text = "¿Borrar tu historial?",
                    fontFamily = JakartaSansFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Paleta.textoNegro,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
                )
                Text(
                    text = "Se eliminarán todos los registros de tu bitácora y tus borradores guardados. Esta acción no se puede deshacer.",
                    fontFamily = JakartaSansFont,
                    fontSize = 14.sp,
                    lineHeight = 19.sp,
                    color = MkSubtitulo
                )
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BotonAccion(texto = if (borrando) "Borrando…" else "Sí, borrar", habilitado = !borrando) {
                        borrando = true
                        scope.launch {
                            mensajeHistorial = when (val r = BienestarRepositorio.borrarHistorial()) {
                                is ResultadoApi.Exito -> {
                                    Borradores.borrarTodos(contexto)   // también los borradores de este teléfono
                                    "Listo, tu historial y tus borradores se borraron." to true
                                }
                                is ResultadoApi.Error -> r.mensaje to false
                            }
                            borrando = false
                            confirmando = false
                        }
                    }
                    OutlinedButton(
                        onClick = { confirmando = false },
                        enabled = !borrando,
                        modifier = Modifier.heightIn(min = 48.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancelar", fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, color = MkTitulo)
                    }
                }
            }
        }
        mensajeHistorial?.let { (texto, exito) -> MensajeResultado(texto, exito) }
    }

    Spacer(modifier = Modifier.height(20.dp))
    Text(
        text = "Limpiar caché",
        fontFamily = OutfitFont,
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        color = Paleta.textoNegro,
        modifier = Modifier.semantics { heading() }
    )
    TextoProximamente("Libera espacio en el teléfono. No borra tu cuenta ni tu información, y tu sesión sigue abierta.")
    Spacer(modifier = Modifier.height(8.dp))
    BotonAccion(texto = if (limpiando) "Limpiando…" else "Limpiar caché", habilitado = !limpiando) {
        limpiando = true
        mensajeCache = null
        scope.launch {
            val liberados = withContext(Dispatchers.IO) { AlmacenamientoLocal.limpiarCache(contexto) }
            mensajeCache = if (liberados > 0)
                "Listo, se liberaron ${AlmacenamientoLocal.formatoTamano(liberados)}. Tu sesión sigue abierta."
            else
                "Listo, tu caché ya estaba limpia. Tu sesión sigue abierta."
            limpiando = false
        }
    }
    mensajeCache?.let { MensajeResultado(it, true) }
}

// botón de acción de Configuración: al menos 48 dp de alto
@Composable
private fun BotonAccion(texto: String, habilitado: Boolean = true, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = habilitado,
        modifier = Modifier.heightIn(min = 48.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = ColorBotonEntrar)
    ) {
        Text(texto, fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Paleta.blanco)
    }
}

// mensaje de resultado: TalkBack lo lee solo cuando aparece
@Composable
private fun MensajeResultado(texto: String, exito: Boolean) {
    Text(
        text = texto,
        fontFamily = JakartaSansFont,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 19.sp,
        color = if (exito) MkExito else MkTitulo,
        modifier = Modifier
            .padding(top = 8.dp)
            .semantics { liveRegion = LiveRegionMode.Polite }
    )
}

// una opción de lista de selección única: toda la fila es el botón (TalkBack: «Grande, botón de selección,
// seleccionado») y mide al menos 48 dp
@Composable
private fun OpcionRadio(texto: String, elegida: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(8.dp))
            .selectable(selected = elegida, onClick = onClick, role = Role.RadioButton),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = elegida,
            onClick = null, // el clic lo maneja toda la fila
            colors = RadioButtonDefaults.colors(selectedColor = MkTitulo),
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        Text(
            text = texto,
            fontFamily = JakartaSansFont,
            fontWeight = if (elegida) FontWeight.Bold else FontWeight.Normal,
            fontSize = 16.sp,
            color = Paleta.textoNegro
        )
    }
}

// Para revisar sin emulador (Preview: 320 dp, texto al 200 %, horizontal)
@PreviewResponsivo
@Composable
private fun PreviewConfiguracion() {
    Box(modifier = Modifier.background(ColorFondoApp)) {
        ContenidoConfiguracion(
            contexto = LocalContext.current,
            onVolver = {},
            onAbrirAyuda = {},
            onCuentaBorrada = {},
            onAbrirRadar = {}
        )
    }
}