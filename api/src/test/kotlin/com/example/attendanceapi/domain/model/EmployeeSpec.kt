package com.example.attendanceapi.domain.model

import arrow.core.right
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.assertions.arrow.core.shouldBeRight

class EmployeeSpec: DescribeSpec({
    describe("EmployeeName"){
        it("20文字以下の名前でEmployeeNameを作成する"){
            EmployeeName.fromString("Madison").shouldBeRight()
        }
    }
})