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
            assertEquals(4000L, patronInhalar(4000, conFuerza).tiempos.sum())
            assertEquals(6000L, patronExhalar(6000, conFuerza).tiempos.sum())
        }
    }

    @Test
    fun conControlDeFuerzaHayUnaFuerzaPorTiempo_ySinControlNo() {
        val a = patronInhalar(4000, true)
        assertEquals(a.tiempos.size, a.fuerzas!!.size)
        assertNull(patronInhalar(4000, false).fuerzas)
        val b = patronExhalar(6000, true)
        assertEquals(b.tiempos.size, b.fuerzas!!.size)
        assertNull(patronExhalar(6000, false).fuerzas)
    }

    @Test
    fun inhalarCreceYExhalarSeApaga() {
        val sube = patronInhalar(4000, true).fuerzas!!.filter { it > 0 }
        assertEquals(sube.sorted(), sube)
        assertTrue(sube.first() < sube.last())
        val baja = patronExhalar(6000, true).fuerzas!!.filter { it > 0 }
        assertEquals(baja.sortedDescending(), baja)
        assertTrue(baja.first() > baja.last())
    }

    @Test
    fun sinControlDeFuerzaInhalarAlargaLosPulsos() {
        val t = patronInhalar(4000, false).tiempos
        val encendidos = (1 until t.size step 2).map { t[it] }
        assertEquals(encendidos.sorted(), encendidos)
        assertTrue(encendidos.first() < encendidos.last())
    }

    @Test
    fun sinControlDeFuerzaExhalarSeApagaConElTiempo() {
        val t = patronExhalar(6000, false).tiempos
        // suma de lo que está encendido en la primera mitad vs la segunda (los tiempos en posición impar vibran)
        var acumulado = 0L
        var primera = 0L
        var segunda = 0L
        for (i in 1 until t.size) {
            if (i % 2 == 1) { if (acumulado < 3000) primera += t[i] else segunda += t[i] }
            acumulado += t[i]
        }
        assertTrue(primera > segunda)
    }

    @Test
    fun inhalarTienePulsosSeparadosYExhalarEsUnaSolaVibracionLarga() {
        val pulsos = (1 until patronInhalar(4000, true).tiempos.size step 2).count()
        assertTrue(pulsos in 4..8)
        val b = patronExhalar(6000, true).tiempos
        assertTrue(b.drop(1).all { it > 0 })   // sin pausas: siempre vibrando
    }

    @Test
    fun ningunTiempoEsNegativo() {
        for (p in listOf(patronInhalar(4000, true), patronInhalar(4000, false), patronExhalar(6000, true), patronExhalar(6000, false))) {
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
