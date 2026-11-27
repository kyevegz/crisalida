package com.example.crisalida

import com.example.crisalida.data.debeHablar
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RespiracionGuiadaTest {
    @Test
    fun laVozNoHablaEnCamuflajeNiConElInterruptorApagado() {
        assertTrue(debeHablar(vozActiva = true, enCamuflaje = false))
        assertFalse(debeHablar(vozActiva = true, enCamuflaje = true))
        assertFalse(debeHablar(vozActiva = false, enCamuflaje = false))
        assertFalse(debeHablar(vozActiva = false, enCamuflaje = true))
    }
}
