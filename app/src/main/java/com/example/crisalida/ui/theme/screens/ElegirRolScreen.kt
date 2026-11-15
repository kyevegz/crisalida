package com.example.crisalida.ui.theme.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crisalida.R
import com.example.crisalida.data.ROL_COMPRADORA
import com.example.crisalida.data.ROL_VENDEDORA
import com.example.crisalida.ui.theme.*

/**
 * Primer paso del registro (issue 2.4): la usuaria elige cómo va a usar Crisálida.
 * El rol decide el resto: la vendedora hace el radar; la compradora tiene un registro corto
 * y no ve bienestar emocional, mascota ni portafolio.
 */
@Composable
fun ElegirRolScreen(
    onElegir: (String) -> Unit,          // recibe ROL_VENDEDORA o ROL_COMPRADORA
    onNavigateToLogin: () -> Unit
) {
    ContenedorAcceso {
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
            HorizontalDivider(modifier = Modifier.weight(1f), thickness = 1.dp, color = ColorTextoPrincipal)
            Text(
                text = "  Regístrate  ",
                fontFamily = JakartaSansFont,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = ColorTextoPrincipal,
                textAlign = TextAlign.Center
            )
            HorizontalDivider(modifier = Modifier.weight(1f), thickness = 1.dp, color = ColorTextoPrincipal)
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
            fontSize = 13.sp,
            color = ColorTextoPrincipal,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        OpcionRol(
            titulo = "Quiero vender",
            descripcion = "Ideal si te encuentras en una situación de vulnerabilidad y buscas obtener ingresos económicos.",
            onClick = { onElegir(ROL_VENDEDORA) }
        )

        Spacer(modifier = Modifier.height(20.dp))

        OpcionRol(
            titulo = "Quiero comprar y publicar retos",
            descripcion = "Ideal si quieres ayudar a otras usuarias. No tienes acceso a los apartados de bienestar emocional, mascota ni opciones de ofrecer productos o servicios.",
            onClick = { onElegir(ROL_COMPRADORA) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        EnlaceInferior(
            texto = "¿Ya tienes una cuenta activa?",
            enlace = "Inicia sesión aquí",
            onClick = onNavigateToLogin
        )
    }
}

// botón + su explicación: TalkBack los lee juntos como una sola opción
@Composable
private fun OpcionRol(titulo: String, descripcion: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {},
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ColorBotonEntrar)
        ) {
            Text(
                text = titulo,
                fontFamily = OutfitFont,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = descripcion,
            fontFamily = JakartaSansFont,
            fontSize = 12.sp,
            lineHeight = 17.sp,
            color = ColorTextoPrincipal,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}
