package com.example.attendanceapi.domain.gateway.api

import arrow.core.Either
import com.example.attendanceapi.domain.model.Attendances
import com.example.attendanceapi.domain.model.DailyAttendance
import com.example.attendanceapi.domain.model.Employee

interface AttendanceGateway {
    fun retrieveAttendances(employeeId: String, channelName: String, year: String, month: String): Either<RetrieveAttendancesError, Pair<Employee, List<DailyAttendance>>>

    fun recordAttendances(token: String, companyId: Int, employeeId: Int, dailyAttendances: List<DailyAttendance>): Either<RecordAttendancesError, RecordAttendancesResult>
}

interface RetrieveAttendancesError

interface RecordAttendancesError

interface RecordAttendancesResult