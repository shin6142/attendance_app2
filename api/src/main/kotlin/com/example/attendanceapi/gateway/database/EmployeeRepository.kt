package com.example.attendanceapi.gateway.database

import com.example.attendance.driver.attendancedb.tables.records.EmployeesRecord
import com.example.attendance.driver.attendancedb.tables.references.EMPLOYEES
import com.example.attendanceapi.domain.gateway.database.EmployeeGateway
import com.example.attendanceapi.domain.model.Employee
import org.jooq.DSLContext
import org.springframework.stereotype.Component

@Component
class EmployeeRepository(private val create: DSLContext) : EmployeeGateway {
    override fun fetch(id: Int): Employee {
        val record = create.selectFrom(EMPLOYEES).where(EMPLOYEES.ID.eq(id)).fetchAny() ?: EmployeesRecord(0, "")
        return Employee(record.id ?: 0, record.name ?: "")
    }

    override fun add(employee: Employee) {
        create.insertInto(EMPLOYEES, EMPLOYEES.ID, EMPLOYEES.NAME).values(employee.id, employee.name).execute()
    }
}