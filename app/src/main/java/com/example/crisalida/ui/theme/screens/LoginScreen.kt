package com.example.crisalida.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crisalida.R
import com.example.crisalida.ui.theme.*

@Composable
fun LoginScreen(
    onLoginClick: (String, String) -> Unit,
    onNavigateToRegister: () -> Unit,
    cargando: Boolean = false,   // true mientras se espera al servidor
    error: String? = null        // mensaje a mostrar si algo falló
) {
    var seudonimo by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var pinVisible by remember { mutableStateOf(false) }

    val puedeEntrar = !cargando && seudonimo.isNotBlank() && pin.length == 6
    val foco = LocalFocusManager.current

    // al completar los 6 dígitos entra sola, el botón sigue por si lo prefiere
    LaunchedEffect(pin) {
        if (puedeEntrar) {
            foco.clearFocus()
            onLoginClick(seudonimo, pin)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorFondoApp)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Crisálida",
                fontFamily = OutfitFont,
                fontWeight = FontWeight.Bold,
                fontSize = 42.sp,
                color = ColorTextoPrincipal
            )

            Spacer(modifier = Modifier.height(12.dp))

            Image(
                painter = painterResource(id = R.drawable.crisalida_logo),
                contentDescription = "Crisálida Logo",
                modifier = Modifier
                    .height(160.dp)
                    .width(130.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(0.85f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), thickness = 1.dp, color = ColorTextoPrincipal)
                Text(
                    text = "  Inicia sesión  ",
                    fontFamily = JakartaSansFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = ColorTextoPrincipal
                )
                HorizontalDivider(modifier = Modifier.weight(1f), thickness = 1.dp, color = ColorTextoPrincipal)
            }

            Spacer(modifier = Modifier.height(28.dp))

            // CAMPO SEUDÓNIMO
            Column(modifier = Modifier.fillMaxWidth(0.9f)) {
                Text(
                    text = "Seudónimo",
                    fontFamily = JakartaSansFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = ColorTextoPrincipal
                )
                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(10.dp))
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
                            fontSize = 16.sp,
                            color = ColorTextoPrincipal
                        ),
                        decorationBox = { innerTextField ->
                            if (seudonimo.isEmpty()) {
                                Text(
                                    text = "Ej. Mariposa1",
                                    fontFamily = JakartaSansFont,
                                    fontSize = 16.sp,
                                    color = ColorPlaceholder
                                )
                            }
                            innerTextField()
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // PIN CON CAJITAS Y OJITO (iconos de la librería Material, sin drawables propios)
            Column(modifier = Modifier.fillMaxWidth(0.9f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "PIN",
                            fontFamily = JakartaSansFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = ColorTextoPrincipal
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { pinVisible = !pinVisible },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (pinVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                contentDescription = if (pinVisible) "Ocultar PIN" else "Ver PIN",
                                tint = ColorTextoPrincipal,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    TextButton(onClick = { /* "¿Olvidaste tu PIN?" — se hará más adelante */ }) {
                        Text(
                            text = "¿Olvidaste tu PIN?",
                            fontFamily = JakartaSansFont,
                            fontSize = 12.sp,
                            color = ColorTextoPrincipal
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box {
                    // Campo invisible que recibe el teclado numérico
                    BasicTextField(
                        value = pin,
                        onValueChange = { if (it.length <= 6 && it.all(Char::isDigit)) pin = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.matchParentSize(),
                        textStyle = TextStyle(color = Color.Transparent)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (i in 0 until 6) {
                            val caracter = if (i < pin.length) {
                                if (pinVisible) pin[i].toString() else "•"
                            } else ""

                            Box(
                                modifier = Modifier
                                    .size(width = 46.dp, height = 54.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ColorCamposFondo),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = caracter,
                                    fontFamily = JakartaSansFont,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ColorTextoPrincipal,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            MensajeError(error)

            Spacer(modifier = Modifier.height(26.dp))

            // BOTÓN ENTRAR
            Button(
                onClick = { onLoginClick(seudonimo, pin) },
                enabled = puedeEntrar,
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorBotonEntrar,
                    disabledContainerColor = ColorBotonEntrar.copy(alpha = 0.5f)
                )
            ) {
                if (cargando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(26.dp),
                        color = Color.White,
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Text(
                        text = "Entrar",
                        fontFamily = OutfitFont,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "¿Aún no tienes cuenta? ",
                    fontFamily = JakartaSansFont,
                    fontSize = 13.sp,
                    color = ColorTextoPrincipal
                )
                TextButton(
                    onClick = { onNavigateToRegister() },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = "Crea una cuenta aquí",
                        fontFamily = JakartaSansFont,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                        color = ColorTextoPrincipal
                    )
                }
            }
        }
    }
}