package com.example.attendanceapi.domain.model

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import javax.accessibility.AccessibleText

class Attendance(
    val attendanceId: UUID,
    val employeeId: String, // TODO: DailyAttendanceに移譲する
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

    fun filterByKind(kinds: List<AttendanceKind>): List<Attendance> =
        this.list.filter { attendance -> kinds.contains(attendance.kind) }

    fun filterByDateAndKind(date: LocalDate, kind: AttendanceKind): Attendance? =
        this.filterByDate(date).list.find { it -> it.kind == kind }

    fun splitIntoDailyAttendances(): List<Pair<String, List<Attendance>>> =
        list.groupBy { it.dateTime.toLocalDate().toString() }.map { Pair(it.key, it.value) }
}

class DailyAttendance(val date: LocalDate, val attendances: List<Attendance>) {
    companion object {
        fun of(date: LocalDate, attendances: List<Attendance>): Either<DailyAttendanceError, DailyAttendance> {
            val validKinds = setOf(AttendanceKind.START, AttendanceKind.LEAVE, AttendanceKind.BACK, AttendanceKind.END)
            val filtered = attendances.filter { attendance -> validKinds.contains(attendance.kind) }
            return if (validKinds == filtered.map { it.kind }.toSet()) {
                DailyAttendance(date, attendances).right()
            } else {
                DailyAttendanceError(attendances.toString()).left()
            }
        }
    }

    fun createBreakRecords(): BreakRecords =
        this.attendances.asSequence().filter { listOf(AttendanceKind.LEAVE, AttendanceKind.BACK).contains(it.kind) }
            .sortedWith(compareBy { it.dateTime })
            .chunked(2) { it[0] to it[1] }
            .map { BreakRecord.of(it) }.filterNotNull().toList().let { BreakRecords("", date, it) }

}

data class DailyAttendanceError(val message: String)

class BreakRecords(val employeeId: String, val date: LocalDate, val breakRecords: List<BreakRecord>) {

    fun default(employeeId: String, date: LocalDate): BreakRecords {
        val list = listOfNotNull(
            BreakRecord.of(
                pair = (Attendance.create(
                    employeeId,
                    LocalDateTime.of(date.year, date.month, date.dayOfMonth, 12, 0, 0),
                    "",
                    AttendanceKind.LEAVE
                ) to (Attendance.create(
                    employeeId,
                    LocalDateTime.of(date.year, date.month, date.dayOfMonth, 13, 0, 0),
                    "",
                    AttendanceKind.LEAVE
                )))
            )
        )
        return BreakRecords(employeeId, date, list)
    }
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

enum class AttendanceKind {
    START,
    LEAVE,
    BACK,
    END,
    UNKNOWN
}