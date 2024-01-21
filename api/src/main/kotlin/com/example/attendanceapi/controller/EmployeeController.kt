package com.example.attendanceapi.controller

import com.example.attendance_api.openapi.generated.controller.EmployeeApi
import com.example.attendance_api.openapi.generated.model.Employee
import com.example.attendanceapi.usecase.EmployeeUseCase
import io.swagger.v3.oas.annotations.Parameter
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
class EmployeeController(private val useCase: EmployeeUseCase) : EmployeeApi {
    override fun get(id: String): ResponseEntity<Employee> =
        useCase.getById(UUID.fromString(id)).fold(
            {
                ResponseEntity(null, HttpStatus.INTERNAL_SERVER_ERROR)
            },
            { output ->
                ResponseEntity(Employee(output.id, output.name), HttpStatus.OK)
            }
        )
}