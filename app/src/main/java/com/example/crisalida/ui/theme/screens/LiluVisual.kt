package com.example.crisalida.ui.theme.screens

import androidx.annotation.DrawableRes
import com.example.crisalida.R

// Cómo se ve Lilu y qué se le dice a la usuaria, en un solo lugar para que Inicio, Mascota y Respirar
// muestren siempre lo mismo. Si Lilu está en reposo se muestra dormida; su etapa, sus flores y el nivel
// de la usuaria no cambian por estar en reposo.

@DrawableRes
fun imagenLilu(etapa: Int?, reposo: Boolean): Int =
    if (reposo) R.drawable.lilu_reposo
    else when (etapa) {
        2 -> R.drawable.lilu_larva
        3 -> R.drawable.lilu_mariposa
        else -> R.drawable.lilu_huevo
    }

fun descripcionImagenLilu(reposo: Boolean): String = if (reposo) "Lilu durmiendo" else "Lilu"

// Los mensajes de reposo nunca reprochan la ausencia: no hablan de "tiempo sin entrar", no piden
// ponerse al corriente y recuerdan que nada se pierde.
fun tituloLilu(etapaNombre: String?, reposo: Boolean): String = when {
    reposo -> "Lilu • Descansando"
    etapaNombre != null -> "Lilu • $etapaNombre"
    else -> "Lilu"
}

fun mensajeLilu(etapa: Int?, reposo: Boolean): String =
    if (reposo) {
        "Lilu está descansando, calentita y tranquila. Tus flores, tu nivel y todo lo que has logrado " +
                "siguen aquí, intactos. Cuando tú quieras, un pequeño paso la despierta."
    } else when (etapa) {
        1 -> "Lilu descansa en su huevo, esperando crecer contigo"
        2 -> "Lilu ya salió y crece con cada paso tuyo"
        3 -> "¡Lilu ya tiene alas! Sigue floreciendo con ella"
        else -> "Cargando a Lilu…"
    }

fun saludoRespirarLilu(reposo: Boolean): String =
    if (reposo) "Lilu está descansando a tu lado. Respira con ella todo el tiempo que necesites, sin prisa."
    else "Lilu está muy feliz de que estés aquí. Quiere recordarte lo valiosa que eres para el mundo."