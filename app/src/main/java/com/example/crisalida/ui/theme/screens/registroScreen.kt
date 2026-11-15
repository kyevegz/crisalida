package com.example.crisalida.ui.theme.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crisalida.R
import com.example.crisalida.ui.theme.*

@Composable
fun RegisterScreen(
    onNextClick: (String, String, String) -> Unit,
    onNavigateToLogin: () -> Unit,
    cargando: Boolean = false,   // true mientras se espera al servidor
    error: String? = null,       // mensaje a mostrar si algo falló
    esCompradora: Boolean = false, // registro corto: sin radar, la cuenta se crea con este botón
    onAvisoCambio: (Boolean, Boolean) -> Unit = { _, _ -> }   // casillas: (acepta aviso y términos, mayor de 18)
) {
    var seudonimo by remember { mutableStateOf("") }
    var pinPrincipal by remember { mutableStateOf("") }
    var pinCamuflaje by remember { mutableStateOf("") }
    var pinPrincipalVisible by remember { mutableStateOf(false) }
    var pinCamuflajeVisible by remember { mutableStateOf(false) }
    var aceptaAviso by remember { mutableStateOf(false) }
    var mayorDeEdad by remember { mutableStateOf(false) }
    var avisoAbierto by remember { mutableStateOf<String?>(null) }   // AVISO_PRIVACIDAD o TERMINOS_DE_USO

    val puedeContinuar = !cargando && seudonimo.trim().length >= 3 &&
            pinPrincipal.length == 6 && pinCamuflaje.length == 6 && aceptaAviso && mayorDeEdad

    // ContenedorAcceso: scroll, barras del sistema, teclado y ancho máximo (ver ComponentesAuth.kt)
    ContenedorAcceso {
        // el título es muy grande: su tamaño crece poco para que "Crisálida" no se parta en 320 dp
        ConEscalaLimitada(tope = 1.2f) {
            Text(
                text = "Crisálida",
                fontFamily = OutfitFont,
                fontWeight = FontWeight.Bold,
                fontSize = 40.sp,
                color = ColorTextoPrincipal,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Image(
            painter = painterResource(id = R.drawable.crisalida_logo),
            contentDescription = "Logo Crisálida",
            modifier = Modifier
                .height(130.dp)
                .width(105.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                thickness = 1.dp,
                color = ColorTextoPrincipal
            )
            Text(
                text = "  Regístrate  ",
                fontFamily = JakartaSansFont,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = ColorTextoPrincipal,
                textAlign = TextAlign.Center
            )
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                thickness = 1.dp,
                color = ColorTextoPrincipal
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Comienza tu camino hacia la independencia",
            fontFamily = JakartaSansFont,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = ColorTextoPrincipal,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Cero requisitos invasivos. Tu identidad real nunca será revelada a terceros, familiares ni contratistas.",
            fontFamily = JakartaSansFont,
            fontWeight = FontWeight.Normal,
            fontSize = 13.sp,
            color = ColorTextoPrincipal,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (esCompradora) "Te registras para comprar y publicar retos" else "Te registras para vender",
            fontFamily = JakartaSansFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            color = ColorBotonEntrar,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        // seudónimo
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Elige un seudónimo",
                fontFamily = JakartaSansFont,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = ColorTextoPrincipal
            )
            NotaConCandado("Es preferible que no uses tu nombre real")
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
                    value = seudonimo,
                    onValueChange = { if (it.length <= 30) seudonimo = it },
                    singleLine = true,
                    textStyle = TextStyle(
                        fontFamily = JakartaSansFont,
                        fontSize = 15.sp,
                        color = ColorTextoPrincipal
                    ),
                    decorationBox = { innerTextField ->
                        if (seudonimo.isEmpty()) {
                            Text(
                                text = "Ej. Mariposa1",
                                fontFamily = JakartaSansFont,
                                fontSize = 15.sp,
                                color = ColorPlaceholder
                            )
                        }
                        innerTextField()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // PIN principal
        Column(modifier = Modifier.fillMaxWidth()) {
            EtiquetaPin(
                titulo = "Crea tu PIN principal",
                visible = pinPrincipalVisible,
                onAlternar = { pinPrincipalVisible = !pinPrincipalVisible }
            )
            NotaConCandado("Guarda y recuerda bien tu PIN de acceso")
            Spacer(modifier = Modifier.height(8.dp))
            CasillasPin(pin = pinPrincipal, visible = pinPrincipalVisible, onCambio = { pinPrincipal = it })
        }

        Spacer(modifier = Modifier.height(16.dp))

        // PIN de camuflaje
        Column(modifier = Modifier.fillMaxWidth()) {
            EtiquetaPin(
                titulo = "Crea tu PIN de camuflaje",
                visible = pinCamuflajeVisible,
                onAlternar = { pinCamuflajeVisible = !pinCamuflajeVisible }
            )
            NotaConCandado("Si alguien te exige abrir la app, ingresa este PIN alternativo y cargará una galería de mariposas.")
            Spacer(modifier = Modifier.height(8.dp))
            CasillasPin(pin = pinCamuflaje, visible = pinCamuflajeVisible, onCambio = { pinCamuflaje = it })
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFE2D4DA))
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Verificado",
                    modifier = Modifier.size(20.dp),
                    tint = ColorTextoPrincipal
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Podrás ingresar datos personales más adelante si así lo deseas, con la garantía de que estos, junto con tus reflexiones íntimas y registros de ventas se protegen con cifrado de extremo a extremo en tu dispositivo.",
                    fontFamily = JakartaSansFont,
                    fontSize = 12.sp,
                    color = ColorTextoPrincipal,
                    lineHeight = 17.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // aviso de privacidad, términos y mayoría de edad: sin esto el servidor no crea la cuenta
        CasillasAviso(
            acepta = aceptaAviso,
            onAcepta = { aceptaAviso = it; onAvisoCambio(aceptaAviso, mayorDeEdad) },
            mayorDeEdad = mayorDeEdad,
            onMayorDeEdad = { mayorDeEdad = it; onAvisoCambio(aceptaAviso, mayorDeEdad) },
            onAbrir = { avisoAbierto = it }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (esCompradora)
                "Tu registro es corto: al terminar entrarás directo a la app."
            else
                "Una vez hayas elegido un seudónimo y un PIN seguro, será momento de responder un formulario que te ayudará a conocer tus talentos y habilidades.",
            fontFamily = JakartaSansFont,
            fontSize = 12.sp,
            color = ColorTextoPrincipal,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        error?.let { MensajeError(it) }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = { onNextClick(seudonimo, pinPrincipal, pinCamuflaje) },
            enabled = puedeContinuar,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ColorBotonEntrar,
                disabledContainerColor = ColorBotonEntrar.copy(alpha = 0.5f)
            )
        ) {
            if (cargando) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.5.dp
                )
            } else {
                Text(
                    text = if (esCompradora) "Crear cuenta" else "Siguiente",
                    fontFamily = OutfitFont,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        EnlaceInferior(
            texto = "¿Ya tienes una cuenta activa?",
            enlace = "Inicia sesión aquí",
            onClick = onNavigateToLogin
        )
    }

    avisoAbierto?.let { DialogoAviso(it) { avisoAbierto = null } }
}

// título del campo de PIN con el ojito; el texto cede espacio para que el ojito nunca se corte
@Composable
private fun EtiquetaPin(titulo: String, visible: Boolean, onAlternar: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = titulo,
            fontFamily = JakartaSansFont,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = ColorTextoPrincipal,
            modifier = Modifier.weight(1f, fill = false)
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(onClick = onAlternar, modifier = Modifier.size(24.dp)) {
            Icon(
                imageVector = if (visible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                contentDescription = if (visible) "Ocultar PIN" else "Ver PIN",
                tint = ColorTextoPrincipal,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// línea gris con candadito; el candado se queda en la primera línea si el texto ocupa varias
@Composable
private fun NotaConCandado(texto: String) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = "Seguridad",
            modifier = Modifier
                .padding(top = 3.dp)
                .size(12.dp),
            tint = ColorPlaceholder
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = texto,
            fontFamily = JakartaSansFont,
            fontSize = 12.sp,
            color = ColorPlaceholder
        )
    }
}