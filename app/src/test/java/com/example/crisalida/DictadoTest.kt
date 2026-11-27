package com.example.crisalida

import com.example.crisalida.data.DICTADO_SIN_PERMISO
import com.example.crisalida.data.mensajeDeErrorDictado
import com.example.crisalida.data.unirDictado
import org.junit.Assert.*
import org.junit.Test

class DictadoTest {
    @Test
    fun enUnCampoVacioEmpiezaConMayuscula() {
        assertEquals("Necesito un pastel", unirDictado("", "  necesito un pastel "))
    }

    @Test
    fun seAgregaAlFinalConUnEspacio() {
        assertEquals("Quiero un pastel para el sábado", unirDictado("Quiero un pastel", "para el sábado"))
        assertEquals("Quiero un pastel para el sábado", unirDictado("Quiero un pastel   ", "para el sábado"))
    }

    @Test
    fun despuesDeUnPuntoEmpiezaConMayuscula() {
        assertEquals("Hola. Gracias", unirDictado("Hola.", "gracias"))
    }

    @Test
    fun conSaltoDeLineaNoMetePuntoNiEspacioDeMas() {
        assertEquals("Hola\nGracias", unirDictado("Hola\n", "gracias"))
    }

    @Test
    fun unaFraseVaciaNoCambiaNada() {
        assertEquals("Hola", unirDictado("Hola", "   "))
    }

    @Test
    fun respetaElMaximoDeCaracteres() {
        assertEquals(10, unirDictado("Hola amigo", "esto es muy largo", 10).length)
    }

    @Test
    fun loErroresSeExplicanEnEspanol() {
        assertEquals(DICTADO_SIN_PERMISO, mensajeDeErrorDictado(9))
        assertNotNull(mensajeDeErrorDictado(7))   // no entendí
        assertNotNull(mensajeDeErrorDictado(2))   // sin conexión
        assertNotNull(mensajeDeErrorDictado(8))   // ocupado
        assertNotNull(mensajeDeErrorDictado(99))  // cualquier otro
        assertNull(mensajeDeErrorDictado(5))      // cancelación propia: no se muestra
    }
}
