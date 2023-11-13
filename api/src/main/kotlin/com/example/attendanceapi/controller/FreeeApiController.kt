package com.example.attendanceapi.controller

import com.example.attendance_api.openapi.generated.controller.FreeeApi
import com.example.attendance_api.openapi.generated.model.FreeeAuthenticationCode
import com.example.attendanceapi.gateway.api.FreeeApiDriver
import io.swagger.v3.oas.annotations.Parameter
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
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
    ): ResponseEntity<Unit> {
        freeeApiDriver.getToken(code).fold(
            { return ResponseEntity(HttpStatus.UNAUTHORIZED) },
            { tokens ->
                FreeeAuthenticationCode(
                    tokens.access_token,
                    tokens.token_type,
                    tokens.expires_in,
                    tokens.refresh_token,
                    tokens.scope,
                    tokens.created_at,
                    tokens.company_id
                )
                return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create("http://localhost:5173?token=${tokens.access_token}"))
                    .build()
            }
        )
    }
}