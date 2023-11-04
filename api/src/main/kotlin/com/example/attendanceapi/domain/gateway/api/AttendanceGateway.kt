package com.example.attendanceapi.domain.gateway.api

import arrow.core.Either
import com.example.attendanceapi.domain.model.Attendances

interface AttendanceGateway {
    fun retrieveAttendances(employeeId: String, year: String, month: String): Either<RetrieveAttendancesError, Attendances>
}

interface RetrieveAttendancesError