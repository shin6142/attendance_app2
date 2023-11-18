package com.example.attendanceapi.domain.model

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.example.attendanceapi.gateway.api.BreakRecord
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class Attendance(
    val attendanceId: UUID,
    val employeeId: String,
    val dateTime: LocalDateTime,
    val context: String,
    val kind: AttendanceKind
) {
    companion object {
        fun create(
            employeeId: String,
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
    private fun filterByDate(date: LocalDate): Attendances =
        Attendances(list.filter { it.onDate(date) })

    fun filterByDateAndKind(date: LocalDate, kind: AttendanceKind): Attendance? =
        this.filterByDate(date).list.find { it -> it.kind == kind }

    fun splitIntoDailyAttendances(): List<Pair<String, List<Attendance>>> =
        list.groupBy { it.dateTime.toLocalDate().toString() }.map { Pair(it.key, it.value) }
}

class DailyAttendance(val date: LocalDate, val attendances: List<Attendance>) {


    fun createBreakCords(): BreakRecords? {
        val breaks = this.attendances.filter { it.kind == AttendanceKind.LEAVE || it.kind == AttendanceKind.END }
        breaks.sortedWith(compareBy { it.dateTime })
    }
}

class BreakRecords(val breakRecords: List<com.example.attendanceapi.gateway.api.BreakRecord>)

class BreakRecord private constructor(pair: Pair<Attendance, Attendance>) {
    companion object {
        fun of(pair: Pair<Attendance, Attendance>) {
            if (pair.first.kind == AttendanceKind.LEAVE || pair.second.kind == AttendanceKind.BACK) {
                BreakRecord(pair).right()
            } else {
                BreakRecordError("").left()
            }
        }
    }
}

data class BreakRecordError(val message: String)

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
