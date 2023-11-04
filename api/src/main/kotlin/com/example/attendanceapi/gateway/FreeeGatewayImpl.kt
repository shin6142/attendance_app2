package com.example.attendanceapi.gateway

import com.example.attendanceapi.domain.gateway.database.FreeeGateway

class FreeeGatewayImpl: FreeeGateway {
    override fun saveAuthenticationCode(code: String): String {
        // TODO save authentication code to DB
        return code
    }
}