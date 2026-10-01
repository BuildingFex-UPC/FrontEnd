package com.example.buildingfexfrontend.socialspaces.domain.model

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/** Validation and scheduling rules shared by reservations (ported from the web utils). */
object ReservationRules {

    const val MAX_GUESTS = 5

    /** Minutes after the reservation start when the guest invite stops working. */
    const val INVITE_EXPIRES_MINUTES_AFTER_START = 25

    /** Returns minutes after midnight, or null when the value is not `HH:mm`. */
    fun timeToMinutes(value: String?): Int? {
        val parts = value?.split(":") ?: return null
        if (parts.size != 2) return null
        val h = parts[0].toIntOrNull() ?: return null
        val m = parts[1].toIntOrNull() ?: return null
        if (h !in 0..23 || m !in 0..59) return null
        return h * 60 + m
    }

    fun timeRangeValid(startTime: String, endTime: String): Boolean {
        val start = timeToMinutes(startTime) ?: return false
        val end = timeToMinutes(endTime) ?: return false
        return start < end
    }

    fun overlaps(a: Reservation, b: Reservation): Boolean {
        if (a.date != b.date) return false
        val aStart = timeToMinutes(a.startTime) ?: return false
        val aEnd = timeToMinutes(a.endTime) ?: return false
        val bStart = timeToMinutes(b.startTime) ?: return false
        val bEnd = timeToMinutes(b.endTime) ?: return false
        return aStart < bEnd && bStart < aEnd
    }

    /** Expiry instant (epoch millis) of the guest invite, or null on invalid input. */
    fun inviteExpiresAtMs(date: String?, startTime: String?): Long? {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        val parsed = runCatching {
            LocalDateTime.parse("${date.orEmpty()} ${startTime.orEmpty()}", formatter)
        }.getOrNull() ?: return null
        return parsed.plus(INVITE_EXPIRES_MINUTES_AFTER_START.toLong(), ChronoUnit.MINUTES)
            .atZone(java.time.ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    fun isInviteExpired(date: String?, startTime: String?, nowMs: Long = System.currentTimeMillis()): Boolean {
        val expiry = inviteExpiresAtMs(date, startTime) ?: return true
        return nowMs >= expiry
    }
}
