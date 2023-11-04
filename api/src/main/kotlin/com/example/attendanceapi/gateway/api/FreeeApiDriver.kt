package com.example.attendanceapi.gateway.api

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

@Component
class FreeeApiDriver(@Autowired private val env: Environment) {

    fun getToken(code: String): Either<FreeeAuthenticationError, FreeeAuthenticationTokens> {
        val clientId = env.getProperty("freee.clientId")
        val clientSecret = env.getProperty("freee.clientSecret")

        val client = OkHttpClient()

        // リクエストボディをフォーム形式で構築
        val requestBody = RequestBody.create(
            "application/x-www-form-urlencoded".toMediaTypeOrNull(),
            "grant_type=authorization_code&" +
                    "client_id=$clientId&" +
                    "client_secret=$clientSecret&" +
                    "code=$code&" +
                    "redirect_uri=http://localhost:8080/freee/authenticate/callback"
        )

        // リクエストを構築
        val request = Request.Builder()
            .url("https://accounts.secure.freee.co.jp/public_api/token")
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .post(requestBody)
            .build()

        // リクエストを実行
        val response = client.newCall(request).execute()

        // レスポンスを出力
        val responseBody = response.body?.string() ?: "{}"
        val json = Json { ignoreUnknownKeys = true }
        return if (response.code == 200) {
            json.decodeFromString<FreeeAuthenticationTokens>(responseBody).right()
        } else {
            FreeeAuthenticationError(statusCode = response.code, "Freee Api 認証に失敗しました").left()
        }
    }


    fun putAttendanceRecords(input: FreeeAttendanceInput): Int {
        // Define the API endpoint URL
        val apiUrl = "https://api.freee.co.jp/hr/api/v1/employees/${input.employeeId}/work_records/${input.date}"

        // Define the request payload (data) as a JSON string
        val requestData = """
            {
                "company_id": ${input.company_id},
                "break_records": [
                    {
                        "clock_in_at": "${input.break_records.clock_in_at}",
                        "clock_out_at": "${input.break_records.clock_out_at}"
                    }
                ],
                "clock_in_at": "${input.clock_in_at}",
                "clock_out_at": "${input.clock_out_at}"
            }
        """.trimIndent()

        // Set the authorization token
        val authorizationToken = "Bearer ${input.authenticationCode}"

        try {
            // Create a URL object
            val url = URL(apiUrl)

            // Open a connection to the URL
            val connection = url.openConnection() as HttpURLConnection

            // Set the HTTP method to PUT
            connection.requestMethod = "PUT"

            // Set request headers
            connection.setRequestProperty("accept", "application/json")
            connection.setRequestProperty("Authorization", "Bearer ${input.authenticationCode}")
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("FREEE-VERSION", "2022-02-01")

            // Enable input and output streams for the connection
            connection.doInput = true
            connection.doOutput = true

            // Write the request data to the output stream
            val outputStream: OutputStream = connection.outputStream
            outputStream.write(requestData.toByteArray())
            outputStream.flush()
            outputStream.close()

            // Get the response code
            val responseCode = connection.responseCode
            println("Response Code: $responseCode")

            // Read the response from the server
            if (responseCode == 200 || responseCode == 201) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                var line: String?
                val response = StringBuilder()

                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }

                reader.close()
                println("Response Data: $response")
            } else {
                println("Request failed with response code: $responseCode")
            }

            // Close the connection
            connection.disconnect()
            return responseCode
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 0
    }

}

@Serializable
data class FreeeAuthenticationTokens(
    val access_token: String,
    val token_type: String,
    val expires_in: Int,
    val refresh_token: String,
    val scope: String,
    val created_at: Int,
    val company_id: Int
)

@Serializable
data class FreeeRegisterAttendancesResponse(
    val break_records: List<BreakRecords>,
    val clock_in_at: String,
    val clock_out_at: String,
    val date: String
)

data class FreeeAuthenticationError(val statusCode: Int, val message: String)

data class FreeeReisterAttendanceError(val statusCode: Int, val message: String)


data class FreeeAttendanceInput(
    val authenticationCode: String,
    val employeeId: Int,
    val date: String,
    val company_id: Int,
    val break_records: BreakRecords,
    val clock_in_at: String,
    val clock_out_at: String,
    val day_pattern: String,
    val early_leaving_mins: Int,
    val is_absence: Boolean = false,
    val lateness_mins: Int = 0,
    val normal_work_clock_in_at: String,
    val normal_work_clock_out_at: String,
    val normal_work_mins: Int = 0,
    val normal_work_mins_by_paid_holiday: Int = 0,
    val note: String = "",
    val paid_holiday: Int = 0,
    val use_attendance_deduction: Boolean = true,
    val use_default_work_pattern: Boolean = true
)

@Serializable
data class BreakRecords(
    val clock_in_at: String,
    val clock_out_at: String,
)