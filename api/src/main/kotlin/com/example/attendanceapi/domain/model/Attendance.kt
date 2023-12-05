package com.example.attendanceapi.domain.model

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class Attendance(
    val attendanceId: UUID,
    val dateTime: LocalDateTime,
    val context: String,
    val kind: AttendanceKind
) {
    companion object {
        fun create(
            dateTime: LocalDateTime,
            context: String,
            kind: AttendanceKind
        ): Attendance =
            Attendance(
                UUID.randomUUID(),
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

class DailyAttendance(val employeeId: String, val date: LocalDate, val attendances: List<Attendance>) {
    companion object {
        fun of(
            employeeId: String,
            date: LocalDate,
            attendances: List<Attendance>
        ): Either<DailyAttendanceError, DailyAttendance> {
            val validKinds = setOf(AttendanceKind.START, AttendanceKind.LEAVE, AttendanceKind.BACK, AttendanceKind.END)
            val filtered = attendances.filter { attendance -> validKinds.contains(attendance.kind) }
            return if (validKinds == filtered.map { it.kind }.toSet()) {
                DailyAttendance(employeeId, date, attendances).right()
            } else {
                DailyAttendanceError(attendances.toString()).left()
            }
        }
    }

    fun start(): Attendance? = this.attendances.find { it.kind == AttendanceKind.START }
    fun end(): Attendance? = this.attendances.find { it.kind == AttendanceKind.END }

    private fun breaks(): List<Attendance> = this.attendances.filter{ it.kind == AttendanceKind.LEAVE || it.kind == AttendanceKind.BACK}

    fun createBreakRecords(): BreakRecords {
        val breaks = this.breaks()
        return if(breaks.isEmpty()){
            BreakRecords.default(employeeId, date)
        }else{
            BreakRecords.default(employeeId, date)
        }
    }

}

data class DailyAttendanceError(val message: String)

class BreakRecords(val employeeId: String, val date: LocalDate, val records: List<BreakRecord>) {

    companion object {
        fun default(employeeId: String, date: LocalDate): BreakRecords {
            val list = listOfNotNull(
                BreakRecord.of(
                    pair = (Attendance.create(
                        LocalDateTime.of(date.year, date.month, date.dayOfMonth, 12, 0, 0),
                        "",
                        AttendanceKind.LEAVE
                    ) to (Attendance.create(
                        LocalDateTime.of(date.year, date.month, date.dayOfMonth, 13, 0, 0),
                        "",
                        AttendanceKind.BACK
                    )))
                )
            )
            return BreakRecords(employeeId, date, list)
        }
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

    fun leave(): Attendance = pair.first
    fun second(): Attendance = pair.second
}

enum class AttendanceKind {
    START,
    LEAVE,
    BACK,
    END,
    UNKNOWN
}