package com.example.crisalida

import com.example.crisalida.data.debeHablar
import com.example.crisalida.data.patronExhalar
import com.example.crisalida.data.patronInhalar
import org.junit.Assert.*
import org.junit.Test

class RespiracionGuiadaTest {
    @Test
    fun losPatronesDuranExactamenteLoQueLaAnimacion() {
        for (conFuerza in listOf(true, false)) {
            assertEquals(2000L, patronInhalar(2000, conFuerza).tiempos.sum())
            assertEquals(2000L, patronExhalar(2000, conFuerza).tiempos.sum())
        }
    }

    @Test
    fun conControlDeFuerzaHayUnaFuerzaPorTiempo_ySinControlNo() {
        val a = patronInhalar(2000, true)
        assertEquals(a.tiempos.size, a.fuerzas!!.size)
        assertNull(patronInhalar(2000, false).fuerzas)
        val b = patronExhalar(2000, true)
        assertEquals(b.tiempos.size, b.fuerzas!!.size)
        assertNull(patronExhalar(2000, false).fuerzas)
    }

    @Test
    fun inhalarCreceYExhalarSeApaga() {
        val sube = patronInhalar(2000, true).fuerzas!!.filter { it > 0 }
        assertEquals(sube.sorted(), sube)
        assertTrue(sube.first() < sube.last())
        val baja = patronExhalar(2000, true).fuerzas!!.filter { it > 0 }
        assertEquals(baja.sortedDescending(), baja)
        assertTrue(baja.first() > baja.last())
    }

    @Test
    fun sinControlDeFuerzaInhalarAlargaLosPulsos() {
        val t = patronInhalar(2000, false).tiempos
        val encendidos = (1 until t.size step 2).map { t[it] }
        assertEquals(encendidos.sorted(), encendidos)
        assertTrue(encendidos.first() < encendidos.last())
    }

    @Test
    fun ningunTiempoEsNegativo() {
        for (p in listOf(patronInhalar(2000, true), patronInhalar(2000, false), patronExhalar(2000, true), patronExhalar(2000, false))) {
            assertTrue(p.tiempos.all { it >= 0 })
        }
    }

    @Test
    fun laVozNoHablaEnCamuflajeNiConElInterruptorApagado() {
        assertTrue(debeHablar(vozActiva = true, enCamuflaje = false))
        assertFalse(debeHablar(vozActiva = true, enCamuflaje = true))
        assertFalse(debeHablar(vozActiva = false, enCamuflaje = false))
        assertFalse(debeHablar(vozActiva = false, enCamuflaje = true))
    }
}
