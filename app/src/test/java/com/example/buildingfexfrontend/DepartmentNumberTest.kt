package com.example.buildingfexfrontend

import com.example.buildingfexfrontend.residents.domain.DepartmentNumber
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DepartmentNumberTest {

    @Test
    fun parse_derivesFloorFromDepartment() {
        assertEquals("304" to "3", DepartmentNumber.parse("304"))
        assertEquals("1204" to "12", DepartmentNumber.parse("1204"))
        assertNull(DepartmentNumber.parse("24"))
        assertNull(DepartmentNumber.parse("abc"))
    }

    @Test
    fun departmentOf_ignoresRandomSuffix() {
        assertEquals("304", DepartmentNumber.departmentOf("304-k7m2"))
        assertEquals("304", DepartmentNumber.departmentOf("304"))
        assertEquals("", DepartmentNumber.departmentOf(null))
        assertEquals("", DepartmentNumber.departmentOf("  "))
    }

    @Test
    fun randomSuffix_hasFourUnambiguousChars() {
        val suffix = DepartmentNumber.randomSuffix()
        assertEquals(4, suffix.length)
        assertTrue(suffix.all { it.isLetterOrDigit() })
        assertTrue(suffix.none { it in "0O1Il" })
    }
}
