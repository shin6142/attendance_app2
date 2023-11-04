package com.example.attendanceapi.controller

import com.example.attendance_api.openapi.generated.controller.EmployeeApi
import com.example.attendance_api.openapi.generated.model.Employee
import com.example.attendanceapi.usecase.EmployeeUseCase
import io.swagger.v3.oas.annotations.Parameter
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class EmployeeController(private val useCase: EmployeeUseCase) : EmployeeApi {
    override fun get(
        @Parameter(
            description = "target employee's id",
            required = true
        ) @PathVariable(value = "id") id: Int
    ): ResponseEntity<Employee> {
        val output = useCase.getById(id)
        return ResponseEntity(Employee(output.id, output.name), HttpStatus.OK)
    }

    override fun create(employee: Employee): ResponseEntity<Employee> {
        val output = useCase.create(EmployeeUseCase.EmployeeInput(employee.id, employee.name))
        return ResponseEntity(Employee(output.id, output.name), HttpStatus.CREATED)
    }
}