package com.example.attendanceapi.usecase

import arrow.core.*
import com.example.attendanceapi.domain.gateway.api.AttendanceGateway
import com.example.attendanceapi.domain.model.Attendance
import com.example.attendanceapi.domain.model.AttendanceKind
import com.example.attendanceapi.domain.model.AttendanceKind.*
import com.example.attendanceapi.domain.model.DailyAttendance
import org.springframework.stereotype.Component
import java.time.*
import java.time.format.DateTimeFormatter

@Component
class AttendanceUseCase(val attendanceGateway: AttendanceGateway) {
    fun getMessages(input: AttendancesInput): Either<GetMonthlyByEmployeeIdError, AttendancesOutput> =
        attendanceGateway.retrieveAttendances(input.employeeId, input.year, input.month)
            .mapLeft { GetMonthlyByEmployeeIdError(input, "") }
            .flatMap { dailyAttendances ->
                dailyAttendances.map { dailyAttendance ->
                    // TODO: kind分ループする
                    dailyAttendance.toDailyAttendanceOutPut()
                }.let { AttendancesOutput(it) }.right()
            }

//    fun getMonthlyByEmployeeId(input: AttendancesInput): Either<GetMonthlyByEmployeeIdError, AttendancesOutput> =
//        attendanceGateway.retrieveAttendances(input.employeeId, input.year, input.month)
//            .mapLeft { GetMonthlyByEmployeeIdError(input, "") }
//            .flatMap { attendances ->
//                val weekDays = getWeekdaysInMonth(input.year.toInt(), input.month.toInt())
//                // TODO: 2回以上のLEAVE, BACKに対応する。
//                filterJapaneseHoliday(weekDays, input.year.toInt()).filterUntilToday().map { day ->
//                    day to listOf(START, LEAVE, BACK, END)
//                }.map { pair ->
//                    pair.second.map { kind ->
//                        val targetAttendance = attendances.filterByDateAndKind(pair.first, kind)
//                        AttendanceOutput(
//                            input.employeeId,
//                            "従業員名",
//                            targetAttendance?.dateTime?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
//                                ?: when (kind) {
//                                    LEAVE -> "${pair.first} 12:00:00"
//                                    START -> ""
//                                    BACK -> "${pair.first} 13:00:00"
//                                    END -> ""
//                                    UNKNOWN -> ""
//                                },
//                            targetAttendance?.context ?: "",
//                            translateKind(kind)
//                        )
//                    }.let {
//                        DailyAttendanceOutPut(pair.first.toString(), it)
//                    }
//
//                }.let { AttendancesOutput(it) }.right()
//            }

    fun recordAttendances(input: RecordAttendancesInput): Either<RecordAttendancesError, RecordAttendancesOutput> =
        input.list.map { dailyAttendanceInput ->
            dailyAttendanceInput.toDailyAttendance().getOrElse {
                return RecordAttendancesError("RecordAttendancesError: toDailyAttendance: ${it.message}").left()
            }
        }.let {
            attendanceGateway.recordAttendances(
                input.token,
                input.companyId.toInt(),
                input.employeeId.toInt(),
                it
            )
        }.mapLeft { error ->
            RecordAttendancesError(error.toString())
        }.flatMap {
            RecordAttendancesOutput(input.employeeId, input.list).right()
        }


    private fun DailyAttendanceInput.toDailyAttendance(): Either<ToDailyAttendanceError, DailyAttendance> {
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val date = LocalDate.parse(this.date, dateFormatter)
        return DailyAttendance.of(
            employeeId = this.employeeId,
            date = date,
            attendances = this.list.map { attendanceInput ->
                attendanceInput.toAttendance(date)
            }
        ).mapLeft {
            ToDailyAttendanceError(it.message)
        }
    }

    private fun AttendanceInput.toAttendance(date: LocalDate): Attendance {
        val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return try {
            Attendance.create(
                LocalDateTime.parse(this.dateTime, dateTimeFormatter),
                this.context,
                when (this.kind) {
                    "START" -> START
                    "LEAVE" -> LEAVE
                    "BACK" -> BACK
                    "END" -> END
                    "UNKNOWN" -> UNKNOWN
                    else -> {
                        UNKNOWN
                    }
                }
            )
        } catch (e: Exception) {
            val defacultTimeStamp = when (this.kind) {
                "START" -> LocalDateTime.of(date, LocalTime.of(9, 0, 0))
                "LEAVE" -> LocalDateTime.of(date, LocalTime.of(12, 0, 0))
                "BACK" -> LocalDateTime.of(date, LocalTime.of(13, 0, 0))
                "END" -> LocalDateTime.of(date, LocalTime.of(18, 0, 0))
                "UNKNOWN" -> LocalDateTime.of(date, LocalTime.of(0, 0, 0))
                else -> {
                    LocalDateTime.of(LocalDate.now(), LocalTime.of(0, 0, 0))
                }
            }

            return Attendance.create(
                defacultTimeStamp,
                this.context,
                when (this.kind) {
                    "START" -> START
                    "LEAVE" -> LEAVE
                    "BACK" -> BACK
                    "END" -> END
                    "UNKNOWN" -> UNKNOWN
                    else -> {
                        UNKNOWN
                    }
                }
            )
        }
    }

    private fun DailyAttendance.toDailyAttendanceOutPut(): DailyAttendanceOutPut {
        val attendances = this.createBreakRecords().records.flatMap { it.pair.toList() }.map {
            it.toAttendanceOutput()
        }.toMutableList()
        attendances.add(0, this.start()?.toAttendanceOutput() ?: AttendanceOutput("", "", "", "", "START"))
        attendances.add(this.end()?.toAttendanceOutput() ?: AttendanceOutput("", "", "", "", "END"))

        return DailyAttendanceOutPut(
            date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
            attendances
        )
    }

    private fun Attendance.toAttendanceOutput(): AttendanceOutput =
        AttendanceOutput(
            "",
            "",
            this.dateTime.toString(),
            this.context,
            this.kind.toString()
        )


    data class AttendancesInput(val employeeId: String, val year: String, val month: String)

    data class AttendancesOutput(val list: List<DailyAttendanceOutPut>)

    data class DailyAttendanceOutPut(val date: String, val attendance: List<AttendanceOutput>)
    data class RecordAttendancesInput(
        val token: String,
        val companyId: String,
        val employeeId: String,
        val list: List<DailyAttendanceInput>
    )

    data class RecordAttendancesOutput(
        val employeeId: String,
        val list: List<DailyAttendanceInput>
    )

    data class DailyAttendanceInput(
        val employeeId: String,
        val date: String,
        val list: List<AttendanceInput>
    )

    data class AttendanceInput(
        val employeeId: String,
        val dateTime: String,
        val context: String,
        val kind: String
    )

    data class AttendanceOutput(
        val employeeId: String,
        val employeeName: String,
        val datetime: String,
        val context: String,
        val kind: String
    )

    private fun translateKind(kind: AttendanceKind): String =
        when (kind) {
            START -> "START"
            LEAVE -> "LEAVE"
            BACK -> "BACK"
            END -> "END"
            UNKNOWN -> "UNKNOWN"
        }
}

interface UseCaseError
data class GetMonthlyByEmployeeIdError(val input: AttendanceUseCase.AttendancesInput, val message: String) :
    UseCaseError

data class ToDailyAttendanceError(val message: String) : UseCaseError

data class RecordAttendancesError(val message: String) : UseCaseError

fun getWeekdaysInMonth(year: Int, monthInt: Int): List<LocalDate> {
    val month = Month.of(monthInt)
    val firstDay = LocalDate.of(year, month, 1)
    val lastDay = firstDay.plusMonths(1).minusDays(1)
    val weekdaysInMonth = mutableListOf<LocalDate>()

    var currentDate = firstDay
    while (!currentDate.isAfter(lastDay)) {
        if (currentDate.dayOfWeek in DayOfWeek.MONDAY..DayOfWeek.FRIDAY) {
            weekdaysInMonth.add(currentDate)
        }
        currentDate = currentDate.plusDays(1)
    }

    return weekdaysInMonth
}

fun getJapaneseHoliday(year: Int): List<LocalDate> {
    return listOf(
        LocalDate.of(year, 1, 1),
        LocalDate.of(year, 1, 2),
        LocalDate.of(year, 2, 11),
        LocalDate.of(year, 2, 23),
        LocalDate.of(year, 4, 29),
        LocalDate.of(year, 5, 3),
        LocalDate.of(year, 5, 4),
        LocalDate.of(year, 5, 5),
        LocalDate.of(year, 7, 3),
        LocalDate.of(year, 8, 11),
        LocalDate.of(year, 9, 3),
        LocalDate.of(year, 10, 2),
        LocalDate.of(year, 11, 3),
        LocalDate.of(year, 11, 23),
        LocalDate.of(year, 12, 23),
    )
}

fun filterJapaneseHoliday(list: List<LocalDate>, year: Int): List<LocalDate> =
    list.filter { date -> getJapaneseHoliday(year).contains(date).not() }

fun List<LocalDate>.filterUntilToday(): List<LocalDate> =
    this.filter { date -> date <= LocalDate.now() }
