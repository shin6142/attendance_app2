package com.example.attendanceapi.controller

import arrow.core.flatMap
import arrow.core.getOrElse
import com.example.attendance_api.openapi.generated.controller.AttendanceApi
import com.example.attendance_api.openapi.generated.model.Attendance
import com.example.attendance_api.openapi.generated.model.Attendances
import com.example.attendance_api.openapi.generated.model.DailyAttendances
import com.example.attendanceapi.gateway.api.BreakRecord
import com.example.attendanceapi.gateway.api.FreeeApiDriver
import com.example.attendanceapi.gateway.api.FreeeAttendanceInput
import com.example.attendanceapi.usecase.AttendanceUseCase
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid


@RestController
class AttendanceController(private val useCase: AttendanceUseCase, val freeeApiDriver: FreeeApiDriver) : AttendanceApi {
    override fun getMonthlyByEmployeeId(
        @Parameter(
            description = "target attendance employee's id",
            required = true
        ) @PathVariable(value = "employee_id") employeeId: String,
        @Parameter(
            description = "target attendance year",
            required = true
        ) @PathVariable(value = "year") year: String,
        @Parameter(
            description = "target attendance year",
            required = true
        ) @PathVariable(value = "month") month: String
    ): ResponseEntity<Attendances> =
        useCase.getMessages(AttendanceUseCase.AttendancesInput(employeeId, year, month)).fold(
            { ResponseEntity(Attendances(emptyList()), HttpStatus.INTERNAL_SERVER_ERROR) },
            { output ->
                output.list.map {
                    DailyAttendances(
                        it.date,
                        it.attendance.map { attendanceOutput ->
                            Attendance(
                                attendanceOutput.employeeId,
                                attendanceOutput.employeeName,
                                attendanceOutput.datetime,
                                attendanceOutput.context,
                                attendanceOutput.kind
                            )
                        }
                    )
                }.let { ResponseEntity(Attendances(it), HttpStatus.OK) }
            }
        )

    override fun outputMessagesMonthlyByEmployeeId(
        @Parameter(
            description = "target attendance employee's id",
            required = true
        ) @PathVariable(value = "employee_id") employeeId: String,
        @Parameter(
            description = "target attendance year",
            required = true
        ) @PathVariable(value = "year") year: String,
        @Parameter(
            description = "target attendance year",
            required = true
        ) @PathVariable(value = "month") month: String
    ): ResponseEntity<String> =
        useCase.getMessages(AttendanceUseCase.AttendancesInput(employeeId, year, month)).fold(
            { ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR) },
            { output ->
                output.list.flatMap { it ->
                    it.attendance.map { attendanceOutput ->
                        listOf(
                            it.date,
                            attendanceOutput.employeeId,
                            attendanceOutput.employeeName,
                            attendanceOutput.datetime,
                            attendanceOutput.kind,
                            attendanceOutput.context,
                        )
                    }
                }.let { rows ->
                    val header = listOf(
                        listOf("access_token", ""),
                        listOf("勤務日", "従業員ID", "従業員名", "打刻時間", "打刻種別", "メッセージ")
                    )
                    val csv = csvWriter {
                        charset = "UTF-8"
                        lineTerminator = "\r\n"
                    }.writeAllAsString(
                        header + rows
                    )
                    ResponseEntity<String>(
                        csv,
                        HttpStatus.OK
                    )
                }
            })


    override fun downloadMonthlyByEmployeeId(
        @Parameter(
            description = "target attendance employee's id",
            required = true
        ) @PathVariable(value = "employee_id") employeeId: String,
        @Parameter(
            description = "target attendance year",
            required = true
        ) @PathVariable(value = "year") year: String,
        @Parameter(
            description = "target attendance year",
            required = true
        ) @PathVariable(value = "month") month: String
    ): ResponseEntity<String> =
        useCase.getMessages(AttendanceUseCase.AttendancesInput(employeeId, year, month)).fold(
            { ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR) },
            { output ->
                output.list.flatMap { it ->
                    it.attendance.map { attendanceOutput ->
                        listOf(
                            it.date,
                            attendanceOutput.employeeId,
                            attendanceOutput.employeeName,
                            attendanceOutput.datetime,
                            attendanceOutput.kind,
                            attendanceOutput.context,
                        )
                    }
                }.let { rows ->
                    val header = listOf(
                        listOf("access_token", ""),
                        listOf("勤務日", "従業員ID", "従業員名", "打刻時間", "打刻種別", "メッセージ")
                    )
                    val csv = csvWriter {
                        charset = "UTF-8"
                        lineTerminator = "\r\n"
                    }.writeAllAsString(
                        header + rows
                    )
                    ResponseEntity<String>(
                        csv,
                        HttpStatus.OK
                    )
                }
            }
        )

    override fun recordMonthlyAttendances(
        @Parameter(
            description = "",
            `in` = ParameterIn.HEADER,
            required = true
        ) @RequestHeader(value = "code", required = true) code: kotlin.String,
        @Parameter(
            description = "target attendance employee's id",
            required = true
        ) @PathVariable("employee_id") employeeId: kotlin.Int,
        @Parameter(
            description = "target attendance year",
            required = true
        ) @PathVariable("year") year: kotlin.String,
        @Parameter(
            description = "target attendance year",
            required = true
        ) @PathVariable("month") month: kotlin.String,
        @Parameter(
            description = "",
            required = true
        ) @Valid @RequestBody dailyAttendances: kotlin.collections.List<DailyAttendances>
    ): ResponseEntity<kotlin.String> {
        //FreeeAttendanceInputに変換
        val companyId = 1884310
        useCase.recordAttendances(
            AttendanceUseCase.RecordAttendancesInput(
                code,
                companyId.toString(),
                employeeId.toString(),
                dailyAttendances.map { it ->
                    AttendanceUseCase.DailyAttendanceInput(
                        // ""の場合の処理
                        employeeId.toString(),
                        it.date,
                        it.attendances.map { attendance ->
                            AttendanceUseCase.AttendanceInput(
                                employeeId = employeeId.toString(),
                                dateTime = attendance.datetime,
                                context = attendance.context,
                                kind = attendance.kind
                            )
                        }
                    )

                }
            )
        ).fold({ error ->
            return ResponseEntity(error.toString(), HttpStatus.BAD_REQUEST)
        }, { output ->
            return ResponseEntity(output.toString(), HttpStatus.CREATED)
        })
    }


    override fun uploadMonthlyByEmployeeId(
        @Parameter(
            description = "target attendance employee's id",
            required = true
        ) @PathVariable("employee_id") employeeId: kotlin.Int,
        @Parameter(
            description = "target attendance year",
            required = true
        ) @PathVariable("year") year: kotlin.String,
        @Parameter(
            description = "target attendance year",
            required = true
        ) @PathVariable("month") month: kotlin.String,
        @Parameter(
            description = "",
            required = true
        ) @Valid @RequestBody body: kotlin.String
    ): ResponseEntity<kotlin.String> {
        val rows = body.split("\n").map { it.split(",") }
            .filter { it.first().contains("${year}-${month}") }.groupBy { it.first() }

        val input = rows.map { (key, value) ->
            FreeeAttendanceInput(
                authenticationCode = body.split("\n").map { it.split(",") }.first().get(1),
                employeeId = employeeId,
                date = key,
                companyId = 1884310,
                breakRecords = listOf(
                    BreakRecord(
                        value.find { it.contains("LEAVE") }?.get(3) ?: "",
                        value.find { it.contains("BACK") }?.get(3) ?: "",
                    )
                ),
                clockInAt = value.find { it.contains("START") }?.get(3) ?: "",
                clockOutAt = value.find { it.contains("END") }?.get(3) ?: ""
            )
        }

        val responses = input.map { freeeApiDriver.putAttendanceRecords(it) }

        return ResponseEntity(
            responses.toString(),
            HttpStatus.CREATED
        )
    }


}