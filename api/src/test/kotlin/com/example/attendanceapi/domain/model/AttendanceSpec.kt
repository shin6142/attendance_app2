package com.example.attendanceapi.domain.model

import arrow.core.raise.result
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
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
            val attendance_20230401_1 = attendance_20230401(12, 0, AttendanceKind.LEAVE)
            val attendance_20230401_2 = attendance_20230401(13, 0, AttendanceKind.BACK)
            val attendance_20230401_3 = attendance_20230401(18, 0, AttendanceKind.LEAVE)
            val attendance_20230401_4 = attendance_20230401(19, 0, AttendanceKind.BACK)
            //given
            val dailyAttendance = DailyAttendance(
                date = LocalDate.of(2023, Month.APRIL, 1),
                attendances = listOf(
                    attendance_20230401_1,
                    attendance_20230401_2,
                    attendance_20230401_3,
                    attendance_20230401_4
                )
            )
            //when
            val actual = dailyAttendance.createBreakRecords()
            //then
            actual.breakRecords[0].pair.first.kind shouldBe AttendanceKind.LEAVE
            actual.breakRecords[0].pair.first.dateTime shouldBe LocalDateTime.of(2023, Month.APRIL, 1, 12, 0)
            actual.breakRecords[0].pair.second.kind shouldBe AttendanceKind.BACK
            actual.breakRecords[0].pair.second.dateTime shouldBe LocalDateTime.of(2023, Month.APRIL, 1, 13, 0)
            actual.breakRecords[1].pair.first.kind shouldBe AttendanceKind.LEAVE
            actual.breakRecords[1].pair.first.dateTime shouldBe LocalDateTime.of(2023, Month.APRIL, 1, 18, 0)
            actual.breakRecords[1].pair.second.kind shouldBe AttendanceKind.BACK
            actual.breakRecords[1].pair.second.dateTime shouldBe LocalDateTime.of(2023, Month.APRIL, 1, 19, 0)
        }
    }
})