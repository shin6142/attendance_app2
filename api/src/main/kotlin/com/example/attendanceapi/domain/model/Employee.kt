package com.example.attendanceapi.domain.model

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import java.util.*

class Employee(val employeeId: UUID, val name: EmployeeName){
    companion object{
        fun create(name: EmployeeName) =
            Employee(UUID.randomUUID(), name)
    }
}

class EmployeeName(val name: String){
    companion object{
        fun fromString(name: String): Either<EmployeeNameError, EmployeeName> =
            if (name.length > 20 || name.isEmpty()){
                EmployeeNameError(name).left()
            }else{
                EmployeeName(name).right()
            }
    }
}

class EmployeeNameError(val name: String)