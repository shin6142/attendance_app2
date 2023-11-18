package com.example.attendanceapi.domain.model

import io.kotest.core.spec.style.DescribeSpec
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.util.*

class AttendanceSpec: DescribeSpec({
    describe("DailyAttendance.createBreakCords"){
        fun attendance_20230401(hour: Int, minutes: Int, kind: AttendanceKind) =
            Attendance(
                UUID.randomUUID(),
                "",
                LocalDateTime.of(2023, Month.APRIL, 1, hour, minutes),
                "leave",
                kind
            )

        it("BreakRecordsを生成する"){
            //given
            DailyAttendance(
                date = LocalDate.of(2023, Month.APRIL, 1),
                attendances = listOf(
                    attendance_20230401(12, 0, AttendanceKind.LEAVE),
                    attendance_20230401(12, 0, AttendanceKind.BACK),
                    attendance_20230401(12, 0, AttendanceKind.LEAVE),
                    attendance_20230401(12, 0, AttendanceKind.BACK)
                )
            )
        }
    }
})