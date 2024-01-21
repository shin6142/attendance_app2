package com.example.attendanceapi.controller

import com.example.attendance_api.openapi.generated.controller.FreeeApi
import com.example.attendance_api.openapi.generated.model.FreeeLoginUser
import com.example.attendanceapi.gateway.api.FreeeApiDriver
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import javax.validation.Valid
import javax.validation.constraints.NotNull

@RestController
class FreeeApiController(val freeeApiDriver: FreeeApiDriver) : FreeeApi {
    override fun getFreeeAuthenticationCode(
        @NotNull @Parameter(
            description = "freee api authentication code",
            required = true
        ) @Valid @RequestParam(value = "code", required = true) code: String
    ): ResponseEntity<Unit> =
        freeeApiDriver.getToken(code).fold(
            { ResponseEntity(HttpStatus.UNAUTHORIZED) },
            { tokens ->
                ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create("http://localhost:5173/attendances/?token=${tokens.access_token}"))
                    .build()
            }
        )

    override fun getFreeeLoginUser(
        @Parameter(description = "", `in` = ParameterIn.HEADER, required = true) @RequestHeader(
            value = "code",
            required = true
        ) code: kotlin.String
    ): ResponseEntity<FreeeLoginUser> =
        freeeApiDriver.getLoginUser(code).fold(
            { ResponseEntity(HttpStatus.UNAUTHORIZED) },
            { user ->
                return user.companies.find { it.id.toInt() == 1884310 }
                    ?.let {
                        ResponseEntity(
                            FreeeLoginUser(
                                id = it.employeeId?.toInt() ?: 0,
                                name = it.displayName ?: "",
                                companyName = it.name,
                                companyId = it.id.toInt()
                            ),
                            HttpStatus.OK
                        )
                    } ?: ResponseEntity(HttpStatus.UNAUTHORIZED)
            }
        )
}