package com.example.attendanceapi.domain.model

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.right
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
    fun createBreakRecords(): BreakRecords =
        this.attendances.asSequence().filter { listOf(AttendanceKind.LEAVE, AttendanceKind.BACK).contains(it.kind) }
            .sortedWith(compareBy { it.dateTime })
            .chunked(2) { it[0] to it[1] }
            .map { BreakRecord.of(it) }.filterNotNull().toList().let { BreakRecords(it) }
}

class BreakRecords(val breakRecords: List<BreakRecord>){
    fun empty(): BreakRecords = BreakRecords(emptyList())
}

class BreakRecord private constructor(val pair: Pair<Attendance, Attendance>) {
    companion object {
        fun of(pair: Pair<Attendance, Attendance>): BreakRecord? {
            return if (pair.first.kind == AttendanceKind.LEAVE || pair.second.kind == AttendanceKind.BACK) {
                BreakRecord(pair)
            } else {
                null
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
