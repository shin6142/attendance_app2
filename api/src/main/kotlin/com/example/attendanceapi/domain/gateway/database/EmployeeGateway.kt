package com.example.attendanceapi.domain.gateway.database

import com.example.attendanceapi.domain.model.Employee

interface EmployeeGateway {
    fun fetch(id: Int): Employee

    fun add(employee: Employee)
}