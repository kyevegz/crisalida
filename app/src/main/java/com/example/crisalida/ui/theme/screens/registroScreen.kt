package com.example.crisalida.ui.theme.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crisalida.R
import com.example.crisalida.ui.theme.*

// ==================== DECLARACIÓN DE FUENTES LOCALES ====================


@Composable
fun RegisterScreen(
    onNextClick: (String, String, String) -> Unit,
    onNavigateToLogin: () -> Unit,
    cargando: Boolean = false,   // true mientras se espera al servidor
    error: String? = null        // mensaje a mostrar si algo falló
) {
    var seudonimo by remember { mutableStateOf("") }
    var pinPrincipal by remember { mutableStateOf("") }
    var pinCamuflaje by remember { mutableStateOf("") }
    var pinPrincipalVisible by remember { mutableStateOf(false) }   // ojito para ver/ocultar el PIN
    var pinCamuflajeVisible by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val puedeContinuar = !cargando && seudonimo.trim().length >= 3 &&
            pinPrincipal.length == 6 && pinCamuflaje.length == 6

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorFondoApp)
            .padding(horizontal = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // 1. TÍTULO PRINCIPAL: "Crisálida"
            Text(
                text = "Crisálida",
                fontFamily = OutfitFont,
                fontWeight = FontWeight.Bold,
                fontSize = 40.sp,
                color = ColorTextoPrincipal
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 2. LOGOTIPO / MARIPOSA
            Image(
                painter = painterResource(id = R.drawable.crisalida_logo),
                contentDescription = "Logo Crisálida",
                modifier = Modifier
                    .height(130.dp)
                    .width(105.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 3. TÍTULO CON LÍNEAS DIVISORIAS: "Regístrate"
            Row(
                modifier = Modifier.fillMaxWidth(0.9f),
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
                    color = ColorTextoPrincipal
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    thickness = 1.dp,
                    color = ColorTextoPrincipal
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 4. SUBTÍTULO Y DESCRIPCIÓN
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
                fontSize = 12.sp,
                color = ColorTextoPrincipal,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 5. CAMPO: ELIGE UN SEUDÓNIMO
            Column(modifier = Modifier.fillMaxWidth(0.95f)) {
                Text(
                    text = "Elige un seudónimo",
                    fontFamily = JakartaSansFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = ColorTextoPrincipal
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Seguridad",
                        modifier = Modifier.size(12.dp),
                        tint = ColorPlaceholder
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Es preferible que no uses tu nombre real",
                        fontFamily = JakartaSansFont,
                        fontSize = 11.sp,
                        color = ColorPlaceholder
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(ColorCamposFondo)
                        .padding(horizontal = 16.dp),
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

            // 6. CAMPO: CREA TU PIN PRINCIPAL
            Column(modifier = Modifier.fillMaxWidth(0.95f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Crea tu PIN principal",
                        fontFamily = JakartaSansFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = ColorTextoPrincipal
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = { pinPrincipalVisible = !pinPrincipalVisible }, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = if (pinPrincipalVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                            contentDescription = if (pinPrincipalVisible) "Ocultar PIN" else "Ver PIN",
                            tint = ColorTextoPrincipal,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Seguridad",
                        modifier = Modifier.size(12.dp),
                        tint = ColorPlaceholder
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Guarda y recuerda bien tu PIN de acceso",
                        fontFamily = JakartaSansFont,
                        fontSize = 11.sp,
                        color = ColorPlaceholder
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                Box {
                    BasicTextField(
                        value = pinPrincipal,
                        onValueChange = { if (it.length <= 6 && it.all(Char::isDigit)) pinPrincipal = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.matchParentSize(),
                        textStyle = TextStyle(color = Color.Transparent)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (i in 0 until 6) {
                            val char = if (i < pinPrincipal.length) (if (pinPrincipalVisible) pinPrincipal[i].toString() else "•") else ""
                            Box(
                                modifier = Modifier
                                    .size(width = 42.dp, height = 48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ColorCamposFondo),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = char,
                                    fontFamily = JakartaSansFont,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorTextoPrincipal,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 7. CAMPO: CREA TU PIN DE CAMUFLAJE
            Column(modifier = Modifier.fillMaxWidth(0.95f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Crea tu PIN de camuflaje",
                        fontFamily = JakartaSansFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = ColorTextoPrincipal
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = { pinCamuflajeVisible = !pinCamuflajeVisible }, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = if (pinCamuflajeVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                            contentDescription = if (pinCamuflajeVisible) "Ocultar PIN" else "Ver PIN",
                            tint = ColorTextoPrincipal,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Seguridad",
                        modifier = Modifier.size(12.dp),
                        tint = ColorPlaceholder
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Si alguien te exige abrir la app, ingresa este PIN alternativo y cargará una galería de mariposas.",
                        fontFamily = JakartaSansFont,
                        fontSize = 10.sp,
                        color = ColorPlaceholder
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                Box {
                    BasicTextField(
                        value = pinCamuflaje,
                        onValueChange = { if (it.length <= 6 && it.all(Char::isDigit)) pinCamuflaje = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.matchParentSize(),
                        textStyle = TextStyle(color = Color.Transparent)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (i in 0 until 6) {
                            val char = if (i < pinCamuflaje.length) (if (pinCamuflajeVisible) pinCamuflaje[i].toString() else "•") else ""
                            Box(
                                modifier = Modifier
                                    .size(width = 42.dp, height = 48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ColorCamposFondo),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = char,
                                    fontFamily = JakartaSansFont,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorTextoPrincipal,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 8. TARJETA INFORMATIVA / ADVERTENCIA DE PRIVACIDAD
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
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
                        fontSize = 11.sp,
                        color = ColorTextoPrincipal,
                        lineHeight = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 9. TEXTO EXPLICATIVO ANTES DEL BOTÓN
            Text(
                text = "Una vez hayas elegido un seudónimo y un PIN seguro, será momento de responder un formulario que te ayudará a conocer tus talentos y habilidades.",
                fontFamily = JakartaSansFont,
                fontSize = 11.sp,
                color = ColorTextoPrincipal,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            error?.let { MensajeError(it) }

            Spacer(modifier = Modifier.height(10.dp))

            // 10. BOTÓN "Siguiente"
            Button(
                onClick = { onNextClick(seudonimo, pinPrincipal, pinCamuflaje) },
                enabled = puedeContinuar,
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .height(52.dp),
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
                        text = "Siguiente",
                        fontFamily = OutfitFont,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 11. ENLACE INFERIOR
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "¿Ya tienes una cuenta activa? ",
                    fontFamily = JakartaSansFont,
                    fontSize = 12.sp,
                    color = ColorTextoPrincipal
                )
                TextButton(
                    onClick = { onNavigateToLogin() },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "Inicia sesión aquí",
                        fontFamily = JakartaSansFont,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        color = ColorTextoPrincipal
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}