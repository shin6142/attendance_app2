package com.example.attendanceapi.domain.gateway.database

interface FreeeGateway {

    fun saveAuthenticationCode(code: String): String
}