package com.example.attendanceapi.gateway.database

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.example.attendance.driver.attendancedb.tables.references.EMPLOYEES
import com.example.attendanceapi.domain.gateway.database.EmployeeRepository
import com.example.attendanceapi.domain.gateway.database.EmployeeRepositoryError
import com.example.attendanceapi.domain.model.Employee
import com.example.attendanceapi.domain.model.EmployeeName
import org.jooq.DSLContext
import org.springframework.stereotype.Component
import java.util.*

@Component
class EmployeeRepository(private val create: DSLContext) :
    EmployeeRepository {
    override fun fetch(employeeId: UUID): Either<EmployeeRepositoryError, Employee> {
        val record = create.selectFrom(EMPLOYEES).where(EMPLOYEES.EMPLOYEE_ID.eq(employeeId)).fetchOne()
        if (record == null){
            return EmployeeRepositoryError(employeeId).left()
        }else{
            return Employee(record.employeeId!!, EmployeeName(record.name ?: "")).right()
        }
    }
}