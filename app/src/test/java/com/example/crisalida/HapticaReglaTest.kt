package com.example.crisalida

import com.example.crisalida.data.debeVibrar
import org.junit.Assert.assertEquals
import org.junit.Test

class HapticaReglaTest {
    @Test
    fun vibraSoloConAjusteSinCamuflajeYConMotor() {
        for (ajuste in listOf(false, true)) for (camuflaje in listOf(false, true)) for (motor in listOf(false, true)) {
            val esperado = ajuste && !camuflaje && motor
            assertEquals("ajuste=$ajuste camuflaje=$camuflaje motor=$motor", esperado, debeVibrar(ajuste, camuflaje, motor))
        }
    }

    @Test
    fun unaSolaCombinacionVibra() {
        assertEquals(true, debeVibrar(ajusteActivo = true, enCamuflaje = false, tieneMotor = true))
        assertEquals(false, debeVibrar(ajusteActivo = true, enCamuflaje = true, tieneMotor = true))
        assertEquals(false, debeVibrar(ajusteActivo = true, enCamuflaje = false, tieneMotor = false))
        assertEquals(false, debeVibrar(ajusteActivo = false, enCamuflaje = false, tieneMotor = true))
    }
}
