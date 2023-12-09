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

    private fun breaks(): Attendances =
        this.attendances.filter { it.kind == AttendanceKind.LEAVE || it.kind == AttendanceKind.BACK }
            .let { Attendances(it) }

    fun createBreakRecords(): BreakRecords {
        val breaks = this.breaks()
        return if (breaks.list.isEmpty()) {
            BreakRecords.default(employeeId, date)
        } else {
            val leaveRecords = breaks.list.filter { it.kind == AttendanceKind.LEAVE }.sortedBy { it.dateTime }
            val backRecords = breaks.list.filter { it.kind == AttendanceKind.BACK }.sortedBy { it.dateTime }
            if(leaveRecords.count() == backRecords.count()){
                BreakRecords(
                    this.employeeId,
                    this.date,
                    leaveRecords.mapIndexed { idx, record ->
                        BreakRecord.of(
                            pair = (record to backRecords[idx])
                        )
                    }
                )
            }else if(leaveRecords.count() > backRecords.count()){
                BreakRecords(
                    this.employeeId,
                    this.date,
                    leaveRecords.mapIndexed { idx, record ->
                        val back = backRecords.getOrNull(idx) ?: Attendance.create(record.dateTime.plusHours(1), "add 1hour to leave time", AttendanceKind.BACK)
                        BreakRecord.of(
                            pair = (record to back)
                        )
                    }
                )
            }else{
                BreakRecords(
                    this.employeeId,
                    this.date,
                    backRecords.mapIndexed { idx, record ->
                        val leave = leaveRecords.getOrNull(idx) ?: Attendance.create(record.dateTime.minusHours(1), "minus 1hour to leave time", AttendanceKind.LEAVE)
                        BreakRecord.of(
                            pair = (record to leave)
                        )
                    }
                )
            }
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
        fun of(pair: Pair<Attendance, Attendance>): BreakRecord {
            return if (pair.first.kind == AttendanceKind.LEAVE || pair.second.kind == AttendanceKind.BACK) {
                BreakRecord(pair)
            } else {
                val date = pair.first.dateTime
                of(
                    Attendance.create(
                        LocalDateTime.of(date.year, date.month, date.dayOfMonth, 12, 0, 0),
                        "defalt leave time",
                        AttendanceKind.LEAVE
                    ) to Attendance.create(
                        LocalDateTime.of(date.year, date.month, date.dayOfMonth, 13, 0, 0),
                        "defalt back time",
                        AttendanceKind.BACK
                    )

                )
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