package com.example.buildingfexfrontend.residents.domain

/**
 * Department number encodes floor + unit: last 2 digits = unit,
 * leading digits = floor. Examples: 304 -> floor 3 unit 04, 1204 -> floor 12.
 */
object DepartmentNumber {

    private val pattern = Regex("^\\d{3,4}$")

    fun normalize(value: String?): String? {
        val digits = value?.filter { it.isDigit() }.orEmpty()
        return digits.ifBlank { null }
    }

    fun isValid(value: String?): Boolean = parse(value) != null

    /** @returns `department` + derived `floor`, or null when malformed. */
    fun parse(value: String?): Pair<String, String>? {
        val digits = normalize(value) ?: return null
        if (!pattern.matches(digits)) return null
        val floorPart = digits.dropLast(2).toIntOrNull() ?: return null
        if (floorPart <= 0) return null
        return digits to floorPart.toString()
    }

    /**
     * Department part of an invite code: "304-K7M2" -> "304",
     * legacy plain codes ("304") -> "304".
     */
    fun departmentOf(code: String?): String = code?.substringBefore('-')?.trim().orEmpty()

    /**
     * Random suffix appended to the department so the invite code stays unique
     * even when the same department number exists in another building.
     * Ambiguous characters (0/O, 1/I/l) are excluded.
     */
    fun randomSuffix(): String {
        val alphabet = "23456789abcdefghjkmnpqrstuvwxyz"
        return buildString(4) { repeat(4) { append(alphabet.random()) } }
    }
}
