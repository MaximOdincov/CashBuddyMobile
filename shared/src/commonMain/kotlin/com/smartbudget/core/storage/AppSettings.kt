package com.smartbudget.core.storage

import com.russhwolf.settings.Settings

/**
 * Локальное хранилище настроек приложения.
 * Хранит: токен авторизации, базовый URL API, выбранную тему.
 */
class AppSettings(private val settings: Settings) {

    var accessToken: String
        get() = settings.getString(KEY_TOKEN, "")
        set(value) = settings.putString(KEY_TOKEN, value)

    val isLoggedIn: Boolean get() = accessToken.isNotBlank()

    /** Базовый URL API. По умолчанию эмулятор Android (10.0.2.2 → localhost хоста). */
    var baseUrl: String
        get() = settings.getString(KEY_BASE_URL, DEFAULT_BASE_URL)
        set(value) = settings.putString(KEY_BASE_URL, value)

    /** Тема: "system" | "light" | "dark" */
    var themeMode: String
        get() = settings.getString(KEY_THEME, "system")
        set(value) = settings.putString(KEY_THEME, value)

    /** share-код для входа на втором устройстве (опционально, для удобства). */
    var lastShareCode: String
        get() = settings.getString(KEY_SHARE_CODE, "")
        set(value) = settings.putString(KEY_SHARE_CODE, value)

    /** Имя пользователя (для приветствия на Обзоре). */
    var username: String
        get() = settings.getString(KEY_USERNAME, "")
        set(value) = settings.putString(KEY_USERNAME, value)

    fun logout() {
        settings.remove(KEY_TOKEN)
        settings.remove(KEY_SHARE_CODE)
        settings.remove(KEY_USERNAME)
    }

    private companion object {
        const val KEY_TOKEN = "access_token"
        const val KEY_BASE_URL = "base_url"
        const val KEY_THEME = "theme_mode"
        const val KEY_SHARE_CODE = "share_code"
        const val KEY_USERNAME = "username"
        const val DEFAULT_BASE_URL = "http://217.114.1.73:8080"
    }
}
