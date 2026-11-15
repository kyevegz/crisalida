package com.example.crisalida.data

import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.WindowManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

// Protege lo que se ve en pantalla:
//  1) «Apps recientes»: nunca se muestra el contenido de la app (la miniatura sale en blanco).
//  2) Capturas y grabación de pantalla: se bloquean en las pantallas sensibles. La usuaria puede
//     desactivarlo en Configuración → Seguridad.
// En modo camuflaje no se aplica nada: la galería falsa debe comportarse como una galería normal.
// Se usa la bandera FLAG_SECURE de la ventana, que Android respeta tanto para capturas como para recientes.
object ProteccionPantalla {
    // pantallas donde se bloquean las capturas (nombres de ruta de la navegación)
    private val rutasSensibles = setOf("bienestar", "bienestar_registros", "perfil", "configuracion")

    var bloquearCapturas by mutableStateOf(true)
        private set

    private var enPantallaSensible = false
    private var enSegundoPlano = false

    private const val ARCHIVO = "crisalida_ajustes"
    private const val CLAVE = "bloquear_capturas"
    private var cargado = false

    fun cargar(context: Context) {
        if (cargado) return
        cargado = true
        bloquearCapturas = context.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE).getBoolean(CLAVE, true)
    }

    fun cambiar(context: Context, activado: Boolean) {
        bloquearCapturas = activado
        context.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE).edit().putBoolean(CLAVE, activado).apply()
    }

    fun esRutaSensible(ruta: String?): Boolean = ruta in rutasSensibles

    fun alCambiarRuta(ruta: String?) {
        enPantallaSensible = esRutaSensible(ruta)
    }

    fun alPasarASegundoPlano() { enSegundoPlano = true }
    fun alVolverAPrimerPlano() { enSegundoPlano = false }

    // aplica el estado actual a la ventana de la app
    fun aplicar(activity: Activity) {
        val camuflaje = SesionActual.esCamuflaje
        // en Android 13+ existe un ajuste propio para ocultar la miniatura de recientes
        if (Build.VERSION.SDK_INT >= 33) activity.setRecentsScreenshotEnabled(camuflaje)

        // en versiones anteriores (y como refuerzo) la miniatura se oculta marcando la ventana como segura
        // en el momento en que la app pasa a segundo plano
        val proteger = !camuflaje && (enSegundoPlano || (bloquearCapturas && enPantallaSensible))
        if (proteger) {
            activity.window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }
}
