package com.example.crisalida.ui.theme

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

// Paleta única de la app. TODOS los colores de las pantallas salen de aquí (no se escriben a mano en ellas).
// Cada color tiene su valor del modo claro (idéntico al de siempre) y, si hace falta, su valor del modo oscuro.
// Como los getters leen un estado de Compose, al cambiar de modo la pantalla se redibuja sola.
// Reglas de los nombres:
//   - «...Fijo»: no cambia con el modo (acentos, botones y textos que van sobre ellos, por ejemplo letras blancas sobre un botón).
//   - el resto: cambia en modo oscuro conservando el contraste (fondos oscuros, textos claros).
// Cómo se elige el aspecto de la app (Configuración > Apariencia)
enum class ModoApariencia { CLARO, OSCURO, SISTEMA }

object Paleta {
    // lo que eligió la usuaria. Se guarda en el teléfono, no en la base de datos. Por defecto, claro (como siempre)
    var modo by mutableStateOf(ModoApariencia.CLARO)
        private set

    // si el teléfono está en modo oscuro (solo importa cuando se eligió «Automático»)
    private var sistemaOscuro by mutableStateOf(false)

    // la galería de camuflaje se ve igual siempre; mientras está abierta no se oscurecen las barras del sistema
    var enCamuflaje by mutableStateOf(false)

    // el modo que se aplica de verdad ahora mismo
    val modoOscuro: Boolean
        get() = when (modo) {
            ModoApariencia.CLARO -> false
            ModoApariencia.OSCURO -> true
            ModoApariencia.SISTEMA -> sistemaOscuro
        }

    private var cargado = false
    private const val ARCHIVO = "crisalida_ajustes"
    private const val CLAVE = "modo_apariencia"
    private const val CLAVE_ANTERIOR = "modo_oscuro"   // la versión anterior guardaba solo sí/no

    fun cargar(context: Context) {
        val app = context.applicationContext
        // esto se actualiza cada vez que se abre la pantalla principal
        sistemaOscuro = (app.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        if (cargado) return
        cargado = true
        val prefs = app.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE)
        val guardado = prefs.getString(CLAVE, null)
        modo = ModoApariencia.values().firstOrNull { it.name == guardado }
            ?: if (prefs.getBoolean(CLAVE_ANTERIOR, false)) ModoApariencia.OSCURO else ModoApariencia.CLARO
    }

    fun cambiar(context: Context, nuevo: ModoApariencia) {
        modo = nuevo
        context.applicationContext.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE).edit().putString(CLAVE, nuevo.name).apply()
    }

    // lo llama el tema cuando el teléfono cambia de modo mientras la app está abierta
    fun actualizarSistema(oscuro: Boolean) {
        sistemaOscuro = oscuro
    }

    private fun c(claro: Long, oscuro: Long): Color = Color(if (modoOscuro) oscuro else claro)

    val acentoPerfil: Color get() = Color(0xFF8A6D9E)   // igual en los dos modos
    val amarilloFondo: Color get() = c(0xFFFFF3CD, 0xFF2B240D)
    val amarilloTexto: Color get() = c(0xFF856404, 0xFFF5E6B8)
    val avatarSeleccionado: Color get() = Color(0xFF7A1F4A)   // igual en los dos modos
    val barraFondo: Color get() = c(0xFFE8D7DD, 0xFF1F171A)
    val barraProgreso: Color get() = Color(0xFFB82C67)   // igual en los dos modos
    val blanco: Color get() = Color(0xFFFFFFFF)   // igual en los dos modos
    val blancoSuave: Color get() = Color(0xFFFFFAFA)   // igual en los dos modos
    val botonEntrar: Color get() = Color(0xFF9E7E9B)   // igual en los dos modos
    val botonVino: Color get() = Color(0xFF7A2E50)   // igual en los dos modos
    val campo: Color get() = c(0xFFE8D6DC, 0xFF1F1719)
    val chipNormal: Color get() = c(0xFFE0DCDA, 0xFF1C1A19)
    val chipRosa: Color get() = c(0xFFFBD7FF, 0xFF2A0D2D)
    val chipRosaTexto: Color get() = c(0xFF5C3A64, 0xFFD7C7DB)
    val error: Color get() = c(0xFFB3261E, 0xFFE5A4A1)
    val etiquetaAmbarFondo: Color get() = c(0xFFF0D9A6, 0xFF211B0E)
    val etiquetaAmbarTexto: Color get() = c(0xFF5A4311, 0xFFF0E5CE)
    val etiquetaAzulFondo: Color get() = c(0xFFAFD0DD, 0xFF12191C)
    val etiquetaAzulTexto: Color get() = c(0xFF11215A, 0xFFCED5F0)
    val etiquetaMoradaFondo: Color get() = c(0xFFC8AEDD, 0xFF17111C)
    val etiquetaMoradaTexto: Color get() = c(0xFF2B115A, 0xFFDACEF0)
    val etiquetaNaranjaFondo: Color get() = c(0xFFDEBDAE, 0xFF1C1511)
    val etiquetaNaranjaTexto: Color get() = c(0xFF5A2511, 0xFFF0D7CE)
    val etiquetaVerdeFondo: Color get() = c(0xFFBFDDB5, 0xFF151C12)
    val etiquetaVerdeTexto: Color get() = c(0xFF1B3F11, 0xFFE0F2DB)
    val exito: Color get() = c(0xFF2E7D32, 0xFFBBE0BD)
    val exitoFijo: Color get() = Color(0xFF2E7D32)   // igual en los dos modos
    val fondoAlertaRosa: Color get() = c(0xFFF4E4EC, 0xFF24161D)
    val fondoApp: Color get() = c(0xFFF2EFEF, 0xFF141115)
    val fondoAyuda: Color get() = c(0xFFD8CCD3, 0xFF1A1719)
    val fondoBorrador: Color get() = c(0xFFF7F0F3, 0xFF23191E)
    val fondoBotonVolver: Color get() = c(0xFFE2D4DA, 0xFF1D171A)
    val fondoBotonVolverLinea: Color get() = c(0xFFE2D4DA, 0xFF54454B)
    val fondoCajaMascota: Color get() = c(0xFFEFE6EB, 0xFF20191D)
    val fondoCuentaSuspendida: Color get() = c(0xFFF3E3EA, 0xFF23171C)
    val fondoError: Color get() = c(0xFFFBE9E7, 0xFF2A1412)
    val fondoEtiquetaVendedora: Color get() = c(0xFFD3C5CD, 0xFF191618)
    val fondoEtiquetaVendedoraLinea: Color get() = c(0xFFD3C5CD, 0xFF52474D)
    val fondoExito: Color get() = c(0xFFE3F1E6, 0xFF17221A)
    val fondoGrisPerfil: Color get() = c(0xFFC5B8C2, 0xFF161416)
    val fondoIconoCaja: Color get() = c(0xFFE3D3DC, 0xFF1D171B)
    val fondoIntro: Color get() = c(0xFFE6D6DE, 0xFF1E171B)
    val fondoItem: Color get() = c(0xFFEFE8EC, 0xFF201A1D)
    val fondoNeutro: Color get() = c(0xFFCCCCCC, 0xFF3A3539)
    val fondoPausado: Color get() = c(0xFFE2D8DE, 0xFF1D181B)
    val fondoTarjetaPerfil: Color get() = c(0xFFE9E1EA, 0xFF1E191F)
    val grisInicio: Color get() = c(0xFF878686, 0xFFB3B2B2)
    val iconoGris: Color get() = c(0xFF7A7A7A, 0xFFB9B9B9)
    val iconosBarra: Color get() = Color(0xFFFFDEF7)   // igual en los dos modos
    val insignia: Color get() = c(0xFFFFB5D5, 0xFF280C18)
    val lila: Color get() = c(0xFFCBB6CC, 0xFF181418)
    val lilaFijo: Color get() = Color(0xFFCBB6CC)   // igual en los dos modos
    val lilaGris: Color get() = c(0xFFD3C8D6, 0xFF19161A)
    val moradoAcento: Color get() = Color(0xFFA47DAB)   // igual en los dos modos
    val neutroClaro: Color get() = c(0xFFD9D9D9, 0xFF1A1A1A)
    val oscuro: Color get() = c(0xFF3B1F40, 0xFFE8DAEA)
    val oscuroRelleno: Color get() = c(0xFF3B1F40, 0xFF5B3566)
    val pistaBarra: Color get() = c(0xFFE8E1E3, 0xFF1E1A1B)
    val placeholder: Color get() = c(0xFFA68B94, 0xFFB29FA6)
    val placeholderMorado: Color get() = c(0xFF836B86, 0xFFBFB4C0)
    val rosaBarra: Color get() = c(0xFFD0A0B5, 0xFF181014)
    val rosaClaro: Color get() = c(0xFFF1E9ED, 0xFF211A1D)
    val rosaGrisClaro: Color get() = c(0xFFDED2D9, 0xFF1C171A)
    val rosaMuyClaro: Color get() = c(0xFFFAF0F4, 0xFF27161D)
    val rosaTarjeta: Color get() = c(0xFFF1D3DF, 0xFF23141A)
    val superficie: Color get() = c(0xFFFFFFFF, 0xFF1F1A1E)
    val superficieSuave: Color get() = c(0xFFF9F2F4, 0xFF25181C)
    val tarjeta: Color get() = c(0xFFFAF8F8, 0xFF221C1C)
    val tarjetaGris: Color get() = c(0xFFF2F0EF, 0xFF1F1D1C)
    val textoAyuda: Color get() = c(0xFF4A3B43, 0xFFDBD4D8)
    val textoCampo: Color get() = c(0xFF2B2B2B, 0xFFE5E5E5)
    val textoEmergencia: Color get() = Color(0xFFEFD6F3)   // igual en los dos modos
    val textoGris: Color get() = c(0xFF545454, 0xFFCECECE)
    val textoNegro: Color get() = c(0xFF000000, 0xFFF2EDEF)
    val textoNegroGaleria: Color get() = Color(0xFF000000)   // igual en los dos modos
    val textoNeutro: Color get() = c(0xFF333333, 0xFFE0E0E0)
    val textoOscuro: Color get() = c(0xFF1E1B1D, 0xFFEEECED)
    val textoPregunta: Color get() = c(0xFF262626, 0xFFE8E8E8)
    val textoPrincipal: Color get() = c(0xFF6B3045, 0xFFE0C4CE)
    val textoPrincipalFijo: Color get() = Color(0xFF6B3045)   // igual en los dos modos
    val textoSobreMorado: Color get() = Color(0xFFFDF0FF)   // igual en los dos modos
    val textoSuave: Color get() = c(0xFF544247, 0xFFD7CFD1)
    val textoVinoOscuro: Color get() = c(0xFF5A1136, 0xFFF0CEDF)
    val titulo: Color get() = c(0xFF7F284F, 0xFFE3BACC)
    val tituloFijo: Color get() = Color(0xFF7F284F)   // igual en los dos modos
    val tituloRosa: Color get() = c(0xFF9D4067, 0xFFD5AABC)
    val tituloRosaFijo: Color get() = Color(0xFF9D4067)   // igual en los dos modos
    val velo: Color get() = Color(0xAA000000)   // transparencia fija
    val veloFoto: Color get() = Color(0x99000000)   // transparencia fija
    val verdeBarraPerfil: Color get() = c(0xFFB7DDB0, 0xFF131C12)
    val verdeBoton: Color get() = Color(0xFF1E681E)   // igual en los dos modos
    val verdeCeroPerdidaFondo: Color get() = c(0xFFC7EBC3, 0xFF142012)
    val verdeCeroPerdidaTexto: Color get() = c(0xFF16521A, 0xFFD1EED3)
    val verdeChip: Color get() = Color(0xFF007032)   // igual en los dos modos
    val verdeOscuro: Color get() = Color(0xFF005524)   // igual en los dos modos
    val verdePunto: Color get() = Color(0xFFA5E3A1)   // igual en los dos modos
    val verdeSuaveFondo: Color get() = c(0xFFD4EDDA, 0xFF152118)
    val verdeSuaveTexto: Color get() = c(0xFF155724, 0xFFCFEFD6)
    val verdeTextoPunto: Color get() = c(0xFF1B5E20, 0xFFCBEBCE)
    val vino: Color get() = c(0xFF62012A, 0xFFFF8DBB)
    val vinoFijo: Color get() = Color(0xFF62012A)   // igual en los dos modos
}
