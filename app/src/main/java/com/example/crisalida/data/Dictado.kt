package com.example.crisalida.data

/*
 * Reglas del dictado por voz (issue 10.3) sin nada de Android, para probarlas con pruebas unitarias.
 */

const val DICTADO_SIN_PERMISO = "No tenemos permiso para usar el micrófono. Puedes seguir escribiendo con el teclado o activar el permiso en los ajustes del teléfono."
const val DICTADO_SIN_SERVICIO = "Tu teléfono no tiene un servicio de dictado por voz. Puedes seguir escribiendo con el teclado."

/**
 * Agrega la frase dictada AL FINAL de lo que ya está escrito: con un espacio de por medio, con mayúscula al empezar
 * (o después de un punto) y sin pasarse de [maximo] caracteres.
 */
fun unirDictado(actual: String, dictado: String, maximo: Int = Int.MAX_VALUE): String {
    val frase = dictado.trim().replace(Regex("\\s+"), " ")
    if (frase.isEmpty()) return actual
    val base = actual.trimEnd(' ')
    val empiezaOracion = base.isEmpty() || base.last() in ".!?\n"
    val nueva = if (empiezaOracion) frase.replaceFirstChar { it.uppercase() } else frase
    val union = when {
        base.isEmpty() -> nueva
        base.endsWith("\n") -> base + nueva
        else -> "$base $nueva"
    }
    return union.take(maximo)
}

/**
 * Mensaje en español claro para cada código de error de SpeechRecognizer. Devuelve null cuando el error no debe
 * mostrarse (el 5 es la cancelación que hace la propia app al salir de la pantalla).
 */
fun mensajeDeErrorDictado(codigo: Int): String? = when (codigo) {
    5 -> null
    9 -> DICTADO_SIN_PERMISO
    6, 7 -> "No te entendí. Inténtalo de nuevo, hablando cerca del teléfono."
    1, 2, 4 -> "No se pudo dictar sin conexión a internet. Conéctate o sigue escribiendo con el teclado."
    3 -> "No pudimos usar el micrófono. Revisa que otra app no lo esté usando."
    8 -> "El dictado está ocupado. Espera un momento e inténtalo de nuevo."
    12, 13 -> "Tu teléfono no tiene dictado en español. Puedes seguir escribiendo con el teclado."
    else -> "No se pudo dictar. Puedes seguir escribiendo con el teclado."
}
