package com.example.attendanceapi.domain.gateway.database

import arrow.core.Either
import com.example.attendanceapi.domain.model.Employee
import java.util.*

interface EmployeeRepository {
    fun fetch(employeeId: UUID): Either<EmployeeRepositoryError, Employee>
}

class EmployeeRepositoryError(val employeeId: UUID){

}