package com.example.crisalida.ui.theme.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crisalida.ui.theme.AjustesTexto
import com.example.crisalida.ui.theme.ColorFondoApp
import com.example.crisalida.ui.theme.ConEscalaLimitada
import com.example.crisalida.ui.theme.JakartaSansFont
import com.example.crisalida.ui.theme.OutfitFont
import com.example.crisalida.data.InactividadSesion
private val MkTitulo = Color(0xFF7F284F)
private val MkSubtitulo = Color(0xFF545454)
private val MkFondoItem = Color(0xFFEFE8EC)

// Configuración: se abre desde el engrane del Perfil. Las secciones Seguridad, Privacidad, Datos y
// almacenamiento y Cuenta se irán llenando en sus propios issues; Apariencia ya tiene el tamaño de
// texto (antes era un diálogo en Perfil) y Ayuda lleva a las líneas de apoyo.
@Composable
fun ConfiguracionScreen(
    onNavigate: (String) -> Unit,
    onVolver: () -> Unit
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
                onAbrirAyuda = { onNavigate("respirar_lilu") }
            )
        }
    }
}

@Composable
private fun ContenidoConfiguracion(
    contexto: Context,
    onVolver: () -> Unit,
    onAbrirAyuda: () -> Unit
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
                TextoProximamente("Aquí podrás cambiar tu PIN y administrar tu código de recuperación.")
            }

            SeccionConfiguracion("Privacidad") {
                TextoProximamente("Aquí podrás ver y ajustar cómo se protege tu información.")
            }

            SeccionConfiguracion("Datos y almacenamiento") {
                TextoProximamente("Aquí podrás revisar los datos que guarda la app en tu teléfono.")
            }

            SeccionConfiguracion("Cuenta") {
                TextoProximamente("Aquí podrás editar tu correo, tu municipio y tu localidad.")
            }

            SeccionConfiguracion("Apariencia") {
                OpcionesTamanoTexto(contexto)
            }

            SeccionConfiguracion("Ayuda") {
                FilaNavegable(
                    texto = "Líneas de ayuda y respirar con Lilu",
                    accion = "Abrir líneas de ayuda",
                    onClick = onAbrirAyuda
                )
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
            colors = CardDefaults.cardColors(containerColor = Color.White),
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
        color = Color.Black
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
            color = Color.Black
        )
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
        color = Color.Black
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
            color = Color.Black
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
            onAbrirAyuda = {}
        )
    }
}