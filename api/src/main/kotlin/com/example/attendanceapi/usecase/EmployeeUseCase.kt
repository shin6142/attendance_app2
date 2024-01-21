package com.example.attendanceapi.usecase

import com.example.attendanceapi.domain.gateway.database.EmployeeGateway
import org.springframework.stereotype.Component

@Component
class EmployeeUseCase(private val employeeGateway: EmployeeGateway) {
    fun getById(id: Int): EmployeeOutput = employeeGateway.fetch(id).let { employee -> EmployeeOutput(employee.id, employee.name) }

    data class EmployeeOutput(val id: String, val name: String)
    data class EmployeeInput(val id: String, val name: String)
}