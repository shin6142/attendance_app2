package com.example.attendanceapi.domain

import com.example.attendanceapi.domain.model.Attendance
import com.example.attendanceapi.domain.model.AttendanceKind
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class AttendanceSpec : DescribeSpec({
    describe("Attendance") {
        it("onDate") {
            val attendance = Attendance.create(
                1,
                LocalDateTime.of(LocalDate.of(2023, 10, 10), LocalTime.of(12, 30, 0, 0)),
                "開始します",
                AttendanceKind.START
            )

            attendance.onDate(LocalDate.of(2023, 10, 10)) shouldBe true
        }
    }
})