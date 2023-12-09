package com.example.attendanceapi.controller

import com.example.attendance_api.openapi.generated.controller.AttendanceApi
import com.example.attendance_api.openapi.generated.model.Attendance
import com.example.attendance_api.openapi.generated.model.Attendances
import com.example.attendance_api.openapi.generated.model.DailyAttendances
import com.example.attendanceapi.usecase.AttendanceUseCase
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
class AttendanceController(private val useCase: AttendanceUseCase) : AttendanceApi {
    override fun getMonthlyByEmployeeId(
        @Parameter(
            description = "target attendance employee's id",
            required = true
        ) @PathVariable(value = "employee_id") employeeId: String,
        @PathVariable("channel_name") channelName: kotlin.String,
        @Parameter(description = "target attendance year", required = true)
        @Parameter(
            description = "target attendance year",
            required = true
        ) @PathVariable(value = "year") year: String,
        @Parameter(
            description = "target attendance year",
            required = true
        ) @PathVariable(value = "month") month: String
    ): ResponseEntity<Attendances> =
        useCase.getMessages(AttendanceUseCase.AttendancesInput(employeeId, year, channelName, month)).fold(
            { ResponseEntity(Attendances(emptyList()), HttpStatus.INTERNAL_SERVER_ERROR) },
            { output ->
                output.list.map {
                    DailyAttendances(
                        it.date,
                        it.attendance.map { attendanceOutput ->
                            Attendance(
                                attendanceOutput.attendanceId,
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
}