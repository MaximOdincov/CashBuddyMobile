package com.smartbudget.data.remote

import com.smartbudget.core.network.parseBaseUrl
import com.smartbudget.core.storage.AppSettings
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.encodedPath

/**
 * Базовый API-клиент. Все репозитории наследуются от него.
 * baseUrl берётся из AppSettings и может меняться (настройки → сервер).
 */
open class ApiClient(
    val httpClient: HttpClient,
    val appSettings: AppSettings
) {

    /** POST с JSON-body. */
    protected suspend inline fun <reified Req, reified Res> post(
        path: String,
        body: Req
    ): Res = httpClient.post { configureUrl(path); setBody(body) }.body()

    /** POST без тела (например /bank/generate). */
    protected suspend inline fun <reified Res> postEmpty(path: String): Res =
        httpClient.post { configureUrl(path) }.body()

    /** POST без тела, но с query-параметрами. */
    protected suspend inline fun <reified Res> postWithParams(
        path: String,
        queryParams: Map<String, String?> = emptyMap()
    ): Res = httpClient.post {
        configureUrl(path)
        queryParams.forEach { (k, v) -> if (v != null) url.parameters.append(k, v) }
    }.body()

    /** PUT с JSON-body. */
    protected suspend inline fun <reified Req, reified Res> put(
        path: String,
        body: Req
    ): Res = httpClient.put { configureUrl(path); setBody(body) }.body()

    /** PUT без ответа (fire-and-forget). */
    protected suspend inline fun <reified Req> putUnit(
        path: String,
        body: Req
    ) {
        httpClient.put { configureUrl(path); setBody(body) }
    }

    /** GET с query-параметрами. null-значения пропускаются. */
    protected suspend inline fun <reified Res> get(
        path: String,
        queryParams: Map<String, String?> = emptyMap()
    ): Res = httpClient.get {
        configureUrl(path)
        queryParams.forEach { (k, v) -> if (v != null) url.parameters.append(k, v) }
    }.body()

    /** Настраивает полный URL: baseUrl + path. */
    protected fun HttpRequestBuilder.configureUrl(path: String) {
        val parsed = parseBaseUrl(appSettings.baseUrl)
        url {
            protocol = parsed.protocol
            host = parsed.host
            port = parsed.port
            encodedPath = path
        }
    }
}
