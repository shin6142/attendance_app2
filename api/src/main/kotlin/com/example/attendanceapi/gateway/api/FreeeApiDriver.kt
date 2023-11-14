package com.example.attendanceapi.gateway.api

import arrow.core.Either
import arrow.core.left
import arrow.core.raise.either
import arrow.core.right
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
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


    fun putAttendanceRecords(input: FreeeAttendanceInput): Either<FreeeReisterAttendanceError, String> {
        try {
            // Define the API endpoint URL
            val apiUrl = "https://api.freee.co.jp/hr/api/v1/employees/${input.employeeId}/work_records/${input.date}"

            // Define the request payload (data) as a JSON string
            val requestData = """
                {
                    "company_id": ${input.companyId},
                    "break_records": [
                        {
                            "clock_in_at": "${input.breakRecords.clockInAt}",
                            "clock_out_at": "${input.breakRecords.clockOutAt}"
                        }
                    ],
                    "clock_in_at": "${input.clockInAt}",
                    "clock_out_at": "${input.clockOutAt}"
                }
            """.trimIndent()

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

            // Close the connection
            connection.disconnect()
            return ("date: ${input.date}, result: $responseCode").right()
        } catch (e: Exception) {
            return FreeeReisterAttendanceError(e.message ?: "Freee打刻登録に失敗しました").left()
        }
    }

    fun getLoginUser(code: String): Either<FreeeAuthenticationError, FreeeLoginUser> = either {
        val authToken = "Bearer $code"
        val headers = HttpHeaders()
        headers.set("Authorization", authToken)
        val url = URL("https://api.freee.co.jp/hr/api/v1/users/me")
        val con = url.openConnection() as HttpURLConnection

        con.connectTimeout = 20_000 // 20 秒
        con.readTimeout = 20_000    // 20 秒
        con.requestMethod = "GET"   // GETの場合は省略可能
        con.setRequestProperty("Authorization", authToken);

        con.connect()
        val str = con.inputStream.bufferedReader(Charsets.UTF_8).use { br ->
            br.readLines().joinToString("")
        }

        val json = Json { ignoreUnknownKeys = true }
        con.disconnect()
        json.decodeFromString<FreeeLoginUser>(str)
    }
}

@Serializable
data class FreeeLoginUser(
    val id: Long,
    val companies: List<Company>
)

@Serializable
data class Company(
    val id: Long,
    val name: String,
    val role: String,
    @SerialName("external_cid") val externalCid: String,
    @SerialName("employee_id") val employeeId: Long?,
    @SerialName("display_name") val displayName: String?
)

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

data class FreeeAuthenticationError(val statusCode: Int, val message: String)

data class FreeeReisterAttendanceError(val message: String)


data class FreeeAttendanceInput(
    val authenticationCode: String,
    val employeeId: Int,
    val date: String,
    val companyId: Int,
    val breakRecords: BreakRecords,
    val clockInAt: String,
    val clockOutAt: String,
)

@Serializable
data class BreakRecords(
    val clockInAt: String,
    val clockOutAt: String,
)