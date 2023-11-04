package com.example.attendanceapi.controller

import com.example.attendance_api.openapi.generated.controller.AttendanceApi
import com.example.attendance_api.openapi.generated.model.Attendance
import com.example.attendance_api.openapi.generated.model.Attendances
import com.example.attendance_api.openapi.generated.model.DailyAttendances
import com.example.attendanceapi.gateway.api.BreakRecords
import com.example.attendanceapi.gateway.api.FreeeApiDriver
import com.example.attendanceapi.gateway.api.FreeeAttendanceInput
import com.example.attendanceapi.usecase.AttendanceUseCase
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import io.swagger.v3.oas.annotations.Parameter
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid


@RestController
class AttendanceController(private val useCase: AttendanceUseCase, val freeeApiDriver: FreeeApiDriver) : AttendanceApi {
    override fun getMonthlyByEmployeeId(
        @Parameter(
            description = "target attendance employee's id",
            required = true
        ) @PathVariable(value = "employee_id") employeeId: Int,
        @Parameter(
            description = "target attendance year",
            required = true
        ) @PathVariable(value = "year") year: String,
        @Parameter(
            description = "target attendance year",
            required = true
        ) @PathVariable(value = "month") month: String
    ): ResponseEntity<Attendances> =
        useCase.getMonthlyByEmployeeId(AttendanceUseCase.AttendancesInput(employeeId, year, month)).fold(
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
        ) @PathVariable("employee_id") employeeId: kotlin.Int,
        @Parameter(
            description = "target attendance year",
            required = true
        ) @PathVariable("year") year: kotlin.String,
        @Parameter(
            description = "target attendance year",
            required = true
        ) @PathVariable("month") month: kotlin.String
    ): ResponseEntity<kotlin.String> =
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
        ) @PathVariable("employee_id") employeeId: kotlin.Int,
        @Parameter(
            description = "target attendance year",
            required = true
        ) @PathVariable("year") year: kotlin.String,
        @Parameter(
            description = "target attendance year",
            required = true
        ) @PathVariable("month") month: kotlin.String,
    ): ResponseEntity<String> =
        useCase.getMonthlyByEmployeeId(AttendanceUseCase.AttendancesInput(employeeId, year, month)).fold(
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
                company_id = 1884310,
                break_records = BreakRecords(
                    value.find { it.contains("LEAVE") }?.get(3) ?: "",
                    value.find { it.contains("BACK") }?.get(3) ?: "",
                ),
                clock_in_at = value.find { it.contains("START") }?.get(3) ?: "",
                clock_out_at = value.find { it.contains("END") }?.get(3) ?: "",
                day_pattern = "",
                early_leaving_mins = 0,
                is_absence = false,
                lateness_mins = 0,
                normal_work_clock_in_at = value.find { it.contains("START") }?.get(3) ?: "",
                normal_work_clock_out_at = value.find { it.contains("END") }?.get(3) ?: "",
                normal_work_mins = 0,
                normal_work_mins_by_paid_holiday = 0,
                note = "",
                paid_holiday = 0,
                use_attendance_deduction = true,
                use_default_work_pattern = true
            )
        }

        val responses = input.map { freeeApiDriver.putAttendanceRecords(it) }

        return ResponseEntity(
            responses.toString(),
            HttpStatus.CREATED
        )

    }
}