package com.example.attendanceapi.domain.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class Attendance(
    val attendanceId: UUID,
    val employeeId: Int,
    val dateTime: LocalDateTime,
    val context: String,
    val kind: AttendanceKind
) {
    companion object {
        fun create(
            employeeId: Int,
            dateTime: LocalDateTime,
            context: String,
            kind: AttendanceKind
        ): Attendance =
            Attendance(
                UUID.randomUUID(),
                employeeId,
                dateTime,
                context,
                kind
            )
    }

    fun onDate(date: LocalDate): Boolean =
        LocalDate.from(this.dateTime) == date

}

class Attendances(val list: List<Attendance>) {
    fun filterByDate(date: LocalDate): Attendances =
        Attendances(list.filter { it.onDate(date) })

    fun filterByDateAndKind(date: LocalDate, kind: AttendanceKind): Attendance? =
        this.filterByDate(date).list.find{ it -> it.kind == kind}

    fun splitIntoDailyAttendances(): List<Pair<String, List<Attendance>>> =
        list.groupBy { it.dateTime.toLocalDate().toString() }.map { Pair(it.key, it.value) }
}

sealed interface AttendancesFilterByDateError {
    data class NotFound(val input: LocalDate, val message: String) : AttendancesFilterByDateError
}

enum class AttendanceKind {
    START,
    LEAVE,
    BACK,
    END,
    UNKNOWN
}