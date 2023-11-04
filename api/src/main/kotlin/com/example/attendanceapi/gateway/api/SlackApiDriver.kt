package com.example.attendanceapi.gateway.api

import arrow.core.Either
import arrow.core.raise.either
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Component
class SlackApiDriver(@Autowired private val env: Environment) {
    fun fetchMessages(employeeId: String, year: String, month: String): Either<SlackApiError, SlackApiResponse> = either {
        val token = env.getProperty("slack.token")
        val authToken = "Bearer $token"
        val userId = "@${employeeId}"
        val before = "${year}-${month}-31"
        val after = "${year}-${month}-01"
        val query = URLEncoder.encode(
            "from:<$userId> in:grp-dev-勤怠 before:$before after:$after on:$before on:$after",
            StandardCharsets.UTF_8.toString()
        )
        val count = "100"

        // HTTPヘッダーを設定
        val headers = HttpHeaders()
        headers.set("Authorization", authToken)


        val url = URL("https://slack.com/api/search.messages?query=$query&count=$count")
        val con = url.openConnection() as HttpURLConnection

        // 接続設定(ミリ秒で指定)
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
        json.decodeFromString<SlackApiResponse>(str)
    }

    data class SlackApiError(val message: String = "slack api failed")

    @Serializable
    data class Message(
        val iid: String,
        val team: String,
        val score: Int,
        val channel: Channel,
        val type: String,
        val user: String,
        val username: String,
        val ts: String,
        val text: String,
        val permalink: String,
    )

    @Serializable
    data class Channel(
        val id: String,
        val is_channel: Boolean,
        val is_group: Boolean,
        val is_im: Boolean,
        val is_mpim: Boolean,
        val is_shared: Boolean,
        val is_org_shared: Boolean,
        val is_ext_shared: Boolean,
        val is_private: Boolean,
        val name: String,
        val pending_shared: List<String>,
        val is_pending_ext_shared: Boolean
    )

    @Serializable
    data class SlackApiResponse(
        val ok: Boolean,
        val query: String,
        val messages: Messages
    )

    @Serializable
    data class Messages(
        val total: Int,
        val pagination: Pagination,
        val paging: Paging,
        val matches: List<Message>
    )

    @Serializable
    data class Pagination(
        val total_count: Int,
        val page: Int,
        val per_page: Int,
        val page_count: Int,
        val first: Int,
        val last: Int
    )

    @Serializable
    data class Paging(
        val count: Int,
        val total: Int,
        val page: Int,
        val pages: Int
    )
}