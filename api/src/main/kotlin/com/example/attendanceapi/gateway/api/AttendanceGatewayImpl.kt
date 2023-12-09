package com.example.attendanceapi.gateway.api

import arrow.core.*
import com.example.attendanceapi.domain.gateway.api.AttendanceGateway
import com.example.attendanceapi.domain.gateway.api.RecordAttendancesError
import com.example.attendanceapi.domain.gateway.api.RecordAttendancesResult
import com.example.attendanceapi.domain.gateway.api.RetrieveAttendancesError
import com.example.attendanceapi.domain.model.Attendance
import com.example.attendanceapi.domain.model.AttendanceKind
import com.example.attendanceapi.domain.model.DailyAttendance
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter


@Component
class AttendanceGatewayImpl(private val slackApiDriver: SlackApiDriver, private val freeeApiDriver: FreeeApiDriver) :
    AttendanceGateway {
    override fun retrieveAttendances(
        employeeId: String,
        channelName: String,
        year: String,
        month: String
    ): Either<RetrieveAttendancesError, List<DailyAttendance>> =
        slackApiDriver.fetchMessages(employeeId, year, channelName, month)
            .mapLeft { RetrieveAttendancesErrorImpl("", "") }
            .flatMap { slackApiResponse ->
                slackApiResponse.messages.matches.map {
                    Attendance.create(
                        // 秒数をミリ秒に変換してInstantを作成
                        Instant.ofEpochMilli((it.ts.toDouble() * 1000).toLong()).atZone(ZoneId.systemDefault())
                            .toLocalDateTime(),
                        it.text,
                        defineKindFromText(it.text)
                    )
                }.groupBy {it.dateTime.toLocalDate()}.map {
                    DailyAttendance(employeeId, it.key, it.value)
                }.right()
            }

    override fun recordAttendances(
        token: String,
        companyId: Int,
        employeeId: Int,
        dailyAttendances: List<DailyAttendance>
    ): Either<RecordAttendancesError, RecordAttendancesResult> =
        dailyAttendances.map { toFreeeAttendanceInput(token, companyId, employeeId, it) }.let {
            freeeApiDriver.saveAttendances(it)
        }.mapLeft { error ->
            RecordAttendancesErrorImpl("", error.message)
        }.flatMap{
            RecordAttendancesResultImpl("", it).right()
        }

    private fun toFreeeAttendanceInput(
        authenticationCode: String,
        companyId: Int,
        employeeId: Int,
        dailyAttendance: DailyAttendance
    ): FreeeAttendanceInput =
        FreeeAttendanceInput(
            authenticationCode = authenticationCode,
            employeeId = employeeId,
            date = dailyAttendance.date.toString(),
            companyId = companyId,
            breakRecords = dailyAttendance.createBreakRecords().records.map { it ->
                BreakRecord(
                    it.pair.first.dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    it.pair.second.dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                )
            },
            clockInAt = dailyAttendance.start()?.dateTime?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) ?: "",
            clockOutAt = dailyAttendance.end()?.dateTime?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) ?: "",
        )

    private fun defineKindFromText(text: String): AttendanceKind =
        if (text.contains(":si:") || text.contains("始")) AttendanceKind.START
        else if (text.contains(":ri:") || text.contains("休") || text.contains("離")) AttendanceKind.LEAVE
        else if (text.contains(":modo:") || text.contains("戻")) AttendanceKind.BACK
        else if (text.contains(":syu:") ||  text.contains("終")) AttendanceKind.END
        else AttendanceKind.UNKNOWN
}

data class RetrieveAttendancesErrorImpl(val input: String, val message: String) : RetrieveAttendancesError
data class RecordAttendancesErrorImpl(val input: String, val message: String) : RecordAttendancesError

data class RecordAttendancesResultImpl (val input: String, val message: List<Any>) : RecordAttendancesResult