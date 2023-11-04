package com.example.attendanceapi.gateway.api

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.right
import com.example.attendanceapi.domain.gateway.api.AttendanceGateway
import com.example.attendanceapi.domain.gateway.api.RetrieveAttendancesError
import com.example.attendanceapi.domain.model.Attendance
import com.example.attendanceapi.domain.model.AttendanceKind
import com.example.attendanceapi.domain.model.Attendances
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.ZoneId


@Component
class AttendanceGatewayImpl(private val slackApiDriver: SlackApiDriver) : AttendanceGateway {
    override fun retrieveAttendances(
        employeeId: Int,
        year: String,
        month: String
    ): Either<RetrieveAttendancesErrorImpl, Attendances> =
        slackApiDriver.fetchMessages(employeeId, year, month)
            .mapLeft { RetrieveAttendancesErrorImpl("", "") }
            .flatMap { slackApiResponse ->
                slackApiResponse.messages.matches.map {
                    Attendance.create(
                        employeeId,
                        // 秒数をミリ秒に変換してInstantを作成
                        Instant.ofEpochMilli((it.ts.toDouble() * 1000).toLong()).atZone(ZoneId.systemDefault())
                            .toLocalDateTime(),
                        it.text,
                        defineKindFromText(it.text)
                    )
                }.let { Attendances(it) }.right()
            }

    private fun defineKindFromText(text: String): AttendanceKind =
        when(text){
            ":si:" -> AttendanceKind.START
            ":ri:" -> AttendanceKind.LEAVE
            ":modo:" -> AttendanceKind.BACK
            ":syu:" -> AttendanceKind.END
            else -> AttendanceKind.UNKNOWN
        }
}

data class RetrieveAttendancesErrorImpl(val input: String, val message: String) : RetrieveAttendancesError