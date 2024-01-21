package com.example.attendanceapi.usecase

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.example.attendanceapi.domain.gateway.database.EmployeeRepository
import org.springframework.stereotype.Component
import java.util.*

@Component
class EmployeeUseCase(private val employeeRepository: EmployeeRepository) {
    fun getById(id: UUID): Either<EmployeeUseCaseError, EmployeeOutput> =
        employeeRepository.fetch(id).fold({
            EmployeeUseCaseError(id).left()
        },{
            EmployeeOutput(it.employeeId.toString(), it.name.name).right()
        })

    data class EmployeeOutput(val id: String, val name: String)
    data class EmployeeInput(val id: String, val name: String)
}

class EmployeeUseCaseError(val employeeId: UUID)