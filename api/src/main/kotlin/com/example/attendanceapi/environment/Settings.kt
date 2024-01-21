package com.example.attendanceapi.environment

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "frontend")
class Frontend {
    var url: String? = null
}

@Component
@ConfigurationProperties(prefix = "slack")
class Settings {
    var frontendUrl: String? = null
}


@Component
class ConfigReader {
    @Autowired
    private val settings: Frontend? = null

    fun getFrontEndUrl(): String =
        settings?.url ?: "http://localhost:5173"
}