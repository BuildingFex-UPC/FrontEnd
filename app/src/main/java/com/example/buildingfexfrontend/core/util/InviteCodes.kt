package com.example.buildingfexfrontend.core.util

/**
 * Invitation codes are "304-k7m2" (department + random suffix; legacy codes
 * are plain departments like "304"), but admins share a whole message. These
 * helpers pull the real code out of whatever the resident pastes.
 */
object InviteCodes {

    private val LABELED = Regex(
        "(?i)c[óo]digo(?:\\s+de\\s+invitaci[óo]n)?\\s*[:#-]?\\s*([A-Za-z0-9][A-Za-z0-9\\-]{0,19})",
    )
    private val DEPARTMENT = Regex(
        "(?i)(?:dept(?:o|artmento)?|departamento)\\.?\\s*([A-Za-z0-9][A-Za-z0-9\\-]{0,19})",
    )
    private val DIGITS = Regex("\\d{3,6}")
    private val AUTO = Regex("\\d{3,6}(?:-[A-Za-z0-9]{2,8})?")

    /** True when the pasted value looks like a shared message rather than a code. */
    fun looksLikeMessage(text: String): Boolean =
        text.length > 12 ||
            text.contains('\n') ||
            text.contains('\r') ||
            LABELED.containsMatchIn(text) ||
            DEPARTMENT.containsMatchIn(text)

    /** Returns the code found in [raw], or null when nothing code-like is present. */
    fun extract(raw: String): String? {
        val text = raw.trim()
        if (text.isEmpty()) return null
        LABELED.find(text)?.let { return it.groupValues[1] }
        DEPARTMENT.find(text)?.let { return it.groupValues[1] }
        if (text.none { it.isWhitespace() }) return text
        DIGITS.find(text)?.let { return it.value }
        return null
    }

    /** Codes worth searching automatically while the user types. */
    fun canAutoSearch(code: String): Boolean = AUTO.matches(code)
}
