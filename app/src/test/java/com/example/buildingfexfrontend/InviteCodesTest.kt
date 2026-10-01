package com.example.buildingfexfrontend

import com.example.buildingfexfrontend.core.util.InviteCodes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InviteCodesTest {

    @Test
    fun extract_bareCode() {
        assertEquals("304", InviteCodes.extract("304"))
        assertEquals("304", InviteCodes.extract("  304  "))
        assertEquals("304-k7m2", InviteCodes.extract("304-k7m2"))
    }

    @Test
    fun extract_labeledCode() {
        assertEquals("304", InviteCodes.extract("Código de invitación: 304"))
        assertEquals("304", InviteCodes.extract("codigo: 304"))
        assertEquals("402B", InviteCodes.extract("Código #402B"))
        assertEquals("304-k7m2", InviteCodes.extract("Código de invitación: 304-k7m2"))
    }

    @Test
    fun extract_fullSharedMessage() {
        val message = "BuildingFex – Invitación para Juan Pérez (Piso 3, Dept. 304).\n\n" +
            "Código de invitación: 304\n\n" +
            "Instala la app BuildingFex y, en la pantalla de acceso, toca " +
            "\"¿Tienes un código de invitación? Actívalo\" para crear tu contraseña."
        assertEquals("304", InviteCodes.extract(message))
    }

    @Test
    fun extract_fullSharedMessageWithSuffixedCode() {
        val message = "BuildingFex – Invitación para Juan Pérez (Piso 3, Dept. 304).\n\n" +
            "Código de invitación: 304-k7m2\n\n" +
            "Instala la app BuildingFex y, en la pantalla de acceso, toca " +
            "\"¿Tienes un código de invitación? Actívalo\" para crear tu contraseña."
        assertEquals("304-k7m2", InviteCodes.extract(message))
    }

    @Test
    fun extract_departmentToken() {
        assertEquals("402", InviteCodes.extract("Piso 4, Dept. 402"))
        assertEquals("304", InviteCodes.extract("departamento 304 del edificio"))
    }

    @Test
    fun extract_numericFallback() {
        assertEquals("304", InviteCodes.extract("este es el 304 que pediste"))
    }

    @Test
    fun extract_garbageReturnsNull() {
        assertNull(InviteCodes.extract(""))
        assertNull(InviteCodes.extract("   "))
        assertNull(InviteCodes.extract("hola mundo"))
    }

    @Test
    fun extract_singleTokenKeptAsIs() {
        assertEquals("ABC-123", InviteCodes.extract("ABC-123"))
    }

    @Test
    fun looksLikeMessageDetection() {
        assertFalse(InviteCodes.looksLikeMessage("304"))
        assertFalse(InviteCodes.looksLikeMessage("ABC-123"))
        assertTrue(InviteCodes.looksLikeMessage("Código de invitación: 304"))
        assertTrue(InviteCodes.looksLikeMessage("304\nCódigo de invitación: 304"))
    }

    @Test
    fun autoSearchOnlyForPlausibleCodes() {
        assertTrue(InviteCodes.canAutoSearch("304"))
        assertTrue(InviteCodes.canAutoSearch("123456"))
        assertTrue(InviteCodes.canAutoSearch("304-k7m2"))
        assertFalse(InviteCodes.canAutoSearch("30"))
        assertFalse(InviteCodes.canAutoSearch("3045678"))
        assertFalse(InviteCodes.canAutoSearch("304-k"))
        assertFalse(InviteCodes.canAutoSearch(""))
        assertFalse(InviteCodes.canAutoSearch("abc"))
    }
}
