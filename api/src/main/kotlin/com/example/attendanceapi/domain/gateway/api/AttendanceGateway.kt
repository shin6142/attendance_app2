package com.example.attendanceapi.domain.gateway.api

import arrow.core.Either
import com.example.attendanceapi.domain.model.Attendances
import com.example.attendanceapi.domain.model.DailyAttendance

interface AttendanceGateway {
    fun retrieveAttendances(employeeId: String, year: String, month: String): Either<RetrieveAttendancesError, List<DailyAttendance>>

    fun recordAttendances(token: String, companyId: Int, employeeId: Int, dailyAttendances: List<DailyAttendance>): Either<RecordAttendancesError, RecordAttendancesResult>
}

interface RetrieveAttendancesError

interface RecordAttendancesError

interface RecordAttendancesResult