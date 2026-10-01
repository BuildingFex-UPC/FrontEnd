package com.example.buildingfexfrontend

import com.example.buildingfexfrontend.finances.domain.model.FixedPayoutRecipient
import com.example.buildingfexfrontend.finances.domain.model.PayoutSchedule
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class PayoutScheduleTest {

    private val ymd: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val today: String = LocalDate.now().format(ymd)

    private fun daysAgo(days: Long): String = LocalDate.now().minusDays(days).format(ymd)

    private fun recipient(
        createdAt: String,
        intervalDays: Int = 30,
        salary: Double = 800.0,
    ) = FixedPayoutRecipient(
        id = "fp-1",
        name = "jose jardinero",
        salary = salary,
        intervalDays = intervalDays,
        createdAt = createdAt,
    )

    @Test
    fun `counts the first period on the creation day`() {
        assertEquals(1, PayoutSchedule.periodsSinceCreation(recipient(daysAgo(0)), today))
    }

    @Test
    fun `counts one period per interval elapsed since creation`() {
        assertEquals(2, PayoutSchedule.periodsSinceCreation(recipient(daysAgo(45)), today))
        assertEquals(3, PayoutSchedule.periodsSinceCreation(recipient(daysAgo(60)), today))
        assertEquals(1, PayoutSchedule.periodsSinceCreation(recipient(daysAgo(29)), today))
    }

    @Test
    fun `returns zero when creation date is missing or invalid`() {
        assertEquals(0, PayoutSchedule.periodsSinceCreation(recipient(""), today))
        assertEquals(0, PayoutSchedule.periodsSinceCreation(recipient("not-a-date"), today))
    }

    @Test
    fun `coerces invalid intervals to at least one day`() {
        assertEquals(4, PayoutSchedule.periodsSinceCreation(recipient(daysAgo(3), intervalDays = 0), today))
    }
}
