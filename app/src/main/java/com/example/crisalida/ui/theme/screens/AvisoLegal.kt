package com.example.crisalida.ui.theme.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crisalida.R
import com.example.crisalida.ui.theme.*

/* ================================================================
 * Aviso de privacidad y Términos de uso (issue 3.7).
 *
 * ⚠️ BORRADOR: los textos están en lenguaje sencillo, pero deben revisarse con asesoría legal y completarse
 * con los datos del responsable antes de publicar la app. Si cambian, sube también AVISO_VERSION en el
 * servidor y VERSION_AVISO en la app: así a todas las usuarias se les vuelve a pedir que lo acepten.
 * ================================================================ */

const val AVISO_PRIVACIDAD = "aviso"
const val TERMINOS_DE_USO = "terminos"

object TextosLegales {

    val avisoPrivacidad = """
Responsable: [completar con el nombre y el domicilio de quien opera Crisálida].

Qué datos guardamos
• Tu seudónimo y tu PIN. El PIN y el PIN de camuflaje se guardan protegidos: nadie, ni nosotras, puede verlos.
• Si te registras para vender: tus respuestas del radar de talentos, tus publicaciones y sus fotos, y lo que anotas en tu bienestar y tu bitácora.
• Si te registras para comprar: tus datos de cuenta y lo que hagas en el marketplace y los retos.
• Solo si tú quieres agregarlos: tu correo, tu municipio y tu localidad.
No te pedimos tu nombre real, tu CURP ni documentos.

Para qué los usamos
• Para que puedas entrar a tu cuenta y mantenerla segura.
• Para recomendarte retos y recursos cerca de ti.
• Para que puedas recuperar tu información si cambias de teléfono.
• Para mantener un espacio seguro: revisar publicaciones y atender reportes.

Con quién los compartimos
No vendemos tus datos ni los compartimos con terceros. Lo que tú publicas (productos, servicios y sus fotos) lo ven otras usuarias, pero tu identidad real nunca se muestra. Podemos entregar información solo si una autoridad competente nos lo exige por ley.

Tus derechos
Puedes pedir ver, corregir o borrar tus datos y oponerte a que los usemos. Desde Configuración puedes borrar tu historial, suspender tu cuenta o borrarla. También puedes escribirnos desde Configuración, en Ayuda → Contacto.

Cambios a este aviso
Si el aviso cambia, te lo mostraremos en la app y te pediremos que lo aceptes otra vez.
""".trimIndent()

    val terminosDeUso = """
Quién puede usar Crisálida
Solo personas mayores de 18 años. Al crear tu cuenta confirmas que lo eres.

Tu cuenta
• Tu seudónimo y tu PIN son personales: no los compartas.
• Guarda tu código de recuperación en un lugar seguro. Se muestra una sola vez.
• Eres responsable de lo que se haga con tu cuenta.

Los dos tipos de cuenta
• Vendedora: puede publicar productos y servicios, aceptar retos y usar el bienestar emocional y la mascota.
• Compradora: puede comprar y publicar retos. No tiene acceso a bienestar emocional, mascota ni a ofrecer productos o servicios.

Lo que no se permite
• Publicar contenido ilegal, engañoso o que ponga en riesgo a otras personas.
• Acosar, amenazar o discriminar a otras usuarias.
• Compartir datos personales de otras personas o intentar descubrir quiénes son.
• Hacerte pasar por otra persona.

Publicaciones y retos
Tú eres responsable de lo que publicas. Podemos revisar, pausar o retirar publicaciones que incumplan estos términos, y suspender cuentas cuando sea necesario para cuidar a la comunidad.

Compras y pagos
El proceso de compra (pago en custodia y entrega) todavía no está activo en esta versión de la app.

Tu seguridad
Crisálida no sustituye a los servicios de emergencia. Si estás en peligro, llama al 911. Dentro de Configuración → Ayuda encuentras líneas de apoyo.

Cambios
Si estos términos cambian, te lo avisaremos en la app y te pediremos que los aceptes otra vez para seguir usando Crisálida.
""".trimIndent()
}

// Ventana con el texto completo (sirve en el registro sin perder lo que la usuaria ya escribió)
@Composable
fun DialogoAviso(tipo: String, onCerrar: () -> Unit) {
    val esAviso = tipo == AVISO_PRIVACIDAD
    AlertDialog(
        onDismissRequest = onCerrar,
        title = {
            Text(
                text = if (esAviso) "Aviso de privacidad" else "Términos de uso",
                fontFamily = OutfitFont,
                fontWeight = FontWeight.Bold,
                color = ColorTextoPrincipal
            )
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = if (esAviso) TextosLegales.avisoPrivacidad else TextosLegales.terminosDeUso,
                    fontFamily = JakartaSansFont,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = Color.Black
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onCerrar) {
                Text("Cerrar", fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, color = ColorTextoPrincipal)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(12.dp)
    )
}

// una casilla con su texto; la casilla mide al menos 48 dp y TalkBack la anuncia con [descripcion]
@Composable
private fun FilaCasilla(
    marcada: Boolean,
    onCambio: (Boolean) -> Unit,
    descripcion: String,
    texto: @Composable () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = marcada,
            onCheckedChange = onCambio,
            colors = CheckboxDefaults.colors(checkedColor = ColorBotonEntrar),
            modifier = Modifier.semantics { contentDescription = descripcion }
        )
        Box(modifier = Modifier.weight(1f)) { texto() }
    }
}

/**
 * Las dos casillas que pide el servidor para crear la cuenta: aviso y términos, y mayoría de edad.
 * [onAbrir] recibe AVISO_PRIVACIDAD o TERMINOS_DE_USO cuando la usuaria toca el enlace.
 */
@Composable
fun CasillasAviso(
    acepta: Boolean,
    onAcepta: (Boolean) -> Unit,
    mayorDeEdad: Boolean,
    onMayorDeEdad: (Boolean) -> Unit,
    onAbrir: (String) -> Unit
) {
    val estiloEnlace = TextLinkStyles(
        SpanStyle(fontWeight = FontWeight.Bold, color = ColorTextoPrincipal, textDecoration = TextDecoration.Underline)
    )
    val textoAviso = buildAnnotatedString {
        append("He leído y acepto el ")
        withLink(LinkAnnotation.Clickable("aviso", estiloEnlace) { onAbrir(AVISO_PRIVACIDAD) }) { append("Aviso de Privacidad") }
        append(" y los ")
        withLink(LinkAnnotation.Clickable("terminos", estiloEnlace) { onAbrir(TERMINOS_DE_USO) }) { append("Términos de uso") }
        append(".")
    }
    Column(modifier = Modifier.fillMaxWidth()) {
        FilaCasilla(acepta, onAcepta, "Acepto el Aviso de Privacidad y los Términos de uso") {
            Text(text = textoAviso, fontFamily = JakartaSansFont, fontSize = 13.sp, lineHeight = 18.sp, color = ColorTextoPrincipal)
        }
        FilaCasilla(mayorDeEdad, onMayorDeEdad, "Confirmo que soy mayor de 18 años") {
            Text(
                text = "Confirmo que soy mayor de 18 años.",
                fontFamily = JakartaSansFont, fontSize = 13.sp, lineHeight = 18.sp, color = ColorTextoPrincipal
            )
        }
    }
}

/**
 * Pantalla para cuentas que todavía no aceptan la versión vigente del aviso (por ejemplo, las creadas antes
 * de que existiera, o cuando el aviso cambia). El login la abre cuando el servidor dice aviso_pendiente.
 */
@Composable
fun AvisoPendienteScreen(
    onAceptar: () -> Unit,
    onRechazar: () -> Unit,
    cargando: Boolean = false,
    error: String? = null
) {
    var acepta by remember { mutableStateOf(false) }
    var mayor by remember { mutableStateOf(false) }
    var dialogo by remember { mutableStateOf<String?>(null) }

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
            modifier = Modifier.height(100.dp).width(80.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Antes de continuar",
            fontFamily = JakartaSansFont,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = ColorTextoPrincipal,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Para usar Crisálida necesitas aceptar nuestro aviso de privacidad y los términos de uso. Léelos con calma: están escritos en lenguaje sencillo.",
            fontFamily = JakartaSansFont,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = ColorTextoPrincipal,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))

        CasillasAviso(acepta, { acepta = it }, mayor, { mayor = it }, onAbrir = { dialogo = it })

        Spacer(modifier = Modifier.height(12.dp))
        error?.let { MensajeError(it) }
        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onAceptar,
            enabled = acepta && mayor && !cargando,
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ColorBotonEntrar,
                disabledContainerColor = ColorBotonEntrar.copy(alpha = 0.5f)
            )
        ) {
            if (cargando) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.5.dp)
            } else {
                Text("Aceptar y continuar", fontFamily = OutfitFont, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
            }
        }
        TextButton(onClick = onRechazar, enabled = !cargando, modifier = Modifier.heightIn(min = 48.dp)) {
            Text("No acepto, salir", fontFamily = JakartaSansFont, fontWeight = FontWeight.Bold, color = ColorTextoPrincipal)
        }
    }

    dialogo?.let { DialogoAviso(it) { dialogo = null } }
}

@PreviewAcceso
@Composable
private fun AvisoPendientePreview() {
    AvisoPendienteScreen(onAceptar = {}, onRechazar = {})
}
