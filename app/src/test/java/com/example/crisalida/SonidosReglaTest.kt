package com.example.crisalida

import com.example.crisalida.data.debeSonar
import org.junit.Assert.assertEquals
import org.junit.Test

class SonidosReglaTest {
    @Test
    fun sueNaSoloConAjusteSinCamuflajeYEnModoSonido() {
        for (ajuste in listOf(false, true)) for (camuflaje in listOf(false, true)) for (timbre in listOf(false, true)) {
            val esperado = ajuste && !camuflaje && timbre
            assertEquals("ajuste=$ajuste camuflaje=$camuflaje timbreNormal=$timbre", esperado, debeSonar(ajuste, camuflaje, timbre))
        }
    }

    @Test
    fun enSilencioOVibracionNoSuenaAunqueEsteActivado() {
        assertEquals(false, debeSonar(ajusteActivo = true, enCamuflaje = false, timbreNormal = false))
    }
}
