package com.smartbudget.core.network

import com.smartbudget.core.storage.AppSettings
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLProtocol
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Создаёт общий HttpClient для всех репозиториев.
 *
 * Движок выбирается автоматически по classpath:
 *  - Android: OkHttp (implementation libs.ktor.engine.okhttp в androidMain)
 *  - iOS: Darwin (implementation libs.ktor.engine.darwin в iosMain)
 */
fun createHttpClient(appSettings: AppSettings): HttpClient = HttpClient {
    // ContentNegotiation: парсим JSON
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
            prettyPrint = false
            isLenient = true
            coerceInputValues = true
        })
    }

    // Логирование
    install(Logging) { level = LogLevel.NONE }

    // Таймауты: AI-запросы бывают долгими (до 120 сек)
    install(io.ktor.client.plugins.HttpTimeout) {
        requestTimeoutMillis = 120_000
        connectTimeoutMillis = 15_000
        socketTimeoutMillis = 120_000
    }

    // Базовые заголовки. Bearer-токен НЕ здесь — он подставляется per-request
    // в ApiClient (defaultRequest фиксирует значения один раз при создании клиента,
    // что ломает авторизацию после логина).
    defaultRequest {
        header(HttpHeaders.ContentType, ContentType.Application.Json)
    }

    // Проброс ошибок: при не-2xx кидаем ApiException с телом
    HttpResponseValidator {
        validateResponse { response ->
            val status: HttpStatusCode = response.status
            if (!status.isSuccess()) {
                val body = try { response.body<String>() } catch (_: Exception) { null }
                throw ApiException(status.value, body)
            }
        }
    }
}

/**
 * Ошибка API: сервер вернул не-2xx.
 */
class ApiException(val statusCode: Int, val body: String?) : Exception(
    "API error $statusCode: ${body?.take(200)}"
)

/**
 * Ошибка сети (нет соединения, таймаут).
 */
class NetworkException(cause: Throwable) : Exception("Network error: ${cause.message}", cause)

/**
 * Парсит базовый URL из настроек, возвращая хост/порт/протокол.
 * Поддерживает "http://10.0.2.2:8080" и "https://example.com".
 */
data class ParsedUrl(val protocol: URLProtocol, val host: String, val port: Int)

fun parseBaseUrl(url: String): ParsedUrl {
    val cleaned = url.trim().removeSuffix("/")
    val noScheme: String
    val protocol: URLProtocol
    when {
        cleaned.startsWith("https://") -> {
            protocol = URLProtocol.HTTPS; noScheme = cleaned.removePrefix("https://")
        }
        cleaned.startsWith("http://") -> {
            protocol = URLProtocol.HTTP; noScheme = cleaned.removePrefix("http://")
        }
        else -> {
            protocol = URLProtocol.HTTP; noScheme = cleaned
        }
    }
    val parts = noScheme.split(":")
    val host = parts[0]
    val port = parts.getOrNull(1)?.toIntOrNull() ?: if (protocol == URLProtocol.HTTPS) 443 else 80
    return ParsedUrl(protocol, host, port)
}
