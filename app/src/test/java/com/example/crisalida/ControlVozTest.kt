package com.example.crisalida

import com.example.crisalida.data.OrdenVoz
import com.example.crisalida.data.interpretarOrden
import com.example.crisalida.data.normalizarFrase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ControlVozTest {
    private val tabla = listOf(
        "retos" to OrdenVoz.RETOS,
        "RETOS" to OrdenVoz.RETOS,
        "ve a retos" to OrdenVoz.RETOS,
        "quiero ver los retos por favor" to OrdenVoz.RETOS,
        "marketplace" to OrdenVoz.MARKETPLACE,
        "Ir al Marketplace" to OrdenVoz.MARKETPLACE,
        "inicio" to OrdenVoz.INICIO,
        "llévame al inicio" to OrdenVoz.INICIO,
        "bienestar" to OrdenVoz.BIENESTAR,
        "Bienestar diario" to OrdenVoz.BIENESTAR,
        "mascota" to OrdenVoz.MASCOTA,
        "perfil" to OrdenVoz.PERFIL,
        "mi perfil" to OrdenVoz.PERFIL,
        "configuración" to OrdenVoz.CONFIGURACION,
        "CONFIGURACIÓN" to OrdenVoz.CONFIGURACION,
        "configuracion" to OrdenVoz.CONFIGURACION,
        "volver" to OrdenVoz.VOLVER,
        "quiero volver" to OrdenVoz.VOLVER,
        "regresar" to OrdenVoz.VOLVER,
        "leer pantalla" to OrdenVoz.LEER_PANTALLA,
        "lee la pantalla" to OrdenVoz.LEER_PANTALLA,
        "omitir" to OrdenVoz.OMITIR,
        "siguiente" to OrdenVoz.SIGUIENTE,
        "atrás" to OrdenVoz.ATRAS,
        "ATRÁS" to OrdenVoz.ATRAS,
        "respirar" to OrdenVoz.RESPIRAR,
        "respirar con Lilu" to OrdenVoz.RESPIRAR,
        "quiero respirar con lilu" to OrdenVoz.RESPIRAR,
        "lilu respirar" to OrdenVoz.RESPIRAR,
        "borradores" to OrdenVoz.BORRADORES,
        "ir a mis borradores" to OrdenVoz.BORRADORES,
        "publicar producto" to OrdenVoz.PUBLICAR_PRODUCTO,
        "quiero publicar un producto" to OrdenVoz.PUBLICAR_PRODUCTO,
        "subir producto" to OrdenVoz.PUBLICAR_PRODUCTO,
        "Publicar un producto" to OrdenVoz.PUBLICAR_PRODUCTO
    )

    @Test
    fun cadaFraseVaAlDestinoEsperado() {
        for ((frase, esperado) in tabla) assertEquals("«$frase»", esperado, interpretarOrden(frase))
    }

    @Test
    fun lasFrasesQueNoSonOrdenesNoHacenNada() {
        for (frase in listOf("", "hola", "qué hora es", "pizza con queso", "   ")) assertNull("«$frase»", interpretarOrden(frase))
    }

    @Test
    fun nuncaHaceNadaDestructivo() {
        val peligrosas = listOf(
            "salida de emergencia", "salir de emergencia", "borrar mi cuenta", "borra todo e ir a inicio",
            "eliminar cuenta", "publicar", "publicar reto", "validar entrega", "validar", "cambiar pin",
            "ve a perfil y cierra sesión", "cerrar sesión"
        )
        for (frase in peligrosas) assertNull("«$frase»", interpretarOrden(frase))
    }

    @Test
    fun mascotaSolaVaALaMascotaYBorradorNoEsBorrar() {
        assertEquals(OrdenVoz.MASCOTA, interpretarOrden("lilu"))
        assertNull(interpretarOrden("borrar borradores"))
        assertNull(interpretarOrden("publicar producto y borrar cuenta"))
        assertNull(interpretarOrden("validar producto"))
    }

    @Test
    fun siHayVariasPalabrasClaveGanaLaPrimera() {
        assertEquals(OrdenVoz.RETOS, interpretarOrden("retos inicio"))
        assertEquals(OrdenVoz.INICIO, interpretarOrden("inicio retos"))
    }

    @Test
    fun laNormalizacionQuitaAcentosYMayusculas() {
        assertEquals("configuracion", normalizarFrase("Configuración"))
        assertEquals("atras", normalizarFrase("ATRÁS"))
    }
}
