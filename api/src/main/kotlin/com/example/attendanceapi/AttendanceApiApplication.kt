package com.example.attendanceapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AttendanceApiApplication

fun main(args: Array<String>) {
    runApplication<AttendanceApiApplication>(*args)
}
