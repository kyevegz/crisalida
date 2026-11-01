package com.example.crisalida
import com.example.crisalida.ui.theme.*
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.MaterialTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.crisalida.ui.screens.RegisterScreen
import com.example.crisalida.ui.screens.*
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = "login" // Pantalla inicial al abrir la app
                ) {
                    // 1. Ruta de la Pantalla de Login
                    composable("login") {
                        LoginScreen(
                            onLoginClick = { seudonimo, pin ->
                                // AQUÍ ES DONDE NAVEGAMOS AL HOME:
                                navController.navigate("home")
                            },
                            onNavigateToRegister = {
                                // Navega hacia la pantalla de registro
                                navController.navigate("register")
                            }
                        )
                    }

                    // 2. Ruta de la Pantalla de Registro
                    composable("register") {
                        RegisterScreen(
                            onNextClick = { seudonimo, pinPrincipal, pinCamuflaje ->
                                // Acción al presionar "Siguiente" en el Registro
                            },
                            onNavigateToLogin = {
                                // Regresa a la pantalla de inicio de sesión
                                navController.popBackStack()
                            }
                        )
                    }

                    // 3. NUEVA RUTA: Pantalla del Home
                    composable("home") {
                        HomeScreen(
                            onNavigate = { route ->
                                // Esto permite que el menú inferior del Home navegue a otras secciones
                                navController.navigate(route)
                            }
                        )
                    }
                }
            }
        }
    }
}



// ==================== TIPOGRAFÍAS ====================
val OutfitFont = FontFamily(
    Font(R.font.outfit_regular, FontWeight.Normal),
    Font(R.font.outfit_medium, FontWeight.Medium),
    Font(R.font.outfit_semibold, FontWeight.SemiBold),
    Font(R.font.outfit_bold, FontWeight.Bold),
    Font(R.font.outfit_extrabold, FontWeight.ExtraBold)
)

val JakartaSansFont = FontFamily(
    Font(R.font.plusjakartasans_regular, FontWeight.Normal),
    Font(R.font.plusjakartasans_bold, FontWeight.Bold),
    Font(R.font.plusjakartasans_semibold, FontWeight.SemiBold),
    Font(R.font.plusjakartasans_medium, FontWeight.Medium),
    Font(R.font.plusjakartasans_extrabold, FontWeight.ExtraBold)
)

@Composable
fun LoginScreen(
    onLoginClick: (String, String) -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var seudonimo by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var pinVisible by remember { mutableStateOf(false) }

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
            // 1. TÍTULO PRINCIPAL: "Crisálida"
            Text(
                text = "Crisálida",
                fontFamily = OutfitFont,
                fontWeight = FontWeight.Bold,
                fontSize = 42.sp,
                color = ColorTextoPrincipal
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 2. ILUSTRACIÓN DE LA CRISÁLIDA / MARIPOSA
            // (Asegúrate de tener tu imagen en res/drawable/img_crisalida.png)
            Image(
                painter = painterResource(id = R.drawable.crisalida_logo),
                contentDescription = "Crisálida Logo",
                modifier = Modifier
                    .height(160.dp)
                    .width(130.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. TÍTULO CON LÍNEAS DIVISORIAS: "Inicia sesión"
            Row(
                modifier = Modifier.fillMaxWidth(0.85f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    thickness = 1.dp,
                    color = ColorTextoPrincipal
                )
                Text(
                    text = "  Inicia sesión  ",
                    fontFamily = JakartaSansFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = ColorTextoPrincipal
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    thickness = 1.dp,
                    color = ColorTextoPrincipal
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 4. CAMPO SEUDÓNIMO
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .align(Alignment.CenterHorizontally)
            ) {
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
                        onValueChange = { seudonimo = it },
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

            // 5. SECCIÓN PIN CON CAJITAS SEPARADAS Y BOTÓN DE VISIBILIDAD
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .align(Alignment.CenterHorizontally)
            ) {
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
                        // Ícono de ojito simulado para mostrar/ocultar PIN
                        IconButton(
                            onClick = { pinVisible = !pinVisible },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = if (pinVisible) R.drawable.ic_eye_on else R.drawable.ic_eye_off),
                                contentDescription = "Ver PIN",
                                tint = ColorTextoPrincipal
                            )
                        }
                    }

                    TextButton(onClick = { /* Acción Olvidaste tu PIN */ }) {
                        Text(
                            text = "¿Olvidaste tu PIN?",
                            fontFamily = JakartaSansFont,
                            fontSize = 12.sp,
                            color = ColorTextoPrincipal
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Campo oculto real para capturar los 6 dígitos del PIN de forma nativa
                Box {
                    BasicTextField(
                        value = pin,
                        onValueChange = { if (it.length <= 6) pin = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.matchParentSize(),
                        textStyle = TextStyle(color = Color.Transparent)
                    )

                    // Visualización de las 6 cajitas idénticas al mockup
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (i in 0 until 6) {
                            val char = if (i < pin.length) {
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
                                    text = char,
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

            Spacer(modifier = Modifier.height(36.dp))

            // 6. BOTÓN "Entrar"
            Button(
                onClick = { onLoginClick(seudonimo, pin) },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(54.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ColorBotonEntrar)
            ) {
                Text(
                    text = "Entrar",
                    fontFamily = OutfitFont,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 7. TEXTO INFERIOR DE REGISTRO
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