package com.example.attendanceapi.usecase

import com.example.attendanceapi.domain.gateway.database.EmployeeGateway
import com.example.attendanceapi.domain.model.Employee
import org.springframework.stereotype.Component

@Component
class EmployeeUseCase(private val employeeGateway: EmployeeGateway) {
    fun getById(id: Int): EmployeeOutput = employeeGateway.fetch(id).let { employee -> EmployeeOutput(employee.id, employee.name) }

    fun create(input: EmployeeInput): EmployeeOutput{
        employeeGateway.add(Employee(input.id, input.name))
        return EmployeeOutput(input.id, input.name)
    }

    data class EmployeeOutput(val id: Int, val name: String)
    data class EmployeeInput(val id: Int, val name: String)
}