package com.example.buildingfexfrontend.core.util

import java.text.NumberFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * Date / currency helpers used across bounded contexts (formatting only:
 * business rules stay inside each context's domain layer).
 */
object Dates {

    private val localeEsPe = Locale("es", "PE")
    private val ymd: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun todayYmd(): String = LocalDate.now().format(ymd)

    fun nowIso(): String =
        java.time.Instant.now().atZone(ZoneId.systemDefault()).toString()

    fun ymdOrNull(value: String?): LocalDate? = try {
        LocalDate.parse(value?.take(10), ymd)
    } catch (e: Exception) {
        null
    }

    fun addDaysYmd(value: String, days: Long): String =
        (ymdOrNull(value) ?: LocalDate.now()).plusDays(days).format(ymd)

    fun compareYmd(a: String?, b: String?): Int {
        val left = a?.take(10) ?: return -1
        val right = b?.take(10) ?: return 1
        return left.compareTo(right)
    }

    fun isPastDue(dueDate: String?): Boolean {
        val due = ymdOrNull(dueDate) ?: return false
        return !due.isAfter(LocalDate.now())
    }

    /** "Mayo 2026" / "May 2026" from a `yyyy-MM-dd` due date. */
    fun monthYearLabel(
        value: String?,
        language: com.example.buildingfexfrontend.core.i18n.Language =
            com.example.buildingfexfrontend.core.i18n.AppLanguage.current,
    ): String {
        val date = ymdOrNull(value)
            ?: return if (language == com.example.buildingfexfrontend.core.i18n.Language.EN) {
                "Current month"
            } else {
                "Mes actual"
            }
        val locale = if (language == com.example.buildingfexfrontend.core.i18n.Language.EN) {
            Locale.ENGLISH
        } else {
            localeEsPe
        }
        val month = date.month.getDisplayName(TextStyle.FULL, locale)
        return month.replaceFirstChar { it.uppercase(locale) } + " " + date.year
    }

    fun displayDate(value: String?): String {
        val date = ymdOrNull(value) ?: return value.orEmpty()
        val day = date.dayOfMonth.toString().padStart(2, '0')
        val month = date.monthValue.toString().padStart(2, '0')
        return "$day/$month/${date.year}"
    }

    fun displayDateTime(iso: String?): String {
        if (iso.isNullOrBlank()) return ""
        return try {
            val instant = java.time.Instant.parse(iso)
            val zoned = instant.atZone(ZoneId.systemDefault())
            val date = displayDate(zoned.format(ymd))
            val time = zoned.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"))
            "$date $time"
        } catch (e: Exception) {
            displayDate(iso)
        }
    }

    /** Peruvian soles: "S/ 150.00". */
    fun currency(amount: Number?): String {
        val value = amount?.toDouble() ?: 0.0
        val format = NumberFormat.getCurrencyInstance(localeEsPe)
        format.maximumFractionDigits = 2
        format.minimumFractionDigits = 2
        return format.format(value)
    }

    fun localDateIsoString(date: java.util.Date = java.util.Date()): String =
        java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date)
}
